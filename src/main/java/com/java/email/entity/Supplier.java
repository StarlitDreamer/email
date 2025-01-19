package com.java.email.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

import java.util.List;

/**
 * Supplier entity representing a supplier.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "suppliers")
public class Supplier {
    private List<String> acceptEmailTypeId;  // 接受的邮件类型ID列表，默认接受所有类型acceptedEmailTypeIds
    private String belongUserid;                // 所属用户ID ownerUserId
    private String birth;                      // 出生日期
    private List<String> commodityId;         // 商品ID列表  commodityIds
    private String contactPerson;              // 联系人
    private String contactWay;                 // 联系方式
    private long createdAt;                    // 创建日期
    private String creatorId;                  // 创建人ID
    private List<String> emails;               // 邮箱列表
    private String sex;                        // 性别
    private int status;             // 分配状态 1:未分配 2:已分配
    private String supplierCountryId;          // 供应商国家ID
    @Id
    private String supplierId;                 // 供应商ID
    private int supplierLevel;       // 供应商等级 1:初级 2:中级 3:高级
    private String supplierName;               // 供应商名称
    private int tradeType;               // 贸易类型 1:工厂 2:贸易商
    private long updatedAt;                    // 更新日期
}