package com.java.email.result;


import lombok.Getter;

/**
 * @author EvoltoStar
 */
@Getter
public enum EmailStatusEnum {
    // 使用数字1、2、3、4、5代表。1是已送达、2是已打开、3是未送达、4是已退信、5是已退订。
    Delivered(1, "已送达"),
    Opened(2, "已打开"),
    Notdelivered(3, "未送达"),
    Bounced(4, "已退信"),
    Unsubscribed(5, "已退订");

    private final Integer code;
    private final String message;

    // 构造函数
    EmailStatusEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    /**
     * 根据状态码获取对应的枚举常量
     *
     * @param code 状态码
     * @return 对应的 EmailStatus 枚举常量，如果没有匹配的状态码，则返回 null
     */
    public static EmailStatusEnum getByCode(Integer code) {
        for (EmailStatusEnum status : EmailStatusEnum.values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        return null;
    }

    /**
     * 根据状态码获取对应的消息
     *
     * @param code 状态码
     * @return 状态消息，如果没有匹配的状态码，则返回 "未知状态"
     */
    public static String getMessageByCode(Integer code) {
        EmailStatusEnum status = getByCode(code);
        return status != null ? status.getMessage() : "未知状态";
    }
}
