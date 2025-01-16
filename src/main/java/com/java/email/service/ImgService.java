package com.java.email.service;

import com.java.email.entity.Img;
import com.java.email.repository.ImgRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ImgService {

    @Autowired
    private ImgRepository imgRepository;

    // 根据状态筛选图片
    public List<Img> getImgsByStatus(int status) {
        return imgRepository.findByStatus(status);
    }

    // 根据创建人 ID 筛选图片
    public List<Img> getImgsByCreatorId(String creatorId) {
        return imgRepository.findByCreatorId(creatorId);
    }

    // 根据图片名称模糊查询
    public List<Img> getImgsByName(String imgName) {
        return imgRepository.findByImgNameContaining(imgName);
    }

    // 根据图片大小范围筛选图片
    public List<Img> getImgsBySizeRange(long minSize, long maxSize) {
        return imgRepository.findByImgSizeBetween(minSize, maxSize);
    }

    // 根据创建时间范围筛选图片
    public List<Img> getImgsByCreatedAtRange(long startTime, long endTime) {
        return imgRepository.findByCreatedAtBetween(startTime, endTime);
    }

    // 根据状态和创建人 ID 组合筛选
    public List<Img> getImgsByStatusAndCreatorId(int status, String creatorId) {
        return imgRepository.findByStatusAndCreatorId(status, creatorId);
    }
}