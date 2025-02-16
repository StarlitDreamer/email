package com.java.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.crypto.digest.DigestUtil;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import co.elastic.clients.elasticsearch.core.GetResponse;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.bulk.BulkOperation;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.monitoring.BulkRequest;
import co.elastic.clients.util.ObjectBuilder;
import com.java.atuhcode.Auth;
import com.java.common.userCommon.ThreadLocalUtil;
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
    private final static String USER_INDEX_NAME = "user";

    private final static String INDEX_NAME = "user_assign";

    public UserAssignServiceImpl(ElasticsearchClient esClient) {
        this.esClient = esClient;
    }

    @Override                       //被分配id 所属用户id
    public void assignUser(String user_id, String belong_user_id) throws IOException {
        String currentUser_id = ThreadLocalUtil.getUserId();//分配者id
        GetResponse<User> response = esClient.get(g -> g
                .index("user")
                .id(belong_user_id), User.class);
        User assignee = response.source();
        // 添加角色验证
        if (assignee == null || (assignee.getUserRole() != 2 && assignee.getUserRole() != 3)) {
            throw new IOException("分配的目标用户必须是管理员");
        }

        GetResponse<User> responseUser = esClient.get(g -> g
                .index("user")
                .id(user_id), User.class);
        User user = responseUser.source();
        // 添加角色验证
        if (user == null) {
            throw new IOException("当前操作用户不存在");
        }
        String assignorName = ThreadLocalUtil.getUserName();  // 获取分配者名称
        String assigneeName = assignee.getUserName();  // 获取被分配者名称

        //String assigneeName = Objects.requireNonNull(Objects.requireNonNull(userService.getUserById(belong_user_id).source()).getUserName());  // 获取被分配者名称
        String currentTime = UserServiceImpl.setTimestamps();
        AssignProcess assignProcess = new AssignProcess(currentUser_id, assignorName, belong_user_id, assigneeName, currentTime);
        UserAssign userAssign = new UserAssign(user_id, List.of(assignProcess));
        esClient.update(u -> u
                .index(USER_INDEX_NAME)
                .id(user_id)
                .doc(Map.of("status", 2,
                        "belong_user_id", assignee.getUserId(),
                        "updated_at", System.currentTimeMillis() / 1000
                )), User.class);
        // 检查 currentUser_id 文档是否存在
        GetResponse<UserAssign> currentUserResponse = esClient.get(g -> g
                .index(INDEX_NAME)
                .id(user_id), UserAssign.class);
        // 如果不存在，创建新文档
        if (!currentUserResponse.found()) {
            esClient.index(i -> i
                    .index(INDEX_NAME)
                    .id(user_id)
                    .document(userAssign));
        } else {
            // 如果存在，更新文档
            List<AssignProcess> collect = Stream.concat(
                            Stream.of(assignProcess),  // 新元素放在前面
                            currentUserResponse.source().getAssignProcess().stream()
                    ).distinct()
                    .collect(Collectors.toList());
            esClient.update(u -> u
                    .index(INDEX_NAME)
                    .id(user_id)
                    .doc(Map.of("assign_process", collect)), UserAssign.class);
        }
    }

    @Override
    public void BatchUserImport(List<CsvUserDto> csvUserDtoList) throws IOException {
        try {
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
                // 大管理导入的用户都是未分配的
                if (user.getUserRole() == 2) {
                    user.setStatus(1);
                } else {
                    user.setStatus(2);
                }
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
                UserAssign userAssign = new UserAssign(userId, List.of(assignProcess));

                // 添加用户索引操作
                operations.add(new BulkOperation.Builder()
                        .index(idx -> idx
                                .index(USER_INDEX_NAME)
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
            esClient.bulk(b -> b
                    .operations(operations)
            );


        } catch (Exception e) {
            e.printStackTrace();
            throw new IOException("批量导入过程中发生错误");
        }

    }


    @Override
    public AssignUserDetailsVo assignUserDetails(String user_id, String page_num, String page_size) throws IOException {
        int pageNum = Integer.parseInt(page_num);
        int pageSize = Integer.parseInt(page_size);
        //不在查询层做分页，因为分配过程是个子字段，es查询不了。直接拿出整个分配过程数组，再单独做分页
        SearchResponse<UserAssign> searchResponse = esClient.search(s -> s
                .index(INDEX_NAME)
                .query(q -> q.bool(b -> b
                        .must(m -> m.term(t -> t.field("user_id").value(user_id)))
                )), UserAssign.class);

        List<AssignProcess> assignProcessList = new ArrayList<>();
        for (Hit<UserAssign> hit : searchResponse.hits().hits()) {
            UserAssign userAssign = hit.source();
            if (userAssign != null && userAssign.getAssignProcess() != null) {
                assignProcessList.addAll(userAssign.getAssignProcess());
            }
        }
        // 计算总数
        int totalItems = assignProcessList.size();

        // 计算分页
        int start = (pageNum - 1) * pageSize;
        int end = Math.min(start + pageSize, assignProcessList.size());

        // 获取当前页的数据
        List<AssignProcess> pageData = new ArrayList<>();
        if (start < assignProcessList.size()) {
            pageData = assignProcessList.subList(start, end);
        }
        // assignProcessList = pageData.stream()
        //         .sorted(Comparator.comparing(AssignProcess::getAssignDate).reversed()) // 按照 scheduleTime 排序
        //         .collect(Collectors.toList());

        return new AssignUserDetailsVo(totalItems, pageNum, pageSize, pageData);
    }
}
