package com.java.email.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch.core.GetResponse;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.json.JsonData;
import com.java.email.pojo.EmailTask;
import com.java.email.service.EmailTaskService;
import com.java.email.service.UserService;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.Objects;
import java.util.Arrays;

@Slf4j
@Service
public class EmailTaskServiceImpl implements EmailTaskService {
    private final UserService userService;
    private final ElasticsearchClient esClient;
    private static final String INDEX_NAME = "email_task_log";

    public EmailTaskServiceImpl(ElasticsearchClient esClient, UserService userService) {
        this.esClient = esClient;
        this.userService = userService;
    }

    // 初始化索引和映射
    public void initEmailTaskIndex() throws IOException {
        // 检查索引是否存在
        boolean exists = esClient.indices().exists(e -> e
                .index(INDEX_NAME)
        ).value();

        if (!exists) {
            // 创建索引并设置映射
            esClient.indices().create(c -> c
                    .index(INDEX_NAME)
                    .mappings(EmailTask.createMapping())  // 使用Email类中定义的映射
            );
        }
    }

    @Override
    public void saveEmailTask(EmailTask emailTask) throws IOException {
        initEmailTaskIndex();

        IndexResponse response = esClient.index(i -> i
                .index(INDEX_NAME)
                .id(emailTask.getEmailTaskId())
                .document(emailTask)
        );
    }

    @Override
    public List<EmailTask> findByDynamicQueryEmailTask(Map<String, String> params, int page, int size, 
            Integer userRole, String userEmail, List<String> managedUserEmails) throws IOException {
        try {
            SearchResponse<EmailTask> response = esClient.search(s -> {
                s.index(INDEX_NAME);
                s.from(page * size);
                s.size(size);

                s.query(q -> q.bool(b -> {

                    // 根据用户角色添加权限过滤
                    addRoleBasedFilter(b, userRole, userEmail, managedUserEmails);

                    // 处理其他查询参数
                    addOtherQueryParams(b, params, userRole, userEmail, managedUserEmails);

                    return b;
                }));

                // 添加排序
                s.sort(sort -> sort
                    .field(f -> f
                        .field("created_at")
                        .order(SortOrder.Desc)
                    )
                );

                return s;
            }, EmailTask.class);

            return response.hits().hits().stream()
                    .map(Hit::source)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error while searching email tasks: ", e);
            throw e;
        }
    }

    private void addRoleBasedFilter(BoolQuery.Builder b, Integer userRole, 
            String userEmail, List<String> managedUserEmails) {
        switch (userRole) {
            case 2: // 大管理员，不需要额外限制
                break;
            case 3: // 小管理员，只能查看管理用户的任务
                if (managedUserEmails != null && !managedUserEmails.isEmpty()) {
                    b.must(m -> m.terms(t -> t
                        .field("sender_id")
                        .terms(tt -> tt.value(managedUserEmails.stream()
                            .map(FieldValue::of)
                            .collect(Collectors.toList())))
                    ));
                }
                break;
            case 4: // 普通用户，只能查看自己的任务
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
            if (value != null)
                {
                switch (key) {
                    case "email_task_id":
                        b.must(m -> m.term(t -> t.field("email_task_id").value(value)));
                        break;
                    case "emailTypeId":
                        b.must(m -> m.term(t -> t.field("email_type_id").value(value)));
                        break;
                    case "subject":
                        b.must(m -> m.match(t -> t.field("subject").query(value)));
                        break;
                    case "taskType":
                        b.must(m -> m.term(t -> t.field("task_type").value(value)));
                        break;
                    case "sender_id":
                        validateSenderAccess(userRole, userEmail, managedUserEmails, value);
                        b.must(m -> m.term(t -> t.field("sender_id").value(value)));
                        break;
                    case "receiver_id":
                        b.must(m -> m.term(t -> t.field("receiver_id").value(value)));
                        break;
                    case "startDate":
                        b.must(m -> m.range(r -> r.field("start_date").gte(JsonData.of(Long.parseLong(value)))));
                        break;
                    case "endDate":
                        b.must(m -> m.range(r -> r.field("end_date").lte(JsonData.of(Long.parseLong(value)))));
                        break;
                    case "senderName":
                        b.must(m -> m.match(t -> t.field("sender_name").query(value)));
                        break;
                    case "receiver_name":
                        b.must(m -> m.match(t -> t.field("receiver_name").query(value)));
                        break;
                }
            }
        });
    }

    private void validateSenderAccess(Integer userRole, String userEmail, 
            List<String> managedUserEmails, String requestedSenderId) {
        if (userRole == 4 && !userEmail.equals(requestedSenderId)) {
            throw new IllegalArgumentException("User can only query their own tasks");
        } else if (userRole == 3 && !managedUserEmails.contains(requestedSenderId)) {
            throw new IllegalArgumentException("Manager can only query managed users' tasks");
        }
    }

    @Override
    public EmailTask findById(String id) throws IOException {
        GetResponse<EmailTask> response = esClient.get(g -> g
                        .index(INDEX_NAME)
                        .id(id),
                EmailTask.class
        );
        return response.found() ? response.source() : null;
    }

    @Override
    public List<EmailTask> findAll() throws IOException {
        SearchResponse<EmailTask> response = esClient.search(s -> s
                        .index(INDEX_NAME)
                        .query(q -> q.matchAll(m -> m)),
                EmailTask.class
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

    @Override
    public List<EmailTask> findByEmailTasks(Map<String, String> params, Integer userRole, String userEmail,List<String> managedUserEmails) throws IOException {
        SearchResponse<EmailTask> response = esClient.search(s -> {
            s.index(INDEX_NAME);

            s.query(q -> q.bool(b -> {
                // 根据用户角色添加基础查询条件
                switch (userRole) {
                    case 2: // 大管理员可以查看所有
                        break;
                    case 3: // 小管理员只能查看自己管理的用户的任务

                        if (!managedUserEmails.isEmpty()) {
                            b.must(m -> m.terms(t -> t
                                .field("sender_id")
                                .terms(tt -> tt.value(managedUserEmails.stream()
                                    .map(FieldValue::of)
                                    .collect(Collectors.toList())))
                            ));
                        }
                        break;
                    case 4: // 普通用户只能查看自己的任务
                        b.must(m -> m.term(t -> t.field("sender_id").value(userEmail)));
                        break;
                    default:
                        throw new IllegalArgumentException("Invalid user role: " + userRole);
                }

                // 处理其他查询参数
                if (!params.isEmpty()) {
                    params.forEach((key, value) -> {
                        if (value != null) {
                            switch (key) {
                                case "task_id":
                                    b.must(m -> m.term(t -> t.field("email_task_id").value(value)));
                                    break;
                                case "emailTypeId":
                                    b.must(m -> m.term(t -> t.field("email_type_id").value(value)));
                                    break;
                                case "taskType":
                                    b.must(m -> m.term(t -> t.field("task_type").value(value)));
                                    break;
                                case "subject":
                                    b.must(m -> m.match(t -> t.field("subject").query(value)));
                                    break;
                                default:
                                    break;
                            }
                        }
                    });
                }
                return b;
            }));

            return s;
        }, EmailTask.class);

        return response.hits().hits().stream()
                .map(Hit::source)
                .collect(Collectors.toList());
    }

}
