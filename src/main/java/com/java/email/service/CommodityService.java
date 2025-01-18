package com.java.email.service;

import com.java.email.common.Result;
import org.springframework.web.multipart.MultipartFile;

public interface CommodityService {
    Result<?> importCategory(MultipartFile file);
} 