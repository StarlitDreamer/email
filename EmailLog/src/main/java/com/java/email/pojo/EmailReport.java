package com.java.email.pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class EmailReport {
    /**
     * 退信数量
     */
    @JsonProperty("bounce_amount")
    private long bounceAmount;
    /**
     * 送达数量
     */
    @JsonProperty("delivery_amount")
    private long deliveryAmount;
    /**
     * 任务id
     */
    @JsonProperty("email_task_id")
    private String emailTaskId;
    /**
     * 具体邮件总数
     */
    @JsonProperty("email_total")
    private long emailTotal;
    /**
     * 打开数量
     */
    @JsonProperty("open_amount")
    private long openAmount;
    /**
     * 退订数量
     */
    @JsonProperty("unsubscribe_amount")
    private long unsubscribeAmount;
}
