package com.java.email.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.java.email.common.Result;
import com.java.email.model.*;
import com.java.email.service.CategoryService;
import com.java.email.service.CommodityService;
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
public class CommodityServiceImpl implements CommodityService {

    private static final Logger log = LoggerFactory.getLogger(CommodityServiceImpl.class);

    @Autowired
    private ElasticsearchClient elasticsearchClient;

    @Autowired
    private CategoryService categoryService;  // 注入 CategoryService

    @PostConstruct
    public void init() {
        createCommodityIndexIfNotExists();
    }

    private void createCommodityIndexIfNotExists() {
        try {
            boolean exists = elasticsearchClient.indices().exists(e -> e.index("commodity")).value();
            if (!exists) {
                elasticsearchClient.indices().create(c -> c
                        .index("commodity")
                        .mappings(m -> m
                                .properties("commodity_name", p -> p
                                        .text(t -> t
                                                .fields("keyword", k -> k
                                                        .keyword(kw -> kw)
                                                )
                                        )
                                )
                                .properties("category_id", p -> p
                                        .keyword(k -> k)
                                )
                        )
                );
            }
        } catch (Exception e) {
            // 处理异常
        }
    }

    @Override
    public Result<?> importCommodity(MultipartFile file) {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {
            
            int successCount = 0;
            int failCount = 0;
            List<String> errorMsg = new ArrayList<>();
            for (CSVRecord record : csvParser) {
                try {
                    String commodityName = record.get(0).trim().replace("\"", "");
                    String categoryName = record.get(1).trim().replace("\"", "");
                    
                    // 1. 检查商品名是否重复
                    SearchResponse<Map> commodityResponse = elasticsearchClient.search(s -> s
                            .index("commodity")
                            .query(q -> q
                                    .term(t -> t
                                            .field("commodity_name.keyword")
                                            .value(commodityName)
                                    )
                            ),
                            Map.class
                    );
                    
                    if (commodityResponse.hits().total().value() > 0) {
                        log.warn("找到重复的商品名称: {}", commodityName);
                        errorMsg.add("找到重复的商品名称: " + commodityName);
                        failCount++;
                        continue;
                    }
                    
                    // 2. 通过品类名称查找品类ID
                    SearchResponse<Map> categoryResponse = elasticsearchClient.search(s -> s
                            .index("category")
                            .query(q -> q
                                    .term(t -> t
                                            .field("category_name.keyword")
                                            .value(categoryName)
                                    )
                            ),
                            Map.class
                    );
                    
                    if (categoryResponse.hits().total().value() == 0) {
                        log.warn("未找到品类: {}", categoryName);
                        errorMsg.add("未找到品类: " + categoryName);
                        failCount++;
                        continue;
                    }
                    
                    String categoryId = categoryResponse.hits().hits().get(0).id();
                    String commodityId = UUID.randomUUID().toString();
                    // 3. 插入商品记录
                    Map<String, Object> document = new HashMap<>();
                    document.put("commodity_id", commodityId);
                    document.put("commodity_name", commodityName);
                    document.put("category_id", categoryId);
                    
                    String now = java.time.Instant.now().toString();
                    document.put("created_at", now);
                    document.put("updated_at", now);
                    
                    elasticsearchClient.index(i -> i
                            .index("commodity")
                            .id(commodityId)
                            .document(document)
                    );
                    successCount++;
                    
                } catch (Exception e) {
                    log.error("处理记录失败: {}", record, e);
                    failCount++;
                }
            }

            ImportCommodityResponse response = new ImportCommodityResponse();
            response.setSuccess_count(successCount);
            response.setFail_count(failCount);
            response.setErrorMsg(errorMsg);
            return Result.success(response);
        } catch (Exception e) {
            return Result.error("导入商品失败: " + e.getMessage());
        }
    }

    @Override
    public Result<?> createCommodity(CommodityCreateRequest request) {
        try {
            // 检查品类是否存在
            boolean categoryExists = elasticsearchClient.exists(e -> e
                    .index("category")
                    .id(request.getCategory_id())
            ).value();

            if (!categoryExists) {
                return Result.error("品类不存在，ID: " + request.getCategory_id());
            }

            // 检查商品名是否重复
            SearchResponse<Map> commodityResponse = elasticsearchClient.search(s -> s
                    .index("commodity")
                    .query(q -> q
                            .term(t -> t
                                    .field("commodity_name.keyword")
                                    .value(request.getCommodity_name())
                            )
                    ),
                    Map.class
            );

            if (commodityResponse.hits().total().value() > 0) {
                log.warn("找到重复的商品名称: {}", request.getCommodity_name());
                return Result.error("商品名称已存在");
            }

            String now = java.time.Instant.now().toString();

            Map<String, Object> document = new HashMap<>();
            String commodityId = UUID.randomUUID().toString();
            document.put("commodity_id", commodityId);
            document.put("commodity_name", request.getCommodity_name());
            document.put("category_id", request.getCategory_id());
            document.put("created_at", now);
            document.put("updated_at", now);

            IndexResponse response = elasticsearchClient.index(i -> i
                    .index("commodity")
                    .id(commodityId)
                    .document(document)
            );

            Map<String, Object> resultData = new HashMap<>();
            resultData.put("commodity_id", commodityId);
            return Result.success(resultData);
        } catch (Exception e) {
            return Result.error("创建商品失败：" + e.getMessage());
        }
    }

