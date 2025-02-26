package com.java.email.service;

import com.java.email.model.entity.Email;
import com.java.email.repository.EmailRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private EmailRepository emailRepository;

    /**
     * 根据emailTaskId更新emailStatus
     *
     * @param emailTaskId 邮件任务ID
     * @param newStatus   新的邮件状态
     * @return 更新后的邮件实体
     */
    public Email updateEmailStatus(String emailTaskId, Integer newStatus) {
        // 根据emailTaskId查找Email实体
        Email email = emailRepository.findByEmailTaskId(emailTaskId)
                .orElseThrow(() -> new RuntimeException("邮件任务未找到"));

        // 更新邮件状态
        email.setEmailStatus(newStatus);

        // 保存更新后的Email实体
        return emailRepository.save(email);
    }

    /**
     * 根据email_task_id更新emailStatus为4
     *
     * @param emailTaskId 邮件任务ID
     * @param newStatus   新的邮件状态
     * @return 更新后的邮件实体
     */
    public Email resetEmailStatus(String emailTaskId, Integer newStatus) {
        // 根据email_task_id查找Email实体
        Email email = emailRepository.findByEmailTaskId(emailTaskId)
                .orElseThrow(() -> new RuntimeException("邮件任务未找到"));

        // 更新邮件状态为4
        email.setEmailStatus(newStatus);

        // 保存更新后的Email实体
        return emailRepository.save(email);
    }
}
