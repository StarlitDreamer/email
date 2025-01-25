package org.easyarch.email.pojo;

import co.elastic.clients.elasticsearch._types.mapping.Property;
import co.elastic.clients.elasticsearch._types.mapping.TypeMapping;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private String belongUserid;     // 所属用户，该用户所属于哪个管理员的uuid
    private String createdAt;        // 创建日期
    private String creatorid;        // 创建人ID
    private Integer status;             // 用户分配状态 1:未分配 2:已分配
    private String updatedAt;        // 更新日期
    private String userAccount;      // 用户登录账号
    private String[] userAuthid;     // 用户权限ID数组
    private String userEmail;        // 用户邮箱
    private String userEmailCode;    // 邮箱授权码
    private String userid;           // 用户ID，使用uuid
    private String userName;         // 用户名
    private String userPassword;     // 用户密码，使用md5加密
    private Integer userRole;           // 用户角色 1:公司 2:大管理 3:小管理 4:用户

    // 定义索引映射
    public static TypeMapping createMapping() {
        return new TypeMapping.Builder()
            .properties("belongUserid", Property.of(p -> p.keyword(k -> k)))
            .properties("createdAt", Property.of(p -> p.date(d -> d)))
            .properties("creatorid", Property.of(p -> p.keyword(k -> k)))
            .properties("status", Property.of(p -> p.long_(l -> l)))
            .properties("updatedAt", Property.of(p -> p.date(d -> d)))
            .properties("userAccount", Property.of(p -> p.keyword(k -> k)))
            .properties("userAuthid", Property.of(p -> p.keyword(k -> k)))
            .properties("userEmail", Property.of(p -> p.keyword(k -> k)))
            .properties("userEmailCode", Property.of(p -> p.keyword(k -> k)))
            .properties("userid", Property.of(p -> p.keyword(k -> k)))
            .properties("userName", Property.of(p -> p.text(t -> t)))
            .properties("userPassword", Property.of(p -> p.keyword(k -> k)))
            .properties("userRole", Property.of(p -> p.long_(l -> l)))
            .build();
    }
} 