package com.java.email.model.entity.dictionary;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;


@Data
@Document(indexName = "email_type")
public class EmailTypeDocument {

    @Id
    @Field(type = FieldType.Keyword)
    private String emailTypeId;

    @Field(type = FieldType.Text, analyzer = "ik_max_word")
    private String emailTypeName;

    @Field(type = FieldType.Date, format = DateFormat.date_time_no_millis)
    private String createdAt;

    @Field(type = FieldType.Date, format = DateFormat.date_time_no_millis)
    private String updatedAt;

} 