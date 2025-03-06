package com.java.email.pojo;

import co.elastic.clients.elasticsearch._types.mapping.Property;
import co.elastic.clients.elasticsearch._types.mapping.TypeMapping;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class UndeliveredEmail {
    /**
     * 邮件id，使用uuid
     */
    @JsonProperty("email_id")
    private String emailId;

    /**
     * 邮件任务id，使用uuid
     */
    @JsonProperty("email_task_id")
    private String emailTaskId;

    /**
     * 发件人id数组，为了支持假用户功能
     * 当前版本数组内只需插入当前用户的id
     */
    @JsonProperty("sender_id")
    private String senderId;

    /**
     * 收件人id数组
     */
    @JsonProperty("receiver_id")
    private String receiverId;

    /**
     * 错误代码，status为2或3时填写
     */
    @JsonProperty("error_code")
    private Integer errorCode;

    /**
     * 错误信息，status为2或3时填写
     */
    @JsonProperty("error_msg")
    private String errorMsg;


    /**
     * 开始时间，秒级时间戳
     */
    @JsonProperty("start_date")
    private long startDate;

    /**
     * 结束时间，秒级时间戳
     */
    @JsonProperty("end_date")
    private long endDate;
    /**
     * 接受者姓名
     */
    @JsonProperty("receiver_name")
    private String receiverName;
    /**
     * 发送者姓名
     */
    @JsonProperty("sender_name")
    private String senderName;

    @JsonProperty("subject")
    private String subject;

    // 定义索引映射
    public static TypeMapping createMapping() {
        return new TypeMapping.Builder()
            .properties("email_id", Property.of(p -> p.keyword(k -> k)))
            .properties("email_task_id", Property.of(p -> p.keyword(k -> k)))
            .properties("sender_id", Property.of(p -> p.keyword(k -> k)))
            .properties("receiver_id", Property.of(p -> p.keyword(k -> k)))
            .properties("error_code", Property.of(p -> p.integer(i -> i)))
            .properties("error_msg", Property.of(p -> p.text(t -> t
                .fields("keyword", f -> f.keyword(k -> k)))))
            .properties("start_date", Property.of(p -> p.long_(l -> l)))
            .properties("end_date", Property.of(p -> p.long_(l -> l)))
            .properties("sender_name", Property.of(p -> p.text(t -> t
                .fields("keyword", f -> f.keyword(k -> k)))))
            .properties("receiver_name", Property.of(p -> p.text(t -> t
                .fields("keyword", f -> f.keyword(k -> k)))))
            .build();
    }
}
