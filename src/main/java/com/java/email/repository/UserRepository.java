package com.java.email.repository;

import com.java.email.esdao.UserDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends ElasticsearchRepository<UserDocument, String> {
    UserDocument findByUserAccount(String userAccount);
} 