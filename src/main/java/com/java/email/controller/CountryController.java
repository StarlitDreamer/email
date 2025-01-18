package com.java.email.controller;

import com.java.email.common.Result;
import com.java.email.model.CountryFilterRequest;
import com.java.email.model.CountryCreateRequest;
import com.java.email.model.CountryDeleteRequest;
import com.java.email.model.CountryUpdateRequest;
import com.java.email.service.CountryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/country")
@CrossOrigin
public class CountryController {

    @Autowired
    private CountryService countryService;

    @PostMapping("/importCountry")
    public Result<?> importCountry(@RequestParam(value = "country.csv", required = false) MultipartFile file) {
        System.out.println("Received country file upload request");
        
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
        
        return countryService.importCountry(file);
    }

    @PostMapping("/filterCountry")
    public Result<?> filterCountry(@RequestBody CountryFilterRequest request) {
        if (request == null) {
            return Result.error("请求参数不能为空");
        }
        if (request.getPage_num() == null || request.getPage_num() < 1) {
            request.setPage_num(1);
        }
        if (request.getPage_size() == null || request.getPage_size() < 1) {
            request.setPage_size(10);
        }
        return countryService.filterCountry(request);
    }

    @PostMapping("/createCountry")
    public Result<?> createCountry(@RequestBody CountryCreateRequest request) {
        if (request == null) {
            return Result.error("请求参数不能为空");
        }
        if (request.getCountry_code() == null || request.getCountry_code().trim().isEmpty()) {
            return Result.error("国家代码不能为空");
        }
        if (request.getCountry_name() == null || request.getCountry_name().trim().isEmpty()) {
            return Result.error("国家名称不能为空");
        }
        return countryService.createCountry(request);
    }

    @PostMapping("/deleteCountry")
    public Result<?> deleteCountry(@RequestBody CountryDeleteRequest request) {
        if (request == null) {
            return Result.error("请求参数不能为空");
        }
        if (request.getCountry_id() == null || request.getCountry_id().trim().isEmpty()) {
            return Result.error("国家ID不能为空");
        }
        return countryService.deleteCountry(request);
    }

    @PostMapping("/updateCountry")
    public Result<?> updateCountry(@RequestBody CountryUpdateRequest request) {
        if (request == null) {
            return Result.error("请求参数不能为空");
        }
        if (request.getCountry_id() == null || request.getCountry_id().trim().isEmpty()) {
            return Result.error("国家ID不能为空");
        }
        return countryService.updateCountry(request);
    }
} 