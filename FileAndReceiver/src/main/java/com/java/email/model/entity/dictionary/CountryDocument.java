package com.java.email.model.entity.dictionary;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.DateFormat;

import java.util.Date;

@Data
@Document(indexName = "country")
public class CountryDocument {
    @Id
    @Field(type = FieldType.Keyword)
    private String countryId;

    @Field(type = FieldType.Text)
    private String countryCode;

    @Field(type = FieldType.Text, analyzer = "ik_max_word")
    private String countryName;

    @Field(type = FieldType.Date, format = DateFormat.date_time_no_millis)
    private String createdAt;

    @Field(type = FieldType.Date, format = DateFormat.date_time_no_millis)
    private String updatedAt;
} 