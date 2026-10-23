package com.gxu.bike;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;

/**
 * 单体应用启动类：合并原 user-service / core-service / dispatch-service
 * 排除 UserDetailsServiceAutoConfiguration：本项目为纯 JWT 认证，不需要 Security 生成默认用户
 */
@SpringBootApplication(exclude = {UserDetailsServiceAutoConfiguration.class})
@MapperScan("com.gxu.bike.mapper")
public class BikeApplication {
    public static void main(String[] args) {
        SpringApplication.run(BikeApplication.class, args);
    }
}
