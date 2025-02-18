package com.java.email.model.entity.file;

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
    @Field(name = "id", type = FieldType.Keyword)
    private String id;

    @Field(name = "img_id", type = FieldType.Keyword)
    private String imgId;

    @Field(name = "img_url", type = FieldType.Keyword)
    private String imgUrl;

    @Field(name = "img_size", type = FieldType.Long)
    private Long imgSize;

    @Field(name = "img_name", type = FieldType.Text, analyzer = "ik_max_word")
    private String imgName;

    @Field(name = "creator_id", type = FieldType.Keyword)
    private String creatorId;

    @Field(name = "belong_user_id", type = FieldType.Keyword)
    private List<String> belongUserId;

    @Field(name = "status", type = FieldType.Integer)
    private Integer status;

    @Field(name = "created_at", type = FieldType.Long)
    private Long createdAt;

    @Field(name = "updated_at", type = FieldType.Long)
    private Long updatedAt;
} 