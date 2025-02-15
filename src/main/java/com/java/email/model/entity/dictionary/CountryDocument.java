package com.java.email.model.entity.dictionary;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.InnerField;
import org.springframework.data.elasticsearch.annotations.MultiField;
import org.springframework.data.elasticsearch.annotations.DateFormat;

import java.util.Date;

@Data
@Document(indexName = "country")
public class CountryDocument {
    @Id
    @Field(name = "country_id", type = FieldType.Keyword)
    private String countryId;

    @Field(name = "country_code", type = FieldType.Text)
    private String countryCode;

    @MultiField(
        mainField = @Field(name = "country_name", type = FieldType.Text, analyzer = "ik_max_word"),
        otherFields = {
            @InnerField(suffix = "keyword", type = FieldType.Keyword)
        }
    )
    private String countryName;

    @Field(name = "created_at", type = FieldType.Date)
    private String createdAt;

    @Field(name = "updated_at", type = FieldType.Date)
    private String updatedAt;
} 