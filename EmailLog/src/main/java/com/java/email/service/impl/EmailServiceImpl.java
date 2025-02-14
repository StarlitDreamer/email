package com.java.email.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.GetResponse;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.json.JsonData;
import lombok.extern.slf4j.Slf4j;
import com.java.email.pojo.Email;
import com.java.email.pojo.UndeliveredEmail;
import com.java.email.service.EmailService;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
@Slf4j
@Service
public class EmailServiceImpl implements EmailService {
    private final ElasticsearchClient esClient;
    private static final String INDEX_NAME = "fail_email_log";

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


    @Override
    public List<UndeliveredEmail> findByDynamicQueryEmail(Map<String, String> params, int page, int size) throws IOException {
        SearchResponse<UndeliveredEmail> response = esClient.search(s -> {
            s.index(INDEX_NAME);
            s.from(page * size);
            s.size(size);

            s.query(q -> q.bool(b -> {
                // 未发送邮件
                b.must(m -> m.term(t -> t.field("errorCode").value("5")));

                // 处理其他查询条件
                if (!params.isEmpty()) {
                    params.forEach((key, value) -> {
                        if (value != null && !"errorCode".equals(key)) { // 跳过errcode参数
                            switch (key) {
                                case "emailId":
                                    b.must(m -> m.term(t -> t.field("emailId.keyword").value(value)));
                                    break;
                                case "emailTaskId":
                                    b.must(m -> m.term(t -> t.field("emailTaskId.keyword").value(value)));
                                    break;
                                case "receiverId":
                                    b.must(m -> m.term(t -> t.field("receiverId.keyword").value(value)));
                                    break;
                                case "senderId":
                                    b.must(m -> m.match(t -> t.field("senderId.keyword").query(value)));
                                    break;
                                case "startDate":
                                    b.must(m -> m.range(r -> r.field("startDate").gte(JsonData.of(Long.parseLong(value)))));
                                    break;
                                case "endDate":
                                    b.must(m -> m.range(r -> r.field("endDate").lte(JsonData.of(Long.parseLong(value)))));
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
        }, UndeliveredEmail.class);

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
    public List<UndeliveredEmail> findAllEmail(String emailTaskId) throws IOException {
        SearchResponse<UndeliveredEmail> response= esClient.search(s -> {
            s.index(INDEX_NAME);
            if(emailTaskId!=null){
                s.query(q->q.match(t->t.field("emailTaskId").query(emailTaskId)));
            }else {
                s.query(q -> q.matchAll(m -> m));
            }
            return s;
        },UndeliveredEmail.class);
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
