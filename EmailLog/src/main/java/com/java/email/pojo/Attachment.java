package com.java.email.pojo;

import co.elastic.clients.elasticsearch._types.mapping.Property;
import co.elastic.clients.elasticsearch._types.mapping.TypeMapping;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Attachment {
    private String attachmentid;     // 附件id，上传到cdn返回来的id
    private String attachmentName;   // 附件名称
    private String attachmentSize;   // 附件大小
    private String attachmenturl;    // 附件url，cdn访问该文件的url
    private String[] belongUserid;   // 所属用户id
    private String createdAt;        // 创建日期
    private String creatorid;        // 创建人
    private int status;             // 分配状态 1:未分配 2:已分配
    private String updatedAt;        // 更新日期

    // 定义索引映射
    public static TypeMapping createMapping() {
        return new TypeMapping.Builder()
            .properties("attachmentid", Property.of(p -> p.keyword(k -> k)))
            .properties("attachmentName", Property.of(p -> p.text(t -> t)))
            .properties("attachmentSize", Property.of(p -> p.keyword(k -> k)))
            .properties("attachmenturl", Property.of(p -> p.keyword(k -> k)))
            .properties("belongUserid", Property.of(p -> p.keyword(k -> k)))
            .properties("createdAt", Property.of(p -> p.date(d -> d)))
            .properties("creatorid", Property.of(p -> p.keyword(k -> k)))
            .properties("status", Property.of(p -> p.long_(l -> l)))
            .properties("updatedAt", Property.of(p -> p.date(d -> d)))
            .build();
    }
} 