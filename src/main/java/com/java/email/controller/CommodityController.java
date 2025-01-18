package com.java.email.controller;

import com.java.email.common.Result;
import com.java.email.service.CommodityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/commodity")
@CrossOrigin
public class CommodityController {

    @Autowired
    private CommodityService commodityService;

    @PostMapping("/importCategory")
    public Result<?> importCategory(
            @RequestParam(value = "category.csv", required = false) MultipartFile paramFile) {
        
        System.out.println("Received file upload request");
        
        // 打印请求信息，帮助调试
        if (paramFile == null) {
            System.out.println("File is null");
            return Result.error("文件不能为空");
        }
        
        if (paramFile.isEmpty()) {
            System.out.println("File is empty");
            return Result.error("文件不能为空");
        }
        
        // 打印更多文件信息以帮助调试
        System.out.println("File name: " + paramFile.getOriginalFilename());
        System.out.println("Content type: " + paramFile.getContentType());
        System.out.println("File size: " + paramFile.getSize());
        
        return commodityService.importCategory(paramFile);
    }
} 