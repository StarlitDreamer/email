package com.java.email.repository;

import com.java.email.entity.EmailTask;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmailTaskRepository extends ElasticsearchRepository<EmailTask, String> {
    // 根据 emailTaskId 查询邮件任务
    EmailTask findByEmailTaskId(String emailTaskId);
}