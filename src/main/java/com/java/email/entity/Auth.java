package com.java.email.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

/**
 * 权限管理实体类。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "auth")
public class Auth {
    @Id
    private String authId;              // 权限ID
    private String authName;            // 权限名称
    private String areaManage;          // 区域管理权限
    private String circleSend;          // 循环发送权限
    private String commodityManage;     // 商品管理权限
    private String countryManage;       // 国家管理权限
    private String customerManage;      // 客户管理权限
    private String emailHistoryManage;  // 邮件历史管理权限
    private String emailServerManage;   // 邮件服务器管理权限
    private String emailTaskManage;     // 邮件任务管理权限
    private String emailTemplateManage; // 邮件模板管理权限
    private String emailTypeManage;     // 邮件类型管理权限
    private String fileManage;          // 文件管理权限
    private String manualSend;          // 手动发送权限
    private String supplierManage;      // 供应商管理权限
    private String taskReport;          // 任务报告权限
    private String totalReport;         // 总报告权限
    private String userManage;          // 用户管理权限

    /**
     * 设置权限ID。
     *
     * @param value 权限ID，不能为 null 或空。
     * @throws IllegalArgumentException 如果权限ID为 null 或空。
     */
    public void setAuthId(String value) {
        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException("Auth ID cannot be null or empty");
        }
        this.authId = value;
    }

    /**
     * 设置权限名称。
     *
     * @param value 权限名称，不能为 null 或空。
     * @throws IllegalArgumentException 如果权限名称为 null 或空。
     */
    public void setAuthName(String value) {
        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException("Auth name cannot be null or empty");
        }
        this.authName = value;
    }
}
