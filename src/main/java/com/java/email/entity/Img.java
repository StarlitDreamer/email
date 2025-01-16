package com.java.email.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.elasticsearch.annotations.Document;

import java.util.List;

/**
 * Img entity representing an image.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "imgs")
public class Img {
    private List<String> ownerUserIds;  // 所属用户ID列表
    private long createdAt;    // 创建日期
    private String creatorId;           // 创建人ID
    private String imgId;               // 图片ID
    private String imgName;             // 图片名称
    private long imgSize;               // 图片大小，单位为字节
    private String imgUrl;              // 图片URL
    private ImgStatus status;           // 分配状态 1:未分配 2:已分配
    private long updatedAt;    // 更新日期
}

/**
 * 图片状态枚举
 */
enum ImgStatus {
    UNASSIGNED(1),  // 未分配
    ASSIGNED(2);    // 已分配

    private final int code;

    ImgStatus(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
