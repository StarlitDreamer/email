package com.java.email.service;


import com.java.email.entity.User;
import com.java.email.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    // 创建用户
    public User createUser(User user) {
        return userRepository.save(user);
    }

    // 根据 ID 查询用户
    public Optional<User> getUserById(String id) {
        return userRepository.findById(id);
    }

    // 更新用户
    public User updateUser(String id, User userDetails) {
        User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
        user.setName(userDetails.getName());
        user.setEmail(userDetails.getEmail());
        user.setAge(userDetails.getAge());
        return userRepository.save(user);
    }

    // 删除用户
    public void deleteUser(String id) {
        userRepository.deleteById(id);
    }

    // 查询所有用户
    public Iterable<User> getAllUsers() {
        return userRepository.findAll();
    }
}