package com.java.email.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

/**
 * 品类管理实体类。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "categories")
public class Category {
    @Id
    private String categoryId;          // 品类ID，使用UUID
    private String categoryName;        // 品类名称
    private long createdAt;             // 创建时间，秒级时间戳
    private long updatedAt;             // 更新时间，秒级时间戳

    /**
     * 设置品类ID。
     *
     * @param value 品类ID，不能为 null 或空。
     * @throws IllegalArgumentException 如果品类ID为 null 或空。
     */
    public void setCategoryId(String value) {
        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException("Category ID cannot be null or empty");
        }
        this.categoryId = value;
    }

    /**
     * 设置品类名称。
     *
     * @param value 品类名称，不能为 null 或空。
     * @throws IllegalArgumentException 如果品类名称为 null 或空。
     */
    public void setCategoryName(String value) {
        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException("Category name cannot be null or empty");
        }
        this.categoryName = value;
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
