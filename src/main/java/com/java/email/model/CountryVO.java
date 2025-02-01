package com.java.email.model;

public class CountryVO {
    private String country_id;        // 国家ID
    private String country_code;      // 国家代码，例如：zh、us等
    private String country_name;      // 国家名称，例如：中国、美国等
    private String created_at;        // ISO 格式的创建时间
    private String updated_at;        // ISO 格式的更新时间

    public String getCountry_id() {
        return country_id;
    }

    public void setCountry_id(String country_id) {
        this.country_id = country_id;
    }

    public String getCountry_code() {
        return country_code;
    }

    public void setCountry_code(String country_code) {
        this.country_code = country_code;
    }

    public String getCountry_name() {
        return country_name;
    }

    public void setCountry_name(String country_name) {
        this.country_name = country_name;
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