    @Override
    public Result<?> filterCommodity(CommodityFilterRequest request) {
        try {
            int from = (request.getPage_num() - 1) * request.getPage_size();

            SearchResponse<Map> response = elasticsearchClient.search(s -> s
                    .index("commodity")
                    .query(q -> {
                        if (StringUtils.hasText(request.getCommodity_name()) && StringUtils.hasText(request.getCategory_id())) {
                            return q.bool(b -> b
                                    .must(m -> m
                                            .match(t -> t
                                                    .field("commodity_name")
                                                    .query(request.getCommodity_name())
                                            )
                                    )
                                    .must(m -> m
                                            .term(t -> t
                                                    .field("category_id")
                                                    .value(request.getCategory_id())
                                            )
                                    )
                            );
                        } else if (StringUtils.hasText(request.getCommodity_name())) {
                            return q.match(t -> t
                                    .field("commodity_name")
                                    .query(request.getCommodity_name())
                            );
                        } else if (StringUtils.hasText(request.getCategory_id())) {
                            return q.term(t -> t
                                    .field("category_id")
                                    .value(request.getCategory_id())
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

            CommodityFilterResponse filterResponse = new CommodityFilterResponse();
            filterResponse.setTotal_items(response.hits().total().value());
            filterResponse.setPage_num(request.getPage_num());
            filterResponse.setPage_size(request.getPage_size());

            List<CommodityVO> commodities = new ArrayList<>();
            for (Hit<Map> hit : response.hits().hits()) {
                CommodityVO commodity = new CommodityVO();
                commodity.setCommodity_id((String) hit.source().get("commodity_id"));
                commodity.setCommodity_name((String) hit.source().get("commodity_name"));
                commodity.setCategory_id((String) hit.source().get("category_id"));
                commodity.setCreated_at((String) hit.source().get("created_at"));
                commodity.setUpdated_at((String) hit.source().get("updated_at"));

                // 获取品类名称
                String categoryId = (String) hit.source().get("category_id");
                Map<String, Object> category = elasticsearchClient.get(g -> g
                        .index("category")
                        .id(categoryId),
                        Map.class
                ).source();
                
                if (category != null) {
                    commodity.setCategory_name((String) category.get("category_name"));
                }

                commodities.add(commodity);
            }
            filterResponse.setCommodity(commodities);

            return Result.success(filterResponse);
        } catch (Exception e) {
            return Result.error("搜索商品失败：" + e.getMessage());
        }
    }

    @Override
    public Result<?> deleteCommodity(CommodityDeleteRequest request) {
        try {
            if (request.getCommodity_id() == null || request.getCommodity_id().trim().isEmpty()) {
                return Result.error("商品ID不能为空");
            }

            boolean exists = elasticsearchClient.exists(e -> e
                    .index("commodity")
                    .id(request.getCommodity_id())
            ).value();

            if (!exists) {
                return Result.error("商品不存在，ID: " + request.getCommodity_id());
            }

            elasticsearchClient.delete(d -> d
                    .index("commodity")
                    .id(request.getCommodity_id())
            );

            Map<String, Object> resultData = new HashMap<>();
            resultData.put("commodity_id", request.getCommodity_id());
            return Result.success(resultData);
        } catch (Exception e) {
            return Result.error("删除商品失败：" + e.getMessage());
        }
    }

    @Override
    public Result<?> updateCommodity(CommodityUpdateRequest request) {
        try {
            if (request.getCommodity_id() == null || request.getCommodity_id().trim().isEmpty()) {
                return Result.error("商品ID不能为空");
            }

            // 先检查商品是否存在
            boolean commodityExists = elasticsearchClient.exists(e -> e
                    .index("commodity")
                    .id(request.getCommodity_id())
            ).value();

            if (!commodityExists) {
                return Result.error("商品不存在，ID: " + request.getCommodity_id());
            }

            // 如果传了商品名，检查商品名是否重复
            if (request.getCommodity_name() != null && !request.getCommodity_name().trim().isEmpty()) {
                SearchResponse<Map> commodityResponse = elasticsearchClient.search(s -> s
                        .index("commodity")
                        .query(q -> q
                            .term(t -> t
                                    .field("commodity_name.keyword")
                                    .value(request.getCommodity_name())
                            )
                        ),
                        Map.class
                );
                // 如果查询结果不为空，且查询结果的id与当前商品id不一致，则商品名称已存在
                if (commodityResponse.hits().total().value() > 0) {
                    String existingId = commodityResponse.hits().hits().get(0).id();
                    if (!existingId.equals(request.getCommodity_id())) {
                        return Result.error("商品名称已存在");
                    }
                }
            }
            
            // 如果传了品类id，检查品类是否存在
            if (request.getCategory_id() != null && !request.getCategory_id().trim().isEmpty()) {
                boolean categoryExists = elasticsearchClient.exists(e -> e
                        .index("category")
                        .id(request.getCategory_id())
                        ).value();

                if (!categoryExists) {
                    return Result.error("品类不存在，ID: " + request.getCategory_id());
                }
            }

            String now = java.time.Instant.now().toString();

            Map<String, Object> document = new HashMap<>();
            document.put("commodity_name", request.getCommodity_name());
            if (request.getCategory_id() != null && !request.getCategory_id().trim().isEmpty()) {
                document.put("category_id", request.getCategory_id());
            }
            document.put("updated_at", now);

            elasticsearchClient.update(u -> u
                    .index("commodity")
                    .id(request.getCommodity_id())
                    .doc(document),
                    Map.class
            );

            Map<String, Object> resultData = new HashMap<>();
            resultData.put("commodity_id", request.getCommodity_id());
            return Result.success(resultData);
        } catch (Exception e) {
            return Result.error("更新商品失败：" + e.getMessage());
        }
    }
} 