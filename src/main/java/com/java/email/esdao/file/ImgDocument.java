package com.java.email.esdao;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.List;

@Data
@Document(indexName = "img")
public class ImgDocument {
    
    @Id
    private String imgId;

    @Field(type = FieldType.Keyword, index = false)
    private String imgUrl;

    @Field(type = FieldType.Keyword, index = false)
    private String imgSize;

    @Field(type = FieldType.Text, analyzer = "ik_max_word")
    private String imgName;

    @Field(type = FieldType.Keyword)
    private String creatorId;

    @Field(type = FieldType.Keyword)
    private List<String> belongUserId;

    @Field(type = FieldType.Integer)
    private Integer status;

    @Field(type = FieldType.Date, index = false)
    private String createdAt;

    @Field(type = FieldType.Date, index = false)
    private String updatedAt;
} 