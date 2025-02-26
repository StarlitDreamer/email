package com.java.email.dto;


import com.java.email.entity.ReceiverCustomer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FilterCustomerResponse {
    public List<ReceiverCustomer> receiverCustomer;
    public Integer total_items;
    public Integer page_num;
    public Integer page_size;
}
