package com.java.email.controller;

import com.java.email.common.Result;
import com.java.email.model.CommodityCreateRequest;
import com.java.email.model.CommodityFilterRequest;
import com.java.email.model.CommodityDeleteRequest;
import com.java.email.model.CommodityUpdateRequest;
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

    @PostMapping("/importCommodity")
    public Result<?> importCommodity(@RequestParam(value = "commodity.csv", required = false) MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return Result.error("文件不能为空");
        }
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

    @PostMapping("/filterCommodity")
    public Result<?> filterCommodity(@RequestBody CommodityFilterRequest request) {
        if (request == null) {
            return Result.error("请求参数不能为空");
        }
        if (request.getPage_num() == null || request.getPage_num() < 1) {
            request.setPage_num(1);
        }
        if (request.getPage_size() == null || request.getPage_size() < 1) {
            request.setPage_size(10);
        }
        return commodityService.filterCommodity(request);
    }

    @PostMapping("/deleteCommodity")
    public Result<?> deleteCommodity(@RequestBody CommodityDeleteRequest request) {
        if (request == null) {
            return Result.error("请求参数不能为空");
        }
        if (request.getCommodity_id() == null || request.getCommodity_id().trim().isEmpty()) {
            return Result.error("商品ID不能为空");
        }
        return commodityService.deleteCommodity(request);
    }

    @PostMapping("/updateCommodity")
    public Result<?> updateCommodity(@RequestBody CommodityUpdateRequest request) {
        if (request == null) {
            return Result.error("请求参数不能为空");
        }
        if (request.getCommodity_id() == null || request.getCommodity_id().trim().isEmpty()) {
            return Result.error("商品ID不能为空");
        }
        if (request.getCommodity_name() == null || request.getCommodity_name().trim().isEmpty()) {
            return Result.error("商品名称不能为空");
        }
        if (request.getCategory_id() == null || request.getCategory_id().trim().isEmpty()) {
            return Result.error("品类ID不能为空");
        }
        return commodityService.updateCommodity(request);
    }
} 