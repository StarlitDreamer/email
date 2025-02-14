package com.java.email.pojo;

import co.elastic.clients.elasticsearch._types.mapping.Property;
import co.elastic.clients.elasticsearch._types.mapping.TypeMapping;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResendStrategy {
    private long resendGap;      // 重发间隔，以分钟为单位
    private long resendTimes;    // 重发次数

    // 定义索引映射
    public static TypeMapping createMapping() {
        return new TypeMapping.Builder()
            .properties("resendGap", Property.of(p -> p.long_(l -> l)))
            .properties("resendTimes", Property.of(p -> p.long_(l -> l)))
            .build();
    }
} 