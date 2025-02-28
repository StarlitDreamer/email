package com.java.email.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SearchAllSupplierDto {
    public String commodity_name;
    public List<String> area_id;
    public List<String>  supplier_country_id;
    public Integer  is_user;
    public Integer  trade_type;
    public Integer  supplier_level;
}
