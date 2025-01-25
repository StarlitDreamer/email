package org.easyarch.email.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.GetResponse;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.json.JsonData;
import lombok.extern.slf4j.Slf4j;
import org.easyarch.email.pojo.Email;
import org.easyarch.email.service.EmailService;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
@Slf4j
@Service
public class EmailServiceImpl implements EmailService {
    private final ElasticsearchClient esClient;
    private static final String INDEX_NAME = "email_log";

    public EmailServiceImpl(ElasticsearchClient esClient) {
        this.esClient = esClient;
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
                    .mappings(Email.createMapping())  // 使用Email类中定义的映射
            );
        }
    }

    // 保存或更新邮件
    @Override
    public void saveEmail(Email email) throws IOException {
        // 确保索引存在
        initEmailIndex();

        // 保存文档
        IndexResponse response = esClient.index(i -> i
                .index(INDEX_NAME)
                .id(email.getEmailId())
                .document(email)
        );

        // 处理响应
        if (response.result().name().equals("Created")) {
            log.info("邮件文档创建成功: {}", email.getEmailId());
        } else if (response.result().name().equals("Updated")) {
            log.info("邮件文档更新成功: {}", email.getEmailId());
        }
    }

    @Override
    public List<Email> findByDynamicQueryEmail(Map<String, String> params, int page, int size) throws IOException {
        SearchResponse<Email> response = esClient.search(s -> {
            s.index(INDEX_NAME);
            s.from(page * size);
            s.size(size);

            // 如果参数不为空，则构造查询条件
            if (!params.isEmpty()) {
                s.query(q -> q.bool(b -> {
                    params.forEach((key, value) -> {
                        if (value != null) {
                            switch (key) {
                                case "emailStatus":
                                    b.must(m -> m.term(t -> t.field("emailStatus").value(Long.parseLong(value))));
                                    break;
                                case "emailTaskId":
                                    b.must(m -> m.term(t -> t.field("emailTaskId").value(value)));
                                    break;
                                case "receiverId":
                                    b.must(m -> m.term(t -> t.field("receiverid").value(value)));
                                    break;
                                case "senderId":
                                    b.must(m -> m.term(t -> t.field("senderid").value(value)));
                                    break;
                                case "startDate":
                                    b.must(m -> m.range(r -> r.field("startDate").gte(JsonData.of(Long.parseLong(value)))));
                                    break;
                                case "endDate":
                                    b.must(m -> m.range(r -> r.field("endDate").lte(JsonData.of(Long.parseLong(value)))));
                                    break;
                                case "createdAt":
                                    b.must(m -> m.range(r -> r.field("createdAt").gte(JsonData.of(Long.parseLong(value)))));
                                    break;
                                default:
                                    // 忽略未定义的字段
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
        }, Email.class);

        // 返回查询结果，映射为 Email 对象列表
        return response.hits().hits().stream()
                .map(Hit::source)
                .collect(Collectors.toList());
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
    public List<Email> findAllEmail(String emailTaskId) throws IOException {
        SearchResponse<Email> response= esClient.search(s -> {
            s.index(INDEX_NAME);
            if(emailTaskId!=null){
                s.query(q->q.match(t->t.field("emailTaskId").query(emailTaskId)));
            }else {
                s.query(q -> q.matchAll(m -> m));
            }
            return s;
        },Email.class);
        return response.hits().hits().stream().map(Hit::source).collect(Collectors.toList());

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
