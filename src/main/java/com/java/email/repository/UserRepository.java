package com.java.email.repository;

import com.java.email.model.entity.User;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends ElasticsearchRepository<User, String> {
    // 根据 userId 查询用户的 userEmail
    User findByUserId(String userId);
}

