package com.java.email.service;

import com.java.email.common.Result;
import com.java.email.model.CountryFilterRequest;
import org.springframework.web.multipart.MultipartFile;

public interface CountryService {
    Result<?> importCountry(MultipartFile file);
    Result<?> filterCountry(CountryFilterRequest request);
} 