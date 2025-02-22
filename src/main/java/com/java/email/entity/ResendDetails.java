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
 * ResendDetails entity representing the details of email resend operations.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "resend_details")
public class ResendDetails {

    @JsonProperty("accepter_email")
    @Field(name = "accepter_email", type = FieldType.Keyword)
    private String accepterEmail;  // 接受者邮箱

    @Id
    @JsonProperty("email_resend_id")
    @Field(name = "email_resend_id", type = FieldType.Keyword)
    private String emailResendId;  // 重发邮件的id

    @JsonProperty("email_task_id")
    @Field(name = "email_task_id", type = FieldType.Keyword)
    private String emailTaskId;  // 邮件任务id

    @JsonProperty("start_time")
    @Field(name = "start_time", type = FieldType.Long)
    private Long startTime;  // 重发开始时间，秒级时间戳

    @JsonProperty("end_time")
    @Field(name = "end_time", type = FieldType.Long)
    private Long endTime;  // 重发结束时间，秒级时间戳

    @JsonProperty("error_msg")
    @Field(name = "error_msg", type = FieldType.Text)
    private String errorMsg;  // 重发错误信息

    @JsonProperty("status")
    @Field(name = "status", type = FieldType.Integer)
    private Integer status;  // 重发状态，0未重发1重发成功2重发失败

}
