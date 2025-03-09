package com.java.email.service;

import com.java.email.model.entity.*;
import com.java.email.model.request.ResetTaskStatusRequest;
import com.java.email.model.request.UpdateTaskStatusRequest;
import com.java.email.repository.*;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class EmailService {

    @Autowired
    private UserService userService;

    @Autowired
    private EmailRepository emailRepository;

    @Autowired
    private EmailTaskRepository emailTaskRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private EmailReportRepository emailReportRepository;

    @Autowired
    private SupplierRepository supplierRepository;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailPausedRepository emailPausedRepository;

    private static final String redisQueueName = "TIMER_TASK9001";//redis队列name

    public String pauseEmailTask(UpdateTaskStatusRequest request) {
        Email email = new Email();
        String emailTaskId = request.getEmailTaskId();
        Integer operateStatus = request.getOperateStatus();
        Email byEmailTaskId = emailRepository.findByEmailTaskId(emailTaskId);

        if (byEmailTaskId == null) {
            throw new RuntimeException("邮件任务未找到");
        }else {
            email.setEmailTaskId(emailTaskId);
            email.setEmailStatus(operateStatus);
        }
        emailRepository.save(email);
        return "Email task paused successfully";
    }

    public String beginEmailTask(UpdateTaskStatusRequest request) {
        long currentTimeMillis = System.currentTimeMillis();
        String emailTaskId = request.getEmailTaskId();
        Integer operateStatus = request.getOperateStatus();

        Email email = emailRepository.findByEmailTaskId(emailTaskId);

        if (email == null) {
            throw new RuntimeException("邮件任务状态未找到");
        }else {
            email.setEmailStatus(operateStatus);
        }
        emailRepository.save(email);

        redisTemplate.opsForValue().set(emailTaskId, emailTaskId,currentTimeMillis/1000);

        return "Email task resumed and stored in Redis";
    }

    /**
     * 根据邮箱查找客户或供应商
     *
     * @param email 邮箱
     * @return 找到的客户ID或供应商ID，若都未找到则返回null
     */
    public String findCustomerOrSupplierByEmail(String email) {
        // 查询客户
        Customer customer = customerRepository.findByEmails(email);
        if (customer != null) {
            return customer.getCustomerId();  // 返回客户ID
        }

        // 如果没有找到客户，查询供应商
        Supplier supplier = supplierRepository.findByEmails(email);
        if (supplier != null) {
            return supplier.getSupplierId();  // 返回供应商ID
        }

        return null;  // 如果都没有找到，返回null
    }

    /**
     * 根据 emailTaskId 更新所有相关 email 的状态
     *
     * @return 更新后的邮件实体集合
     */
    public Email updateEmailStatusForAll(String currentUserId, int currentUserRole, UpdateTaskStatusRequest request) {
        String emailTaskId = request.getEmailTaskId();

        // 根据 emailTaskId 查找所有相关的 Email 实体
        Email email = emailRepository.findByEmailTaskId(request.getEmailTaskId());

        // 根据 emailTaskId 查找 EmailTask，获取 sender_id
        EmailTask emailTask = emailTaskRepository.findByEmailTaskId(emailTaskId);

        Integer emailStatus = email.getEmailStatus();
        if (emailStatus != null && (emailStatus == 5 || emailStatus == 6)) {
            throw new IllegalStateException("更新邮件状态失败: " +
                    (emailStatus == 5 ? "邮件任务异常" : "邮件任务已完成"));
        }

        if (emailTask == null) {
            throw new RuntimeException("邮件任务未找到");
        }

        String senderEmail = emailTask.getSenderId();

        String senderId = userService.getUserIdByEmail(senderEmail);

        // 判断当前用户角色进行权限控制
        if (currentUserRole == 4 && !currentUserId.equals(senderId)) {
            // 普通用户只能修改自己的邮件
            throw new RuntimeException("普通用户，无权限修改该邮件任务");
        }

        List<String> subordinateUserIds = userService.getSubordinateUserIds(currentUserId);
        if (currentUserRole == 3) {
            // 小管理可以修改自己和下属用户的邮件，获取下属用户列表
            subordinateUserIds.add(currentUserId); // 小管理可以修改自己和下属用户的邮件
        }

        if (email == null) {
            throw new RuntimeException("邮件任务未找到相关邮件");
        }

        if (currentUserRole == 4 && !senderId.equals(currentUserId)) {
            // 普通用户只能修改自己的邮件
            throw new RuntimeException("普通用户1，无权限修改该邮件");
        }

        if (currentUserRole == 3 && !subordinateUserIds.contains(senderId)) {
            // 小管理只能修改自己和下属的邮件
            throw new RuntimeException("小管理，无权限修改该邮件");
        }

        // 更新邮件状态
        email.setEmailStatus(Integer.valueOf(request.getOperateStatus()));

        // 批量保存更新后的 Email 实体
        return emailRepository.save(email);
    }

    /**
     * 根据 email_task_id 更新所有相关 email 的状态为 4
     *
     * @return 更新后的邮件实体集合
     */
    public Email resetEmailStatusForAll(String currentUserId, int currentUserRole, ResetTaskStatusRequest request) {

        EmailTask byEmailTaskId = emailTaskRepository.findByEmailTaskId(request.getTaskId());

        String senderId = byEmailTaskId.getSenderId();

        User byUserEmail = userRepository.findByUserEmail(senderId);

        // 判断当前用户角色进行权限控制
        if (currentUserRole == 4 && !currentUserId.equals(byUserEmail.getUserId())) {
            // 普通用户只能修改自己的邮件
            throw new RuntimeException("无权限修改该邮件任务");
        }

        String emailTaskId = UUID.randomUUID().toString();

        EmailTask emailTask = new EmailTask();
        emailTask.setEmailTaskId(emailTaskId);
        emailTask.setEmailId(emailTaskId);
        emailTask.setSubject(byEmailTaskId.getSubject());
        emailTask.setEmailTypeId(byEmailTaskId.getEmailTypeId());
        emailTask.setEmailContent(byEmailTaskId.getEmailContent());
        emailTask.setSenderId(byEmailTaskId.getSenderId());
        emailTask.setSenderName(byEmailTaskId.getSenderName());
        emailTask.setReceiverId(byEmailTaskId.getReceiverId());
        emailTask.setReceiverName(byEmailTaskId.getReceiverName());
        emailTask.setAttachment(byEmailTaskId.getAttachment());
        emailTask.setTaskCycle(byEmailTaskId.getTaskCycle());
        emailTask.setIndex(0L);
        emailTask.setTaskType(2);

        long currentTime = System.currentTimeMillis() / 1000;

        emailTask.setCreatedAt(currentTime);

        emailTask.setStartDate(currentTime);

        emailTask.setEndDate(currentTime + byEmailTaskId.getTaskCycle() * 24 * 60 * 60);

        long intervalDate = 60 * 60;

        if (emailTask.getReceiverId().size() != 0) {
            intervalDate = emailTask.getTaskCycle() * 24 * 60 * 60 / emailTask.getReceiverId().size();
        }

        emailTask.setIntervalDate(intervalDate);

        emailTaskRepository.save(emailTask);

        // Create Email object for the "email" index
        Email email = new Email();
        email.setEmailTaskId(emailTaskId); // Set email_task_id
        email.setEmailId(emailTaskId);
        email.setCreatedAt(currentTime);  // Set created_at
        email.setUpdateAt(currentTime);   // Set update_at
        email.setEmailStatus(1);          // Set email_status to 1 (开始状态)

        // Save Email to Elasticsearch
        emailRepository.save(email);

        EmailReport emailReport = new EmailReport();
        emailReport.setEmailTaskId(emailTaskId);
        emailReport.setEmailTotal((long) byEmailTaskId.getReceiverId().size());

        emailReportRepository.save(emailReport);

        // Save email_task_id to Redis
        redisTemplate.opsForZSet().add(redisQueueName, emailTaskId, currentTime);

        // 根据 emailTaskId 查找所有相关的 Email 实体
        Email emails = emailRepository.findByEmailTaskId(request.getTaskId());

        if (emails == null) {
            throw new RuntimeException("邮件任务未找到");
        }

        // 遍历所有邮件并更新其状态为 4
        emails.setEmailStatus(4);

        // 批量保存更新后的 Email 实体
        return emailRepository.save(emails);
    }
}