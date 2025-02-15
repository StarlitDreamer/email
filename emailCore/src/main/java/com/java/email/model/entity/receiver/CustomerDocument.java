package com.java.email.model.entity.receiver;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.List;

@Data
@Document(indexName = "customer")
public class CustomerDocument {
    @Id
    @Field(name = "customer_id", type = FieldType.Keyword)
    private String customerId;

    @Field(name = "customer_name", type = FieldType.Text, analyzer = "ik_max_word")
    private String customerName;

    @Field(name = "creator_id", type = FieldType.Keyword)
    private String creatorId;

    @Field(name = "contact_person", type = FieldType.Text, analyzer = "ik_max_word")
    private String contactPerson;

    @Field(name = "contact_way", type = FieldType.Text, analyzer = "ik_max_word")
    private String contactWay;

    @Field(name = "customer_level", type = FieldType.Integer)
    private Integer customerLevel;

    @Field(name = "customer_country_id", type = FieldType.Keyword)
    private String customerCountryId;

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

    @Field(name = "accept_email_type_id", type = FieldType.Keyword)
    private List<String> acceptEmailTypeId;

    @Field(name = "created_at", type = FieldType.Date)
    private String createdAt;

    @Field(name = "updated_at", type = FieldType.Date)
    private String updatedAt;
} 