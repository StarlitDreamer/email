package com.java.email.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.java.email.common.Result;
import com.java.email.model.ImportCategoryResponse;
import com.java.email.model.CategoryCreateRequest;
import com.java.email.model.CategoryFilterRequest;
import com.java.email.model.CategoryFilterResponse;
import com.java.email.model.CategoryVO;
import com.java.email.model.CategoryDeleteRequest;
import com.java.email.model.ImportCommodityResponse;
import com.java.email.model.CommodityCreateRequest;
import com.java.email.model.CommodityFilterRequest;
import com.java.email.model.CommodityFilterResponse;
import com.java.email.model.CommodityVO;
import com.java.email.model.CommodityDeleteRequest;
import com.java.email.model.CommodityUpdateRequest;
import com.java.email.model.CategoryUpdateRequest;
import com.java.email.service.CommodityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jakarta.annotation.PostConstruct;

@Service
public class CommodityServiceImpl implements CommodityService {

    @Autowired
    private ElasticsearchClient elasticsearchClient;

    @PostConstruct
    public void init() {
        createCategoryIndexIfNotExists();
        createCommodityIndexIfNotExists();
    }

    private void createIndexIfNotExists() {
        try {
            boolean exists = elasticsearchClient.indices().exists(e -> e.index("category_index")).value();
            if (!exists) {
                elasticsearchClient.indices().create(c -> c
                        .index("category_index")
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
        try {
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8)
            );

            int successCount = 0;
            int failCount = 0;
            String line;
            
            // 跳过CSV头行
            reader.readLine();
            
            // 读取每一行数据
            while ((line = reader.readLine()) != null) {
                try {
                    // 处理CSV行
                    String[] fields = line.split(",");
                    // TODO: 根据实际CSV格式处理数据
                    successCount++;
                } catch (Exception e) {
                    failCount++;
                }
            }

            // 构建响应数据
            ImportCategoryResponse response = new ImportCategoryResponse();
            response.setSuccess_count(successCount);
            response.setFail_count(failCount);

            return Result.success(response);
        } catch (Exception e) {
            return Result.error("导入失败：" + e.getMessage());
        }
    }

    @Override
    public Result<?> createCategory(CategoryCreateRequest request) {
        try {
            // 构建文档
            Map<String, Object> document = new HashMap<>();
            document.put("category_name", request.getCategory_name());

            // 保存到 Elasticsearch
            IndexResponse response = elasticsearchClient.index(i -> i
                    .index("category_index")
                    .document(document)
            );

            // 构建响应数据
            Map<String, Object> resultData = new HashMap<>();
            resultData.put("category_id", response.id());
            return Result.success(resultData);
        } catch (Exception e) {
            return Result.error("创建品类失败：" + e.getMessage());
        }
    }

    @Override
    public Result<?> filterCategory(CategoryFilterRequest request) {
        try {
            // 计算从第几条记录开始
            int from = (request.getPage_num() - 1) * request.getPage_size();

            // 构建搜索请求
            SearchResponse<Map> response = elasticsearchClient.search(s -> s
                    .index("category_index")
                    .query(q -> {
                        if (StringUtils.hasText(request.getCategory_name())) {
                            return q.term(t -> t
                                    .field("category_name.keyword")
                                    .value(request.getCategory_name())
                            );
                        }
                        return q.matchAll(ma -> ma);
                    })
                    .from(from)
                    .size(request.getPage_size()),
                    Map.class
            );

            // 构建响应对象
            CategoryFilterResponse filterResponse = new CategoryFilterResponse();
            filterResponse.setTotal_items(response.hits().total().value());
            filterResponse.setPage_num(request.getPage_num());
            filterResponse.setPage_size(request.getPage_size());

            // 转换搜索结果
            List<CategoryVO> categories = new ArrayList<>();
            for (Hit<Map> hit : response.hits().hits()) {
                CategoryVO category = new CategoryVO();
                category.setCategory_id(hit.id());
                category.setCategory_name((String) hit.source().get("category_name"));
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
            // 检查参数
            if (request.getCategory_id() == null || request.getCategory_id().trim().isEmpty()) {
                return Result.error("品类ID不能为空");
            }

            // 先检查文档是否存在
            boolean exists = elasticsearchClient.exists(e -> e
                    .index("category_index")
                    .id(request.getCategory_id())
            ).value();

            if (!exists) {
                return Result.error("品类不存在，ID: " + request.getCategory_id());
            }

            // 执行删除操作
            elasticsearchClient.delete(d -> d
                    .index("category_index")
                    .id(request.getCategory_id())
            );

            // 构建响应数据
            Map<String, Object> resultData = new HashMap<>();
            resultData.put("category_id", request.getCategory_id());
            return Result.success(resultData);
        } catch (Exception e) {
            return Result.error("删除品类失败：" + e.getMessage());
        }
    }

    @Override
    public Result<?> importCommodity(MultipartFile file) {
        try {
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8)
            );

            int successCount = 0;
            int failCount = 0;
            String line;
            
            // 跳过CSV头行
            reader.readLine();
            
            // 读取每一行数据
            while ((line = reader.readLine()) != null) {
                try {
                    // 处理CSV行
                    String[] fields = line.split(",");
                    if (fields.length >= 2) {  // 假设CSV至少包含商品名称和品类ID
                        Map<String, Object> document = new HashMap<>();
                        document.put("commodity_name", fields[0].trim());
                        document.put("category_id", fields[1].trim());
                        
                        // 保存到 Elasticsearch
                        elasticsearchClient.index(i -> i
                                .index("commodity_index")
                                .document(document)
                        );
                        
                        successCount++;
                    } else {
                        failCount++;
                    }
                } catch (Exception e) {
                    failCount++;
                }
            }

            // 构建响应数据
            ImportCommodityResponse response = new ImportCommodityResponse();
            response.setSuccess_count(successCount);
            response.setFail_count(failCount);

            return Result.success(response);
        } catch (Exception e) {
            return Result.error("导入商品失败：" + e.getMessage());
        }
    }

