package com.java.email.repository;

import com.java.email.esdao.UserDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends ElasticsearchRepository<UserDocument, String> {
    // 根据用户账号查找用户
    UserDocument findByUserAccount(String userAccount);
    
    // 根据用户邮箱查找用户
    UserDocument findByUserEmail(String userEmail);
    
    // 根据创建者ID查找用户
    List<UserDocument> findByCreatorId(String creatorId);
    
    // 根据状态查询
    List<UserDocument> findByStatus(Integer status);
    
    // 根据用户名模糊查询
    List<UserDocument> findByUserNameLike(String userName);

    Optional<UserDocument> findByUserId(String userId);

    List<UserDocument> findByBelongUserId(String currentUserId);
} 