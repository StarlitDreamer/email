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
 * 商品管理实体类。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "commodity")
public class Commodity {
    @Id
    @JsonProperty("commodity_id")
    @Field(name = "commodity_id", type = FieldType.Keyword)
    private String commodityId;           // 商品ID，使用UUID，不分词

    @JsonProperty("category_id")
    @Field(name = "category_id", type = FieldType.Keyword)
    private String categoryId;            // 品类ID，商品所属品类

    @JsonProperty("commodity_name")
    @Field(name = "commodity_name", type = FieldType.Text)
    private String commodityName;         // 商品名称，不分词

    @JsonProperty("created_at")
    @Field(name = "created_at", type = FieldType.Date)
    private String createdAt;               // 创建时间，秒级时间戳

    @JsonProperty("updated_at")
    @Field(name = "updated_at", type = FieldType.Date)
    private String updatedAt;               // 更新时间，秒级时间戳
}
