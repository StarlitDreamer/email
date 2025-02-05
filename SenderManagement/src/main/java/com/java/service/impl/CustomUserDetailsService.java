package com.java.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class CustomUserDetailsService implements UserDetailsService {

@Autowired
private UserServiceImpl userService;

@Override
public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
    // 根据 userId 查询用户信息和权限
    List<String> authIds;
    try {
        authIds = Objects.requireNonNull(userService.getUserById(userId).source()).getUserAuthId();
    } catch (IOException e) {
        throw new RuntimeException(e);
    }
    List<GrantedAuthority> authorities = authIds.stream()
            .map(authId -> new SimpleGrantedAuthority("AUTH_" + authId)) // 权限前缀
            .collect(Collectors.toList());

    return new User(userId, "", authorities); // 假设密码为空
}
}