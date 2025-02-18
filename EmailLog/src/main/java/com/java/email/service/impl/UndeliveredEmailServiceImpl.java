package com.java.email.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.json.JsonData;
import com.java.email.pojo.UndeliveredEmail;
import com.java.email.service.UndeliveredEmailService;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
@Service
public class UndeliveredEmailServiceImpl implements UndeliveredEmailService {
    private final ElasticsearchClient esClient;
    private static final String INDEX_NAME = "fail_email_log";

    public UndeliveredEmailServiceImpl(ElasticsearchClient esClient) {
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
                    .mappings(UndeliveredEmail.createMapping())  // 使用Email类中定义的映射
            );
        }
    }
    @Override
    public void saveEmail(UndeliveredEmail email) throws IOException {
        initEmailTaskIndex();
        IndexResponse response = esClient.index(i -> i
                .index(INDEX_NAME)
                .id(email.getEmailId())
                .document(email)
        );
    }

    @Override
    public List<UndeliveredEmail> findByDynamicQueryEmail(Map<String, String> params, int page, int size) throws IOException {
        SearchResponse<UndeliveredEmail> response = esClient.search(s -> {
            s.index(INDEX_NAME);
            s.from(page * size);
            s.size(size);

            s.query(q -> q.bool(b -> {
                // 未发送邮件
                b.must(m -> m.term(t -> t.field("errorCode").value("6")));

                // 处理其他查询条件
                if (!params.isEmpty()) {
                    params.forEach((key, value) -> {
                        if (value != null && !"errorCode".equals(key)) { // 跳过errcode参数
                            switch (key) {
                                case "emailId":
                                    b.must(m -> m.term(t -> t.field("emailId").value(value)));
                                    break;
                                case "emailTaskId":
                                    b.must(m -> m.term(t -> t.field("emailTaskId").value(value)));
                                    break;
                                case "receiverId":
                                    b.must(m -> m.term(t -> t.field("receiverId").value(value)));
                                    break;
                                case "senderId":
                                    b.must(m -> m.match(t -> t.field("senderId").query(value)));
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


}
