package com.java.email.Dto;

import com.java.email.entity.Attachment;
import lombok.Data;

import java.util.List;
//更新生日邮件任务的状态、主题、模板ID和附件
@Data
public class UpdateBirthdayEmailTaskRequest {
    private int operateStatus;  // 操作状态
    private String subject;     // 主题
    private String templateId;  // 模板ID
    private List<Attachment> attachments; // 附件列表
}
