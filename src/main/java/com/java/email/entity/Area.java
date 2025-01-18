package com.java.email.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

import java.util.List;

/**
 * 区域管理实体类。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "areas")
public class Area {
    @Id
    private String areaId;              // 区域ID
    private List<String> areaCountry;   // 区域内国家ID列表
    private String areaName;            // 区域名称
    private long createdAt;             // 创建时间，秒级时间戳
    private long updatedAt;             // 更新时间，秒级时间戳

    /**
     * 设置区域ID。
     *
     * @param value 区域ID，不能为 null 或空。
     * @throws IllegalArgumentException 如果区域ID为 null 或空。
     */
    public void setAreaId(String value) {
        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException("Area ID cannot be null or empty");
        }
        this.areaId = value;
    }

    /**
     * 设置区域内国家ID列表。
     *
     * @param value 区域内国家ID列表，不能为 null 或空。
     * @throws IllegalArgumentException 如果区域内国家ID列表为 null 或空。
     */
    public void setAreaCountry(List<String> value) {
        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException("Area country list cannot be null or empty");
        }
        this.areaCountry = value;
    }

    /**
     * 设置区域名称。
     *
     * @param value 区域名称，不能为 null 或空。
     * @throws IllegalArgumentException 如果区域名称为 null 或空。
     */
    public void setAreaName(String value) {
        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException("Area name cannot be null or empty");
        }
        this.areaName = value;
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
