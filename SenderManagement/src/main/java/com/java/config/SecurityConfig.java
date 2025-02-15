//package com.java.config;
//
//import com.java.email.aop.interceptors.CheckTokenInterceptor;
//import com.java.handler.CustomAccessDeniedHandler;
//import com.java.handler.UserAuthenticationFilter;
//import com.java.service.impl.CustomUserDetailsService;
//import jakarta.servlet.FilterChain;
//import jakarta.servlet.ServletException;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.authentication.AuthenticationManager;
//import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
//import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//import org.springframework.security.config.http.SessionCreationPolicy;
//import org.springframework.security.core.userdetails.UserDetailsService;
//import org.springframework.security.web.SecurityFilterChain;
//import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
//import org.springframework.web.filter.OncePerRequestFilter;
//
//import java.io.IOException;
//
//@Configuration
//@EnableWebSecurity
//@EnableMethodSecurity(prePostEnabled = true)
//public class SecurityConfig {
//
//    @Autowired
//    private CustomAccessDeniedHandler customAccessDeniedHandler;
//
//
//    @Autowired
//    private CustomUserDetailsService customUserDetailsService;
//    @Autowired
//    private UserAuthenticationFilter userAuthenticationFilter;
//    @Autowired
//    private CheckTokenInterceptor checkTokenInterceptor;
//
//    @Bean
//    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//        http
//                .addFilterBefore(new OncePerRequestFilter() {
//                    @Override
//                    protected void doFilterInternal(HttpServletRequest request,
//                                                    HttpServletResponse response,
//                                                    FilterChain filterChain) throws ServletException, IOException {
//                        try {
//                            if (checkTokenInterceptor.preHandle(request, response, null)) {
//                                filterChain.doFilter(request, response);
//                            } else {
//                                // Token 验证失败时的处理
//                                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
//                                response.setContentType("application/json;charset=UTF-8");
//                                response.getWriter().write("{\"code\":401,\"message\":\"未授权或token已过期\"}");
//                            }
//                        } catch (Exception e) {
//                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
//                            response.setContentType("application/json;charset=UTF-8");
//                            response.getWriter().write("{\"code\":403,\"message\":\"访问被拒绝\"}");
//                        }
//                    }
//
//                    @Override
//                    protected boolean shouldNotFilter(HttpServletRequest request) {
//                        // 排除不需要验证token的路径
//                        String path = request.getServletPath();
//                        return path.equals("/user/login") ||
//                                path.equals("/user/logout") ||
//                                path.equals("/userManage/filterUser") ||
//                                path.equals("/error");
//                    }
//                }, UsernamePasswordAuthenticationFilter.class)
//                .exceptionHandling(ex -> ex
//                        .authenticationEntryPoint((request, response, authException) -> {
//                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
//                            response.setContentType("application/json;charset=UTF-8");
//                            response.getWriter().write("{\"code\":401,\"message\":\"请先登录\"}");
//                        })
//                        .accessDeniedHandler(customAccessDeniedHandler))
//                .csrf(csrf -> csrf.disable())
//                .sessionManagement(session -> session
//                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
//                .authorizeHttpRequests(authz -> authz
//                        .requestMatchers("/user/login", "/user/logout", "/error", "/userManage/filterUser").permitAll()
//                        .anyRequest().authenticated());
//        // 将token验证逻辑集成到Spring Security中
//
//
//        return http.build();
//    }
//
//    @Bean
//    public AuthenticationManager authenticationManager(
//            AuthenticationConfiguration authenticationConfiguration) throws Exception {
//        return authenticationConfiguration.getAuthenticationManager();
//    }
//
//
//}