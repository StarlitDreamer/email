package com.java.email.constant;

import java.util.ArrayList;
import java.util.Arrays;

public class UserConstData {
    public static final String COMPANY_USER_ID = "1";
    public static final String COMPANY_USER_NAME = "公司";
    public static final String COMPANY_USER_ACCOUNT = "gongsi";

    public static final String ADMIN_LARGE_USER_ID = "2";
    public static final String ADMIN_LARGE_USER_NAME = "大管理";
    public static final String ADMIN_LARGE_USER_ACCOUNT = "adminLarge";
    public static final ArrayList<String> ADMIN_LARGE_AUTH_ID = new ArrayList<>(Arrays.asList("3","4","5","6","7","8","9","10","11","12","13","14","15","16"));

    public static final Integer ROLE_COMPANY = 1;
    public static final Integer ROLE_ADMIN_LARGE = 2;
    public static final Integer ROLE_ADMIN_SMALL = 3;
    public static final Integer ROLE_USER = 4;

    // 邮件类型
    public static final String BIRTH_TYPE_ID = "birth";
    public static final String BIRTH_TYPE_NAME = "生日邮件";
}