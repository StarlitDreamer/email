package com.java.email.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch.core.GetResponse;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.json.JsonData;
import com.java.email.pojo.EmailTask;
import lombok.extern.slf4j.Slf4j;
import com.java.email.pojo.Email;
import com.java.email.pojo.UndeliveredEmail;
import com.java.email.service.EmailService;
import com.java.email.service.UserService;
import com.java.email.service.CustomerService;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.Objects;
import java.util.Arrays;
import java.util.Collections;
import java.util.ArrayList;

@Slf4j
@Service
public class EmailServiceImpl implements EmailService {
    private final ElasticsearchClient esClient;
    private final UserService userService;
    private final CustomerService customerService;
    private static final String INDEX_NAME = "undelivered_email";

    public EmailServiceImpl(ElasticsearchClient esClient, UserService userService, CustomerService customerService) {
        this.esClient = esClient;
        this.userService = userService;
        this.customerService = customerService;
    }

    // 初始化索引和映射
    public void initEmailIndex() throws IOException {
        // 检查索引是否存在
        boolean exists = esClient.indices().exists(e -> e
                .index(INDEX_NAME)
        ).value();

        if (!exists) {
            // 创建索引并设置映射
            esClient.indices().create(c -> c
                    .index(INDEX_NAME)
                    .mappings(UndeliveredEmail.createMapping())  // 使用Email类中定义的映射
            );
        }
    }
    @Override
    public void saveEmailTask(UndeliveredEmail emailTask) throws IOException {
        initEmailIndex();

        IndexResponse response = esClient.index(i -> i
                .index(INDEX_NAME)
                .id(emailTask.getEmailId())
                .document(emailTask)
        );
    }


