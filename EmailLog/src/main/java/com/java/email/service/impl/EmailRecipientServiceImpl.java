package com.java.email.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.TermQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.TermsQuery;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.java.email.pojo.Customer;
import com.java.email.pojo.Supplier;
import com.java.email.service.EmailRecipientService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class EmailRecipientServiceImpl implements EmailRecipientService {

    private final ElasticsearchClient esClient;
    private static final String CUSTOMER_INDEX = "customer";
    private static final String SUPPLIER_INDEX = "supplier";
    private static final String USER_INDEX = "user";

    @Autowired
    public EmailRecipientServiceImpl(ElasticsearchClient esClient) {
        this.esClient = esClient;
    }

    /**
     * 根据邮箱获取用户详细信息
     * @param email 邮箱
     * @return Map包含用户类型和用户信息
     */

    @Override
    public Map<String, String> getRecipientDetail(String email) {
        try {
            Map<String, String> result = new HashMap<>();

            // 构建查询
            Query query = TermQuery.of(t -> t
                    .field("emails")
                    .value(email)
            )._toQuery();

            // 先查询Customer索引
            SearchResponse<Customer> customerResponse = esClient.search(s -> s
                            .index(CUSTOMER_INDEX)
                            .query(query)
                            .size(1),
                    Customer.class
            );

            // 如果在Customer中找到
            if (customerResponse.hits().total().value() > 0) {
                Customer customer = customerResponse.hits().hits().get(0).source();
                if (customer != null) {
                    result.put("name", customer.getCustomerName());
                    result.put("belongUserId", customer.getBelongUserid());
                    result.put("level", String.valueOf(customer.getCustomerLevel()));
                    result.put("birth", customer.getBirth());
                    return result;
                }
            }

            // 如果Customer中没找到，查询Supplier索引
            SearchResponse<Supplier> supplierResponse = esClient.search(s -> s
                            .index(SUPPLIER_INDEX)
                            .query(query)
                            .size(1),
                    Supplier.class
            );

            // 如果在Supplier中找到
            if (supplierResponse.hits().total().value() > 0) {
                Supplier supplier = supplierResponse.hits().hits().get(0).source();
                if (supplier != null) {
                    result.put("name", supplier.getSupplierName());
                    result.put("level", String.valueOf(supplier.getSupplierLevel()));
                    result.put("belongUserId", supplier.getBelongUserid());
                    result.put("birth", supplier.getBirth());
                    return result;
                }
            }

            log.info("未找到收件人信息: email={}", email);
            return null;

        } catch (Exception e) {
            log.error("获取收件人详细信息失败: email={}, error={}", email, e.getMessage(), e);
            return null;
        }
    }

    @Override
    public Map<String, String> getRecipientDetail(String email, String level) {
        try {
            Map<String, String> result = new HashMap<>();

            // 构建Customer查询
            BoolQuery.Builder customerBoolQuery = new BoolQuery.Builder()
                .must(TermQuery.of(t -> t
                    .field("emails")
                    .value(email)
                )._toQuery());

            // 如果level不为空，添加level查询条件
            if (level != null && !level.isEmpty()) {
                customerBoolQuery.must(TermQuery.of(t -> t
                    .field("customer_level")
                    .value(Long.parseLong(level))
                )._toQuery());
            }

            // 先查询Customer索引
            SearchResponse<Customer> customerResponse = esClient.search(s -> s
                .index(CUSTOMER_INDEX)
                .query(customerBoolQuery.build()._toQuery())
                .size(1),
                Customer.class
            );

            // 如果在Customer中找到
            if (customerResponse.hits().total().value() > 0) {
                Customer customer = customerResponse.hits().hits().get(0).source();
                if (customer != null) {
                    result.put("name", customer.getCustomerName());
                    result.put("belongUserId", customer.getBelongUserid());
                    result.put("level", String.valueOf(customer.getCustomerLevel()));
                    result.put("birth", customer.getBirth());
                    return result;
                }
            }

            // 构建Supplier查询
            BoolQuery.Builder supplierBoolQuery = new BoolQuery.Builder()
                .must(TermQuery.of(t -> t
                    .field("emails")
                    .value(email)
                )._toQuery());

            // 如果level不为空，添加level查询条件
            if (level != null && !level.isEmpty()) {
                supplierBoolQuery.must(TermQuery.of(t -> t
                    .field("supplier_level")
                    .value(level)
                )._toQuery());
            }

            // 查询Supplier索引
            SearchResponse<Supplier> supplierResponse = esClient.search(s -> s
                .index(SUPPLIER_INDEX)
                .query(supplierBoolQuery.build()._toQuery())
                .size(1),
                Supplier.class
            );

            // 如果在Supplier中找到
            if (supplierResponse.hits().total().value() > 0) {
                Supplier supplier = supplierResponse.hits().hits().get(0).source();
                if (supplier != null) {
                    result.put("name", supplier.getSupplierName());
                    result.put("level", String.valueOf(supplier.getSupplierLevel()));
                    result.put("belongUserId", supplier.getBelongUserid());
                    result.put("birth", supplier.getBirth());
                    return result;
                }
            }

            log.info("未找到收件人信息: email={}, level={}", email, level);
            return null;

        } catch (Exception e) {
            log.error("获取收件人详细信息失败: email={}, level={}, error={}",
                email, level, e.getMessage(), e);
            return null;
        }
    }


    /**
     * 从Customer和Supplier索引中查找符合条件的收件人邮箱
     */
    @Override
    public Set<String> findMatchingRecipientEmails(Map<String, String> params) {
        Set<String> emails = new HashSet<>();
        try {
            // 如果没有查询条件，返回null表示不需要邮箱过滤
            if (params == null || params.isEmpty() || 
                (!params.containsKey("receiver_level") && !params.containsKey("birth"))) {
                return null;
            }

            // 并行查询Customer和Supplier索引
            CompletableFuture<SearchResponse<Customer>> customerFuture = CompletableFuture.supplyAsync(() -> {
                try {
                    return esClient.search(s -> s
                        .index(CUSTOMER_INDEX)
                        .query(q -> q
                            .bool(b -> {
                                // 处理等级查询
                                if (params.containsKey("receiver_level")) {
                                    b.must(m -> m
                                        .term(t -> t
                                            .field("customer_level")
                                            .value(params.get("receiver_level"))
                                        )
                                    );
                                }
                                
                                // 处理生日查询
                                if (params.containsKey("birth")) {
                                    b.must(m -> m
                                        .term(t -> t
                                            .field("birth")
                                            .value(params.get("birth"))
                                        )
                                    );
                                }
                                return b;
                            })
                        )
                        .size(1000),
                        Customer.class
                    );
                } catch (Exception e) {
                    log.error("Customer search error: {}", e.getMessage(), e);
                    return null;
                }
            });

            CompletableFuture<SearchResponse<Supplier>> supplierFuture = CompletableFuture.supplyAsync(() -> {
                try {
                    return esClient.search(s -> s
                        .index(SUPPLIER_INDEX)
                        .query(q -> q
                            .bool(b -> {
                                // 处理等级查询
                                if (params.containsKey("receiver_level")) {
                                    b.must(m -> m
                                        .term(t -> t
                                            .field("supplier_level")
                                            .value(params.get("receiver_level"))
                                        )
                                    );
                                }

                                // 处理生日查询
                                if (params.containsKey("birth")) {
                                    b.must(m -> m
                                        .term(t -> t
                                            .field("birth")
                                            .value(params.get("birth"))
                                        )
                                    );
                                }
                                return b;
                            })
                        )
                        .size(1000),
                        Supplier.class
                    );
                } catch (Exception e) {
                    log.error("Supplier search error: {}", e.getMessage(), e);
                    return null;
                }
            });

            // 等待所有查询完成
            CompletableFuture.allOf(customerFuture, supplierFuture).join();

            // 处理Customer结果
            SearchResponse<Customer> customerResponse = customerFuture.get();
            if (customerResponse != null) {
                for (Hit<Customer> hit : customerResponse.hits().hits()) {
                    Customer customer = hit.source();
                    if (customer != null && customer.getEmails() != null) {
                        emails.addAll(Arrays.asList(customer.getEmails()));
                    }
                }
            }

            // 处理Supplier结果
            SearchResponse<Supplier> supplierResponse = supplierFuture.get();
            if (supplierResponse != null) {
                for (Hit<Supplier> hit : supplierResponse.hits().hits()) {
                    Supplier supplier = hit.source();
                    if (supplier != null && supplier.getEmails() != null) {
                        emails.addAll(Arrays.asList(supplier.getEmails()));
                    }
                }
            }

            return emails;

        } catch (Exception e) {
            log.error("Error finding matching recipient emails: params={}, error={}", 
                params, e.getMessage(), e);
            return null;  // 发生错误时返回null，表示不进行邮箱过滤
        }
    }


}