package com.java.email.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

/**
 * 国家管理实体类。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "countries")
public class Country {
    @Id
    private String countryId;           // 国家ID，使用UUID
    private String countryCode;         // 国家代码，例如中国是zh、美国是us
    private String countryName;         // 国家名称
    private long createdAt;             // 创建时间，秒级时间戳
    private long updatedAt;             // 更新时间，秒级时间戳

    /**
     * 设置国家ID。
     *
     * @param value 国家ID，不能为 null 或空。
     * @throws IllegalArgumentException 如果国家ID为 null 或空。
     */
    public void setCountryId(String value) {
        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException("Country ID cannot be null or empty");
        }
        this.countryId = value;
    }

    /**
     * 设置国家代码。
     *
     * @param value 国家代码，不能为 null 或空。
     * @throws IllegalArgumentException 如果国家代码为 null 或空。
     */
    public void setCountryCode(String value) {
        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException("Country code cannot be null or empty");
        }
        this.countryCode = value;
    }

    /**
     * 设置国家名称。
     *
     * @param value 国家名称，不能为 null 或空。
     * @throws IllegalArgumentException 如果国家名称为 null 或空。
     */
    public void setCountryName(String value) {
        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException("Country name cannot be null or empty");
        }
        this.countryName = value;
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
