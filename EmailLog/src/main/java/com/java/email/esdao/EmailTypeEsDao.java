package com.java.email.esdao;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
public class EmailTypeEsDao {

    @Autowired
    private ElasticsearchClient elasticsearchClient;

    public void saveEmailType(String emailTypeName) throws IOException {
        Map<String, Object> document = new HashMap<>();
        document.put("email_type_name", emailTypeName);

        IndexResponse response = elasticsearchClient.index(i -> i
                .index("email_index")
                .document(document)
        );
    }
}
