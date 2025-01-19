package com.java.email.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

import java.util.List;

/**
 * Template management class for email templates.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "templates")
public class Template {
    private List<String> belongUserId;  // 所属用户ID列表ownerUserIds
    private String creator;             // 创建人
    private String creatorId;           // 创建人ID
    private int status;      // 模板状态 1:未分配 2:已分配
    private String templateContent;     // 模板内容
    @Id
    private String templateId;          // 模板ID
    private String templateName;        // 模板名称
    private int templateTypeId;    // 模板类型templateType
}