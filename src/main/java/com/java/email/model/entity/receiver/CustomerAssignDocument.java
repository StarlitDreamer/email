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
    private String customerId;

    @Field(type = FieldType.Object)
    private List<Map<String, Object>> assignProcess;
}