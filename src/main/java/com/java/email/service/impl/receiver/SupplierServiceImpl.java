package com.java.email.service.impl.receiver;

import com.java.email.common.Response.PageResponse;
import com.java.email.common.Response.Result;
import com.java.email.common.Response.ResultCode;
import com.java.email.esdao.repository.dictionary.CommodityRepository;
import com.java.email.esdao.repository.dictionary.CountryRepository;
import com.java.email.esdao.repository.dictionary.EmailTypeRepository;
import com.java.email.esdao.repository.receiver.SupplierAssignRepository;
import com.java.email.esdao.repository.receiver.SupplierRepository;
import com.java.email.model.entity.dictionary.CommodityDocument;
import com.java.email.model.entity.dictionary.CountryDocument;
import com.java.email.model.entity.dictionary.EmailTypeDocument;
import com.java.email.model.entity.receiver.SupplierAssignDocument;
import com.java.email.model.entity.receiver.SupplierDocument;
import com.java.email.service.receiver.SupplierService;
import com.java.email.utils.LogUtil;
import com.java.email.common.userCommon.SubordinateValidation;
import com.java.email.common.userCommon.ThreadLocalUtil;
import com.java.email.constant.MagicMathConstData;
import com.java.email.constant.ReceiverConstData;
import com.java.email.constant.UserConstData;
import com.java.email.common.userCommon.SubordinateValidation.ValidationResult;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import java.util.Map;
import java.util.HashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.java.email.model.dto.request.SupplierFilterRequest;
import java.util.ArrayList;
import java.util.Arrays;

import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.http.ResponseEntity;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.client.erhlc.NativeSearchQuery;
import org.springframework.data.elasticsearch.client.erhlc.NativeSearchQueryBuilder;

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import com.java.email.esdao.repository.user.UserRepository;
import com.java.email.model.entity.user.UserDocument;

@Service
public class SupplierServiceImpl implements SupplierService {

    @Autowired
    private SupplierRepository supplierRepository;

    @Autowired
    private CommodityRepository commodityRepository;

    @Autowired
    private CountryRepository countryRepository;

    @Autowired
    private EmailTypeRepository emailTypeRepository;

    @Autowired
    private SupplierAssignRepository supplierAssignRepository;

    @Autowired
    private SubordinateValidation subordinateValidation;

    @Autowired
    private ElasticsearchOperations elasticsearchOperations;

    @Autowired
    private UserRepository userRepository;

