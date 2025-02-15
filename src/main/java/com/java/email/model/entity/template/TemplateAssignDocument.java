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
    @Field(name = "template_id", type = FieldType.Keyword)
    private String templateId;

    @Field(name = "assign_process", type = FieldType.Object)
    private List<Map<String, Object>> assignProcess;
} 