package com.java.email.service;

import com.java.email.model.entity.Attachment;
import com.java.email.model.entity.Customer;
import com.java.email.model.entity.Email;
import com.java.email.model.entity.EmailTask;
import com.java.email.model.request.CreateCycleEmailTaskRequest;
import com.java.email.model.request.CreateEmailTaskRequest;
import com.java.email.model.request.UpdateBirthEmailTaskRequest;
import com.java.email.model.response.GetEmailsByCustomerIdsResponse;
import com.java.email.model.response.GetEmailsBySupplierIdsResponse;
import com.java.email.repository.CustomerRepository;
import com.java.email.repository.EmailRepository;
import com.java.email.repository.EmailTaskRepository;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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
    private CustomerRepository customerRepository;

    @Autowired
    private SupplierService supplierService;

    @Autowired
    private UserService userService;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    private static final String redisQueueName = "TIMER_TASK9001";//redis队列name

    /**
     * 创建普通邮件发送任务
     */
    public String createEmailTask(String currentUserId, CreateEmailTaskRequest request) {
        // Generate UUID for email_task_id
        String emailTaskId = UUID.randomUUID().toString();

        // 存储接受者结果的集合
        List<String> receiverNames = new ArrayList<>();
        List<String> receiverEmails = new ArrayList<>();

        String emailTypeId = request.getEmailTypeId();

        //获取接受者id列表
        List<String> customerId = request.getCustomerId();

        // 假设 customers 是存储 Customer 对象的列表
        List<Customer> customers = customerRepository.findByCustomerIdIn(customerId);

        // 过滤掉那些在 noAcceptEmailTypeId 中包含 emailTypeId 的客户
        List<String> receiverId = customers.stream()
                .filter(customer -> customer.getNoAcceptEmailTypeId() == null || !customer.getNoAcceptEmailTypeId().contains(emailTypeId))
                .map(Customer::getCustomerId)
                .collect(Collectors.toList());

        if (receiverId != null && !receiverId.isEmpty()) {
            // List 不为空
            List<GetEmailsByCustomerIdsResponse> customerEmailsAndNames = customerService.getCustomerEmailsAndNames(receiverId);

            // 遍历并按需求存储
            for (GetEmailsByCustomerIdsResponse response : customerEmailsAndNames) {
                String customerName = response.getCustomerName();
                List<String> emails = response.getCustomerEmails();

                // 将 customerName 添加多次
                for (int i = 0; i < emails.size(); i++) {
                    receiverNames.add(customerName);
                    receiverEmails.add(emails.get(i));
                }
            }
        }

        List<String> receiverSupplierId = request.getReceiverSupplierId();

        if (receiverSupplierId != null && !receiverSupplierId.isEmpty()) {
            // List 不为空
            List<GetEmailsBySupplierIdsResponse> supplierEmailsAndNames = supplierService.getSupplierEmailsAndNames(receiverSupplierId);

            for (GetEmailsBySupplierIdsResponse response : supplierEmailsAndNames) {
                String supplierName = response.getSupplierName();
                List<String> emails = response.getSupplierEmails();

                // 将 customerName 添加多次
                for (int i = 0; i < emails.size(); i++) {
                    receiverNames.add(supplierName);
                    receiverEmails.add(emails.get(i));
                }
            }
        }

        //redis中的key
        String receiverKey = request.getReceiverKey();
        String receiverSupplierKey = request.getReceiverSupplierKey();

        //获取redis中接受者id列表
        List<String> receiverKeyId = new ArrayList<>();
        List<String> receiverKeySupplierId = new ArrayList<>();

        // 根据 receiverKey 从 Redis 中取出存储的值
        ValueOperations<String, Object> operations = redisTemplate.opsForValue();

        if (receiverKey != null) {
            Object cachedReceiverList = operations.get(receiverKey);

            // 如果缓存中有数据，进行处理
            if (cachedReceiverList != null) {
                // 将从 Redis 中取出的对象转换为 List<String>
                List<String> receiverList = (List<String>) cachedReceiverList;

                // 遍历 receiverList，打印每个 receiver_id
                for (String receiverIds : receiverList) {
                    receiverKeyId.add(receiverIds);
                }
            }
        }

        if (receiverSupplierKey != null) {
            Object cachedReceiverSupplierList = operations.get(receiverSupplierKey);

            if (cachedReceiverSupplierList != null) {
                List<String> receiverList = (List<String>) cachedReceiverSupplierList;

                for (String receiverIds : receiverList) {
                    receiverKeySupplierId.add(receiverIds);
                }
            }
        }

        List<GetEmailsByCustomerIdsResponse> customerKeyEmailsAndNames = customerService.getCustomerEmailsAndNames(receiverKeyId);

        List<GetEmailsBySupplierIdsResponse> supplierKeyEmailsAndNames = supplierService.getSupplierEmailsAndNames(receiverKeySupplierId);

        for (GetEmailsByCustomerIdsResponse response : customerKeyEmailsAndNames) {
            String customerName = response.getCustomerName();
            List<String> emails = response.getCustomerEmails();

            // 将 customerName 添加多次
            for (int i = 0; i < emails.size(); i++) {
                receiverNames.add(customerName);
                receiverEmails.add(emails.get(i));
            }
        }

        for (GetEmailsBySupplierIdsResponse response : supplierKeyEmailsAndNames) {
            String supplierName = response.getSupplierName();
            List<String> emails = response.getSupplierEmails();

            // 将 customerName 添加多次
            for (int i = 0; i < emails.size(); i++) {
                receiverNames.add(supplierName);
                receiverEmails.add(emails.get(i));
            }
        }

        String emailContent = request.getEmailContent();

        // 使用 StringBuilder 进行字符串拼接
        StringBuilder emailContentBuilder = new StringBuilder(emailContent);

        // 找到 </body> 标签的位置
        int bodyEndIndex = emailContent.indexOf("</body>");

        // 确保 <body> 标签结束的位置正确
        if (bodyEndIndex != -1) {
            // 获取 <body> 标签结束位置前的内容
            String bodyContent = emailContent.substring(0, bodyEndIndex);

            // 准备附件信息
            StringBuilder attachmentInfoBuilder = new StringBuilder();
            for (Attachment attachment : request.getAttachment()) {
                String attachmentInfo = "附件名称: " + (attachment.getAttachmentName() != null ? attachment.getAttachmentName() : "未知") +
                        ", 附件URL: " + attachment.getAttachmentUrl();
                attachmentInfoBuilder.append("<p>").append(attachmentInfo).append("</p><br>");  // 每个附件信息包裹在 <p> 标签中
            }

            // 将附件信息插入到 <body> 结束标签之前
            emailContentBuilder.replace(bodyEndIndex, bodyEndIndex, attachmentInfoBuilder.toString());
        }

        // 在 emailContent 最后插入 unsubscribe URL
        String unsubscribeUrl = "http://localhost:8080/email-report/unsubscribe?emailTaskId=${emailTaskId}&receiverEmail=${receiverEmail}";
        emailContentBuilder.append("<br><p><a href=\"").append(unsubscribeUrl).append("\">点击此处取消订阅</a></p>");

        // 获取最终的 emailContent
        emailContent = emailContentBuilder.toString();


        // Create EmailTask object
        EmailTask emailTask = new EmailTask();
        emailTask.setEmailTaskId(emailTaskId);
        emailTask.setSubject(request.getSubject());
        emailTask.setEmailTypeId(request.getEmailTypeId());
        emailTask.setTemplateId(request.getTemplateId());
        emailTask.setSenderId(userService.getUserEmailByUserId(currentUserId));
        emailTask.setSenderName(userService.getUserNameByUserId(currentUserId));
        emailTask.setEmailContent(emailContent);
        emailTask.setAttachment(request.getAttachment());
        emailTask.setReceiverName(receiverNames);
        emailTask.setReceiverId(receiverEmails);
        emailTask.setTaskType(1);
        emailTask.setIndex(0L);

        // Set created_at timestamp
        long currentTime = System.currentTimeMillis() / 1000;
        emailTask.setCreatedAt(currentTime);
        emailTask.setStartDate(currentTime);
        // Save to Elasticsearch
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

        redisTemplate.opsForZSet().add(redisQueueName, emailTaskId, currentTime);

        return "Email task created with ID: " + emailTaskId;
    }

    /**
     * 创建循环邮件发送任务
     */
    public String createCycleEmailTask(String currentUserId, CreateCycleEmailTaskRequest request) {
        // Generate UUID for email_task_id
        String emailTaskId = UUID.randomUUID().toString();

        // 存储接受者结果的集合
        List<String> receiverNames = new ArrayList<>();
        List<String> receiverEmails = new ArrayList<>();

        //获取接受者id列表
        List<String> receiverId = request.getReceiverId();
        List<String> receiverSupplierId = request.getReceiverSupplierId();

        if (receiverId != null && !receiverId.isEmpty()) {
            // List 不为空
            List<GetEmailsByCustomerIdsResponse> customerEmailsAndNames = customerService.getCustomerEmailsAndNames(receiverId);

            // 遍历并按需求存储
            for (GetEmailsByCustomerIdsResponse response : customerEmailsAndNames) {
                String customerName = response.getCustomerName();
                List<String> emails = response.getCustomerEmails();

                // 将 customerName 添加多次
                for (int i = 0; i < emails.size(); i++) {
                    receiverNames.add(customerName);
                    receiverEmails.add(emails.get(i));
                }
            }
        }

        if (receiverSupplierId != null && !receiverSupplierId.isEmpty()) {
            // List 不为空
            List<GetEmailsBySupplierIdsResponse> supplierEmailsAndNames = supplierService.getSupplierEmailsAndNames(receiverSupplierId);

            for (GetEmailsBySupplierIdsResponse response : supplierEmailsAndNames) {
                String supplierName = response.getSupplierName();
                List<String> emails = response.getSupplierEmails();

                // 将 customerName 添加多次
                for (int i = 0; i < emails.size(); i++) {
                    receiverNames.add(supplierName);
                    receiverEmails.add(emails.get(i));
                }
            }
        }

        //redis中的key
        String receiverKey = request.getReceiverKey();
        String receiverSupplierKey = request.getReceiverSupplierKey();

        //获取redis中接受者id列表
        List<String> receiverKeyId = new ArrayList<>();
        List<String> receiverKeySupplierId = new ArrayList<>();

        // 根据 receiverKey 从 Redis 中取出存储的值
        ValueOperations<String, Object> operations = redisTemplate.opsForValue();

        if (receiverKey != null) {
            Object cachedReceiverList = operations.get(receiverKey);

            // 如果缓存中有数据，进行处理
            if (cachedReceiverList != null) {
                // 将从 Redis 中取出的对象转换为 List<String>
                List<String> receiverList = (List<String>) cachedReceiverList;

                // 遍历 receiverList，打印每个 receiver_id
                for (String receiverIds : receiverList) {
                    receiverKeyId.add(receiverIds);
                }
            }
        }

        if (receiverSupplierKey != null) {
            Object cachedReceiverSupplierList = operations.get(receiverSupplierKey);

            if (cachedReceiverSupplierList != null) {
                List<String> receiverList = (List<String>) cachedReceiverSupplierList;

                for (String receiverIds : receiverList) {
                    receiverKeySupplierId.add(receiverIds);
                }
            }
        }

        List<GetEmailsByCustomerIdsResponse> customerKeyEmailsAndNames = customerService.getCustomerEmailsAndNames(receiverKeyId);

        List<GetEmailsBySupplierIdsResponse> supplierKeyEmailsAndNames = supplierService.getSupplierEmailsAndNames(receiverKeySupplierId);

        for (GetEmailsByCustomerIdsResponse response : customerKeyEmailsAndNames) {
            String customerName = response.getCustomerName();
            List<String> emails = response.getCustomerEmails();

            // 将 customerName 添加多次
            for (int i = 0; i < emails.size(); i++) {
                receiverNames.add(customerName);
                receiverEmails.add(emails.get(i));
            }
        }

        for (GetEmailsBySupplierIdsResponse response : supplierKeyEmailsAndNames) {
            String supplierName = response.getSupplierName();
            List<String> emails = response.getSupplierEmails();

            // 将 customerName 添加多次
            for (int i = 0; i < emails.size(); i++) {
                receiverNames.add(supplierName);
                receiverEmails.add(emails.get(i));
            }
        }

        String templateId = request.getTemplateId();

        String templateContentById = templateService.getTemplateContentById(templateId);

        // 使用 StringBuilder 进行字符串拼接
        StringBuilder emailContentBuilder = new StringBuilder(templateContentById);

        // 找到 </body> 标签的位置
        int bodyEndIndex = templateContentById.indexOf("</body>");

        // 确保 <body> 标签结束的位置正确
        if (bodyEndIndex != -1) {
            // 获取 <body> 标签结束位置前的内容
            String bodyContent = templateContentById.substring(0, bodyEndIndex);

            // 准备附件信息
            StringBuilder attachmentInfoBuilder = new StringBuilder();
            for (Attachment attachment : request.getAttachment()) {
                String attachmentInfo = "附件名称: " + (attachment.getAttachmentName() != null ? attachment.getAttachmentName() : "未知") +
                        ", 附件URL: " + attachment.getAttachmentUrl();
                attachmentInfoBuilder.append("<p>").append(attachmentInfo).append("</p><br>");  // 每个附件信息包裹在 <p> 标签中
            }

            // 将附件信息插入到 <body> 结束标签之前
            emailContentBuilder.replace(bodyEndIndex, bodyEndIndex, attachmentInfoBuilder.toString());
        }

        // 获取最终的 emailContent
        templateContentById = emailContentBuilder.toString();

        // Create EmailTask object
        EmailTask emailTask = new EmailTask();
        emailTask.setEmailTaskId(emailTaskId);
        emailTask.setEmailId(emailTaskId);
        emailTask.setSubject(request.getSubject());
        emailTask.setEmailTypeId(request.getEmailTypeId());
        emailTask.setTemplateId(request.getTemplateId());
        emailTask.setEmailContent(templateContentById);
        emailTask.setSenderId(userService.getUserEmailByUserId(currentUserId));
        emailTask.setSenderName(userService.getUserEmailByUserId(currentUserId));
        emailTask.setReceiverName(receiverNames);
        emailTask.setReceiverId(receiverEmails);
        emailTask.setAttachment(request.getAttachment());
        emailTask.setIndex(0L);
        emailTask.setTaskType(2);


        // Set created_at timestamp
        long currentTime = System.currentTimeMillis() / 1000;
        emailTask.setCreatedAt(currentTime);

        emailTask.setStartDate(currentTime);

        //获取发送天数
        long sendCycle = request.getSendCycle();

        // 计算结束时间
        long endTime = currentTime + sendCycle * 24 * 60 * 60;
        emailTask.setEndDate(endTime);

        long intervalDate = 60 * 60;

        if (receiverEmails.size() != 0) {
            intervalDate = sendCycle * 24 * 60 * 60 / receiverEmails.size();
        }

        emailTask.setIntervalDate(intervalDate);

        // Save to Elasticsearch
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

        // Save email_task_id to Redis
        redisTemplate.opsForZSet().add(redisQueueName, emailTaskId, currentTime);

        return "Email task created with ID: " + emailTaskId;
    }

    /**
     * 创建节日邮件发送任务
     */
    public String createFestivalEmailTask(String currentUserId, EmailTask request) {
        // Generate UUID for email_task_id
        String emailTaskId = UUID.randomUUID().toString();

        // 存储接受者结果的集合
        List<String> receiverNames = new ArrayList<>();
        List<String> receiverEmails = new ArrayList<>();

        //获取接受者id列表
        List<String> receiverId = request.getReceiverId();
        List<String> receiverSupplierId = request.getReceiverSupplierId();

        if (receiverId != null && !receiverId.isEmpty()) {
            // List 不为空
            List<GetEmailsByCustomerIdsResponse> customerEmailsAndNames = customerService.getCustomerEmailsAndNames(receiverId);

            // 遍历并按需求存储
            for (GetEmailsByCustomerIdsResponse response : customerEmailsAndNames) {
                String customerName = response.getCustomerName();
                List<String> emails = response.getCustomerEmails();

                // 将 customerName 添加多次
                for (int i = 0; i < emails.size(); i++) {
                    receiverNames.add(customerName);
                    receiverEmails.add(emails.get(i));
                }
            }
        }

        if (receiverSupplierId != null && !receiverSupplierId.isEmpty()) {
            // List 不为空
            List<GetEmailsBySupplierIdsResponse> supplierEmailsAndNames = supplierService.getSupplierEmailsAndNames(receiverSupplierId);

            for (GetEmailsBySupplierIdsResponse response : supplierEmailsAndNames) {
                String supplierName = response.getSupplierName();
                List<String> emails = response.getSupplierEmails();

                // 将 customerName 添加多次
                for (int i = 0; i < emails.size(); i++) {
                    receiverNames.add(supplierName);
                    receiverEmails.add(emails.get(i));
                }
            }
        }

        //redis中的key
        String receiverKey = request.getReceiverKey();
        String receiverSupplierKey = request.getReceiverSupplierKey();

        //获取redis中接受者id列表
        List<String> receiverKeyId = new ArrayList<>();
        List<String> receiverKeySupplierId = new ArrayList<>();

        // 根据 receiverKey 从 Redis 中取出存储的值
        ValueOperations<String, Object> operations = redisTemplate.opsForValue();

        if (receiverKey != null) {
            Object cachedReceiverList = operations.get(receiverKey);

            // 如果缓存中有数据，进行处理
            if (cachedReceiverList != null) {
                // 将从 Redis 中取出的对象转换为 List<String>
                List<String> receiverList = (List<String>) cachedReceiverList;

                // 遍历 receiverList，打印每个 receiver_id
                for (String receiverIds : receiverList) {
                    receiverKeyId.add(receiverIds);
                }
            }
        }

        if (receiverSupplierKey != null) {
            Object cachedReceiverSupplierList = operations.get(receiverSupplierKey);

            if (cachedReceiverSupplierList != null) {
                List<String> receiverList = (List<String>) cachedReceiverSupplierList;

                for (String receiverIds : receiverList) {
                    receiverKeySupplierId.add(receiverIds);
                }
            }
        }

        List<GetEmailsByCustomerIdsResponse> customerKeyEmailsAndNames = customerService.getCustomerEmailsAndNames(receiverKeyId);

        List<GetEmailsBySupplierIdsResponse> supplierKeyEmailsAndNames = supplierService.getSupplierEmailsAndNames(receiverKeySupplierId);

        for (GetEmailsByCustomerIdsResponse response : customerKeyEmailsAndNames) {
            String customerName = response.getCustomerName();
            List<String> emails = response.getCustomerEmails();

            // 将 customerName 添加多次
            for (int i = 0; i < emails.size(); i++) {
                receiverNames.add(customerName);
                receiverEmails.add(emails.get(i));
            }
        }

        for (GetEmailsBySupplierIdsResponse response : supplierKeyEmailsAndNames) {
            String supplierName = response.getSupplierName();
            List<String> emails = response.getSupplierEmails();

            // 将 customerName 添加多次
            for (int i = 0; i < emails.size(); i++) {
                receiverNames.add(supplierName);
                receiverEmails.add(emails.get(i));
            }
        }

        String templateId = request.getTemplateId();

        String templateContentById = templateService.getTemplateContentById(templateId);

        // 使用 StringBuilder 进行字符串拼接
        StringBuilder emailContentBuilder = new StringBuilder(templateContentById);

        // 找到 </body> 标签的位置
        int bodyEndIndex = templateContentById.indexOf("</body>");

        // 确保 <body> 标签结束的位置正确
        if (bodyEndIndex != -1) {
            // 获取 <body> 标签结束位置前的内容
            String bodyContent = templateContentById.substring(0, bodyEndIndex);

            // 准备附件信息
            StringBuilder attachmentInfoBuilder = new StringBuilder();
            for (Attachment attachment : request.getAttachment()) {
                String attachmentInfo = "附件名称: " + (attachment.getAttachmentName() != null ? attachment.getAttachmentName() : "未知") +
                        ", 附件URL: " + attachment.getAttachmentUrl();
                attachmentInfoBuilder.append("<p>").append(attachmentInfo).append("</p><br>");  // 每个附件信息包裹在 <p> 标签中
            }

            // 将附件信息插入到 <body> 结束标签之前
            emailContentBuilder.replace(bodyEndIndex, bodyEndIndex, attachmentInfoBuilder.toString());
        }

        // 获取最终的 emailContent
        templateContentById = emailContentBuilder.toString();

        // Create EmailTask object
        EmailTask emailTask = new EmailTask();
        emailTask.setEmailTaskId(emailTaskId);
        emailTask.setSubject(request.getSubject());
        emailTask.setEmailContent(templateContentById);
        emailTask.setEmailTypeId(request.getEmailTypeId());
        emailTask.setTemplateId(request.getTemplateId());
        emailTask.setReceiverName(receiverNames);
        emailTask.setReceiverId(receiverEmails);
        emailTask.setSenderId(userService.getUserEmailByUserId(currentUserId));
        emailTask.setSenderName(userService.getUserEmailByUserId(currentUserId));
        emailTask.setAttachment(request.getAttachment());
        emailTask.setTaskType(3);
        emailTask.setStartDate(request.getStartDate());

        // Set created_at timestamp
        long currentTime = System.currentTimeMillis() / 1000;
        emailTask.setCreatedAt(currentTime);

        // 计算结束时间为当前时间6小时后的时间戳
        long endTime = currentTime + 6 * 60 * 60;
        emailTask.setEndDate(endTime);

        // Save to Elasticsearch
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

        // Save email_task_id to Redis
        redisTemplate.opsForZSet().add(redisQueueName, emailTaskId, currentTime);

        return "Email task created with ID: " + emailTaskId;
    }

    /**
     * 改变生日任务状态
     */
    public String updateBirthEmailTask(String tackId, UpdateBirthEmailTaskRequest request) {
        String templateId = request.getTemplateId();
        String templateContentById = templateService.getTemplateContentById(templateId);

        EmailTask emailTask = new EmailTask();
        emailTask.setEmailTaskId(tackId);
        emailTask.setSubject(request.getSubject());
        emailTask.setTemplateId(request.getTemplateId());
        emailTask.setAttachment(request.getAttachment());
        emailTask.setEmailContent(templateContentById);

        long currentTime = System.currentTimeMillis() / 1000;

        Email email = new Email();
        email.setEmailTaskId(tackId); // Set email_task_id
        email.setCreatedAt(currentTime);  // Set created_at
        email.setUpdateAt(currentTime);   // Set update_at
        email.setEmailStatus(Integer.valueOf(request.getEmailStatus()));          // Set email_status to 1 (开始状态)

        emailTaskRepository.save(emailTask);

        emailRepository.save(email);
        return "Email task updated";
    }
}