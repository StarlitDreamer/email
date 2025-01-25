package com.java.email.model.domain;

import lombok.Data;
import java.util.List;

@Data
public class Img {
    private String imgId;
    private String imgUrl;
    private String imgSize;
    private String imgName;
    private String creatorId;
    private List<String> belongUserId;
    private Integer status;
    private String createdAt;
    private String updatedAt;
} 