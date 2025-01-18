package com.java.email.controller;

import com.java.email.common.Result;
import com.java.email.model.CategoryCreateRequest;
import com.java.email.model.CategoryFilterRequest;
import com.java.email.model.CategoryDeleteRequest;
import com.java.email.model.CommodityCreateRequest;
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

    @PostMapping("/createCategory")
    public Result<?> createCategory(@RequestBody CategoryCreateRequest request) {
        if (request == null || request.getCategory_name() == null || request.getCategory_name().trim().isEmpty()) {
            return Result.error("品类名称不能为空");
        }
        return commodityService.createCategory(request);
    }

    @PostMapping("/filterCategory")
    public Result<?> filterCategory(@RequestBody CategoryFilterRequest request) {
        if (request == null) {
            return Result.error("请求参数不能为空");
        }
        if (request.getPage_num() == null || request.getPage_num() < 1) {
            request.setPage_num(1);
        }
        if (request.getPage_size() == null || request.getPage_size() < 1) {
            request.setPage_size(10);
        }
        return commodityService.filterCategory(request);
    }

    @PostMapping("/deleteCategory")
    public Result<?> deleteCategory(@RequestBody CategoryDeleteRequest request) {
        if (request == null) {
            return Result.error("请求参数不能为空");
        }
        if (request.getCategory_id() == null || request.getCategory_id().trim().isEmpty()) {
            return Result.error("品类ID不能为空");
        }
        return commodityService.deleteCategory(request);
    }

    @PostMapping("/importCommodity")
    public Result<?> importCommodity(@RequestParam(value = "commodity.csv", required = false) MultipartFile file) {
        System.out.println("Received commodity file upload request");
        
        if (file == null) {
            System.out.println("File is null");
            return Result.error("文件不能为空");
        }
        
        if (file.isEmpty()) {
            System.out.println("File is empty");
            return Result.error("文件不能为空");
        }
        
        System.out.println("File name: " + file.getOriginalFilename());
        System.out.println("Content type: " + file.getContentType());
        System.out.println("File size: " + file.getSize());
        
        return commodityService.importCommodity(file);
    }

    @PostMapping("/createCommodity")
    public Result<?> createCommodity(@RequestBody CommodityCreateRequest request) {
        if (request == null) {
            return Result.error("请求参数不能为空");
        }
        if (request.getCommodity_name() == null || request.getCommodity_name().trim().isEmpty()) {
            return Result.error("商品名称不能为空");
        }
        if (request.getCategory_id() == null || request.getCategory_id().trim().isEmpty()) {
            return Result.error("品类ID不能为空");
        }
        return commodityService.createCommodity(request);
    }
} 