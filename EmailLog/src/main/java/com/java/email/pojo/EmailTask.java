package com.java.email.pojo;



import co.elastic.clients.elasticsearch._types.mapping.Property;
import co.elastic.clients.elasticsearch._types.mapping.TypeMapping;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;


@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class EmailTask {
    @JsonProperty("attachment")
    private Attachment[] attachment;

    @JsonProperty("bounce_amount")
    private int bounceAmount; // ES 是 integer

    @JsonProperty("created_at")
    private long createdAt;

    @JsonProperty("email_content")
    private String emailContent;

    @JsonProperty("email_id")
    private String emailId;

    @JsonProperty("email_task_id")
    private String emailTaskId;

    @JsonProperty("email_type_id")
    private String emailTypeId;

    @JsonProperty("end_date")
    private long endDate;

    @JsonProperty("index")
    private int index; // ES 是 integer

    @JsonProperty("interval_date")
    private int intervalDate; // ES 是 integer

    @JsonProperty("receiver_id")
    private String[] receiverId;

    @JsonProperty("sender_id")
    private String senderId;

    @JsonProperty("shadowId")
    private String[] shadowId;

    @JsonProperty("start_date")
    private long startDate;

    @JsonProperty("subject")
    private String subject;

    @JsonProperty("task_cycle")
    private Integer taskCycle;

    @JsonProperty("task_type")
    private Integer taskType;

    @JsonProperty("template_id")
    private String templateId;

    @JsonProperty("unsubscribe_amount")
    private int unsubscribeAmount; // ES 是 integer

    @JsonProperty("sender_name")
    private String senderName; // 修改首字母小写

    @JsonProperty("receiver_name")
    private String[] receiverName; // 修改首字母小写

}
