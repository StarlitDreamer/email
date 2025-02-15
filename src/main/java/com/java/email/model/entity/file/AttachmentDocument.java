package com.java.email.model.entity.file;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.DateFormat;

import java.util.List;

@Data
@Document(indexName = "attachment")
public class AttachmentDocument {
    @Id
    @Field(name = "attachment_id", type = FieldType.Keyword)
    private String attachmentId;

    @Field(name = "attachment_url", type = FieldType.Keyword)
    private String attachmentUrl;

    @Field(name = "attachment_size", type = FieldType.Long)
    private Long attachmentSize;

    @Field(name = "attachment_name", type = FieldType.Text, analyzer = "ik_max_word")
    private String attachmentName;

    @Field(name = "creator_id", type = FieldType.Keyword)
    private String creatorId;

    @Field(name = "belong_user_id", type = FieldType.Keyword)
    private List<String> belongUserId;

    @Field(name = "status", type = FieldType.Integer)
    private Integer status;

    @Field(name = "created_at", type = FieldType.Long)
    private Long createdAt;

    @Field(name = "updated_at", type = FieldType.Long)
    private Long updatedAt;
} 