package com.java.email.service.impl;


import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.java.email.pojo.Customer;
import com.java.email.pojo.Email;
import com.java.email.pojo.EmailTask;
import com.java.email.service.CustomerService;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.util.List;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.Collections;
import java.util.Map;
import java.util.HashMap;


@Service
public class CustomerServiceImpl implements CustomerService {
    private final ElasticsearchClient esClient;
    private static final String CUSTOMER_INDEX = "customer";

    public CustomerServiceImpl(ElasticsearchClient esClient) {
        this.esClient = esClient;
    }


    // 初始化索引和映射
    public void initEmailIndex() throws IOException {
        // 检查索引是否存在
        boolean exists = esClient.indices().exists(e -> e
                .index(CUSTOMER_INDEX)
        ).value();

        if (!exists) {
            // 创建索引并设置映射
            esClient.indices().create(c -> c
                    .index(CUSTOMER_INDEX)
                    .mappings(Customer.createMapping())  // 使用Email类中定义的映射
            );
        }
    }
    @Override
    public void saveCustomer(Customer customer) throws IOException {
        initEmailIndex();

        IndexResponse response = esClient.index(i -> i
                .index(CUSTOMER_INDEX)
                .id(customer.getCustomerid())
                .document(customer)
        );
    }

    @Override
    public List<String> findEmailsByCustomerLevel(long customerLevel) throws IOException {
        SearchResponse<Customer> response = esClient.search(s -> s
            .index(CUSTOMER_INDEX)
            .query(q -> q
                .bool(b -> b
                    .must(m -> m.term(t -> t
                        .field("customer_level")
                        .value(customerLevel)
                    ))
                )
            )
            .size(10000), // 设置一个较大的值以获取所有匹配的客户
            Customer.class
        );

        return response.hits().hits().stream()
                .map(Hit::source)
                .filter(Objects::nonNull)
                .filter(customer -> customer.getEmails() != null)
                .flatMap(customer -> Arrays.stream(customer.getEmails()))
                .collect(Collectors.toList());
    }


    @Override
    public Customer findCustomerByEmail(String email) throws IOException {
        SearchResponse<Customer> response = esClient.search(s -> s
            .index(CUSTOMER_INDEX)
            .query(q -> q
                .bool(b -> b
                    .must(m -> m.match(t -> t
                        .field("emails").query(email)

                    ))
                )
            )
            .size(1), // 只需要找到第一个匹配的客户
            Customer.class
        );

        return response.hits().hits().stream()
                .map(Hit::source)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }
    @Override
    public List<String> findMatchingCustomerEmails(Map<String, String> params) throws IOException {
        if (!params.containsKey("receiverLevel") && !params.containsKey("receiverBirth")) {
            return Collections.emptyList();
        }
            SearchResponse<Customer> response = esClient.search(s -> s
                            .index(CUSTOMER_INDEX)
                            .query(q -> q
                                    .bool(b -> {
                                        if (params.containsKey("receiverLevel")) {
                                            String level = params.get("receiverLevel");
                                            if (level != null && !level.isEmpty()) {
                                                b.must(m -> m.term(t -> t
                                                        .field("customer_level")
                                                        .value(Long.parseLong(level))
                                                ));
                                            }
                                        }

                                        if (params.containsKey("receiverBirth")) {
                                            String birth = params.get("receiverBirth");
                                            if (birth != null && !birth.isEmpty()) {
                                                b.must(m -> m.term(t -> t
                                                        .field("birth")
                                                        .value(birth)
                                                ));
                                            }
                                        }

                                        return b;
                                    })
                            )
                            .size(10000),
                    Customer.class
            );

            return response.hits().hits().stream()
                    .map(Hit::source)
                    .filter(Objects::nonNull)
                    .filter(customer -> customer.getEmails() != null && customer.getEmails().length > 0)
                    .flatMap(customer -> Arrays.stream(customer.getEmails()))
                    .distinct()
                    .collect(Collectors.toList());
        }


    @Override
    public Map<String, Customer> findCustomersByEmails(List<String> emails) throws IOException {
        if (emails == null || emails.isEmpty()) {
            return Collections.emptyMap();
        }

        SearchResponse<Customer> response = esClient.search(s -> s
            .index(CUSTOMER_INDEX)
            .query(q -> q
                .bool(b -> b
                    .must(m -> m.terms(t -> t
                        .field("emails")
                        .terms(tt -> tt
                            .value(emails.stream()
                                .map(FieldValue::of)
                                .collect(Collectors.toList()))
                        )
                    ))
                )
            )
            .size(emails.size()),
            Customer.class
        );

        Map<String, Customer> emailCustomerMap = new HashMap<>();
        response.hits().hits().stream()
                .map(Hit::source)
                .filter(Objects::nonNull)
                .filter(customer -> customer.getEmails() != null)
                .forEach(customer -> {
                    Arrays.stream(customer.getEmails())
                            .filter(emails::contains)
                            .forEach(email -> emailCustomerMap.put(email, customer));
                });

        return emailCustomerMap;
    }
} 