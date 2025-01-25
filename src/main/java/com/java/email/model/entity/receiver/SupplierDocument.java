package com.java.email.model.entity.receiver;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.List;

@Data
@Document(indexName = "supplier")
public class SupplierDocument {
    @Id
    @Field(type = FieldType.Keyword)
    private String supplierId;

    @Field(type = FieldType.Text, analyzer = "ik_max_word")
    private String supplierName;

    @Field(type = FieldType.Keyword)
    private String creatorId;

    @Field(type = FieldType.Text, analyzer = "ik_max_word")
    private String contactPerson;

    @Field(type = FieldType.Text, analyzer = "ik_max_word")
    private String contactWay;

    @Field(type = FieldType.Integer)
    private Integer supplierLevel;

    @Field(type = FieldType.Keyword)
    private String supplierCountryId;

    @Field(type = FieldType.Integer)
    private Integer tradeType;

    @Field(type = FieldType.Keyword)
    private List<String> commodityId;

    @Field(type = FieldType.Keyword)
    private String sex;

    @Field(type = FieldType.Date, format = DateFormat.date_time_no_millis)
    private String birth;

    @Field(type = FieldType.Text, analyzer = "ik_max_word")
    private List<String> emails;

    @Field(type = FieldType.Integer)
    private Integer status;

    @Field(type = FieldType.Keyword)
    private String belongUserId;

    @Field(type = FieldType.Keyword)
    private List<String> acceptEmailTypeId;

    @Field(type = FieldType.Date, format = DateFormat.date_time_no_millis)
    private String createdAt;

    @Field(type = FieldType.Date, format = DateFormat.date_time_no_millis)
    private String updatedAt;
}
