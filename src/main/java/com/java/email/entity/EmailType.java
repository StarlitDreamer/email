package com.java.email.entity;

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

    /**
     * 设置邮件类型ID。
     *
     * @param value 邮件类型ID，不能为 null 或空。
     * @throws IllegalArgumentException 如果邮件类型ID为 null 或空。
     */
    public void setEmailTypeId(String value) {
        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException("Email type ID cannot be null or empty");
        }
        this.emailTypeId = value;
    }

    /**
     * 设置邮件类型名称。
     *
     * @param value 邮件类型名称，不能为 null 或空。
     * @throws IllegalArgumentException 如果邮件类型名称为 null 或空。
     */
    public void setEmailTypeName(String value) {
        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException("Email type name cannot be null or empty");
        }
        this.emailTypeName = value;
    }

    /**
     * 设置创建时间。
     *
     * @param value 创建时间，秒级时间戳。
     * @throws IllegalArgumentException 如果创建时间为负数。
     */
    public void setCreatedAt(long value) {
        if (value < 0) {
            throw new IllegalArgumentException("Created at timestamp cannot be negative");
        }
        this.createdAt = value;
    }

    /**
     * 设置更新时间。
     *
     * @param value 更新时间，秒级时间戳。
     * @throws IllegalArgumentException 如果更新时间为负数。
     */
    public void setUpdatedAt(long value) {
        if (value < 0) {
            throw new IllegalArgumentException("Updated at timestamp cannot be negative");
        }
        this.updatedAt = value;
    }
}
