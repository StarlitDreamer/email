package com.java.email.service;

import com.java.email.common.Result;
import com.java.email.model.CategoryCreateRequest;
import com.java.email.model.CategoryFilterRequest;
import com.java.email.model.CategoryDeleteRequest;
import com.java.email.model.CommodityCreateRequest;
import com.java.email.model.CommodityFilterRequest;
import com.java.email.model.CommodityDeleteRequest;
import com.java.email.model.CommodityUpdateRequest;
import org.springframework.web.multipart.MultipartFile;

public interface CommodityService {
    Result<?> importCategory(MultipartFile file);
    Result<?> createCategory(CategoryCreateRequest request);
    Result<?> filterCategory(CategoryFilterRequest request);
    Result<?> deleteCategory(CategoryDeleteRequest request);
    Result<?> importCommodity(MultipartFile file);
    Result<?> createCommodity(CommodityCreateRequest request);
    Result<?> filterCommodity(CommodityFilterRequest request);
    Result<?> deleteCommodity(CommodityDeleteRequest request);
    Result<?> updateCommodity(CommodityUpdateRequest request);
} 