package com.java.email.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.java.email.common.Result;
import com.java.email.model.ImportCountryResponse;
import com.java.email.model.CountryFilterRequest;
import com.java.email.model.CountryFilterResponse;
import com.java.email.model.CountryVO;
import com.java.email.service.CountryService;
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
public class CountryServiceImpl implements CountryService {

    @Autowired
    private ElasticsearchClient elasticsearchClient;

    @PostConstruct
    public void init() {
        createCountryIndexIfNotExists();
    }

    private void createCountryIndexIfNotExists() {
        try {
            boolean exists = elasticsearchClient.indices().exists(e -> e.index("country_index")).value();
            if (!exists) {
                elasticsearchClient.indices().create(c -> c
                        .index("country_index")
                        .mappings(m -> m
                                .properties("country_name", p -> p
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
    public Result<?> importCountry(MultipartFile file) {
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
                    if (fields.length >= 1) {  // 假设CSV至少包含国家名称
                        Map<String, Object> document = new HashMap<>();
                        document.put("country_name", fields[0].trim());
                        
                        // 保存到 Elasticsearch
                        elasticsearchClient.index(i -> i
                                .index("country_index")
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
            ImportCountryResponse response = new ImportCountryResponse();
            response.setSuccess_count(successCount);
            response.setFail_count(failCount);

            return Result.success(response);
        } catch (Exception e) {
            return Result.error("导入国家失败：" + e.getMessage());
        }
    }

    @Override
    public Result<?> filterCountry(CountryFilterRequest request) {
        try {
            // 计算从第几条记录开始
            int from = (request.getPage_num() - 1) * request.getPage_size();

            // 检查索引是否存在，不存在则返回空结果
            boolean exists = elasticsearchClient.indices().exists(e -> e
                    .index("country_index")
            ).value();

            if (!exists) {
                // 返回空结果但保持数据结构一致
                CountryFilterResponse emptyResponse = new CountryFilterResponse();
                emptyResponse.setTotal_items(0L);
                emptyResponse.setPage_num(request.getPage_num());
                emptyResponse.setPage_size(request.getPage_size());
                emptyResponse.setCountry(new ArrayList<>());
                return Result.success(emptyResponse);
            }

            // 构建搜索请求
            SearchResponse<Map> response = elasticsearchClient.search(s -> s
                    .index("country_index")
                    .query(q -> {
                        if (StringUtils.hasText(request.getCountry_code()) && StringUtils.hasText(request.getCountry_name())) {
                            return q.bool(b -> b
                                    .must(m -> m
                                            .term(t -> t
                                                    .field("country_code.keyword")
                                                    .value(request.getCountry_code())
                                            )
                                    )
                                    .must(m -> m
                                            .term(t -> t
                                                    .field("country_name.keyword")
                                                    .value(request.getCountry_name())
                                            )
                                    )
                            );
                        } else if (StringUtils.hasText(request.getCountry_code())) {
                            return q.term(t -> t
                                    .field("country_code.keyword")
                                    .value(request.getCountry_code())
                            );
                        } else if (StringUtils.hasText(request.getCountry_name())) {
                            return q.term(t -> t
                                    .field("country_name.keyword")
                                    .value(request.getCountry_name())
                            );
                        }
                        return q.matchAll(ma -> ma);
                    })
                    .from(from)
                    .size(request.getPage_size()),
                    Map.class
            );

            // 构建响应对象
            CountryFilterResponse filterResponse = new CountryFilterResponse();
            filterResponse.setTotal_items(response.hits().total().value());
            filterResponse.setPage_num(request.getPage_num());
            filterResponse.setPage_size(request.getPage_size());

            // 转换搜索结果
            List<CountryVO> countries = new ArrayList<>();
            for (Hit<Map> hit : response.hits().hits()) {
                CountryVO country = new CountryVO();
                country.setCountry_code(hit.id());
                country.setCountry_name((String) hit.source().get("country_name"));
                countries.add(country);
            }
            filterResponse.setCountry(countries);

            return Result.success(filterResponse);
        } catch (Exception e) {
            // 出错时也要保持数据结构一致
            CountryFilterResponse errorResponse = new CountryFilterResponse();
            errorResponse.setTotal_items(0L);
            errorResponse.setPage_num(request.getPage_num() != null ? request.getPage_num() : 1);
            errorResponse.setPage_size(request.getPage_size() != null ? request.getPage_size() : 10);
            errorResponse.setCountry(new ArrayList<>());
            return Result.success(errorResponse);
        }
    }
} 