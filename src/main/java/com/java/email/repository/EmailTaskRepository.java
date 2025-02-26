package com.java.email.repository;

import com.java.email.model.entity.EmailTask;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmailTaskRepository extends ElasticsearchRepository<EmailTask, String> {
    // 根据 emailTaskId 查询邮件任务
    EmailTask findByEmailTaskId(String emailTaskId);

    /**
     * 根据任务类型查询邮件任务
     *
     * @param taskType 任务类型（3: 节日发送, 4: 生日发送）
     * @return 符合条件的邮件任务列表
     */
    List<EmailTask> findByTaskType(int taskType);
}