package com.java.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.crypto.digest.DigestUtil;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch.core.*;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.java.atuhcode.Auth;
import com.java.common.Redis.RedisService;
import com.java.common.Response.ResultCode;
import com.java.common.userCommon.ThreadLocalUtil;
import com.java.constant.MagicMathConstData;
import com.java.constant.RedisConstData;
import com.java.model.domain.AssignProcess;
import com.java.model.domain.User;
import com.java.model.domain.UserAssign;
import com.java.model.dto.CreateUserDto;
import com.java.model.dto.UpdateUserDto;
import com.java.model.vo.CheckUserVo;
import com.java.model.vo.FilterUserVo;
import com.java.model.vo.UserVo;
import com.java.service.UserService;
import com.java.utils.JwtUtil;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private RedisService redisService;
    private final ElasticsearchClient esClient;
    private static final String INDEX_NAME = "user";

    public UserServiceImpl(ElasticsearchClient esClient) {
        this.esClient = esClient;
    }

    @Override
    public String createUser(CreateUserDto user) throws IOException {
        Integer userRole = ThreadLocalUtil.getUserRole();
        String currentUserId = ThreadLocalUtil.getUserId();
        if (userRole == null) {
            throw new IOException("服务器异常");
        }
        int currentUserRole = userRole;
        User userDocument = createUserDocument(user.getUser_role(), user.getUser_name(), user.getUser_account(), user.getUser_password(), user.getUser_email(), user.getUser_email_code());
        if (currentUserRole == 4) {//拥有用户管理权限的普通用户
            GetResponse<User> response = getUserById(currentUserId);
            String belongUserId = response.source().getBelongUserId();
            userDocument.setBelongUserId(belongUserId);
            userDocument.setCreatorId(currentUserId);
        }
        if (currentUserRole == 2 || currentUserRole == 3) {//大管理和小管理
            userDocument.setCreatorId(currentUserId);
            userDocument.setBelongUserId(currentUserId);
        }
        if (currentUserRole == 3 || currentUserRole == 4) {//小管理和普通用户创建的默认已分配，因为他们不能分配用户给别人。
            userDocument.setStatus(2);
        }
        esClient.index(i -> i.index(INDEX_NAME).id(userDocument.getUserId()).document(userDocument));
        return userDocument.getUserId();
    }

    @Override
    public FilterUserVo filterUser(String user_name, String user_account, String user_email, String belong_user_name, Integer status, Integer page_num, Integer page_size) throws IOException {
        try {
            //提供筛选条件筛选用户，小管理只能筛选出下属用户
            String current_id = ThreadLocalUtil.getUserId();
            Integer currentUserRole = ThreadLocalUtil.getUserRole();
            BoolQuery.Builder boolQuery = new BoolQuery.Builder();
            // 根据角色添加查询条件
            if (currentUserRole == 3) {
                boolQuery.must(m -> m.term(t -> t.field("belong_user_id").value(current_id)));
            } else if (currentUserRole == 4) {
                GetResponse<User> response = getUserById(current_id);
                String belongUserId = response.source().getBelongUserId();
                boolQuery.must(m -> m.term(t -> t.field("belong_user_id").value(belongUserId)));
            }
            Map<String, Object> filters = new HashMap<>();
            if (user_name != null && !user_name.isEmpty()) {
                filters.put("user_name", user_name);
            }
            if (user_account != null && !user_account.isEmpty()) {
                filters.put("user_account", user_account);
            }
            if (user_email != null && !user_email.isEmpty()) {
                filters.put("user_email", user_email);
            }
            if (status != null && (status == 1 || status == 2)) {
                filters.put("status", status);
            }
            // 要查询的是所属用户名。所属用户名不在user表中，需要单独查询。
            // if (belong_user_name != null && !belong_user_name.isEmpty()) {

            //     filters.put("belong_user_name", belong_user_name);
            // }

            // 先查询 belong_user_name 对应的用户
            if (belong_user_name != null && !belong_user_name.isEmpty()) {
                SearchResponse<User> belongUserResponse = esClient.search(s -> s
                        .index(INDEX_NAME)
                        .query(q -> q
                                .match(m -> m
                                        .field("user_name")
                                        .query(belong_user_name)
                                )
                        ), User.class
                );

                // 收集所有匹配用户的 userId
                List<String> belongUserIds = belongUserResponse.hits().hits().stream()
                        .map(hit -> hit.source().getUserId())
                        .collect(Collectors.toList());

                // 如果找到匹配的用户，添加到查询条件中
                if (!belongUserIds.isEmpty()) {
                    boolQuery.must(q -> q
                            .terms(t -> t
                                    .field("belong_user_id")
                                    .terms(t2 -> t2
                                            .value(belongUserIds.stream()
                                                    .map(FieldValue::of)
                                                    .collect(Collectors.toList()))
                                    )
                            )
                    );
                }
            }
            filters.forEach((key, value) -> {
                if (value != null && !value.toString().isEmpty()) {
                    switch (key) {
                        case "user_name":
                            boolQuery.should(m -> m.match(mm -> mm.field(key).query(value.toString())));
                            break;
                        case "user_account":
                            boolQuery.should(m -> m.match(mm -> mm.field(key).query(value.toString())));
                            break;
                        case "user_email":
                            boolQuery.should(m -> m.match(mm -> mm.field(key).query(value.toString())));
                            break;
                        // case "belong_user_name":
                        //     boolQuery.should(m -> m.match(mm -> mm.field(key).query(value.toString())));
                        //     break;
                        case "status":
                            boolQuery.must(m -> m.term(t -> t.field("status").value(FieldValue.of(value))));
                            break;
                        default:
                            break;
                    }
                }
            });
            SearchResponse<User> searchResponse;
            if (filters.isEmpty()) {
                searchResponse = esClient.search(s -> s
                        .index(INDEX_NAME)
                        .from((page_num - 1) * page_size)
                        .size(page_size)
                        .sort(sort -> sort  // 添加排序
                                .field(f -> f
                                        .field("updated_at")
                                        .order(SortOrder.Desc)
                                )
                        ), User.class);
            } else {
                searchResponse = esClient.search(s -> s
                        .index(INDEX_NAME)
                        .query(q -> q.bool(boolQuery.build()))
                        .from((page_num - 1) * page_size)
                        .size(page_size)
                        .sort(sort -> sort  // 添加排序
                                .field(f -> f
                                        .field("updated_at")
                                        .order(SortOrder.Desc)
                                )
                        ), User.class);
            }

            List<UserVo> userVoList = new ArrayList<>();
            // 收集所有需要查询的 belongUserId
            Set<String> belongUserIds = searchResponse.hits().hits().stream()
                    .map(hit -> hit.source().getBelongUserId())
                    .filter(id -> id != null && !id.isEmpty())
                    .collect(Collectors.toSet());

            // 批量查询所属用户
            Map<String, String> belongUserMap = new HashMap<>();
            if (!belongUserIds.isEmpty()) {
                try {
                    MgetResponse<User> mgetResponse = esClient.mget(m -> m
                                    .index(INDEX_NAME)
                                    .ids(belongUserIds.stream().toList()),
                            User.class
                    );
                    mgetResponse.docs().forEach(doc -> {
                        if (doc.result().found() && doc.result().source() != null) {
                            belongUserMap.put(doc.result().id(), doc.result().source().getUserName());
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
            for (Hit<User> hit : searchResponse.hits().hits()) {
                User user = hit.source();
                if (user != null) {
                    String belongUserName = belongUserMap.getOrDefault(user.getBelongUserId(), "");

                    UserVo userVo = new UserVo(
                            user.getUserId(),
                            user.getUserName(),
                            //user.getBelongUserId(),
                            belongUserName,
                            user.getUserAccount(),
                            user.getUserEmail(),
                            user.getStatus()
                    );
                    userVoList.add(userVo);
                }
            }
            int items = userVoList.size();
            System.out.println(userVoList.size());
            System.out.println("items: " + new FilterUserVo(items, page_num, page_size, userVoList));

            // 获取总数
            long totalHits = searchResponse.hits().total().value();
            return new FilterUserVo((int) totalHits, page_num, page_size, userVoList);

        } catch (Exception e) {
            e.printStackTrace();
            throw new IOException("查询失败");

        }
    }

    @Override
    public CheckUserVo checkUser(String user_id) throws IOException {
        GetResponse<User> response = getUserById(user_id);
        User user = response.source();
        if (user == null) {
            throw new IOException("该用户不存在");
        }
        return new CheckUserVo(user.getUserPassword(), user.getUserEmailCode());
    }

    @Override
    public void updateUserinfo(UpdateUserDto user) throws IOException {
        //修改用户信息
        Map<String, String> map = Stream.of(
                        new AbstractMap.SimpleEntry<>("user_name", user.getUser_name()),
                        new AbstractMap.SimpleEntry<>("user_account", user.getUser_account()),
                        new AbstractMap.SimpleEntry<>("user_email", user.getUser_email()),
                        new AbstractMap.SimpleEntry<>("user_password", user.getUser_password()),
                        new AbstractMap.SimpleEntry<>("user_email_code", user.getUser_email_code())
                )
                .filter(entry -> entry.getValue() != null && !entry.getValue().isEmpty())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        if (map.containsKey("user_password")) {
            map.put("user_password", DigestUtil.md5Hex(map.get("user_password")));
        }
        if (map.isEmpty()) {
            throw new IOException("没有需要修改的信息");
        }
        esClient.update(u -> u
                        .index(INDEX_NAME)
                        .id(user.getUser_id())
                        .doc(map),
                User.class);
        boolean redisDelete = redisService.delete(RedisConstData.USER_LOGIN_TOKEN + user.getUser_id());
        if(!redisDelete){
            throw new IOException("更新用户信息，删除token失败");
        }
    }

    @Override
    public void updateUserAuth(String user_id, List<String> user_auth_id, Integer user_role) throws IOException {
        // 修改用户权限
        // 用户角色只有大管理才可以修改，注意权限问题。
        // 用户升为小管理之后，使用数字3、4代表用户角色。3是小管理、4是用户
        // 如果 user_auth_id 为空，则根据 user_role 赋予对应的权限，
        // 如果 user_role 为 3，则所属用户默认改成当前大管理
        // 如果 user_role 为空，则根据 user_auth_id 修改权限，但如果当前用户角色是 3 时，不能赋予 16 权限
        // 如果 user_auth_id 和 user_role 都不为空，则优先根据 user_auth_id 修改权限，
        // 如果 user_role 为 3，则所属用户默认改成当前大管理
        // 根据userRole修改权限;

        // 公司和大管理不允许被修改
        GetResponse<User> checkUserResponse = esClient.get(g -> g
                .index(INDEX_NAME)
                .id(user_id), User.class);
        User checkUser = checkUserResponse.source();
        if (checkUser == null) {
            throw new IOException("用户不存在");
        }
        Integer checkUserRole = checkUser.getUserRole();
        if (checkUserRole == null || checkUserRole == 1 || checkUserRole == 2) {
            throw new IOException("公司和大管理员不允许被修改");
        }

        String currentUserId = ThreadLocalUtil.getUserId();
        Integer currentUserRole = ThreadLocalUtil.getUserRole();
        // 角色修改权限检查
        if (user_role != null && currentUserRole != 2) {
            throw new IOException("只有大管理员可以修改用户角色");
        }

        // 小管理权限限制
        if (currentUserRole == 3 && user_auth_id != null) {
            List<String> restrictedAuths = Arrays.asList("12", "13", "14", "15", "16");
            if (user_auth_id.stream().anyMatch(restrictedAuths::contains)) {
                throw new IOException("小管理无法赋予字典管理和邮件服务器管理权限");
            }
        }

        List<String> authsByRole = null;
        if (user_role != null) {
            authsByRole = Auth.getAuthsByRole(user_role);
            if (user_auth_id == null || user_auth_id.isEmpty()) {
                if (user_role.equals(3)) {
                    user_auth_id = authsByRole;
                    esClient.update(u -> u
                            .index(INDEX_NAME)
                            .id(user_id)
                            //.doc(Map.of("belong_user_id", currentUserId))
                            .doc(Map.of("user_role", user_role)), User.class);
                }
            }
        }
        List<String> userAuthId = Objects.requireNonNull(getUserById(user_id).source()).getUserAuthId();
        ;
        List<String> mergedList = null;
        if (authsByRole == null && user_auth_id != null) {//添加
            mergedList = Stream.concat(user_auth_id.stream(), userAuthId.stream()).distinct().collect(Collectors.toList());
        }
        if (authsByRole != null && user_auth_id == null) {//覆盖
            mergedList = authsByRole;
        }
        if (authsByRole != null && user_auth_id != null) {//覆盖
            mergedList = Stream.concat(authsByRole.stream(), user_auth_id.stream())
                    .distinct()
                    .collect(Collectors.toList());
        }
        Map<String, Object> updateData = new HashMap<>();
        updateData.put("user_auth_id", mergedList);
        updateData.put("updated_at", System.currentTimeMillis() / 1000);
        if (user_role != null) {
            updateData.put("user_role", user_role);
        }
        esClient.update(u -> u
                .index(INDEX_NAME)
                .id(user_id)
                .doc(updateData), User.class);


        // 更新权限后，删除token。
        boolean redisDelete = redisService.delete(RedisConstData.USER_LOGIN_TOKEN + user_id);
        if(!redisDelete){
            throw new IOException("更新用户信息，删除token失败");
        }
        // 下发新token，获取用户信息
//        GetResponse<User> userResponse = esClient.get(g -> g
//                .index(INDEX_NAME)
//                .id(user_id), User.class);
//        User userDoc = userResponse.source();
//        if (userDoc == null) {
//            throw new IOException("用户不存在");
//        }
//        String userId = userDoc.getUserId();
//        if (userId == null) {
//            throw new IOException("用户信息有误");
//        }
//        String userName = userDoc.getUserName();
//        if (userName == null) {
//            throw new IOException("用户姓名信息有误");
//        }
//        Integer userRole = userDoc.getUserRole();
//        if (userRole == null || userRole != 1 && userRole != 2 && userRole != 3 && userRole != 4) {
//            throw new IOException("用户角色信息有误");
//        }
//        Map<String, Object> userMap = new HashMap<>();
//        userMap.put("id", userId);
//        userMap.put("name", userName);
//        userMap.put("role", userRole); // Use the userRole variable declared earlier
//        String token = JwtUtil.genToken(userMap);
//        boolean redisSet = redisService.set(RedisConstData.USER_LOGIN_TOKEN + userId, token, MagicMathConstData.REDIS_VERIFY_TOKEN_TIMEOUT, TimeUnit.HOURS);
//        if (!redisSet) {
//            throw new IOException("redis存入token失败");
//        }
    }

    // 删除用户的方法
    @Override
    public void deleteUser(String userId) throws IOException {
        // 查询用户信息
        GetResponse<User> response = esClient.get(g -> g
                .index(INDEX_NAME)
                .id(userId), User.class);

        if (!response.found()) {
            throw new IOException("用户不存在");
        }

        User user = response.source();
        if (user == null) {
            throw new IOException("用户信息获取失败");
        }

        // 检查用户角色
        Integer userRole = user.getUserRole();
        if (userRole == null || userRole != 1 && userRole != 2 && userRole != 3 && userRole != 4) {
            throw new IOException("用户角色信息有误");
        }

        // 如果是公司或大管理员，不允许删除
        if (userRole == 1 || userRole == 2) {
            throw new IOException("不能删除公司或大管理员账号");
        }
        boolean redisDelete = redisService.delete(RedisConstData.USER_LOGIN_TOKEN + userId);
        if(!redisDelete){
            throw new IOException("删除token失败");
        }
        deleteUserFromUserIndex(userId);
        deleteUserFromUserAssignIndex(userId);
    }

    @Override
    public List<String> getUserAuth(String user_id) throws IOException {
        GetResponse<User> response = esClient.get(g -> g
                .index("user")
                .id(user_id), User.class);
        User user = response.source();
        return user.getUserAuthId();
    }

    private void deleteUserFromUserIndex(String userId) throws IOException {
        // 创建删除请求，删除 user 索引中的用户文档
        DeleteRequest deleteRequest = new DeleteRequest.Builder()
                .index(INDEX_NAME)
                .id(userId)
                .build();
        esClient.delete(deleteRequest);
    }

    private void deleteUserFromUserAssignIndex(String userId) throws IOException {
        // 查找 user_assign 中与 userId 相关的记录
        SearchRequest searchRequest = new SearchRequest.Builder()
                .index("user_assign")
                .query(q -> q.bool(b -> b
                        .should(s -> s.term(t -> t.field("assignProcess.assignorId").value(userId)))  // 查找 assignorId
                        .should(s -> s.term(t -> t.field("assignProcess.assigneeId").value(userId))) // 查找 assigneeId
                ))
                .build();

        if (searchRequest.query().bool().should().size() == 0) {
            return;
        }
        // 执行查询，获取相关记录
        SearchResponse<UserAssign> searchResponse = esClient.search(searchRequest, UserAssign.class);

        // 遍历查询结果
        for (var hit : searchResponse.hits().hits()) {
            String userAssignId = hit.id(); // 获取 user_assign 文档的 ID
            UserAssign userAssign = hit.source();

            // 过滤 assignProcess 中的项，删除指定 userId 的项
            List<AssignProcess> updatedAssignProcess = userAssign.getAssignProcess().stream()
                    .filter(assignProcess -> !(assignProcess.getAssignorId().equals(userId) || assignProcess.getAssigneeId().equals(userId)))
                    .collect(Collectors.toList());

            // 只有在过滤后还有剩余的 assignProcess 项时才更新
            if (!updatedAssignProcess.isEmpty()) {
                // 创建一个 Map 来表示更新数据
                Map<String, Object> updateMap = new HashMap<>();
                updateMap.put("assignProcess", updatedAssignProcess);  // 将更新后的 assignProcess 数组放入 map
                // 更新 user_assign 文档，保留其他 assignProcess 项
                UpdateRequest updateRequest = new UpdateRequest.Builder()
                        .index("user_assign")
                        .id(userAssignId)
                        .doc(updateMap)  // 使用 Map 更新
                        .build();
                esClient.update(updateRequest, UserAssign.class);
            } else {
                // 如果没有剩余的 assignProcess 项，则删除 user_assign 文档
                DeleteRequest deleteRequest = new DeleteRequest.Builder()
                        .index("user_assign")
                        .id(userAssignId)
                        .build();
                esClient.delete(deleteRequest);
            }
        }
    }

    //判断用户权限
    @Override
    public boolean hasAuth(String userId, String authId) throws IOException {
        GetResponse<User> response = getUserById(userId);
        User user = response.source();
        if (user == null) {
            throw new IOException("该用户不存在");
        }
        return user.getUserAuthId().contains(authId);
    }

    public GetResponse<User> getUserById(String userId) throws IOException {

        GetResponse<User> response = esClient.get(g -> g
                        .index(INDEX_NAME)
                        .id(userId),
                User.class
        );
        if (response.found()) {
            return response;
        }

        throw new IOException("未查找到" + userId + "用户信息");
    }

    public User createUserDocument(Integer user_role, String user_name, String user_account, String user_password, String user_email, String user_email_code) {
        User userDocument = new User();
        String[] parts = user_email.split("@");
        String userHost = parts[1];
        userDocument.setUserId(IdUtil.randomUUID());//uuid
        userDocument.setUserRole(user_role);
        userDocument.setCreatorId(null);//创建者id
        userDocument.setBelongUserId(null);//所属用户id
        userDocument.setUserName(user_name);
        userDocument.setUserAccount(user_account);
        userDocument.setUserPassword(DigestUtil.md5Hex(user_password));//md5加密
        userDocument.setUserEmail(user_email);
        userDocument.setUserEmailCode(user_email_code);
        userDocument.setUserAuthId(Auth.getAuthsByRole(user_role));//根据角色获取权限
        userDocument.setStatus(1);//用户分配的状态 未分配
        userDocument.setCreatedAt(getTimestampWithTimezone());  // 设置创建时间和更新时间
        userDocument.setUpdatedAt(getTimestampWithTimezone());
        userDocument.setUserHost(userHost);
        return userDocument;
    }

    public static String setTimestamps() {
        LocalDateTime currentTime = LocalDateTime.now();
        return currentTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'"));
    }

    public static long getTimestampWithTimezone() {
        return Instant.now().getEpochSecond();
    }


}
