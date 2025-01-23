package com.java.email.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.GetResponse;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import com.java.email.pojo.User;
import com.java.email.service.UserService;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import java.io.IOException;
@Slf4j
@Service
public class UserServiceImpl implements UserService {
    private final ElasticsearchClient esClient;
    private static final String INDEX_NAME = "email_users";

    public UserServiceImpl(ElasticsearchClient esClient) {
        this.esClient = esClient;
    }

    // 初始化索引和映射
    public void initUserIndex() throws IOException {
        // 检查索引是否存在
        boolean exists = esClient.indices().exists(e -> e
                .index(INDEX_NAME)
        ).value();

        if (!exists) {
            // 创建索引并设置映射
            esClient.indices().create(c -> c
                    .index(INDEX_NAME)
                    .mappings(User.createMapping())  // 使用Email类中定义的映射
            );
        }
    }

    // 保存或更新邮件
    @Override
    public void saveUser(User user) throws IOException {
        // 确保索引存在
        initUserIndex();

        // 保存文档
        IndexResponse response = esClient.index(i -> i
                .index(INDEX_NAME)
                .id(user.getUserid())
                .document(user)
        );

        // 处理响应
        if (response.result().name().equals("Created")) {
            log.info("邮件文档创建成功: {}",user.getUserid());
        } else if (response.result().name().equals("Updated")) {
            log.info("邮件文档更新成功: {}", user.getUserid());
        }
    }

    @Override
    public User findById(String id) throws IOException {
        GetResponse<User> response = esClient.get(g -> g
                        .index(INDEX_NAME)
                        .id(id),
                User.class
        );
        return response.found() ? response.source() : null;
    }

    @Override
    public User findByUserId(String userId) throws IOException {
        SearchResponse<User> response= esClient.search(s -> {
            s.index(INDEX_NAME);
            if(userId!=null){
                s.query(q->q.term(t->t.field("userid").value(userId)));
            }
            return s;
        },User.class);

        return response.hits().hits().get(0).source();
    }
}
