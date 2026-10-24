package com.campus.bike.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 静态资源映射：把上传目录映射为可直接访问的 URL
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${file.upload-path:uploads/}")
    private String uploadPath;

    @Value("${file.access-prefix:/uploads/}")
    private String accessPrefix;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String prefix = accessPrefix.endsWith("/") ? accessPrefix : accessPrefix + "/";

        File dir = new File(uploadPath);
        if (!dir.isAbsolute()) {
            dir = new File(System.getProperty("user.dir"), uploadPath);
        }
        String location = dir.getAbsolutePath();
        if (!location.endsWith(File.separator)) {
            location = location + File.separator;
        }
        registry.addResourceHandler(prefix + "**")
                .addResourceLocations("file:" + location);
    }
}
