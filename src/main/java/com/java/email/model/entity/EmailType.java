package com.java.email.model.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

/**
 * 邮件类型实体类。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "email_type")
public class EmailType {
    @Id
    private String emailTypeId;         // 邮件类型ID，使用UUID
    private String emailTypeName;       // 邮件类型名称
    private long createdAt;             // 创建时间，秒级时间戳
    private long updatedAt;             // 更新时间，秒级时间戳
}
