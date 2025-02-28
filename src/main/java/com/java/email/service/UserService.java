package com.java.email.service;

import com.java.email.model.entity.User;
import com.java.email.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    // 根据 userId 查询 userEmail
    public String getUserEmailByUserId(String userId) {
        User user = userRepository.findByUserId(userId);
        if (user != null) {
            return user.getUserEmail();
        } else {
            throw new RuntimeException("User not found for userId: " + userId);
        }
    }
}
