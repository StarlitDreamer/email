package com.java.controller;

import com.java.atuhcode.Auth;
import com.java.atuhcode.IAuth;
import com.java.email.common.userCommon.ThreadLocalUtil;
import com.java.model.domain.Result;
import com.java.model.dto.CreateUserDto;
import com.java.model.dto.UpdateUserDto;
import com.java.model.vo.AssignUserDetailsVo;
import com.java.model.vo.CheckUserVo;
import com.java.model.vo.FilterUserVo;
import com.java.service.UserAssignService;
import com.java.service.UserService;
import com.java.utils.ResultUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/userManage")
public class SenderManageController {
    @Autowired
    private UserService userService;
    @Autowired
    private UserAssignService userAssignService;

    @PostMapping("/createUser")
    public Result createUser(@RequestBody@Validated CreateUserDto user) throws IOException {
        String currentUserId = ThreadLocalUtil.getUserId();//通过treadLocal获取当前操作者的用户id
        try {
            if (!userService.hasAuth(currentUserId, Auth.USER_MANAGE.auth_id())) {
                return ResultUtils.error("没有权限创建用户");
            }
            String user_id = userService.createUser(user);
            return ResultUtils.success(user_id);
        } catch (Exception e) {
            return ResultUtils.error(e.getMessage());
        }
    }

    @GetMapping("/filterUser")
    public Result filterUser(@RequestParam(value = "user_name", required = false) String user_name, @RequestParam(value = "user_account", required = false) String user_account,
                             @RequestParam(value = "user_email", required = false) String user_email, @RequestParam(value = "belong_user_name") String belong_user_name, @RequestParam(value = "status") Integer status, @RequestParam(value = "page_num") Integer page_num,
                             @RequestParam(value = "page_size") Integer page_size) {
        String currentUserId = ThreadLocalUtil.getUserId();
        try {
            if (!userService.hasAuth(currentUserId, Auth.USER_MANAGE.auth_id())) {
                return ResultUtils.error("没有权限筛选用户");
            }
            FilterUserVo filterUserVo = userService.filterUser(user_name, user_account, user_email, belong_user_name, status, page_num, page_size);
            return ResultUtils.success(filterUserVo);
        } catch (Exception e) {
            return ResultUtils.error(e.getMessage());
        }
    }

    @GetMapping("/checkUser")
    public Result checkUser(@RequestParam(value = "user_id") String user_id) {
        String currentUserId = ThreadLocalUtil.getUserId();
        try {
            if (!userService.hasAuth(currentUserId, Auth.USER_MANAGE.auth_id())) {
                return ResultUtils.error("没有权限查看用户密码");
            }
            CheckUserVo checkUserVo = userService.checkUser(user_id);
            return ResultUtils.success(checkUserVo);
        } catch (Exception e) {
            return ResultUtils.error(e.getMessage());
        }
    }

    @PostMapping("/updateUserinfo")
    public Result updateUserinfo(@RequestBody@Validated UpdateUserDto user)  {
        String currentUserId = ThreadLocalUtil.getUserId();
        try {
            if (!userService.hasAuth(currentUserId, Auth.USER_MANAGE.auth_id())) {
                return ResultUtils.error("没有权限修改用户信息");
            }
            System.out.println(user);
            userService.updateUserinfo(user);
            return ResultUtils.success();
        } catch (Exception e) {
            return ResultUtils.error(e.getMessage());
        }
    }

    @PostMapping("/updateUserAuth")
    public Result updateUserAuth(@RequestParam(value = "user_id") String user_id, @RequestParam(value = "user_auth_id",required = false) List<String> user_auth_id, @RequestParam(value = "user_role",required = false) Integer user_role) {
        Integer currentUserRole = ThreadLocalUtil.getUserRole();
        try {
            if (currentUserRole.equals(4)) {
                return ResultUtils.error("没有权限修改用户信息");
            }
            userService.updateUserAuth(user_id, user_auth_id, user_role);
            return ResultUtils.success();
        } catch (Exception e) {
            e.printStackTrace();
            return ResultUtils.error("修改权限失败");
        }
    }

    @PostMapping("/deleteUser")
    public Result deleteUser(@RequestParam(value = "user_id") String user_id) {
        String currentUserId = ThreadLocalUtil.getUserId();
        try {
            if (!userService.hasAuth(currentUserId, Auth.USER_MANAGE.auth_id())) {
                return ResultUtils.error("没有权限删除用户");
            }
           userService.deleteUser(user_id);
            return ResultUtils.success();
        } catch (Exception e) {
            return ResultUtils.error(e.getMessage());
        }
    }
    @PostMapping("/assignUser")
    public Result assignUser(@RequestParam(value = "belong_user_id") String belong_user_id) {
        try {
            userAssignService.assignUser(belong_user_id);
            return ResultUtils.success();
        } catch (Exception e) {
            return ResultUtils.error("操作失败");
        }
    }
    @GetMapping("/getAssignUserDetails")
    public Result getAssignUserDetails(@RequestParam(value = "user_id") String user_id, @RequestParam(value = "page_num") String page_num, @RequestParam(value = "page_size") String page_size) {
        try {
            AssignUserDetailsVo assignUserDetailsVo = userAssignService.assignUserDetails(user_id, page_num, page_size);
            return ResultUtils.success(assignUserDetailsVo);
        } catch (IOException e) {
            return ResultUtils.error("操作失败");
        }
    }
   @GetMapping("/getAuth")
   public Result getAuth() {
       return ResultUtils.success(Auth.getAllAuths());
  }
}
