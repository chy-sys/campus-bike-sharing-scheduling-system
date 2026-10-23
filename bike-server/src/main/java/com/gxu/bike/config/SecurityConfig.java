package com.gxu.bike.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * 合并原三个微服务各自的 SecurityConfig，放行规则取并集
 */
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        // 放行规则取三个服务的并集
                        .antMatchers("/health", "/test/**", "/auth/**")
                        .permitAll()
                        .anyRequest().authenticated()
                )
                // 前后端分离项目建议关闭CSRF防护
                .csrf(csrf -> csrf.disable());
        return http.build();
    }
}
