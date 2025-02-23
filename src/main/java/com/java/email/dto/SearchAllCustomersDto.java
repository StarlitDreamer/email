package com.java.email.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SearchAllCustomersDto {
    public String commodity_name;
    public List<String> area_id;
    public List<String>  country_id;
    public Integer  trade_type;
    public Integer  receiver_level;
}
