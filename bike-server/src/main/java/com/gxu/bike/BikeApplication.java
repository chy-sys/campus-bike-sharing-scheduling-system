package com.gxu.bike;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 单体应用启动类：合并原 user-service / core-service / dispatch-service
 */
@SpringBootApplication
@MapperScan("com.gxu.bike.mapper")
public class BikeApplication {
    public static void main(String[] args) {
        SpringApplication.run(BikeApplication.class, args);
    }
}