    private static final LogUtil logUtil = LogUtil.getLogger(SupplierServiceImpl.class);

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result createSupplier(SupplierDocument supplierDocument) {
        if (supplierDocument == null) {
            return new Result(ResultCode.R_ParamError);
        }
        // 校验非空字段
        if (supplierDocument.getSupplierName() != null) {
            if (supplierDocument.getSupplierName().trim().isEmpty()) {
                logUtil.error("供应商名称不能为空");
                return new Result(ResultCode.R_ParamError);
            }
        }
        if (supplierDocument.getContactPerson() != null) {
            if (supplierDocument.getContactPerson().trim().isEmpty()) {
                logUtil.error("联系人不能为空");
                return new Result(ResultCode.R_ParamError);
            }
        }
        if (supplierDocument.getContactWay() != null) {
            if (supplierDocument.getContactWay().trim().isEmpty()) {
                logUtil.error("联系方式不能为空");
                return new Result(ResultCode.R_ParamError);
            }
        }
        if (supplierDocument.getBirth() != null) {
            if (supplierDocument.getBirth().trim().isEmpty()) {
                logUtil.error("出生日期不能为空");
                return new Result(ResultCode.R_ParamError);
            }
            try {
                // Validate birth date format (yyyy-MM-dd)
                if (!supplierDocument.getBirth().matches("^\\d{4}-\\d{2}-\\d{2}$")) {
                    logUtil.error("出生日期格式错误，应为yyyy-MM-dd格式");
                    return new Result(ResultCode.R_ParamError);
                }

                // Convert to ISO format
                DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                LocalDate date = LocalDate.parse(supplierDocument.getBirth(), inputFormatter);
                String isoDate = date.atStartOfDay(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT);
                supplierDocument.setBirth(isoDate);
                java.time.Instant.parse(supplierDocument.getBirth());
            } catch (Exception e) {
                logUtil.error("出生日期格式错误: " + e.getMessage());
                return new Result(ResultCode.R_ParamError);
            }
        }
        if (supplierDocument.getSex() != null) {
            if (!(supplierDocument.getSex().equals("男") || supplierDocument.getSex().equals("女"))) {
                logUtil.error("性别必须为男或女");
                return new Result(ResultCode.R_ParamError);
            }
        }
        if (supplierDocument.getEmails() != null) {
            if (supplierDocument.getEmails().isEmpty()) {
                logUtil.error("邮箱不能为空");
                return new Result(ResultCode.R_ParamError);
            }
            for (String email : supplierDocument.getEmails()) {
                if (email == null || email.trim().isEmpty()) {
                    logUtil.error("邮箱不能为空");
                    return new Result(ResultCode.R_ParamError);
                }
                if (!email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")) {
                    logUtil.error("邮箱格式错误: " + email);
                    return new Result(ResultCode.R_ParamError);
                }
            }
        }
        if (supplierDocument.getSupplierLevel() != null) {
            if (supplierDocument.getSupplierLevel() < ReceiverConstData.SUPPLIER_LEVEL_LOW   || supplierDocument.getSupplierLevel() > ReceiverConstData.SUPPLIER_LEVEL_HIGH) {
                logUtil.error("供应商等级必须在1-3之间");
                return new Result(ResultCode.R_ParamError);
            }
        }
        if (supplierDocument.getTradeType() != null) {
            if (supplierDocument.getTradeType() < ReceiverConstData.TRADE_TYPE_FACTORY || supplierDocument.getTradeType() > ReceiverConstData.TRADE_TYPE_TRADER) {
                logUtil.error("贸易类型必须在1-2之间");
                return new Result(ResultCode.R_ParamError);
            }
        }
        if (supplierDocument.getCommodityId() != null) {
            if (supplierDocument.getCommodityId().isEmpty()) {
                logUtil.error("商品ID不能为空");
                return new Result(ResultCode.R_ParamError);
            }
            for (String commodityId : supplierDocument.getCommodityId()) {
                if (commodityId == null || commodityId.trim().isEmpty()) {
                    logUtil.error("商品ID不能为空");
                    return new Result(ResultCode.R_ParamError);
                }
                // 验证商品ID是否存在
                CommodityDocument commodityDoc = commodityRepository.findById(commodityId).orElse(null);
                if (commodityDoc == null) {
                    logUtil.error("商品不存在: " + commodityId);
                    return new Result(ResultCode.R_CommodityNotFound); 
                }
            }
        }
        if (supplierDocument.getSupplierCountryId() != null) {
            if (supplierDocument.getSupplierCountryId().trim().isEmpty()) {
                logUtil.error("供应商国家ID不能为空");
                return new Result(ResultCode.R_ParamError);
            }
            CountryDocument countryDoc = countryRepository.findById(supplierDocument.getSupplierCountryId()).orElse(null);
            if (countryDoc == null) {
                logUtil.error("国家不存在: " + supplierDocument.getSupplierCountryId());
                return new Result(ResultCode.R_CountryNotFound); 
            }
        }

        // 获取用户ID
        String userId = ThreadLocalUtil.getUserId();
        Integer userRole =  ThreadLocalUtil.getUserRole();
        if(userId == null || userRole == null){
            return new Result(ResultCode.R_UserNotFound);
        }
        

        try {
            // Save the supplier document
            supplierDocument.setSupplierId(UUID.randomUUID().toString());
            // 普通用户默认已分配
            if(userRole == 4){
                supplierDocument.setStatus(MagicMathConstData.SUPPLIER_STATUS_ASSIGNED);
            }else{
                supplierDocument.setStatus(MagicMathConstData.SUPPLIER_STATUS_UNASSIGNED);
            }
            supplierDocument.setBelongUserId(userId);
            supplierDocument.setCreatorId(userId);
            // 默认接受所有邮件类型
            List<String> emailTypeIds = StreamSupport.stream(emailTypeRepository.findAll().spliterator(), false)
                    .map(EmailTypeDocument::getEmailTypeId)
                    .collect(Collectors.toList());
            supplierDocument.setAcceptEmailTypeId(emailTypeIds);
            // 获取当前时间
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
            String currentTime = LocalDateTime.now().format(formatter);
            supplierDocument.setCreatedAt(currentTime);
            supplierDocument.setUpdatedAt(currentTime);
            SupplierDocument savedSupplier = supplierRepository.save(supplierDocument);
            if (savedSupplier == null) {
                return new Result(ResultCode.R_Fail);
            }
            return new Result(ResultCode.R_Ok); 
        } catch (Exception e) {
            logUtil.error("Error saving supplier: " + e.getMessage());
            return new Result(ResultCode.R_Error);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result updateSupplier(SupplierDocument supplierDocument) {
        if (supplierDocument == null) {
            return new Result(ResultCode.R_ParamError);
        }
        // 验证供应商ID
        if (supplierDocument.getSupplierId() == null || supplierDocument.getSupplierId().trim().isEmpty()) {
            logUtil.error("供应商ID不能为空");
            return new Result(ResultCode.R_ParamError);
        }
        // 检查供应商是否存在
        SupplierDocument existingSupplier = supplierRepository.findById(supplierDocument.getSupplierId()).orElse(null);
        if (existingSupplier == null) {
            logUtil.error("供应商不存在: " + supplierDocument.getSupplierId());
            return new Result(ResultCode.R_SupplierNotFound);
        }
        // 获取用户ID
        String userId = ThreadLocalUtil.getUserId();
        Integer userRole =  ThreadLocalUtil.getUserRole();
        if(userId == null || userRole == null){
            return new Result(ResultCode.R_UserNotFound);
        }
        if (userRole == 4) {
            if (!userId.equals(existingSupplier.getBelongUserId())) {
                logUtil.error("无权修改非本人所属的供应商");
                return new Result(ResultCode.R_NoAuth);
            }
        }

        // 校验非空字段
        if (supplierDocument.getSupplierName() != null && !supplierDocument.getSupplierName().trim().isEmpty()) {
            existingSupplier.setSupplierName(supplierDocument.getSupplierName().trim());
        }
        if (supplierDocument.getContactPerson() != null && !supplierDocument.getContactPerson().trim().isEmpty()) {
            existingSupplier.setContactPerson(supplierDocument.getContactPerson().trim());
        }
        if (supplierDocument.getContactWay() != null && !supplierDocument.getContactWay().trim().isEmpty()) {
            existingSupplier.setContactWay(supplierDocument.getContactWay().trim());
        }
        if (supplierDocument.getBirth() != null && !supplierDocument.getBirth().trim().isEmpty()) {
            try {
                // Validate birth date format (yyyy-MM-dd)
                if (!supplierDocument.getBirth().matches("^\\d{4}-\\d{2}-\\d{2}$")) {
                    logUtil.error("出生日期格式错误，应为yyyy-MM-dd格式");
                    return new Result(ResultCode.R_ParamError);
                }

                // Convert to ISO format
                DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                LocalDate date = LocalDate.parse(supplierDocument.getBirth(), inputFormatter);
                String isoDate = date.atStartOfDay(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT);
                supplierDocument.setBirth(isoDate);
                java.time.Instant.parse(supplierDocument.getBirth());
                existingSupplier.setBirth(supplierDocument.getBirth());
            } catch (Exception e) {
                logUtil.error("出生日期格式错误: " + e.getMessage());
                return new Result(ResultCode.R_ParamError);
            }
        }
        if (supplierDocument.getSex() != null && !supplierDocument.getSex().trim().isEmpty()) {
            if (!(supplierDocument.getSex().equals("男") || supplierDocument.getSex().equals("女"))) {
                logUtil.error("性别必须为男或女");
                return new Result(ResultCode.R_ParamError);
            }
            existingSupplier.setSex(supplierDocument.getSex());
        }
        if (supplierDocument.getEmails() != null && !supplierDocument.getEmails().isEmpty()) {
            for (String email : supplierDocument.getEmails()) {
                if (email == null || email.trim().isEmpty()) {
                    logUtil.error("邮箱不能为空");
                    return new Result(ResultCode.R_ParamError);
                }
                if (!email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")) {
                    logUtil.error("邮箱格式错误: " + email);
                    return new Result(ResultCode.R_ParamError);
                }
            }
            existingSupplier.setEmails(supplierDocument.getEmails());
        }
        if (supplierDocument.getSupplierLevel() != null) {
            if (supplierDocument.getSupplierLevel() < ReceiverConstData.SUPPLIER_LEVEL_LOW || 
                supplierDocument.getSupplierLevel() > ReceiverConstData.SUPPLIER_LEVEL_HIGH) {
                logUtil.error("供应商等级必须在1-3之间");
                return new Result(ResultCode.R_ParamError);
            }
            existingSupplier.setSupplierLevel(supplierDocument.getSupplierLevel());
        }
        if (supplierDocument.getTradeType() != null) {
            if (!(supplierDocument.getTradeType().equals(ReceiverConstData.TRADE_TYPE_FACTORY) || 
                  supplierDocument.getTradeType().equals(ReceiverConstData.TRADE_TYPE_TRADER))) {
                logUtil.error("贸易类型必须在1-2之间");
                return new Result(ResultCode.R_ParamError);
            }
            existingSupplier.setTradeType(supplierDocument.getTradeType());
        }
        if (supplierDocument.getCommodityId() != null && !supplierDocument.getCommodityId().isEmpty()) {
            for (String commodityId : supplierDocument.getCommodityId()) {
                if (commodityId == null || commodityId.trim().isEmpty()) {
                    logUtil.error("商品ID不能为空");
                    return new Result(ResultCode.R_ParamError);
                }
                // 验证商品ID是否存在
                CommodityDocument commodityDoc = commodityRepository.findById(commodityId).orElse(null);
                if (commodityDoc == null) {
                    logUtil.error("商品不存在: " + commodityId);
                    return new Result(ResultCode.R_CommodityNotFound); 
                }
            }
            existingSupplier.setCommodityId(supplierDocument.getCommodityId());
        }
        if (supplierDocument.getSupplierCountryId() != null && !supplierDocument.getSupplierCountryId().trim().isEmpty()) {
            CountryDocument countryDoc = countryRepository.findById(supplierDocument.getSupplierCountryId()).orElse(null);
            if (countryDoc == null) {
                logUtil.error("国家不存在: " + supplierDocument.getSupplierCountryId());
                return new Result(ResultCode.R_CountryNotFound); 
            }
            existingSupplier.setSupplierCountryId(supplierDocument.getSupplierCountryId());
        }

        try {
            // 更新时间
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
            String currentTime = LocalDateTime.now().format(formatter);
            existingSupplier.setUpdatedAt(currentTime);
            
            SupplierDocument savedSupplier = supplierRepository.save(existingSupplier);
            if (savedSupplier == null) {
                return new Result(ResultCode.R_Fail);
            }
            return new Result(ResultCode.R_Ok); 
        } catch (Exception e) {
            logUtil.error("Error updating supplier: " + e.getMessage());
            return new Result(ResultCode.R_Error);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result deleteSupplier(SupplierDocument supplierDocument) {
        if (supplierDocument == null) {
            return new Result(ResultCode.R_ParamError);
        }
        String supplierId = supplierDocument.getSupplierId();
        if (supplierId == null || supplierId.trim().isEmpty()) {
            return new Result(ResultCode.R_ParamError);
        }

        // 获取用户ID和角色
        String userId = ThreadLocalUtil.getUserId();
        Integer userRole = ThreadLocalUtil.getUserRole();
        if (userId == null || userRole == null) {
            return new Result(ResultCode.R_UserNotFound);
        }

        // 检查供应商是否存在
        SupplierDocument existingSupplier = supplierRepository.findById(supplierId).orElse(null);
        if (existingSupplier == null) {
            logUtil.error("供应商不存在: " + supplierId);
            return new Result(ResultCode.R_SupplierNotFound);
        }

        // 如果是主管（角色为3），需要验证权限
        if (userRole == 3) {
            // 检查是否为创建人
            boolean isCreator = userId.equals(existingSupplier.getCreatorId());
            
            // 检查创建人和所属用户是否都是下属
            ValidationResult creatorValidation = subordinateValidation.isSubordinate(userId, existingSupplier.getCreatorId());
            ValidationResult belongValidation = subordinateValidation.isSubordinate(userId, existingSupplier.getBelongUserId());
            boolean hasSubordinate = belongValidation.isValid() && creatorValidation.isValid();
            
            // 如果既不是创建人，也不是下属，则无权删除
            if (!isCreator && !hasSubordinate) {
                return new Result(ResultCode.R_NoAuth);
            }
        }
        // 如果是普通用户（角色为4），需要验证权限
        if (userRole == 4) {
            if (!userId.equals(existingSupplier.getBelongUserId()) || !userId.equals(existingSupplier.getCreatorId())) {
                logUtil.error("无权删除非本人创建或所属的供应商");
                return new Result(ResultCode.R_NoAuth);
            }
        }

        try {
            supplierRepository.deleteById(supplierId);
            supplierAssignRepository.deleteById(supplierId);
            return new Result(ResultCode.R_Ok);
        } catch (Exception e) {
            logUtil.error("Error deleting supplier: " + e.getMessage());
            return new Result(ResultCode.R_Error);
        }
    }

    @Override
    public Result filterSupplier(SupplierFilterRequest request) {
        if (request == null) {
            return new Result(ResultCode.R_ParamError);
        }

        // 获取用户ID和角色
        String currentUserId = ThreadLocalUtil.getUserId();
        Integer userRole = ThreadLocalUtil.getUserRole();
        if (currentUserId == null || userRole == null) {
            return new Result(ResultCode.R_UserNotFound);
        }
        String currentUserName = ThreadLocalUtil.getUserName();
        if(currentUserName == null){
            return new Result(ResultCode.R_UserNotFound);
        }

        // 检查pageNum和pageSize
        if (request.getPageNum() == null || request.getPageNum() <= 0 || request.getPageSize() == null || request.getPageSize() <= 0) {
            return new Result(ResultCode.R_ParamError);
        }

        String belongUserName = request.getBelongUserName();
        if(belongUserName != null && !belongUserName.trim().isEmpty()){
            belongUserName = belongUserName.trim();
        }
        
        try {
            // 构建分页
            Pageable pageable = PageRequest.of(request.getPageNum() - 1, request.getPageSize());
            NativeQuery searchQuery = null;
            // 主查询条件
            BoolQuery.Builder mainQuery = new BoolQuery.Builder();

            // 所属用户为公司，则直接查询所有与公司相关的供应商
            if(belongUserName != null && belongUserName.equals(UserConstData.COMPANY_USER_NAME)) {
                mainQuery.must(m -> m.term(t -> t.field("belong_user_id").value(UserConstData.COMPANY_USER_ID)));
                // 创建人条件
                if (request.getCreatorName() != null && !request.getCreatorName().trim().isEmpty()) {
                    List<UserDocument> creators = userRepository.findByUserNameLike(request.getCreatorName().trim());
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
                        mainQuery.must(m -> m.bool(creatorQuery.build()));
                    }
                }

                // 供应商名称条件
                if (request.getSupplierName() != null && !request.getSupplierName().trim().isEmpty()) {
                    mainQuery.must(m -> m
                            .match(t -> t
                                    .field("supplier_name")
                                    .query(request.getSupplierName().trim())
                            )
                    );
                }

                // 联系人条件
                if (request.getContactPerson() != null && !request.getContactPerson().trim().isEmpty()) {
                    mainQuery.must(m -> m
                            .match(t -> t
                                    .field("contact_person")
                                    .query(request.getContactPerson().trim())
                            )
                    );
                }

                // 联系方式条件
                if (request.getContactWay() != null && !request.getContactWay().trim().isEmpty()) {
                    mainQuery.must(m -> m
                            .match(t -> t
                                    .field("contact_way")
                                    .query(request.getContactWay().trim())
                            )
                    );
                }

                // 供应商等级条件
                if (request.getSupplierLevel() != null) {
                    mainQuery.must(m -> m
                            .term(t -> t
                                    .field("supplier_level")
                                    .value(request.getSupplierLevel())
                            )
                    );
                }

                // 供应商状态条件
                if (request.getStatus() != null) {
                    mainQuery.must(m -> m
                            .term(t -> t
                                    .field("status")
                                    .value(request.getStatus())
                            )
                    );
                }

                // 性别条件
                if (request.getSex() != null) {
                    mainQuery.must(m -> m
                            .term(t -> t
                                    .field("sex")
                                    .value(request.getSex())
                            )
                    );
                }

                // 生日条件
                if (request.getBirth() != null && !request.getBirth().trim().isEmpty()) {
                    mainQuery.must(m -> m
                            .term(t -> t
                                    .field("birth")
                                    .value(request.getBirth().trim())
                            )
                    );
                }

                // 邮箱条件
                if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
                    mainQuery.must(m -> m
                            .match(t -> t
                                    .field("emails")
                                    .query(request.getEmail().trim())
                            )
                    );
                }

                // 邮件类型条件
                if (request.getAcceptEmailTypeId() != null && !request.getAcceptEmailTypeId().isEmpty()) {
                    BoolQuery.Builder emailTypeQuery = new BoolQuery.Builder();
                    for (String emailTypeId : request.getAcceptEmailTypeId()) {
                        emailTypeQuery.should(s -> s
                                .term(t -> t
                                        .field("accept_email_type_id")
                                        .value(emailTypeId)
                                )
                        );
                    }
                    mainQuery.must(m -> m.bool(emailTypeQuery.build()));
                }

                // 国家条件
                if (request.getSupplierCountryId() != null && !request.getSupplierCountryId().trim().isEmpty()) {
                    mainQuery.must(m -> m
                            .term(t -> t
                                    .field("supplier_country_id")
                                    .value(request.getSupplierCountryId().trim())
                            )
                    );
                }

                // 贸易类型条件
                if (request.getTradeType() != null) {
                    mainQuery.must(m -> m
                            .term(t -> t
                                    .field("trade_type")
                                    .value(request.getTradeType())
                            )
                    );
                }

                // 商品ID条件
                if (request.getCommodityName() != null && !request.getCommodityName().trim().isEmpty()) {
                    List<CommodityDocument> commodities = commodityRepository.findByCommodityNameLike(request.getCommodityName().trim());
                    if (!commodities.isEmpty()) {
                        BoolQuery.Builder commodityQuery = new BoolQuery.Builder();
                        for (CommodityDocument commodity : commodities) {
                            commodityQuery.should(s -> s
                                    .term(t -> t
                                            .field("commodity_id")
                                            .value(commodity.getCommodityId())
                                    )
                            );
                        }
                        mainQuery.must(m -> m.bool(commodityQuery.build()));
                    }
                }

                searchQuery = NativeQuery.builder()
                    .withQuery(q -> q.bool(mainQuery.build()))
                    .withSort(Sort.by(Sort.Direction.DESC, "created_at"))
                    .withPageable(pageable)
                    .build();

                // 执行查询
                SearchHits<SupplierDocument> searchHits = elasticsearchOperations.search(searchQuery, SupplierDocument.class);
                
                // 获取当前页的数据
                List<SupplierDocument> suppliers = searchHits.getSearchHits().stream()
                    .map(SearchHit::getContent)
                    .collect(Collectors.toList());

                return new Result(
                    ResultCode.R_Ok, 
                    new PageResponse<>(
                        searchHits.getTotalHits(),
                        request.getPageNum(),
                        request.getPageSize(),
                        convertToResponseFormat(suppliers)
                    )
                );
            }
            
            if(userRole.equals(UserConstData.ROLE_ADMIN_LARGE)){
                // 创建人条件
                if(request.getCreatorName() != null && !request.getCreatorName().trim().isEmpty()){
                    List<UserDocument> creators = userRepository.findByUserNameLike(request.getCreatorName().trim());
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
                        mainQuery.must(m -> m.bool(creatorQuery.build()));
                    }
                }

                // 所属用户条件
                if(request.getBelongUserName() != null && !request.getBelongUserName().trim().isEmpty()){
                    List<UserDocument> belongUsers = userRepository.findByUserNameLike(request.getBelongUserName().trim());
                    if (!belongUsers.isEmpty()) {
                        BoolQuery.Builder belongQuery = new BoolQuery.Builder();
                        for (UserDocument belongUser : belongUsers) {
                            belongQuery.should(s -> s
                                    .term(t -> t
                                            .field("belong_user_id")
                                            .value(belongUser.getUserId())
                                    )
                            );
                        }
                        mainQuery.must(m -> m.bool(belongQuery.build()));
                    }
                }
                
                // 供应商名称条件
                if (request.getSupplierName() != null && !request.getSupplierName().trim().isEmpty()) {
                    mainQuery.must(m -> m
                            .match(t -> t
                                    .field("supplier_name")
                                    .query(request.getSupplierName().trim())
                            )
                    );
                }

                // 联系人条件
                if (request.getContactPerson() != null && !request.getContactPerson().trim().isEmpty()) {
                    mainQuery.must(m -> m
                            .match(t -> t
                                    .field("contact_person")
                                    .query(request.getContactPerson().trim())
                            )
                    );
                }

                // 联系方式条件
                if (request.getContactWay() != null && !request.getContactWay().trim().isEmpty()) {
                    mainQuery.must(m -> m
                            .match(t -> t
                                    .field("contact_way")
                                    .query(request.getContactWay().trim())
                            )
                    );
                }

                // 供应商等级条件
                if (request.getSupplierLevel() != null && request.getSupplierLevel() >= ReceiverConstData.SUPPLIER_LEVEL_LOW && request.getSupplierLevel() <= ReceiverConstData.SUPPLIER_LEVEL_HIGH) {
                    mainQuery.must(m -> m
                            .term(t -> t
                                    .field("supplier_level")
                                    .value(request.getSupplierLevel())
                            )
                    );
                }

                // 贸易类型条件
                if (request.getTradeType() != null && (request.getTradeType().equals(ReceiverConstData.TRADE_TYPE_FACTORY) || request.getTradeType().equals(ReceiverConstData.TRADE_TYPE_TRADER))) {
                    mainQuery.must(m -> m
                            .term(t -> t
                                    .field("trade_type")
                                    .value(request.getTradeType())
                            )
                    );
                }

                // 国家ID条件
                if (request.getSupplierCountryId() != null && !request.getSupplierCountryId().trim().isEmpty()) {
                    mainQuery.must(m -> m
                            .term(t -> t
                                    .field("supplier_country_id")
                                    .value(request.getSupplierCountryId().trim())
                            )
                    );
                }

                // 商品ID条件
                if (request.getCommodityName() != null && !request.getCommodityName().trim().isEmpty()) {
                    // 根据商品名称查询商品ID
                    List<CommodityDocument> commodities = commodityRepository.findByCommodityNameLike(request.getCommodityName().trim());
                    if (!commodities.isEmpty()) {
                        BoolQuery.Builder commodityQuery = new BoolQuery.Builder();
                        for (CommodityDocument commodity : commodities) {
                            commodityQuery.should(s -> s
                                    .term(t -> t
                                            .field("commodity_id")
                                            .value(commodity.getCommodityId())
                                    )
                            );
                        }
                        mainQuery.must(m -> m.bool(commodityQuery.build()));
                    }
                }

                // 性别条件
                if (request.getSex() != null && (request.getSex().equals("男") || request.getSex().equals("女"))) {
                    mainQuery.must(m -> m
                            .term(t -> t
                                    .field("sex")
                                    .value(request.getSex())
                            )
                    );
                }

                // 出生日期条件
                if (request.getBirth() != null && !request.getBirth().trim().isEmpty()) {
                    try {
                        java.time.Instant.parse(request.getBirth());
                        mainQuery.must(m -> m
                                .term(t -> t
                                        .field("birth")
                                        .value(request.getBirth())
                                )
                        );
                    } catch (Exception e) {
                        logUtil.error("出生日期格式错误: " + e.getMessage());
                        return new Result(ResultCode.R_ParamError);
                    }
                }

                // 邮箱条件
                if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
                    mainQuery.must(m -> m
                            .match(t -> t
                                    .field("emails")
                                    .query(request.getEmail().trim())
                            )
                    );
                }

                // 邮件类型条件
                if (request.getAcceptEmailTypeId() != null && !request.getAcceptEmailTypeId().isEmpty()) {
                    BoolQuery.Builder emailTypeQuery = new BoolQuery.Builder();
                    for (String emailTypeId : request.getAcceptEmailTypeId()) {
                        emailTypeQuery.should(s -> s
                                .term(t -> t
                                        .field("accept_email_type_id")
                                        .value(emailTypeId)
                                )
                        );
                    }
                    mainQuery.must(m -> m.bool(emailTypeQuery.build()));
                }

                // 状态条件
                if (request.getStatus() != null && (request.getStatus() == MagicMathConstData.SUPPLIER_STATUS_ASSIGNED || request.getStatus() == MagicMathConstData.SUPPLIER_STATUS_UNASSIGNED)) {
                    mainQuery.must(m -> m
                            .term(t -> t
                                    .field("status")
                                    .value(request.getStatus())
                            )
                    );
                }

                // 构建查询
                searchQuery = NativeQuery.builder()
                    .withQuery(q -> q.bool(mainQuery.build()))
                    .withSort(Sort.by(Sort.Direction.DESC, "created_at"))
                    .withPageable(pageable)
                    .build();
            }
            
            if(userRole.equals(UserConstData.ROLE_ADMIN_SMALL)){
                // 添加必要的权限过滤
                BoolQuery.Builder accessQuery = new BoolQuery.Builder();
                boolean hasCreator = false;
                boolean hasBelong = false;

                // 验证创建人是否为自己或下属
                if(request.getCreatorName() != null && !request.getCreatorName().trim().isEmpty()){
                    hasCreator = true;
                    ValidationResult creatorValidation = subordinateValidation.findSubordinatesAndSelfByName(
                            request.getCreatorName().trim(),
                            currentUserId
                    );
                    if (!creatorValidation.isValid()) {
                        return new Result(ResultCode.R_NotBelongToAdmin);
                    }
                    List<UserDocument> validUsers = creatorValidation.getValidUsers();
                    // 创建人条件
                    BoolQuery.Builder creatorQuery = new BoolQuery.Builder();
                    for (UserDocument user : validUsers) {
                        creatorQuery.should(s -> s
                                .term(t -> t
                                        .field("creator_id")
                                        .value(user.getUserId())
                                )
                        );
                    }
                    mainQuery.must(m -> m.bool(creatorQuery.build()));
                }
                
                // 验证所属用户是否为自己或下属
                if(request.getBelongUserName() != null && !request.getBelongUserName().trim().isEmpty()){
                    hasBelong = true;
                    ValidationResult belongValidation = subordinateValidation.findSubordinatesAndSelfByName(
                            request.getBelongUserName().trim(),
                            currentUserId
                    );
                    if (!belongValidation.isValid()) {
                        return new Result(ResultCode.R_NotBelongToAdmin);
                    }
                    List<UserDocument> validUsers = belongValidation.getValidUsers();
                    // 所属用户条件
                    BoolQuery.Builder belongQuery = new BoolQuery.Builder();
                    for (UserDocument user : validUsers) {
                        belongQuery.should(s -> s
                                .term(t -> t
                                        .field("belong_user_id")
                                        .value(user.getUserId())
                                )
                        );
                    }
                    mainQuery.must(m -> m.bool(belongQuery.build()));
                } 
                
                // 如果创建者和所属用户都没有指定，则添加必要查询条件
                if (!hasCreator && !hasBelong) {
                    // 创建者是自己或下属
                    BoolQuery.Builder creatorAccessQuery = new BoolQuery.Builder();
                    creatorAccessQuery.should(s -> s
                            .term(t -> t
                                    .field("creator_id")
                                    .value(currentUserId)
                            )
                    );
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
                }
                
                // 供应商名称条件
                if (request.getSupplierName() != null && !request.getSupplierName().trim().isEmpty()) {
                    mainQuery.must(m -> m
                            .match(t -> t
                                    .field("supplier_name")
                                    .query(request.getSupplierName().trim())
                            )
                    );
                }

                // 联系人条件
                if (request.getContactPerson() != null && !request.getContactPerson().trim().isEmpty()) {
                    mainQuery.must(m -> m
                            .match(t -> t
                                    .field("contact_person")
                                    .query(request.getContactPerson().trim())
                            )
                    );
                }

                // 联系方式条件
                if (request.getContactWay() != null && !request.getContactWay().trim().isEmpty()) {
                    mainQuery.must(m -> m
                            .match(t -> t
                                    .field("contact_way")
                                    .query(request.getContactWay().trim())
                            )
                    );
                }
                
                // 供应商等级条件
                if (request.getSupplierLevel() != null && request.getSupplierLevel() >= ReceiverConstData.SUPPLIER_LEVEL_LOW && request.getSupplierLevel() <= ReceiverConstData.SUPPLIER_LEVEL_HIGH) {
                    mainQuery.must(m -> m
                            .term(t -> t
                                    .field("supplier_level")
                                    .value(request.getSupplierLevel())
                            )
                    );
                }

                
                // 贸易类型条件
                if (request.getTradeType() != null && (request.getTradeType().equals(ReceiverConstData.TRADE_TYPE_FACTORY) || request.getTradeType().equals(ReceiverConstData.TRADE_TYPE_TRADER))) {
                    mainQuery.must(m -> m
                            .term(t -> t
                                    .field("trade_type")
                                    .value(request.getTradeType())
                            )
                    );
                }

                // 国家ID条件
                if (request.getSupplierCountryId() != null && !request.getSupplierCountryId().trim().isEmpty()) {
                    mainQuery.must(m -> m
                            .term(t -> t
                                    .field("supplier_country_id")
                                    .value(request.getSupplierCountryId().trim())
                            )
                    );
                }

                // 商品ID条件
                if (request.getCommodityName() != null && !request.getCommodityName().trim().isEmpty()) {
                    // 根据商品名称查询商品ID
                    List<CommodityDocument> commodities = commodityRepository.findByCommodityNameLike(request.getCommodityName().trim());
                    if (!commodities.isEmpty()) {
                        BoolQuery.Builder commodityQuery = new BoolQuery.Builder();
                        for (CommodityDocument commodity : commodities) {
                            commodityQuery.should(s -> s
                                    .term(t -> t
                                            .field("commodity_id")
                                            .value(commodity.getCommodityId())
                                    )
                            );
                        }
                        mainQuery.must(m -> m.bool(commodityQuery.build()));
                    }
                }

                // 性别条件
                if (request.getSex() != null && (request.getSex().equals("男") || request.getSex().equals("女"))) {
                    mainQuery.must(m -> m
                            .term(t -> t
                                    .field("sex")
                                    .value(request.getSex())
                            )
                    );
                }

                // 出生日期条件
                if (request.getBirth() != null && !request.getBirth().trim().isEmpty()) {
                    try {
                        java.time.Instant.parse(request.getBirth());
                        mainQuery.must(m -> m
                                .term(t -> t
                                        .field("birth")
                                        .value(request.getBirth())
                                )
                        );
                    } catch (Exception e) {
                        logUtil.error("出生日期格式错误: " + e.getMessage());
                        return new Result(ResultCode.R_ParamError);
                    }
                }

                // 邮箱条件
                if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
                    mainQuery.must(m -> m
                            .match(t -> t
                                    .field("emails")
                                    .query(request.getEmail().trim())
                            )
                    );
                }

                // 邮件类型条件
                if (request.getAcceptEmailTypeId() != null && !request.getAcceptEmailTypeId().isEmpty()) {
                    BoolQuery.Builder emailTypeQuery = new BoolQuery.Builder();
                    for (String emailTypeId : request.getAcceptEmailTypeId()) {
                        emailTypeQuery.should(s -> s
                                .term(t -> t
                                        .field("accept_email_type_id")
                                        .value(emailTypeId)
                                )
                        );
                    }
                    mainQuery.must(m -> m.bool(emailTypeQuery.build()));
                }

                // 状态条件
                if (request.getStatus() != null && (request.getStatus() == MagicMathConstData.SUPPLIER_STATUS_ASSIGNED || request.getStatus() == MagicMathConstData.SUPPLIER_STATUS_UNASSIGNED)) {
                    mainQuery.must(m -> m
                            .term(t -> t
                                    .field("status")
                                    .value(request.getStatus())
                            )
                    );
                }
                // 构建查询
                searchQuery = NativeQuery.builder()
                        .withQuery(q -> q.bool(mainQuery.build()))
                        .withSort(Sort.by(Sort.Direction.DESC, "created_at"))
                        .withPageable(pageable)
                        .build();

            }

            if(userRole.equals(UserConstData.ROLE_USER)){
                // 普通用户必须查询自己的供应商
                BoolQuery.Builder belongQuery = new BoolQuery.Builder();
                belongQuery.should(s -> s
                            .term(t -> t
                                .field("belong_user_id")
                                .value(currentUserId)
                            )
                        );
                // 添加所属用户条件
                mainQuery.must(m -> m.bool(belongQuery.build()));
                
                // 供应商名称条件
                if (request.getSupplierName() != null && !request.getSupplierName().trim().isEmpty()) {
                    mainQuery.must(m -> m
                            .match(t -> t
                                    .field("supplier_name")
                                    .query(request.getSupplierName().trim())
                            )
                    );
                }

                // 联系人条件
                if (request.getContactPerson() != null && !request.getContactPerson().trim().isEmpty()) {
                    mainQuery.must(m -> m
                            .match(t -> t
                                    .field("contact_person")
                                    .query(request.getContactPerson().trim())
                            )
                    );
                }

                // 联系方式条件
                if (request.getContactWay() != null && !request.getContactWay().trim().isEmpty()) {
                    mainQuery.must(m -> m
                            .match(t -> t
                                    .field("contact_way")
                                    .query(request.getContactWay().trim())
                            )
                    );
                }

                // 供应商等级条件
                if (request.getSupplierLevel() != null && request.getSupplierLevel() >= ReceiverConstData.SUPPLIER_LEVEL_LOW && request.getSupplierLevel() <= ReceiverConstData.SUPPLIER_LEVEL_HIGH) {
                    mainQuery.must(m -> m
                            .term(t -> t
                                    .field("supplier_level")
                                    .value(request.getSupplierLevel())
                            )
                    );
                }

                // 贸易类型条件
                if (request.getTradeType() != null && (request.getTradeType().equals(ReceiverConstData.TRADE_TYPE_FACTORY) || request.getTradeType().equals(ReceiverConstData.TRADE_TYPE_TRADER))) {
                    mainQuery.must(m -> m
                            .term(t -> t
                                    .field("trade_type")
                                    .value(request.getTradeType())
                            )
                    );
                }

                // 国家ID条件
                if (request.getSupplierCountryId() != null && !request.getSupplierCountryId().trim().isEmpty()) {
                    mainQuery.must(m -> m
                            .term(t -> t
                                    .field("supplier_country_id")
                                    .value(request.getSupplierCountryId().trim())
                            )
                    );
                }

                // 商品ID条件
                if (request.getCommodityName() != null && !request.getCommodityName().trim().isEmpty()) {
                    // 根据商品名称查询商品ID
                    List<CommodityDocument> commodities = commodityRepository.findByCommodityNameLike(request.getCommodityName().trim());
                    if (!commodities.isEmpty()) {
                        BoolQuery.Builder commodityQuery = new BoolQuery.Builder();
                        for (CommodityDocument commodity : commodities) {
                            commodityQuery.should(s -> s
                                    .term(t -> t
                                            .field("commodity_id")
                                            .value(commodity.getCommodityId())
                                    )
                            );
                        }
                        mainQuery.must(m -> m.bool(commodityQuery.build()));
                    }
                }

                // 性别条件
                if (request.getSex() != null && ("男".equals(request.getSex()) || "女".equals(request.getSex()))) {
                    mainQuery.must(m -> m
                            .term(t -> t
                                    .field("sex")
                                    .value(request.getSex())
                            )
                    );
                }

                // 出生日期条件
                if (request.getBirth() != null && !request.getBirth().trim().isEmpty()) {
                    try {
                        java.time.Instant.parse(request.getBirth());
                        mainQuery.must(m -> m
                                .term(t -> t
                                        .field("birth")
                                        .value(request.getBirth())
                                )
                        );
                    } catch (Exception e) {
                        logUtil.error("出生日期格式错误: " + e.getMessage());
                        return new Result(ResultCode.R_ParamError);
                    }
                }

                // 邮箱条件
                if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
                    mainQuery.must(m -> m
                            .match(t -> t
                                    .field("emails")
                                    .query(request.getEmail().trim())
                            )
                    );
                }

                // 邮件类型条件
                if (request.getAcceptEmailTypeId() != null && !request.getAcceptEmailTypeId().isEmpty()) {
                    BoolQuery.Builder emailTypeQuery = new BoolQuery.Builder();
                    for (String emailTypeId : request.getAcceptEmailTypeId()) {
                        emailTypeQuery.should(s -> s
                                .term(t -> t
                                        .field("accept_email_type_id")
                                        .value(emailTypeId)
                                )
                        );
                    }
                    mainQuery.must(m -> m.bool(emailTypeQuery.build()));
                }


                // 构建查询
                searchQuery = NativeQuery.builder()
                    .withQuery(q -> q.bool(mainQuery.build()))
                    .withSort(Sort.by(Sort.Direction.DESC, "created_at"))
                    .withPageable(pageable)
                    .build();

            }
            
