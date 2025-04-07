package com.java.service;

import com.java.common.Response.Result;
import com.java.model.dto.CsvUserDto;
import com.java.model.vo.AssignUserDetailsVo;

import java.io.IOException;
import java.util.List;

public interface UserAssignService {
    public void assignUser(String user_id, String belong_user_id) throws IOException;
    public com.java.model.domain.Result BatchUserImport(List<CsvUserDto> csvUserDtoList) throws IOException;
    public AssignUserDetailsVo assignUserDetails(String user_id, String page_num, String page_size) throws IOException;

}
