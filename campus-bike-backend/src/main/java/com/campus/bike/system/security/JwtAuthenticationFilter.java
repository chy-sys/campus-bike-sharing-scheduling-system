package com.campus.bike.system.security;

import com.campus.bike.common.constant.Constants;
import com.campus.bike.common.utils.JwtUtil;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * JWT 认证过滤器：解析请求头中的 Bearer Token，校验通过后写入 SecurityContext。
 * 不标注 @Component，由 SecurityConfig 手动创建，避免被 Servlet 容器重复注册。
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtUtil jwtUtil;

    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader(AUTHORIZATION_HEADER);
        if (header != null && header.startsWith(BEARER_PREFIX)) {
            String token = header.substring(BEARER_PREFIX.length());
            if (jwtUtil.validateToken(token)) {
                String phone = jwtUtil.getPhoneFromToken(token);
                Long userId = jwtUtil.getUserIdFromToken(token);
                Integer role = jwtUtil.getRoleFromToken(token);

                // 按角色授予权限：普通用户 / 运维人员 / 管理员
                String authority;
                if (role != null && role == Constants.ROLE_ADMIN) {
                    authority = "ROLE_ADMIN";
                } else if (role != null && role == Constants.ROLE_STAFF) {
                    authority = "ROLE_STAFF";
                } else {
                    authority = "ROLE_USER";
                }

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                phone, null, Collections.singletonList(new SimpleGrantedAuthority(authority)));
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);

                // 供 Controller / Service 直接取当前登录用户信息
                request.setAttribute(Constants.ATTR_USER_ID, userId);
                request.setAttribute(Constants.ATTR_PHONE, phone);
                request.setAttribute(Constants.ATTR_ROLE, role);
            }
        }
        filterChain.doFilter(request, response);
    }
}
