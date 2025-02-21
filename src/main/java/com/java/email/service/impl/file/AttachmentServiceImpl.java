package com.java.email.service.impl.file;

import com.java.email.common.Response.PageResponse;
import com.java.email.common.Response.Result;
import com.java.email.common.Response.ResultCode;
import com.java.email.constant.MagicMathConstData;
import com.java.email.constant.UserConstData;
import com.java.email.model.entity.file.AttachmentAssignDocument;
import com.java.email.model.entity.file.AttachmentDocument;
import com.java.email.model.entity.receiver.SupplierAssignDocument;
import com.java.email.model.entity.receiver.SupplierDocument;
import com.java.email.model.entity.user.UserDocument;
import com.java.email.esdao.repository.file.AttachmentAssignRepository;
import com.java.email.esdao.repository.file.AttachmentRepository;
import com.java.email.esdao.repository.user.UserRepository;
import com.java.email.service.file.AttachmentService;
import com.java.email.utils.LogUtil;
import com.java.email.common.userCommon.ThreadLocalUtil;
import com.java.email.common.userCommon.SubordinateValidation;
import com.java.email.common.userCommon.SubordinateValidation.ValidationResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.apache.commons.net.ftp.FTPClient;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.ChannelSftp;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.regions.Regions;

@Service
public class AttachmentServiceImpl implements AttachmentService {

    @Autowired
    private AttachmentRepository attachmentRepository;

    @Autowired
    private AttachmentAssignRepository attachmentAssignRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ElasticsearchOperations elasticsearchOperations;

    @Autowired
    private SubordinateValidation subordinateValidation;

    @Autowired
    private RestTemplate restTemplate;

