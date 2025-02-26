package com.java.email.model.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.List;

/**
 * Img entity representing an image.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "img")
public class Img {
    @JsonProperty("belong_user_id")
    @Field(name = "belong_user_id", type = FieldType.Keyword)
    private List<String> belongUserId;  // 所属用户ID列表

    @JsonProperty("created_at")
    @Field(name = "created_at", type = FieldType.Long)
    private Long createdAt;    // 创建日期

    @JsonProperty("creator_id")
    @Field(name = "creator_id", type = FieldType.Keyword)
    private String creatorId;           // 创建人ID

    @Id
    @Field(name = "img_id", type = FieldType.Keyword)
    private String imgId;               // 图片ID

    @JsonProperty("img_name")
    @Field(name = "img_name", type = FieldType.Text)
    private String imgName;             // 图片名称

    @JsonProperty("img_size")
    @Field(name = "img_size", type = FieldType.Long)
    private Long imgSize;               // 图片大小，单位为字节

    @JsonProperty("img_url")
    @Field(name = "img_url", type = FieldType.Text)
    private String imgUrl;              // 图片URL

    @JsonProperty("status")
    @Field(name = "status", type = FieldType.Integer)
    private Integer status;           // 分配状态 1:未分配 2:已分配

    @JsonProperty("updated_at")
    @Field(name = "updated_at", type = FieldType.Long)
    private Long updatedAt;    // 更新日期
}