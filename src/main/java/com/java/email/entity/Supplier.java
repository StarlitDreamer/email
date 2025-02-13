package com.java.email.entity;

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
 * Supplier entity representing a supplier.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "supplier")
public class Supplier {
    @Id
    @JsonProperty("supplier_id")
    @Field(name = "supplier_id", type = FieldType.Keyword)
    private String supplierId;             // 供应商ID

    @JsonProperty("supplier_name")
    @Field(name = "supplier_name", type = FieldType.Text)
    private String supplierName;           // 供应商名称

    @JsonProperty("supplier_level")
    @Field(name = "supplier_level", type = FieldType.Integer)
    private int supplierLevel;             // 供应商等级 1:初级 2:中级 3:高级

    @JsonProperty("supplier_country_id")
    @Field(name = "supplier_country_id", type = FieldType.Keyword)
    private String supplierCountryId;      // 供应商国家ID

    @JsonProperty("belong_user_id")
    @Field(name = "belong_user_id", type = FieldType.Keyword)
    private List<String> belongUserid;     // 所属用户ID列表 ownerUserIds

    @JsonProperty("birth")
    @Field(name = "birth", type = FieldType.Keyword)
    private String birth;                  // 出生日期

    @JsonProperty("commodity_id")
    @Field(name = "commodity_id", type = FieldType.Keyword)
    private List<String> commodityId;      // 商品ID列表 commodityIds

    @JsonProperty("contact_person")
    @Field(name = "contact_person", type = FieldType.Text)
    private String contactPerson;          // 联系人

    @JsonProperty("contact_way")
    @Field(name = "contact_way", type = FieldType.Text)
    private String contactWay;             // 联系方式

    @JsonProperty("email")
    @Field(name = "email", type = FieldType.Keyword)
    private List<String> emails;           // 邮箱列表

    @JsonProperty("sex")
    @Field(name = "sex", type = FieldType.Keyword)
    private String sex;                    // 性别

    @JsonProperty("status")
    @Field(name = "status", type = FieldType.Integer)
    private int status;                    // 分配状态 1:未分配 2:已分配

    @JsonProperty("created_at")
    @Field(name = "created_at", type = FieldType.Date)
    private String createdAt;                // 创建日期

    @JsonProperty("creator_id")
    @Field(name = "creator_id", type = FieldType.Keyword)
    private String creatorId;              // 创建人ID

    @JsonProperty("updated_at")
    @Field(name = "updated_at", type = FieldType.Date)
    private String updatedAt;                // 更新日期


    @JsonProperty("trade_type")
    @Field(name = "trade_type", type = FieldType.Integer)
    private String tradeType;                // 更新日期

//    private List<String> acceptEmailTypeId;  // 接受的邮件类型ID列表，默认接受所有类型acceptedEmailTypeIds
//    private String belongUserid;                // 所属用户ID ownerUserId
//    private String birth;                      // 出生日期
//    private List<String> commodityId;         // 商品ID列表  commodityIds
//    private String contactPerson;              // 联系人
//    private String contactWay;                 // 联系方式
//    private long createdAt;                    // 创建日期
//    private String creatorId;                  // 创建人ID
//    private List<String> emails;               // 邮箱列表
//    private String sex;                        // 性别
//    private int status;             // 分配状态 1:未分配 2:已分配
//    private String supplierCountryId;          // 供应商国家ID
//    @Id
//    private String supplierId;                 // 供应商ID
//    private int supplierLevel;       // 供应商等级 1:初级 2:中级 3:高级
//    private String supplierName;               // 供应商名称
//    private int tradeType;               // 贸易类型 1:工厂 2:贸易商
//    private long updatedAt;                    // 更新日期
}