package com.java.email.service;

import com.java.email.Dto.CreateCycleEmailTaskRequest;
import com.java.email.Dto.CreateFestivalEmailTaskRequest;
import com.java.email.entity.Attachment;
import com.java.email.entity.EmailTask;
import com.java.email.entity.Receiver;
import com.java.email.repository.EmailTaskRepository;
import com.java.email.Dto.CreateEmailTaskRequest;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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
    public EmailTask updateOperateStatus(String emailTaskId, int operateStatus, String currentUserId) {
        // 查找邮件任务
        EmailTask emailTask = emailTaskRepository.findById(emailTaskId)
                .orElseThrow(() -> new IllegalArgumentException("邮件任务不存在"));

        // 检查当前用户是否有权限修改该任务
        if (!emailTask.getSenderIds().contains(currentUserId)) {
            throw new IllegalArgumentException("无权修改该邮件任务");
        }

        // 更新任务操作状态
        emailTask.setOperateStatus(operateStatus);

        // 保存更新后的任务
        return emailTaskRepository.save(emailTask);
    }
//    public EmailTask updateOperateStatus(String emailTaskId, int operateStatus) {
//        // 查找邮件任务
//        EmailTask emailTask = emailTaskRepository.findById(emailTaskId)
//                .orElseThrow(() -> new IllegalArgumentException("邮件任务不存在"));
//
//        // 更新任务操作状态
//        emailTask.setOperateStatus(operateStatus);
//
//        // 保存更新后的任务
//        return emailTaskRepository.save(emailTask);
//    }


    /**
     * 根据 emailTaskId 更新 operateStatus
     *
     * @param emailTaskId   邮件任务ID
     * @param operateStatus 新的操作状态
     * @return 更新后的邮件任务
     * @throws IllegalArgumentException 如果邮件任务不存在或状态无效
     */
    public EmailTask resetOperateStatus(String emailTaskId, int operateStatus) {
        // 查询邮件任务
        EmailTask emailTask = emailTaskRepository.findByEmailTaskId(emailTaskId);
        if (emailTask == null) {
            throw new IllegalArgumentException("邮件任务不存在，emailTaskId: " + emailTaskId);
        }

        // 检查 operateStatus 是否有效（1: 开始态, 2: 暂停态, 3: 终止态, 4: 重置态）
        if (operateStatus < 1 || operateStatus > 4) {
            throw new IllegalArgumentException("无效的操作状态，operateStatus: " + operateStatus);
        }

        // 更新 operateStatus
        emailTask.setOperateStatus(operateStatus);

        // 保存更新后的邮件任务
        return emailTaskRepository.save(emailTask);
    }

    /**
     * 更新邮件任务的状态、主题、模板ID和附件
     *
     * @param emailTaskId   邮件任务ID
     * @param operateStatus 新的操作状态
     * @param subject       新的主题
     * @param templateId    新的模板ID
     * @param attachments   新的附件列表
     * @return 更新后的邮件任务
     */
    public EmailTask updateBirthdayEmailTask(
            String emailTaskId,
            int operateStatus,
            String subject,
            String templateId,
            List<Attachment> attachments) {
        // 查询邮件任务
        EmailTask emailTask = emailTaskRepository.findByEmailTaskId(emailTaskId);
        if (emailTask == null) {
            throw new IllegalArgumentException("邮件任务不存在，emailTaskId: " + emailTaskId);
        }

        // 更新操作状态
        emailTask.setOperateStatus(operateStatus);

        // 更新主题
        emailTask.setSubject(subject);

        // 更新模板ID
        emailTask.setTemplateId(templateId);

        // 更新附件列表
        emailTask.setAttachments(attachments);

        // 保存更新后的邮件任务
        return emailTaskRepository.save(emailTask);
    }

    /**
     * 查询节日发送的邮件任务
     *
     * @return 节日发送的邮件任务列表
     */
    public List<EmailTask> findFestivalTasks() {
        return emailTaskRepository.findByTaskType(3); // 3 表示节日发送
    }

    /**
     * 查询生日发送的邮件任务
     *
     * @return 生日发送的邮件任务列表
     */
    public List<EmailTask> findBirthdayTasks() {
        return emailTaskRepository.findByTaskType(4); // 4 表示生日发送
    }

    /**
     * 创建手动发送的邮件任务
     *
     * @return 创建的任务对象
     */
    public EmailTask createEmailTask(CreateEmailTaskRequest request) {
        EmailTask emailTask = new EmailTask();
        emailTask.setEmailTaskId(UUID.randomUUID().toString());
        emailTask.setSubject(request.getSubject());
        emailTask.setEmailTypeId(request.getEmailTypeId());
        emailTask.setTemplateId(request.getTemplateId());
        emailTask.setEmailContent(request.getEmailContent());
        emailTask.setReceiverIds(request.getReceiverIds());
        emailTask.setReceiverKey(request.getReceiverKey());
        emailTask.setCancelReceiverIds(request.getCancelReceiverIds());
        emailTask.setAttachments(request.getAttachments());
        emailTask.setCreatedAt(System.currentTimeMillis() / 1000);
        emailTask.setOperateStatus(1); // 默认设置为开始态
        emailTask.setTaskStatus(1); // 默认设置为发送中

        return emailTaskRepository.save(emailTask);
    }

    /**
     * 创建循环发送的邮件任务
     *
     * @return 创建的任务对象
     */
    public EmailTask createCycleEmailTask(CreateCycleEmailTaskRequest request) {
        EmailTask emailTask = new EmailTask();
        emailTask.setEmailTaskId(UUID.randomUUID().toString()); // 生成唯一ID
        emailTask.setSubject(request.getSubject());
        emailTask.setEmailTypeId(request.getEmailTypeId());
        emailTask.setTemplateId(request.getTemplateId());
        emailTask.setReceiverIds(request.getReceiverIds());
        emailTask.setAttachments(request.getAttachments());
        emailTask.setTaskCycle(request.getTaskCycle()); // 设置发送周期
        emailTask.setCreatedAt(System.currentTimeMillis() / 1000); // 设置创建时间
        emailTask.setOperateStatus(1); // 默认设置为开始态
        emailTask.setTaskStatus(1); // 默认设置为发送中

        return emailTaskRepository.save(emailTask);
    }

    /**
     * 创建节日发送的邮件任务
     *
     * @return 创建的任务对象
     */
    public EmailTask createFestivalEmailTask(CreateFestivalEmailTaskRequest request) {
        EmailTask emailTask = new EmailTask();
        emailTask.setEmailTaskId(UUID.randomUUID().toString()); // 生成唯一ID
        emailTask.setSubject(request.getSubject());
        emailTask.setTemplateId(request.getTemplateId());
        emailTask.setStartDate(request.getStartDate());
        emailTask.setAttachments(request.getAttachments());

        // 提取收件人ID列表
        List<String> receiverIds = request.getReceivers().stream()
                .map(Receiver::getReceiverId)
                .collect(Collectors.toList());
        emailTask.setReceiverIds(receiverIds);

        emailTask.setCreatedAt(System.currentTimeMillis() / 1000); // 设置创建时间
        emailTask.setOperateStatus(1); // 默认设置为开始态
        emailTask.setTaskStatus(1); // 默认设置为发送中
        emailTask.setTaskType(3); // 设置为节日发送类型

        return emailTaskRepository.save(emailTask);
    }
}