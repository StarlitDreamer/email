package com.java.email.model.entity.template;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.List;

@Data
@Document(indexName = "template")
public class TemplateDocument {
    @Id
    @Field(name = "id", type = FieldType.Keyword)
    private String id;

    @Field(name = "template_id", type = FieldType.Keyword)
    private String templateId;

    @Field(name = "template_name", type = FieldType.Text, analyzer = "ik_max_word")
    private String templateName;

    @Field(name = "template_type_id", type = FieldType.Keyword)
    private String templateTypeId;

    @Field(name = "template_content", type = FieldType.Text)
    private String templateContent;

    @Field(name = "creator_id", type = FieldType.Keyword)
    private String creatorId;

    @Field(name = "creator_name", type = FieldType.Text, analyzer = "ik_max_word")
    private String creatorName;

    @Field(name = "belong_user_id", type = FieldType.Keyword)
    private List<String> belongUserId;

    @Field(name = "status", type = FieldType.Integer)
    private Integer status;

    @Field(name = "created_at", type = FieldType.Date, format = DateFormat.date_time_no_millis)
    private String createdAt;

    @Field(name = "updated_at", type = FieldType.Date, format = DateFormat.date_time_no_millis)
    private String updatedAt;

} 