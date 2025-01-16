package com.java.email.esdao;

import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class EmailTypeEsDao {

    @Autowired
    private RestHighLevelClient elasticsearchClient;

    public void saveEmailType(String emailTypeName) throws Exception {
        Map<String, Object> jsonMap = new HashMap<>();
        jsonMap.put("email_type_name", emailTypeName);

        IndexRequest indexRequest = new IndexRequest("email_index")
                .source(jsonMap);

        elasticsearchClient.index(indexRequest, RequestOptions.DEFAULT);
    }
}
