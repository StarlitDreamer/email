package com.java.email.model;

import java.util.List;

public class CommodityFilterResponse {
    private Long total_items;
    private Integer page_num;
    private Integer page_size;
    private List<CommodityVO> commodity;

    public Long getTotal_items() {
        return total_items;
    }

    public void setTotal_items(Long total_items) {
        this.total_items = total_items;
    }

    public Integer getPage_num() {
        return page_num;
    }

    public void setPage_num(Integer page_num) {
        this.page_num = page_num;
    }

    public Integer getPage_size() {
        return page_size;
    }

    public void setPage_size(Integer page_size) {
        this.page_size = page_size;
    }

    public List<CommodityVO> getCommodity() {
        return commodity;
    }

    public void setCommodity(List<CommodityVO> commodity) {
        this.commodity = commodity;
    }
} 