package com.java.email.common.Response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageResponse<T> {
    private long totalItems;
    private int pageNum;
    private int pageSize;
    private List<T> items;
}
