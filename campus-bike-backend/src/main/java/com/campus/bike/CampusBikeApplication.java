package com.campus.bike;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;

/**
 * 校园共享单车调度管理系统 —— 后端单体服务唯一启动入口
 * <p>
 * 排除 UserDetailsServiceAutoConfiguration：本项目为纯 JWT 认证，
 * 不需要 Spring Security 自动生成默认用户。
 */
@SpringBootApplication(exclude = {UserDetailsServiceAutoConfiguration.class})
@MapperScan({
        "com.campus.bike.modules.user.mapper",
        "com.campus.bike.modules.vehicle.mapper",
        "com.campus.bike.modules.station.mapper",
        "com.campus.bike.modules.order.mapper",
        "com.campus.bike.modules.repair.mapper",
        "com.campus.bike.modules.dispatch.mapper"
})
public class CampusBikeApplication {

    public static void main(String[] args) {
        SpringApplication.run(CampusBikeApplication.class, args);
    }
}
