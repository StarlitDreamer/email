package com.java.email.controller;

import com.java.email.common.Result;
import com.java.email.entity.Img;
import com.java.email.service.ImgService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/imgs")
public class ImgController {

    @Autowired
    private ImgService imgService;

    // 根据状态筛选图片
    @GetMapping("/by-status/{status}")
    public Result<List<Img>> getImgsByStatus(@PathVariable int status) {
        return Result.success(imgService.getImgsByStatus(status));
    }

    // 根据创建人 ID 筛选图片
    @GetMapping("/by-creator/{creatorId}")
    public Result<List<Img>> getImgsByCreatorId(@PathVariable String creatorId) {
        return Result.success(imgService.getImgsByCreatorId(creatorId));
    }

    // 根据图片名称模糊查询
    @GetMapping("/by-name/{imgName}")
    public Result<List<Img>> getImgsByName(@PathVariable String imgName) {
        return Result.success(imgService.getImgsByName(imgName));
    }

    // 根据图片大小范围筛选图片
    @GetMapping("/by-size/{minSize}/{maxSize}")
    public Result<List<Img>> getImgsBySizeRange(
            @PathVariable long minSize,
            @PathVariable long maxSize) {
        return Result.success(imgService.getImgsBySizeRange(minSize, maxSize));
    }

    // 根据创建时间范围筛选图片
    @GetMapping("/by-created-at/{startTime}/{endTime}")
    public Result<List<Img>> getImgsByCreatedAtRange(
            @PathVariable long startTime,
            @PathVariable long endTime) {
        return Result.success(imgService.getImgsByCreatedAtRange(startTime, endTime));
    }

    // 根据状态和创建人 ID 组合筛选
    @GetMapping("/by-status-and-creator/{status}/{creatorId}")
    public Result<List<Img>> getImgsByStatusAndCreatorId(
            @PathVariable int status,
            @PathVariable String creatorId) {
        return Result.success(imgService.getImgsByStatusAndCreatorId(status, creatorId));
    }
}