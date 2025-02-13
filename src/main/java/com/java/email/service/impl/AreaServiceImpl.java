package com.java.email.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.java.email.common.Result;
import com.java.email.model.AreaCreateRequest;
import com.java.email.model.AreaFilterRequest;
import com.java.email.model.AreaFilterResponse;
import com.java.email.model.AreaVO;
import com.java.email.model.AreaDeleteRequest;
import com.java.email.model.AreaUpdateRequest;
import com.java.email.service.AreaService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class AreaServiceImpl implements AreaService {

    @Autowired
    private ElasticsearchClient elasticsearchClient;

    @PostConstruct
    public void init() {
        createAreaIndexIfNotExists();
    }

    private void createAreaIndexIfNotExists() {
        try {
            boolean exists = elasticsearchClient.indices().exists(e -> e.index("area")).value();
            if (!exists) {
                elasticsearchClient.indices().create(c -> c
                        .index("area")
                        .mappings(m -> m
                                .properties("area_name", p -> p
                                        .text(t -> t
                                                .fields("keyword", k -> k
                                                        .keyword(kw -> kw)
                                                )
                                        )
                                )
                                .properties("country_id", p -> p
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
    public Result<?> createArea(AreaCreateRequest request) {
        try {
            // 获取当前时间的 ISO 格式字符串
            String now = java.time.Instant.now().toString();

            // 检查区域名是否重复
            SearchResponse<Map> areaResponse = elasticsearchClient.search(s -> s
                    .index("area")
                    .query(q -> q
                            .term(t -> t
                                    .field("area_name.keyword")
                                    .value(request.getArea_name())
                            )
                    ),
                    Map.class
            );
            if (areaResponse.hits().total().value() > 0) {
                return Result.error("区域名称已存在");
            }
            // 构建文档
            Map<String, Object> document = new HashMap<>();
            String areaId = UUID.randomUUID().toString();
            document.put("area_id", areaId);
            document.put("area_name", request.getArea_name());
            document.put("area_country", request.getArea_country());
            document.put("created_at", now);
            document.put("updated_at", now);

            // 保存到 Elasticsearch
            IndexResponse response = elasticsearchClient.index(i -> i
                    .index("area")
                    .id(areaId)
                    .document(document)
            );

            // 构建响应数据
            Map<String, Object> resultData = new HashMap<>();
            resultData.put("area_id", areaId);
            return Result.success(resultData);
        } catch (Exception e) {
            return Result.error("创建区域失败：" + e.getMessage());
        }
    }

    @Override
    public Result<?> filterArea(AreaFilterRequest request) {
        try {
            // 计算从第几条记录开始
            int from = (request.getPage_num() - 1) * request.getPage_size();

            // 检查索引是否存在，不存在则返回空结果
            boolean exists = elasticsearchClient.indices().exists(e -> e
                    .index("area")
            ).value();

            if (!exists) {
                // 返回空结果但保持数据结构一致
                AreaFilterResponse emptyResponse = new AreaFilterResponse();
                emptyResponse.setTotal_items(0L);
                emptyResponse.setPage_num(request.getPage_num());
                emptyResponse.setPage_size(request.getPage_size());
                emptyResponse.setArea(new ArrayList<>());
                return Result.success(emptyResponse);
            }

            // 构建搜索请求
            SearchResponse<Map> response = elasticsearchClient.search(s -> s
                    .index("area")
                    .query(q -> {
                        if (StringUtils.hasText(request.getArea_name()) && StringUtils.hasText(request.getCountry_id())) {
                            return q.bool(b -> b
                                    .must(m -> m
                                            .match(t -> t
                                                    .field("area_name")
                                                    .query(request.getArea_name())
                                            )
                                    )
                                    .must(m -> m
                                            .term(t -> t
                                                    .field("area_country")
                                                    .value(request.getCountry_id())
                                            )
                                    )
                            );
                        } else if (StringUtils.hasText(request.getArea_name())) {
                            return q.match(t -> t
                                    .field("area_name")
                                    .query(request.getArea_name())
                            );
                        } else if (StringUtils.hasText(request.getCountry_id())) {
                            return q.term(t -> t
                                    .field("area_country")
                                    .value(request.getCountry_id())
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

            // 构建响应对象
            AreaFilterResponse filterResponse = new AreaFilterResponse();
            filterResponse.setTotal_items(response.hits().total().value());
            filterResponse.setPage_num(request.getPage_num());
            filterResponse.setPage_size(request.getPage_size());

            // 转换搜索结果
            List<AreaVO> areas = new ArrayList<>();
            for (Hit<Map> hit : response.hits().hits()) {
                AreaVO area = new AreaVO();
                area.setArea_id((String) hit.source().get("area_id"));
                area.setArea_name((String) hit.source().get("area_name"));
                List<String> countryIds = (List<String>) hit.source().get("area_country");
                List<String> countryNames = new ArrayList<>();
                for (String countryId : countryIds) {
                    Map<String, Object> country = elasticsearchClient.get(g -> g
                            .index("country")
                            .id(countryId),
                            Map.class
                    ).source();
                    if (country != null) {
                        countryNames.add((String) country.get("country_name"));
                    }
                }
                area.setArea_country(countryNames);
                area.setCreated_at((String) hit.source().get("created_at"));
                area.setUpdated_at((String) hit.source().get("updated_at"));
                areas.add(area);
            }
            filterResponse.setArea(areas);

            return Result.success(filterResponse);
        } catch (Exception e) {
            // 出错时也要保持数据结构一致
            AreaFilterResponse errorResponse = new AreaFilterResponse();
            errorResponse.setTotal_items(0L);
            errorResponse.setPage_num(request.getPage_num() != null ? request.getPage_num() : 1);
            errorResponse.setPage_size(request.getPage_size() != null ? request.getPage_size() : 10);
            errorResponse.setArea(new ArrayList<>());
            return Result.success(errorResponse);
        }
    }

    @Override
    public Result<?> deleteArea(AreaDeleteRequest request) {
        try {
            // 检查参数
            if (request.getArea_id() == null || request.getArea_id().trim().isEmpty()) {
                return Result.error("区域ID不能为空");
            }

            // 先检查文档是否存在
            boolean exists = elasticsearchClient.exists(e -> e
                    .index("area")
                    .id(request.getArea_id())
            ).value();

            if (!exists) {
                return Result.error("区域不存在，ID: " + request.getArea_id());
            }

            // 执行删除操作
            elasticsearchClient.delete(d -> d
                    .index("area")
                    .id(request.getArea_id())
            );

            // 构建响应数据
            Map<String, Object> resultData = new HashMap<>();
            resultData.put("area_id", request.getArea_id());
            return Result.success(resultData);
        } catch (Exception e) {
            return Result.error("删除区域失败：" + e.getMessage());
        }
    }

    @Override
    public Result<?> updateArea(AreaUpdateRequest request) {
        try {
            // 检查参数
            if (request.getArea_id() == null || request.getArea_id().trim().isEmpty()) {
                return Result.error("区域ID不能为空");
            }

            // 先检查区域是否存在
            boolean exists = elasticsearchClient.exists(e -> e
                    .index("area")
                    .id(request.getArea_id())
            ).value();

            if (!exists) {
                return Result.error("区域不存在，ID: " + request.getArea_id());
            }

            // 检查区域名是否重复
            SearchResponse<Map> areaResponse = elasticsearchClient.search(s -> s
                    .index("area")
                    .query(q -> q
                            .term(t -> t
                                    .field("area_name.keyword")
                                    .value(request.getArea_name())
                            )
                    ),
                    Map.class
            );
            // 如果查询结果不为空，且查询结果的id与当前商品id不一致，则商品名称已存在
            if (areaResponse.hits().total().value() > 0) {
                String existingId = areaResponse.hits().hits().get(0).id();
                if (!existingId.equals(request.getArea_id())) {
                    return Result.error("区域名称已存在");
                }
            }

            // 如果提供了国家ID列表，检查所有国家是否存在
            if (request.getCountry_id().size() > 0) {
                for (String countryId : request.getCountry_id()) {
                    boolean countryExists = elasticsearchClient.exists(e -> e
                            .index("country")
                            .id(countryId)
                    ).value();

                    if (!countryExists) {
                        return Result.error("国家不存在，ID: " + countryId);
                    }
                }
            }

            // 获取当前时间的 ISO 格式字符串
            String now = java.time.Instant.now().toString();

            // 构建更新文档
            Map<String, Object> document = new HashMap<>();
            if (StringUtils.hasText(request.getArea_name())) {
                document.put("area_name", request.getArea_name());
            }
            if (request.getCountry_id().size() > 0) {
                document.put("area_country", request.getCountry_id());
            }
            document.put("updated_at", now);

            // 执行更新操作
            elasticsearchClient.update(u -> u
                    .index("area")
                    .id(request.getArea_id())
                    .doc(document),
                    Map.class
            );

            // 构建响应数据
            Map<String, Object> resultData = new HashMap<>();
            resultData.put("area_id", request.getArea_id());
            return Result.success(resultData);
        } catch (Exception e) {
            return Result.error("更新区域失败：" + e.getMessage());
        }
    }
} 