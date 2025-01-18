package com.java.email.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.java.email.common.Result;
import com.java.email.model.EmailTypeFilterRequest;
import com.java.email.model.EmailTypeFilterResponse;
import com.java.email.model.EmailTypeVO;
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
            // 创建要存储到ES的数据
            Map<String, Object> document = new HashMap<>();
            document.put("email_type_name", emailTypeName);

            // 执行索引请求
            IndexResponse response = elasticsearchClient.index(i -> i
                    .index("email_index")
                    .document(document)
            );

            return Result.success();
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
}