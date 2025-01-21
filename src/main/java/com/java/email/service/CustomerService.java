package com.java.email.service;

import com.java.email.common.Result;
import com.java.email.entity.Customer;
import com.java.email.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.UUID;

@Service
public class CustomerService {
    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 根据条件筛选客户
     *
     * @param belongUserId  所属用户ID
     * @param customerLevel 客户等级
     * @param customerName  客户名称
     * @param status        分配状态
     * @param tradeType     贸易类型
     * @param page          页码
     * @param size          每页大小
     * @return 符合条件的客户分页结果
     */
    public Result<Page<Customer>> findCustomersByCriteria(
            String belongUserId, Integer customerLevel, String customerName, Integer status, Integer tradeType,
            int page, int size,
            String currentUserId, int currentUserRole) { // 新增当前用户ID和角色参数
        try {
            Page<Customer> customers;

            // 创建分页对象
            Pageable pageable = PageRequest.of(page, size);

            // 根据用户角色动态构建查询条件
            if (currentUserRole == 4) { // 普通用户
                // 只能查看 belongUserid 是自己的或公司的客户
                if (belongUserId != null && !belongUserId.equals(currentUserId) && !belongUserId.equals("1")) {
                    // 如果 belongUserId 不是当前用户也不是公司，返回空结果
                    return Result.success(Page.empty(pageable));
                }

                // 动态构建查询条件
                if (customerLevel != null) {
                    customers = customerRepository.findByCustomerLevelAndBelongUserIdIn(customerLevel, Arrays.asList(currentUserId, "1"), pageable);
                } else if (customerName != null) {
                    customers = customerRepository.findByCustomerNameAndBelongUserIdIn(customerName, Arrays.asList(currentUserId, "1"), pageable);
                } else if (status != null) {
                    customers = customerRepository.findByStatusAndBelongUserIdIn(status, Arrays.asList(currentUserId, "1"), pageable);
                } else if (tradeType != null) {
                    customers = customerRepository.findByTradeTypeAndBelongUserIdIn(tradeType, Arrays.asList(currentUserId, "1"), pageable);
                } else {
                    // 如果没有条件，返回 belongUserid 是自己的或公司的客户
                    customers = customerRepository.findByBelongUserIdIn(Arrays.asList(currentUserId, "1"), pageable);
                }
            } else if (currentUserRole == 2) { // 大管理
                // 可以查看所有客户
                if (belongUserId != null) {
                    customers = customerRepository.findByBelongUserId(belongUserId, pageable);
                } else if (customerLevel != null) {
                    customers = customerRepository.findByCustomerLevel(customerLevel, pageable);
                } else if (customerName != null) {
                    customers = customerRepository.findByCustomerName(customerName, pageable);
                } else if (status != null) {
                    customers = customerRepository.findByStatus(status, pageable);
                } else if (tradeType != null) {
                    customers = customerRepository.findByTradeType(tradeType, pageable);
                } else {
                    // 如果没有条件，返回所有客户
                    customers = customerRepository.findAll(pageable);
                }
            } else {
                // 其他角色，返回空结果
                return Result.success(Page.empty(pageable));
            }

            // 返回成功结果
            return Result.success(customers);
        } catch (Exception e) {
            // 返回错误结果
            return Result.error("查询客户失败: " + e.getMessage());
        }
    }
//    public Result<Page<Customer>> findCustomersByCriteria(String belongUserId, Integer customerLevel,
//                                                          String customerName, Integer status, Integer tradeType,
//                                                          int page, int size) {
//        try {
//            Page<Customer> customers;
//
//            // 创建分页对象
//            Pageable pageable = PageRequest.of(page, size);
//
//            // 动态构建查询条件
//            if (belongUserId != null) {
//                customers = customerRepository.findByBelongUserId(belongUserId, pageable);
//            } else if (customerLevel != null) {
//                customers = customerRepository.findByCustomerLevel(customerLevel, pageable);
//            } else if (customerName != null) {
//                customers = customerRepository.findByCustomerName(customerName, pageable);
//            } else if (status != null) {
//                customers = customerRepository.findByStatus(status, pageable);
//            } else if (tradeType != null) {
//                customers = customerRepository.findByTradeType(tradeType, pageable);
//            } else {
//                // 如果没有条件，返回所有客户（分页）
//                customers = customerRepository.findAll(pageable);
//            }
//
//            // 返回成功结果
//            return Result.success(customers);
//        } catch (Exception e) {
//            // 返回错误结果
//            return Result.error("查询客户失败: " + e.getMessage());
//        }
//    }

