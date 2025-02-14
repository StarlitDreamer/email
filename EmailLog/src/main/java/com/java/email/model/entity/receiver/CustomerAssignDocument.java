package com.java.email.model.entity.receiver;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.List;
import java.util.Map;

@Data
@Document(indexName = "customer_assign")
public class CustomerAssignDocument {
    @Id
    @Field(name = "id", type = FieldType.Keyword)
    private String id;

    @Field(name = "customer_id", type = FieldType.Keyword)
    private String customerId;

    @Field(name = "assign_process", type = FieldType.Object)
    private List<Map<String, Object>> assignProcess;
}