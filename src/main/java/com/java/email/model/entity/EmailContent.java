package com.java.email.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
@JsonIgnoreProperties(ignoreUnknown = true)
@Document(indexName = "email_content")
public class EmailContent {
    @Id
    @JsonProperty("email_task_id")
    @Field(name = "email_task_id", type = FieldType.Keyword)
    private String emailTaskId;

    @JsonProperty("email_content")
    @Field(name = "email_content", type = FieldType.Text)
    private String emailContent;
}
