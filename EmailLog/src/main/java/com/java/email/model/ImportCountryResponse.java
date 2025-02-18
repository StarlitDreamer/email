package com.java.email.model;

public class ImportCountryResponse {
    private Integer success_count;
    private Integer fail_count;

    public Integer getSuccess_count() {
        return success_count;
    }

    public void setSuccess_count(Integer success_count) {
        this.success_count = success_count;
    }

    public Integer getFail_count() {
        return fail_count;
    }

    public void setFail_count(Integer fail_count) {
        this.fail_count = fail_count;
    }
} 