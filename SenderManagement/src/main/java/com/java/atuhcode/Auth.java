package com.java.atuhcode;

import com.java.model.vo.AuthVo;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public enum Auth implements IAuth{
    MANUAL_SEND("1", "手动发送"),
    CIRCLE_SEND("2", "循环发送"),
    FILE_MANAGE("3", "文件管理"),
    EMAIL_TASK_MANAGE("4", "邮件任务管理"),
    EMAIL_HISTORY_MANAGE("5", "邮件历史管理"),
    EMAIL_TEMPLATE_MANAGE("6", "邮件模板管理"),
    TOTAL_REPORT("7", "综合报表"),
    TASK_REPORT("8", "任务报表"),
    USER_MANAGE("9", "用户管理"),
    SUPPLIER_MANAGE("10", "供应商管理"),
    CUSTOMER_MANAGE("11", "客户管理"),
    EMAIL_TYPE_MANAGE("12", "邮件类型管理"),
    COMMODITY_MANAGE("13", "商品管理"),
    COUNTRY_MANAGE("14", "国家管理"),
    AREA_MANAGE("15", "区域管理"),
    EMAIL_SERVER_MANAGE("16", "邮件服务器管理");

    private static final Map<Integer, List<String>> roleAuthMap=new HashMap<>();

    private final String auth_id;//权限id
    private final String auth_name;//权限名称

    Auth(String authId, String authName) {
        this.auth_id = authId;
        this.auth_name = authName;
    }

    static {
        initRoleAuthMap();
    }

    private static void initRoleAuthMap() {
        roleAuthMap.put(2, Arrays.asList("1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16"));
        roleAuthMap.put(3, Arrays.asList("1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11"));
        roleAuthMap.put(4, Arrays.asList("1", "2", "3", "4", "5", "7", "8", "10", "11"));
    }


    public static List<String> getAuthsByRole(int role) {
        return roleAuthMap.getOrDefault(role, List.of());
    }
    public static List<AuthVo> getAllAuths() {
        return Arrays.stream(Auth.values())
                .map(auth -> new AuthVo(auth.auth_id, auth.auth_name))
                .collect(Collectors.toList());
    }

    @Override
    public String auth_id(){return auth_id; }
    @Override
    public String auth_name(){return auth_name; }
}
