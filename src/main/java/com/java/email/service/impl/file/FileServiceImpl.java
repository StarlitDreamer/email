package com.java.email.service.impl.file;

import com.java.email.common.Response.PageResponse;
import com.java.email.common.Response.Result;
import com.java.email.common.Response.ResultCode;
import com.java.email.common.userCommon.ThreadLocalUtil;
import com.java.email.constant.UserConstData;
import com.java.email.service.file.FileService;
import com.java.email.utils.LogUtil;

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;

import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;

import com.java.email.esdao.repository.user.UserRepository;
import com.java.email.model.entity.user.UserDocument;

@Service
public class FileServiceImpl implements FileService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ElasticsearchOperations elasticsearchOperations;

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

    @Override
    public Result filterAdmin(Map<String, Object> params) {
        // 获取当前用户角色
        Integer userRole = ThreadLocalUtil.getUserRole();
        if (userRole != null && (userRole == 2 || userRole == 3)) {
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
            // 构建查询条件
            BoolQuery.Builder mainQuery = new BoolQuery.Builder();

            // 添加角色条件
            mainQuery.must(m -> m
            .bool(b -> b
                    .should(s -> s
                            .term(t -> t
                                    .field("user_role")
                                    .value(2)
                            )
                    )
                    .should(s -> s
                            .term(t -> t
                                    .field("user_role")
                                    .value(3)
                                    )
                            )
                            .minimumShouldMatch("1")
                    )
            );

            // 添加搜索条件
            if (!userName.isEmpty()) {
                String finalUserName = userName;
                mainQuery.must(m -> m
                        .match(t -> t
                                .field("user_name")
                                .query(finalUserName)
                        )
                );
            }
            if (!userAccount.isEmpty()) {
                String finalUserAccount = userAccount;
                mainQuery.must(m -> m
                        .term(t -> t
                                .field("user_account")
                                .value(finalUserAccount)
                        )
                );
            }
            if (!userEmail.isEmpty()) {
                String finalUserEmail = userEmail;
                mainQuery.must(m -> m
                        .term(t -> t
                                .field("user_email")
                                .value(finalUserEmail)
                        )
                );
            }

            // 构建分页
            Pageable pageable = PageRequest.of(pageNum - 1, pageSize);

            // 构建查询
            NativeQuery searchQuery = NativeQuery.builder()
                    .withQuery(q -> q.bool(mainQuery.build()))
                    .withSort(Sort.by(Sort.Direction.DESC, "updated_at"))
                    .withPageable(pageable)
                    .build();

            SearchHits<UserDocument> searchHits = elasticsearchOperations.search(
                        searchQuery,
                        UserDocument.class
            );

            // 处理结果
            List<Map<String, Object>> content = searchHits.stream()
                    .map(hit -> {
                        UserDocument doc = hit.getContent();
                        Map<String, Object> item = new HashMap<>();
                        item.put("id", doc.getUserId());
                        item.put("name", doc.getUserName());
                        return item;
                    })
                    .collect(Collectors.toList());

            return new Result(
                    ResultCode.R_Ok,
                    new PageResponse<>(
                            searchHits.getTotalHits(),
                            pageNum,
                            pageSize,
                            content
                    )
            );
        }
        return new Result(ResultCode.R_NoAuth);
    }
}