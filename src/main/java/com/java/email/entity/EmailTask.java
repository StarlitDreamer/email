package com.java.email.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.List;

/**
 * 邮件任务管理实体类。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "email_task")
public class EmailTask {
    @JsonProperty("email_id")
    @Field(name = "email_id", type = FieldType.Keyword)
    private String emailId;               // 邮件ID，使用UUID，不分词

    @JsonProperty("email_task_id")
    @Id
    @Field(name = "email_task_id", type = FieldType.Keyword)
    private String emailTaskId;           // 邮件任务ID，使用UUID，不分词

    @JsonProperty("email_type_id")
    @Field(name = "email_type_id", type = FieldType.Keyword)
    private String emailTypeId;           // 邮件类型ID，字典里的邮件类型ID，不分词

    @JsonProperty("subject")
    @Field(name = "subject", type = FieldType.Text)
    private String subject;               // 邮件主题，支持分词

    @JsonProperty("email_content")
    @Field(name = "email_content", type = FieldType.Text)
    private String emailContent;          // 邮件内容，支持全文检索

    @JsonProperty("created_at")
    @Field(name = "created_at", type = FieldType.Long)
    private Long createdAt;               // 创建时间，秒级时间戳

    @JsonProperty("start_date")
    @Field(name = "start_date", type = FieldType.Long)
    private Long startDate;               // 开始时间，秒级时间戳

    @JsonProperty("end_date")
    @Field(name = "end_date", type = FieldType.Long)
    private Long endDate;                 // 结束时间，秒级时间戳

    @JsonProperty("task_cycle")
    @Field(name = "task_cycle", type = FieldType.Long)
    private Long taskCycle;               // 任务循环周期，循环发送任务填写

    @JsonProperty("task_type")
    @Field(name = "task_type", type = FieldType.Integer)
    private Integer taskType;             // 任务类型：1-普通邮件，2-循环邮件，3-定时发送，4-生日发送

    @JsonProperty("index")
    @Field(name = "index", type = FieldType.Long)
    private Long index;                   // 循环邮件的发送下标，初始值为0

    @JsonProperty("interval_date")
    @Field(name = "interval_date", type = FieldType.Long)
    private Long intervalDate;            // 循环邮件的间隔时间，秒级时间戳

    @JsonProperty("receiver_id")
    @Field(name = "receiver_id", type = FieldType.Keyword)
    private List<String> receiverId;      // 收件人ID列表，不分词

    @JsonProperty("receiver_name")
    @Field(name = "receiver_name", type = FieldType.Text)
    private List<String> receiverName;    // 收件人姓名列表，支持分词

    @JsonProperty("sender_id")
    @Field(name = "sender_id", type = FieldType.Keyword)
    private String senderId;              // 发件人ID，不分词

    @JsonProperty("sender_name")
    @Field(name = "sender_name", type = FieldType.Text)
    private String senderName;            // 发送者姓名，支持分词

    @JsonProperty("attachment")
    @Field(name = "attachment", type = FieldType.Nested)
    private List<Attachment> attachment;  // 附件列表，包含多个附件

    @JsonProperty("bounce_amount")
    @Field(name = "bounce_amount", type = FieldType.Long)
    private Long bounceAmount;            // 退信数量

    @JsonProperty("unsubscribe_amount")
    @Field(name = "unsubscribe_amount", type = FieldType.Long)
    private Long unsubscribeAmount;       // 退订数量

    @JsonProperty("shadow_id")
    @Field(name = "shadow_id", type = FieldType.Keyword)
    private String shadowId;              // 僵尸用户邮箱号，此版本不用，不分词

    @JsonProperty("template_id")
    @Field(name = "template_id", type = FieldType.Keyword)
    private String templateId;            // 模板ID，不分词

//    @JsonProperty("interval_date")
//    @Field(name = "interval_date", type = FieldType.Long)
//    private Long intervalDate;            // 循环邮件的间隔时间，秒级时间戳


//
//    @Id
//    @JsonProperty("email_id")
//    private String emailId; // 邮件id
//
//    @JsonProperty("email_task_id")
//    @Field(name = "email_task_id", type = FieldType.Keyword)
//    private String emailTaskId; // 邮件任务id
//
//    @JsonProperty("email_type_id")
//    @Field(name = "email_type_id", type = FieldType.Keyword)
//    private String emailTypeId; // 邮件类型id
//
//    @JsonProperty("task_type")
//    @Field(name = "task_type", type = FieldType.Integer)
//    private Integer taskType;  // 任务类型
//
//    @JsonProperty("task_cycle")
//    @Field(name = "task_cycle", type = FieldType.Integer)
//    private Integer taskCycle; // 任务循环周期
//
//    @JsonProperty("sender_id")
//    @Field(name = "sender_id", type = FieldType.Keyword)
//    private String senderId; // 发件人id
//
//    @JsonProperty("shadow_id")
//    @Field(name = "shadow_id", type = FieldType.Keyword)
//    private ArrayList<String> shadowId; // 影子用户id
//
//    @JsonProperty("receiver_id")
//    @Field(name = "receiver_id", type = FieldType.Keyword)
//    private ArrayList<String> receiverId; // 收件人id
//
//    @JsonProperty("attachment")
//    @Field(name = "attachment", type = FieldType.Keyword)
//    private String attachment; // 附件的url
//
//    @JsonProperty("template_id")
//    @Field(name = "template_id", type = FieldType.Keyword)
//    private String templateId; // 模板id
//
//    @JsonProperty("subject")
//    @Field(name = "subject", type = FieldType.Keyword)
//    private String subject; // 邮件主题
//
//    @JsonProperty("email_content")
//    @Field(name = "email_content", type = FieldType.Keyword)
//    private String emailContent; // 邮件内容
//
//    @JsonProperty("bounce_amount")
//    @Field(name = "bounce_amount", type = FieldType.Integer)
//    private Integer bounceAmount; // 退信数量
//
//    @JsonProperty("unsubscribe_amount")
//    @Field(name = "unsubscribe_amount", type = FieldType.Integer)
//    private Integer unsubscribeAmount; // 退订数量
//
//    @JsonProperty("created_at")
//    @Field(name = "created_at", type = FieldType.Long)
//    private Long createdAt; // 任务创建时间
//
//    @JsonProperty("start_date")
//    @Field(name = "start_date", type = FieldType.Long)
//    private Long startDate; // 邮件任务开始时间
//
//    @JsonProperty("end_date")
//    @Field(name = "end_date", type = FieldType.Long)
//    private Long endDate; // 邮件任务的结束时间
//
//    @JsonProperty("interval_date")
//    @Field(name = "interval_date", type = FieldType.Integer)
//    private Integer intervalDate; // 任务的间隔时间，秒级时间戳
//
//    @JsonProperty("index")
//    @Field(name = "index", type = FieldType.Integer)
//    private Integer index; // 循环任务的发送下标


//    @Id
//    @JsonProperty("email_task_id")
//    private String emailTaskId; // 邮件任务ID，使用UUID
//
//    @JsonProperty("attachments")
//    private List<Attachment> attachments; // 附件列表
//
//    @JsonProperty("bounce_amount")
//    private long bounceAmount; // 退信数量
//
//    @JsonProperty("created_at")
//    private long createdAt; // 创建时间，秒级时间戳
//
//    @JsonProperty("email_content")
//    @Field(name = "email_content", type = FieldType.Keyword)
//    private String emailContent; // 邮件内容

//    @JsonProperty("email_type_id")
//    private String emailTypeId; // 邮件类型ID，字典里面的邮件类型ID

//    @JsonProperty("end_date")
//    private long endDate; // 结束时间，秒级时间戳
//
//    @JsonProperty("operate_status")
//    private int operateStatus; // 任务操作状态
//
//    @JsonProperty("shadow_id")
//    @Field(name = "shadow_id", type = FieldType.Keyword)
//    private ArrayList<String> shadowId; // 影子用户id
//
//    @JsonProperty("receiver_id")
//    @Field(name = "receiver_id", type = FieldType.Keyword)
//    private ArrayList<String> receiverId; // 收件人id
//
//
//    @JsonProperty("receiver_key")
//    private String receiverKey; // 收件人在redis中的key

//    @JsonProperty("sender_id")
//    @Field(name = "sender_id", type = FieldType.Keyword)
//    private String senderId; // 发件人id

//    @JsonProperty("start_date")
//    @Field(name = "start_date", type = FieldType.Long)
//    private Long startDate; // 邮件任务开始时间
//
//    @JsonProperty("index")
//    @Field(name = "index", type = FieldType.Integer)
//    private Integer index; // 循环任务的发送下标
//
//    @JsonProperty("subject")
//    @Field(name = "subject", type = FieldType.Keyword)
//    private String subject; // 邮件主题

//    @JsonProperty("task_cycle")
//    private long taskCycle; // 任务循环周期

//    @JsonProperty("task_status")
//    private int taskStatus; // 任务状态
//
//    @JsonProperty("task_type")
//    private int taskType; // 任务类型

//    @JsonProperty("template_id")
//    private String templateId; // 模板ID

//    @JsonProperty("unsubscribe_amount")
//    private long unsubscribeAmount; // 退订数量




//    private String emailTaskId;         // 邮件任务ID，使用UUID
//    private List<Attachment> attachments; // 附件列表
//    private long bounceAmount;          // 退信数量
//    private long createdAt;             // 创建时间，秒级时间戳
//    private String emailContent;        // 邮件内容
//    private String emailTypeId;         // 邮件类型ID，字典里面的邮件类型ID
//    private long endDate;               // 结束时间，秒级时间戳
//    private int operateStatus;          // 任务操作状态，使用数字1、2、3、4代表状态。1是开始态、2是暂停态、3是终止态、4是重置态
//    private List<String> receiverIds;   // 收件人ID列表
//    private List<String> cancelReceiverIds;//取消收件的收件人ID列表             新加
//    private String receiverKey;         //收件人在redis中的key                 新加
//    private List<String> senderIds;     // 发件人ID列表
//    private long startDate;             // 开始时间，秒级时间戳
//    private String subject;             // 主题
//    private long taskCycle;             // 任务循环周期，如果是循环发送类型的任务才会填写这个字段
//    private int taskStatus;             // 任务状态，使用数字1、2、3、4、5、6代表任务状态。1是发送中、2是发送暂停、3是发送终止、4是发送重置、5是发送成功、6是发送失败
//    private int taskType;               // 任务类型，使用数字1、2、3、4代表任务类型。1是手动发送、2是循环发送、3是节日发送、4是生日发送
//    private String templateId;          // 模板ID
//    private long unsubscribeAmount;     // 退订数量

//        @Id
//        private String email_id; //邮件id
//
//        @Field(type = FieldType.Keyword)
//        private String email_task_id; //邮件任务id
//
//        @Field(type = FieldType.Keyword)
//        private String email_type_id; //邮件类型id
//
//        @Field(type = FieldType.Integer)
//        private Integer task_type; //任务类型
//
//        @Field(type = FieldType.Integer)
//        private Integer task_cycle; //任务循环周期
//
//        @Field(type = FieldType.Keyword)//发件人id
//        private String sender_id;
//
//        @Field(type = FieldType.Keyword)
//        private ArrayList<String> shadow_id; //影子用户id
//
//        @Field(type = FieldType.Keyword)
//        private ArrayList<String> receiver_id; //收件人id
//
//        @Field(type = FieldType.Keyword)
//        private String attachment; //附件的url
//
//        @Field(type = FieldType.Keyword)
//        private String template_id; //模板id
//
//        @Field(type = FieldType.Keyword)
//        private String subject; //邮件主题
//
//        @Field(type = FieldType.Keyword)
//        private String email_content; //邮件内容
//
//        @Field(type = FieldType.Integer)
//        private Integer bounce_amount; //退信数量
//
//        @Field(type = FieldType.Integer)
//        private Integer unsubscribe_amount; //退订数量
//
//        @Field(type = FieldType.Long)
//        private Long created_at; //任务创建时间
//
//        @Field(type = FieldType.Long)
//        private Long start_date; //邮件任务开始时间
//
//        @Field(type = FieldType.Long)
//        private Long end_date; //邮件任务的结束时间
//
//        @Field(type = FieldType.Integer)
//        private Integer interval_date; //任务的间隔时间，秒级时间戳
//
//        @Field(type = FieldType.Integer)
//        private Integer index; //循环任务的发送下标;


//    /**
//     * 设置邮件任务ID。
//     *
//     * @param value 邮件任务ID，不能为 null 或空。
//     * @throws IllegalArgumentException 如果邮件任务ID为 null 或空。
//     */
//    public void setEmailTaskId(String value) {
//        if (value == null || value.isEmpty()) {
//            throw new IllegalArgumentException("Email task ID cannot be null or empty");
//        }
//        this.emailTaskId = value;
//    }
//
//    /**
//     * 设置邮件类型ID。
//     *
//     * @param value 邮件类型ID，不能为 null 或空。
//     * @throws IllegalArgumentException 如果邮件类型ID为 null 或空。
//     */
//    public void setEmailTypeId(String value) {
//        if (value == null || value.isEmpty()) {
//            throw new IllegalArgumentException("Email type ID cannot be null or empty");
//        }
//        this.emailTypeId = value;
//    }
//
//    /**
//     * 设置模板ID。
//     *
//     * @param value 模板ID，不能为 null 或空。
//     * @throws IllegalArgumentException 如果模板ID为 null 或空。
//     */
//    public void setTemplateId(String value) {
//        if (value == null || value.isEmpty()) {
//            throw new IllegalArgumentException("Template ID cannot be null or empty");
//        }
//        this.templateId = value;
//    }
//
//    /**
//     * 设置任务操作状态。
//     *
//     * @param value 任务操作状态，取值范围为1到4。
//     * @throws IllegalArgumentException 如果任务操作状态不在有效范围内。
//     */
//    public void setOperateStatus(int value) {
//        if (value < 1 || value > 4) {
//            throw new IllegalArgumentException("Operate status must be between 1 and 4");
//        }
//        this.operateStatus = value;
//    }
//
//    /**
//     * 设置任务状态。
//     *
//     * @param value 任务状态，取值范围为1到6。
//     * @throws IllegalArgumentException 如果任务状态不在有效范围内。
//     */
//    public void setTaskStatus(int value) {
//        if (value < 1 || value > 6) {
//            throw new IllegalArgumentException("Task status must be between 1 and 6");
//        }
//        this.taskStatus = value;
//    }
//
//    /**
//     * 设置任务类型。
//     *
//     * @param value 任务类型，取值范围为1到4。
//     * @throws IllegalArgumentException 如果任务类型不在有效范围内。
//     */
//    public void setTaskType(int value) {
//        if (value < 1 || value > 4) {
//            throw new IllegalArgumentException("Task type must be between 1 and 4");
//        }
//        this.taskType = value;
//    }
}
