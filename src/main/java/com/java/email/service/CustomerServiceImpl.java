package com.java.email.service;

import cn.hutool.core.util.IdUtil;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch.core.CountResponse;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.java.email.dto.FilterCustomerResponse;
import com.java.email.dto.FilterCustomersDto;
import com.java.email.dto.SearchAllCustomersDto;
import com.java.email.dto.SearchAllCustomersResponse;
import com.java.email.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class CustomerServiceImpl {

    // 注入 RedisTemplate，用于与 Redis 进行数据交互
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    // Elasticsearch 客户端
    private final ElasticsearchClient esClient;

    // Elasticsearch 中存储客户信息的索引名称
    private final String INDEX_NAME = "customer";

   // 用于存储属于用户的ID列表
    private final List<String> belongUserIds = new ArrayList<>();

    // 构造函数，注入 ElasticsearchClient
    public CustomerServiceImpl(ElasticsearchClient esClient) {
        this.esClient = esClient;
    }

    // 根据当前用户ID、角色和过滤条件，查询客户列表
    public FilterCustomerResponse FilterFindCustomers(String currentUserId, int currentUserRole, FilterCustomersDto filterCustomersDto) throws IOException {

        // 获取分页参数
        int num = filterCustomersDto.getPage_num();
        int size = filterCustomersDto.getPage_size();

        // 获取过滤条件的具体值
        String commodityName = filterCustomersDto.commodity_name;
        List<String> areaId = filterCustomersDto.area_id;
        List<String> customerCountryId = filterCustomersDto.customer_country_id;
        Integer tradeType = filterCustomersDto.trade_type;
        Integer customerLevel = filterCustomersDto.customer_level;

        // 创建一个布尔查询构建器，用于构建复杂查询条件
        BoolQuery.Builder boolQuery = new BoolQuery.Builder();

        // 用于存储最终的过滤条件
        Map<String, Object> filters = new HashMap<>();

        // 如果当前用户角色是 4（假设为特殊角色），则限制只能查询特定的用户ID
        if (currentUserRole == 4) {
            belongUserIds.add(currentUserId);
            belongUserIds.add("1");  // 可能代表管理员或系统用户
            filters.put("belong_user_id", belongUserIds);
            System.out.println(belongUserIds);
        }

        // 如果提供了商品名称，构建商品查询
        if (commodityName != null) {
            // 通过 Elasticsearch 查询商品信息
            SearchResponse<Commodity> searchResponse = esClient.search(s -> s
                    .index("commodity")  // 查询的索引为 "commodity"
                    .query(q -> q.bool(b -> b
                            // 使用 match 查询商品名称
                            .must(m -> m.match(t -> t.field("commodity_name").query(commodityName)))
                    )), Commodity.class);

            // 提取符合条件的商品ID，并将其加入过滤条件
            List<String> CustomerIds = searchResponse.hits().hits().stream()
                    .map(hit -> hit.source().getCommodityId())  // 获取商品ID
                    .toList();
            filters.put("commodity_id", CustomerIds);  // 将商品ID加入过滤条件
        }

        List<String> areaCountryIds = null;

        if (areaId != null && !areaId.isEmpty()) {
            List<FieldValue> fieldValues = areaId.stream()
                    .map(FieldValue::of)
                    .collect(Collectors.toList());

            // 查询area索引，获取与areaId匹配的area_country
            SearchResponse<Area> areaSearchResponse = esClient.search(s -> s
                    .index("area")  // 查询的索引为 "area"
                    .query(q -> q.bool(b -> b
                            // 使用terms查询area_id字段，查询提供的areaId
                            .must(m -> m.terms(t -> t.field("area_id").terms(v -> v.value(fieldValues))))
                    )), Area.class);

            // 提取area_country，进行flatten操作以获得所有国家ID
           areaCountryIds = areaSearchResponse.hits().hits().stream()
                    .flatMap(hit -> hit.source().getAreaCountry().stream())  // 展开areaCountry中的List<String>
                    .filter(Objects::nonNull)  // 排除空值
                    .distinct()  // 去重
                    .collect(Collectors.toList());  // 转换为List<String>


            // 将area_country作为过滤条件
            if (!areaCountryIds.isEmpty()) {
                filters.put("customer_country_id", areaCountryIds);  // 将area_country作为筛选条件放入filters
            }
        }

        // 如果提供了 customerCountryId，则合并并去重
        if (customerCountryId != null && !customerCountryId.isEmpty()) {
            // 合并并去重
            Set<String> mergedCountryIds = new HashSet<>(areaCountryIds);
            mergedCountryIds.addAll(customerCountryId);

            // 将最终的 mergedCountryIds 作为过滤条件加入 filters
            filters.put("customer_country_id", new ArrayList<>(mergedCountryIds));  // ArrayList<String> 类型
        } else {
            // 如果没有提供 customerCountryId，则直接使用 areaCountryIds 作为过滤条件
            filters.put("customer_country_id", areaCountryIds);  // List<String> 类型
        }

//// 如果提供了客户所在国家ID，则加入过滤条件
//        if (customerCountryId != null && !customerCountryId.isEmpty()) {
//            // 将客户所在国家ID合并到filters中
//            List<String> countryIds = new ArrayList<>(areaCountryIds);
//            countryIds.addAll(customerCountryId);  // 合并areaCountryIds和customerCountryId
//            filters.put("customer_country_id", countryIds);  // 将合并后的countryIds作为筛选条件放入filters
//        }


        // 如果提供了交易类型，则加入过滤条件
        if (tradeType != null) {
            filters.put("trade_type", tradeType);
        }

        // 如果提供了客户级别，则加入过滤条件
        if (customerLevel != null) {
            filters.put("customer_level", customerLevel);
        }

        // 遍历过滤条件，构建布尔查询
        filters.forEach((key, value) -> {
            if (value instanceof List<?> listValue && !listValue.isEmpty()) {
                // 如果过滤条件是列表，使用 terms 查询
                List<FieldValue> fieldValues = listValue.stream()
                        .map(v -> FieldValue.of(v.toString()))  // 将列表元素转换为 FieldValue
                        .toList();
                boolQuery.must(q -> q.terms(t -> t.field(key).terms(v -> v.value(fieldValues))));
            }
            if (value instanceof String stringValue && !stringValue.isEmpty()) {
                // 如果过滤条件是字符串，使用 match 查询
                boolQuery.should(q -> q.match(m -> m.field(key).query(stringValue)));
            }
            if (value instanceof Integer intValue) {
                // 如果过滤条件是整数，使用 term 查询
                boolQuery.must(m -> m.term(t -> t.field(key).value(FieldValue.of(intValue))));
            }
        });

        // 执行 Elasticsearch 查询
        SearchResponse<Customer> searchResponse;
        // 如果没有任何过滤条件，则直接查询客户信息
        if (filters.isEmpty()) {
            searchResponse = esClient.search(s -> s
                            .index(INDEX_NAME)  // 查询的索引为 "customer"
                            .query(q -> q.bool(boolQuery.build()))  // 使用构建的布尔查询
                            .from((num - 1) * size)  // 分页，计算跳过的记录数
                            .size(size),  // 每页的记录数
                    Customer.class);
        } else {
            // 如果有过滤条件，也使用布尔查询进行过滤
            searchResponse = esClient.search(s -> s
                    .index(INDEX_NAME)
                    .query(q -> q.bool(boolQuery.build()))
                    .from((num - 1) * size)
                    .size(size), Customer.class);
        }

        // 处理 Elasticsearch 查询结果，将每个客户转换为 Receiver 对象
        List<ReceiverCustomer> receiverList = new ArrayList<>();
        for (Hit<Customer> CustomerHit : searchResponse.hits().hits()) {
            Customer receiver = CustomerHit.source();
            if (receiver == null) {
                continue;  // 如果客户数据为空，则跳过
            }
            // 将查询到的客户信息包装成 Receiver 对象
            receiverList.add(new ReceiverCustomer(receiver.getCustomerId(), receiver.getCustomerName()));
        }
        belongUserIds.clear();
        // 返回过滤后的客户列表和分页信息
        return new FilterCustomerResponse(receiverList, receiverList.size(), num, size);
    }

//    public FilterCustomersResponse FilterFindSupplier(String currentUserId, int currentUserRole, FilterSuppliersDto filterSuppliersDto) throws IOException {
//        int num=filterSuppliersDto.getPage_num();
//        int size=filterSuppliersDto.getPage_size();
//        String commodityName = filterSuppliersDto.commodity_name;
//        List<String> areaId = filterSuppliersDto.area_id;
//        List<String> supplierCountryId = filterSuppliersDto.supplier_country_id;
//        Integer tradeType = filterSuppliersDto.trade_type;
//        Integer supplierLevel = filterSuppliersDto.supplier_level;
//
//        BoolQuery.Builder boolQuery = new BoolQuery.Builder();
//        Map<String, Object> filters = new HashMap<>();
//
//
//        if (currentUserRole == 4) {
//            belongUserIds.add(currentUserId);
//            belongUserIds.add("1");
//            filters.put("belongUserId", belongUserIds);
//        }
//
//        if (commodityName != null && !commodityName.isEmpty()) {
//            SearchResponse<Commodity> searchResponse = esClient.search(s -> s
//                    .index("commodity")
//                    .query(q -> q.bool(b -> b
////                            .must(m -> m.term(t -> t.field("commodity_name").value(commodityName)))
//                                    .must(m -> m.match(t -> t.field("commodity_name").query(commodityName)))
//                    )), Commodity.class);
//            List<String> CustomerIds = searchResponse.hits().hits().stream()
//                    .map(hit -> hit.source().getCommodityId())
//                    .toList();
//            filters.put("commodity_id", CustomerIds);
//        }
//
//        List<String> areaCountryIds = null;
//
//        if (areaId != null && !areaId.isEmpty()) {
//            List<FieldValue> fieldValues = areaId.stream()
//                    .map(FieldValue::of)
//                    .collect(Collectors.toList());
//
//            // 查询area索引，获取与areaId匹配的area_country
//            SearchResponse<Area> areaSearchResponse = esClient.search(s -> s
//                    .index("area")  // 查询的索引为 "area"
//                    .query(q -> q.bool(b -> b
//                            // 使用terms查询area_id字段，查询提供的areaId
//                            .must(m -> m.terms(t -> t.field("area_id").terms(v -> v.value(fieldValues))))
//                    )), Area.class);
//
//            // 提取area_country，进行flatten操作以获得所有国家ID
//            areaCountryIds = areaSearchResponse.hits().hits().stream()
//                    .flatMap(hit -> hit.source().getAreaCountry().stream())  // 展开areaCountry中的List<String>
//                    .filter(Objects::nonNull)  // 排除空值
//                    .distinct()  // 去重
//                    .collect(Collectors.toList());  // 转换为List<String>
//
//
//            // 将area_country作为过滤条件
//            if (!areaCountryIds.isEmpty()) {
//                filters.put("supplier_country_id", areaCountryIds);  // 将area_country作为筛选条件放入filters
//            }
//        }
//
//        // 如果提供了 customerCountryId，则合并并去重
//        if (supplierCountryId != null && !supplierCountryId.isEmpty()) {
//            // 合并并去重
//            Set<String> mergedCountryIds = new HashSet<>(areaCountryIds);
//            mergedCountryIds.addAll(supplierCountryId);
//
//            // 将最终的 mergedCountryIds 作为过滤条件加入 filters
//            filters.put("supplier_country_id", new ArrayList<>(mergedCountryIds));  // ArrayList<String> 类型
//        } else {
//            // 如果没有提供 customerCountryId，则直接使用 areaCountryIds 作为过滤条件
//            filters.put("supplier_country_id", areaCountryIds);  // List<String> 类型
//        }
//
//        if (tradeType != null) {
//            filters.put("trade_type", tradeType);
//        }
//        if (supplierLevel != null) {
//            filters.put("supplier_level", supplierLevel);
//        }
//
//        filters.forEach((key, value) -> {
//            if (value instanceof List<?> listValue && !listValue.isEmpty()) {
//                List<FieldValue> fieldValues = listValue.stream()
//                        .map(v -> FieldValue.of(v.toString()))
//                        .toList();
//                boolQuery.should(q -> q.terms(t -> t.field(key).terms(v -> v.value(fieldValues))));
//            }
//            if (value instanceof String stringValue && !stringValue.isEmpty()) {
//                boolQuery.should(q -> q.match(m -> m.field(key).query(stringValue)));
//            }
//            if (value instanceof Integer intValue) {
//                boolQuery.must(m -> m.term(t -> t.field(key).value(FieldValue.of(intValue))));
//            }
//
//        });
//
//        SearchResponse<Supplier> searchResponse;
//        if (filters.isEmpty()) {
//            searchResponse = esClient.search(s -> s
//                    .index("supplier")
//                    .query(q -> q.bool(boolQuery.build()))
//                    .from((num - 1) * size)
//                    .size(size), Supplier.class);
//        } else {
//            searchResponse = esClient.search(s -> s
//                    .index("supplier")
//                    .query(q -> q.bool(boolQuery.build()))
//                    .from((num - 1) * size)
//                    .size(size), Supplier.class);
//        }
//
//        List<Receiver> receiverList = new ArrayList<>();
//        for (Hit<Supplier> CustomerHit : searchResponse.hits().hits()) {
//            Supplier receiver = CustomerHit.source();
//            if (receiver == null) {
//                continue;
//            }
//            receiverList.add(new Receiver(receiver.getSupplierId(), receiver.getSupplierName()));
//        }
//        return new FilterCustomersResponse(receiverList, receiverList.size(), num, size);
//    }

    public SearchAllCustomersResponse findCustomers(String currentUserId, int currentUserRole, SearchAllCustomersDto searchAllCustomersDto) throws IOException {
        CountResponse countResponse = esClient.count(c -> c
                .index(INDEX_NAME)  // 索引名称
        );
        int totalCount = (int) countResponse.count();
        String commodityName = searchAllCustomersDto.commodity_name;
        List<String> areaId = searchAllCustomersDto.area_id;
        List<String> countryId = searchAllCustomersDto.customer_country_id;
        Integer tradeType = searchAllCustomersDto.trade_type;
        Integer receiverLevel = searchAllCustomersDto.customer_level;
        BoolQuery.Builder boolQuery = new BoolQuery.Builder();
        Map<String, Object> filters = new HashMap<>();

        // 用于存储属于用户的ID列表
        final List<String> belongUserIds = new ArrayList<>();

        if (currentUserRole == 4) {
            belongUserIds.add(currentUserId);
            belongUserIds.add("1");
            filters.put("belongUserId", belongUserIds);
        }
        if (commodityName != null && !commodityName.isEmpty()) {
            SearchResponse<Commodity> searchResponse = esClient.search(s -> s
                    .index("commodity")
                    .query(q -> q.bool(b -> b
                            .must(m -> m.term(t -> t.field("commodity_name").value(commodityName)))
                    )), Commodity.class);
            List<String> CustomerIds = searchResponse.hits().hits().stream()
                    .map(hit -> hit.source().getCommodityId())
                    .toList();
            filters.put("commodity_id", CustomerIds);
        }
        if (areaId != null && !areaId.isEmpty()) {
            filters.put("area_id", areaId);
        }
        if (countryId != null && !countryId.isEmpty()) {
            filters.put("country_id", countryId);
        }
        if (tradeType != null) {
            filters.put("trade_type", tradeType);
        }
        if (receiverLevel != null) {
            filters.put("receiver_level", receiverLevel);
        }
        filters.forEach((key, value) -> {
            if (value instanceof List<?> listValue && !listValue.isEmpty()) {
                List<FieldValue> fieldValues = listValue.stream()
                        .map(v -> FieldValue.of(v.toString()))
                        .toList();
                boolQuery.should(q -> q.terms(t -> t.field(key).terms(v -> v.value(fieldValues))));
            }
            if (value instanceof String stringValue && !stringValue.isEmpty()) {
                boolQuery.should(q -> q.match(m -> m.field(key).query(stringValue)));
            }
            if (value instanceof Integer intValue) {
                boolQuery.must(m -> m.term(t -> t.field(key).value(FieldValue.of(intValue))));
            }

        });
        SearchResponse<Customer> searchResponse;
        if (filters.isEmpty()) {
            searchResponse = esClient.search(s -> s
                    .index(INDEX_NAME)
                    .query(q -> q.bool(boolQuery.build()))
                    .from(0)
                    .size(totalCount), Customer.class);
        } else {
            searchResponse = esClient.search(s -> s
                    .index(INDEX_NAME)
                    .query(q -> q.bool(boolQuery.build()))
                    .size(totalCount), Customer.class);
        }

        List<ReceiverCustomer> receiverList = new ArrayList<>();
        for (Hit<Customer> CustomerHit : searchResponse.hits().hits()) {
            Customer receiver = CustomerHit.source();
            if (receiver == null) {
                continue;
            }
            receiverList.add(new ReceiverCustomer(receiver.getCustomerId(), receiver.getCustomerName()));
        }
        String receiver_key = "receiver_list:" + IdUtil.fastUUID();
        ValueOperations<String, Object> operations = redisTemplate.opsForValue();
        operations.set(receiver_key, receiverList, 10, TimeUnit.MINUTES);
        return new SearchAllCustomersResponse(receiverList.size(), receiver_key);
    }

    public SearchAllCustomersResponse FindSupplier(String currentUserId, int currentUserRole, SearchAllCustomersDto searchAllCustomersDto) throws IOException {
        CountResponse countResponse = esClient.count(c -> c
                .index("supplier")  // 索引名称
        );
        int totalCount = (int) countResponse.count();
        String commodityName = searchAllCustomersDto.commodity_name;
        List<String> areaId = searchAllCustomersDto.area_id;
        List<String> countryId = searchAllCustomersDto.customer_country_id;
        Integer tradeType = searchAllCustomersDto.trade_type;
        Integer receiverLevel = searchAllCustomersDto.customer_level;
        BoolQuery.Builder boolQuery = new BoolQuery.Builder();
        Map<String, Object> filters = new HashMap<>();

        // 用于存储属于用户的ID列表
        final List<String> belongUserIds = new ArrayList<>();

        if (currentUserRole == 4) {
            belongUserIds.add(currentUserId);
            belongUserIds.add("1");
            filters.put("belongUserId", belongUserIds);
        }
        if (commodityName != null && !commodityName.isEmpty()) {
            SearchResponse<Commodity> searchResponse = esClient.search(s -> s
                    .index("commodity")
                    .query(q -> q.bool(b -> b
                            .must(m -> m.term(t -> t.field("commodity_name").value(commodityName)))
                    )), Commodity.class);
            List<String> CustomerIds = searchResponse.hits().hits().stream()
                    .map(hit -> hit.source().getCommodityId())
                    .toList();
            filters.put("commodity_id", CustomerIds);
        }
        if (areaId != null && !areaId.isEmpty()) {
            filters.put("area_id", areaId);
        }
        if (countryId != null && !countryId.isEmpty()) {
            filters.put("country_id", countryId);
        }
        if (tradeType != null) {
            filters.put("trade_type", tradeType);
        }
        if (receiverLevel != null) {
            filters.put("receiver_level", receiverLevel);
        }

        filters.forEach((key, value) -> {
            if (value instanceof List<?> listValue && !listValue.isEmpty()) {
                List<FieldValue> fieldValues = listValue.stream()
                        .map(v -> FieldValue.of(v.toString()))
                        .toList();
                boolQuery.should(q -> q.terms(t -> t.field(key).terms(v -> v.value(fieldValues))));
            }
            if (value instanceof String stringValue && !stringValue.isEmpty()) {
                boolQuery.should(q -> q.match(m -> m.field(key).query(stringValue)));
            }
            if (value instanceof Integer intValue) {
                boolQuery.must(m -> m.term(t -> t.field(key).value(FieldValue.of(intValue))));
            }

        });
        SearchResponse<Supplier> searchResponse;
        if (filters.isEmpty()) {
            searchResponse = esClient.search(s -> s
                    .index("supplier")
                    .query(q -> q.bool(boolQuery.build()))
                    .from(0)
                    .size(totalCount), Supplier.class);
        } else {
            searchResponse = esClient.search(s -> s
                    .index("supplier")
                    .query(q -> q.bool(boolQuery.build()))
                    .from(0)
                    .size(totalCount), Supplier.class);
        }

        List<ReceiverSupplier> receiverList = new ArrayList<>();
        for (Hit<Supplier> CustomerHit : searchResponse.hits().hits()) {
            Supplier receiver = CustomerHit.source();
            if (receiver == null) {
                continue;
            }
            receiverList.add(new ReceiverSupplier(receiver.getSupplierId(), receiver.getSupplierName()));
        }
        String supplier_key = "supplier_list:" + IdUtil.fastUUID();
        ValueOperations<String, Object> operations = redisTemplate.opsForValue();
        operations.set(supplier_key, receiverList, 10, TimeUnit.MINUTES);
        return new SearchAllCustomersResponse(receiverList.size(), supplier_key);
    }
}

