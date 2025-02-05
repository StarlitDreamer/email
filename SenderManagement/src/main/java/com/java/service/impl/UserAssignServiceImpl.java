package com.java.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.GetResponse;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.java.email.common.userCommon.ThreadLocalUtil;
import com.java.model.domain.AssignProcess;
import com.java.model.domain.User;
import com.java.model.domain.UserAssign;
import com.java.model.vo.AssignUserDetailsVo;
import com.java.service.UserAssignService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.util.*;
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
        String currentUser_id= ThreadLocalUtil.getUserId();//分配者id
        GetResponse<User> response = esClient.get(g -> g
                .index("user")
                .id(belong_user_id), User.class);
        User user =  response.source();
        if (user == null) {
            throw new IOException("不存在该用户");
        }
        if (user.getUserRole() != 3) {
            throw new IOException("不是管理员不行");
        }
        String assignorName = ThreadLocalUtil.getUserName();  // 获取分配者名称
        String assigneeName = Objects.requireNonNull(Objects.requireNonNull(userService.getUserById(belong_user_id).source()).getUserName());  // 获取被分配者名称
        String currentTime = UserServiceImpl.setTimestamps();
        AssignProcess assignProcess = new AssignProcess(currentUser_id, assignorName, belong_user_id, assigneeName, currentTime);
        UserAssign userAssign = new UserAssign(currentUser_id, List.of(assignProcess));
        esClient.update(u -> u
                .index(INDEX_NAME)
                .id(belong_user_id)
                .doc(Map.of("status",2)), User.class);
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