    private static final LogUtil logUtil = LogUtil.getLogger(AttachmentServiceImpl.class);


    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result uploadAttachment(Map<String, List<Map<String, String>>> request) {
        try {
            // 角色验证
            String userId = ThreadLocalUtil.getUserId();
            if (userId == null) {
                return new Result(ResultCode.R_Error);
            }
            Integer userRole = ThreadLocalUtil.getUserRole();
            if (userRole == null) {
                return new Result(ResultCode.R_Error);
            }
            // 参数校验
            if (request == null || !request.containsKey("attachment")) {
                return new Result(ResultCode.R_ParamError);
            }
            List<Map<String, String>> attachments = request.get("attachment");
            if (attachments == null || attachments.isEmpty()) {
                return new Result(ResultCode.R_ParamError);
            }

            // 获取当前用户ID
            List<String> belongUserIds = new ArrayList<>();
            belongUserIds.add(userId);

            // 插入附件
            List<AttachmentDocument> attachmentDocuments = new ArrayList<>();
            for (Map<String, String> attachment : attachments) {
                AttachmentDocument doc = new AttachmentDocument();
                doc.setAttachmentId(attachment.get("attachment_id"));
                doc.setAttachmentUrl(attachment.get("attachment_url"));
                doc.setAttachmentSize(Long.parseLong(attachment.get("attachment_size")));
                doc.setAttachmentName(attachment.get("attachment_name"));
                doc.setCreatorId(userId);
                doc.setBelongUserId(belongUserIds);
                // 普通用户上传的附件默认是已分配
                if (userRole == 4) {
                    doc.setStatus(MagicMathConstData.ATTACHMENT_STATUS_ASSIGNED);
                } else {
                    doc.setStatus(MagicMathConstData.ATTACHMENT_STATUS_UNASSIGNED);
                }
                doc.setCreatedAt(System.currentTimeMillis() / 1000);
                doc.setUpdatedAt(System.currentTimeMillis() / 1000);
                attachmentDocuments.add(doc);
            }
            attachmentRepository.saveAll(attachmentDocuments);
            return new Result(ResultCode.R_Ok);
        } catch (Exception e) {
            logUtil.error("Error saving attachments: " + e.getMessage());
            throw e;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result assignAttachment(Map<String, Object> request) {
        try {
            // 获取当前用户角色
            Integer userRole = ThreadLocalUtil.getUserRole();
            if (userRole == null || (userRole != 2 && userRole != 3)) {
                return new Result(ResultCode.R_NoAuth);
            }

            // 检查参数
            if (!request.containsKey("attachment_id") || !request.containsKey("belong_user_id")) {
                return new Result(ResultCode.R_ParamError);
            }

            String attachmentId = (String) request.get("attachment_id");
            @SuppressWarnings("unchecked")
            List<String> belongUserIds = (List<String>) request.get("belong_user_id");

            if (attachmentId == null || belongUserIds == null || belongUserIds.isEmpty()) {
                return new Result(ResultCode.R_ParamError);
            }

            // 检查附件是否存在
            AttachmentDocument attachmentDoc = attachmentRepository.findById(attachmentId).orElse(null);
            if (attachmentDoc == null) {
                return new Result(ResultCode.R_AttachmentNotFound);
            }

            // 检查所有用户ID是否存在
            String assignId = ThreadLocalUtil.getUserId();
            if (assignId == null) {
                return new Result(ResultCode.R_Error);
            }
            // 先查询并验证所有用户,如果全部合格，则保存起来可以直接用，不用每次都查询
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
                // 验证附件当前所属用户是否包含自己或下属
                Set<String> subordinateIds = subordinateValidation.getAllSubordinateIds(assignId);
                if (subordinateIds == null) {
                    logUtil.error("获取下属列表失败");
                    return new Result(ResultCode.R_Error);
                }
                List<String> attachmentBelongUserIds = attachmentDoc.getBelongUserId();
                if (attachmentBelongUserIds == null || attachmentBelongUserIds.isEmpty()) {
                    logUtil.error("附件所属用户ID列表为空");
                    return new Result(ResultCode.R_Error);
                }

                // 检查是否至少有一个所属用户ID在下属列表中或者是指定用户
                boolean hasValidUser = attachmentBelongUserIds.stream()
                        .anyMatch(id -> subordinateIds.contains(id) || id.equals(assignId));

                if (!hasValidUser) {
                    logUtil.error("无权分配非本人或下属的附件");
                    return new Result(ResultCode.R_NoAuth);
                }

                // 验证要分配的用户是否是自己下属
                if (!belongUserIds.contains(UserConstData.COMPANY_USER_ID)) {
                    for (UserDocument userDoc : userDocs.values()) {
                        if (!userDoc.getBelongUserId().equals(assignId) && !userDoc.getUserId().equals(assignId)) {
                            return new Result(ResultCode.R_NotBelongToAdmin);
                        }
                    }
                }

            }

            // 更新附件的归属用户
            attachmentDoc.setBelongUserId(belongUserIds);
            attachmentDoc.setStatus(MagicMathConstData.ATTACHMENT_STATUS_ASSIGNED);
            attachmentDoc.setUpdatedAt(System.currentTimeMillis() / 1000);
            attachmentRepository.save(attachmentDoc);

            // 创建分配记录
            Map<String, Object> process = new HashMap<>();
            process.put("assignor_id", assignId);
            process.put("assignor_name", ThreadLocalUtil.getUserName());

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
            AttachmentAssignDocument existingAssignDoc = attachmentAssignRepository.findById(attachmentId).orElse(null);
            List<Map<String, Object>> processList;
            if (existingAssignDoc != null) {
                processList = existingAssignDoc.getAssignProcess();
                processList.add(0, process);
                existingAssignDoc.setAssignProcess(processList);
                attachmentAssignRepository.save(existingAssignDoc);
            } else {
                processList = new ArrayList<>();
                processList.add(process);
                AttachmentAssignDocument assignDoc = new AttachmentAssignDocument();
                assignDoc.setAttachmentId(attachmentId);
                assignDoc.setAssignProcess(processList);
                attachmentAssignRepository.save(assignDoc);
            }
            return new Result(ResultCode.R_Ok);

        } catch (Exception e) {
            logUtil.error("Error assigning attachment: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public Result assignAttachmentDetails(Map<String, Object> request) {
        // 参数校验
        if (!request.containsKey("attachment_id") || !request.containsKey("page_num") || !request.containsKey("page_size")) {
            return new Result(ResultCode.R_ParamError);
        }
        Integer pageNum = (Integer) request.get("page_num");
        Integer pageSize = (Integer) request.get("page_size");
        if (pageNum <= 0 || pageSize <= 0) {
            return new Result(ResultCode.R_PageError);
        }

        try {
            String attachmentId = (String) request.get("attachment_id");
            // 检查附件是否存在
            AttachmentDocument attachment = attachmentRepository.findById(attachmentId).orElse(null);
            if (attachment == null) {
                logUtil.error("附件不存在: " + attachmentId);
                return new Result(ResultCode.R_AttachmentNotFound);
            }
            // 获取分配历史
            AttachmentAssignDocument assignDocument = attachmentAssignRepository.findById(attachmentId).orElse(null);
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
            logUtil.error("Error getting attachment assign details: " + e.getMessage());
            return new Result(ResultCode.R_Error, "Error getting attachment assign details: " + e.getMessage());
        }
    }

    @Override
    public Result filterAttachment(Map<String, Object> request) {
        try {
            // 参数校验
            if (request == null) {
                return new Result(ResultCode.R_ParamError);
            }

            String attachmentName = (String) request.get("attachment_name");
            String belongUserName = (String) request.get("belong_user_name");
            String creatorName = (String) request.get("creator_name");
            Integer status = request.get("status") != null ? (Integer) request.get("status") : 0;
            Integer pageNum = request.get("page_num") != null ? (Integer) request.get("page_num") : 1;
            Integer pageSize = request.get("page_size") != null ? (Integer) request.get("page_size") : 30;

            // 校验分页参数
            if (pageNum < 1 || pageSize < 1) {
                return new Result(ResultCode.R_PageError);
            }

            // 校验状态参数
            if (status != 0 && status != 1 && status != 2) {
                return new Result(ResultCode.R_ParamError);
            }

            // 如果belongUserName是"公司"，直接执行查询
            if (StringUtils.hasText(belongUserName) && "公司".equals(belongUserName)) {
                BoolQuery.Builder mainQuery = new BoolQuery.Builder();

                // 公司id默认是1
                mainQuery.must(m -> m
                        .term(t -> t
                                .field("belong_user_id")
                                .value(UserConstData.COMPANY_USER_ID)
                        )
                );

                // 添加附件名称条件
                if (StringUtils.hasText(attachmentName)) {
                    mainQuery.must(m -> m
                            .match(t -> t
                                    .field("attachment_name")
                                    .query(attachmentName)
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

                SearchHits<AttachmentDocument> searchHits = elasticsearchOperations.search(
                        searchQuery,
                        AttachmentDocument.class
                );

                List<AttachmentDocument> content = searchHits.stream()
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
                BoolQuery.Builder mainQuery = new BoolQuery.Builder();
                boolean hasValidConditions = false;  // 标记是否有有效的查询条件

                // 检查是否有任何查询条件
                if (!StringUtils.hasText(attachmentName) &&
                        !StringUtils.hasText(belongUserName) &&
                        !StringUtils.hasText(creatorName) &&
                        (status == null || status == 0)) {
                    // 没有查询条件时，直接查询所有数据
                    mainQuery.must(m -> m
                            .matchAll(ma -> ma)
                    );
                } else {
                    // 有查询条件时的处理
                    if (StringUtils.hasText(attachmentName)) {
                        mainQuery.must(m -> m
                                .match(t -> t
                                        .field("attachment_name")
                                        .query(attachmentName)
                                )
                        );
                        hasValidConditions = true;
                    }

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

                    if (status != null && status != 0) {
                        mainQuery.must(m -> m
                                .term(t -> t
                                        .field("status")
                                        .value(status)
                                )
                        );
                        hasValidConditions = true;
                    }

                    // 如果所有的查询条件都无效（比如都没找到对应的用户），返回空结果
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

                SearchHits<AttachmentDocument> searchHits = elasticsearchOperations.search(
                        searchQuery,
                        AttachmentDocument.class
                );

                List<AttachmentDocument> content = searchHits.stream()
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
                BoolQuery.Builder mainQuery = new BoolQuery.Builder();
                boolean hasValidConditions = false;  // 标记是否有有效的查询条件

                // 检查是否有任何查询条件
                if (!StringUtils.hasText(attachmentName) &&
                        !StringUtils.hasText(belongUserName) &&
                        !StringUtils.hasText(creatorName) &&
                        (status == null || status == 0)) {
                    // 没有查询条件时，直接查询所有有权限的数据

                } else {
                    // 有查询条件时的处理
                    if (StringUtils.hasText(attachmentName)) {
                        mainQuery.must(m -> m
                                .match(t -> t
                                        .field("attachment_name")
                                        .query(attachmentName)
                                )
                        );
                        hasValidConditions = true;
                    }

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

                SearchHits<AttachmentDocument> searchHits = elasticsearchOperations.search(
                        searchQuery,
                        AttachmentDocument.class
                );

                List<AttachmentDocument> content = searchHits.stream()
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
                // 添加附件名称条件
                if (StringUtils.hasText(attachmentName)) {
                    mainQuery.must(m -> m
                            .match(t -> t
                                    .field("attachment_name")
                                    .query(attachmentName)
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

                SearchHits<AttachmentDocument> searchHits = elasticsearchOperations.search(
                        searchQuery,
                        AttachmentDocument.class
                );

                // 处理结果，只返回指定字段
                List<Map<String, Object>> content = searchHits.stream()
                        .map(hit -> {
                            AttachmentDocument doc = hit.getContent();
                            Map<String, Object> item = new HashMap<>();
                            item.put("id", doc.getAttachmentId());
                            item.put("name", doc.getAttachmentName());
                            item.put("url", doc.getAttachmentUrl());

                            // 查询创建者名称
                            String responseCreatorName = userRepository.findById(doc.getCreatorId())
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

            return new Result(ResultCode.R_Error);
        } catch (Exception e) {
            logUtil.error("Error filtering attachments: " + e.getMessage());
            return new Result(ResultCode.R_Error);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result deleteAttachment(Map<String, Object> request) {
        // 参数校验
        if (request == null) {
            return new Result(ResultCode.R_ParamError);
        }
        if (!request.containsKey("attachment_id")) {
            return new Result(ResultCode.R_ParamError);
        }
        String attachmentId = (String) request.get("attachment_id");
        AttachmentDocument attachmentDoc = attachmentRepository.findById(attachmentId).orElse(null);
        if (attachmentDoc == null) {
            return new Result(ResultCode.R_AttachmentNotFound);
        }
        // 获取当前用户信息
        String currentUserId = ThreadLocalUtil.getUserId();
        Integer userRole = ThreadLocalUtil.getUserRole();
        if (currentUserId == null || userRole == null) {
            return new Result(ResultCode.R_UserNotFound);
        }


        // 角色3需要检查创建者是否是自己或下属
        if (userRole == 3) {
            if(!subordinateValidation.isSubordinateOrSelf(attachmentDoc.getCreatorId(), currentUserId)){
                return new Result(ResultCode.R_NoAuth);
            }
        }
        // 角色4需要检查是否为创建者且是所属用户
        if (userRole == 4) {
            String creatorId = attachmentDoc.getCreatorId();
            List<String> belongUserIds = attachmentDoc.getBelongUserId();

            if (!currentUserId.equals(creatorId) ||
                    belongUserIds == null ||
                    !belongUserIds.contains(currentUserId)) {
                return new Result(ResultCode.R_NoAuth);
            }
        }
        // 删除附件
        try {
            // 获取文件URL并删除文件
            String attachmentUrl = attachmentDoc.getAttachmentUrl();
            if (attachmentUrl != null && !attachmentUrl.isEmpty()) {
                logUtil.info("开始删除附件：" + attachmentUrl);
                
                try {
                    URL url = new URL(attachmentUrl);
                    String protocol = url.getProtocol().toLowerCase();
                    
                    switch (protocol) {
                        case "http":
                        case "https":
                            // 修改 HTTP/HTTPS 删除处理
                            HttpHeaders headers = new HttpHeaders();
                            headers.setAccept(Collections.singletonList(MediaType.ALL));
                            HttpEntity<?> requestEntity = new HttpEntity<>(headers);
                            
                            ResponseEntity<Void> response = restTemplate.exchange(
                                attachmentUrl,
                                HttpMethod.DELETE,
                                requestEntity,
                                Void.class
                            );
                            
                            if (response.getStatusCode().is2xxSuccessful()) {
                                break;
                            } else {
                                throw new Exception("Failed to delete file: " + response.getStatusCode());
                            }
                            
                        case "file":
                            // 本地文件协议
                            Path filePath = Paths.get(url.toURI());
                            Files.deleteIfExists(filePath);
                            break;
                            
                        case "ftp":
                        case "sftp":
                            // FTP/SFTP 协议
                            deleteFtpFile(url);
                            break;
                            
                        case "s3":
                            // Amazon S3 协议
                            deleteS3File(url);
                            break;
                            
                        default:
                            // 尝试作为本地路径处理
                            Path localPath = Paths.get(attachmentUrl);
                            Files.deleteIfExists(localPath);
                    }
                    
                    logUtil.info("文件删除成功：" + attachmentUrl);
                    // 删除数据库记录
                    attachmentRepository.deleteById(attachmentId);
                    attachmentAssignRepository.deleteById(attachmentId);
                    return new Result(ResultCode.R_Ok);
                
                } catch (Exception e) {
                    logUtil.error("删除文件失败：" + e.getMessage());
                    return new Result(ResultCode.R_Error);
                }
            } else {
                logUtil.info("附件地址不合法：" + attachmentUrl);
                return new Result(ResultCode.R_UrlNotFound);
            }
        } catch (Exception e) {
            logUtil.error("Error deleting attachment: " + e.getMessage());
            return new Result(ResultCode.R_Error);
        }
    }

    // FTP/SFTP 文件删除方法
    private void deleteFtpFile(URL url) throws Exception {
        String protocol = url.getProtocol().toLowerCase();
        String host = url.getHost();
        int port = url.getPort();
        String path = url.getPath();
        String userInfo = url.getUserInfo();
        
        if ("ftp".equals(protocol)) {
            // FTP 删除
            FTPClient ftpClient = new FTPClient();
            try {
                // 连接设置
                ftpClient.setConnectTimeout(5000);
                ftpClient.connect(host, port != -1 ? port : 21);
                
                // 登录
                if (userInfo != null) {
                    String[] credentials = userInfo.split(":");
                    ftpClient.login(credentials[0], credentials.length > 1 ? credentials[1] : "");
                } else {
                    ftpClient.login("anonymous", "");
                }
                
                // 删除文件
                boolean deleted = ftpClient.deleteFile(path);
                if (!deleted) {
                    throw new Exception("Failed to delete FTP file: " + path);
                }
                
            } finally {
                if (ftpClient.isConnected()) {
                    ftpClient.disconnect();
                }
            }
        } else if ("sftp".equals(protocol)) {
            // SFTP 删除
            JSch jsch = new JSch();
            Session session = null;
            ChannelSftp channelSftp = null;
            
            try {
                // 创建会话
                session = jsch.getSession(
                    userInfo != null ? userInfo.split(":")[0] : "anonymous",
                    host,
                    port != -1 ? port : 22
                );
                
                // 设置密码
                if (userInfo != null && userInfo.split(":").length > 1) {
                    session.setPassword(userInfo.split(":")[1]);
                }
                
                // 跳过主机密钥验证
                java.util.Properties config = new java.util.Properties();
                config.put("StrictHostKeyChecking", "no");
                session.setConfig(config);
                
                // 连接
                session.connect(5000);
                
                // 打开 SFTP 通道
                channelSftp = (ChannelSftp) session.openChannel("sftp");
                channelSftp.connect();
                
                // 删除文件
                channelSftp.rm(path);
                
            } finally {
                if (channelSftp != null && channelSftp.isConnected()) {
                    channelSftp.disconnect();
                }
                if (session != null && session.isConnected()) {
                    session.disconnect();
                }
            }
        }
    }

    // S3 文件删除方法
    private void deleteS3File(URL url) throws Exception {
        // 解析 S3 URL
        // 格式: s3://bucket-name/key
        String path = url.getPath();
        String bucket = url.getHost();
        String key = path.startsWith("/") ? path.substring(1) : path;
        
        // 创建 S3 客户端
        AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
            .withRegion(Regions.DEFAULT_REGION)  // 设置你的 region
            .build();
        
        try {
            // 删除对象
            s3Client.deleteObject(bucket, key);
        } catch (AmazonS3Exception e) {
            throw new Exception("Failed to delete S3 file: " + e.getMessage());
        } finally {
            s3Client.shutdown();
        }
    }

    // 处理查询结果，转换为所需格式
    private List<Map<String, Object>> convertToResponseFormat(List<AttachmentDocument> documents) {
        return documents.stream().map(doc -> {
            Map<String, Object> item = new HashMap<>();
            item.put("id", doc.getAttachmentId());
            item.put("name", doc.getAttachmentName());
            item.put("url", doc.getAttachmentUrl());

            // 查询创建者名称
            String creatorName = userRepository.findById(doc.getCreatorId())
                    .map(UserDocument::getUserName)
                    .orElse("");
            item.put("creator_name", creatorName);

            // 查询所属用户名称列表
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

            item.put("status", doc.getStatus());
            return item;
        }).collect(Collectors.toList());
    }

}