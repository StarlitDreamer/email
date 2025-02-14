package com.java.email.model;

import java.util.List;

public class AreaVO {
    private String area_id;
    private String area_name;
    private List<String> country_name;

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

    public List<String> getCountry_name() {
        return country_name;
    }

    public void setCountry_name(List<String> country_name) {
        this.country_name = country_name;
    }
} 