package com.java.email.repository;

import com.java.email.model.entity.Email;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

public interface EmailRepository extends ElasticsearchRepository<Email, String> {
//    Optional<Email> findByEmailTaskId(String emailTaskId);

    // 根据 emailTaskId 查找所有对应的邮件记录
    List<Email> findByEmailTaskId(String emailTaskId);
}