package com.java.email.model.entity.receiver;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.List;
import java.util.Map;

@Data
@Document(indexName = "supplier_assign")
public class SupplierAssignDocument {
    
    @Id
    private String supplierId;

    @Field(type = FieldType.Object)
    private List<Map<String, Object>> assignProcess;
}