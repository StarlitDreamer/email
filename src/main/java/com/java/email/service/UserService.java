package com.java.email.service;

import com.java.email.model.entity.User;
import com.java.email.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    /**
     * 根据用户ID查询下属用户的ID列表
     *
     * @param userId 用户ID
     * @return 下属用户的ID列表
     */
//    public List<String> getSubordinateUserIds(String userId) {
//        // 根据belongUserid查询用户列表
//        List<User> subordinateUsers = userRepository.findByBelongUserid(userId);
//
//        // 提取用户ID列表
//        return subordinateUsers.stream()
//                .map(User::getUserId)
//                .collect(Collectors.toList());
//    }

    // 返回下属用户的 userid 列表
    public List<String> getSubordinateUserIds(String userId) {
        List<User> users = userRepository.findByBelongUserid(userId);
        return users.stream()
                .map(User::getUserId) // 提取 userid
                .collect(Collectors.toList());
    }
}