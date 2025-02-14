package com.java.email.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch.core.GetResponse;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.json.JsonData;
import com.java.email.pojo.EmailTask;
import com.java.email.service.EmailTaskService;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class EmailTaskServiceImpl implements EmailTaskService {
    private final ElasticsearchClient esClient;
    private static final String INDEX_NAME = "email_task_log";

    public EmailTaskServiceImpl(ElasticsearchClient esClient) {
        this.esClient = esClient;
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
    public List<EmailTask> findByDynamicQueryEmailTask(Map<String, String> params, int page, int size) throws IOException {
        SearchResponse<EmailTask> response = esClient.search(s -> {
            s.index(INDEX_NAME);
            s.from(page * size);
            s.size(size);

            // 如果参数不为空，则构造查询条件
            if (!params.isEmpty()) {
                s.query(q -> q.bool(b -> {
                    params.forEach((key, value) -> {
                        if (value != null) {
                            switch (key) {
                                case "emailTaskId":
                                    b.must(m -> m.term(t -> t.field("emailTaskId").value(value)));
                                    break;
                                case "emailTypeId": // 修正字段名
                                    b.must(m -> m.term(t -> t.field("emailTypeId").value(value)));
                                    break;
                                case "subject":
                                    b.must(m -> m.match(t -> t.field("subject").query(value)));
                                    break;
                                case "taskStatus":
                                    b.must(m -> m.term(t -> t.field("taskStatus").value(value)));
                                    break;
                                case "taskType":
                                    b.must(m -> m.term(t -> t.field("taskType").value(value)));
                                    break;
                                case "startDate":
                                    b.must(m -> m.range(r -> r.field("startDate").gte(JsonData.of(Long.parseLong(value)))));
                                    break;
                                case "endDate":
                                    b.must(m -> m.range(r -> r.field("endDate").lte(JsonData.of(Long.parseLong(value)))));
                                    break;
                                case "senderId": // 查询数组中是否包含指定值
                                    b.must(m -> m.term(t -> t.field("senderId").value(value)));
                                    break;
                                case "receiverId": // 查询数组中是否包含指定值
                                    b.must(m -> m.term(t -> t.field("receiverId").value(value)));
                                    break;
                                case "senderName":
                                    b.must(m -> m.match(t -> t.field("senderName").query(value)));
                                    break;
                                case "receiverName":
                                    b.must(m -> m.match(t -> t.field("receiverName").query(value)));
                                    break;
                                case "createdAt":
                                    b.must(m -> m.match(r -> r.field("createdAt").query((FieldValue) JsonData.of(Long.parseLong(value)))));
                                    break;
                                default:
                                    // 忽略未定义的查询字段
                                    break;
                            }
                        }
                    });
                    return b;
                }));
            } else {
                // 如果没有任何查询参数，则查询所有数据
                s.query(q -> q.matchAll(m -> m));
            }

            return s;
        }, EmailTask.class);

        // 返回查询结果，映射为 EmailTask 对象列表
        return response.hits().hits().stream()
                .map(Hit::source)
                .collect(Collectors.toList());
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
    public List<EmailTask> findByEmailTasks(Map<String, String> params) throws IOException {
        SearchResponse<EmailTask> response = esClient.search(s -> {
            s.index(INDEX_NAME);

            // 如果参数不为空，则构造查询条件
            if (!params.isEmpty()) {
                s.query(q -> q.bool(b -> {
                    params.forEach((key, value) -> {
                        if (value != null) {
                            switch (key) {
                                case "emailTaskId":
                                    b.must(m -> m.term(t -> t.field("emailTaskId").value(value)));
                                    break;
                                case "emailTypeId": // 修正字段名
                                    b.must(m -> m.term(t -> t.field("emailTypeId").value(value)));
                                    break;
                                case "subject":
                                    b.must(m -> m.match(t -> t.field("subject").query(value)));
                                    break;
                                case "taskStatus":
                                    b.must(m -> m.term(t -> t.field("taskStatus").value(value)));
                                    break;
                                case "taskType":
                                    b.must(m -> m.term(t -> t.field("taskType").value(value)));
                                    break;
                                case "startDate":
                                    b.must(m -> m.range(r -> r.field("startDate").gte(JsonData.of(Long.parseLong(value)))));
                                    break;
                                case "endDate":
                                    b.must(m -> m.range(r -> r.field("endDate").lte(JsonData.of(Long.parseLong(value)))));
                                    break;
                                default:
                                    // 忽略未定义的查询字段
                                    break;
                            }
                        }
                    });
                    return b;
                }));
            } else {
                // 如果没有任何查询参数，则查询所有数据
                s.query(q -> q.matchAll(m -> m));
            }

            return s;
        }, EmailTask.class);
        return response.hits().hits().stream()
                .map(Hit::source)
                .collect(Collectors.toList());
    }


}
