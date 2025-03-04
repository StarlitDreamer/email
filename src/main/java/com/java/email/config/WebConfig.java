package com.java.email.config;

import com.java.email.aop.CheckTokenInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.ArrayList;
import java.util.List;

    
@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Autowired
    private CheckTokenInterceptor checkTokenInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        List<String> excludePathList=new ArrayList<>();
        excludePathList.add("/userManage/createUser");
        excludePathList.add("/user/login");
        excludePathList.add("/user/logout");
        excludePathList.add("/error");
        registry.addInterceptor(checkTokenInterceptor).addPathPatterns("/**").excludePathPatterns(excludePathList);
    }

    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();
    
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(5000);
        requestFactory.setReadTimeout(5000);
    
        restTemplate.setRequestFactory(requestFactory);
        
        return restTemplate;
    }
}
