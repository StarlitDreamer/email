package com.java.email.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

/**
 * 商品管理实体类。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "commodities")
public class Commodity {
    @Id
    private String commodityId;         // 商品ID，使用UUID
    private String categoryId;          // 品类ID，商品所属品类
    private String commodityName;       // 商品名称
    private long createdAt;             // 创建时间，秒级时间戳
    private long updatedAt;             // 更新时间，秒级时间戳

    /**
     * 设置商品ID。
     *
     * @param value 商品ID，不能为 null 或空。
     * @throws IllegalArgumentException 如果商品ID为 null 或空。
     */
    public void setCommodityId(String value) {
        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException("Commodity ID cannot be null or empty");
        }
        this.commodityId = value;
    }

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
     * 设置商品名称。
     *
     * @param value 商品名称，不能为 null 或空。
     * @throws IllegalArgumentException 如果商品名称为 null 或空。
     */
    public void setCommodityName(String value) {
        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException("Commodity name cannot be null or empty");
        }
        this.commodityName = value;
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
