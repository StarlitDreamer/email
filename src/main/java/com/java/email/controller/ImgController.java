package com.java.email.controller;

import com.java.email.common.Result;
import com.java.email.entity.Img;
import com.java.email.service.ImgService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/imgs")
public class ImgController {

    @Autowired
    private ImgService imgService;

    /**
     * 根据条件筛选图片
     *
     * @param belongUserId 所属用户ID列表
     * @param creatorId    创建人ID
     * @param status       图片状态
     * @param imgName      图片名称
     * @param imgSize      图片大小
     * @param page         页码
     * @param size         每页大小
     * @return 符合条件的图片分页结果
     */
    @GetMapping("/search")
    public Result<Page<Img>> findImgsByCriteria(
            @RequestParam(required = false) List<String> belongUserId,
            @RequestParam(required = false) String creatorId,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String imgName,
            @RequestParam(required = false) Long imgSize,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return imgService.findImgsByCriteria(
                belongUserId, creatorId, status, imgName, imgSize, page, size);
    }
}