package com.java.email.model.response;


import com.java.email.model.entity.ReceiverSupplier;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FilterSupplierResponse {
    public List<ReceiverSupplier> receiverSupplier;
    public Integer total_items;
    public Integer page_num;
    public Integer page_size;
}
