package com.java.email.esdao.file;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.List;
import java.util.Map;

@Data
@Document(indexName = "img_assign")
public class ImgAssignDocument {
    
    @Id
    private String imgId;

    @Field(type = FieldType.Object)
    private List<Map<String, Object>> assignProcess;
} 