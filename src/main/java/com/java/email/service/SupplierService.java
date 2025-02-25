package com.java.email.service;

import com.java.email.common.Result;
import com.java.email.entity.Supplier;
import com.java.email.repository.SupplierRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class SupplierService {
    @Autowired
    private SupplierRepository supplierRepository;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 根据 supplierId 数组查询对应的 emails，并去除重复的 emails。
     *
     * @param supplierIds 供应商 ID 数组
     * @return 去重后的 emails 列表
     */
    public List<String> getUniqueEmailsBySupplierIds(List<String> supplierIds) {
        List<Supplier> suppliers = supplierRepository.findBySupplierIdIn(supplierIds);

        // 使用 HashSet 去重
        Set<String> uniqueEmails = new HashSet<>();

        for (Supplier supplier : suppliers) {
            uniqueEmails.addAll(supplier.getEmails());
        }

        return uniqueEmails.stream().collect(Collectors.toList());
    }




    /**
     * 根据条件筛选供应商
     *
     * @param ownerUserId   所属用户ID
     * @param supplierLevel 供应商等级
     * @param supplierName  供应商名称
     * @param status        分配状态
     * @param tradeType     贸易类型
     * @param page          页码
     * @param size          每页大小
     * @return 符合条件的供应商分页结果
     */
    public Result<Page<Supplier>> findSuppliersByCriteria(
            String ownerUserId, Integer supplierLevel, String supplierName, Integer status, Integer tradeType,
            int page, int size,
            String currentUserId, int currentUserRole) { // 新增当前用户ID和角色参数
        try {
            Page<Supplier> suppliers;

            // 创建分页对象
            Pageable pageable = PageRequest.of(page, size);

            // 根据用户角色动态构建查询条件
            if (currentUserRole == 4) { // 普通用户
                // 只能查看 belongUserid 是自己的或公司的供应商
                if (ownerUserId != null && !ownerUserId.equals(currentUserId) && !ownerUserId.equals("1")) {
                    // 如果 ownerUserId 不是当前用户也不是公司，返回空结果
                    return Result.success(Page.empty(pageable));
                }

                // 动态构建查询条件
                if (supplierLevel != null) {
                    suppliers = supplierRepository.findBySupplierLevelAndBelongUseridIn(supplierLevel, Arrays.asList(currentUserId, "1"), pageable);
                } else if (supplierName != null) {
                    suppliers = supplierRepository.findBySupplierNameAndBelongUseridIn(supplierName, Arrays.asList(currentUserId, "1"), pageable);
                } else if (status != null) {
                    suppliers = supplierRepository.findByStatusAndBelongUseridIn(status, Arrays.asList(currentUserId, "1"), pageable);
                } else if (tradeType != null) {
                    suppliers = supplierRepository.findByTradeTypeAndBelongUseridIn(tradeType, Arrays.asList(currentUserId, "1"), pageable);
                } else {
                    // 如果没有条件，返回 belongUserid 是自己的或公司的供应商
                    suppliers = supplierRepository.findByBelongUseridIn(Arrays.asList(currentUserId, "1"), pageable);
                }
            } else if (currentUserRole == 2) { // 大管理
                // 可以查看所有供应商
                if (ownerUserId != null) {
                    suppliers = supplierRepository.findByBelongUserid(ownerUserId, pageable);
                } else if (supplierLevel != null) {
                    suppliers = supplierRepository.findBySupplierLevel(supplierLevel, pageable);
                } else if (supplierName != null) {
                    suppliers = supplierRepository.findBySupplierName(supplierName, pageable);
                } else if (status != null) {
                    suppliers = supplierRepository.findByStatus(status, pageable);
                } else if (tradeType != null) {
                    suppliers = supplierRepository.findByTradeType(tradeType, pageable);
                } else {
                    // 如果没有条件，返回所有供应商
                    suppliers = supplierRepository.findAll(pageable);
                }
            } else {
                // 其他角色，返回空结果
                return Result.success(Page.empty(pageable));
            }

            // 返回成功结果
            return Result.success(suppliers);
        } catch (Exception e) {
            // 返回错误结果
            return Result.error("查询供应商失败: " + e.getMessage());
        }
    }

    /**
     * 根据条件筛选供应商，存入redis返回rediskey
     *
     * @param ownerUserId   所属用户ID
     * @param supplierLevel 供应商等级
     * @param supplierName  供应商名称
     * @param status        分配状态
     * @param tradeType     贸易类型
     * @param page          页码
     * @param size          每页大小
     * @return 符合条件的供应商分页结果
     */
    public Result<String> findSuppliersByCriteriaRedis(
            String ownerUserId, Integer supplierLevel, String supplierName, Integer status, Integer tradeType,
            int page, int size,
            String currentUserId, int currentUserRole) { // 新增当前用户ID和角色参数
        try {
            Page<Supplier> suppliers;

            // 创建分页对象
            Pageable pageable = PageRequest.of(page, size);

            // 根据用户角色动态构建查询条件
            if (currentUserRole == 4) { // 普通用户
                // 只能查看 belongUserid 是自己的或公司的供应商
                if (ownerUserId != null && !ownerUserId.equals(currentUserId) && !ownerUserId.equals("1")) {
                    // 如果 ownerUserId 不是当前用户也不是公司，返回空结果
                    return Result.success("supplier:search:empty"); // 返回一个空的 Redis Key
                }

                // 动态构建查询条件
                if (supplierLevel != null) {
                    suppliers = supplierRepository.findBySupplierLevelAndBelongUseridIn(supplierLevel, Arrays.asList(currentUserId, "1"), pageable);
                } else if (supplierName != null) {
                    suppliers = supplierRepository.findBySupplierNameAndBelongUseridIn(supplierName, Arrays.asList(currentUserId, "1"), pageable);
                } else if (status != null) {
                    suppliers = supplierRepository.findByStatusAndBelongUseridIn(status, Arrays.asList(currentUserId, "1"), pageable);
                } else if (tradeType != null) {
                    suppliers = supplierRepository.findByTradeTypeAndBelongUseridIn(tradeType, Arrays.asList(currentUserId, "1"), pageable);
                } else {
                    // 如果没有条件，返回 belongUserid 是自己的或公司的供应商
                    suppliers = supplierRepository.findByBelongUseridIn(Arrays.asList(currentUserId, "1"), pageable);
                }
            } else if (currentUserRole == 2) { // 大管理
                // 可以查看所有供应商
                if (ownerUserId != null) {
                    suppliers = supplierRepository.findByBelongUserid(ownerUserId, pageable);
                } else if (supplierLevel != null) {
                    suppliers = supplierRepository.findBySupplierLevel(supplierLevel, pageable);
                } else if (supplierName != null) {
                    suppliers = supplierRepository.findBySupplierName(supplierName, pageable);
                } else if (status != null) {
                    suppliers = supplierRepository.findByStatus(status, pageable);
                } else if (tradeType != null) {
                    suppliers = supplierRepository.findByTradeType(tradeType, pageable);
                } else {
                    // 如果没有条件，返回所有供应商
                    suppliers = supplierRepository.findAll(pageable);
                }
            } else {
                // 其他角色，返回空结果
                return Result.success("supplier:search:empty"); // 返回一个空的 Redis Key
            }

            // 生成唯一的 Redis Key
            String redisKey = "supplier:search:" + UUID.randomUUID().toString();

            // 设置 Redis 的 Value 序列化器为 JSON 格式
            redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());

            // 将查询结果的内容（List<Supplier>）存入 Redis，设置过期时间为 10 分钟
            redisTemplate.opsForValue().set(redisKey, suppliers.getContent(), 10, TimeUnit.MINUTES);

            // 返回 Redis Key
            return Result.success(redisKey);
        } catch (Exception e) {
            // 返回错误结果
            return Result.error("查询供应商失败: " + e.getMessage());
        }
    }


    public List<Supplier> getSuppliersFromRedis(String redisKey) {
        // 从 Redis 中读取数据
        Object result = redisTemplate.opsForValue().get(redisKey);

        if (result instanceof List) {
            // 反序列化为 List<Supplier>
            return (List<Supplier>) result;
        } else {
            throw new RuntimeException("Redis 中的数据格式不正确");
        }
    }
}