            // 执行查询
            SearchHits<SupplierDocument> searchHits = elasticsearchOperations.search(searchQuery, SupplierDocument.class);
            
            // 获取当前页的数据
            List<SupplierDocument> suppliers = searchHits.getSearchHits().stream()
                .map(SearchHit::getContent)
                .collect(Collectors.toList());

            return new Result(
                ResultCode.R_Ok, 
                new PageResponse<>(
                    searchHits.getTotalHits(),
                    request.getPageNum(),
                    request.getPageSize(),
                    convertToResponseFormat(suppliers)
                )
            );
        } catch (Exception e) {
            logUtil.error("Error filtering suppliers: " + e.getMessage());
            return new Result(ResultCode.R_Error);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result assignSupplier(SupplierDocument supplierDocument) {
        // 参数校验
        if (supplierDocument == null || 
            supplierDocument.getSupplierId() == null || 
            supplierDocument.getSupplierId().trim().isEmpty() ||
            supplierDocument.getBelongUserId() == null || 
            supplierDocument.getBelongUserId().trim().isEmpty()) {
            return new Result(ResultCode.R_ParamError);
        }

        // 获取当前用户信息
        String currentUserId = ThreadLocalUtil.getUserId();
        Integer userRole = ThreadLocalUtil.getUserRole();
        if (currentUserId == null || userRole == null) {
            return new Result(ResultCode.R_UserNotFound);
        }
        String currentUserName = ThreadLocalUtil.getUserName();
        if (currentUserName == null) {
            return new Result(ResultCode.R_UserNotFound);
        }

        try {
            // 检查供应商是否存在
            SupplierDocument existingSupplier = supplierRepository.findById(supplierDocument.getSupplierId()).orElse(null);
            if (existingSupplier == null) {
                logUtil.error("供应商不存在: " + supplierDocument.getSupplierId());
                return new Result(ResultCode.R_SupplierNotFound);
            }

            // 检查目标用户是否存在
            UserDocument targetUser = userRepository.findById(supplierDocument.getBelongUserId()).orElse(null);
            if (targetUser == null) {
                logUtil.error("目标用户不存在: " + supplierDocument.getBelongUserId());
                return new Result(ResultCode.R_UserNotFound);
            }
            String targetUserName = targetUser.getUserName();
            if (targetUserName == null) {
                return new Result(ResultCode.R_UserNotFound);
            }

            // 权限检查
            if (userRole.equals(UserConstData.ROLE_ADMIN_SMALL)) {
                // 小管理员只能分配给自己的下属，且只能分配自己或下属的供应商
                boolean targetValidation = subordinateValidation.isSubordinateOrSelf(
                    supplierDocument.getBelongUserId(), 
                    currentUserId
                );
                if (!targetValidation) {
                    logUtil.error("无权分配给非下属用户");
                    return new Result(ResultCode.R_NoAuth);
                }
            } else if (!userRole.equals(UserConstData.ROLE_ADMIN_LARGE)) {
                // 非管理员无权分配
                logUtil.error("无权分配供应商");
                return new Result(ResultCode.R_NoAuth);
            }

            // 更新供应商信息
            existingSupplier.setBelongUserId(supplierDocument.getBelongUserId());
            existingSupplier.setStatus(MagicMathConstData.SUPPLIER_STATUS_ASSIGNED);
            
            // 更新时间
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
            String currentTime = LocalDateTime.now().format(formatter);
            existingSupplier.setUpdatedAt(currentTime);

            // 保存更新
            SupplierDocument savedSupplier = supplierRepository.save(existingSupplier);
            if (savedSupplier == null) {
                return new Result(ResultCode.R_UpdateDbFailed);
            }
            String supplierId = savedSupplier.getSupplierId();
            String belongUserId = savedSupplier.getBelongUserId();

            // 更新分配记录
            SupplierAssignDocument existingAssignment = supplierAssignRepository.findById(supplierId).orElse(null);
            Map<String, Object> process = new HashMap<>();
            process.put("assignor_id", currentUserId);
            process.put("assignor_name", currentUserName);
            process.put("assignee_id", belongUserId);
            process.put("assignee_name", targetUserName);
            process.put("assign_date", currentTime);

            if (existingAssignment == null) {
                existingAssignment = new SupplierAssignDocument();
                existingAssignment.setSupplierId(supplierId);
                existingAssignment.setAssignProcess(new ArrayList<>());
                existingAssignment.getAssignProcess().add(process);
            } else {
                existingAssignment.getAssignProcess().add(0, process);
            }
            supplierAssignRepository.save(existingAssignment);
            return new Result(ResultCode.R_Ok);
        } catch (Exception e) {
            logUtil.error("Error assigning supplier: " + e.getMessage());
            return new Result(ResultCode.R_Error);
        }
    }

    @Override
    public Result assignSupplierDetails(Map<String, Object> params) {
        // 参数校验
        if (params == null || 
            params.get("supplier_id") == null || 
            params.get("page_num") == null || 
            params.get("page_size") == null) {
            return new Result(ResultCode.R_ParamError);
        }

        String supplierId = (String) params.get("supplier_id");
        Integer pageNum = (Integer) params.get("page_num");
        Integer pageSize = (Integer) params.get("page_size");

        if (supplierId == null || supplierId.trim().isEmpty()) {
            return new Result(ResultCode.R_ParamError);
        }
        if (pageNum <= 0 || pageSize <= 0) {
            return new Result(ResultCode.R_ParamError);
        }

        // 获取当前用户信息
        String currentUserId = ThreadLocalUtil.getUserId();
        Integer userRole = ThreadLocalUtil.getUserRole();
        if (currentUserId == null || userRole == null) {
            return new Result(ResultCode.R_UserNotFound);
        }

        try {
            // 检查供应商是否存在
            SupplierDocument supplier = supplierRepository.findById(supplierId).orElse(null);
            if (supplier == null) {
                logUtil.error("供应商不存在: " + supplierId);
                return new Result(ResultCode.R_SupplierNotFound);
            }

            // 获取分配历史
            SupplierAssignDocument assignDocument = supplierAssignRepository.findById(supplierId).orElse(null);
            if (assignDocument == null || assignDocument.getAssignProcess() == null) {
                return new Result(ResultCode.R_Ok, new PageResponse<>(0L, pageNum, pageSize, new ArrayList<>()));
            }

            // 获取总记录数
            long total = assignDocument.getAssignProcess().size();

            // 计算分页
            int start = (pageNum - 1) * pageSize;
            int end = Math.min(start + pageSize, assignDocument.getAssignProcess().size());

            // 获取当前页的数据
            List<Map<String, Object>> pageData = new ArrayList<>();
            if (start < assignDocument.getAssignProcess().size()) {
                pageData = assignDocument.getAssignProcess().subList(start, end);
            }

            // 返回分页数据
            return new Result(
                ResultCode.R_Ok, 
                new PageResponse<>(
                    total,
                    pageNum,
                    pageSize,
                    pageData
                )
            );

        } catch (NumberFormatException e) {
            logUtil.error("页码格式错误: " + e.getMessage());
            return new Result(ResultCode.R_ParamError);
        } catch (Exception e) {
            logUtil.error("Error getting supplier assign details: " + e.getMessage());
            return new Result(ResultCode.R_Error);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result allAssignSupplier(Map<String, Object> params) {
        // 参数校验
        if (params == null || 
            params.get("supplier_id") == null || 
            params.get("belong_user_id") == null) {
            return new Result(ResultCode.R_ParamError);
        }

        // 获取参数
        List<String> supplierIds;
        try {
            supplierIds = (List<String>) params.get("supplier_id");
        } catch (ClassCastException e) {
            logUtil.error("供应商ID列表格式错误");
            return new Result(ResultCode.R_ParamError);
        }
        String belongUserId = (String) params.get("belong_user_id");

        // 参数有效性校验
        if (supplierIds.isEmpty() || belongUserId == null || belongUserId.trim().isEmpty()) {
            return new Result(ResultCode.R_ParamError);
        }

        // 获取当前用户信息
        String currentUserId = ThreadLocalUtil.getUserId();
        Integer userRole = ThreadLocalUtil.getUserRole();
        String currentUserName = ThreadLocalUtil.getUserName();
        if (currentUserId == null || userRole == null || currentUserName == null) {
            return new Result(ResultCode.R_UserNotFound);
        }

        try {
            // 检查目标用户是否存在
            UserDocument targetUser = userRepository.findById(belongUserId).orElse(null);
            if (targetUser == null) {
                logUtil.error("目标用户不存在: " + belongUserId);
                return new Result(ResultCode.R_UserNotFound);
            }
            String targetUserName = targetUser.getUserName();
            if (targetUserName == null) {
                return new Result(ResultCode.R_UserNotFound);
            }

            // 权限检查
            if (userRole.equals(UserConstData.ROLE_ADMIN_SMALL)) {
                if(!belongUserId.equals(UserConstData.COMPANY_USER_ID)){
                    // 小管理员只能分配给自己或下属
                    boolean targetValidation = subordinateValidation.isSubordinateOrSelf(
                        belongUserId, 
                        currentUserId
                );
                if (!targetValidation) {
                    logUtil.error("无权分配给非下属用户");
                        return new Result(ResultCode.R_NoAuth);
                    }
                }
            } else if (!userRole.equals(UserConstData.ROLE_ADMIN_LARGE)) {
                // 非管理员无权分配
                logUtil.error("无权分配供应商");
                return new Result(ResultCode.R_NoAuth);
            }

            // 获取当前时间
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
            String currentTime = LocalDateTime.now().format(formatter);

            // 批量更新供应商
            for (String supplierId : supplierIds) {
                // 检查供应商是否存在
                SupplierDocument existingSupplier = supplierRepository.findById(supplierId).orElse(null);
                if (existingSupplier == null) {
                    logUtil.error("供应商不存在: " + supplierId);
                    continue;
                }

                // 更新供应商信息
                existingSupplier.setBelongUserId(belongUserId);
                existingSupplier.setStatus(MagicMathConstData.SUPPLIER_STATUS_ASSIGNED);
                existingSupplier.setUpdatedAt(currentTime);

                // 保存更新
                SupplierDocument savedSupplier = supplierRepository.save(existingSupplier);
                if (savedSupplier == null) {
                    continue;
                }

                // 更新分配记录
                SupplierAssignDocument existingAssignment = supplierAssignRepository.findById(supplierId).orElse(null);
                Map<String, Object> process = new HashMap<>();
                process.put("assignor_id", currentUserId);
                process.put("assignor_name", currentUserName);
                process.put("assignee_id", belongUserId);
                process.put("assignee_name", targetUserName);
                process.put("assign_date", currentTime);

                if (existingAssignment == null) {
                    existingAssignment = new SupplierAssignDocument();
                    existingAssignment.setSupplierId(supplierId);
                    existingAssignment.setAssignProcess(new ArrayList<>());
                    existingAssignment.getAssignProcess().add(process);
                } else {
                    existingAssignment.getAssignProcess().add(0, process);
                }
                supplierAssignRepository.save(existingAssignment);
            }
            return new Result(ResultCode.R_Ok);
        } catch (Exception e) {
            logUtil.error("Error assigning suppliers: " + e.getMessage());
            return new Result(ResultCode.R_Error);
        }
    }

    private List<Map<String, Object>> convertToResponseFormat(List<SupplierDocument> suppliers) {
        if (suppliers == null || suppliers.isEmpty()) {
            return new ArrayList<>();
        }

        return suppliers.stream()
                .map(supplier -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("supplier_id", supplier.getSupplierId() );
                    map.put("supplier_name", supplier.getSupplierName());
                    
                    // Get creator name from creator id
                    UserDocument creator = userRepository.findById(supplier.getCreatorId()).orElse(null);
                    map.put("creator_name", creator != null ? creator.getUserName() : "");
                    
                    map.put("contact_person", supplier.getContactPerson());
                    map.put("contact_way", supplier.getContactWay());
                    map.put("supplier_level", supplier.getSupplierLevel());
                    
                    // Get country name from country id
                    CountryDocument country = countryRepository.findById(supplier.getSupplierCountryId()).orElse(null);
                    map.put("supplier_country_name", country != null ? country.getCountryName() : "");
                    
                    map.put("trade_type", supplier.getTradeType());
                    
                    // Get commodity names from commodity ids
                    List<String> commodityNames = supplier.getCommodityId().stream()
                            .map(id -> commodityRepository.findById(id)
                                    .map(CommodityDocument::getCommodityName)
                            .orElse(""))
                            .collect(Collectors.toList());
                    map.put("commodity_name", commodityNames);
                    
                    map.put("sex", supplier.getSex());
                    map.put("birth", supplier.getBirth());
                    map.put("emails", supplier.getEmails());
                    map.put("status", supplier.getStatus());
                    
                    // Get belong user name from belong user id
                    UserDocument belongUser = userRepository.findById(supplier.getBelongUserId()).orElse(null);
                    map.put("belong_user_name", belongUser != null ? belongUser.getUserName() : "");
                    
                    // Get email type names from email type ids
                    List<String> emailTypeNames = supplier.getAcceptEmailTypeId().stream()
                            .map(id -> emailTypeRepository.findById(id)
                                    .map(EmailTypeDocument::getEmailTypeName)
                                    .orElse(""))
                            .collect(Collectors.toList());
                    map.put("accept_email_type_name", emailTypeNames);
                    
                    return map;
                })
                .collect(Collectors.toList());
    }

    @Override
    public Result importSupplier(MultipartFile file) {
        if (file.isEmpty()) {
            return new Result(ResultCode.R_ParamError);
        }

        if (!file.getOriginalFilename().endsWith(".csv")) {
            return new Result(ResultCode.R_ParamError);
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            // 跳过CSV头行
            String headerLine = reader.readLine();
            String line;
            List<SupplierDocument> suppliers = new ArrayList<>();
            
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",");
                SupplierDocument supplier = new SupplierDocument();
                
                // 设置基本信息
                supplier.setSupplierName(data[0]);
                supplier.setContactPerson(data[1]);
                supplier.setContactWay(data[2]);
                supplier.setSupplierLevel(Integer.parseInt(data[3]));
                
                // 通过国家名称查询国家ID
                String countryName = data[4];
                List<CountryDocument> country = countryRepository.findByCountryNameLike(countryName);
                logUtil.info("country: " + country);
                CountryDocument matchedCountry = null;
                for (CountryDocument c : country) {
                    if (c.getCountryName().equals(countryName)) {
                        matchedCountry = c;
                        break;
                    }
                }
                if (matchedCountry == null) {
                    continue;
                }
                supplier.setSupplierCountryId(matchedCountry.getCountryId());
                
                supplier.setTradeType(Integer.parseInt(data[5]));
                
                // 处理商品名称数组，转换为商品ID
                String[] commodityNames = data[6].split(";");
                List<String> commodityIds = new ArrayList<>();
                for (String commodityName : commodityNames) {
                    CommodityDocument commodity = commodityRepository.findByCommodityName(commodityName.trim());
                    if (commodity == null) {
                        continue;
                    }
                    commodityIds.add(commodity.getCommodityId());
                }
                supplier.setCommodityId(commodityIds);
                
                supplier.setSex(data[7]);
                
                // 处理birth日期，转换为ISO格式
                String birthDate = data[8];
                if (birthDate != null && !birthDate.isEmpty()) {
                    try {
                        // 支持多种日期格式: yyyy/MM/dd, yyyy-MM-dd, 包括单位数的月日
                        DateTimeFormatter[] formatters = {
                            DateTimeFormatter.ofPattern("yyyy/MM/dd"),
                            DateTimeFormatter.ofPattern("yyyy/M/d"),
                            DateTimeFormatter.ofPattern("yyyy-MM-dd"),
                            DateTimeFormatter.ofPattern("yyyy-M-d")
                        };

                        LocalDate date = null;
                        for (DateTimeFormatter formatter : formatters) {
                            try {
                                date = LocalDate.parse(birthDate, formatter);
                                break;
                            } catch (DateTimeParseException e) {
                                continue;
                            }
                        }

                        if (date == null) {
                            logUtil.error("无法解析日期: " + birthDate);
                            throw new DateTimeParseException("无法解析日期", birthDate, 0);
                        }

                        String isoDate = date.atStartOfDay(ZoneOffset.UTC)
                                .format(DateTimeFormatter.ISO_INSTANT);
                        supplier.setBirth(isoDate);
                    } catch (DateTimeParseException e) {
                        logUtil.error("日期格式错误，应为yyyy/MM/dd格式：" + birthDate);
                        continue;
                    }
                }
                
                // 处理邮箱列表
                String[] emails = data[9].split(";");
                supplier.setEmails(Arrays.asList(emails));
                // 设置接受的邮件类型ID列表
                List<String> emailTypeIds = StreamSupport.stream(emailTypeRepository.findAll().spliterator(), false)
                        .map(EmailTypeDocument::getEmailTypeId)
                        .collect(Collectors.toList());
                supplier.setAcceptEmailTypeId(emailTypeIds);

                // 设置用户相关字段
                String userId = ThreadLocalUtil.getUserId();
                supplier.setBelongUserId(userId);
                supplier.setCreatorId(userId);

                // 设置状态为已分配(2)
                supplier.setStatus(MagicMathConstData.SUPPLIER_STATUS_ASSIGNED);

                // 生成供应商ID
                supplier.setSupplierId(UUID.randomUUID().toString());

                // 设置创建和更新时间
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
                String currentTime = LocalDateTime.now().format(formatter);
                supplier.setCreatedAt(currentTime);
                supplier.setUpdatedAt(currentTime);
                
                suppliers.add(supplier);
            }
            
            // 批量保存到ES
            supplierRepository.saveAll(suppliers);
            
            return new Result(ResultCode.R_Ok, suppliers.size());
            
        } catch (IOException e) {
            logUtil.error("CSV文件读取失败", e);
            return new Result(ResultCode.R_Error, "文件处理失败：" + e.getMessage());
        } catch (Exception e) {
            logUtil.error("数据处理失败", e);
            return new Result(ResultCode.R_Error, "数据处理失败：" + e.getMessage());
        }
    }

}
