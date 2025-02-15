package com.java.config;

import com.java.handler.CustomAccessDeniedHandler;
import com.java.handler.UserAuthenticationFilter;
import com.java.service.impl.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

@Autowired
private CustomAccessDeniedHandler customAccessDeniedHandler;


@Autowired
private CustomUserDetailsService customUserDetailsService;
@Autowired
private UserAuthenticationFilter userAuthenticationFilter;

@Bean
public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
            .csrf(csrf -> csrf.disable())
            // 设置无状态会话
            .sessionManagement(session -> session
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            // 所有请求都允许访问，权限控制交给注解处理
            .authorizeHttpRequests(authz -> authz
                    .anyRequest().permitAll())
            // 禁用表单登录
            .formLogin(form -> form.disable())
            // 禁用 HTTP Basic 认证
            .httpBasic(basic -> basic.disable())
            // 添加自定义过滤器
            .addFilterBefore(userAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            // 配置访问拒绝处理器
            .exceptionHandling(exception -> exception
                    .accessDeniedHandler(customAccessDeniedHandler));

    return http.build();
}

@Bean
public AuthenticationManager authenticationManager(
        AuthenticationConfiguration authenticationConfiguration) throws Exception {
    return authenticationConfiguration.getAuthenticationManager();
}
@Bean
public UserDetailsService userDetailsService() {
    return customUserDetailsService;
}
}