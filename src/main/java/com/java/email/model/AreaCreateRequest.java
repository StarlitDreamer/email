package com.java.email.model;

import java.util.List;

public class AreaCreateRequest {
    private String area_name;         // 区域名称，例如：欧洲、亚洲等
    private List<String> area_country; // 区域包含的国家ID列表

    public String getArea_name() {
        return area_name;
    }

    public void setArea_name(String area_name) {
        this.area_name = area_name;
    }

    public List<String> getArea_country() {
        return area_country;
    }

    public void setArea_country(List<String> area_country) {
        this.area_country = area_country;
    }
} 