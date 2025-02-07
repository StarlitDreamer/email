package com.java.email.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "test")
public class Test {
    @Id
    private String id;  // Elasticsearch的文档ID

    @JsonProperty("date")
    @Field(name = "date", type = FieldType.Keyword)
    private String date;        // 所属用户ID
}
