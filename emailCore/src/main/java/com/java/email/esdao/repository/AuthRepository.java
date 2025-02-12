package com.java.email.esdao.repository;

import com.java.email.model.entity.AuthDocument;

import java.util.Optional;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuthRepository extends ElasticsearchRepository<AuthDocument, String> {
    AuthDocument save(AuthDocument authDocument);

    AuthDocument findByAuthId(String authId);

    Optional<AuthDocument> findByAuthName(String authName);
} 