    private void createCategoryIndexIfNotExists() {
        try {
            boolean exists = elasticsearchClient.indices().exists(e -> e.index("category_index")).value();
            if (!exists) {
                elasticsearchClient.indices().create(c -> c
                        .index("category_index")
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
    public Result<?> createCommodity(CommodityCreateRequest request) {
        try {
            // 检查参数
            if (request.getCommodity_name() == null || request.getCommodity_name().trim().isEmpty()) {
                return Result.error("商品名称不能为空");
            }
            if (request.getCategory_id() == null || request.getCategory_id().trim().isEmpty()) {
                return Result.error("品类ID不能为空");
            }

            // 检查品类是否存在
            boolean exists = elasticsearchClient.exists(e -> e
                    .index("category_index")
                    .id(request.getCategory_id())
            ).value();

            if (!exists) {
                return Result.error("品类不存在，ID: " + request.getCategory_id());
            }

            // 构建文档
            Map<String, Object> document = new HashMap<>();
            document.put("commodity_name", request.getCommodity_name());
            document.put("category_id", request.getCategory_id());

            // 保存到 Elasticsearch
            IndexResponse response = elasticsearchClient.index(i -> i
                    .index("commodity_index")
                    .document(document)
            );

            // 构建响应数据
            Map<String, Object> resultData = new HashMap<>();
            resultData.put("commodity_id", response.id());
            return Result.success(resultData);
        } catch (Exception e) {
            return Result.error("创建商品失败：" + e.getMessage());
        }
    }

    @Override
    public Result<?> filterCommodity(CommodityFilterRequest request) {
        try {
            // 计算从第几条记录开始
            int from = (request.getPage_num() - 1) * request.getPage_size();

            // 检查索引是否存在，不存在则返回空结果
            boolean exists = elasticsearchClient.indices().exists(e -> e
                    .index("commodity_index")
            ).value();

            if (!exists) {
                // 返回空结果但保持数据结构一致
                CommodityFilterResponse emptyResponse = new CommodityFilterResponse();
                emptyResponse.setTotal_items(0L);
                emptyResponse.setPage_num(request.getPage_num());
                emptyResponse.setPage_size(request.getPage_size());
                emptyResponse.setCommodity(new ArrayList<>());
                return Result.success(emptyResponse);
            }

            // 构建搜索请求
            SearchResponse<Map> response = elasticsearchClient.search(s -> s
                    .index("commodity_index")
                    .query(q -> {
                        if (StringUtils.hasText(request.getCommodity_name()) && StringUtils.hasText(request.getCategory_id())) {
                            return q.bool(b -> b
                                    .must(m -> m
                                            .term(t -> t
                                                    .field("commodity_name.keyword")
                                                    .value(request.getCommodity_name())
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
                            return q.term(t -> t
                                    .field("commodity_name.keyword")
                                    .value(request.getCommodity_name())
                            );
                        } else if (StringUtils.hasText(request.getCategory_id())) {
                            return q.term(t -> t
                                    .field("category_id")
                                    .value(request.getCategory_id())
                            );
                        }
                        return q.matchAll(ma -> ma);
                    })
                    .from(from)
                    .size(request.getPage_size()),
                    Map.class
            );

            // 构建响应对象
            CommodityFilterResponse filterResponse = new CommodityFilterResponse();
            filterResponse.setTotal_items(response.hits().total().value());
            filterResponse.setPage_num(request.getPage_num());
            filterResponse.setPage_size(request.getPage_size());

            // 转换搜索结果
            List<CommodityVO> commodities = new ArrayList<>();
            for (Hit<Map> hit : response.hits().hits()) {
                CommodityVO commodity = new CommodityVO();
                commodity.setCommodity_id(hit.id());
                commodity.setCommodity_name((String) hit.source().get("commodity_name"));
                
                // 获取品类名称
                String categoryId = (String) hit.source().get("category_id");
                try {
                    Map<String, Object> category = elasticsearchClient.get(g -> g
                            .index("category_index")
                            .id(categoryId),
                            Map.class
                    ).source();
                    if (category != null) {
                        commodity.setCategory_name((String) category.get("category_name"));
                    }
                } catch (Exception e) {
                    // 如果获取品类失败，设置为空
                    commodity.setCategory_name("");
                }
                
                commodities.add(commodity);
            }
            filterResponse.setCommodity(commodities);

            return Result.success(filterResponse);
        } catch (Exception e) {
            // 出错时也要保持数据结构一致
            CommodityFilterResponse errorResponse = new CommodityFilterResponse();
            errorResponse.setTotal_items(0L);
            errorResponse.setPage_num(request.getPage_num() != null ? request.getPage_num() : 1);
            errorResponse.setPage_size(request.getPage_size() != null ? request.getPage_size() : 10);
            errorResponse.setCommodity(new ArrayList<>());
            return Result.success(errorResponse);
        }
    }

    private void createCommodityIndexIfNotExists() {
        try {
            boolean exists = elasticsearchClient.indices().exists(e -> e.index("commodity_index")).value();
            if (!exists) {
                elasticsearchClient.indices().create(c -> c
                        .index("commodity_index")
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
                                .properties("category_name", p -> p
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
    public Result<?> deleteCommodity(CommodityDeleteRequest request) {
        try {
            // 检查参数
            if (request.getCommodity_id() == null || request.getCommodity_id().trim().isEmpty()) {
                return Result.error("商品ID不能为空");
            }

            // 先检查文档是否存在
            boolean exists = elasticsearchClient.exists(e -> e
                    .index("commodity_index")
                    .id(request.getCommodity_id())
            ).value();

            if (!exists) {
                return Result.error("商品不存在，ID: " + request.getCommodity_id());
            }

            // 执行删除操作
            elasticsearchClient.delete(d -> d
                    .index("commodity_index")
                    .id(request.getCommodity_id())
            );

            // 构建响应数据
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
            // 检查参数
            if (request.getCommodity_id() == null || request.getCommodity_id().trim().isEmpty()) {
                return Result.error("商品ID不能为空");
            }
            if (request.getCommodity_name() == null || request.getCommodity_name().trim().isEmpty()) {
                return Result.error("商品名称不能为空");
            }
            if (request.getCategory_id() == null || request.getCategory_id().trim().isEmpty()) {
                return Result.error("品类ID不能为空");
            }

            // 先检查商品是否存在
            boolean commodityExists = elasticsearchClient.exists(e -> e
                    .index("commodity_index")
                    .id(request.getCommodity_id())
            ).value();

            if (!commodityExists) {
                return Result.error("商品不存在，ID: " + request.getCommodity_id());
            }

            // 检查品类是否存在
            boolean categoryExists = elasticsearchClient.exists(e -> e
                    .index("category_index")
                    .id(request.getCategory_id())
            ).value();

            if (!categoryExists) {
                return Result.error("品类不存在，ID: " + request.getCategory_id());
            }

            // 构建更新文档
            Map<String, Object> document = new HashMap<>();
            document.put("commodity_name", request.getCommodity_name());
            document.put("category_id", request.getCategory_id());

            // 执行更新操作
            elasticsearchClient.update(u -> u
                    .index("commodity_index")
                    .id(request.getCommodity_id())
                    .doc(document),
                    Map.class
            );

            // 构建响应数据
            Map<String, Object> resultData = new HashMap<>();
            resultData.put("commodity_id", request.getCommodity_id());
            return Result.success(resultData);
        } catch (Exception e) {
            return Result.error("更新商品失败：" + e.getMessage());
        }
    }

    @Override
    public Result<?> updateCategory(CategoryUpdateRequest request) {
        try {
            // 检查参数
            if (request.getCategory_id() == null || request.getCategory_id().trim().isEmpty()) {
                return Result.error("品类ID不能为空");
            }
            if (request.getCategory_name() == null || request.getCategory_name().trim().isEmpty()) {
                return Result.error("品类名称不能为空");
            }

            // 先检查品类是否存在
            boolean exists = elasticsearchClient.exists(e -> e
                    .index("category_index")
                    .id(request.getCategory_id())
            ).value();

            if (!exists) {
                return Result.error("品类不存在，ID: " + request.getCategory_id());
            }

            // 构建更新文档
            Map<String, Object> document = new HashMap<>();
            document.put("category_name", request.getCategory_name());

            // 执行更新操作
            elasticsearchClient.update(u -> u
                    .index("category_index")
                    .id(request.getCategory_id())
                    .doc(document),
                    Map.class
            );

            // 构建响应数据
            Map<String, Object> resultData = new HashMap<>();
            resultData.put("category_id", request.getCategory_id());
            return Result.success(resultData);
        } catch (Exception e) {
            return Result.error("更新品类失败：" + e.getMessage());
        }
    }
} 