package com.campus.bike.config;

import com.campus.bike.system.security.SecurityHandler;
import com.campus.bike.system.security.JwtAuthenticationFilter;
import com.campus.bike.common.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * 安全配置：白名单放行 + JWT 无状态认证
 */
@Configuration
public class SecurityConfig {

    @Autowired
    private SecurityHandler securityHandler;

    @Autowired
    private JwtUtil jwtUtil;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        // 无需登录即可访问
                        .antMatchers("/health", "/test/**", "/auth/**")
                        .permitAll()
                        .anyRequest().authenticated()
                )
                // 未认证 / 无权限统一返回 JSON
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(securityHandler)
                        .accessDeniedHandler(securityHandler)
                )
                // 前后端分离 + JWT，禁用 Session
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // 前后端分离项目建议关闭CSRF防护
                .csrf(csrf -> csrf.disable())
                // 在用户名密码认证之前解析 JWT
                .addFilterBefore(new JwtAuthenticationFilter(jwtUtil), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * 密码加密器，供注册/登录时统一使用
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
