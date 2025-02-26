package com.java.email.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FilterCustomersDto {
    public String commodity_name;
    public List<String> area_id;
    public List<String>  customer_country_id;
    public Integer  trade_type;
    public Integer  customer_level;
    @Builder.Default
    public Integer  page_num=1;
    @Builder.Default
    public Integer  page_size=10;
}