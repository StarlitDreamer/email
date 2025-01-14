package com.java.email.service.impl.file;

import com.java.email.common.Response.PageResponse;
import com.java.email.common.Response.Result;
import com.java.email.common.Response.ResultCode;
import com.java.email.constant.AuthConstData;
import com.java.email.constant.MagicMathConstData;
import com.java.email.constant.UserConstData;
import com.java.email.model.entity.UserDocument;
import com.java.email.model.entity.file.ImgAssignDocument;
import com.java.email.model.entity.file.ImgDocument;
import com.java.email.esdao.repository.file.ImgRepository;
import com.java.email.esdao.repository.file.ImgAssignRepository;
import com.java.email.esdao.repository.user.UserRepository;
import com.java.email.service.file.ImgService;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.java.email.utils.LogUtil;
import com.java.email.common.userCommon.AuthValidation;
import com.java.email.common.userCommon.SubordinateValidation;
import com.java.email.common.userCommon.SubordinateValidation.ValidationResult;
import com.java.email.common.userCommon.ThreadLocalUtil;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ImgServiceImpl implements ImgService {

    @Autowired
    private ImgRepository imgRepository;

    @Autowired
    private ImgAssignRepository imgAssignRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ElasticsearchOperations elasticsearchOperations;

    @Autowired
    private SubordinateValidation subordinateValidation;

    private static final LogUtil logUtil = LogUtil.getLogger(ImgServiceImpl.class);

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result uploadImg(Map<String, List<Map<String, String>>> request) {
        try {
            String userId = ThreadLocalUtil.getUserId();
            if (userId == null) {
                return new Result(ResultCode.R_UserNotFound);
            }
            // 参数校验
            if (request == null || !request.containsKey("img")) {
                return new Result(ResultCode.R_ParamError);
            }
            List<Map<String, String>> images = request.get("img");
            if (images == null || images.isEmpty()) {
                return new Result(ResultCode.R_ParamError);
            }

            // 获取当前时间
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
            String currentTime = LocalDateTime.now().format(formatter);

            // 获取当前用户ID
            List<String> belongUserIds = new ArrayList<>();
            belongUserIds.add(userId);

            // 插入图片
            List<ImgDocument> imgDocuments = new ArrayList<>();
            for (Map<String, String> img : images) {
                ImgDocument doc = new ImgDocument();
                doc.setImgId(img.get("img_id"));
                doc.setImgUrl(img.get("img_url"));
                doc.setImgSize(img.get("img_size"));
                doc.setImgName(img.get("img_name"));
                doc.setCreatorId(userId);
                doc.setBelongUserId(belongUserIds);
                doc.setStatus(MagicMathConstData.IMG_STATUS_UNASSIGNED);
                doc.setCreatedAt(currentTime);
                doc.setUpdatedAt(currentTime);
                imgDocuments.add(doc);
            }

            imgRepository.saveAll(imgDocuments);
            return new Result(ResultCode.R_Ok);
        } catch (Exception e) {
            logUtil.error("Error saving images: " + e.getMessage());
            throw e;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result assignImg(Map<String, Object> request) {
        try {
            // 获取当前用户角色
            Integer userRole = ThreadLocalUtil.getUserRole();
            if (userRole == null || (userRole != 2 && userRole != 3)) {
                return new Result(ResultCode.R_NoAuth);
            }

            // 检查参数
            if (!request.containsKey("img_id") || !request.containsKey("belong_user_id")) {
                return new Result(ResultCode.R_ParamError);
            }

            String imgId = (String) request.get("img_id");
            @SuppressWarnings("unchecked")
            List<String> belongUserIds = (List<String>) request.get("belong_user_id");

            if (imgId == null || belongUserIds == null || belongUserIds.isEmpty()) {
                return new Result(ResultCode.R_ParamError);
            }

            // 检查附件是否存在
            ImgDocument imgDoc = imgRepository.findById(imgId).orElse(null);
            if (imgDoc == null) {
                return new Result(ResultCode.R_ImgNotFound);
            }

            // 检查所有用户ID是否存在
            String assignId = ThreadLocalUtil.getUserId();
            if (assignId == null) {
                return new Result(ResultCode.R_Error);
            }
            // 先查询并验证所有用户,如果全部合格，则保存起来可以直接用，不用每次都查询
            Map<String, UserDocument> userDocs = new HashMap<>();
            for (String userId : belongUserIds) {
                UserDocument userDoc = userRepository.findByUserId(userId).orElse(null);
                if (userDoc == null) {
                    return new Result(ResultCode.R_Error, "User " + userId + " not found");
                }
                userDocs.put(userId, userDoc);
            }

            // 如果是小管理员(role=3)，检查用户是否属于自己管理
            if (userRole == 3) {
                if (!belongUserIds.contains("1")) {
                    for (UserDocument userDoc : userDocs.values()) {
                        if (!userDoc.getBelongUserId().equals(assignId)) {
                            return new Result(ResultCode.R_NotBelongToAdmin);
                        }
                    }
                }

            }

            // 更新附件的归属用户
            imgDoc.setBelongUserId(belongUserIds);
            imgDoc.setStatus(MagicMathConstData.IMG_STATUS_ASSIGNED);
            imgDoc.setUpdatedAt(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")));
            imgRepository.save(imgDoc);

            // 创建分配记录
            Map<String, Object> process = new HashMap<>();
            process.put("assignor_id", assignId);
            process.put("assignor_name", ThreadLocalUtil.getUserName());

            List<Map<String, String>> assigneeList = new ArrayList<>();
            for (UserDocument userDoc : userDocs.values()) {
                Map<String, String> assignee = new HashMap<>();
                assignee.put("id", userDoc.getUserId());
                assignee.put("name", userDoc.getUserName());
                assigneeList.add(assignee);
            }
            process.put("assignee", assigneeList);
            process.put("assign_date", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")));

            // 查找现有的分配记录进行追加，没有则创建新的。
            ImgAssignDocument existingAssignDoc = imgAssignRepository.findById(imgId).orElse(null);
            List<Map<String, Object>> processList;
            if (existingAssignDoc != null) {
                processList = existingAssignDoc.getAssignProcess();
                processList.add(0, process);
                existingAssignDoc.setAssignProcess(processList);
                imgAssignRepository.save(existingAssignDoc);
            } else {
                processList = new ArrayList<>();
                processList.add(process);
                ImgAssignDocument assignDoc = new ImgAssignDocument();
                assignDoc.setImgId(imgId);
                assignDoc.setAssignProcess(processList);
                imgAssignRepository.save(assignDoc);
            }
            return new Result(ResultCode.R_Ok);

        } catch (Exception e) {
            logUtil.error("Error assigning attachment: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public Result assignImgDetails(Map<String, Object> request) {
        // 参数校验
        if (!request.containsKey("img_id") || !request.containsKey("page_num") || !request.containsKey("page_size")) {
            return new Result(ResultCode.R_ParamError);
        }
        Integer pageNum = (Integer) request.get("page_num");
        Integer pageSize = (Integer) request.get("page_size");
        if (pageNum <= 0 || pageSize <= 0) {
            return new Result(ResultCode.R_PageError);
        }
        String imgId = (String) request.get("img_id");
        ImgAssignDocument assignDoc = imgAssignRepository.findById(imgId).orElse(null);
        if (assignDoc == null) {
            return new Result(ResultCode.R_ImgNotFound);
        }

        try {
            // 查找附件分配记录
            List<Map<String, Object>> processList = assignDoc.getAssignProcess();
            if (processList == null || processList.isEmpty()) {
                return new Result(ResultCode.R_Ok, new PageResponse<>(0, pageNum, pageSize, new ArrayList<>()));
            }

            // 计算分页
            int total = processList.size();
            int start = (pageNum - 1) * pageSize;
            int end = Math.min(start + pageSize, total);

            // 验证分页参数
            if (start >= total) {
                return new Result(ResultCode.R_NoData);
            }

            // 获取当前页的数据
            List<Map<String, Object>> pageData = processList.subList(start, end);
            return new Result(ResultCode.R_Ok, new PageResponse<>(total, pageNum, pageSize, pageData));

        } catch (Exception e) {
            logUtil.error("Error getting img assign details: " + e.getMessage());
            return new Result(ResultCode.R_Error, "Error getting img assign details: " + e.getMessage());
        }
    }

    @Override
    public Result deleteImg(Map<String, Object> request) {
        // 参数校验
        if (!request.containsKey("img_id")) {
            return new Result(ResultCode.R_ParamError);
        }
        String imgId = (String) request.get("img_id");
        ImgDocument imgDoc = imgRepository.findById(imgId).orElse(null);
        if (imgDoc == null) {
            return new Result(ResultCode.R_ImgNotFound);
        }
        // 获取当前用户信息
        String currentUserId = ThreadLocalUtil.getUserId();
        Integer userRole = ThreadLocalUtil.getUserRole();
        if (currentUserId == null || userRole == null) {
            return new Result(ResultCode.R_UserNotFound);
        }

        // 角色2可以直接删除
        if (userRole == 2) {
            // Continue to delete
        }
        // 角色3需要检查创建者和所属用户
        else if (userRole == 3) {
            String creatorId = imgDoc.getCreatorId();
            List<String> belongUserIds = imgDoc.getBelongUserId();

            // 检查是否为创建者且在所属用户中
            boolean isCreatorAndBelongs = currentUserId.equals(creatorId) &&
                    (belongUserIds != null && belongUserIds.contains(currentUserId));

            // 如果是创建者且在所属用户中，直接删除
            if (isCreatorAndBelongs) {
                try {
                    imgRepository.deleteById(imgId);
                    return new Result(ResultCode.R_Ok);
                } catch (Exception e) {
                    logUtil.error("Error deleting img: " + e.getMessage());
                    return new Result(ResultCode.R_DeleteFileError);
                }
            }

            // 检查创建者和所属用户是否都包含下属
            boolean hasSubordinates = false;
            // 检查创建者是否为下属
            UserDocument creator = userRepository.findByUserId(creatorId).orElse(null);
            boolean creatorIsSubordinate = creator != null && currentUserId.equals(creator.getBelongUserId());

            // 检查所属用户是否包含下属
            boolean hasBelongUserSubordinate = false;
            if (belongUserIds != null && !belongUserIds.isEmpty()) {
                hasBelongUserSubordinate = belongUserIds.stream()
                        .map(id -> userRepository.findByUserId(id).orElse(null))
                        .filter(user -> user != null)
                        .anyMatch(user -> currentUserId.equals(user.getBelongUserId()));
            }

            hasSubordinates = creatorIsSubordinate && hasBelongUserSubordinate;
            if (!hasSubordinates) {
                return new Result(ResultCode.R_NoAuth);
            }
        }
        // 角色4需要检查是否为创建者且是所属用户
        else if (userRole == 4) {
            String creatorId = imgDoc.getCreatorId();
            List<String> belongUserIds = imgDoc.getBelongUserId();

            if (!currentUserId.equals(creatorId) ||
                    belongUserIds == null ||
                    !belongUserIds.contains(currentUserId)) {
                return new Result(ResultCode.R_NoAuth);
            }
        } else {
            return new Result(ResultCode.R_NoAuth);
        }
        // 删除附件
        try {
            imgRepository.deleteById(imgId);
            return new Result(ResultCode.R_Ok);
        } catch (Exception e) {
            logUtil.error("Error deleting img: " + e.getMessage());
            return new Result(ResultCode.R_DeleteFileError);
        }
    }

    @Override
    public Result filterImg(Map<String, Object> request) {
        // 参数校验
        if (request == null) {
            return new Result(ResultCode.R_ParamError);
        }

        String imgName = (String) request.get("img_name");
        String belongUserName = (String) request.get("belong_user_name");
        String creatorName = (String) request.get("creator_name");
        Integer status = request.get("status") != null ? (Integer) request.get("status") : 0;
        Integer pageNum = request.get("page_num") != null ? (Integer) request.get("page_num") : 1;
        Integer pageSize = request.get("page_size") != null ? (Integer) request.get("page_size") : 50;

        // 校验分页参数
        if (pageNum < 1 || pageSize < 1) {
            return new Result(ResultCode.R_PageError);
        }

        // 校验状态参数
        if (status != 0 && status != 1 && status != 2) {
            return new Result(ResultCode.R_ParamError);
        }
        // 如果belongUserName是公司名称，直接执行查询
        if (StringUtils.hasText(belongUserName) && UserConstData.COMPANY_USER_NAME.equals(belongUserName)) {
            // 创建主查询
            BoolQueryBuilder mainQuery = QueryBuilders.boolQuery();

            // 添加公司id条件
            mainQuery.must(QueryBuilders.termQuery("belongUserId", UserConstData.COMPANY_USER_ID));

            // 添加附件名称条件
            if (StringUtils.hasText(imgName)) {
                mainQuery.must(QueryBuilders.matchQuery("imgName", imgName));
            }

            // 添加创建者名称条件
            if (StringUtils.hasText(creatorName)) {
                List<UserDocument> creators = userRepository.findByUserNameLike(creatorName);
                if (!creators.isEmpty()) {
                    BoolQueryBuilder creatorQuery = QueryBuilders.boolQuery();
                    for (UserDocument creator : creators) {
                        creatorQuery.should(QueryBuilders.termQuery("creatorId", creator.getUserId()));
                    }
                    creatorQuery.minimumShouldMatch(1);
                    mainQuery.must(creatorQuery);
                }
            }

            // 添加状态条件
            if (status != null && status != 0) {
                mainQuery.must(QueryBuilders.termQuery("status", status));
            }

            // 执行查询
            Pageable pageable = PageRequest.of(pageNum - 1, pageSize);
            NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
                    .withQuery(mainQuery)
                    .withSort(SortBuilders.fieldSort("createdAt").order(SortOrder.DESC))
                    .withPageable(pageable)
                    .build();

            SearchHits<ImgDocument> searchHits = elasticsearchOperations.search(
                    searchQuery,
                    ImgDocument.class
            );

            List<ImgDocument> content = searchHits.stream()
                    .map(SearchHit::getContent)
                    .collect(Collectors.toList());

            return new Result(
                    ResultCode.R_Ok,
                    new PageResponse<>(
                            searchHits.getTotalHits(),
                            pageNum,
                            pageSize,
                            convertToResponseFormat(content)
                    )
            );
        }

        // 获取当前用户信息
        String currentUserId = ThreadLocalUtil.getUserId();
        Integer userRole = ThreadLocalUtil.getUserRole();
        if (currentUserId == null || userRole == null) {
            return new Result(ResultCode.R_UserNotFound);
        }

        // 角色2 - 大管理员
        if (userRole == 2) {
            // 创建主查询
            BoolQueryBuilder mainQuery = QueryBuilders.boolQuery();
            int validConditions = 0;

            // 添加附件名称条件
            if (StringUtils.hasText(imgName)) {
                mainQuery.should(QueryBuilders.matchQuery("imgName", imgName));
                validConditions++;
            }

            // 添加所属用户条件
            if (StringUtils.hasText(belongUserName)) {
                List<UserDocument> belongUsers = userRepository.findByUserNameLike(belongUserName);
                if (!belongUsers.isEmpty()) {
                    BoolQueryBuilder belongUserQuery = QueryBuilders.boolQuery();
                    for (UserDocument user : belongUsers) {
                        belongUserQuery.should(QueryBuilders.termQuery("belongUserId", user.getUserId()));
                    }
                    belongUserQuery.minimumShouldMatch(1);
                    mainQuery.should(belongUserQuery);
                    validConditions++;
                }
            }

            // 添加创建者条件
            if (StringUtils.hasText(creatorName)) {
                List<UserDocument> creators = userRepository.findByUserNameLike(creatorName);
                if (!creators.isEmpty()) {
                    BoolQueryBuilder creatorQuery = QueryBuilders.boolQuery();
                    for (UserDocument creator : creators) {
                        creatorQuery.should(QueryBuilders.termQuery("creatorId", creator.getUserId()));
                    }
                    creatorQuery.minimumShouldMatch(1);
                    mainQuery.should(creatorQuery);
                    validConditions++;
                }
            }

            // 添加状态条件
            if (status != null && status != 0) {
                mainQuery.must(QueryBuilders.termQuery("status", status));
                validConditions++;
            }

            // 如果没有任何有效条件，返回所有文档
            if (validConditions == 0) {
                Pageable pageable = PageRequest.of(pageNum - 1, pageSize);
                NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
                        .withQuery(QueryBuilders.matchAllQuery())
                        .withSort(SortBuilders.fieldSort("createdAt").order(SortOrder.DESC))
                        .withPageable(pageable)
                        .build();

                SearchHits<ImgDocument> searchHits = elasticsearchOperations.search(
                        searchQuery,
                        ImgDocument.class
                );

                List<ImgDocument> content = searchHits.stream()
                        .map(SearchHit::getContent)
                        .collect(Collectors.toList());

                return new Result(
                        ResultCode.R_Ok,
                        new PageResponse<>(
                                searchHits.getTotalHits(),
                                pageNum,
                                pageSize,
                                convertToResponseFormat(content)
                        )
                );
            }

            // 有查询条件时
            Pageable pageable = PageRequest.of(pageNum - 1, pageSize);
            NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
                    .withQuery(mainQuery)
                    .withSort(SortBuilders.fieldSort("createdAt").order(SortOrder.DESC))
                    .withPageable(pageable)
                    .build();

            SearchHits<ImgDocument> searchHits = elasticsearchOperations.search(
                    searchQuery,
                    ImgDocument.class
            );

            // 处理结果
            List<ImgDocument> content = searchHits.stream()
                    .map(SearchHit::getContent)
                    .collect(Collectors.toList());

            return new Result(
                    ResultCode.R_Ok,
                    new PageResponse<>(
                            searchHits.getTotalHits(),
                            pageNum,
                            pageSize,
                            convertToResponseFormat(content)
                    )
            );
        }
        // 角色3 - 小管理员
        else if (userRole == 3) {
            // 创建主查询
            BoolQueryBuilder mainQuery = QueryBuilders.boolQuery();

            // 1. 首先添加权限过滤（必须满足）
            BoolQueryBuilder accessQuery = QueryBuilders.boolQuery();

            // 创建者是自己或下属
            BoolQueryBuilder creatorAccessQuery = QueryBuilders.boolQuery()
                    .should(QueryBuilders.termQuery("creatorId", currentUserId));

            // 添加下属的创建者条件
            List<UserDocument> subordinates = userRepository.findByBelongUserId(currentUserId);
            if (!subordinates.isEmpty()) {
                for (UserDocument sub : subordinates) {
                    creatorAccessQuery.should(QueryBuilders.termQuery("creatorId", sub.getUserId()));
                }
            }
            accessQuery.should(creatorAccessQuery);

            // 所属用户是自己或下属
            BoolQueryBuilder belongAccessQuery = QueryBuilders.boolQuery()
                    .should(QueryBuilders.termQuery("belongUserId", currentUserId));

            if (!subordinates.isEmpty()) {
                for (UserDocument sub : subordinates) {
                    belongAccessQuery.should(QueryBuilders.termQuery("belongUserId", sub.getUserId()));
                }
            }
            accessQuery.should(belongAccessQuery);

            // 至少满足一个权限条件（创建者或所属用户）
            accessQuery.minimumShouldMatch(1);
            mainQuery.must(accessQuery);

            // 2. 添加搜索条件（如果有）
            // 添加图片名称条件
            if (StringUtils.hasText(imgName)) {
                mainQuery.must(QueryBuilders.matchQuery("imgName", imgName));
            }

            // 添加所属用户名称条件
            if (StringUtils.hasText(belongUserName)) {
                ValidationResult belongValidation = subordinateValidation.findSubordinatesAndSelfByName(
                        belongUserName,
                        currentUserId
                );
                if (!belongValidation.isValid()) {
                    return new Result(ResultCode.R_NotBelongToAdmin);
                }
                BoolQueryBuilder belongQuery = QueryBuilders.boolQuery();
                for (String id : belongValidation.getValidUserIds()) {
                    belongQuery.should(QueryBuilders.termQuery("belongUserId", id));
                }
                mainQuery.must(belongQuery);
            }

            // 添加创建者名称条件
            if (StringUtils.hasText(creatorName)) {
                ValidationResult creatorValidation = subordinateValidation.findSubordinatesAndSelfByName(
                        creatorName,
                        currentUserId
                );
                if (!creatorValidation.isValid()) {
                    return new Result(ResultCode.R_NotBelongToAdmin);
                }
                BoolQueryBuilder creatorQuery = QueryBuilders.boolQuery();
                for (String id : creatorValidation.getValidUserIds()) {
                    creatorQuery.should(QueryBuilders.termQuery("creatorId", id));
                }
                mainQuery.must(creatorQuery);
            }

            // 添加状态条件
            if (status != null && status != 0) {
                mainQuery.must(QueryBuilders.termQuery("status", status));
            }

            // 执行查询
            Pageable pageable = PageRequest.of(pageNum - 1, pageSize);
            NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
                    .withQuery(mainQuery)
                    .withSort(SortBuilders.fieldSort("createdAt").order(SortOrder.DESC))
                    .withPageable(pageable)
                    .build();

            SearchHits<ImgDocument> searchHits = elasticsearchOperations.search(
                    searchQuery,
                    ImgDocument.class
            );

            List<ImgDocument> content = searchHits.stream()
                    .map(SearchHit::getContent)
                    .collect(Collectors.toList());

            return new Result(
                    ResultCode.R_Ok,
                    new PageResponse<>(
                            searchHits.getTotalHits(),
                            pageNum,
                            pageSize,
                            convertToResponseFormat(content)
                    )
            );
        }
        // 角色4 - 普通用户
        else if (userRole == 4) {
            // 创建主查询
            BoolQueryBuilder mainQuery = QueryBuilders.boolQuery();
            String currentUserName = ThreadLocalUtil.getUserName();

            // 状态只能为0
            if (status == null || status != 0) {
                return new Result(ResultCode.R_ParamError);
            }

            // 验证创建者名称（如果有）
            if (StringUtils.hasText(creatorName)) {
                // 获取当前用户信息
                if (currentUserName == null || !currentUserName.equals(creatorName)) {
                    return new Result(ResultCode.R_CreatorError);
                }
                mainQuery.must(QueryBuilders.termQuery("creatorId", currentUserId));
            }

            // 添加附件名称条件
            if (StringUtils.hasText(imgName)) {
                mainQuery.must(QueryBuilders.matchQuery("imgName", imgName));
            }

            // 处理belongUserName条件
            if (StringUtils.hasText(belongUserName)) {
                if ("公司".equals(belongUserName)) {
                    // 如果是"公司"，不添加belongUserId过滤
                } else {
                    // 验证belongUserName是否是自己
                    if (!belongUserName.equals(currentUserName)) {
                        return new Result(ResultCode.R_BelongUserError);
                    }
                    // 是自己，添加belongUserId条件
                    mainQuery.must(QueryBuilders.termQuery("belongUserId", currentUserId));
                }
            }

            // 无论有没有其他条件，都必须确保所属用户包含自己
            BoolQueryBuilder belongQuery = QueryBuilders.boolQuery()
                    .must(QueryBuilders.termQuery("belongUserId", currentUserId));
            mainQuery.must(belongQuery);

            // 执行查询
            Pageable pageable = PageRequest.of(pageNum - 1, pageSize);
            NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
                    .withQuery(mainQuery)
                    .withSort(SortBuilders.fieldSort("createdAt").order(SortOrder.DESC))
                    .withPageable(pageable)
                    .build();

            SearchHits<ImgDocument> searchHits = elasticsearchOperations.search(
                    searchQuery,
                    ImgDocument.class
            );

            // 处理结果，只返回指定字段
            List<Map<String, Object>> content = searchHits.stream()
                    .map(hit -> {
                        ImgDocument doc = hit.getContent();
                        Map<String, Object> item = new HashMap<>();
                        item.put("id", doc.getImgId());
                        item.put("name", doc.getImgName());

                        // 查询创建者名称
                        String responseCreatorName = userRepository.findByUserId(doc.getCreatorId())
                                .map(UserDocument::getUserName)
                                .orElse("");
                        item.put("creator_name", responseCreatorName);

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

        return new Result(ResultCode.R_Fail);
    }


    // 处理查询结果，转换为所需格式
    private List<Map<String, Object>> convertToResponseFormat(List<ImgDocument> documents) {
        return documents.stream().map(doc -> {
            Map<String, Object> item = new HashMap<>();
            item.put("id", doc.getImgId());
            item.put("name", doc.getImgName());

            // 查询创建者名称
            String creatorName = userRepository.findByUserId(doc.getCreatorId())
                    .map(UserDocument::getUserName)
                    .orElse("");
            item.put("creator_name", creatorName);

            // 查询所属用户名称列表
            List<String> belongUserNames = new ArrayList<>();
            if (doc.getBelongUserId() != null && !doc.getBelongUserId().isEmpty()) {
                belongUserNames = doc.getBelongUserId().stream()
                        .map(userId -> userRepository.findByUserId(userId)
                                .map(UserDocument::getUserName)
                                .orElse(""))
                        .filter(name -> !name.isEmpty())
                        .collect(Collectors.toList());
            }
            item.put("belong_user_name", belongUserNames);

            item.put("status", doc.getStatus());
            return item;
        }).collect(Collectors.toList());
    }
} 