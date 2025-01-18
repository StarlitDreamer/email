package com.java.email.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 重发策略实体类。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResendStrategy {
    private long resendGap;             // 重发间隔，以分钟为单位
    private long resendTimes;           // 重发次数

    /**
     * 设置重发间隔。
     *
     * @param value 重发间隔，必须为正数。
     * @throws IllegalArgumentException 如果重发间隔为负数。
     */
    public void setResendGap(long value) {
        if (value < 0) {
            throw new IllegalArgumentException("Resend gap cannot be negative");
        }
        this.resendGap = value;
    }

    /**
     * 设置重发次数。
     *
     * @param value 重发次数，必须为正数。
     * @throws IllegalArgumentException 如果重发次数为负数。
     */
    public void setResendTimes(long value) {
        if (value < 0) {
            throw new IllegalArgumentException("Resend times cannot be negative");
        }
        this.resendTimes = value;
    }
}
