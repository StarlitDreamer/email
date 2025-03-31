package com.java.email.model;

import java.util.List;
import java.util.Map;
public class AreaVO {
    private String area_id;           // 区域ID
    private String area_name;         // 区域名称，例如：欧洲、亚洲等
    private List<Map<String, Object>> area_country; // 区域包含的国家ID列表
    private String created_at;        // ISO 格式的创建时间
    private String updated_at;        // ISO 格式的更新时间

    public String getArea_id() {
        return area_id;
    }

    public void setArea_id(String area_id) {
        this.area_id = area_id;
    }

    public String getArea_name() {
        return area_name;
    }

    public void setArea_name(String area_name) {
        this.area_name = area_name;
    }

    public List<Map<String, Object>> getArea_country() {
        return area_country;
    }

    public void setArea_country(List<Map<String, Object>> area_country) {
        this.area_country = area_country;
    }

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    public String getUpdated_at() {
        return updated_at;
    }

    public void setUpdated_at(String updated_at) {
        this.updated_at = updated_at;
    }
} 