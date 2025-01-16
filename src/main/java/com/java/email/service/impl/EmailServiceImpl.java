package com.java.email.service.impl;

import com.java.email.common.Result;
import com.java.email.service.EmailService;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class EmailServiceImpl implements EmailService {

    @Autowired
    private RestHighLevelClient elasticsearchClient;

    @Override
    public Result<?> createEmail(String emailTypeName) {
        try {
            // 创建要存储到ES的数据
            Map<String, Object> jsonMap = new HashMap<>();
            jsonMap.put("email_type_name", emailTypeName);

            // 创建索引请求
            IndexRequest indexRequest = new IndexRequest("email_index")
                    .source(jsonMap);

            // 执行索引请求
            elasticsearchClient.index(indexRequest, RequestOptions.DEFAULT);

            // 使用无参的 success() 方法
            return Result.success();
        } catch (Exception e) {
            return Result.error("创建邮件类型失败：" + e.getMessage());
        }
    }
}