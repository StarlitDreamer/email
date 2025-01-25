package com.java.email.model.entity.template;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import lombok.Data;

import java.util.List;

@Data
@Document(indexName = "template")
public class TemplateDocument {

    @Id
    @Field(type = FieldType.Keyword)
    private String templateId;

    @Field(type = FieldType.Text, analyzer = "ik_max_word")
    private String templateName;

    @Field(type = FieldType.Keyword)
    private String templateTypeId;

    @Field(type = FieldType.Text)
    private String templateContent;

    @Field(type = FieldType.Keyword)
    private String creatorId;

    @Field(type = FieldType.Text, analyzer = "ik_max_word")
    private String creatorName;

    @Field(type = FieldType.Keyword)
    private List<String> belongUserId;

    @Field(type = FieldType.Integer)
    private Integer status;

    @Field(type = FieldType.Date, format = DateFormat.date_time_no_millis)
    private String createdAt;

    @Field(type = FieldType.Date, format = DateFormat.date_time_no_millis)
    private String updatedAt;

} 