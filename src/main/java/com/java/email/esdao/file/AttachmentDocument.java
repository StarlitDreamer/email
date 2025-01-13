package com.java.email.esdao.file;

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
    private String attachmentId;

    @Field(type = FieldType.Keyword)
    private String attachmentUrl;

    @Field(type = FieldType.Keyword)
    private String attachmentSize;

    @Field(type = FieldType.Text, analyzer = "ik_max_word")
    private String attachmentName;

    @Field(type = FieldType.Keyword)
    private String creatorId;

    @Field(type = FieldType.Keyword)
    private List<String> belongUserId;

    @Field(type = FieldType.Integer)
    private Integer status;

    @Field(type = FieldType.Date, format = DateFormat.date_time)
    private String createdAt;

    @Field(type = FieldType.Date, format = DateFormat.date_time)
    private String updatedAt;
} 