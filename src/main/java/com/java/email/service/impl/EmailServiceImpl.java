package com.java.email.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.java.email.common.Result;
import com.java.email.model.EmailTypeFilterRequest;
import com.java.email.model.EmailTypeFilterResponse;
import com.java.email.model.EmailTypeVO;
import com.java.email.model.EmailTypeUpdateRequest;
import com.java.email.model.EmailTypeDeleteRequest;
import com.java.email.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class EmailServiceImpl implements EmailService {

    @Autowired
    private ElasticsearchClient elasticsearchClient;

    @Override
    public Result<?> createEmail(String emailTypeName) {
        try {
            Map<String, Object> document = new HashMap<>();
            document.put("email_type_name", emailTypeName);

            IndexResponse response = elasticsearchClient.index(i -> i
                    .index("email_index")
                    .document(document)
            );

            Map<String, Object> resultData = new HashMap<>();
            resultData.put("email_type_id", response.id());
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

            // 构建搜索请求
            SearchResponse<Map> response = elasticsearchClient.search(s -> s
                    .index("email_index")
                    .query(q -> {
                        if (StringUtils.hasText(request.getEmail_type_name())) {
                            return q.match(m -> m
                                    .field("email_type_name")
                                    .query(request.getEmail_type_name())
                            );
                        }
                        return q.matchAll(ma -> ma);
                    })
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
                emailType.setEmail_type_id(hit.id());
                emailType.setEmail_type_name((String) hit.source().get("email_type_name"));
                emailTypes.add(emailType);
            }
            filterResponse.setEmail_type(emailTypes);

            return Result.success(filterResponse);
        } catch (Exception e) {
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
            boolean exists = elasticsearchClient.exists(e -> e
                    .index("email_index")
                    .id(request.getEmail_type_id())
            ).value();

            if (!exists) {
                return Result.error("邮件类型不存在，ID: " + request.getEmail_type_id());
            }

            // 构建更新文档
            Map<String, Object> document = new HashMap<>();
            document.put("email_type_name", request.getEmail_type_name());

            // 执行更新操作
            elasticsearchClient.update(u -> u
                    .index("email_index")
                    .id(request.getEmail_type_id())
                    .doc(document),
                    Map.class
            );

            Map<String, Object> resultData = new HashMap<>();
            resultData.put("email_type_id", request.getEmail_type_id());
            return Result.success(resultData);
        } catch (Exception e) {
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
            boolean exists = elasticsearchClient.exists(e -> e
                    .index("email_index")
                    .id(request.getEmail_type_id())
            ).value();

            if (!exists) {
                return Result.error("邮件类型不存在，ID: " + request.getEmail_type_id());
            }

            // 执行删除操作
            elasticsearchClient.delete(d -> d
                    .index("email_index")
                    .id(request.getEmail_type_id())
            );

            Map<String, Object> resultData = new HashMap<>();
            resultData.put("email_type_id", request.getEmail_type_id());
            return Result.success(resultData);
        } catch (Exception e) {
            return Result.error("删除邮件类型失败：" + e.getMessage());
        }
    }
}