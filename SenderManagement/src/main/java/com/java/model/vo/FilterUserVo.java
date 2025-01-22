package com.java.model.vo;

import com.java.model.domain.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FilterUserVo {
    private long totalItems;
    private int pageNum;
    private int pageSize;
    private List<UserVo> user;
}
