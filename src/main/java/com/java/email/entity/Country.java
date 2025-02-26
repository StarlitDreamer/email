package com.java.email.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

/**
 * 国家管理实体类。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "country")
public class Country {
    @Id
    @JsonProperty("country_id")
    @Field(name = "country_id", type = FieldType.Keyword)
    private String countryId;           // 国家ID，使用UUID，不分词

    @JsonProperty("country_code")
    @Field(name = "country_code", type = FieldType.Text)
    private String countryCode;         // 国家代码，例如中国是zh、美国是us

    @JsonProperty("country_name")
    @Field(name = "country_name", type = FieldType.Text)
    private String countryName;         // 国家名称


    @JsonProperty("created_at")
    @Field(name = "created_at", type = FieldType.Date)
    private String createdAt;             // 创建时间，秒级时间戳

    @JsonProperty("updated_at")
    @Field(name = "updated_at", type = FieldType.Date)
    private String updatedAt;             // 更新时间，秒级时间戳
}