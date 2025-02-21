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
    @Field(name = "supplier_id", type = FieldType.Keyword)
    private String supplierId;

    @Field(name = "supplier_name", type = FieldType.Text, analyzer = "ik_max_word")
    private String supplierName;

    @Field(name = "creator_id", type = FieldType.Keyword)
    private String creatorId;

    @Field(name = "contact_person", type = FieldType.Text, analyzer = "ik_max_word")
    private String contactPerson;

    @Field(name = "contact_way", type = FieldType.Text, analyzer = "ik_max_word")
    private String contactWay;

    @Field(name = "supplier_level", type = FieldType.Integer)
    private Integer supplierLevel;

    @Field(name = "supplier_country_id", type = FieldType.Keyword)
    private String supplierCountryId;

    @Field(name = "trade_type", type = FieldType.Integer)
    private Integer tradeType;

    @Field(name = "commodity_id", type = FieldType.Keyword)
    private List<String> commodityId;

    @Field(name = "sex", type = FieldType.Keyword)
    private String sex;

    @Field(name = "birth", type = FieldType.Date)
    private String birth;

    @Field(name = "emails", type = FieldType.Text, analyzer = "ik_max_word")
    private List<String> emails;

    @Field(name = "status", type = FieldType.Integer)
    private Integer status;

    @Field(name = "belong_user_id", type = FieldType.Keyword)
    private String belongUserId;

    @Field(name = "no_accept_email_type_id", type = FieldType.Keyword)
    private List<String> noAcceptEmailTypeId;

    @Field(name = "created_at", type = FieldType.Date)
    private String createdAt;

    @Field(name = "updated_at", type = FieldType.Date)
    private String updatedAt;
}
