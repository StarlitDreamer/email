package com.java.email.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.TermQuery;
import co.elastic.clients.elasticsearch.core.GetResponse;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.java.email.pojo.Customer;
import com.java.email.pojo.Supplier;
import lombok.extern.slf4j.Slf4j;
import com.java.email.pojo.User;
import com.java.email.service.UserService;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;

@Slf4j
@Service
public class UserServiceImpl implements UserService {
    private final ElasticsearchClient esClient;
    private static final String INDEX_NAME = "user";

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
    public User findByUserEmail(String email) {
        try {
            // 构建精确匹配查询
            Query query = new Query.Builder()
                .term(t -> t
                    .field("user_email")
                    .value(email)
                )
                .build();

            // 执行搜索
            SearchResponse<User> response = esClient.search(s -> s
                .index(INDEX_NAME)
                .query(query), 
                User.class
            );

            // 检查结果
            if (response.hits().total().value() > 0) {
                return response.hits().hits().get(0).source();
            }
            
            return null;  // 如果没有找到用户，返回 null
            
        } catch (Exception e) {
            log.error("Error finding user by email: " + e.getMessage());
            return null;
        }
    }

    @Override
    public String findUserEmailByUserName(String senderName) throws IOException {
        SearchResponse<User> response= esClient.search(s -> {
            s.index(INDEX_NAME);
            if(senderName!=null){
                s.query(q->q.match(t->t.field("user_name").query(senderName)));
            }
            return s;
        },User.class);

        return response.hits().hits().get(0).source().getUserEmail();
    }

    @Override
    public List<String> findManagedUserEmails(String userId) {
        Set<String> emails = new HashSet<>();  // 使用Set避免重复邮箱
        
        try {
            // 先查询用户信息获取belong_user_id
//            GetResponse<User> userResponse = esClient.get(g -> g
//                .index(INDEX_NAME)
//                .id(userId),
//                User.class
//            );
//
//            if (!userResponse.found() || userResponse.source() == null) {
//                log.warn("User not found: userId={}", userId);
//                return new ArrayList<>();
//            }
//
//            String belongUserId = userResponse.source().getBelongUserid();
//            if (belongUserId == null) {
//                log.warn("User has no belong_user_id: userId={}", userId);
//                return new ArrayList<>();
//            }

            // 构建精确匹配查询
            Query query = TermQuery.of(t -> t
                .field("belong_user_id")
                .value(userId)
            )._toQuery();

            // 查询Customer索引
            SearchResponse<Customer> customerResponse = esClient.search(s -> s
                .index("customer")
                .query(query)
                .size(1000),
                Customer.class
            );

            // 提取Customer邮箱
            for (Hit<Customer> hit : customerResponse.hits().hits()) {
                Customer customer = hit.source();
                if (customer != null && customer.getEmails() != null) {
                    emails.addAll(Arrays.asList(customer.getEmails()));
                }
            }

            // 查询Supplier索引
            SearchResponse<Supplier> supplierResponse = esClient.search(s -> s
                .index("supplier")
                .query(query)
                .size(1000),
                Supplier.class
            );

            // 提取Supplier邮箱
            for (Hit<Supplier> hit : supplierResponse.hits().hits()) {
                Supplier supplier = hit.source();
                if (supplier != null && supplier.getEmails() != null) {
                    emails.addAll(Arrays.asList(supplier.getEmails()));
                }
            }

            return new ArrayList<>(emails);  // 转换为List返回
            
        } catch (Exception e) {
            log.error("Error finding managed emails from customer and supplier: userId={}, error={}", 
                userId, e.getMessage(), e);
            return new ArrayList<>();
        }
    }

}
