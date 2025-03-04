package com.java.email.repository;

import com.java.email.model.entity.EmailPaused;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmailPausedRepository extends ElasticsearchRepository<EmailPaused, String> {
    EmailPaused findByEmailTaskId(String emailTaskId);
}

