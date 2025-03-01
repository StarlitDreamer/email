package com.java.email.pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class EmailType {
    /**
     * 创建时间
     */
    @JsonProperty("created_at")
    private String createdAt;
    /**
     * 邮件类型id，使用uuid
     */
    @JsonProperty("email_type_id")
    private String emailTypeId;
    /**
     * 邮件类型名称
     */
    @JsonProperty("email_type_name")
    private String emailTypeName;
    /**
     * 更新时间
     */
    @JsonProperty("updated_at")
    private String updatedAt;

}