    /**
     * 根据条件筛选客户并存入redis中，返回redisKey
     *
     * @param belongUserId  所属用户ID
     * @param customerLevel 客户等级
     * @param customerName  客户名称
     * @param status        分配状态
     * @param tradeType     贸易类型
     * @param page          页码
     * @param size          每页大小
     * @return 符合条件的客户分页结果
     */
    public Result<String> findCustomersByCriteriaRedis(
            String belongUserId, Integer customerLevel, String customerName, Integer status, Integer tradeType,
            int page, int size,
            String currentUserId, int currentUserRole) { // 新增当前用户ID和角色参数
        try {
            Page<Customer> customers;

            // 创建分页对象
            Pageable pageable = PageRequest.of(page, size);

            // 根据用户角色动态构建查询条件
            if (currentUserRole == 4) { // 普通用户
                // 只能查看 belongUserid 是自己的或公司的客户
                if (belongUserId != null && !belongUserId.equals(currentUserId) && !belongUserId.equals("1")) {
                    // 如果 belongUserId 不是当前用户也不是公司，返回空结果
                    return Result.success("customer:search:empty"); // 返回一个空的 Redis Key
                }

                // 动态构建查询条件
                if (customerLevel != null) {
                    customers = customerRepository.findByCustomerLevelAndBelongUserIdIn(customerLevel, Arrays.asList(currentUserId, "1"), pageable);
                } else if (customerName != null) {
                    customers = customerRepository.findByCustomerNameAndBelongUserIdIn(customerName, Arrays.asList(currentUserId, "1"), pageable);
                } else if (status != null) {
                    customers = customerRepository.findByStatusAndBelongUserIdIn(status, Arrays.asList(currentUserId, "1"), pageable);
                } else if (tradeType != null) {
                    customers = customerRepository.findByTradeTypeAndBelongUserIdIn(tradeType, Arrays.asList(currentUserId, "1"), pageable);
                } else {
                    // 如果没有条件，返回 belongUserid 是自己的或公司的客户
                    customers = customerRepository.findByBelongUserIdIn(Arrays.asList(currentUserId, "1"), pageable);
                }
            } else if (currentUserRole == 2) { // 大管理
                // 可以查看所有客户
                if (belongUserId != null) {
                    customers = customerRepository.findByBelongUserId(belongUserId, pageable);
                } else if (customerLevel != null) {
                    customers = customerRepository.findByCustomerLevel(customerLevel, pageable);
                } else if (customerName != null) {
                    customers = customerRepository.findByCustomerName(customerName, pageable);
                } else if (status != null) {
                    customers = customerRepository.findByStatus(status, pageable);
                } else if (tradeType != null) {
                    customers = customerRepository.findByTradeType(tradeType, pageable);
                } else {
                    // 如果没有条件，返回所有客户
                    customers = customerRepository.findAll(pageable);
                }
            } else {
                // 其他角色，返回空结果
                return Result.success("customer:search:empty"); // 返回一个空的 Redis Key
            }

            // 生成唯一的 Redis Key
            String redisKey = "customer:search:" + UUID.randomUUID().toString();

            // 设置 Redis 的 Value 序列化器为 JSON 格式
            redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());

            // 将查询结果的内容（List<Customer>）存入 Redis
            redisTemplate.opsForValue().set(redisKey, customers.getContent());

            // 返回 Redis Key
            return Result.success(redisKey);
        } catch (Exception e) {
            // 返回错误结果
            return Result.error("查询客户失败: " + e.getMessage());
        }
    }
//    public Result<String> findCustomersByCriteriaRedis(String belongUserId, Integer customerLevel,
//                                                       String customerName, Integer status, Integer tradeType,
//                                                       int page, int size) {
//        try {
//            Page<Customer> customers;
//
//            // 创建分页对象
//            Pageable pageable = PageRequest.of(page, size);
//
//            // 动态构建查询条件
//            if (belongUserId != null) {
//                customers = customerRepository.findByBelongUserId(belongUserId, pageable);
//            } else if (customerLevel != null) {
//                customers = customerRepository.findByCustomerLevel(customerLevel, pageable);
//            } else if (customerName != null) {
//                customers = customerRepository.findByCustomerName(customerName, pageable);
//            } else if (status != null) {
//                customers = customerRepository.findByStatus(status, pageable);
//            } else if (tradeType != null) {
//                customers = customerRepository.findByTradeType(tradeType, pageable);
//            } else {
//                // 如果没有条件，返回所有客户（分页）
//                customers = customerRepository.findAll(pageable);
//            }
//
//            // 生成唯一的 Redis Key
//            String redisKey = "customer:search:" + UUID.randomUUID().toString();
//
//            // 设置 Redis 的 Value 序列化器为 JSON 格式
//            redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());
//
//            // 将查询结果的内容（List<Customer>）存入 Redis，设置过期时间为 10 分钟
//            redisTemplate.opsForValue().set(redisKey, customers.getContent());
//
//            // 返回 Redis Key
//            return Result.success(redisKey);
//        } catch (Exception e) {
//            // 返回错误结果
//            return Result.error("查询客户失败: " + e.getMessage());
//        }
//    }
}