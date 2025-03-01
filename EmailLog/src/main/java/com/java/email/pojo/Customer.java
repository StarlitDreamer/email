package com.java.email.pojo;

import co.elastic.clients.elasticsearch._types.mapping.Property;
import co.elastic.clients.elasticsearch._types.mapping.TypeMapping;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.io.Serializable;

/**
 * customer 客户表
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Customer implements Serializable {
    private static final long serialVersionUID = 10045L;
    /**

    /**
     * 所属用户，该客户所属于哪个用户，插入用户的uuid
     */
    @JsonProperty("belong_user_id")
    private String belongUserid;
    /**
     * 出生日期
     */
    private String birth;
    /**
     * 商品id
     */
    @JsonProperty("commodity_id")
    private String[] commodityid;
    /**
     * 联系人
     */
    @JsonProperty("contact_person")
    private String contactPerson;
    /**
     * 联系方式
     */
    @JsonProperty("contact_way")
    private String contactWay;
    /**
     * 创建日期
     */
    @JsonProperty("created_at")
    private String createdAt;
    /**
     * 创建人id
     */
    @JsonProperty("creator_id")
    private String creatorid;
    /**
     * 客户国家
     */
    @JsonProperty("customer_country_id")
    private String customerCountryid;
    /**
     * 客户id，使用uuid
     */
    @JsonProperty("customer_id")
    private String customerid;
    /**
     * 客户等级，使用数字1、2、3代表等级。1是初级、2是中级、3是高级
     */
    @JsonProperty("customer_level")
    private long customerLevel;
    /**
     * 客户名称
     */
    @JsonProperty("customer_name")
    private String customerName;
    /**
     * 邮箱
     */
    private String[] emails;
    /**
     * 性别
     */
    private String sex;
    /**
     * 分配状态，使用数字1、2代表分配状态。1是未分配、2是已分配
     */
    private long status;
    /**
     * 贸易类型，使用数字1、2代表贸易类型。1是工厂、2是贸易商
     */
    @JsonProperty("trade_type")
    private long tradeType;
    /**
     * 更新日期
     */
    @JsonProperty("updated_at")
    private String updatedAt;

    @JsonProperty("no_accept_email_type_id")
    private String[] noAcceptEmailTypeId;

    // 定义索引映射
    public static TypeMapping createMapping() {
        return new TypeMapping.Builder()
            .properties("accept_email_typeid", Property.of(p -> p.keyword(k -> k)))
            .properties("belong_user_id", Property.of(p -> p.keyword(k -> k)))
            .properties("birth", Property.of(p -> p.keyword(k -> k)))
            .properties("commodity_id", Property.of(p -> p.keyword(k -> k)))
            .properties("contact_person", Property.of(p -> p.text(t -> t
                .fields("keyword", f -> f.keyword(k -> k)))))
            .properties("contact_way", Property.of(p -> p.keyword(k -> k)))
            .properties("created_at", Property.of(p -> p.keyword(k -> k)))
            .properties("creator_id", Property.of(p -> p.keyword(k -> k)))
            .properties("customer_countryid", Property.of(p -> p.keyword(k -> k)))
            .properties("customer_id", Property.of(p -> p.keyword(k -> k)))
            .properties("customer_level", Property.of(p -> p.long_(l -> l)))
            .properties("customer_name", Property.of(p -> p.text(t -> t
                .fields("keyword", f -> f.keyword(k -> k)))))
            .properties("emails", Property.of(p -> p.keyword(k -> k)))
            .properties("sex", Property.of(p -> p.keyword(k -> k)))
            .properties("status", Property.of(p -> p.long_(l -> l)))
            .properties("trade_type", Property.of(p -> p.long_(l -> l)))
            .properties("updated_at", Property.of(p -> p.keyword(k -> k)))
            .build();
    }
}
