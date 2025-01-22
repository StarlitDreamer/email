package com.java.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.elasticsearch.annotations.Document;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthVo {
    private String authId;    // 权限ID
    private String authName;  // 权限名称
}
