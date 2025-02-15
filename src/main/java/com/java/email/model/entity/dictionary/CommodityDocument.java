package com.java.email.model.entity.dictionary;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.DateFormat;

import java.util.Date;

@Data
@Document(indexName = "commodity")
public class CommodityDocument {
    @Id
    @Field(name = "commodity_id", type = FieldType.Keyword)
    private String commodityId;

    @Field(name = "category_id", type = FieldType.Keyword) 
    private String categoryId;

    @Field(name = "commodity_name", type = FieldType.Text, analyzer = "ik_max_word")
    private String commodityName;

    @Field(name = "created_at", type = FieldType.Date)
    private String createdAt;

    @Field(name = "updated_at", type = FieldType.Date)
    private String updatedAt;
} 