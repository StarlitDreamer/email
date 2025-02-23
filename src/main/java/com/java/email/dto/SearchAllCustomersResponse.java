package com.java.email.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SearchAllCustomersResponse {
    public Integer total_items;
    public String  receiver_key;
}
