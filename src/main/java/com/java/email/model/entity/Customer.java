package com.java.email.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.List;

/**
 * Customer entity representing a customer.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@Document(indexName = "customer")
public class Customer {
    @Id
    @JsonProperty("customer_id")
    @Field(name = "customer_id", type = FieldType.Keyword)
    private String customerId;                 // 客户ID

    @JsonProperty("customer_name")
    @Field(name = "customer_name", type = FieldType.Text)
    private String customerName;               // 客户名称

    @JsonProperty("creator_id")
    @Field(name = "creator_id", type = FieldType.Keyword)
    private String creatorId;                  // 创建人ID

    @JsonProperty("contact_person")
    @Field(name = "contact_person", type = FieldType.Text)
    private String contactPerson;              // 联系人

    @JsonProperty("contact_way")
    @Field(name = "contact_way", type = FieldType.Text)
    private String contactWay;                 // 联系方式

    @JsonProperty("customer_level")
    @Field(name = "customer_level", type = FieldType.Integer)
    private Integer customerLevel;                 // 客户等级 1:初级 2:中级 3:高级

    @JsonProperty("customer_country_id")
    @Field(name = "customer_country_id", type = FieldType.Keyword)
    private String customerCountryId;          // 客户国家ID

    @JsonProperty("trade_type")
    @Field(name = "trade_type", type = FieldType.Integer)
    private Integer tradeType;               // 贸易类型 1:工厂 2:贸易商

    @JsonProperty("commodity_id")
    @Field(name = "commodity_id", type = FieldType.Keyword)
    private List<String> commodityId;         // 商品ID列表

    @JsonProperty("sex")
    @Field(name = "sex", type = FieldType.Keyword)
    private String sex;                        // 性别

    @JsonProperty("birth")
    @Field(name = "birth", type = FieldType.Date)
    private String birth;                      // 出生日期

    @JsonProperty("emails")
    @Field(name = "emails", type = FieldType.Keyword)
    private List<String> emails;               // 邮箱列表


    @JsonProperty("status")
    @Field(name = "status", type = FieldType.Integer)
    private Integer status;             // 分配状态 1:未分配 2:已分配

    @JsonProperty("belong_user_id")
    @Field(name = "belong_user_id", type = FieldType.Keyword)
    private String belongUserId;               // 所属用户ID

    /**
     * 不接受的邮件类型id，当退订某种类型时，就从这里删除对应的类型id
     */
    @JsonProperty("no_accept_email_type_id")
    @Field(name = "no_accept_email_type_id", type = FieldType.Keyword)
    private List<String> noAcceptEmailTypeId;

    @JsonProperty("created_at")
    @Field(name = "created_at", type = FieldType.Date)
    private String createdAt;           // 创建日期

    @JsonProperty("updated_at")
    @Field(name = "updated_at", type = FieldType.Date)
    private String updatedAt;           // 更新日期

}