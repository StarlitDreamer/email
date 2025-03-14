package com.java.email.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch._types.SortOrder;
import com.java.email.common.Result;
import com.java.email.model.EmailTypeFilterRequest;
import com.java.email.model.EmailTypeFilterResponse;
import com.java.email.model.EmailTypeVO;
import com.java.email.model.EmailTypeUpdateRequest;
import com.java.email.model.EmailTypeDeleteRequest;
import com.java.email.service.EmailService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import lombok.extern.slf4j.Slf4j;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import com.java.email.constant.TypeConstData;
@Service
@Slf4j
public class EmailServiceImpl implements EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailServiceImpl.class);

    
    @Autowired
    private ElasticsearchClient elasticsearchClient;

    @PostConstruct
    public void init() {
        createEmailTypeIndexIfNotExists();
    }

    private void createEmailTypeIndexIfNotExists() {
        try {
            boolean exists = elasticsearchClient.indices().exists(e -> e.index("email_type")).value();
            if (!exists) {
                elasticsearchClient.indices().create(c -> c
                        .index("email_type")
                        .mappings(m -> m
                                .properties("email_type_name", p -> p
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
    public Result<?> createEmail(String emailTypeName) {
        try {
            // 检查邮件类型名称是否重复
            SearchResponse<Map> emailTypeResponse = elasticsearchClient.search(s -> s
                    .index("email_type")
                    .query(q -> q
                            .term(t -> t
                                    .field("email_type_name.keyword")
                                    .value(emailTypeName)
                            )
                    ),
                    Map.class
            );
            if (emailTypeResponse.hits().total().value() > 0) {
                return Result.error("邮件类型名称已存在");
            }
            // 获取当前时间的 ISO 格式字符串
            String now = java.time.Instant.now().toString();
            
            // 构建文档
            Map<String, Object> document = new HashMap<>();
            String emailTypeId = UUID.randomUUID().toString();
            document.put("email_type_id", emailTypeId);
            document.put("email_type_name", emailTypeName);
            document.put("created_at", now);
            document.put("updated_at", now);

            // 保存到 Elasticsearch
            IndexResponse response = elasticsearchClient.index(i -> i
                    .index("email_type")
                    .id(emailTypeId)
                    .document(document)
            );

            // 构建响应数据
            Map<String, Object> resultData = new HashMap<>();
            resultData.put("email_type_id", emailTypeId);
            return Result.success(resultData);
        } catch (Exception e) {
            return Result.error("创建邮件类型失败：" + e.getMessage());
        }
    }

    @Override
    public Result<?> filterEmailType(EmailTypeFilterRequest request) {
        try {
            // 计算从第几条记录开始
            int from = (request.getPage_num() - 1) * request.getPage_size();

            // 构建搜索请求，id为birth的不返回
            SearchResponse<Map> response = elasticsearchClient.search(s -> s
                    .index("email_type")
                    .query(q -> q
                            .bool(b -> b
                                    .must(m -> {
                                        if (StringUtils.hasText(request.getEmail_type_name())) {
                                            return m.match(t -> t
                                                    .field("email_type_name")
                                                    .query(request.getEmail_type_name())
                                            );
                                        }
                                        return m.matchAll(ma -> ma);
                                    })
                                    .mustNot(mn -> mn
                                            .term(t -> t
                                                    .field("email_type_id")
                                                    .value("birth")
                                            )
                                    )
                            )
                    )
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
            EmailTypeFilterResponse filterResponse = new EmailTypeFilterResponse();
            filterResponse.setTotal_items(response.hits().total().value());
            filterResponse.setPage_num(request.getPage_num());
            filterResponse.setPage_size(request.getPage_size());

            // 转换搜索结果
            List<EmailTypeVO> emailTypes = new ArrayList<>();
            for (Hit<Map> hit : response.hits().hits()) {
                EmailTypeVO emailType = new EmailTypeVO();
                emailType.setEmail_type_id((String) hit.source().get("email_type_id"));
                emailType.setEmail_type_name((String) hit.source().get("email_type_name"));
                emailType.setCreated_at((String) hit.source().get("created_at"));
                emailType.setUpdated_at((String) hit.source().get("updated_at"));
                emailTypes.add(emailType);
            }
            filterResponse.setEmail_type(emailTypes);

            return Result.success(filterResponse);
        } catch (Exception e) {
            System.out.println(e);
            return Result.error("搜索邮件类型失败：" + e.getMessage());
        }
    }

    @Override
    public Result<?> updateEmailType(EmailTypeUpdateRequest request) {
        try {
            // 检查参数
            if (request.getEmail_type_id() == null || request.getEmail_type_id().trim().isEmpty()) {
                return Result.error("邮件类型ID不能为空");
            }
            if (request.getEmail_type_name() == null || request.getEmail_type_name().trim().isEmpty()) {
                return Result.error("邮件类型名称不能为空");
            }

            // 先检查文档是否存在
            log.info("Checking email type with ID: {}", request.getEmail_type_id());
            
            boolean exists = elasticsearchClient.exists(e -> e
                    .index("email_type")
                    .id(request.getEmail_type_id())
            ).value();
            
            log.info("Email type exists: {}", exists);
            
            if (!exists) {
                return Result.error("邮件类型不存在，ID: " + request.getEmail_type_id());
            }

            // 检查邮件类型名称是否重复
            SearchResponse<Map> emailTypeResponse = elasticsearchClient.search(s -> s
                    .index("email_type")
                    .query(q -> q
                            .term(t -> t
                                    .field("email_type_name.keyword")
                                    .value(request.getEmail_type_name())
                            )
                    ),
                    Map.class
            );
            if (emailTypeResponse.hits().total().value() > 0) {
                return Result.error("邮件类型名称已存在");
            }
            // 获取当前时间的 ISO 格式字符串
            String now = java.time.Instant.now().toString();

            // 构建更新文档
            Map<String, Object> document = new HashMap<>();
            document.put("email_type_name", request.getEmail_type_name());
            document.put("updated_at", now);

            // 执行更新操作
            elasticsearchClient.update(u -> u
                    .index("email_type")
                    .id(request.getEmail_type_id())
                    .doc(document),
                    Map.class
            );

            // 构建响应数据
            Map<String, Object> resultData = new HashMap<>();
            resultData.put("email_type_id", request.getEmail_type_id());
            return Result.success(resultData);
        } catch (Exception e) {
            log.error("Error updating email type: ", e);
            return Result.error("更新邮件类型失败：" + e.getMessage());
        }
    }

    @Override
    public Result<?> deleteEmailType(EmailTypeDeleteRequest request) {
        try {

            // 检查参数
            if (request.getEmail_type_id() == null || request.getEmail_type_id().trim().isEmpty()) {
                return Result.error("邮件类型ID不能为空");
            }

            // 先检查文档是否存在
            log.info("Checking email type with ID: {}", request.getEmail_type_id());
            
            boolean exists = elasticsearchClient.exists(e -> e
                    .index("email_type")
                    .id(request.getEmail_type_id())
            ).value();
            
            log.info("Email type exists: {}", exists);
            
            if (!exists) {
                return Result.error("邮件类型不存在，ID: " + request.getEmail_type_id());
            }

            // 执行删除操作
            elasticsearchClient.delete(d -> d
                    .index("email_type")
                    .id(request.getEmail_type_id())
            );

            Map<String, Object> resultData = new HashMap<>();
            resultData.put("email_type_id", request.getEmail_type_id());
            return Result.success(resultData);
        } catch (Exception e) {
            log.error("Error deleting email type: ", e);
            return Result.error("删除邮件类型失败：" + e.getMessage());
        }
    }
}