package com.java.config;

import com.java.handler.CustomAccessDeniedHandler;
import com.java.service.impl.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {
@Autowired
private CustomAccessDeniedHandler customAccessDeniedHandler;

@Bean
public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(authz -> authz
                    .anyRequest().authenticated()
            )
            .httpBasic(withDefaults())
            .exceptionHandling(exception -> exception
                    .accessDeniedHandler(customAccessDeniedHandler)
            );
    return http.build();
}
@Bean
public UserDetailsService userDetailsService() {
    return new CustomUserDetailsService();
}

}