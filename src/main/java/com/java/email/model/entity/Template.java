package com.java.email.model.entity;

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
 * Template management class for email templates.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "template")
public class Template {

    @JsonProperty("belong_user_id")
    @Field(name = "belong_user_id", type = FieldType.Keyword)
    private List<String> belongUserId;  // 所属用户ID列表 ownerUserIds

    @JsonProperty("creator")
    @Field(name = "creator", type = FieldType.Keyword)
    private String creator;             // 创建人

    @JsonProperty("creator_id")
    @Field(name = "creator_id", type = FieldType.Keyword)
    private String creatorId;           // 创建人ID

    @JsonProperty("status")
    @Field(name = "status", type = FieldType.Integer)
    private int status;                 // 模板状态 1:未分配 2:已分配

    @JsonProperty("template_content")
    @Field(name = "template_content", type = FieldType.Text)
    private String templateContent;     // 模板内容

    @Id
    @JsonProperty("template_id")
    @Field(name = "template_id", type = FieldType.Keyword)
    private String templateId;          // 模板ID

    @JsonProperty("template_name")
    @Field(name = "template_name", type = FieldType.Text)
    private String templateName;        // 模板名称

    @JsonProperty("template_type_id")
    @Field(name = "template_type_id", type = FieldType.Keyword)
    private String templateTypeId;         // 模板类型 templateType

    @JsonProperty("created_at")
    @Field(name = "created_at", type = FieldType.Date)
    private String createdAt;

    @Field(name = "updated_at", type = FieldType.Date)
    @JsonProperty("updated_at")
    private String updatedAt;
}