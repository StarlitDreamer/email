package com.java.email.service.impl.file;

import com.java.email.common.Response.PageResponse;
import com.java.email.common.Response.Result;
import com.java.email.common.Response.ResultCode;
import com.java.email.common.userCommon.ThreadLocalUtil;
import com.java.email.constant.UserConstData;
import com.java.email.service.file.FileService;
import com.java.email.utils.LogUtil;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import com.java.email.esdao.repository.user.UserRepository;
import com.java.email.model.entity.user.UserDocument;

@Service
public class FileServiceImpl implements FileService {

    @Autowired
    private UserRepository userRepository;

    private static final LogUtil logUtil = LogUtil.getLogger(FileServiceImpl.class);

    @Override
    public Result filterUser(Map<String, Object> params) {
        // 获取当前用户角色
        Integer userRole = ThreadLocalUtil.getUserRole();
        if (userRole == null) {
            return new Result(ResultCode.R_Error);
        }
        // 获取当前用户ID
        String currentUserId = ThreadLocalUtil.getUserId();
        if (currentUserId == null) {
            return new Result(ResultCode.R_Error);
        }
        logUtil.info("当前用户: " + userRole + " " + currentUserId);

        // 参数校验
        if (params == null) {
            return new Result(ResultCode.R_ParamError);
        }
        String userName = (String) params.get("user_name");
        String userAccount = (String) params.get("user_account");
        String userEmail = (String) params.get("user_email");
        Integer pageNum = params.get("page_num") != null ? (Integer) params.get("page_num") : 1;
        Integer pageSize = params.get("page_size") != null ? (Integer) params.get("page_size") : 30;

        // 校验分页参数
        if (pageNum < 1 || pageSize < 1) {
            return new Result(ResultCode.R_PageError);
        }

        // 处理搜索参数,如果为null则设为空字符串
        userName = userName != null ? userName : "";
        userAccount = userAccount != null ? userAccount : "";
        userEmail = userEmail != null ? userEmail : "";

        // 根据角色判断查询逻辑
        if (userRole.equals(UserConstData.ROLE_ADMIN_LARGE)) {
            // 角色2可以直接查询所有符合条件的用户
            List<UserDocument> users = userRepository.findByUserNameLikeAndUserAccountLikeAndUserEmailLike(
                    userName, userAccount, userEmail);

            // 过滤掉userId为1的用户，并且只返回id和name
            List<Map<String, String>> filteredUsers = users.stream()
                    .filter(u -> !UserConstData.COMPANY_USER_ID.equals(u.getUserId()))
                    .map(user -> {
                        Map<String, String> filtered = new HashMap<>();
                        filtered.put("id", user.getUserId());
                        filtered.put("name", user.getUserName());
                        return filtered;
                    })
                    .collect(Collectors.toList());

            // 计算总数
            int total = filteredUsers.size();

            // 分页处理
            int start = (pageNum - 1) * pageSize;
            int end = Math.min(start + pageSize, total);
            if (start >= total) {
                return new Result(ResultCode.R_NoData);
            }
            List<Map<String, String>> pagedUsers = filteredUsers.subList(start, end);

            // 构建分页响应
            PageResponse<Map<String, String>> pageResponse = new PageResponse<>();
            pageResponse.setTotal_items(total);
            pageResponse.setPage_num(pageNum);
            pageResponse.setPage_size(pageSize);
            pageResponse.setData(pagedUsers);

            return new Result(ResultCode.R_Ok, pageResponse);

        } else if (userRole.equals(UserConstData.ROLE_ADMIN_SMALL)) {
            // 角色3需要过滤,只返回自己和下属
            List<UserDocument> users = userRepository.findByUserNameLikeAndUserAccountLikeAndUserEmailLike(
                    userName, userAccount, userEmail);

            // 过滤结果,只保留自己和下属
            List<Map<String, String>> filteredUsers = users.stream()
                    .filter(u -> currentUserId.equals(u.getUserId()) || // 是自己
                            currentUserId.equals(u.getBelongUserId())) // 是下属
                    .map(user -> {
                        Map<String, String> filtered = new HashMap<>();
                        filtered.put("id", user.getUserId());
                        filtered.put("name", user.getUserName());
                        return filtered;
                    })
                    .collect(Collectors.toList());

            // 计算总数
            int total = filteredUsers.size();

            // 分页处理
            int start = (pageNum - 1) * pageSize;
            int end = Math.min(start + pageSize, total);

            List<Map<String, String>> pagedUsers = filteredUsers.subList(start, end);

            // 构建分页响应
            PageResponse<Map<String, String>> pageResponse = new PageResponse<>();
            pageResponse.setTotal_items(total);
            pageResponse.setPage_num(pageNum);
            pageResponse.setPage_size(pageSize);
            pageResponse.setData(pagedUsers);

            return new Result(ResultCode.R_Ok, pageResponse);

        } else {
            return new Result(ResultCode.R_NoAuth);
        }
    }
}