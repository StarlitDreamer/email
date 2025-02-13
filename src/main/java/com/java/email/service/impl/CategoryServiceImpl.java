package com.java.email.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch._types.SortOrder;
import com.java.email.common.Result;
import com.java.email.model.*;
import com.java.email.service.CategoryService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class CategoryServiceImpl implements CategoryService {

    private static final Logger log = LoggerFactory.getLogger(CategoryServiceImpl.class);

    @Autowired
    private ElasticsearchClient elasticsearchClient;

    @PostConstruct
    public void init() {
        createCategoryIndexIfNotExists();
    }

    private void createCategoryIndexIfNotExists() {
        try {
            boolean exists = elasticsearchClient.indices().exists(e -> e.index("category")).value();
            if (!exists) {
                elasticsearchClient.indices().create(c -> c
                        .index("category")
                        .mappings(m -> m
                                .properties("category_name", p -> p
                                        .text(t -> t
                                                .fields("keyword", k -> k
                                                        .keyword(kw -> kw)
                                                )
                                        )
                                )
                        )
                );
            }
        } catch (Exception e) {
            // 处理异常
        }
    }

    @Override
    public Result<?> importCategory(MultipartFile file) {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {
            
            int successCount = 0;
            int failCount = 0;

            for (CSVRecord record : csvParser) {
                try {
                    String categoryName = record.get(0).trim().replace("\"", "");
                    log.info("检查品类名称是否已存在: {}", categoryName);
                    
                    // 检查品类名称是否已存在
                    SearchResponse<Map> searchResponse = elasticsearchClient.search(s -> s
                            .index("category")
                            .query(q -> q
                                    .term(t -> t
                                            .field("category_name.keyword")
                                            .value(categoryName)
                                    )
                            ),
                            Map.class
                    );

                    long total = searchResponse.hits().total().value();
                    log.info("找到 {} 个匹配的品类: {}", total, categoryName);

                    if (total > 0) {
                        log.warn("找到重复的品类: {}", categoryName);
                        failCount++;
                        continue;
                    }

                    // 构建文档
                    Map<String, Object> document = new HashMap<>();
                    String categoryId = UUID.randomUUID().toString();
                    document.put("category_id", categoryId);
                    document.put("category_name", categoryName);
                    
                    String now = java.time.Instant.now().toString();
                    document.put("created_at", now);
                    document.put("updated_at", now);
                    
                    elasticsearchClient.index(i -> i
                            .index("category")
                            .id(categoryId)
                            .document(document)
                    );
                    successCount++;
                    
                } catch (Exception e) {
                    log.error("处理记录失败: {}", record, e);
                    failCount++;
                }
            }

            ImportCategoryResponse response = new ImportCategoryResponse();
            response.setSuccess_count(successCount);
            response.setFail_count(failCount);
            return Result.success(response);
        } catch (Exception e) {
            return Result.error("导入品类失败：" + e.getMessage());
        }
    }

    @Override
    public Result<?> createCategory(CategoryCreateRequest request) {
        try {
            // 检查是否已存在相同的category_name
            SearchResponse<Map> searchResponse = elasticsearchClient.search(s -> s
                    .index("category")
                    .query(q -> q
                            .term(t -> t
                                    .field("category_name.keyword")
                                    .value(request.getCategory_name())
                            )
                    ),
                    Map.class
            );

            if (searchResponse.hits().total().value() > 0) {
                return Result.error("品类名称已存在");
            }
            // 获取当前时间的 ISO 格式字符串
            String now = java.time.Instant.now().toString();

            Map<String, Object> document = new HashMap<>();
            String categoryId = UUID.randomUUID().toString();
            document.put("category_id", categoryId);
            document.put("category_name", request.getCategory_name());
            document.put("created_at", now);
            document.put("updated_at", now);
            IndexResponse response = elasticsearchClient.index(i -> i
                    .index("category")
                    .id(categoryId)
                    .document(document)
            );

            Map<String, Object> resultData = new HashMap<>();
            resultData.put("category_id", categoryId);
            return Result.success(resultData);
        } catch (Exception e) {
            return Result.error("创建品类失败：" + e.getMessage());
        }
    }

    @Override
    public Result<?> filterCategory(CategoryFilterRequest request) {
        try {
            int from = (request.getPage_num() - 1) * request.getPage_size();

            SearchResponse<Map> response = elasticsearchClient.search(s -> s
                    .index("category")
                    .query(q -> {
                        if (StringUtils.hasText(request.getCategory_name())) {
                            return q.match(t -> t
                                    .field("category_name")
                                    .query(request.getCategory_name())
                            );
                        }
                        return q.matchAll(ma -> ma);
                    })
                    .sort(sort -> sort
                            .field(f -> f
                                    .field("updated_at")
                                    .order(SortOrder.Desc)
                            )
                    )
                    .from(from)
                    .size(request.getPage_size()),
                    Map.class
            );

            CategoryFilterResponse filterResponse = new CategoryFilterResponse();
            filterResponse.setTotal_items(response.hits().total().value());
            filterResponse.setPage_num(request.getPage_num());
            filterResponse.setPage_size(request.getPage_size());

            List<CategoryVO> categories = new ArrayList<>();
            for (Hit<Map> hit : response.hits().hits()) {
                CategoryVO category = new CategoryVO();
                category.setCategory_id((String) hit.source().get("category_id"));
                category.setCategory_name((String) hit.source().get("category_name"));
                category.setCreated_at((String) hit.source().get("created_at"));
                category.setUpdated_at((String) hit.source().get("updated_at"));
                categories.add(category);
            }
            filterResponse.setCategory(categories);

            return Result.success(filterResponse);
        } catch (Exception e) {
            return Result.error("搜索品类失败：" + e.getMessage());
        }
    }

    @Override
    public Result<?> deleteCategory(CategoryDeleteRequest request) {
        try {
            if (request.getCategory_id() == null || request.getCategory_id().trim().isEmpty()) {
                return Result.error("品类ID不能为空");
            }

            boolean exists = elasticsearchClient.exists(e -> e
                    .index("category")
                    .id(request.getCategory_id())
            ).value();

            if (!exists) {
                return Result.error("品类不存在，ID: " + request.getCategory_id());
            }

            elasticsearchClient.delete(d -> d
                    .index("category")
                    .id(request.getCategory_id())
            );

            Map<String, Object> resultData = new HashMap<>();
            resultData.put("category_id", request.getCategory_id());
            return Result.success(resultData);
        } catch (Exception e) {
            return Result.error("删除品类失败：" + e.getMessage());
        }
    }

    @Override
    public Result<?> updateCategory(CategoryUpdateRequest request) {
        try {
            if (request.getCategory_id() == null || request.getCategory_id().trim().isEmpty()) {
                return Result.error("品类ID不能为空");
            }
            if (request.getCategory_name() == null || request.getCategory_name().trim().isEmpty()) {
                return Result.error("品类名称不能为空");
            }

            boolean exists = elasticsearchClient.exists(e -> e
                    .index("category")
                    .id(request.getCategory_id())
            ).value();

            if (!exists) {
                return Result.error("品类不存在，ID: " + request.getCategory_id());
            }

            // 检查品类名称是否重复
            SearchResponse<Map> categoryResponse = elasticsearchClient.search(s -> s
                    .index("category")
                    .query(q -> q
                            .term(t -> t
                                    .field("category_name.keyword")
                                    .value(request.getCategory_name())
                            )
                    ),
                    Map.class
            );
            if (categoryResponse.hits().total().value() > 0) {
                return Result.error("品类名称已存在");
            }

            // 获取当前时间的 ISO 格式字符串
            String now = java.time.Instant.now().toString();

            Map<String, Object> document = new HashMap<>();
            document.put("category_name", request.getCategory_name());
            document.put("updated_at", now);

            elasticsearchClient.update(u -> u
                    .index("category")
                    .id(request.getCategory_id())
                    .doc(document),
                    Map.class
            );

            Map<String, Object> resultData = new HashMap<>();
            resultData.put("category_id", request.getCategory_id());
            return Result.success(resultData);
        } catch (Exception e) {
            return Result.error("更新品类失败：" + e.getMessage());
        }
    }
} 