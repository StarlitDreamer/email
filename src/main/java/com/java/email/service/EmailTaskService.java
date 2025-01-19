package com.java.email.service;

import com.java.email.entity.EmailTask;
import com.java.email.repository.EmailTaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EmailTaskService {

    @Autowired
    private EmailTaskRepository emailTaskRepository;

    /**
     * 根据邮件任务 ID 更新任务操作状态
     *
     * @param emailTaskId   邮件任务 ID
     * @param operateStatus 新的任务操作状态
     * @return 更新后的邮件任务
     * @throws IllegalArgumentException 如果任务操作状态无效或任务不存在
     */
    public EmailTask updateOperateStatus(String emailTaskId, int operateStatus) {
        // 查找邮件任务
        EmailTask emailTask = emailTaskRepository.findById(emailTaskId)
                .orElseThrow(() -> new IllegalArgumentException("邮件任务不存在"));

        // 更新任务操作状态
        emailTask.setOperateStatus(operateStatus);

        // 保存更新后的任务
        return emailTaskRepository.save(emailTask);
    }
}