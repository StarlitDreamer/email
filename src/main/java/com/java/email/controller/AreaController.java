package com.java.email.controller;

import com.java.email.common.Result;
import com.java.email.model.AreaCreateRequest;
import com.java.email.model.AreaFilterRequest;
import com.java.email.model.AreaDeleteRequest;
import com.java.email.model.AreaUpdateRequest;
import com.java.email.service.AreaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/area")
@CrossOrigin
public class AreaController {

    @Autowired
    private AreaService areaService;

    @PostMapping("/createArea")
    public Result<?> createArea(@RequestBody AreaCreateRequest request) {
        if (request == null) {
            return Result.error("请求参数不能为空");
        }
        if (request.getArea_name() == null || request.getArea_name().trim().isEmpty()) {
            return Result.error("区域名称不能为空");
        }
        if (request.getArea_country() == null || request.getArea_country().isEmpty()) {
            return Result.error("区域包含的国家不能为空");
        }
        return areaService.createArea(request);
    }

    @PostMapping("/filterArea")
    public Result<?> filterArea(@RequestBody AreaFilterRequest request) {
        if (request == null) {
            return Result.error("请求参数不能为空");
        }
        if (request.getPage_num() == null || request.getPage_num() < 1) {
            request.setPage_num(1);
        }
        if (request.getPage_size() == null || request.getPage_size() < 1) {
            request.setPage_size(10);
        }
        return areaService.filterArea(request);
    }

    @PostMapping("/deleteArea")
    public Result<?> deleteArea(@RequestBody AreaDeleteRequest request) {
        if (request == null) {
            return Result.error("请求参数不能为空");
        }
        if (request.getArea_id() == null || request.getArea_id().trim().isEmpty()) {
            return Result.error("区域ID不能为空");
        }
        return areaService.deleteArea(request);
    }

    @PostMapping("/updateArea")
    public Result<?> updateArea(@RequestBody AreaUpdateRequest request) {
        if (request == null) {
            return Result.error("请求参数不能为空");
        }
        if (request.getArea_id() == null || request.getArea_id().trim().isEmpty()) {
            return Result.error("区域ID不能为空");
        }
        return areaService.updateArea(request);
    }
} 