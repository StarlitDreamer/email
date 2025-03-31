package com.java.email.model;

import java.util.List;

public class ImportCountryResponse {
    private Integer success_count;
    private Integer fail_count;
    private List<String> errorMsg;

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

    public List<String> getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(List<String> errorMsg) {
        this.errorMsg = errorMsg;
    }
} 