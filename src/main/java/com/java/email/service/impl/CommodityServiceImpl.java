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

@Service
public class CommodityServiceImpl implements CommodityService {

    @Autowired
    private ElasticsearchClient elasticsearchClient;

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
} 