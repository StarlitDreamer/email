package com.java.email.service;

import com.java.email.model.request.CreateCycleEmailTaskRequest;
import com.java.email.model.request.CreateEmailTaskRequest;
import com.java.email.model.entity.Email;
import com.java.email.model.entity.EmailTask;
import com.java.email.repository.EmailRepository;
import com.java.email.repository.EmailTaskRepository;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class EmailTaskService {

    @Autowired
    private EmailTaskRepository emailTaskRepository;

    @Autowired
    private EmailRepository emailRepository;

    @Autowired
    private TemplateService templateService;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private  SupplierService supplierService;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    private static final String redisQueueName = "TIMER_TASK9001";//redis队列name

    /**
     * 创建普通邮件发送任务
     */
    public String createEmailTask(CreateEmailTaskRequest request) {
        // Generate UUID for email_task_id
        String emailTaskId = UUID.randomUUID().toString();

        //获取接受者id列表
        List<String> receiverId = request.getReceiverId();
        List<String> receiverSupplierId = request.getReceiverSupplierId();

        // 使用 HashSet 合并并去重
        Set<String> allUniqueEmails = new HashSet<>();


        // 接受者邮箱列表
        List<String> receiverEmails= new ArrayList<>(allUniqueEmails);

        String receiverKey = request.getReceiverKey();
        String receiverSupplierKey = request.getReceiverSupplierKey();

        // Create EmailTask object
        EmailTask emailTask = new EmailTask();
        emailTask.setEmailTaskId(emailTaskId);
        emailTask.setSenderId(request.getSenderId());
        emailTask.setSubject(request.getSubject());
        emailTask.setEmailTypeId(request.getEmailTypeId());
        emailTask.setTemplateId(request.getTemplateId());
        emailTask.setEmailContent(request.getEmailContent());
        emailTask.setReceiverId(receiverEmails);
        emailTask.setReceiverSupplierId(request.getReceiverSupplierId());
        emailTask.setAttachment(request.getAttachment());
        emailTask.setTaskType(1);
        emailTask.setIndex(0L);

        // Set created_at timestamp
        long currentTime = System.currentTimeMillis() / 1000;
        emailTask.setCreatedAt(currentTime);

        // Save to Elasticsearch
        emailTaskRepository.save(emailTask);

        // Create Email object for the "email" index
        Email email = new Email();
        email.setEmailTaskId(emailTaskId); // Set email_task_id
        email.setCreatedAt(currentTime);  // Set created_at
        email.setUpdateAt(currentTime);   // Set update_at
        email.setEmailStatus(1);          // Set email_status to 1 (开始状态)

        // Save Email to Elasticsearch
        emailRepository.save(email);

        redisTemplate.opsForZSet().add(redisQueueName, emailTaskId, currentTime);

        return "Email task created with ID: " + emailTaskId;
    }

    /**
     * 创建循环邮件发送任务
     */
    public String createCycleEmailTask(CreateCycleEmailTaskRequest request) {
        // Generate UUID for email_task_id
        String emailTaskId = UUID.randomUUID().toString();

        String templateId = request.getTemplateId();

        String templateContentById = templateService.getTemplateContentById(templateId);

        // Create EmailTask object
        EmailTask emailTask = new EmailTask();
        emailTask.setEmailTaskId(emailTaskId);
        emailTask.setEmailId(emailTaskId);
        emailTask.setSubject(request.getSubject());
        emailTask.setEmailTypeId(request.getEmailTypeId());
        emailTask.setTemplateId(request.getTemplateId());
        emailTask.setEmailContent(templateContentById);
        emailTask.setReceiverId(request.getReceiverId());
        emailTask.setReceiverSupplierId(request.getReceiverSupplierId());
        emailTask.setReceiverKey(request.getReceiverKey());
        emailTask.setReceiverSupplierKey(request.getReceiverSupplierKey());
        emailTask.setAttachment(request.getAttachment());
        emailTask.setIndex(0L);
        emailTask.setSenderId(request.getSenderId());
        emailTask.setTaskType(2);


        // Set created_at timestamp
        long currentTime = System.currentTimeMillis() / 1000;
        emailTask.setCreatedAt(currentTime);

        emailTask.setStartDate(currentTime);

        //获取发送天数
        long sendCycle = request.getSendCycle();

        // 计算结束时间为当前时间6小时后的时间戳
        long endTime = currentTime + sendCycle * 24 * 60 * 60;
        emailTask.setEndDate(endTime);


        emailTask.setIntervalDate(sendCycle * 24 * 60 * 60);
        // Save to Elasticsearch
        emailTaskRepository.save(emailTask);


        // Create Email object for the "email" index
        Email email = new Email();
        email.setEmailTaskId(emailTaskId); // Set email_task_id
        email.setCreatedAt(currentTime);  // Set created_at
        email.setUpdateAt(currentTime);   // Set update_at
        email.setEmailStatus(1);          // Set email_status to 1 (开始状态)

        // Save Email to Elasticsearch
        emailRepository.save(email);

        // Save email_task_id to Redis
//        redisTemplate.opsForValue().set("email_task_id:" + emailTaskId, emailTaskId);
        redisTemplate.opsForZSet().add(redisQueueName, emailTaskId, currentTime);

        return "Email task created with ID: " + emailTaskId;
    }

    /**
     * 创建节日邮件发送任务
     */
    public String createFestivalEmailTask(EmailTask request) {
        // Generate UUID for email_task_id
        String emailTaskId = UUID.randomUUID().toString();

        // Create EmailTask object
        EmailTask emailTask = new EmailTask();
        emailTask.setEmailTaskId(emailTaskId);
        emailTask.setSubject(request.getSubject());
        emailTask.setEmailTypeId(request.getEmailTypeId());
        emailTask.setTemplateId(request.getTemplateId());
        emailTask.setReceiverId(request.getReceiverId());
        emailTask.setReceiverSupplierId(request.getReceiverSupplierId());
        emailTask.setReceiverKey(request.getReceiverKey());
        emailTask.setReceiverSupplierKey(request.getReceiverSupplierKey());
        emailTask.setAttachment(request.getAttachment());
        emailTask.setTaskType(request.getTaskType());
        emailTask.setStartDate(request.getStartDate());

        // Set created_at timestamp
        long currentTime = System.currentTimeMillis() / 1000;
        emailTask.setCreatedAt(currentTime);

        // 计算结束时间为当前时间6小时后的时间戳
//        long endTime = currentTime + 6 * 60 * 60;
//        emailTask.setEndDate(endTime);

        // Save to Elasticsearch
        emailTaskRepository.save(emailTask);


        // Create Email object for the "email" index
        Email email = new Email();
        email.setEmailTaskId(emailTaskId); // Set email_task_id
        email.setCreatedAt(currentTime);  // Set created_at
        email.setUpdateAt(currentTime);   // Set update_at
        email.setEmailStatus(1);          // Set email_status to 1 (开始状态)

        // Save Email to Elasticsearch
        emailRepository.save(email);

        // Save email_task_id to Redis
//        redisTemplate.opsForValue().set("email_task_id:" + emailTaskId, emailTaskId);
        redisTemplate.opsForValue().set(emailTaskId, "youjian");

        return "Email task created with ID: " + emailTaskId;
    }
}