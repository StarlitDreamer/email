package com.java.email.esdao.repository.user;

import java.util.Optional;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import com.java.email.model.entity.user.AuthDocument;

@Repository
public interface AuthRepository extends ElasticsearchRepository<AuthDocument, String> {
    AuthDocument save(AuthDocument authDocument);

    AuthDocument findByAuthId(String authId);

    Optional<AuthDocument> findByAuthName(String authName);
} 