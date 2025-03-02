package com.java.email.repository;

import com.java.email.model.entity.Email;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface EmailRepository extends ElasticsearchRepository<Email, String> {
    Email findByEmailTaskId(String emailTaskId);

    // 根据 emailTaskId 查找所有对应的邮件记录
//    List<Email> findByEmailTaskId(String emailTaskId);
}