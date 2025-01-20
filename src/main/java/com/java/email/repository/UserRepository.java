package com.java.email.repository;

import com.java.email.entity.User;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends ElasticsearchRepository<User, String> {

    /**
     * 根据belongUserid查询用户列表
     *
     * @param belongUserid 所属用户ID
     * @return 用户列表
     */
    List<User> findByBelongUserid(String belongUserid);

    List<String> findUserIdsByBelongUserid(String belongUserid); // 直接查询下属用户的 userid
}