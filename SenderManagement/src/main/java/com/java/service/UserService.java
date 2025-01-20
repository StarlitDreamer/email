package com.java.service;

import com.java.model.dto.CreateUserDto;
import com.java.model.dto.UpdateUserDto;
import com.java.model.vo.CheckUserVo;
import com.java.model.vo.FilterUserVo;

import javax.validation.constraints.NotBlank;
import java.io.IOException;
import java.util.List;

public interface UserService {
    public String createUser(CreateUserDto user) throws IOException;
    public boolean hasAuth(String userId, String authId) throws IOException;
    public CheckUserVo checkUser(String user_id) throws IOException;
    public FilterUserVo filterUser(String user_name, String user_account, String user_email,String belong_user_name,Integer status,Integer page_num,Integer page_size) throws IOException;
    public void updateUserinfo(UpdateUserDto user) throws IOException;
    public void updateUserAuth(String user_id, List<String> user_auth_id,Integer user_role) throws IOException;
    public void deleteUser(String user_id) throws IOException;
}
