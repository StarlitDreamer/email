package com.java.email.esdao.repository.user;

import com.java.email.model.entity.user.AuthDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AuthRepository extends ElasticsearchRepository<AuthDocument, String> {
    AuthDocument save(AuthDocument authDocument);

    AuthDocument findByAuthId(String authId);

    Optional<AuthDocument> findByAuthName(String authName);
} 