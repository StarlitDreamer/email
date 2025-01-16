package com.java.email.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

/**
 * Attachment entity representing an attachment.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Attachment {
    private String attachmentId;        // 附件ID
    private String attachmentName;      // 附件名称
    private long attachmentSize;        // 附件大小，单位为字节
    private String attachmentUrl;       // 附件URL
    private List<String> ownerUserIds;  // 所属用户ID列表
    private long createdAt;             // 创建日期
    private String creatorId;           // 创建人ID
    private AttachmentStatus status;    // 分配状态 1:未分配 2:已分配
    private long updatedAt;             // 更新日期
}

/**
 * 附件状态枚举
 */
enum AttachmentStatus {
    UNASSIGNED(1),  // 未分配
    ASSIGNED(2);    // 已分配

    private final int code;

    AttachmentStatus(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
