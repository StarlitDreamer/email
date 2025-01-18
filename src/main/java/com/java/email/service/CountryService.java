package com.java.email.service;

import com.java.email.common.Result;
import com.java.email.model.CountryFilterRequest;
import com.java.email.model.CountryCreateRequest;
import com.java.email.model.CountryDeleteRequest;
import com.java.email.model.CountryUpdateRequest;
import org.springframework.web.multipart.MultipartFile;

public interface CountryService {
    Result<?> importCountry(MultipartFile file);
    Result<?> filterCountry(CountryFilterRequest request);
    Result<?> createCountry(CountryCreateRequest request);
    Result<?> deleteCountry(CountryDeleteRequest request);
    Result<?> updateCountry(CountryUpdateRequest request);
} 