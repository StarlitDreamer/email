package com.java.email.controller;

import com.java.email.common.Result;
import com.java.email.model.CategoryCreateRequest;
import com.java.email.model.CategoryFilterRequest;
import com.java.email.model.CategoryDeleteRequest;
import com.java.email.model.CategoryUpdateRequest;
import com.java.email.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/commodity")
@CrossOrigin
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @PostMapping("/importCategory")
    public Result<?> importCategory(@RequestParam(value = "category.csv", required = false) MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return Result.error("文件不能为空");
        }
        return categoryService.importCategory(file);
    }

    @PostMapping("/createCategory")
    public Result<?> createCategory(@RequestBody CategoryCreateRequest request) {
        if (request == null || request.getCategory_name() == null || request.getCategory_name().trim().isEmpty()) {
            return Result.error("品类名称不能为空");
        }
        return categoryService.createCategory(request);
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
        return categoryService.filterCategory(request);
    }

    @PostMapping("/deleteCategory")
    public Result<?> deleteCategory(@RequestBody CategoryDeleteRequest request) {
        if (request == null) {
            return Result.error("请求参数不能为空");
        }
        if (request.getCategory_id() == null || request.getCategory_id().trim().isEmpty()) {
            return Result.error("品类ID不能为空");
        }
        return categoryService.deleteCategory(request);
    }

    @PostMapping("/updateCategory")
    public Result<?> updateCategory(@RequestBody CategoryUpdateRequest request) {
        if (request == null) {
            return Result.error("请求参数不能为空");
        }
        if (request.getCategory_id() == null || request.getCategory_id().trim().isEmpty()) {
            return Result.error("品类ID不能为空");
        }
        if (request.getCategory_name() == null || request.getCategory_name().trim().isEmpty()) {
            return Result.error("品类名称不能为空");
        }
        return categoryService.updateCategory(request);
    }
} 