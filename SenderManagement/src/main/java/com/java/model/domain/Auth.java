package com.java.model.domain;

import lombok.Data;

@Data
public class Auth {
    private String auth_id;
    private String auth_name;//权限名称    下面是目前所有的权限，每一个id对应一个name
    private String manual_send;//手动发送   4
    private String circle_send;//循环发送   4
    private String file_manage;//文件管理   4
    private String email_task_manage;//邮件任务管理  4
    private String email_history_manage;//邮件历史管理  4
    private String email_template_manage;//邮件模板管理
    private String total_report;//综合报表  4
    private String task_report;//任务报表   4
    private String user_manage;//用户管理
    private String supplier_manage;//供应商管理  4
    private String customer_manage;//客户管理    4
    private String email_type_manage;//邮件类型管理
    private String commodity_manage;//商品管理
    private String country_manage;//国家管理
    private String area_manage;//区域管理
    private String email_server_manage;//邮件服务器管理
}
