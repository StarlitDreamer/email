package com.java.email.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "email_details")
public class EmailDetail {

    @Id
    @Field(name = "email_id", type = FieldType.Keyword)
    private String emailId;  // 邮件ID

    @JsonProperty("email_task_id")
    @Field(name = "email_task_id", type = FieldType.Keyword)
    private String emailTaskId;  // 邮件任务ID

    @JsonProperty("start_date")
    @Field(name = "start_date", type = FieldType.Long)
    private Long startDate;  // 开始时间，秒级时间戳

    @JsonProperty("end_date")
    @Field(name = "end_date", type = FieldType.Long)
    private Long endDate;  // 结束时间，秒级时间戳

    @JsonProperty("error_code")
    @Field(name = "error_code", type = FieldType.Integer)
    private Integer errorCode;  // 状态码，200成功，500失败

    @JsonProperty("error_msg")
    @Field(name = "error_msg", type = FieldType.Text)
    private String errorMsg;  // 错误信息，失败的错误信息

    @JsonProperty("receiver_id")
    @Field(name = "receiver_id", type = FieldType.Keyword)
    private String receiverId;  // 收件人ID，收件人邮箱

    @JsonProperty("receiver_name")
    @Field(name = "receiver_name", type = FieldType.Text)
    private String receiverName;  // 收件人姓名

    @JsonProperty("sender_id")
    @Field(name = "sender_id", type = FieldType.Keyword)
    private String senderId;  // 发件人ID，发件人邮箱

    @JsonProperty("sender_name")
    @Field(name = "sender_name", type = FieldType.Text)
    private String senderName;  // 发送者姓名

    @JsonProperty("empty")
    @Field(name = "empty", type = FieldType.Keyword)
    private String empty;  // 空字段，不分词
}
