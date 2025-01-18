package com.java.email.service;

import com.java.email.common.Result;
import com.java.email.model.CategoryCreateRequest;
import com.java.email.model.CategoryFilterRequest;
import org.springframework.web.multipart.MultipartFile;

public interface CommodityService {
    Result<?> importCategory(MultipartFile file);
    Result<?> createCategory(CategoryCreateRequest request);
    Result<?> filterCategory(CategoryFilterRequest request);
} 