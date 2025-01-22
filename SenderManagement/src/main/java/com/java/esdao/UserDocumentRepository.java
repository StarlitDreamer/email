package com.java.esdao;

import com.java.model.domain.User;

import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserDocumentRepository extends ElasticsearchRepository<User, String> {
    @Query("{" +
            "    \"bool\": {" +
            "        \"must\": [" +
            "            {\"match\": {\"user_id\": \"?0\"}}" +
            "        ]" +
            "    }" )
    Optional<User>findByUser_id(String user_id);


}
