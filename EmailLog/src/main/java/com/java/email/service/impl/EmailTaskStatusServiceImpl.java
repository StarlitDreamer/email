package com.java.email.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.java.email.pojo.Email;
import com.java.email.pojo.EmailTask;
import com.java.email.service.EmailTaskStatusService;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
@Service
public class EmailTaskStatusServiceImpl implements EmailTaskStatusService {

    private final ElasticsearchClient esClient;
    private static final String INDEX_NAME = "email";

    public EmailTaskStatusServiceImpl(ElasticsearchClient esClient) {
        this.esClient = esClient;
    }

    @Override
    public Set<String> findEmailTaskIds(long emailStatus) throws IOException {
        SearchResponse<Email> response = esClient.search(s -> {
            s.index(INDEX_NAME);

            s.query(q -> q.bool(b -> {
                b.must(m -> m.term(t -> t.field("email_status").value(FieldValue.of(emailStatus))));
                return b;
            }));

            return s;
        }, Email.class);

        return response.hits().hits().stream().map(Hit::source)
                .filter(Objects::nonNull)
                .map(Email::getEmailTaskId)
                .collect(Collectors.toSet());


    }
}
