package com.java.service;

import com.java.model.vo.AssignUserDetailsVo;

import java.io.IOException;

public interface UserAssignService {
    public void assignUser(String belong_user_id) throws IOException;
    public AssignUserDetailsVo assignUserDetails(String user_id, String page_num, String page_size) throws IOException;
}
