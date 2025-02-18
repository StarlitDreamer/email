package com.java.email.model.entity.template;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.List;
import java.util.Map;

@Data
@Document(indexName = "template_assign")
public class TemplateAssignDocument {
    
    @Id
    private String templateId;

    @Field(type = FieldType.Object)
    private List<Map<String, Object>> assignProcess;
} 