//package com.java.email.service;
//
//import cn.hutool.core.util.IdUtil;
//import co.elastic.clients.elasticsearch.ElasticsearchClient;
//import co.elastic.clients.elasticsearch._types.FieldValue;
//import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
//import co.elastic.clients.elasticsearch._types.query_dsl.Query;
//import co.elastic.clients.elasticsearch.core.CountResponse;
//import co.elastic.clients.elasticsearch.core.SearchResponse;
//import co.elastic.clients.elasticsearch.core.search.Hit;
//import com.java.email.dto.FilterCustomersDto;
//import com.java.email.dto.FilterCustomersResponse;
//import com.java.email.dto.SearchAllCustomersDto;
//import com.java.email.dto.SearchAllCustomersResponse;
//import com.java.email.entity.Commodity;
//import com.java.email.entity.Customer;
//import com.java.email.entity.Receiver;
//import com.java.email.entity.Supplier;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.data.redis.core.ValueOperations;
//import org.springframework.stereotype.Service;
//
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.concurrent.TimeUnit;
//
//@Service
//public class CustomerServiceImpl {
//    @Autowired
//    private RedisTemplate<String, Object> redisTemplate;
//    private final ElasticsearchClient esClient;
//    private final String INDEX_NAME = "customer";
//    private final List<String> belongUserIds = new ArrayList<>();
//
//    public CustomerServiceImpl(ElasticsearchClient esClient) {
//        this.esClient = esClient;
//    }
//
//    public FilterCustomersResponse FilterFindCustomers(String currentUserId, int currentUserRole, FilterCustomersDto filterCustomersDto) throws IOException {
//    int num=Integer.parseInt(filterCustomersDto.getPage_num());
//    int size= Integer.parseInt(filterCustomersDto.getPage_size());
//    String commodityName = filterCustomersDto.commodity_name;
//    List<String> areaId = filterCustomersDto.area_id;
//    String countryId = filterCustomersDto.customer_country_id;
//    Integer tradeType =filterCustomersDto.trade_type;
//    Integer receiverLevel = filterCustomersDto.receiver_level;
//    BoolQuery.Builder boolQuery = new BoolQuery.Builder();
//    Map<String, Object> filters = new HashMap<>();
//
//    if(currentUserRole==4){
//        belongUserIds.add(currentUserId);
//        belongUserIds.add("1");
//        filters.put("belongUserId",belongUserIds);
//        List<FieldValue> fieldValues = belongUserIds.stream()
//                .map(v -> FieldValue.of(v.toString()))
//                .toList();
//
//        SearchResponse<Customer> roleFilteredResponse = esClient.search(s -> s
//                .index(INDEX_NAME)
//                .query(q -> q.bool(b -> b
//                        .must(m -> m.terms(t -> t.field("belongUserId").terms(v -> v.value(fieldValues))))
//                )), Customer.class);
//
//
//        // 获取符合条件的客户ID集合
//        List<String> roleFilteredCustomerIds = roleFilteredResponse.hits().hits().stream()
//                .map(hit -> hit.source().getCustomerId())
//                .toList();
//
//        List<FieldValue> roleFilteredCustomerIdsValues = roleFilteredCustomerIds.stream()
//                .map(v -> FieldValue.of(v.toString()))
//                .toList();
//
//        if (!roleFilteredCustomerIdsValues.isEmpty()) {
//            filters.put("customer_id", roleFilteredCustomerIdsValues);
//        }
//    }
//
//        if (commodityName!= null && !commodityName.isEmpty()) {
//        SearchResponse<Commodity> searchResponse = esClient.search(s -> s
//                .index("commodity")
//                .query(q -> q.bool(b -> b
//                        .must(m -> m.term(t -> t.field("commodity_name").value(commodityName)))
//                )), Commodity.class);
//        List<String> CustomerIds = searchResponse.hits().hits().stream()
//                .map(hit -> hit.source().getCommodityId())
//                .toList();
//
//
//        filters.put("commodity_id", CustomerIds);
//    }
//    if (areaId != null && !areaId.isEmpty()) {
//        filters.put("area_id", areaId);
//    }
//    if (countryId != null && !countryId.isEmpty()) {
//        filters.put("customer_country_id", countryId);
//    }
//    if (tradeType !=null) {
//        filters.put("trade_type", tradeType);
//    }
//    if (receiverLevel != null) {
//        filters.put("receiver_level", receiverLevel);
//    }
//
//    filters.forEach((key, value) -> {
//        if (value instanceof List<?> listValue && !listValue.isEmpty()) {
//            List<FieldValue> fieldValues = listValue.stream()
//                    .map(v -> FieldValue.of(v.toString()))
//                    .toList();
//            boolQuery.should(q -> q.terms(t -> t.field(key).terms(v -> v.value(fieldValues))));
//        }
//        if (value instanceof String stringValue && !stringValue.isEmpty()) {
//            boolQuery.should(q -> q.match(m -> m.field(key).query(stringValue)));
//        }
//        if (value instanceof Integer intValue) {
//            boolQuery.must(m -> m.term(t -> t.field(key).value(FieldValue.of( intValue))));
//        }
//    });
//
//    SearchResponse<Customer> searchResponse;
//    if (filters.isEmpty()) {
//        searchResponse = esClient.search(s -> s
//                .index(INDEX_NAME)
//                .query(q -> q.bool(b -> b
//                        .must(m -> m.terms(t -> t.field("customer_id").terms(v -> v.value((List<FieldValue>) filters.get("customer_id")))))
//                        .must((List<Query>) boolQuery.build()) // 添加后续的过滤条件
//                ))
//                .from((num- 1) * size)
//                .size(size), Customer.class);
//    } else {
//        searchResponse = esClient.search(s -> s
//                .index(INDEX_NAME)
//                .query(q -> q.bool(boolQuery.build()))
//                .from((num - 1) * size)
//                .size(size), Customer.class);
//    }
//
//    List<Receiver> receiverList = new ArrayList<>();
//    for(Hit<Customer> CustomerHit:searchResponse.hits().hits()){
//        Customer receiver = CustomerHit.source();
//        if(receiver==null){
//          continue;
//        }
//        receiverList.add(new Receiver(receiver.getCustomerId(),receiver.getCustomerName()));
//    }
//    return new FilterCustomersResponse(receiverList,receiverList.size(),num,size);
//}
//
//
//    public FilterCustomersResponse FilterFindSupplier(String currentUserId,int currentUserRole,FilterCustomersDto filterCustomersDto) throws IOException {
//        int num=Integer.parseInt(filterCustomersDto.getPage_num());
//        int size= Integer.parseInt(filterCustomersDto.getPage_size());
//        String commodityName = filterCustomersDto.commodity_name;
//        List<String> areaId = filterCustomersDto.area_id;
//        String countryId = filterCustomersDto.customer_country_id;
//        Integer tradeType =filterCustomersDto.trade_type;
//        Integer receiverLevel = filterCustomersDto.receiver_level;
//        BoolQuery.Builder boolQuery = new BoolQuery.Builder();
//        Map<String, Object> filters = new HashMap<>();
//
//        if(currentUserRole==4){
//            belongUserIds.add(currentUserId);
//            belongUserIds.add("1");
//            filters.put("belongUserId",belongUserIds);
//        }
//        if (commodityName!= null && !commodityName.isEmpty()) {
//            SearchResponse<Commodity> searchResponse = esClient.search(s -> s
//                    .index("commodity")
//                    .query(q -> q.bool(b -> b
//                            .must(m -> m.term(t -> t.field("commodity_name").value(commodityName)))
//                    )), Commodity.class);
//            List<String> CustomerIds = searchResponse.hits().hits().stream()
//                    .map(hit -> hit.source().getCommodityId())
//                    .toList();
//            filters.put("commodity_id", CustomerIds);
//        }
//        if (areaId != null && !areaId.isEmpty()) {
//            filters.put("area_id", areaId);
//        }
//        if (countryId != null && !countryId.isEmpty()) {
//            filters.put("country_id", countryId);
//        }
//        if (tradeType !=null) {
//            filters.put("trade_type", tradeType);
//        }
//        if (receiverLevel != null) {
//            filters.put("receiver_level", receiverLevel);
//        }
//
//        filters.forEach((key, value) -> {
//            if (value instanceof List<?> listValue && !listValue.isEmpty()) {
//                List<FieldValue> fieldValues = listValue.stream()
//                        .map(v -> FieldValue.of(v.toString()))
//                        .toList();
//                boolQuery.should(q -> q.terms(t -> t.field(key).terms(v -> v.value(fieldValues))));
//            }
//            if (value instanceof String stringValue && !stringValue.isEmpty()) {
//                boolQuery.should(q -> q.match(m -> m.field(key).query(stringValue)));
//            }
//            if (value instanceof Integer intValue) {
//                boolQuery.must(m -> m.term(t -> t.field(key).value(FieldValue.of( intValue))));
//            }
//
//        });
//
//        SearchResponse<Supplier> searchResponse;
//        if (filters.isEmpty()) {
//            searchResponse = esClient.search(s -> s
//                    .index("supplier")
//                    .query(q -> q.bool(boolQuery.build()))
//                    .from((num- 1) * size)
//                    .size(size), Supplier.class);
//        } else {
//            searchResponse = esClient.search(s -> s
//                    .index("supplier")
//                    .query(q -> q.bool(boolQuery.build()))
//                    .from((num - 1) * size)
//                    .size(size), Supplier.class);
//        }
//
//        List<Receiver> receiverList = new ArrayList<>();
//        for(Hit<Supplier> CustomerHit:searchResponse.hits().hits()){
//            Supplier receiver = CustomerHit.source();
//            if(receiver==null){
//                continue;
//            }
//            receiverList.add(new Receiver(receiver.getSupplierId(),receiver.getSupplierName()));
//        }
//        return new FilterCustomersResponse(receiverList,receiverList.size(),num,size);
//    }
//
//
//public SearchAllCustomersResponse findCustomers(String currentUserId, int currentUserRole, SearchAllCustomersDto searchAllCustomersDto) throws IOException {
//    CountResponse countResponse = esClient.count(c -> c
//            .index(INDEX_NAME)  // 索引名称
//    );
//    int totalCount = (int)countResponse.count();
//    String commodityName = searchAllCustomersDto.commodity_name;
//    List<String> areaId = searchAllCustomersDto.area_id;
//    List<String> countryId = searchAllCustomersDto.country_id;
//    Integer tradeType =searchAllCustomersDto.trade_type;
//    Integer receiverLevel = searchAllCustomersDto.receiver_level;
//    BoolQuery.Builder boolQuery = new BoolQuery.Builder();
//    Map<String, Object> filters = new HashMap<>();
//    if(currentUserRole==4){
//        belongUserIds.add(currentUserId);
//        belongUserIds.add("1");
//        filters.put("belongUserId",belongUserIds);
//    }
//    if (commodityName!= null && !commodityName.isEmpty()) {
//        SearchResponse<Commodity> searchResponse = esClient.search(s -> s
//                .index("commodity")
//                .query(q -> q.bool(b -> b
//                        .must(m -> m.term(t -> t.field("commodity_name").value(commodityName)))
//                )), Commodity.class);
//        List<String> CustomerIds = searchResponse.hits().hits().stream()
//                .map(hit -> hit.source().getCommodityId())
//                .toList();
//        filters.put("commodity_id", CustomerIds);
//    }
//    if (areaId != null && !areaId.isEmpty()) {
//        filters.put("area_id", areaId);
//    }
//    if (countryId != null && !countryId.isEmpty()) {
//        filters.put("country_id", countryId);
//    }
//    if (tradeType !=null) {
//        filters.put("trade_type", tradeType);
//    }
//    if (receiverLevel != null) {
//        filters.put("receiver_level", receiverLevel);
//    }
//    filters.forEach((key, value) -> {
//        if (value instanceof List<?> listValue && !listValue.isEmpty()) {
//            List<FieldValue> fieldValues = listValue.stream()
//                    .map(v -> FieldValue.of(v.toString()))
//                    .toList();
//            boolQuery.should(q -> q.terms(t -> t.field(key).terms(v -> v.value(fieldValues))));
//        }
//        if (value instanceof String stringValue && !stringValue.isEmpty()) {
//            boolQuery.should(q -> q.match(m -> m.field(key).query(stringValue)));
//        }
//        if (value instanceof Integer intValue) {
//            boolQuery.must(m -> m.term(t -> t.field(key).value(FieldValue.of(intValue))));
//        }
//
//    });
//    SearchResponse<Customer> searchResponse;
//    if (filters.isEmpty()) {
//        searchResponse = esClient.search(s -> s
//                .index(INDEX_NAME)
//                .query(q -> q.bool(boolQuery.build()))
//                .from(0)
//                .size(totalCount), Customer.class);
//    } else {
//        searchResponse = esClient.search(s -> s
//                .index(INDEX_NAME)
//                .query(q -> q.bool(boolQuery.build()))
//                .size(totalCount), Customer.class);
//    }
//
//    List<Receiver> receiverList = new ArrayList<>();
//    for(Hit<Customer> CustomerHit:searchResponse.hits().hits()){
//        Customer receiver = CustomerHit.source();
//        if(receiver==null){
//            continue;
//        }
//        receiverList.add(new Receiver(receiver.getCustomerId(),receiver.getCustomerName()));
//    }
//    String receiver_key = "receiver_list:" + IdUtil.fastUUID();
//    ValueOperations<String, Object> operations = redisTemplate.opsForValue();
//    operations.set(receiver_key, receiverList, 10, TimeUnit.MINUTES);
//    return new SearchAllCustomersResponse(receiverList.size(),receiver_key);
//}
//
//    public SearchAllCustomersResponse FindSupplier(String currentUserId,int currentUserRole,SearchAllCustomersDto searchAllCustomersDto) throws IOException {
//        CountResponse countResponse = esClient.count(c -> c
//                .index("supplier")  // 索引名称
//        );
//        int totalCount = (int)countResponse.count();
//        String commodityName = searchAllCustomersDto.commodity_name;
//        List<String> areaId = searchAllCustomersDto.area_id;
//        List<String> countryId = searchAllCustomersDto.country_id;
//        Integer tradeType =searchAllCustomersDto.trade_type;
//        Integer receiverLevel = searchAllCustomersDto.receiver_level;
//        BoolQuery.Builder boolQuery = new BoolQuery.Builder();
//        Map<String, Object> filters = new HashMap<>();
//
//        if(currentUserRole==4){
//            belongUserIds.add(currentUserId);
//            belongUserIds.add("1");
//            filters.put("belongUserId",belongUserIds);
//        }
//        if (commodityName!= null && !commodityName.isEmpty()) {
//            SearchResponse<Commodity> searchResponse = esClient.search(s -> s
//                    .index("commodity")
//                    .query(q -> q.bool(b -> b
//                            .must(m -> m.term(t -> t.field("commodity_name").value(commodityName)))
//                    )), Commodity.class);
//            List<String> CustomerIds = searchResponse.hits().hits().stream()
//                    .map(hit -> hit.source().getCommodityId())
//                    .toList();
//            filters.put("commodity_id", CustomerIds);
//        }
//        if (areaId != null && !areaId.isEmpty()) {
//            filters.put("area_id", areaId);
//        }
//        if (countryId != null && !countryId.isEmpty()) {
//            filters.put("country_id", countryId);
//        }
//        if (tradeType !=null) {
//            filters.put("trade_type", tradeType);
//        }
//        if (receiverLevel != null) {
//            filters.put("receiver_level", receiverLevel);
//        }
//
//        filters.forEach((key, value) -> {
//            if (value instanceof List<?> listValue && !listValue.isEmpty()) {
//                List<FieldValue> fieldValues = listValue.stream()
//                        .map(v -> FieldValue.of(v.toString()))
//                        .toList();
//                boolQuery.should(q -> q.terms(t -> t.field(key).terms(v -> v.value(fieldValues))));
//            }
//            if (value instanceof String stringValue && !stringValue.isEmpty()) {
//                boolQuery.should(q -> q.match(m -> m.field(key).query(stringValue)));
//            }
//            if (value instanceof Integer intValue) {
//                boolQuery.must(m -> m.term(t -> t.field(key).value(FieldValue.of( intValue))));
//            }
//
//        });
//        SearchResponse<Supplier> searchResponse;
//        if (filters.isEmpty()) {
//            searchResponse = esClient.search(s -> s
//                    .index("supplier")
//                    .query(q -> q.bool(boolQuery.build()))
//                    .from(0)
//                    .size(totalCount), Supplier.class);
//        } else {
//            searchResponse = esClient.search(s -> s
//                    .index("supplier")
//                    .query(q -> q.bool(boolQuery.build()))
//                    .from(0)
//                    .size(totalCount), Supplier.class);
//        }
//
//        List<Receiver> receiverList = new ArrayList<>();
//        for(Hit<Supplier> CustomerHit:searchResponse.hits().hits()){
//            Supplier receiver = CustomerHit.source();
//            if(receiver==null){
//                continue;
//            }
//            receiverList.add(new Receiver(receiver.getSupplierId(),receiver.getSupplierName()));
//        }
//        String supplier_key = "supplier_list:" + IdUtil.fastUUID();
//        ValueOperations<String, Object> operations = redisTemplate.opsForValue();
//        operations.set(supplier_key, receiverList, 10, TimeUnit.MINUTES);
//        return new SearchAllCustomersResponse(receiverList.size(),supplier_key);
//    }
//}