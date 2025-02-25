package com.java.email.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class RsendDetails {
    /**
     * 接受者邮箱
     */
    @JsonProperty("accepter_eamil")
    private String accepterEmail;
    /**
     * 重发邮件的id
     */
    @JsonProperty("email_resend_id")
    private String emailResendId;
    /**
     * 邮件任务id
     */
    @JsonProperty("email_task_id")
    private String emailTaskId;
    /**
     * 重发结束时间，秒级时间戳
     */
    @JsonProperty("end_time")
    private long endTime;
    /**
     * 重发错误信息
     */
    @JsonProperty("error_msg")
    private String errorMsg;
    /**
     * 重发开始时间，秒级时间戳
     */
    @JsonProperty("start_time")
    private long startTime;
    /**
     * 重发状态，0未重发1重发成功2重发失败
     */
    @JsonProperty("status")
    private long status;
}
