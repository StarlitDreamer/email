package com.java.email.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "area")
public class Area {
    @Id
    @JsonProperty("area_id")
    @Field(name = "area_id", type = FieldType.Keyword)
    private String areaId;               // 区域id

    @JsonProperty("area_name")
    @Field(name = "area_name", type = FieldType.Text)
    private String areaName;             // 区域名称

    @JsonProperty("area_country")
    @Field(name = "area_country", type = FieldType.Keyword)
    private List<String> areaCountry;    // 区域内国家id

    @JsonProperty("created_at")
    @Field(name = "created_at", type = FieldType.Date)
    private String createdAt;            // 创建时间

    @JsonProperty("updated_at")
    @Field(name = "updated_at", type = FieldType.Date)
    private String updatedAt;            // 更新时间
}