package com.java.email.model.entity.file;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.List;
import java.util.Map;

@Data
@Document(indexName = "attachment_assign")
public class AttachmentAssignDocument {
    
    @Id
    @Field(name = "attachment_id", type = FieldType.Keyword)
    private String attachmentId;

    @Field(name = "assign_process", type = FieldType.Object)
    private List<Map<String, Object>> assignProcess;
} 