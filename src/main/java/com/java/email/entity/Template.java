package com.java.email.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
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
    private List<String> ownerUserIds;  // 所属用户ID列表
    private String creator;             // 创建人
    private String creatorId;           // 创建人ID
    private TemplateStatus status;      // 模板状态 1:未分配 2:已分配
    private String templateContent;     // 模板内容
    private String templateId;          // 模板ID
    private String templateName;        // 模板名称
    private TemplateType templateType;  // 模板类型
}

/**
 * 模板状态枚举
 */
enum TemplateStatus {
    UNASSIGNED(1),  // 未分配
    ASSIGNED(2);    // 已分配

    private final int code;

    TemplateStatus(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}

/**
 * 模板类型枚举
 */
enum TemplateType {
    EMAIL_TYPE_1("type1"),  // 邮件类型1
    EMAIL_TYPE_2("type2");  // 邮件类型2

    private final String typeId;

    TemplateType(String typeId) {
        this.typeId = typeId;
    }

    public String getTypeId() {
        return typeId;
    }
}
