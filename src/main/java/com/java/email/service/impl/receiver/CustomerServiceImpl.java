package com.java.email.service.impl.receiver;

import com.java.email.common.Response.PageResponse;
import com.java.email.common.Response.Result;
import com.java.email.common.Response.ResultCode;
import com.java.email.esdao.repository.dictionary.CommodityRepository;
import com.java.email.esdao.repository.dictionary.CountryRepository;
import com.java.email.esdao.repository.dictionary.EmailTypeRepository;
import com.java.email.esdao.repository.receiver.CustomerAssignRepository;
import com.java.email.esdao.repository.receiver.CustomerRepository;
import com.java.email.esdao.repository.user.UserRepository;
import com.java.email.model.dto.request.CustomerFilterRequest;
import com.java.email.model.entity.dictionary.CommodityDocument;
import com.java.email.model.entity.dictionary.CountryDocument;
import com.java.email.model.entity.dictionary.EmailTypeDocument;
import com.java.email.model.entity.receiver.CustomerAssignDocument;
import com.java.email.model.entity.receiver.CustomerDocument;
import com.java.email.model.entity.user.UserDocument;
import com.java.email.service.receiver.CustomerService;
import com.java.email.utils.LogUtil;

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;

import com.java.email.common.userCommon.SubordinateValidation;
import com.java.email.common.userCommon.SubordinateValidation.ValidationResult;
import com.java.email.common.userCommon.ThreadLocalUtil;
import com.java.email.constant.MagicMathConstData;
import com.java.email.constant.ReceiverConstData;
import com.java.email.constant.UserConstData;
import com.java.email.model.entity.user.UserDocument;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class CustomerServiceImpl implements CustomerService {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private CommodityRepository commodityRepository;

    @Autowired
    private CountryRepository countryRepository;

    @Autowired
    private EmailTypeRepository emailTypeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CustomerAssignRepository customerAssignRepository;

    @Autowired
    private SubordinateValidation subordinateValidation;

    @Autowired
    private ElasticsearchOperations elasticsearchOperations;

    private static final LogUtil logUtil = LogUtil.getLogger(CustomerServiceImpl.class);

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result createCustomer(CustomerDocument customerDocument) {
        if (customerDocument == null) {
            return new Result(ResultCode.R_ParamError);
        }
        // 校验必填字段
        if (customerDocument.getCustomerName() == null || customerDocument.getCustomerName().trim().isEmpty()) {
            logUtil.error("客户名称不能为空");
            return new Result(ResultCode.R_ParamError);
        }
        if (customerDocument.getContactPerson() == null || customerDocument.getContactPerson().trim().isEmpty()) {
            logUtil.error("联系人不能为空");
            return new Result(ResultCode.R_ParamError);
        }
        if (customerDocument.getContactWay() == null || customerDocument.getContactWay().trim().isEmpty()) {
            logUtil.error("联系方式不能为空");
            return new Result(ResultCode.R_ParamError);
        }
        if (customerDocument.getBirth() == null || customerDocument.getBirth().trim().isEmpty()) {
            logUtil.error("出生日期不能为空");
            return new Result(ResultCode.R_ParamError);
        }
        try {
            // Validate birth date format (yyyy-MM-dd)
            if (!customerDocument.getBirth().matches("^\\d{4}-\\d{2}-\\d{2}$")) {
                logUtil.error("出生日期格式错误，应为yyyy-MM-dd格式");
                return new Result(ResultCode.R_ParamError);
            }

            // Convert to ISO format
            DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate date = LocalDate.parse(customerDocument.getBirth(), inputFormatter);
            String isoDate = date.atStartOfDay(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT);
            customerDocument.setBirth(isoDate);
            java.time.Instant.parse(customerDocument.getBirth());
        } catch (Exception e) {
            logUtil.error("出生日期格式错误: " + e.getMessage());
            return new Result(ResultCode.R_ParamError);
        }
        if (customerDocument.getSex() == null || !(customerDocument.getSex().equals("男") || customerDocument.getSex().equals("女"))) {
            logUtil.error("性别必须为男或女");
            return new Result(ResultCode.R_ParamError);
        }
        if (customerDocument.getEmails() == null || customerDocument.getEmails().isEmpty()) {
            logUtil.error("邮箱不能为空");
            return new Result(ResultCode.R_ParamError);
        }
        for (String email : customerDocument.getEmails()) {
            if (email == null || email.trim().isEmpty()) {
                logUtil.error("邮箱不能为空");
                return new Result(ResultCode.R_ParamError);
            }
            if (!email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")) {
                logUtil.error("邮箱格式错误: " + email);
                return new Result(ResultCode.R_ParamError);
            }
        }
        if (customerDocument.getCustomerLevel() == null || customerDocument.getCustomerLevel() < ReceiverConstData.CUSTOMER_LEVEL_LOW || customerDocument.getCustomerLevel() > ReceiverConstData.CUSTOMER_LEVEL_HIGH) {
            logUtil.error("客户等级必须在1-3之间");
            return new Result(ResultCode.R_ParamError);
        }

        if (customerDocument.getTradeType() == null || customerDocument.getTradeType() < ReceiverConstData.TRADE_TYPE_FACTORY || customerDocument.getTradeType() > ReceiverConstData.TRADE_TYPE_TRADER) {
            logUtil.error("贸易类型必须在1-2之间");
            return new Result(ResultCode.R_ParamError);
        }
        if (customerDocument.getCommodityId() == null || customerDocument.getCommodityId().isEmpty()) {
            logUtil.error("商品ID不能为空");
            return new Result(ResultCode.R_ParamError);
        }
        for (String commodityId : customerDocument.getCommodityId()) {
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
        if(customerDocument.getCustomerCountryId() == null || customerDocument.getCustomerCountryId().trim().isEmpty()){
            logUtil.error("客户国家ID不能为空");
            return new Result(ResultCode.R_ParamError);
        }
        CountryDocument countryDoc = countryRepository.findById(customerDocument.getCustomerCountryId()).orElse(null);
        if (countryDoc == null) {
            logUtil.error("国家不存在: " + customerDocument.getCustomerCountryId());
            return new Result(ResultCode.R_CountryNotFound); 
        }

        // 获取用户ID
        String userId = ThreadLocalUtil.getUserId();
        Integer userRole =  ThreadLocalUtil.getUserRole();
        if(userId == null || userRole == null){
            return new Result(ResultCode.R_UserNotFound);
        }
        
        try {
            // Save the customer document
            String customerId = UUID.randomUUID().toString();
            customerDocument.setCustomerId(customerId);
            // 普通用户默认已分配
            if(userRole == 4){
                customerDocument.setStatus(MagicMathConstData.CUSTOMER_STATUS_ASSIGNED);
            }else{
                customerDocument.setStatus(MagicMathConstData.CUSTOMER_STATUS_UNASSIGNED);
            }
            customerDocument.setBelongUserId(userId);
            customerDocument.setCreatorId(userId);
            // 默认接受所有邮件类型
            List<String> emailTypeIds = StreamSupport.stream(emailTypeRepository.findAll().spliterator(), false)
                    .map(EmailTypeDocument::getEmailTypeId)
                    .collect(Collectors.toList());
            customerDocument.setAcceptEmailTypeId(emailTypeIds);
            // 获取当前时间
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
            String currentTime = LocalDateTime.now().format(formatter);
            customerDocument.setCreatedAt(currentTime);
            customerDocument.setUpdatedAt(currentTime);
            CustomerDocument savedCustomer = customerRepository.save(customerDocument);
            if (savedCustomer == null) {
                return new Result(ResultCode.R_Fail);
            }
            return new Result(ResultCode.R_Ok); 
        } catch (Exception e) {
            logUtil.error("Error saving customer: " + e.getMessage());
            return new Result(ResultCode.R_Error);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result updateCustomer(CustomerDocument customerDocument) {
        if (customerDocument == null) {
            return new Result(ResultCode.R_ParamError);
        }
        // 验证客户ID
        if (customerDocument.getCustomerId() == null || customerDocument.getCustomerId().trim().isEmpty()) {
            logUtil.error("客户ID不能为空");
            return new Result(ResultCode.R_ParamError);
        }
        // 检查客户是否存在
        CustomerDocument existingCustomer = customerRepository.findById(customerDocument.getCustomerId()).orElse(null);
        if (existingCustomer == null) {
            logUtil.error("客户不存在: " + customerDocument.getCustomerId());
            return new Result(ResultCode.R_CustomerNotFound);
        }

        // 获取用户ID
        String userId = ThreadLocalUtil.getUserId();
        Integer userRole =  ThreadLocalUtil.getUserRole();
        if(userId == null || userRole == null){
            return new Result(ResultCode.R_UserNotFound);
        }
        if (userRole == 4) {
            if (!userId.equals(existingCustomer.getBelongUserId())) {
                logUtil.error("无权修改非本人所属的客户");
                return new Result(ResultCode.R_NoAuth);
            }
        }
        // 校验非空字段
        if (customerDocument.getCustomerName() != null && !customerDocument.getCustomerName().trim().isEmpty()) {
            existingCustomer.setCustomerName(customerDocument.getCustomerName().trim());
        }
        if (customerDocument.getContactPerson() != null && !customerDocument.getContactPerson().trim().isEmpty()) {
            existingCustomer.setContactPerson(customerDocument.getContactPerson().trim());
        }
        if (customerDocument.getContactWay() != null && !customerDocument.getContactWay().trim().isEmpty()) {
            existingCustomer.setContactWay(customerDocument.getContactWay().trim());
        }
        if (customerDocument.getBirth() != null && !customerDocument.getBirth().trim().isEmpty()) {
            try {
                // Validate birth date format (yyyy-MM-dd)
                if (!customerDocument.getBirth().matches("^\\d{4}-\\d{2}-\\d{2}$")) {
                    logUtil.error("出生日期格式错误，应为yyyy-MM-dd格式");
                    return new Result(ResultCode.R_ParamError);
                }

                // Convert to ISO format
                DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                LocalDate date = LocalDate.parse(customerDocument.getBirth(), inputFormatter);
                String isoDate = date.atStartOfDay(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT);
                customerDocument.setBirth(isoDate);
                java.time.Instant.parse(customerDocument.getBirth());
                existingCustomer.setBirth(customerDocument.getBirth());
            } catch (Exception e) {
                logUtil.error("出生日期格式错误: " + e.getMessage());
                return new Result(ResultCode.R_ParamError);
            }
        }
        if (customerDocument.getSex() != null && !customerDocument.getSex().trim().isEmpty()) {
            if (!(customerDocument.getSex().equals("男") || customerDocument.getSex().equals("女"))) {
                logUtil.error("性别必须为男或女");
                return new Result(ResultCode.R_ParamError);
            }
            existingCustomer.setSex(customerDocument.getSex());
        }
        if (customerDocument.getEmails() != null && !customerDocument.getEmails().isEmpty()) {
            for (String email : customerDocument.getEmails()) {
                if (email == null || email.trim().isEmpty()) {
                    logUtil.error("邮箱不能为空");
                    return new Result(ResultCode.R_ParamError);
                }
                if (!email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")) {
                    logUtil.error("邮箱格式错误: " + email);
                    return new Result(ResultCode.R_ParamError);
                }
            }
            existingCustomer.setEmails(customerDocument.getEmails());
        }
        if (customerDocument.getCustomerLevel() != null && customerDocument.getCustomerLevel() != 0) {
            if (customerDocument.getCustomerLevel() < ReceiverConstData.CUSTOMER_LEVEL_LOW || 
                customerDocument.getCustomerLevel() > ReceiverConstData.CUSTOMER_LEVEL_HIGH) {
                logUtil.error("客户等级必须在1-3之间");
                return new Result(ResultCode.R_ParamError);
            }
            existingCustomer.setCustomerLevel(customerDocument.getCustomerLevel());
        }
        if (customerDocument.getTradeType() != null && customerDocument.getTradeType() != 0) {
            if (!(customerDocument.getTradeType().equals(ReceiverConstData.TRADE_TYPE_FACTORY) || 
                  customerDocument.getTradeType().equals(ReceiverConstData.TRADE_TYPE_TRADER))) {
                logUtil.error("贸易类型必须在1-2之间");
                return new Result(ResultCode.R_ParamError);
            }
            existingCustomer.setTradeType(customerDocument.getTradeType());
        }
        if (customerDocument.getCommodityId() != null && !customerDocument.getCommodityId().isEmpty()) {
            for (String commodityId : customerDocument.getCommodityId()) {
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
            existingCustomer.setCommodityId(customerDocument.getCommodityId());
        }
        if (customerDocument.getCustomerCountryId() != null && !customerDocument.getCustomerCountryId().trim().isEmpty()) {
            CountryDocument countryDoc = countryRepository.findById(customerDocument.getCustomerCountryId()).orElse(null);
            if (countryDoc == null) {
                logUtil.error("国家不存在: " + customerDocument.getCustomerCountryId());
                return new Result(ResultCode.R_CountryNotFound); 
            }
            existingCustomer.setCustomerCountryId(customerDocument.getCustomerCountryId());
        }

        try {
            // 更新时间
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
            String currentTime = LocalDateTime.now().format(formatter);
            existingCustomer.setUpdatedAt(currentTime);
            
            CustomerDocument savedCustomer = customerRepository.save(existingCustomer);
            if (savedCustomer == null) {
                return new Result(ResultCode.R_Fail);
            }
            return new Result(ResultCode.R_Ok); 
        } catch (Exception e) {
            logUtil.error("Error updating customer: " + e.getMessage());
            return new Result(ResultCode.R_Error);
        }
    }

    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result deleteCustomer(CustomerDocument customerDocument) {
        if (customerDocument == null || customerDocument.getCustomerId().trim().isEmpty()) {
            logUtil.error("客户ID不能为空");
            return new Result(ResultCode.R_ParamError);
        }
        String customerId = customerDocument.getCustomerId();
        // 获取用户ID和角色
        String userId = ThreadLocalUtil.getUserId();
        Integer userRole = ThreadLocalUtil.getUserRole();
        if (userId == null || userRole == null) {
            return new Result(ResultCode.R_UserNotFound);
        }

        // 检查客户是否存在
        CustomerDocument existingCustomer = customerRepository.findById(customerId).orElse(null);
        if (existingCustomer == null) {
            logUtil.error("客户不存在: " + customerId);
            return new Result(ResultCode.R_CustomerNotFound);
        }

        // 如果是小管理（角色为3），需要验证权限
        if (userRole.equals(UserConstData.ROLE_ADMIN_SMALL)) {
            // 检查是否为创建人
            boolean isCreator = userId.equals(existingCustomer.getCreatorId());
            
            // 检查创建人和所属用户是否都是下属
            ValidationResult creatorValidation = subordinateValidation.isSubordinate(userId, existingCustomer.getCreatorId());
            ValidationResult belongValidation = subordinateValidation.isSubordinate(userId, existingCustomer.getBelongUserId());
            boolean hasSubordinate = belongValidation.isValid() && creatorValidation.isValid();
            
            // 如果既不是创建人，也不是下属，则无权删除
            if (!isCreator && !hasSubordinate) {
                return new Result(ResultCode.R_NoAuth);
            }
        }
        // 如果是普通用户（角色为4），需要验证权限
        if (userRole.equals(UserConstData.ROLE_USER)) {
            if (!userId.equals(existingCustomer.getBelongUserId()) || !userId.equals(existingCustomer.getCreatorId())) {
                logUtil.error("无权删除非本人创建或所属的客户");
                return new Result(ResultCode.R_NoAuth);
            }
        }

        try {
            customerRepository.deleteByCustomerId(customerId);
            customerAssignRepository.deleteByCustomerId(customerId);
            return new Result(ResultCode.R_Ok);
        } catch (Exception e) {
            logUtil.error("Error deleting customer: " + e.getMessage());
            return new Result(ResultCode.R_Error);
        }
    }

    @Override
    public Result filterCustomer(CustomerFilterRequest request) {
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

        String belongUserName = request.getBelongUserName();
        if(belongUserName != null && !belongUserName.trim().isEmpty()){
            belongUserName = belongUserName.trim();
        }
        try {
            // 创建分页对象
            Pageable pageable = PageRequest.of(
                request.getPageNum() - 1,
                request.getPageSize()
            );

            // 创建查询构建器
            BoolQuery.Builder mainQuery = new BoolQuery.Builder();
            NativeQuery searchQuery = null;

            // 所属用户为公司，则直接查询所有与公司相关的客户
            if(belongUserName != null && belongUserName.equals(UserConstData.COMPANY_USER_NAME)) {
                mainQuery.must(m -> m.term(t -> t.field("belong_user_id").value(currentUserId)));
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

                // 客户名称条件
                if (request.getCustomerName() != null && !request.getCustomerName().trim().isEmpty()) {
                    mainQuery.must(m -> m
                            .match(t -> t
                                    .field("customer_name")
                                    .query(request.getCustomerName().trim())
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

                // 客户等级条件
                if (request.getCustomerLevel() != null) {
                    mainQuery.must(m -> m
                            .term(t -> t
                                    .field("customer_level")
                                    .value(request.getCustomerLevel())
                            )
                    );
                }

                // 客户状态条件
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
                    if (!request.getBirth().matches("^\\d{4}-\\d{2}-\\d{2}$")) {
                        logUtil.error("出生日期格式错误，应为yyyy-MM-dd格式");
                        return new Result(ResultCode.R_ParamError);
                    }

                    // Convert to ISO format
                    DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                    LocalDate date = LocalDate.parse(request.getBirth(), inputFormatter);
                    String isoDate = date.atStartOfDay(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT);
                    mainQuery.must(m -> m
                            .term(t -> t
                                    .field("birth")
                                    .value(isoDate)
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
                if (request.getCustomerCountryId() != null && !request.getCustomerCountryId().trim().isEmpty()) {
                    mainQuery.must(m -> m
                            .term(t -> t
                                    .field("customer_country_id")
                                    .value(request.getCustomerCountryId().trim())
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
                SearchHits<CustomerDocument> searchHits = elasticsearchOperations.search(searchQuery, CustomerDocument.class);
                
                // 获取当前页的数据
                List<CustomerDocument> customers = searchHits.getSearchHits().stream()
                    .map(SearchHit::getContent)
                    .collect(Collectors.toList());

                return new Result(
                    ResultCode.R_Ok, 
                    new PageResponse<>(
                        searchHits.getTotalHits(),
                        request.getPageNum(),
                        request.getPageSize(),
                        convertToResponseFormat(customers)
                    )
                );
            }
            if(userRole.equals(UserConstData.ROLE_ADMIN_LARGE)){
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

                // 所属用户条件
                if (request.getBelongUserName() != null && !request.getBelongUserName().trim().isEmpty()) {
                    List<UserDocument> belongUsers = userRepository.findByUserNameLike(request.getBelongUserName().trim());
                    if (!belongUsers.isEmpty()) {
                        BoolQuery.Builder belongQuery = new BoolQuery.Builder();
                        for (UserDocument user : belongUsers) {
                            belongQuery.should(s -> s
                                    .term(t -> t
                                            .field("belong_user_id")
                                            .value(user.getUserId())
                                    )
                            );
                        }
                        mainQuery.must(m -> m.bool(belongQuery.build()));
                    }
                }

                // 客户名称条件
                if (request.getCustomerName() != null && !request.getCustomerName().trim().isEmpty()) {
                    mainQuery.must(m -> m
                            .match(t -> t
                                    .field("customer_name")
                                    .query(request.getCustomerName().trim())
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

                // 客户等级条件
                if (request.getCustomerLevel() != null && request.getCustomerLevel() >= ReceiverConstData.CUSTOMER_LEVEL_LOW && request.getCustomerLevel() <= ReceiverConstData.CUSTOMER_LEVEL_HIGH) {
                    mainQuery.must(m -> m
                            .term(t -> t
                                    .field("customer_level")
                                    .value(request.getCustomerLevel())
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
                if (request.getCustomerCountryId() != null && !request.getCustomerCountryId().trim().isEmpty()) {
                    mainQuery.must(m -> m
                            .term(t -> t
                                    .field("customer_country_id")
                                    .value(request.getCustomerCountryId().trim())
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
                        if (!request.getBirth().matches("^\\d{4}-\\d{2}-\\d{2}$")) {
                            logUtil.error("出生日期格式错误，应为yyyy-MM-dd格式");
                            return new Result(ResultCode.R_ParamError);
                        }

                        // Convert to ISO format
                        DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                        LocalDate date = LocalDate.parse(request.getBirth(), inputFormatter);
                        String isoDate = date.atStartOfDay(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT);
                        mainQuery.must(m -> m
                                .term(t -> t
                                        .field("birth")
                                        .value(isoDate)
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
                if (request.getStatus() != null && (request.getStatus() == MagicMathConstData.CUSTOMER_STATUS_ASSIGNED || request.getStatus() == MagicMathConstData.CUSTOMER_STATUS_UNASSIGNED)) {
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

                // 如果创建者和所属用户都没有指定，则添加必要权限过滤
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

                // 客户名称条件
                if (request.getCustomerName() != null && !request.getCustomerName().trim().isEmpty()) {
                    mainQuery.must(m -> m
                            .match(t -> t
                                    .field("customer_name")
                                    .query(request.getCustomerName().trim())
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

                // 客户等级条件
                if (request.getCustomerLevel() != null && request.getCustomerLevel() >= ReceiverConstData.CUSTOMER_LEVEL_LOW && request.getCustomerLevel() <= ReceiverConstData.CUSTOMER_LEVEL_HIGH) {
                    mainQuery.must(m -> m
                            .term(t -> t
                                    .field("customer_level")
                                    .value(request.getCustomerLevel())
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
                if (request.getCustomerCountryId() != null && !request.getCustomerCountryId().trim().isEmpty()) {
                    mainQuery.must(m -> m
                            .term(t -> t
                                    .field("customer_country_id")
                                    .value(request.getCustomerCountryId().trim())
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
                        if (!request.getBirth().matches("^\\d{4}-\\d{2}-\\d{2}$")) {
                            logUtil.error("出生日期格式错误，应为yyyy-MM-dd格式");
                            return new Result(ResultCode.R_ParamError);
                        }

                        // Convert to ISO format
                        DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                        LocalDate date = LocalDate.parse(request.getBirth(), inputFormatter);
                        String isoDate = date.atStartOfDay(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT);
                        mainQuery.must(m -> m
                                .term(t -> t
                                        .field("birth")
                                        .value(isoDate)
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
                if (request.getStatus() != null && (request.getStatus() == MagicMathConstData.CUSTOMER_STATUS_ASSIGNED || request.getStatus() == MagicMathConstData.CUSTOMER_STATUS_UNASSIGNED)) {
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
                // 普通用户必须查询自己的客户
                BoolQuery.Builder belongQuery = new BoolQuery.Builder();
                belongQuery.should(s -> s
                            .term(t -> t
                                .field("belong_user_id")
                                .value(currentUserId)
                            )
                        );
                // 添加所属用户条件
                mainQuery.must(m -> m.bool(belongQuery.build()));
                
                // 客户名称条件
                if (request.getCustomerName() != null && !request.getCustomerName().trim().isEmpty()) {
                    mainQuery.must(m -> m
                            .match(t -> t
                                    .field("customer_name")
                                    .query(request.getCustomerName().trim())
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

                // 客户等级条件
                if (request.getCustomerLevel() != null && request.getCustomerLevel() >= ReceiverConstData.CUSTOMER_LEVEL_LOW && request.getCustomerLevel() <= ReceiverConstData.CUSTOMER_LEVEL_HIGH) {
                    mainQuery.must(m -> m
                            .term(t -> t
                                    .field("customer_level")
                                    .value(request.getCustomerLevel())
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
                if (request.getCustomerCountryId() != null && !request.getCustomerCountryId().trim().isEmpty()) {
                    mainQuery.must(m -> m
                            .term(t -> t
                                    .field("customer_country_id")
                                    .value(request.getCustomerCountryId().trim())
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
                        if (!request.getBirth().matches("^\\d{4}-\\d{2}-\\d{2}$")) {
                            logUtil.error("出生日期格式错误，应为yyyy-MM-dd格式");
                            return new Result(ResultCode.R_ParamError);
                        }

                        // Convert to ISO format
                        DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                        LocalDate date = LocalDate.parse(request.getBirth(), inputFormatter);
                        String isoDate = date.atStartOfDay(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT);
                        mainQuery.must(m -> m
                                .term(t -> t
                                        .field("birth")
                                        .value(isoDate)
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
            SearchHits<CustomerDocument> searchHits = elasticsearchOperations.search(searchQuery, CustomerDocument.class);
            
            // 获取当前页的数据
            List<CustomerDocument> customers = searchHits.getSearchHits().stream()
                .map(SearchHit::getContent)
                .collect(Collectors.toList());

            return new Result(
                ResultCode.R_Ok, 
                new PageResponse<>(
                    searchHits.getTotalHits(),
                    request.getPageNum(),
                    request.getPageSize(),
                    convertToResponseFormat(customers)
                )
            );
        } catch (Exception e) {
            logUtil.error("Error filtering customers: " + e.getMessage());
            return new Result(ResultCode.R_Error);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result assignCustomer(CustomerDocument customerDocument) {
        // 参数校验
        if (customerDocument == null || 
            customerDocument.getCustomerId() == null || 
            customerDocument.getCustomerId().trim().isEmpty() ||
            customerDocument.getBelongUserId() == null || 
            customerDocument.getBelongUserId().trim().isEmpty()) {
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
            // 检查客户是否存在
            CustomerDocument existingCustomer = customerRepository.findById(customerDocument.getCustomerId()).orElse(null);
            if (existingCustomer == null) {
                logUtil.error("客户不存在: " + customerDocument.getCustomerId());
                return new Result(ResultCode.R_CustomerNotFound);
            }

            // 检查目标用户是否存在
            UserDocument targetUser = userRepository.findById(customerDocument.getBelongUserId()).orElse(null);
            if (targetUser == null) {
                logUtil.error("目标用户不存在: " + customerDocument.getBelongUserId());
                return new Result(ResultCode.R_UserNotFound);
            }
            String targetUserName = targetUser.getUserName();
            if (targetUserName == null) {
                return new Result(ResultCode.R_UserNotFound);
            }

            // 权限检查
            if (userRole.equals(UserConstData.ROLE_ADMIN_SMALL)) {
                if (!customerDocument.getBelongUserId().equals(UserConstData.COMPANY_USER_ID)) {
                // 小管理员只能分配给自己的下属和自己
                boolean targetValidation = subordinateValidation.isSubordinateOrSelf(
                    customerDocument.getBelongUserId(), 
                    currentUserId
                    );
                    if (!targetValidation) {
                        logUtil.error("无权分配给非下属用户");
                        return new Result(ResultCode.R_NoAuth);
                    }
                }
            } else if (!userRole.equals(UserConstData.ROLE_ADMIN_LARGE)) {
                // 非管理员无权分配
                logUtil.error("无权分配客户");
                return new Result(ResultCode.R_NoAuth);
            }

            // 更新客户信息
            existingCustomer.setBelongUserId(customerDocument.getBelongUserId());
            existingCustomer.setStatus(MagicMathConstData.CUSTOMER_STATUS_ASSIGNED);
            
            // 更新时间
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
            String currentTime = LocalDateTime.now().format(formatter);
            existingCustomer.setUpdatedAt(currentTime);

            // 保存更新
            CustomerDocument savedCustomer = customerRepository.save(existingCustomer);
            if (savedCustomer == null) {
                return new Result(ResultCode.R_UpdateDbFailed);
            }
            String customerId = savedCustomer.getCustomerId();
            String belongUserId = savedCustomer.getBelongUserId();


            // 更新分配记录
            CustomerAssignDocument existingAssignment = customerAssignRepository.findById(customerId).orElse(null);
            Map<String, Object> process = new HashMap<>();
            process.put("assignor_id", currentUserId);
            process.put("assignor_name", currentUserName);
            process.put("assignee_id", belongUserId);
            process.put("assignee_name", targetUserName);
            process.put("assign_date", currentTime);

            if (existingAssignment == null) {
                existingAssignment = new CustomerAssignDocument();
                existingAssignment.setCustomerId(customerId);
                existingAssignment.setAssignProcess(new ArrayList<>());
                existingAssignment.getAssignProcess().add(process);
            } else {
                existingAssignment.getAssignProcess().add(0, process);
            }
            customerAssignRepository.save(existingAssignment);
            return new Result(ResultCode.R_Ok);
        } catch (Exception e) {
            logUtil.error("Error assigning customer: " + e.getMessage());
            return new Result(ResultCode.R_Error);
        }
    }

    @Override
    public Result assignCustomerDetails(Map<String, Object> params) {
        // 参数校验
        if (params == null || 
            params.get("customer_id") == null || 
            params.get("page_num") == null || 
            params.get("page_size") == null) {
            return new Result(ResultCode.R_ParamError);
        }

        String customerId = (String) params.get("customer_id");
        Integer pageNum = (Integer) params.get("page_num");
        Integer pageSize = (Integer) params.get("page_size");

        if (customerId == null || customerId.trim().isEmpty()) {
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
            // 检查客户是否存在
            CustomerDocument customer = customerRepository.findById(customerId).orElse(null);
            if (customer == null) {
                logUtil.error("客户不存在: " + customerId);
                return new Result(ResultCode.R_CustomerNotFound);
            }

            // 获取分配历史
            CustomerAssignDocument assignDocument = customerAssignRepository.findById(customerId).orElse(null);
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
            logUtil.error("Error getting customer assign details: " + e.getMessage());
            return new Result(ResultCode.R_Error);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result allAssignCustomer(Map<String, Object> params) {
        // 参数校验
        if (params == null || 
            params.get("customer_id") == null || 
            params.get("belong_user_id") == null) {
            return new Result(ResultCode.R_ParamError);
        }

        // 获取参数
        List<String> customerIds;
        try {
            customerIds = (List<String>) params.get("customer_id");
        } catch (ClassCastException e) {
            logUtil.error("客户ID列表格式错误");
            return new Result(ResultCode.R_ParamError);
        }
        String belongUserId = (String) params.get("belong_user_id");

        // 参数有效性校验
        if (customerIds.isEmpty() || belongUserId == null || belongUserId.trim().isEmpty()) {
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
                logUtil.error("无权分配客户");
                return new Result(ResultCode.R_NoAuth);
            }

            // 获取当前时间
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
            String currentTime = LocalDateTime.now().format(formatter);

            // 批量更新客户
            for (String customerId : customerIds) {
                // 检查客户是否存在
                CustomerDocument existingCustomer = customerRepository.findById(customerId).orElse(null);
                if (existingCustomer == null) {
                    logUtil.error("客户不存在: " + customerId);
                    continue;
                }

                // 更新客户信息
                existingCustomer.setBelongUserId(belongUserId);
                existingCustomer.setStatus(MagicMathConstData.CUSTOMER_STATUS_ASSIGNED);
                existingCustomer.setUpdatedAt(currentTime);

                // 保存更新
                CustomerDocument savedCustomer = customerRepository.save(existingCustomer);
                if (savedCustomer == null) {
                    continue;
                }

                // 更新分配记录
                CustomerAssignDocument existingAssignment = customerAssignRepository.findById(customerId).orElse(null);
                Map<String, Object> process = new HashMap<>();
                process.put("assignor_id", currentUserId);
                process.put("assignor_name", currentUserName);
                process.put("assignee_id", belongUserId);
                process.put("assignee_name", targetUserName);
                process.put("assign_date", currentTime);

                if (existingAssignment == null) {
                    existingAssignment = new CustomerAssignDocument();
                    existingAssignment.setCustomerId(customerId);
                    existingAssignment.setAssignProcess(new ArrayList<>());
                    existingAssignment.getAssignProcess().add(process);
                } else {
                    existingAssignment.getAssignProcess().add(0, process);
                }
                customerAssignRepository.save(existingAssignment);
            }

            return new Result(ResultCode.R_Ok);
        } catch (Exception e) {
            logUtil.error("Error assigning customers: " + e.getMessage());
            return new Result(ResultCode.R_Error);
        }
    }

    @Override
    public Result importCustomer(MultipartFile file) {
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
            List<CustomerDocument> customers = new ArrayList<>();
            
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",");
                CustomerDocument customer = new CustomerDocument();
                
                // 设置基本信息
                customer.setCustomerName(data[0]);
                customer.setContactPerson(data[1]);
                customer.setContactWay(data[2]);
                customer.setCustomerLevel(Integer.parseInt(data[3]));
                
                // 通过国家名称查询国家ID
                String countryName = data[4];
                CountryDocument country = countryRepository.findByCountryName(countryName);
                if (country != null) {
                    customer.setCustomerCountryId(country.getCountryId());
                }
                if (country == null) {
                    logUtil.error("国家不存在: " + countryName);
                    continue;
                }
                
                customer.setTradeType(Integer.parseInt(data[5]));
                
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
                customer.setCommodityId(commodityIds);
                
                customer.setSex(data[7]);
                
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
                        customer.setBirth(isoDate);
                    } catch (DateTimeParseException e) {
                        logUtil.error("日期格式错误，应为yyyy/MM/dd格式：" + birthDate);
                        continue;
                    }
                }
                
                // 处理邮箱列表
                String[] emails = data[9].split(";");
                customer.setEmails(Arrays.asList(emails));
                
                // 设置接受的邮件类型ID列表
                List<String> emailTypeIds = StreamSupport.stream(emailTypeRepository.findAll().spliterator(), false)
                        .map(EmailTypeDocument::getEmailTypeId)
                        .collect(Collectors.toList());
                customer.setAcceptEmailTypeId(emailTypeIds);

                // 设置用户相关字段
                String userId = ThreadLocalUtil.getUserId();
                customer.setBelongUserId(userId);
                customer.setCreatorId(userId);

                // 设置状态为已分配(2)
                customer.setStatus(MagicMathConstData.CUSTOMER_STATUS_ASSIGNED);

                // 生成客户ID
                String customerId = UUID.randomUUID().toString();
                customer.setCustomerId(customerId);

                // 设置创建和更新时间
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
                String currentTime = LocalDateTime.now().format(formatter);
                customer.setCreatedAt(currentTime);
                customer.setUpdatedAt(currentTime);
                
                customers.add(customer);
            }
            
            // 批量保存到ES
            customerRepository.saveAll(customers);
            
            return new Result(ResultCode.R_Ok, customers.size());
            
        } catch (IOException e) {
            logUtil.error("CSV文件读取失败", e);
            return new Result(ResultCode.R_Error, "文件处理失败：" + e.getMessage());
        } catch (Exception e) {
            logUtil.error("数据处理失败", e);
            return new Result(ResultCode.R_Error, "数据处理失败：" + e.getMessage());
        }
    }

    private List<Map<String, Object>> convertToResponseFormat(List<CustomerDocument> customers) {
        if (customers == null || customers.isEmpty()) {
            return new ArrayList<>();
        }

        return customers.stream()
                .map(customer -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("customer_id", customer.getCustomerId());
                    map.put("customer_name", customer.getCustomerName());
                    
                    // Get creator name from creator id
                    UserDocument creator = userRepository.findById(customer.getCreatorId()).orElse(null);
                    map.put("creator_name", creator != null ? creator.getUserName() : "");
                    
                    // Get belong user name from belong user id
                    UserDocument belongUser = userRepository.findById(customer.getBelongUserId()).orElse(null);
                    map.put("belong_user_name", belongUser != null ? belongUser.getUserName() : "");
                    
                    map.put("contact_person", customer.getContactPerson());
                    map.put("contact_way", customer.getContactWay());
                    map.put("customer_level", customer.getCustomerLevel());
                    map.put("trade_type", customer.getTradeType());
                    
                    // Get country name from country id
                    CountryDocument country = countryRepository.findById(customer.getCustomerCountryId()).orElse(null);
                    map.put("customer_country_name", country != null ? country.getCountryName() : "");
                    
                    // Get commodity names from commodity ids
                    List<String> commodityNames = new ArrayList<>();
                    if (customer.getCommodityId() != null) {
                        for (String commodityId : customer.getCommodityId()) {
                            CommodityDocument commodity = commodityRepository.findById(commodityId).orElse(null);
                            if (commodity != null) {
                                commodityNames.add(commodity.getCommodityName());
                            }
                        }
                    }
                    map.put("commodity_name", commodityNames);
                    
                    map.put("sex", customer.getSex());
                    map.put("birth", customer.getBirth());
                    map.put("emails", customer.getEmails());
                    
                    // Get email type names from email type ids
                    List<String> emailTypeNames = new ArrayList<>();
                    if (customer.getAcceptEmailTypeId() != null) {
                        for (String emailTypeId : customer.getAcceptEmailTypeId()) {
                            EmailTypeDocument emailType = emailTypeRepository.findById(emailTypeId).orElse(null);
                            if (emailType != null) {
                                emailTypeNames.add(emailType.getEmailTypeName());
                            }
                        }
                    }
                    map.put("accept_email_type_name", emailTypeNames);
                    
                    map.put("status", customer.getStatus());
                    map.put("created_at", customer.getCreatedAt());
                    map.put("updated_at", customer.getUpdatedAt());
                    
                    return map;
                })
                .collect(Collectors.toList());
    }
} 