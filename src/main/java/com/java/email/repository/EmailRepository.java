package com.java.email.repository;

import com.java.email.model.entity.Email;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.Optional;

public interface EmailRepository extends ElasticsearchRepository<Email, String> {
    Optional<Email> findByEmailTaskId(String emailTaskId);  // 根据emailTaskId查询
}
