package com.java.model.vo;

import com.java.model.domain.AssignProcess;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AssignUserDetailsVo {
    private int totalItems;
    private int pageNum;
    private int pageSize;
    private List<AssignProcess> assignProcess;
}
