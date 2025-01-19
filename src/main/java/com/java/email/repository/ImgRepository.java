package com.java.email.repository;

import com.java.email.entity.Img;
import org.springframework.data.domain.Page;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.data.domain.Pageable;

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

    // 根据图片 ID 查找图片
    Img findByImgId(String imgId);

    // 根据所属用户 ID 列表查询图片（分页）
    Page<Img> findByBelongUserIdIn(List<String> belongUserId, Pageable pageable);

    // 根据创建人 ID 查询图片（分页）
    Page<Img> findByCreatorId(String creatorId, Pageable pageable);

    // 根据状态查询图片（分页）
    Page<Img> findByStatus(int status, Pageable pageable);

    // 根据图片名称查询图片（分页）
    Page<Img> findByImgName(String imgName, Pageable pageable);

    // 根据图片大小查询图片（分页）
    Page<Img> findByImgSize(long imgSize, Pageable pageable);

    // 分页查询所有图片
    Page<Img> findAll(Pageable pageable);
}