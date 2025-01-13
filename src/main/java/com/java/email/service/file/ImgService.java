package com.java.email.service.file;

import com.java.email.common.Response.Result;
import java.util.Map;
import java.util.List;

public interface ImgService {
    // 上传图片
    Result uploadImg(Map<String, List<Map<String, String>>> request);

    // 分配图片
    Result assignImg(Map<String, Object> request);

    // 获取图片分配详情
    Result assignImgDetails(Map<String, Object> request);

    // 删除图片
    Result deleteImg(Map<String, Object> request);

    // 筛选图片
    Result filterImg(Map<String, Object> request);
} 