package com.java.email.service;

import com.java.email.common.Result;
import com.java.email.model.AreaCreateRequest;
import com.java.email.model.AreaFilterRequest;
import com.java.email.model.AreaDeleteRequest;
import com.java.email.model.AreaUpdateRequest;

public interface AreaService {
    Result<?> createArea(AreaCreateRequest request);
    Result<?> filterArea(AreaFilterRequest request);
    Result<?> deleteArea(AreaDeleteRequest request);
    Result<?> updateArea(AreaUpdateRequest request);
} 