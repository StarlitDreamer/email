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
import com.java.email.service.EmailRecipientService;
import com.java.email.vo.EmailVo;
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
import java.util.Set;
import java.util.HashSet;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
public class EmailServiceImpl implements EmailService {
    private final ElasticsearchClient esClient;
    private final EmailRecipientService emailRecipientService;
    private static final String INDEX_NAME = "email_details";

    public EmailServiceImpl(ElasticsearchClient esClient, EmailRecipientService emailRecipientService) {
        this.esClient = esClient;
        this.emailRecipientService = emailRecipientService;
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
    public EmailVo findByDynamicQueryEmail(Map<String, String> params, int page, int size,
                                           Integer userRole, String userEmail, List<String> managedUserEmails) throws IOException {
        try {
            // 先获取符合条件的收件人邮箱
            Set<String> recipientEmails = params != null ?
                    emailRecipientService.findMatchingRecipientEmails(params) : null;


            SearchResponse<UndeliveredEmail> response = esClient.search(s -> {
                s.index(INDEX_NAME);
                s.from((page - 1) * size);
                s.size(size);

                s.query(q -> q.bool(b -> {
                    // 处理邮件状态查询
                    if (params != null && params.containsKey("email_status")) {
                        String status = params.get("email_status");
                        if ("200".equals(status)) {
                            // 发送成功的邮件
                            b.must(m -> m.term(t -> t.field("error_code").value(200)));
                        } else if ("500".equals(status)) {
                            // 未发送的邮件
                            b.must(m -> m.term(t -> t.field("error_code").value(500)));
                        } else if ("535".equals(status)) {
                            b.must(m->m.term(t -> t.field("error_code").value(535)));

                        }
                        // 如果status不是200或500，忽略此条件
                    }

                    // 添加收件人邮箱过滤（仅当recipientEmails不为null且不为空时）
                    if (recipientEmails != null && !recipientEmails.isEmpty()) {
                        b.must(m -> m.terms(t -> t
                                .field("receiver_id")
                                .terms(tt -> tt.value(recipientEmails.stream()
                                        .map(FieldValue::of)
                                        .collect(Collectors.toList())))
                        ));
                    }

                    // 根据用户角色添加权限过滤
                    addRoleBasedFilter(b, userRole, userEmail, managedUserEmails);

                    // 处理其他查询参数
                    if (params != null && !params.isEmpty()) {
                        addOtherQueryParams(b, params, userRole, userEmail, managedUserEmails);
                    } else {
                        b.must(m -> m.matchAll(ma -> ma));
                    }

                    return b;
                }));

                // 添加默认排序
                s.sort(sort -> sort
                        .field(f -> f
                                .field("start_date")
                                .order(SortOrder.Desc)
                        )
                );

                return s;
            }, UndeliveredEmail.class);
            assert response.hits().total() != null;
            long totalHits = response.hits().total().value();
            EmailVo emailVo = new EmailVo();
            emailVo.setEmailList(response.hits().hits().stream()
                    .map(Hit::source)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList()));
            emailVo.setTotal(totalHits);

            return emailVo;
        } catch (Exception e) {
            log.error("Error while searching emails: params={}, userRole={}, userEmail={}",
                    params, userRole, userEmail, e);
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
        // 如果params为空，不添加任何查询条件，返回所有结果
        if (params == null || params.isEmpty()) {
            return;
        }
        params.forEach((key, value) -> {
            if (value != null && !value.isEmpty() &&
                    !Arrays.asList("email_status", "receiver_level", "receiver_birth").contains(key)) {
                switch (key) {
                    case "email_id":
                        b.must(m -> m.term(t -> t.field("email_id").value(value)));
                        break;
                    case "email_task_id":
                        b.must(m -> m.term(t -> t.field("email_task_id").value(value)));
                        break;
                    case "sender_email":
                        validateSenderAccess(userRole, userEmail, managedUserEmails, value);
                        b.must(m -> m.term(t -> t.field("sender_id").value(value)));
                        break;
                    case "receiver_email":
                        b.must(m -> m.term(t -> t.field("receiver_id").value(value)));
                        break;
                    case "sender_name":
                        b.must(m -> m.match(t -> t.field("sender_name").query(value)));
                        break;
                    case "receiver_name":
                        b.must(m -> m.match(t -> t.field("receiver_name").query(value)));
                        break;
                    case "start_date":
                        b.must(m -> m.range(r -> r
                                .field("end_date")
                                .gte(JsonData.of(Long.parseLong(value)))
                        ));
                        break;
                    case "end_date":
                        b.must(m -> m.range(r -> r
                                .field("start_date")
                                .lte(JsonData.of(Long.parseLong(value)))
                        ));
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
    public EmailVo findByDynamicQueryUndeliveredEmail(Map<String, String> params, List<String> emailTaskIds, int page, int size, Integer userRole, String userEmail, List<String> finalManagedUserEmails) throws IOException {
        try {
            // 如果emailTaskIds为null，直接返回空结果
            if (emailTaskIds == null || emailTaskIds.isEmpty()) {
                EmailVo emptyResult = new EmailVo();
                emptyResult.setEmailList(Collections.emptyList());
                emptyResult.setTotal(0L);
                return emptyResult;
            }

            // 先获取符合条件的收件人邮箱
            Set<String> recipientEmails = params != null ?
                    emailRecipientService.findMatchingRecipientEmails(params) : null;

            SearchResponse<UndeliveredEmail> response = esClient.search(s -> {
                s.index(INDEX_NAME);
                s.from((page - 1) * size);
                s.size(size);

                s.query(q -> q.bool(b -> {
                    // 必须匹配指定的emailTaskIds
                    b.must(m -> m.terms(t -> t
                            .field("email_task_id")
                            .terms(tt -> tt.value(emailTaskIds.stream()
                                    .map(FieldValue::of)
                                    .collect(Collectors.toList())))
                    ));

                    // 发送失败的邮件
                    b.should(m -> m.term(t -> t.field("error_code").value(500)));
                    b.should(m -> m.term(t -> t.field("error_code").value(535)));
                    b.minimumShouldMatch("1"); // 至少匹配一个错误码

                    // 添加收件人邮箱过滤
                    if (recipientEmails != null && !recipientEmails.isEmpty()) {
                        b.must(m -> m.terms(t -> t
                                .field("receiver_id")
                                .terms(tt -> tt.value(recipientEmails.stream()
                                        .map(FieldValue::of)
                                        .collect(Collectors.toList())))
                        ));
                    }

                    // 添加重发邮件ID过滤
                    Set<String> resendEmailId = params != null ? 
                        emailRecipientService.findResendDetails(params) : null;
                    if (resendEmailId != null && !resendEmailId.isEmpty()) {
                        b.must(m -> m.terms(t -> t
                                .field("email_id")
                                .terms(tt -> tt.value(resendEmailId.stream()
                                        .map(FieldValue::of)
                                        .collect(Collectors.toList())))
                        ));
                    }

                    // 添加角色权限过滤
                    addRoleBasedFilter(b, userRole, userEmail, finalManagedUserEmails);

                    // 处理其他查询参数
                    if (params != null && !params.isEmpty()) {
                        addOtherQueryParams(b, params, userRole, userEmail, finalManagedUserEmails);
                    }

                    return b;
                }));

                // 添加默认排序
                s.sort(sort -> sort
                        .field(f -> f
                                .field("start_date")
                                .order(SortOrder.Desc)
                        )
                );

                return s;
            }, UndeliveredEmail.class);

            assert response.hits().total() != null;
            long totalHits = response.hits().total().value();
            EmailVo emailVo = new EmailVo();
            emailVo.setEmailList(response.hits().hits().stream()
                    .map(Hit::source)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList()));
            emailVo.setTotal(totalHits);

            return emailVo;
        } catch (Exception e) {
            log.error("Error while searching emails: params={}, userRole={}, userEmail={}",
                    params, userRole, userEmail, e);
            throw e;
        }
    }


}
