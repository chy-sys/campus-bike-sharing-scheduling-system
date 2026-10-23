package com.campus.bike.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 高德地图配置：站点电子围栏、骑行路径规划
 * 在 application.yml 中配置 amap.key 后生效
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "amap")
public class AmapConfig {

    /** 前端 JS API key */
    private String key;

    /** 服务端 Web 服务 key */
    private String webServiceKey;

    /** 是否可用 */
    public boolean enabled() {
        return key != null && !key.trim().isEmpty();
    }
}
