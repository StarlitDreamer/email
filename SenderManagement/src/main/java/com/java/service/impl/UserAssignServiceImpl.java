package com.java.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.crypto.digest.DigestUtil;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import co.elastic.clients.elasticsearch.core.GetResponse;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.bulk.BulkOperation;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.monitoring.BulkRequest;
import co.elastic.clients.util.ObjectBuilder;
import com.java.atuhcode.Auth;
import com.java.email.common.userCommon.ThreadLocalUtil;
import com.java.model.domain.AssignProcess;
import com.java.model.domain.User;
import com.java.model.domain.UserAssign;
import com.java.model.dto.CsvUserDto;
import com.java.model.vo.AssignUserDetailsVo;
import com.java.service.UserAssignService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class UserAssignServiceImpl implements UserAssignService {
@Autowired
private UserServiceImpl userService;
private final ElasticsearchClient esClient;
private final static String INDEX_NAME = "user_assign";

public UserAssignServiceImpl(ElasticsearchClient esClient) {
    this.esClient = esClient;
}

@Override                       //被分配id 所属用户id
public void assignUser(String belong_user_id) throws IOException {
    String currentUser_id = ThreadLocalUtil.getUserId();//分配者id
    GetResponse<User> response = esClient.get(g -> g
            .index("user")
            .id(belong_user_id), User.class);
    User user = response.source();
    // 添加角色验证
    if (user == null || (user.getUserRole() != 2 && user.getUserRole() != 3)) {
        throw new IOException("分配的目标用户必须是管理员");
    }
    String assignorName = ThreadLocalUtil.getUserName();  // 获取分配者名称
    String assigneeName = Objects.requireNonNull(Objects.requireNonNull(userService.getUserById(belong_user_id).source()).getUserName());  // 获取被分配者名称
    String currentTime = UserServiceImpl.setTimestamps();
    AssignProcess assignProcess = new AssignProcess(currentUser_id, assignorName, belong_user_id, assigneeName, currentTime);
    UserAssign userAssign = new UserAssign(currentUser_id, List.of(assignProcess));
    esClient.update(u -> u
            .index(INDEX_NAME)
            .id(belong_user_id)
            .doc(Map.of("status", 2)), User.class);
    // 检查 currentUser_id 文档是否存在
    GetResponse<UserAssign> currentUserResponse = esClient.get(g -> g
            .index(INDEX_NAME)
            .id(currentUser_id), UserAssign.class);
    // 如果不存在，创建新文档
    if (!currentUserResponse.found()) {
        esClient.index(i -> i
                .index(INDEX_NAME)
                .id(currentUser_id)
                .document(userAssign));
    } else {
        // 如果存在，更新文档
        List<AssignProcess> collect = Stream.concat(currentUserResponse.source().getAssignProcess().stream(), Stream.of(assignProcess)).distinct().collect(Collectors.toList());
        esClient.update(u -> u
                .index(INDEX_NAME)
                .id(currentUser_id)
                .doc(Map.of("userAssign", collect)), UserAssign.class);
    }
}

@Override
public void BatchUserImport(List<CsvUserDto> csvUserDtoList) throws IOException {
    Integer currentUserRole = ThreadLocalUtil.getUserRole();
    String currentUserId = ThreadLocalUtil.getUserId();
    String currentUserName = ThreadLocalUtil.getUserName();
    List<User> users = new ArrayList<>();

    // 如果是普通用户导入，获取其所属管理员ID
    String belongUserId = currentUserId;
    if (currentUserRole == 4) {
        GetResponse<User> response = userService.getUserById(currentUserId);
        assert response.source() != null;
        belongUserId = response.source().getBelongUserId();
    }

    // 批量创建用户和分配记录
    List<BulkOperation> operations = new ArrayList<>();
    String currentTime = UserServiceImpl.setTimestamps();


    for (CsvUserDto csvUser : csvUserDtoList) {
        User user = new User();
        String userId = IdUtil.randomUUID();
        user.setUserId(userId);

        // 小管理和用户导入时，只能创建普通用户
        if (currentUserRole == 3 || currentUserRole == 4) {
            user.setUserRole(4); // 设置为普通用户
        } else {
            user.setUserRole(Integer.parseInt(csvUser.getUserRole()));
        }
        user.setUserName(csvUser.getUserName());
        user.setUserAccount(csvUser.getUserAccount());
        user.setUserPassword(DigestUtil.md5Hex(csvUser.getUserPassword())); // 密码需要加密
        user.setUserEmail(csvUser.getUserEmail());
        user.setUserEmailCode(csvUser.getUserEmailCode());
        user.setCreatorId(currentUserId);
        user.setBelongUserId(belongUserId);
        user.setUserAuthId(Auth.roleAuthMap.get(user.getUserRole()));
        user.setStatus(2);
        user.setCreatedAt(UserServiceImpl.getTimestampWithTimezone());
        user.setUpdatedAt(UserServiceImpl.getTimestampWithTimezone());
        String[] parts = csvUser.getUserEmail().split("@");
        user.setUserHost(parts[1]);
        users.add(user);
        // 创建用户分配记录
        AssignProcess assignProcess = new AssignProcess(
                currentUserId,
                currentUserName,
                userId,
                csvUser.getUserName(),
                currentTime
        );
        UserAssign userAssign = new UserAssign(currentUserId, List.of(assignProcess));

        // 添加用户索引操作
        operations.add(new BulkOperation.Builder()
                .index(idx -> idx
                        .index("user")
                        .id(userId)
                        .document(user)
                ).build());

        // 添加用户分配记录索引操作
        operations.add(new BulkOperation.Builder()
                .index(idx -> idx
                        .index(INDEX_NAME)
                        .id(currentUserId)
                        .document(userAssign)
                ).build());
    }

// 执行批量操作
    BulkResponse bulkResponse = esClient.bulk(b -> b
            .operations(operations)
    );

// 检查是否有错误
    if (bulkResponse.errors()) {
        throw new IOException("批量导入过程中发生错误");
    }
}



@Override
public AssignUserDetailsVo assignUserDetails(String user_id, String page_num, String page_size) throws IOException {
    int pageNum = Integer.parseInt(page_num);
    int pageSize = Integer.parseInt(page_size);
    SearchResponse<UserAssign> searchResponse = esClient.search(s -> s
            .index(INDEX_NAME)
            .query(q -> q.bool(b -> b
                    .must(m -> m.term(t -> t.field("userId").value(user_id)))
            ))
            .from((pageNum - 1) * pageSize)
            .size(pageSize), UserAssign.class);
    List<AssignProcess> assignProcessList = new ArrayList<>();
    for (Hit<UserAssign> hit : searchResponse.hits().hits()) {
        UserAssign userAssign = hit.source();
        if (userAssign != null && userAssign.getAssignProcess() != null) {
            assignProcessList.addAll(userAssign.getAssignProcess());
        }
    }
    int totalItems = (int) searchResponse.hits().total().value();
    return new AssignUserDetailsVo(totalItems, pageNum, pageSize, assignProcessList);
}
}
