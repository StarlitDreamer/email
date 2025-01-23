package com.java.email.result;

import lombok.Getter;

@Getter
public enum EmailTaskTypeEnum {

    //使用数字1、2、3、4代表任务类型。1是手动发送、2是循环发送、3是节日发送、4是生日发送

    ManualSend(1,"手动发送"),
    LoopSend(2,"循环发送"),
    HolidaySend(3,"节日发送"),
    BirthdaySend(4,"生日发送");




    private final Integer code;
    private final String message;

    // 构造函数
    EmailTaskTypeEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    /**
     * 根据状态码获取对应的枚举常量
     *
     * @param code 状态码
     * @return 对应的 EmailStatus 枚举常量，如果没有匹配的状态码，则返回 null
     */
    public static EmailTaskTypeEnum getByCode(Integer code) {
        for (EmailTaskTypeEnum status : EmailTaskTypeEnum.values()) {
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
        EmailTaskTypeEnum status = getByCode(code);
        return status != null ? status.getMessage() : "未知状态";
    }
}
