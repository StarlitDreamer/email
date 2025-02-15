package com.java.config;

import com.java.aop.interceptors.CheckTokenInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private CheckTokenInterceptor checkTokenInterceptor;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        System.out.println();
        registry.addMapping("/**")  // 所有接口
                .allowedOriginPatterns("*")  // 使用 allowedOriginPatterns 代替 allowedOrigins
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")  // 允许的方法
                .allowedHeaders("*")  // 允许的请求头
                .allowCredentials(true)  // 允许携带认证信息
                .maxAge(3600);  // 预检请求的有效期，单位为秒
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        List<String> excludePathList = new ArrayList<>();
        //excludePathList.add("/userManage/createUser");
        excludePathList.add("/user/login");
        excludePathList.add("/user/logout");
        excludePathList.add("/error");
        registry.addInterceptor(checkTokenInterceptor).addPathPatterns("/**").excludePathPatterns(excludePathList);
    }
}