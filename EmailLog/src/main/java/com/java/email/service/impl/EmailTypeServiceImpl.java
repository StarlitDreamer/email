package com.java.email.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import com.java.email.pojo.EmailType;
import com.java.email.pojo.User;
import com.java.email.service.EmailTypeService;
import org.springframework.stereotype.Service;

import java.io.IOException;
@Service
public class EmailTypeServiceImpl implements EmailTypeService {
    private final ElasticsearchClient esClient;
    private static final String INDEX_NAME = "email_type";

    public EmailTypeServiceImpl(ElasticsearchClient esClient) {
        this.esClient = esClient;
    }

    @Override
    public String findByEmailTypeName(String emailTypeId) throws IOException {
        SearchResponse<EmailType> response= esClient.search(s -> {
            s.index(INDEX_NAME);
            if(emailTypeId!=null){
                s.query(q->q.term(t->t.field("email_type_id").value(emailTypeId)));
            }
            return s;
        }, EmailType.class);

        assert response.hits().hits().get(0).source() != null;
        return response.hits().hits().get(0).source().getEmailTypeName();
    }
}
