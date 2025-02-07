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
 * Attachment entity representing an attachment.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "attachment")
public class Attachment {
    @Id
    @JsonProperty("attachment_id")
    @Field(name = "attachment_id", type = FieldType.Keyword)
    private String attachmentId;        // 附件ID

    @JsonProperty("attachment_name")
    @Field(name = "attachment_name", type = FieldType.Text)
    private String attachmentName;      // 附件名称

    @JsonProperty("attachment_size")
    @Field(name = "attachment_size", type = FieldType.Long)
    private long attachmentSize;        // 附件大小，单位为字节

    @JsonProperty("attachment_url")
    @Field(name = "attachment_url", type = FieldType.Keyword)
    private String attachmentUrl;       // 附件URL

    @JsonProperty("belong_user_id")
    @Field(name = "belong_user_id", type = FieldType.Keyword)
    private List<String> belongUserId;  // 所属用户ID列表 ownerUserIds

    @JsonProperty("created_at")
    @Field(name = "created_at", type = FieldType.Long)
    private long createdAt;             // 创建日期

    @JsonProperty("creator_id")
    @Field(name = "creator_id", type = FieldType.Keyword)
    private String creatorId;           // 创建人ID

    @JsonProperty("status")
    @Field(name = "status", type = FieldType.Integer)
    private int status;                 // 分配状态 1:未分配 2:已分配

    @JsonProperty("updated_at")
    @Field(name = "updated_at", type = FieldType.Long)
    private long updatedAt;             // 更新日期
//    @Id
//    @JsonProperty("attachment_id")
//    private String attachmentId;        // 附件ID
//
//    @JsonProperty("attachment_name")
//    private String attachmentName;      // 附件名称
//
//    @JsonProperty("attachment_size")
//    private long attachmentSize;        // 附件大小，单位为字节
//
//    @JsonProperty("attachment_url")
//    private String attachmentUrl;       // 附件URL
//
//    @JsonProperty("belong_user_id")
//    private List<String> belongUserId;  // 所属用户ID列表 ownerUserIds
//
//    @JsonProperty("created_at")
//    private long createdAt;             // 创建日期
//
//    @JsonProperty("creator_id")
//    private String creatorId;           // 创建人ID
//
//    @JsonProperty("status")
//    private int status;    // 分配状态 1:未分配 2:已分配
//
//    @JsonProperty("updated_at")
//    private long updatedAt;             // 更新日期
//    @Id
//    @JsonProperty("")
//    private String attachmentId;        // 附件ID
//    private String attachmentName;      // 附件名称
//    private long attachmentSize;        // 附件大小，单位为字节
//    private String attachmentUrl;       // 附件URL
//    private List<String> belongUserId;  // 所属用户ID列表  ownerUserIds
//    private long createdAt;             // 创建日期
//    private String creatorId;           // 创建人ID
//    private int status;    // 分配状态 1:未分配 2:已分配
//    private long updatedAt;             // 更新日期
}