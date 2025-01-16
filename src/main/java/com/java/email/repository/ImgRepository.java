package com.java.email.repository;

import com.java.email.entity.Img;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

public interface ImgRepository extends ElasticsearchRepository<Img, String> {

    // 根据状态筛选图片
    List<Img> findByStatus(int status);

    // 根据创建人 ID 筛选图片
    List<Img> findByCreatorId(String creatorId);

    // 根据图片名称模糊查询
    List<Img> findByImgNameContaining(String imgName);

    // 根据图片大小范围筛选图片
    List<Img> findByImgSizeBetween(long minSize, long maxSize);

    // 根据创建时间范围筛选图片
    List<Img> findByCreatedAtBetween(long startTime, long endTime);

    // 根据状态和创建人 ID 组合筛选
    List<Img> findByStatusAndCreatorId(int status, String creatorId);
}