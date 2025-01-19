package com.java.email.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

import java.util.List;

/**
 * Attachment entity representing an attachment.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "attachments")
public class Attachment {
    @Id
    private String attachmentId;        // 附件ID
    private String attachmentName;      // 附件名称
    private long attachmentSize;        // 附件大小，单位为字节
    private String attachmentUrl;       // 附件URL
    private List<String> belongUserId;  // 所属用户ID列表  ownerUserIds
    private long createdAt;             // 创建日期
    private String creatorId;           // 创建人ID
    private int status;    // 分配状态 1:未分配 2:已分配
    private long updatedAt;             // 更新日期
}