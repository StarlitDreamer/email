package com.java.email.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

/**
 * Supplier entity representing a supplier.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Supplier {
    private List<String> acceptedEmailTypeIds;  // 接受的邮件类型ID列表，默认接受所有类型
    private String ownerUserId;                // 所属用户ID
    private String birth;                      // 出生日期
    private List<String> commodityIds;         // 商品ID列表
    private String contactPerson;              // 联系人
    private String contactWay;                 // 联系方式
    private long createdAt;                    // 创建日期
    private String creatorId;                  // 创建人ID
    private List<String> emails;               // 邮箱列表
    private String sex;                        // 性别
    private SupplierStatus status;             // 分配状态 1:未分配 2:已分配
    private String supplierCountryId;          // 供应商国家ID
    private String supplierId;                 // 供应商ID
    private SupplierLevel supplierLevel;       // 供应商等级 1:初级 2:中级 3:高级
    private String supplierName;               // 供应商名称
    private TradeType tradeType;               // 贸易类型 1:工厂 2:贸易商
    private long updatedAt;                    // 更新日期
}

/**
 * 供应商状态枚举
 */
enum SupplierStatus {
    UNASSIGNED(1),  // 未分配
    ASSIGNED(2);    // 已分配

    private final int code;

    SupplierStatus(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}

/**
 * 供应商等级枚举
 */
enum SupplierLevel {
    PRIMARY(1),      // 初级
    INTERMEDIATE(2), // 中级
    ADVANCED(3);     // 高级

    private final int code;

    SupplierLevel(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}

/**
 * 贸易类型枚举
 */
enum TradeType {
    FACTORY(1),  // 工厂
    TRADER(2);   // 贸易商

    private final int code;

    TradeType(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}