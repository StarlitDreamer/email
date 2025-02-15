package com.java.email.model.entity.dictionary;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;


@Data
@Document(indexName = "email_type")
public class EmailTypeDocument {

    @Id
    @Field(name = "email_type_id", type = FieldType.Keyword)
    private String emailTypeId;

    @Field(name = "email_type_name", type = FieldType.Text, analyzer = "ik_max_word")
    private String emailTypeName;

    @Field(name = "created_at", type = FieldType.Date)
    private String createdAt;

    @Field(name = "updated_at", type = FieldType.Date)
    private String updatedAt;

} 