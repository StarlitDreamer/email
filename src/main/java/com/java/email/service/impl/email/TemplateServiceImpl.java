package com.java.email.service.impl.email;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.java.email.service.email.TemplateService;
import com.java.email.utils.LogUtil;
import com.java.email.common.Response.PageResponse;
import com.java.email.common.Response.Result;
import com.java.email.common.Response.ResultCode;
import com.java.email.common.userCommon.SubordinateValidation;
import com.java.email.common.userCommon.SubordinateValidation.ValidationResult;
import com.java.email.common.userCommon.ThreadLocalUtil;
import com.java.email.constant.MagicMathConstData;
import com.java.email.constant.UserConstData;
import com.java.email.model.entity.dictionary.EmailTypeDocument;
import com.java.email.model.entity.file.AttachmentAssignDocument;
import com.java.email.model.entity.file.AttachmentDocument;
import com.java.email.model.entity.template.TemplateAssignDocument;
import com.java.email.model.entity.template.TemplateDocument;
import com.java.email.model.entity.user.UserDocument;
import com.java.email.esdao.repository.template.TemplateRepository;
import com.java.email.esdao.repository.user.UserRepository;
import com.java.email.esdao.repository.dictionary.EmailTypeRepository;
import com.java.email.esdao.repository.template.TemplateAssignRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.springframework.util.StringUtils;

import java.util.stream.Collectors;

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;

@Service
public class TemplateServiceImpl implements TemplateService {
    @Autowired
    private TemplateRepository templateRepository;

    @Autowired
    private TemplateAssignRepository templateAssignRepository;

