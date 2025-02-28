package com.java.email.service;

import com.java.email.model.entity.Email;
import com.java.email.model.request.UpdateTaskStatusRequest;
import com.java.email.repository.EmailRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private EmailRepository emailRepository;

//    @Transactional
//    public boolean updateEmailStatus(String emailTaskId, Integer emailStatus) {
//        // 查找对应的 Email
//        Email email = emailRepository.findByEmailTaskId(emailTaskId);
//        if (email != null) {
//            // 更新 email_status
//            email.setEmailStatus(emailStatus);
//            email.setUpdateAt(System.currentTimeMillis() / 1000);  // 更新状态的时间
//            emailRepository.save(email);  // 保存更新后的记录
//            return true;
//        }
//        return false;
//    }

    /**
     * 根据emailTaskId更新emailStatus
     *
     * @return 更新后的邮件实体
     */
    public Email updateEmailStatus(UpdateTaskStatusRequest request) {
        // 根据emailTaskId查找Email实体
        Email email = emailRepository.findByEmailTaskId(request.getTaskId())
                .orElseThrow(() -> new RuntimeException("邮件任务未找到"));

        // 更新邮件状态
        email.setEmailStatus(Integer.valueOf(request.getOperateStatus()));

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
