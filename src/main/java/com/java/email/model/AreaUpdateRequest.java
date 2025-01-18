package com.java.email.model;

import java.util.List;

public class AreaUpdateRequest {
    private String area_id;
    private String area_name;
    private List<String> country_id;

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

    public List<String> getCountry_id() {
        return country_id;
    }

    public void setCountry_id(List<String> country_id) {
        this.country_id = country_id;
    }
} 