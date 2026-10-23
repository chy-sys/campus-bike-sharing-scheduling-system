package com.gxu.bike.controller;

import com.gxu.bike.dto.LoginRequest;
import com.gxu.bike.dto.LoginResponse;
import com.gxu.bike.dto.RegisterRequest;
import com.gxu.bike.entity.User;
import com.gxu.bike.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody RegisterRequest request) {
        Map<String, Object> result = new HashMap<>();
        // 开发阶段：验证码固定为 "123456"
        if (!"123456".equals(request.getVerifyCode())) {
            result.put("code", 400);
            result.put("msg", "验证码错误");
            return result;
        }
        boolean success = userService.register(request.getPhone(), request.getPassword(), request.getUsername());
        if (success) {
            result.put("code", 200);
            result.put("msg", "注册成功");
        } else {
            result.put("code", 400);
            result.put("msg", "手机号已存在");
        }
        return result;
    }

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody LoginRequest request) {
        Map<String, Object> result = new HashMap<>();
        String token = userService.login(request.getPhone(), request.getPassword());
        if (token != null) {
            User user = userService.getUserByPhone(request.getPhone());
            result.put("code", 200);
            result.put("msg", "登录成功");
            result.put("data", new LoginResponse(token, user.getId(), user.getUsername(), user.getPhone()));
        } else {
            result.put("code", 401);
            result.put("msg", "手机号或密码错误");
        }
        return result;
    }
}
