package com.java.email.result;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum ResultCodeEnum {

    SUCCESS(200, "成功"),
    FAIL(400, "网络波动");

    private final Integer code;
    private final String message;

    ResultCodeEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    // 用于序列化：将枚举转为字符串
    @JsonValue
    public String toJson() {
        return this.name();
    }

    // 用于反序列化：从字符串值解析枚举
    @JsonCreator
    public static ResultCodeEnum fromString(String value) {
        for (ResultCodeEnum enumValue : ResultCodeEnum.values()) {
            if (enumValue.name().equalsIgnoreCase(value)) {
                return enumValue;
            }
        }
        throw new IllegalArgumentException("Unknown value: " + value);
    }
}
