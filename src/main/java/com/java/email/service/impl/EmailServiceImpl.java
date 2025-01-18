package com.java.email.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import com.java.email.common.Result;
import com.java.email.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
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
}