    @Override
    public List<UndeliveredEmail> findByDynamicQueryEmail(Map<String, String> params, int page, int size, 
            Integer userRole, String userEmail, List<String> managedUserEmails) throws IOException {
        try {
            SearchResponse<UndeliveredEmail> response = esClient.search(s -> {
                s.index(INDEX_NAME);
                s.from(page * size);
                s.size(size);

                s.query(q -> q.bool(b -> {
                    // 处理邮件状态查询
                    if (params.containsKey("error_code")) {
                        String status = params.get("error_code");
                        if ("200".equals(status)) {
                            // 发送成功的邮件
                            b.must(m -> m.term(t -> t.field("error_code").value(200)));
                        } else if ("500".equals(status)) {
                            // 未发送的邮件
                            b.must(m -> m.term(t -> t.field("error_code").value(500)));
                        }
                        // 如果status不是200或500，忽略此条件
                    }

                    // 处理客户等级和生日查询
                    List<String> customerEmails = null;
                    try {
                        customerEmails = customerService.findMatchingCustomerEmails(params);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    if (!customerEmails.isEmpty()) {
                        List<String> finalCustomerEmails = customerEmails;
                        b.must(m -> m.terms(t -> t
                            .field("receiver_id")
                            .terms(tt -> tt.value(finalCustomerEmails.stream()
                                .map(FieldValue::of)
                                .collect(Collectors.toList())))
                        ));
                    }

                    // 根据用户角色添加权限过滤
                    addRoleBasedFilter(b, userRole, userEmail, managedUserEmails);

                    // 处理其他查询参数
                    addOtherQueryParams(b, params, userRole, userEmail, managedUserEmails);

                    return b;
                }));

                return s;
            }, UndeliveredEmail.class);

            return response.hits().hits().stream()
                    .map(Hit::source)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error while searching emails: ", e);
            throw e;
        }
    }



    private void addRoleBasedFilter(BoolQuery.Builder b, Integer userRole,
                                    String userEmail, List<String> managedUserEmails) {
        switch (userRole) {
            case 2: // 大管理员，不需要额外限制
                break;
            case 3: // 小管理员，只能查看管理用户的邮件
                if (managedUserEmails != null && !managedUserEmails.isEmpty()) {
                    b.must(m -> m.terms(t -> t
                        .field("sender_id")
                        .terms(tt -> tt.value(managedUserEmails.stream()
                            .map(FieldValue::of)
                            .collect(Collectors.toList())))
                    ));
                }
                break;
            case 4: // 普通用户，只能查看自己的邮件
                if (userEmail != null && !userEmail.isEmpty()) {
                    b.must(m -> m.term(t -> t.field("sender_id").value(userEmail)));
                }
                break;
            default:
                throw new IllegalArgumentException("Invalid user role: " + userRole);
        }
    }

    private void addOtherQueryParams(BoolQuery.Builder b, Map<String, String> params, 
            Integer userRole, String userEmail, List<String> managedUserEmails) {
        params.forEach((key, value) -> {
            if (value != null && !value.isEmpty() && 
                !Arrays.asList("error_code", "customer_level", "birth").contains(key)) {
                switch (key) {
                    case "email_id":
                        b.must(m -> m.term(t -> t.field("email_id").value(value)));
                        break;
                    case "emailTaskId":
                        b.must(m -> m.term(t -> t.field("email_task_id").value(value)));
                        break;
                    case "senderId":
                        validateSenderAccess(userRole, userEmail, managedUserEmails, value);
                        b.must(m -> m.term(t -> t.field("sender_id").value(value)));
                        break;
                        case "receiverId":
                            b.must(m -> m.term(t -> t.field("receiver_id").value(value)));
                            break;
                    case "senderName":
                        b.must(m -> m.term(t -> t.field("sender_name.keyword").value(value)));
                        break;
                        case "receiverName":
                            b.must(m -> m.term(t -> t.field("receiver_name.keyword").value(value)));
                            break;
                    case "startDate":
                        b.must(m -> m.range(r -> r.field("start_date").gte(JsonData.of(Long.parseLong(value)))));
                        break;
                    case "endDate":
                        b.must(m -> m.range(r -> r.field("end_date").lte(JsonData.of(Long.parseLong(value)))));
                        break;
                }
            }
        });
    }

    private void validateSenderAccess(Integer userRole, String userEmail, List<String> managedUserEmails, String requestedSenderId) {
        if (userRole == 4 && !userEmail.equals(requestedSenderId)) {
            throw new IllegalArgumentException("User can only query their own emails");
        } else if (userRole == 3 && !managedUserEmails.contains(requestedSenderId)) {
            throw new IllegalArgumentException("Manager can only query managed users' emails");
        }
    }

    @Override
    public Email findById(String id) throws IOException {
        GetResponse<Email> response = esClient.get(g -> g
                        .index(INDEX_NAME)
                        .id(id),
                Email.class
        );
        return response.found() ? response.source() : null;
    }

    @Override
    public List<UndeliveredEmail> findAllEmail(String emailTaskId, Integer userRole, String userEmail, 
            List<String> managedUserEmails) throws IOException {
        SearchResponse<UndeliveredEmail> response = esClient.search(s -> {
            s.index(INDEX_NAME);
            
            s.query(q -> q.bool(b -> {
                // 添加权限过滤
                switch (userRole) {
                    case 2: // 大管理员
                        break;
                    case 3: // 小管理员
                        if (!managedUserEmails.isEmpty()) {
                            b.must(m -> m.terms(t -> t
                                .field("senderId")
                                .terms(tt -> tt.value(managedUserEmails.stream()
                                    .map(FieldValue::of)
                                    .collect(Collectors.toList())))
                            ));
                        }
                        break;
                    case 4: // 普通用户
                        b.must(m -> m.term(t -> t.field("senderId").value(userEmail)));
                        break;
                    default:
                        throw new IllegalArgumentException("Invalid user role: " + userRole);
                }

                // 添加emailTaskId过滤
                if (emailTaskId != null) {
                    b.must(m -> m.term(t -> t.field("emailTaskId.keyword").value(emailTaskId)));
                }
                
                return b;
            }));
            
            return s;
        }, UndeliveredEmail.class);
        
        return response.hits().hits().stream()
                .map(Hit::source)
                .collect(Collectors.toList());
    }

    @Override
    public List<Email> findAll() throws IOException {
        SearchResponse<Email> response = esClient.search(s -> s
                        .index(INDEX_NAME)
                        .query(q -> q.matchAll(m -> m)),
                Email.class
        );
        return response.hits().hits().stream()
                .map(Hit::source)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(String id) throws IOException {
        esClient.delete(d -> d
                .index(INDEX_NAME)
                .id(id)
        );
    }
}
