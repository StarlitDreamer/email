package com.java.email.service.impl.receiver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.java.email.common.Response.PageResponse;
import com.java.email.common.Response.Result;
import com.java.email.common.Response.ResultCode;
import com.java.email.common.userCommon.SubordinateValidation;
import com.java.email.common.userCommon.ThreadLocalUtil;
import com.java.email.constant.UserConstData;
import com.java.email.esdao.repository.receiver.SupplierRepository;
import com.java.email.esdao.repository.user.UserRepository;
import com.java.email.esdao.repository.receiver.SupplierAssignRepository;
import com.java.email.esdao.repository.receiver.CustomerRepository;
import com.java.email.esdao.repository.dictionary.CategoryRepository;
import com.java.email.esdao.repository.receiver.CustomerAssignRepository;
import com.java.email.model.entity.dictionary.CategoryDocument;
import com.java.email.model.entity.dictionary.CommodityDocument;
import com.java.email.model.entity.receiver.CustomerAssignDocument;
import com.java.email.model.entity.receiver.CustomerDocument;
import com.java.email.model.entity.receiver.SupplierAssignDocument;
import com.java.email.model.entity.receiver.SupplierDocument;
import com.java.email.model.entity.user.UserDocument;
import com.java.email.service.receiver.ReceiverService;
import com.java.email.utils.LogUtil;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class ReceiverServiceImpl implements ReceiverService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SubordinateValidation subordinateValidation;

    @Autowired
    private SupplierRepository supplierRepository;

    @Autowired
    private SupplierAssignRepository supplierAssignRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private CustomerAssignRepository customerAssignRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ElasticsearchOperations elasticsearchOperations;

    private static final LogUtil logUtil = LogUtil.getLogger(ReceiverServiceImpl.class);


    @Override
    public Result filterUser(Map<String, Object> request) {
        // 参数校验
        if (request == null) {
            return new Result(ResultCode.R_ParamError);
        }

        String userName = (String)request.get("user_name");
        if (userName == null || userName.trim().isEmpty()) {
            return new Result(ResultCode.R_ParamError);
        }

        // 获取当前用户信息
        String userId = ThreadLocalUtil.getUserId();
        Integer userRole = ThreadLocalUtil.getUserRole();
        if (userRole == null || !(userRole.equals(UserConstData.ROLE_ADMIN_LARGE) || userRole.equals(UserConstData.ROLE_ADMIN_SMALL))) {
            return new Result(ResultCode.R_NoAuth);
        }

        // 如果查询的是公司用户，直接返回公司用户信息
        if (userName.equals(UserConstData.COMPANY_USER_NAME)) {
            Map<String, Object> userMap = new HashMap<>();
            userMap.put("user_id", UserConstData.COMPANY_USER_ID);
            userMap.put("user_name", UserConstData.COMPANY_USER_NAME);
            return new Result(ResultCode.R_Ok, userMap);
        }

        // 创建用户列表
        List<Map<String, Object>> userList = new ArrayList<>();

        // 根据用户名模糊查询用户
        List<UserDocument> users = userRepository.findByUserNameLike(userName.trim());

        for (UserDocument user : users) {
            // 如果是小管理，只返回自己和下属
            if (userRole.equals(UserConstData.ROLE_ADMIN_SMALL)) {
                if (!user.getBelongUserId().equals(userId) && !userId.equals(user.getUserId())) {
                    continue;
                }
            }

            Map<String, Object> userMap = new HashMap<>();
            userMap.put("user_id", user.getUserId());
            userMap.put("user_name", user.getUserName());
            userMap.put("user_account", user.getUserAccount());
            userMap.put("user_email", user.getUserEmail());
            
            // 查询所属用户名称
            String belongUserId = user.getBelongUserId();
            if (belongUserId != null) {
                UserDocument belongUser = userRepository.findById(belongUserId).orElse(null);
                if (belongUser != null) {
                    userMap.put("belong_user_name", belongUser.getUserName());
                }
            }
            
            userList.add(userMap);
        }

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("user", userList);
        return new Result(ResultCode.R_Ok, resultMap);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result changeBelongUser(Map<String, Object> request) {
        // 参数校验
        if (request == null) {
            return new Result(ResultCode.R_ParamError);
        }

        String oldUserId = (String) request.get("old_user_id");
        String newUserId = (String) request.get("new_user_id");
        Integer receiverType = (Integer) request.get("receiver_type");

        if (oldUserId == null || oldUserId.trim().isEmpty() ||
            newUserId == null || newUserId.trim().isEmpty() ||
            receiverType == null || (receiverType != 1 && receiverType != 2)) {
            return new Result(ResultCode.R_ParamError);
        }

        // 获取当前用户信息
        String currentUserId = ThreadLocalUtil.getUserId();
        Integer userRole = ThreadLocalUtil.getUserRole();
        String currentUserName = ThreadLocalUtil.getUserName();
        if (currentUserId == null || userRole == null || currentUserName == null) {
            return new Result(ResultCode.R_UserNotFound);
        }
        // 权限检查
        if (!userRole.equals(UserConstData.ROLE_ADMIN_LARGE) && !userRole.equals(UserConstData.ROLE_ADMIN_SMALL)) {
            logUtil.error("无权更改所属用户");
            return new Result(ResultCode.R_NoAuth);
        }
        try {
            // 检查新旧用户是否存在
            UserDocument oldUser = userRepository.findById(oldUserId).orElse(null);
            UserDocument newUser = userRepository.findById(newUserId).orElse(null);
            if (oldUser == null || newUser == null) {
                logUtil.error("用户不存在");
                return new Result(ResultCode.R_UserNotFound);
            }
            if (oldUserId.equals(newUserId)) {
                return new Result(ResultCode.R_Ok);
            }
            // 旧用户不能是公司用户
            String oldUserName = oldUser.getUserName();
            if (oldUserName == null || oldUserName.trim().isEmpty()) {
                return new Result(ResultCode.R_UserNotFound);
            }
            if (oldUserId.equals(UserConstData.COMPANY_USER_ID) || oldUserName.equals(UserConstData.COMPANY_USER_NAME)) {
                logUtil.error("不能更改公司用户所属用户");
                return new Result(ResultCode.R_NoAuth);
            }
            // 小管理员只能操作自己的下属，但是新用户可以是公司
            if (userRole.equals(UserConstData.ROLE_ADMIN_SMALL)) {
                boolean oldUserValidation = subordinateValidation.isSubordinateOrSelf(oldUserId, currentUserId);
                if (!oldUserValidation) {
                    logUtil.error("无权操作非下属用户");
                    return new Result(ResultCode.R_NoAuth);
                }
                if (!newUserId.equals(UserConstData.COMPANY_USER_ID)) {
                    boolean newUserValidation = subordinateValidation.isSubordinateOrSelf(newUserId, currentUserId);
                    if (!newUserValidation) {
                        logUtil.error("无权操作非下属用户");
                        return new Result(ResultCode.R_NoAuth);
                    }
                }
            }

            // 获取当前时间
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
            String currentTime = LocalDateTime.now().format(formatter);

            if (receiverType == 1) {
                // 处理供应商
                List<SupplierDocument> suppliers = supplierRepository.findByBelongUserId(oldUserId);
                for (SupplierDocument supplier : suppliers) {
                    // 更新供应商信息
                    supplier.setBelongUserId(newUserId);
                    supplier.setUpdatedAt(currentTime);
                    supplierRepository.save(supplier);

                    String supplierId = supplier.getSupplierId();
                    if (supplierId == null || supplierId.trim().isEmpty()) {
                        continue;
                    }
                    String belongUserName = newUser.getUserName();
                    if (belongUserName == null || belongUserName.trim().isEmpty()) {
                        continue;
                    }
                    // 更新分配记录
                    SupplierAssignDocument assignDocument = supplierAssignRepository.findById(supplierId).orElse(null);
                    Map<String, Object> process = new HashMap<>();
                    process.put("assignor_id", currentUserId);
                    process.put("assignor_name", currentUserName);
                    process.put("assignee_id", newUserId);
                    process.put("assignee_name", belongUserName);
                    process.put("assign_date", currentTime);

                    if (assignDocument == null) {
                        assignDocument = new SupplierAssignDocument();
                        assignDocument.setSupplierId(supplierId);
                        assignDocument.setAssignProcess(new ArrayList<>());
                        assignDocument.getAssignProcess().add(process);
                    } else {
                        assignDocument.getAssignProcess().add(0, process);
                    }
                    supplierAssignRepository.save(assignDocument);
                }
            } else {
                // 处理客户
                List<CustomerDocument> customers = customerRepository.findByBelongUserId(oldUserId);
                for (CustomerDocument customer : customers) {
                    // 更新客户信息
                    customer.setBelongUserId(newUserId);
                    customer.setUpdatedAt(currentTime);
                    customerRepository.save(customer);

                    String customerId = customer.getCustomerId();
                    if (customerId == null || customerId.trim().isEmpty()) {
                        continue;
                    }
                    String belongUserName = newUser.getUserName();
                    if (belongUserName == null || belongUserName.trim().isEmpty()) {
                        continue;
                    }
                    // 更新分配记录
                    CustomerAssignDocument assignDocument = customerAssignRepository.findById(customerId).orElse(null);
                    Map<String, Object> process = new HashMap<>();
                    process.put("assignor_id", currentUserId);
                    process.put("assignor_name", currentUserName);
                    process.put("assignee_id", newUserId);
                    process.put("assignee_name", belongUserName);
                    process.put("assign_date", currentTime);

                    if (assignDocument == null) {
                        assignDocument = new CustomerAssignDocument();
                        assignDocument.setCustomerId(customerId);
                        assignDocument.setAssignProcess(new ArrayList<>());
                        assignDocument.getAssignProcess().add(process);
                    } else {
                        assignDocument.getAssignProcess().add(0, process);
                    }
                    customerAssignRepository.save(assignDocument);
                }
            }

            return new Result(ResultCode.R_Ok);
        } catch (Exception e) {
            logUtil.error("Error changing belong user: " + e.getMessage());
            return new Result(ResultCode.R_Error);
        }
    }

    @Override
    public Result getCategory() {
        try {
            // 获取所有分类
            Iterable<CategoryDocument> categories = categoryRepository.findAll();
            
            // 转换为前端需要的格式
            List<Map<String, Object>> resultList = new ArrayList<>();
            for (CategoryDocument category : categories) {
                Map<String, Object> map = new HashMap<>();
                map.put("id", category.getCategoryId());
                map.put("name", category.getCategoryName());
                resultList.add(map);
            }
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("category", resultList);

            return new Result(ResultCode.R_Ok, resultMap);
        } catch (Exception e) {
            logUtil.error("Error getting categories: " + e.getMessage());
            return new Result(ResultCode.R_Error);
        }
    }

    @Override
    public Result filterCommodity(Map<String, Object> request) {
        // 参数校验
        if (request == null) {
            return new Result(ResultCode.R_ParamError);
        }

        try {
            // 获取分页参数
            int pageNum = request.get("page_num") == null ? 1 : Integer.parseInt(request.get("page_num").toString());
            int pageSize = request.get("page_size") == null ? 10 : Integer.parseInt(request.get("page_size").toString());

            // 创建分页对象
            Pageable pageable = PageRequest.of(pageNum - 1, pageSize);

            // 创建查询构建器
            BoolQuery.Builder mainQuery = new BoolQuery.Builder();

            // 添加商品名称条件
            String commodityName = (String) request.get("commodity_name");
            if (commodityName != null && !commodityName.trim().isEmpty()) {
                mainQuery.must(m -> m
                    .match(t -> t
                        .field("commodityName")
                        .query(commodityName)
                    )
                );
            }

            // 添加分类ID条件
            String categoryId = (String) request.get("category_id");
            if (categoryId != null && !categoryId.trim().isEmpty()) {
                mainQuery.must(m -> m
                    .term(t -> t
                        .field("categoryId")
                        .value(categoryId)
                    )
                );
            }

            // 创建搜索查询
            NativeQuery searchQuery = NativeQuery.builder()
                .withQuery(mainQuery.build()._toQuery())
                .withSort(Sort.by(Sort.Direction.DESC, "createdAt"))
                .withPageable(pageable)
                .build();

            // 执行搜索
            SearchHits<CommodityDocument> searchHits = elasticsearchOperations.search(
                searchQuery,
                CommodityDocument.class
            );

            // 转换搜索结果
            List<Map<String, Object>> resultList = searchHits.getSearchHits().stream()
                .map(hit -> {
                    CommodityDocument commodity = hit.getContent();
                    Map<String, Object> map = new HashMap<>();
                    map.put("commodity_id", commodity.getCommodityId());
                    map.put("commodity_name", commodity.getCommodityName());
                    return map;
                })
                .collect(Collectors.toList());

            // 返回分页结果
            return new Result(
                ResultCode.R_Ok,
                new PageResponse<>(
                    searchHits.getTotalHits(),
                    pageNum,
                    pageSize,
                    resultList
                )
            );

        } catch (NumberFormatException e) {
            logUtil.error("页码格式错误: " + e.getMessage());
            return new Result(ResultCode.R_ParamError);
        } catch (Exception e) {
            logUtil.error("Error filtering commodities: " + e.getMessage());
            return new Result(ResultCode.R_Error);
        }
    }
}
