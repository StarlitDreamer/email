package com.java.email.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class EmailManage {
    /**
     * 创建时间，秒级时间戳
     */
    @JsonProperty("created_at")
    private Long createdAt;

    /**
     * 邮件id，使用uuid
     */
    @JsonProperty("email_id")
    private String emailId;

    /**
     * 邮件状态，使用数字1开始 2暂停 3终止 4重置 5异常 6完成
     */
    @JsonProperty("email_status")
    private Long emailStatus;

    /**
     * 邮件任务id，使用uuid
     */
    @JsonProperty("email_task_id")
    private String emailTaskId;

    /**
     * 状态修改时间，秒级时间戳
     */
    @JsonProperty("update_at")
    private Long updateAt;
}
