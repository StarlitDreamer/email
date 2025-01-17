package com.java.email.service;

import com.java.email.common.Result;
import com.java.email.entity.Img;
import com.java.email.repository.ImgRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ImgService {

    @Autowired
    private ImgRepository imgRepository;

//    // 根据状态筛选图片
//    public List<Img> getImgsByStatus(int status) {
//        return imgRepository.findByStatus(status);
//    }
//
//    // 根据创建人 ID 筛选图片
//    public List<Img> getImgsByCreatorId(String creatorId) {
//        return imgRepository.findByCreatorId(creatorId);
//    }
//
//    // 根据图片名称模糊查询
//    public List<Img> getImgsByName(String imgName) {
//        return imgRepository.findByImgNameContaining(imgName);
//    }
//
//    // 根据图片大小范围筛选图片
//    public List<Img> getImgsBySizeRange(long minSize, long maxSize) {
//        return imgRepository.findByImgSizeBetween(minSize, maxSize);
//    }
//
//    // 根据创建时间范围筛选图片
//    public List<Img> getImgsByCreatedAtRange(long startTime, long endTime) {
//        return imgRepository.findByCreatedAtBetween(startTime, endTime);
//    }
//
//    // 根据状态和创建人 ID 组合筛选
//    public List<Img> getImgsByStatusAndCreatorId(int status, String creatorId) {
//        return imgRepository.findByStatusAndCreatorId(status, creatorId);
//    }

    /**
     * 根据条件筛选图片
     *
     * @param ownerUserIds 所属用户ID列表
     * @param creatorId    创建人ID
     * @param status       图片状态
     * @param imgName      图片名称
     * @param imgSize      图片大小
     * @param page         页码
     * @param size         每页大小
     * @return 符合条件的图片分页结果
     */
    public Result<Page<Img>> findImgsByCriteria(List<String> ownerUserIds, String creatorId,
                                                Integer status, String imgName, Long imgSize,
                                                int page, int size) {
        try {
            Page<Img> imgs;

            // 创建分页对象
            Pageable pageable =  PageRequest.of(page, size);

            // 动态构建查询条件
            if (ownerUserIds != null && !ownerUserIds.isEmpty()) {
                imgs = imgRepository.findByOwnerUserIdsIn(ownerUserIds, pageable);
            } else if (creatorId != null) {
                imgs = imgRepository.findByCreatorId(creatorId, pageable);
            } else if (status != null) {
                imgs = imgRepository.findByStatus(status, pageable);
            } else if (imgName != null) {
                imgs = imgRepository.findByImgName(imgName, pageable);
            } else if (imgSize != null) {
                imgs = imgRepository.findByImgSize(imgSize, pageable);
            } else {
                // 如果没有条件，返回所有图片（分页）
                imgs = imgRepository.findAll(pageable);
            }

            // 返回成功结果
            return Result.success(imgs);
        } catch (Exception e) {
            // 返回错误结果
            return Result.error("查询失败: " + e.getMessage());
        }
    }
}