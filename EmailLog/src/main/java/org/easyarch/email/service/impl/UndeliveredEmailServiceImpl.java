package org.easyarch.email.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.json.JsonData;
import org.easyarch.email.pojo.Email;
import org.easyarch.email.pojo.UndeliveredEmail;
import org.easyarch.email.service.UndeliveredEmailService;
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

    @Override
    public void saveEmail(UndeliveredEmail emailTask) throws IOException {
        IndexResponse response = esClient.index(i -> i
                .index(INDEX_NAME)
                .id(emailTask.getEmailTaskId())
                .document(emailTask)
        );
    }

    @Override
    public List<UndeliveredEmail> findByDynamicQueryEmail(Map<String, String> params, int page, int size) throws IOException {
        SearchResponse<UndeliveredEmail> response = esClient.search(s -> {
            s.index(INDEX_NAME);
            s.from(page * size);
            s.size(size);

            // 如果参数不为空，则构造查询条件
            if (!params.isEmpty()) {
                s.query(q -> q.bool(b -> {
                    params.forEach((key, value) -> {
                        if (value != null) {
                            switch (key) {
                                case "errcode":
                                    b.must(m -> m.term(t -> t.field("errcode").value(value)));
                                    break;
                                case "emailId":
                                    b.must(m -> m.term(t -> t.field("emailId").value(value)));
                                    break;
                                case "emailTaskId":
                                    b.must(m -> m.term(t -> t.field("emailTaskId").value(value)));
                                    break;
                                case "receiverId":
                                    b.must(m -> m.term(t -> t.field("receiverId").value(value)));
                                    break;
                                case "senderId": // 查询数组中是否包含指定值
                                    b.must(m -> m.match(t -> t.field("senderId").query(value)));
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
        }, UndeliveredEmail.class);

        // 返回查询结果，映射为 UndeliveredEmail 对象列表
        return response.hits().hits().stream()
                .map(Hit::source)
                .collect(Collectors.toList());
    }

}
