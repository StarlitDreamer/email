package com.java.email.service;

import com.java.email.model.entity.Email;
import com.java.email.model.entity.EmailTask;
import com.java.email.model.request.ResetTaskStatusRequest;
import com.java.email.model.request.UpdateTaskStatusRequest;
import com.java.email.repository.EmailRepository;
import com.java.email.repository.EmailTaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmailService {

    @Autowired
    private UserService userService;

    @Autowired
    private EmailRepository emailRepository;

    @Autowired
    private EmailTaskRepository emailTaskRepository;


    /**
     * 根据 emailTaskId 更新所有相关 email 的状态
     *
     * @return 更新后的邮件实体集合
     */
    public List<Email> updateEmailStatusForAll(String currentUserId, int currentUserRole, UpdateTaskStatusRequest request) {
        // 根据 emailTaskId 查找 EmailTask，获取 sender_id
        EmailTask emailTask = emailTaskRepository.findByEmailTaskId(request.getEmailTaskId());
        if (emailTask == null) {
            throw new RuntimeException("邮件任务未找到");
        }

        String senderId = emailTask.getSenderId();

        // 判断当前用户角色进行权限控制
        if (currentUserRole == 4 && !currentUserId.equals(senderId)) {
            // 普通用户只能修改自己的邮件
            throw new RuntimeException("无权限修改该邮件任务");
        }

        List<String> subordinateUserIds = userService.getSubordinateUserIds(currentUserId);
        if (currentUserRole == 3) {
            // 小管理可以修改自己和下属用户的邮件，获取下属用户列表
            subordinateUserIds.add(currentUserId); // 小管理可以修改自己和下属用户的邮件
        }

        // 根据 emailTaskId 查找所有相关的 Email 实体
        List<Email> emails = emailRepository.findByEmailTaskId(request.getEmailTaskId());

        if (emails.isEmpty()) {
            throw new RuntimeException("邮件任务未找到相关邮件");
        }

        // 遍历所有邮件并更新其状态
        for (Email email : emails) {
            if (currentUserRole == 4 && !senderId.equals(currentUserId)) {
                // 普通用户只能修改自己的邮件
                throw new RuntimeException("无权限修改该邮件");
            }

            if (currentUserRole == 3 && !subordinateUserIds.contains(senderId)) {
                // 小管理只能修改自己和下属的邮件
                throw new RuntimeException("无权限修改该邮件");
            }

            // 更新邮件状态
            email.setEmailStatus(Integer.valueOf(request.getOperateStatus()));
        }

        // 批量保存更新后的 Email 实体
        return (List<Email>) emailRepository.saveAll(emails);
    }

//    public List<Email> updateEmailStatusForAll(String currentUserId, int currentUserRole,UpdateTaskStatusRequest request) {
//        // 根据 emailTaskId 查找所有相关的 Email 实体
//        List<Email> emails = emailRepository.findByEmailTaskId(request.getEmailTaskId());
//
//        if (emails.isEmpty()) {
//            throw new RuntimeException("邮件任务未找到");
//        }
//
//        // 遍历所有邮件并更新其状态
//        for (Email email : emails) {
//            email.setEmailStatus(Integer.valueOf(request.getOperateStatus()));
//        }
//
//        // 批量保存更新后的 Email 实体
//        return (List<Email>) emailRepository.saveAll(emails);
//    }

    /**
     * 根据 email_task_id 更新所有相关 email 的状态为 4
     *
     * @return 更新后的邮件实体集合
     */
    public List<Email> resetEmailStatusForAll(ResetTaskStatusRequest request) {
        // 根据 emailTaskId 查找所有相关的 Email 实体
        List<Email> emails = emailRepository.findByEmailTaskId(request.getTaskId());

        if (emails.isEmpty()) {
            throw new RuntimeException("邮件任务未找到");
        }

        // 遍历所有邮件并更新其状态为 4
        for (Email email : emails) {
            email.setEmailStatus(4);
        }

        // 批量保存更新后的 Email 实体
        return (List<Email>) emailRepository.saveAll(emails);
    }
//    /**
//     * 根据emailTaskId更新emailStatus
//     *
//     * @return 更新后的邮件实体
//     */
//    public Email updateEmailStatus(UpdateTaskStatusRequest request) {
//        // 根据emailTaskId查找Email实体
//        Email email = emailRepository.findByEmailTaskId(request.getTaskId())
//                .orElseThrow(() -> new RuntimeException("邮件任务未找到"));
//
//        // 更新邮件状态
//        email.setEmailStatus(Integer.valueOf(request.getOperateStatus()));
//
//        // 保存更新后的Email实体
//        return emailRepository.save(email);
//    }
//
//    /**
//     * 根据email_task_id更新emailStatus为4
//     *
//     * @return 更新后的邮件实体
//     */
//    public Email resetEmailStatus(ResetTaskStatusRequest request) {
//        // 根据email_task_id查找Email实体
//        Email email = emailRepository.findByEmailTaskId(request.getTaskId())
//                .orElseThrow(() -> new RuntimeException("邮件任务未找到"));
//
//        // 更新邮件状态为4
//        email.setEmailStatus(4);
//
//        // 保存更新后的Email实体
//        return emailRepository.save(email);
//    }
}
