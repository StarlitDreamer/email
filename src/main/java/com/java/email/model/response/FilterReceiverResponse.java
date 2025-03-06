package com.java.email.model.response;

import com.java.email.model.entity.Receiver;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FilterReceiverResponse {
    public Long total_items;
    public Integer page_num;
    public Integer page_size;
    public List<Receiver> receiver;
}
