package com.java.email.model.entity.dictionary;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Data
@Document(indexName = "category")
public class CategoryDocument {

    @Id
    @Field(name = "id", type = FieldType.Keyword)
    private String id;

    @Field(name = "category_id", type = FieldType.Keyword)
    private String categoryId;

    @Field(name = "category_name", type = FieldType.Text, analyzer = "ik_max_word")
    private String categoryName;

    @Field(name = "created_at", type = FieldType.Date)
    private String createdAt;

    @Field(name = "updated_at", type = FieldType.Date)
    private String updatedAt;
}
