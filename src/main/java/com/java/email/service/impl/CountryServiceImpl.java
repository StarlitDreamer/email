package com.java.email.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.java.email.common.Result;
import com.java.email.model.ImportCountryResponse;
import com.java.email.model.CountryFilterRequest;
import com.java.email.model.CountryFilterResponse;
import com.java.email.model.CountryVO;
import com.java.email.model.CountryCreateRequest;
import com.java.email.model.CountryDeleteRequest;
import com.java.email.model.CountryUpdateRequest;
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
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

@Service
public class CountryServiceImpl implements CountryService {

    private static final Logger log = LoggerFactory.getLogger(CountryServiceImpl.class);
    @Autowired
    private ElasticsearchClient elasticsearchClient;

    @PostConstruct
    public void init() {
        createCountryIndexIfNotExists();
    }

    private void createCountryIndexIfNotExists() {
        try {
            boolean exists = elasticsearchClient.indices().exists(e -> e.index("country")).value();
            if (!exists) {
                elasticsearchClient.indices().create(c -> c
                        .index("country")
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
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {
            
            int successCount = 0;
            int failCount = 0;
            List<String> errorMsg = new ArrayList<>();

            for (CSVRecord record : csvParser) {
                try {
                    String countryName = record.get(0).trim().replace("\"", "");
                    String countryCode = record.get(1).trim().replace("\"", "");
                    
                    // 检查国家名称是否重复
                    SearchResponse<Map> nameResponse = elasticsearchClient.search(s -> s
                            .index("country")
                            .query(q -> q
                                    .term(t -> t
                                            .field("country_name.keyword")
                                            .value(countryName)
                                    )
                            ),
                            Map.class
                    );
                    
                    if (nameResponse.hits().total().value() > 0) {
                        log.warn("Duplicate country name: {}", countryName);
                        errorMsg.add("找到重复的国家名称: " + countryName);
                        failCount++;
                        continue;
                    }

                    // 检查国家代码是否重复
                    SearchResponse<Map> codeResponse = elasticsearchClient.search(s -> s
                            .index("country")
                            .query(q -> q
                                    .term(t -> t
                                            .field("country_code.keyword")
                                            .value(countryCode)
                                    )
                            ),
                            Map.class
                    );
                    
                    if (codeResponse.hits().total().value() > 0) {
                        log.warn("Duplicate country code: {}", countryCode);
                        errorMsg.add("找到重复的国家代码: " + countryCode);
                        failCount++;
                        continue;
                    }
                    
                    
                    
                    // 构建文档
                    Map<String, Object> document = new HashMap<>();
                    String countryId = UUID.randomUUID().toString();
                    document.put("country_name", countryName);
                    document.put("country_code", countryCode);
                    document.put("country_id", countryId);
                    
                    String now = java.time.Instant.now().toString();
                    document.put("created_at", now);
                    document.put("updated_at", now);
                    
                    // 保存到 Elasticsearch
                    elasticsearchClient.index(i -> i
                            .index("country")
                            .id(countryId)
                            .document(document)
                    );
                    
                    successCount++;
                } catch (Exception e) {
                    log.error("Error processing line: {}", record, e);
                    failCount++;
                }
            }

            // 构建响应数据
            ImportCountryResponse response = new ImportCountryResponse();
            response.setSuccess_count(successCount);
            response.setFail_count(failCount);
            response.setErrorMsg(errorMsg);
            return Result.success(response);
        } catch (Exception e) {
            log.error("Import failed", e);
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
                    .index("country")
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
                    .index("country")
                    .query(q -> {
                        if (StringUtils.hasText(request.getCountry_code()) && StringUtils.hasText(request.getCountry_name())) {
                            return q.bool(b -> b
                                    .must(m -> m
                                            .match(t -> t
                                                    .field("country_code")
                                                    .query(request.getCountry_code())
                                            )
                                    )
                                    .must(m -> m
                                            .match(t -> t
                                                    .field("country_name")
                                                    .query(request.getCountry_name())
                                            )
                                    )
                            );
                        } else if (StringUtils.hasText(request.getCountry_code())) {
                            return q.match(t -> t
                                    .field("country_code")
                                    .query(request.getCountry_code())
                            );
                        } else if (StringUtils.hasText(request.getCountry_name())) {
                            return q.match(t -> t
                                    .field("country_name")
                                    .query(request.getCountry_name())
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
            CountryFilterResponse filterResponse = new CountryFilterResponse();
            filterResponse.setTotal_items(response.hits().total().value());
            filterResponse.setPage_num(request.getPage_num());
            filterResponse.setPage_size(request.getPage_size());

            // 转换搜索结果
            List<CountryVO> countries = new ArrayList<>();
            for (Hit<Map> hit : response.hits().hits()) {
                CountryVO country = new CountryVO();
                country.setCountry_id((String) hit.source().get("country_id"));
                country.setCountry_code((String) hit.source().get("country_code"));
                country.setCountry_name((String) hit.source().get("country_name"));
                country.setCreated_at((String) hit.source().get("created_at"));
                country.setUpdated_at((String) hit.source().get("updated_at"));
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

    @Override
    public Result<?> createCountry(CountryCreateRequest request) {
        try {
            // 检查国家代码是否重复
            SearchResponse<Map> countryResponse = elasticsearchClient.search(s -> s
                    .index("country")
                    .query(q -> q
                            .term(t -> t
                                    .field("country_code.keyword")
                                    .value(request.getCountry_code())
                            )
                    ),
                    Map.class
            );
            if (countryResponse.hits().total().value() > 0) {
                return Result.error("国家代码已存在");
            }
            // 检查国家名称是否重复
            SearchResponse<Map> countryNameResponse = elasticsearchClient.search(s -> s
                    .index("country")
                    .query(q -> q
                            .term(t -> t
                                    .field("country_name.keyword")
                                    .value(request.getCountry_name())
                            )
                    ),
                    Map.class
            );  
            if (countryNameResponse.hits().total().value() > 0) {
                return Result.error("国家名称已存在");
            }

            // 获取当前时间的 ISO 格式字符串
            String now = java.time.Instant.now().toString();

            // 构建文档
            Map<String, Object> document = new HashMap<>();
            String countryId = UUID.randomUUID().toString();
            document.put("country_id", countryId);
            document.put("country_code", request.getCountry_code());
            document.put("country_name", request.getCountry_name());
            document.put("created_at", now);
            document.put("updated_at", now);
            // 保存到 Elasticsearch
            IndexResponse response = elasticsearchClient.index(i -> i
                    .index("country")
                    .id(countryId)
                    .document(document)
            );

            // 构建响应数据
            Map<String, Object> resultData = new HashMap<>();
            resultData.put("country_id", countryId);
            return Result.success(resultData);
        } catch (Exception e) {
            return Result.error("创建国家失败：" + e.getMessage());
        }
    }

    @Override
    public Result<?> deleteCountry(CountryDeleteRequest request) {
        try {
            // 检查参数
            if (request.getCountry_id() == null || request.getCountry_id().trim().isEmpty()) {
                return Result.error("国家ID不能为空");
            }

            // 先检查文档是否存在
            boolean exists = elasticsearchClient.exists(e -> e
                    .index("country")
                    .id(request.getCountry_id())
            ).value();

            if (!exists) {
                return Result.error("国家不存在，ID: " + request.getCountry_id());
            }

            // 执行删除操作
            elasticsearchClient.delete(d -> d
                    .index("country")
                    .id(request.getCountry_id())
            );

            // 构建响应数据
            Map<String, Object> resultData = new HashMap<>();
            resultData.put("country_id", request.getCountry_id());
            return Result.success(resultData);
        } catch (Exception e) {
            return Result.error("删除国家失败：" + e.getMessage());
        }
    }

    @Override
    public Result<?> updateCountry(CountryUpdateRequest request) {
        try {
            // 检查参数
            if (request.getCountry_id() == null || request.getCountry_id().trim().isEmpty()) {
                return Result.error("国家ID不能为空");
            }

            // 先检查文档是否存在
            boolean exists = elasticsearchClient.exists(e -> e
                    .index("country")
                    .id(request.getCountry_id())
            ).value();

            if (!exists) {
                return Result.error("国家不存在，ID: " + request.getCountry_id());
            }
            // 检查国家代码是否重复
            SearchResponse<Map> countryResponse = elasticsearchClient.search(s -> s
                    .index("country")
                    .query(q -> q
                            .term(t -> t
                                    .field("country_code.keyword")
                                    .value(request.getCountry_code())
                            )
                    ),
                    Map.class
            );
            if (countryResponse.hits().total().value() > 0) {
                String existingId = countryResponse.hits().hits().get(0).id();
                if (!existingId.equals(request.getCountry_id())) {
                    return Result.error("国家代码已存在");
                }
            }
            // 检查国家名称是否重复
            SearchResponse<Map> countryNameResponse = elasticsearchClient.search(s -> s
                    .index("country")
                    .query(q -> q
                            .term(t -> t
                                    .field("country_name.keyword")
                                    .value(request.getCountry_name())
                            )
                    ),
                    Map.class
            );
            if (countryNameResponse.hits().total().value() > 0) {
                String existingId = countryNameResponse.hits().hits().get(0).id();
                if (!existingId.equals(request.getCountry_id())) {
                    return Result.error("国家名称已存在");
                }
            }

            // 获取当前时间的 ISO 格式字符串
            String now = java.time.Instant.now().toString();

            // 构建更新文档
            Map<String, Object> document = new HashMap<>();
            if (StringUtils.hasText(request.getCountry_code())) {
                document.put("country_code", request.getCountry_code());
            }
            if (StringUtils.hasText(request.getCountry_name())) {
                document.put("country_name", request.getCountry_name());
            }
            document.put("updated_at", now);

            // 执行更新操作
            elasticsearchClient.update(u -> u
                    .index("country")
                    .id(request.getCountry_id())
                    .doc(document),
                    Map.class
            );

            // 构建响应数据
            Map<String, Object> resultData = new HashMap<>();
            resultData.put("country_id", request.getCountry_id());
            return Result.success(resultData);
        } catch (Exception e) {
            return Result.error("更新国家失败：" + e.getMessage());
        }
    }
} 