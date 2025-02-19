package com.java.email.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.io.Serializable;

/**
 * supplier 供应商表
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Supplier implements Serializable {
    private static final long serialVersionUID = 100212415L;
    /**
     * 接受的邮件类型id，默认接受所有类型，当退订某种类型时，就从这里删除对应的类型id
     */
    @JsonProperty("accept_email_type_id")
    private String[] acceptEmailTypeid;
    /**
     * 所属用户，该供应商所属于哪个用户，插入用户的uuid
     */
    @JsonProperty("belong_user_id")
    private String belongUserid;
    /**
     * 出生日期
     */
    @JsonProperty("birth")
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
     * 创建人
     */
    @JsonProperty("creator_id")
    private String creatorid;
    /**
     * 邮箱
     */
    @JsonProperty("emails")
    private String[] emails;
    /**
     * 性别
     */
    @JsonProperty("sex")
    private String sex;
    /**
     * 分配状态，使用数字1、2代表分配状态。1是未分配、2是已分配
     */
    @JsonProperty("status")
    private long status;
    /**
     * 供应商国家
     */
    @JsonProperty("supplier_country_id")
    private String supplierCountryid;
    /**
     * 供应商id，使用uuid
     */
    @JsonProperty("supplier_id")
    private String supplierid;
    /**
     * 供应商等级，使用数字1、2、3代表等级。1是初级、2是中级、3是高级
     */
    @JsonProperty("supplier_level")
    private long supplierLevel;
    /**
     * 供应商名称
     */
    @JsonProperty("supplier_name")
    private String supplierName;
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
}
