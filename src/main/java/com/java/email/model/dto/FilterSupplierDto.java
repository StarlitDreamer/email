package com.java.email.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FilterSupplierDto {
    public String commodity_name;
    public List<String> area_id;
    public List<String>  supplier_country_id;
    public Integer  trade_type;
    public Integer supplier_level;
    @Builder.Default
    public Integer  page_num=1;
    @Builder.Default
    public Integer  page_size=10;
}