    private static final LogUtil logUtil = LogUtil.getLogger(TemplateServiceImpl.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailTypeRepository emailTypeRepository;

    @Autowired
    private ElasticsearchOperations elasticsearchOperations;

    @Autowired
    private SubordinateValidation subordinateValidation;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result saveTemplate(Map<String, Object> request) {
        // 参数校验
        if (request == null) {
            return new Result(ResultCode.R_ParamError);
        }

        String templateId = (String) request.get("template_id");
        String templateName = (String) request.get("template_name");
        String templateTypeId = (String) request.get("template_type_id");
        String templateContent = (String) request.get("template_content");

        // 验证模板类型id
        EmailTypeDocument emailTypeDoc = emailTypeRepository.findById(templateTypeId).orElse(null);
        if (emailTypeDoc == null) {
            return new Result(ResultCode.R_ParamError);
        }

        // 校验必填字段
        if (!StringUtils.hasText(templateName) ||
                !StringUtils.hasText(templateTypeId) ||
                !StringUtils.hasText(templateContent)) {
            return new Result(ResultCode.R_ParamError);
        }

        // 获取当前时间
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
        String currentTime = LocalDateTime.now().format(formatter);

        // 获取当前用户ID
        String userId = ThreadLocalUtil.getUserId();
        if (userId == null) {
            return new Result(ResultCode.R_UserNotFound);
        }
        String userName = ThreadLocalUtil.getUserName();
        if (userName == null) {
            return new Result(ResultCode.R_UserNotFound);
        }
        Integer userRole = ThreadLocalUtil.getUserRole();
        if (userRole == null) {
            return new Result(ResultCode.R_UserNotFound);
        }
        // 创建模板文档对象
        TemplateDocument templateDoc = new TemplateDocument();
        templateDoc.setTemplateName(templateName);
        templateDoc.setTemplateTypeId(templateTypeId);
        templateDoc.setTemplateContent(templateContent);

        try {
            if (!StringUtils.hasText(templateId)) {
                // 新建记录
                List<String> belongUserId = new ArrayList<>();
                belongUserId.add(userId);
                templateDoc.setBelongUserId(belongUserId);
                templateDoc.setTemplateId(java.util.UUID.randomUUID().toString());
                templateDoc.setCreatorId(userId);
                templateDoc.setCreatorName(userName);
                if (userRole == 4) {
                    templateDoc.setStatus(MagicMathConstData.TEMPLATE_STATUS_ASSIGNED);
                    // 创建分配记录
                    Map<String, Object> process = new HashMap<>();
                    // 分配人
                    process.put("assignor_id", userId);
                    process.put("assignor_name", userName);
                    // 被分配人
                    List<Map<String, String>> assigneeList = new ArrayList<>();
                    Map<String, String> assignee = new HashMap<>();
                    assignee.put("assignee_id", userId);
                    assignee.put("assignee_name", userName);
                    assigneeList.add(assignee);
                    process.put("assignee", assigneeList);
                    // 分配时间
                    process.put("assign_date", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")));

                    List<Map<String, Object>> processList = new ArrayList<>();
                    processList.add(process);
                    TemplateAssignDocument assignDoc = new TemplateAssignDocument();
                    assignDoc.setTemplateId(templateId);
                    assignDoc.setAssignProcess(processList);
                    templateAssignRepository.save(assignDoc);
                } else {
                    templateDoc.setStatus(MagicMathConstData.TEMPLATE_STATUS_UNASSIGNED);
                }
                templateDoc.setCreatedAt(currentTime);
                templateDoc.setUpdatedAt(currentTime);
                templateRepository.save(templateDoc);
                return new Result(ResultCode.R_Ok);
            } else {
                // 更新记录
                TemplateDocument existingDoc = templateRepository.findById(templateId).orElse(null);
                if (existingDoc == null) {
                    return new Result(ResultCode.R_TemplateNotFound);
                }
                if (userRole == 3) {
                    // 验证模板当前所属用户是否包含自己或下属
                    Set<String> subordinateIds = subordinateValidation.getAllSubordinateIds(userId);
                    if (subordinateIds == null) {
                        logUtil.error("获取下属列表失败");
                        return new Result(ResultCode.R_Error);
                    }
                    if(!subordinateIds.contains(existingDoc.getBelongUserId()) && !existingDoc.getBelongUserId().contains(userId)){
                        logUtil.error("无权修改非本人或下属的模板");
                        return new Result(ResultCode.R_NoAuth);
                    }
                }
                if (userRole == 4) {
                    // 检查当前用户是否在模板的所属用户列表中且是创建人
                    List<String> belongUserIds = existingDoc.getBelongUserId();
                    String creatorId = existingDoc.getCreatorId();
                    if (belongUserIds == null || !belongUserIds.contains(userId) || !userId.equals(creatorId)) {
                        return new Result(ResultCode.R_NoAuth);
                    }
                }
                existingDoc.setTemplateName(templateName);
                existingDoc.setTemplateTypeId(templateTypeId);
                existingDoc.setTemplateContent(templateContent);
                existingDoc.setUpdatedAt(currentTime);
                templateRepository.save(existingDoc);
                return new Result(ResultCode.R_Ok);
            }
        } catch (Exception e) {
            logUtil.error("Error saving template: " + e.getMessage());
            return new Result(ResultCode.R_Error);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result assignTemplate(Map<String, Object> request) {
        try {
            // 获取当前用户角色
            Integer userRole = ThreadLocalUtil.getUserRole();
            if (userRole == null || (userRole != 2 && userRole != 3)) {
                return new Result(ResultCode.R_NoAuth);
            }
            String currentUserName = ThreadLocalUtil.getUserName();
            if (currentUserName == null) {
                return new Result(ResultCode.R_UserNotFound);
            }
            // 检查参数
            if (!request.containsKey("template_id") || !request.containsKey("belong_user_id")) {
                return new Result(ResultCode.R_ParamError);
            }

            String templateId = (String) request.get("template_id");
            @SuppressWarnings("unchecked")
            List<String> belongUserIds = (List<String>) request.get("belong_user_id");

            if (templateId == null || belongUserIds == null || belongUserIds.isEmpty()) {
                return new Result(ResultCode.R_ParamError);
            }

            // 检查模板是否存在
            TemplateDocument templateDoc = templateRepository.findById(templateId).orElse(null);
            if (templateDoc == null) {
                return new Result(ResultCode.R_TemplateNotFound);
            }

            // 获取当前用户ID
            String currentUserId = ThreadLocalUtil.getUserId();
            if (currentUserId == null) {
                return new Result(ResultCode.R_UserNotFound);
            }
            // 验证所有用户
            Map<String, UserDocument> userDocs = new HashMap<>();
            for (String userId : belongUserIds) {
                UserDocument userDoc = userRepository.findById(userId).orElse(null);
                if (userDoc == null) {
                    return new Result(ResultCode.R_BelongUserNotFound);
                }
                userDocs.put(userId, userDoc);
            }
            // 如果是小管理员(role=3)，检查用户是否属于自己管理
            if (userRole == 3) {
                // 验证模板当前所属用户是否包含自己或下属
                Set<String> subordinateIds = subordinateValidation.getAllSubordinateIds(currentUserId);
                if (subordinateIds == null) {
                    logUtil.error("获取下属列表失败");
                    return new Result(ResultCode.R_Error);
                }
                List<String> templateBelongUserIds = templateDoc.getBelongUserId();
                if (templateBelongUserIds == null || templateBelongUserIds.isEmpty()) {
                    logUtil.error("模板所属用户ID列表为空");
                    return new Result(ResultCode.R_Error);
                }

                // 检查是否至少有一个所属用户ID在下属列表中或者是指定用户
                boolean hasValidUser = templateBelongUserIds.stream()
                        .anyMatch(id -> subordinateIds.contains(id) || id.equals(currentUserId));

                if (!hasValidUser) {
                    logUtil.error("无权分配非本人或下属的模板");
                    return new Result(ResultCode.R_NoAuth);
                }
                if (!belongUserIds.contains(UserConstData.COMPANY_USER_ID)) {
                    for (UserDocument userDoc : userDocs.values()) {
                        // 检查用户是否属于自己管理或者是自己
                        if (!userDoc.getBelongUserId().equals(currentUserId) && !userDoc.getUserId().equals(currentUserId)) {
                            return new Result(ResultCode.R_NotBelongToAdmin);
                        }
                    }
                }
            }
            // 更新模板的归属用户
            templateDoc.setBelongUserId(belongUserIds);
            templateDoc.setStatus(MagicMathConstData.TEMPLATE_STATUS_ASSIGNED);
            templateDoc.setUpdatedAt(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")));
            templateRepository.save(templateDoc);

            // 创建分配记录
            Map<String, Object> process = new HashMap<>();
            process.put("assignor_id", currentUserId);
            process.put("assignor_name", currentUserName);

            List<Map<String, String>> assigneeList = new ArrayList<>();
            for (UserDocument userDoc : userDocs.values()) {
                Map<String, String> assignee = new HashMap<>();
                assignee.put("assignee_id", userDoc.getUserId());
                assignee.put("assignee_name", userDoc.getUserName());
                assigneeList.add(assignee);
            }
            process.put("assignee", assigneeList);
            process.put("assign_date", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")));

            // 查找现有的分配记录进行追加，没有则创建新的。
            TemplateAssignDocument existingAssignDoc = templateAssignRepository.findById(templateId).orElse(null);
            List<Map<String, Object>> processList;
            if (existingAssignDoc != null) {
                processList = existingAssignDoc.getAssignProcess();
                processList.add(0, process);
                existingAssignDoc.setAssignProcess(processList);
                templateAssignRepository.save(existingAssignDoc);
            } else {
                processList = new ArrayList<>();
                processList.add(process);
                TemplateAssignDocument assignDoc = new TemplateAssignDocument();
                assignDoc.setTemplateId(templateId);
                assignDoc.setAssignProcess(processList);
                templateAssignRepository.save(assignDoc);
            }
            return new Result(ResultCode.R_Ok);
        } catch (Exception e) {
            logUtil.error("Error assigning template: " + e.getMessage());
            return new Result(ResultCode.R_Error);
        }
    }

    @Override
    public Result assignTemplateDetails(Map<String, Object> request) {
        // 参数校验
        if (!request.containsKey("template_id") || !request.containsKey("page_num") || !request.containsKey("page_size")) {
            return new Result(ResultCode.R_ParamError);
        }

        Integer pageNum = (Integer) request.get("page_num");
        Integer pageSize = (Integer) request.get("page_size");
        if (pageNum <= 0 || pageSize <= 0) {
            return new Result(ResultCode.R_PageError);
        }


        try {
            String templateId = (String) request.get("template_id");
            // 检查附件是否存在
            TemplateDocument template = templateRepository.findById(templateId).orElse(null);
            if (template == null) {
                logUtil.error("模板不存在: " + templateId);
                return new Result(ResultCode.R_TemplateNotFound);
            }
            // 获取分配历史
            TemplateAssignDocument assignDocument = templateAssignRepository.findById(templateId).orElse(null);
            if (assignDocument == null || assignDocument.getAssignProcess() == null) {
                return new Result(ResultCode.R_Ok, new PageResponse<>(0L, pageNum, pageSize, new ArrayList<>()));
            }

            // 计算分页
            int total = assignDocument.getAssignProcess().size();
            int start = (pageNum - 1) * pageSize;
            int end = Math.min(start + pageSize, total);

            // 验证分页参数
            if (start >= total) {
                return new Result(ResultCode.R_Ok, new PageResponse<>(0L, pageNum, pageSize, new ArrayList<>()));
            }

            // 获取当前页的数据
            List<Map<String, Object>> pageData = assignDocument.getAssignProcess().subList(start, end);
            return new Result(ResultCode.R_Ok, new PageResponse<>(total, pageNum, pageSize, pageData));

        } catch (Exception e) {
            logUtil.error("Error getting template assign details: " + e.getMessage());
            return new Result(ResultCode.R_Error);
        }
    }

    @Override
    public Result filterTemplate(Map<String, Object> request) {
        try {
            // 获取当前用户角色和ID
            Integer userRole = ThreadLocalUtil.getUserRole();
            String currentUserId = ThreadLocalUtil.getUserId();
            if (userRole == null || currentUserId == null) {
                return new Result(ResultCode.R_UserNotFound);
            }

            // 参数校验
            if (!request.containsKey("page_num") || !request.containsKey("page_size")) {
                return new Result(ResultCode.R_ParamError);
            }
            Integer pageNum = (Integer) request.get("page_num");
            Integer pageSize = (Integer) request.get("page_size");
            if (pageNum <= 0 || pageSize <= 0) {
                return new Result(ResultCode.R_PageError);
            }

            String templateName = (String) request.get("template_name");
            String creatorName = (String) request.get("creator_name");
            String belongUserName = (String) request.get("belong_user_name");
            String templateTypeId = (String) request.get("template_type_id");
            Integer status = (Integer) request.get("status");


            // 如果belongUserName是"公司"，直接执行查询
            if (StringUtils.hasText(belongUserName) && "公司".equals(belongUserName)) {
                BoolQuery.Builder mainQuery = new BoolQuery.Builder();
                mainQuery.must(m -> m
                        .term(t -> t
                                .field("belong_user_id")
                                .value(UserConstData.COMPANY_USER_ID)
                        )
                );
                // 添加模板名称条件
                if (StringUtils.hasText(templateName)) {
                    mainQuery.must(m -> m
                            .match(t -> t
                                    .field("template_name")
                                    .query(templateName)
                            )
                    );
                }

                // 添加模板类型条件
                if (StringUtils.hasText(templateTypeId)) {
                    mainQuery.must(m -> m
                            .term(t -> t
                                    .field("template_type_id")
                                    .value(templateTypeId)
                            )
                    );
                }

                // 添加创建者名称条件
                if (StringUtils.hasText(creatorName)) {
                    List<UserDocument> creators = userRepository.findByUserNameLike(creatorName);
                    if (!creators.isEmpty()) {
                        BoolQuery.Builder creatorQuery = new BoolQuery.Builder();
                        for (UserDocument creator : creators) {
                            creatorQuery.should(s -> s
                                    .term(t -> t
                                            .field("creator_id")
                                            .value(creator.getUserId())
                                    )
                            );
                        }
                        creatorQuery.minimumShouldMatch("1");
                        mainQuery.must(m -> m.bool(creatorQuery.build()));
                    }
                }

                // 添加状态条件
                if (status != null && status != 0) {
                    mainQuery.must(m -> m
                            .term(t -> t
                                    .field("status")
                                    .value(status)
                            )
                    );
                }
                // 执行查询
                Pageable pageable = PageRequest.of(pageNum - 1, pageSize);
                NativeQuery searchQuery = NativeQuery.builder()
                        .withQuery(q -> q.bool(mainQuery.build()))
                        .withSort(Sort.by(Sort.Direction.DESC, "updated_at"))
                        .withPageable(pageable)
                        .build();

                SearchHits<TemplateDocument> searchHits = elasticsearchOperations.search(
                        searchQuery,
                        TemplateDocument.class
                );

                List<TemplateDocument> content = searchHits.stream()
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

            // 角色2 - 大管理员
            if (userRole == 2) {
                // 构建查询
                BoolQuery.Builder mainQuery = new BoolQuery.Builder();
                boolean hasValidConditions = false;

                // 检查是否有任何查询条件
                if (!StringUtils.hasText(templateName) &&
                        !StringUtils.hasText(creatorName) &&
                        !StringUtils.hasText(belongUserName) &&
                        !StringUtils.hasText(templateTypeId) &&
                        (status == null || status == 0)) {
                    // 没有查询条件时，直接查询所有数据
                    mainQuery.must(m -> m
                            .matchAll(ma -> ma)
                    );
                } else {
                    // 添加模板名称条件
                    if (StringUtils.hasText(templateName)) {
                        mainQuery.must(m -> m
                                .match(t -> t
                                        .field("template_name")
                                        .query(templateName)
                                )
                        );
                        hasValidConditions = true;
                    }

                    // 添加创建者名称条件
                    if (StringUtils.hasText(creatorName)) {
                        List<UserDocument> creators = userRepository.findByUserNameLike(creatorName);
                        if (!creators.isEmpty()) {
                            BoolQuery.Builder creatorQuery = new BoolQuery.Builder();
                            for (UserDocument creator : creators) {
                                creatorQuery.should(s -> s
                                        .term(t -> t
                                                .field("creator_id")
                                                .value(creator.getUserId())
                                        )
                                );
                            }
                            creatorQuery.minimumShouldMatch("1");
                            mainQuery.must(m -> m.bool(creatorQuery.build()));
                            hasValidConditions = true;
                        }
                    }

                    // 添加所属用户名称条件
                    if (StringUtils.hasText(belongUserName)) {
                        List<UserDocument> belongUsers = userRepository.findByUserNameLike(belongUserName);
                        if (!belongUsers.isEmpty()) {
                            BoolQuery.Builder belongUserQuery = new BoolQuery.Builder();
                            for (UserDocument user : belongUsers) {
                                belongUserQuery.should(s -> s
                                        .term(t -> t
                                                .field("belong_user_id")
                                                .value(user.getUserId())
                                        )
                                );
                            }
                            belongUserQuery.minimumShouldMatch("1");
                            mainQuery.must(m -> m.bool(belongUserQuery.build()));
                            hasValidConditions = true;
                        }
                    }

                    // 添加模板类型条件
                    if (StringUtils.hasText(templateTypeId)) {
                        mainQuery.must(m -> m
                                .term(t -> t
                                        .field("template_type_id")
                                        .value(templateTypeId)
                                )
                        );
                        hasValidConditions = true;
                    }

                    // 添加状态条件
                    if (status != null && status != 0) {
                        mainQuery.must(m -> m
                                .term(t -> t
                                        .field("status")
                                        .value(status)
                                )
                        );
                        hasValidConditions = true;
                    }

                    // 如果所有的查询条件都无效，返回空结果
                    if (!hasValidConditions) {
                        return new Result(
                                ResultCode.R_Ok,
                                new PageResponse<>(0L, pageNum, pageSize, new ArrayList<>())
                        );
                    }
                }
                // 执行查询
                Pageable pageable = PageRequest.of(pageNum - 1, pageSize);
                NativeQuery searchQuery = NativeQuery.builder()
                        .withQuery(q -> q.bool(mainQuery.build()))
                        .withSort(Sort.by(Sort.Direction.DESC, "updated_at"))
                        .withPageable(pageable)
                        .build();

                SearchHits<TemplateDocument> searchHits = elasticsearchOperations.search(
                        searchQuery,
                        TemplateDocument.class
                );

                List<TemplateDocument> content = searchHits.stream()
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
            if (userRole == 3) {
                BoolQuery.Builder mainQuery = new BoolQuery.Builder();
                boolean hasValidConditions = false;  // 标记是否有有效的查询条件
                // 检查是否有任何查询条件
                if (!StringUtils.hasText(templateName) &&
                        !StringUtils.hasText(creatorName) &&
                        !StringUtils.hasText(belongUserName) &&
                        !StringUtils.hasText(templateTypeId) &&
                        (status == null || status == 0)) {
                    // 没有查询条件时，直接查询所有有权限的数据
                } else {
                    // 有查询条件时的处理  
                    if (StringUtils.hasText(templateName)) {
                        mainQuery.must(m -> m
                                .match(t -> t
                                        .field("template_name")
                                        .query(templateName)
                                )
                        );
                        hasValidConditions = true;
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
                        BoolQuery.Builder creatorQuery = new BoolQuery.Builder();
                        for (String id : creatorValidation.getValidUserIds()) {
                            creatorQuery.should(s -> s
                                    .term(t -> t
                                            .field("creator_id")
                                            .value(id)
                                    )
                            );
                        }
                        mainQuery.must(m -> m.bool(creatorQuery.build()));
                        hasValidConditions = true;
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
                        BoolQuery.Builder belongQuery = new BoolQuery.Builder();
                        for (String id : belongValidation.getValidUserIds()) {
                            belongQuery.should(s -> s
                                    .term(t -> t
                                            .field("belong_user_id")
                                            .value(id)
                                    )
                            );
                        }
                        mainQuery.must(m -> m.bool(belongQuery.build()));
                        hasValidConditions = true;
                    }

                    // 添加模板类型条件
                    if (StringUtils.hasText(templateTypeId)) {
                        mainQuery.must(m -> m
                                .term(t -> t
                                        .field("template_type_id")
                                        .value(templateTypeId)
                                )
                        );
                        hasValidConditions = true;
                    }

                    // 添加状态条件
                    if (status != null && status != 0) {
                        mainQuery.must(m -> m
                                .term(t -> t
                                        .field("status")
                                        .value(status)
                                )
                        );
                        hasValidConditions = true;
                    }

                    // 如果所有的查询条件都无效，返回空结果
                    if (!hasValidConditions) {
                        return new Result(
                                ResultCode.R_Ok,
                                new PageResponse<>(0L, pageNum, pageSize, new ArrayList<>())
                        );
                    }
                }
                // 添加权限过滤
                BoolQuery.Builder accessQuery = new BoolQuery.Builder();
                // 创建者是自己或下属
                BoolQuery.Builder creatorAccessQuery = new BoolQuery.Builder();
                creatorAccessQuery.should(s -> s
                        .term(t -> t
                                .field("creator_id")
                                .value(currentUserId)
                        )
                );
                // 添加下属的创建者条件
                List<UserDocument> subordinates = userRepository.findByBelongUserId(currentUserId);
                if (!subordinates.isEmpty()) {
                    for (UserDocument sub : subordinates) {
                        creatorAccessQuery.should(s -> s
                                .term(t -> t
                                        .field("creator_id")
                                        .value(sub.getUserId())
                                )
                        );
                    }
                }
                accessQuery.should(s -> s.bool(creatorAccessQuery.build()));
                // 所属用户是自己或下属
                BoolQuery.Builder belongAccessQuery = new BoolQuery.Builder();
                belongAccessQuery.should(s -> s
                        .term(t -> t
                                .field("belong_user_id")
                                .value(currentUserId)
                        )
                );

                if (!subordinates.isEmpty()) {
                    for (UserDocument sub : subordinates) {
                        belongAccessQuery.should(s -> s
                                .term(t -> t
                                        .field("belong_user_id")
                                        .value(sub.getUserId())
                                )
                        );
                    }
                }
                accessQuery.should(s -> s.bool(belongAccessQuery.build()));
                // 至少满足一个权限条件
                accessQuery.minimumShouldMatch("1");
                mainQuery.must(m -> m.bool(accessQuery.build()));
                // 执行查询
                Pageable pageable = PageRequest.of(pageNum - 1, pageSize);
                NativeQuery searchQuery = NativeQuery.builder()
                        .withQuery(q -> q.bool(mainQuery.build()))
                        .withSort(Sort.by(Sort.Direction.DESC, "updated_at"))
                        .withPageable(pageable)
                        .build();

                SearchHits<TemplateDocument> searchHits = elasticsearchOperations.search(
                        searchQuery,
                        TemplateDocument.class
                );

                List<TemplateDocument> content = searchHits.stream()
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
            if (userRole == 4) {
                // 普通用户不允许查询创建者
                if (StringUtils.hasText(creatorName)) {
                    return new Result(ResultCode.R_ParamError);
                }
                // 状态只能为0
                if (status != 0) {
                    return new Result(ResultCode.R_ParamError);
                }
                BoolQuery.Builder mainQuery = new BoolQuery.Builder();
                String currentUserName = ThreadLocalUtil.getUserName();
                if (currentUserName == null) {
                    return new Result(ResultCode.R_UserNotFound);
                }
                // 添加模板名称条件
                if (StringUtils.hasText(templateName)) {
                    mainQuery.must(m -> m
                            .match(t -> t
                                    .field("template_name")
                                    .query(templateName)
                            )
                    );
                }

                // 处理belongUserName条件
                if (StringUtils.hasText(belongUserName)) {
                    // 验证belongUserName是否是自己
                    if (!belongUserName.equals(currentUserName)) {
                        return new Result(ResultCode.R_BelongUserError);
                    }
                    // 是自己，添加belongUserId条件
                    mainQuery.must(m -> m
                            .term(t -> t
                                    .field("belong_user_id")
                                    .value(currentUserId)
                            )
                    );
                }

                // 添加模板类型条件
                if (StringUtils.hasText(templateTypeId)) {
                    mainQuery.must(m -> m
                            .term(t -> t
                                    .field("template_type_id")
                                    .value(templateTypeId)
                            )
                    );
                }

                // 无论有没有其他条件，都必须确保所属用户包含自己
                mainQuery.must(m -> m
                        .term(t -> t
                                .field("belong_user_id")
                                .value(currentUserId)
                        )
                );

                // 执行查询
                Pageable pageable = PageRequest.of(pageNum - 1, pageSize);
                NativeQuery searchQuery = NativeQuery.builder()
                        .withQuery(q -> q.bool(mainQuery.build()))
                        .withSort(Sort.by(Sort.Direction.DESC, "updated_at"))
                        .withPageable(pageable)
                        .build();

                SearchHits<TemplateDocument> searchHits = elasticsearchOperations.search(
                        searchQuery,
                        TemplateDocument.class
                );
                // 处理结果，只返回指定字段
                List<Map<String, Object>> content = searchHits.stream()
                        .map(hit -> {
                            TemplateDocument doc = hit.getContent();
                            Map<String, Object> item = new HashMap<>();
                            item.put("id", doc.getTemplateId());
                            item.put("name", doc.getTemplateName());
                            item.put("creator_name", doc.getCreatorName());
                            // 通过templateTypeId查询templateTypeName
                            EmailTypeDocument emailTypeDoc = emailTypeRepository.findById(doc.getTemplateTypeId()).orElse(null);
                            if (emailTypeDoc == null) {
                                item.put("template_type_name", "无");
                                item.put("template_type_id", "");
                            } else {
                                item.put("template_type_id", emailTypeDoc.getEmailTypeId());
                                item.put("template_type_name", emailTypeDoc.getEmailTypeName());
                            }
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
            return new Result(ResultCode.R_Error);
        } catch (Exception e) {
            logUtil.error("Error filtering templates: " + e);
            return new Result(ResultCode.R_Error);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result deleteTemplate(Map<String, Object> request) {
        // 参数校验
        if (request == null) {
            return new Result(ResultCode.R_ParamError);
        }
        if (!request.containsKey("template_id")) {
            return new Result(ResultCode.R_ParamError);
        }
        String templateId = (String) request.get("template_id");
        TemplateDocument templateDoc = templateRepository.findById(templateId).orElse(null);
        if (templateDoc == null) {
            return new Result(ResultCode.R_TemplateNotFound);
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
        // 角色3需要检查创建者是否是自己或下属
        else if (userRole == 3) {
            if(!subordinateValidation.isSubordinateOrSelf(templateDoc.getCreatorId(), currentUserId)){
                return new Result(ResultCode.R_NoAuth);
            }
        }
        // 角色4需要检查是否为创建者且是所属用户
        else if (userRole == 4) {
            String creatorId = templateDoc.getCreatorId();
            List<String> belongUserIds = templateDoc.getBelongUserId();

            if (!currentUserId.equals(creatorId) ||
                    belongUserIds == null ||
                    !belongUserIds.contains(currentUserId)) {
                return new Result(ResultCode.R_NoAuth);
            }
        } else {
            return new Result(ResultCode.R_NoAuth);
        }

        // 删除模板
        try {
            templateRepository.deleteById(templateId);
            templateAssignRepository.deleteById(templateId);
            return new Result(ResultCode.R_Ok);
        } catch (Exception e) {
            logUtil.error("Error deleting template: " + e.getMessage());
            return new Result(ResultCode.R_Error);
        }
    }


    // 处理查询结果，转换为所需格式
    private List<Map<String, Object>> convertToResponseFormat(List<TemplateDocument> documents) {
        return documents.stream().map(doc -> {
            Map<String, Object> item = new HashMap<>();
            item.put("id", doc.getTemplateId());
            item.put("name", doc.getTemplateName());
            item.put("creator_name", doc.getCreatorName());
            // 获取归属用户名称列表
            List<String> belongUserNames = new ArrayList<>();
            if (doc.getBelongUserId() != null && !doc.getBelongUserId().isEmpty()) {
                belongUserNames = doc.getBelongUserId().stream()
                        .map(userId -> userRepository.findById(userId)
                                .map(UserDocument::getUserName)
                                .orElse(""))
                        .filter(name -> !name.isEmpty())
                        .collect(Collectors.toList());
            }
            item.put("belong_user_name", belongUserNames);
            // 通过templateTypeId查询templateTypeName
            EmailTypeDocument emailTypeDoc = emailTypeRepository.findById(doc.getTemplateTypeId()).orElse(null);
            if (emailTypeDoc == null) {
                item.put("template_type_name", "无");
                item.put("template_type_id", "");
            } else {
                item.put("template_type_id", emailTypeDoc.getEmailTypeId());
                item.put("template_type_name", emailTypeDoc.getEmailTypeName());
            }
            item.put("status", doc.getStatus());
            return item;
        }).collect(Collectors.toList());
    }

    @Override
    public Result checkTemplate(Map<String, Object> request) {
        if (request == null) {
            return new Result(ResultCode.R_ParamError);
        }
        if (!request.containsKey("template_id")) {
            return new Result(ResultCode.R_ParamError);
        }
        String templateId = (String) request.get("template_id");
        TemplateDocument templateDoc = templateRepository.findById(templateId).orElse(null);
        if (templateDoc == null) {
            return new Result(ResultCode.R_TemplateNotFound);
        }
        String templateContent = templateDoc.getTemplateContent();
        return new Result(ResultCode.R_Ok, templateContent);
    }

} 