package com.java.email.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CommodityServiceImpl implements CommodityService {

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
        try {
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8)
            );

            int successCount = 0;
            int failCount = 0;
            String line;
            
            // 跳过CSV头行
            reader.readLine();
            
            while ((line = reader.readLine()) != null) {
                try {
                    String[] fields = line.split(",");
                    if (fields.length >= 2) {  // 假设CSV包含商品名称和品类ID
                        // 检查品类是否存在
                        String categoryId = fields[1].trim();
                        boolean categoryExists = elasticsearchClient.exists(e -> e
                                .index("category")
                                .id(categoryId)
                        ).value();

                        if (!categoryExists) {
                            failCount++;
                            continue;
                        }

                        Map<String, Object> document = new HashMap<>();
                        document.put("commodity_name", fields[0].trim());
                        document.put("category_id", categoryId);
                        
                        // 获取当前时间的 ISO 格式字符串
                        String now = java.time.Instant.now().toString();
                        document.put("created_at", now);
                        document.put("updated_at", now);
                        
                        elasticsearchClient.index(i -> i
                                .index("commodity")
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

            ImportCommodityResponse response = new ImportCommodityResponse();
            response.setSuccess_count(successCount);
            response.setFail_count(failCount);
            return Result.success(response);
        } catch (Exception e) {
            return Result.error("导入商品失败：" + e.getMessage());
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

            String now = java.time.Instant.now().toString();

            Map<String, Object> document = new HashMap<>();
            document.put("commodity_name", request.getCommodity_name());
            document.put("category_id", request.getCategory_id());
            document.put("created_at", now);
            document.put("updated_at", now);

            IndexResponse response = elasticsearchClient.index(i -> i
                    .index("commodity")
                    .document(document)
            );

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
            int from = (request.getPage_num() - 1) * request.getPage_size();

            SearchResponse<Map> response = elasticsearchClient.search(s -> s
                    .index("commodity")
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

            CommodityFilterResponse filterResponse = new CommodityFilterResponse();
            filterResponse.setTotal_items(response.hits().total().value());
            filterResponse.setPage_num(request.getPage_num());
            filterResponse.setPage_size(request.getPage_size());

            List<CommodityVO> commodities = new ArrayList<>();
            for (Hit<Map> hit : response.hits().hits()) {
                CommodityVO commodity = new CommodityVO();
                commodity.setCommodity_id(hit.id());
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

            // 检查品类是否存在
            boolean categoryExists = elasticsearchClient.exists(e -> e
                    .index("category")
                    .id(request.getCategory_id())
            ).value();

            if (!categoryExists) {
                return Result.error("品类不存在，ID: " + request.getCategory_id());
            }

            String now = java.time.Instant.now().toString();

            Map<String, Object> document = new HashMap<>();
            document.put("commodity_name", request.getCommodity_name());
            document.put("category_id", request.getCategory_id());
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