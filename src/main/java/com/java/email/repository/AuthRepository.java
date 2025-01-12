package com.java.email.repository;

import com.java.email.esdao.AuthDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuthRepository extends ElasticsearchRepository<AuthDocument, String> {
    AuthDocument save(AuthDocument authDocument);

    AuthDocument findByAuthId(String authId);
} 