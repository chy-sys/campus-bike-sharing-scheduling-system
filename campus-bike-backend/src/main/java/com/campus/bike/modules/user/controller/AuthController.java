package com.campus.bike.modules.user.controller;

import com.campus.bike.common.R;
import com.campus.bike.common.constant.Constants;
import com.campus.bike.modules.user.dto.LoginRequest;
import com.campus.bike.modules.user.dto.LoginResponse;
import com.campus.bike.modules.user.dto.RegisterRequest;
import com.campus.bike.modules.user.entity.User;
import com.campus.bike.modules.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public R<Void> register(@Valid @RequestBody RegisterRequest request) {
        // 开发阶段：验证码固定为 "123456"
        if (!"123456".equals(request.getVerifyCode())) {
            return R.fail("验证码错误");
        }
        boolean success = userService.register(request.getPhone(), request.getPassword(), request.getUsername());
        return success ? R.ok("注册成功", null) : R.fail("手机号已存在");
    }

    @PostMapping("/login")
    public R<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        String token = userService.login(request.getPhone(), request.getPassword());
        if (token == null) {
            return R.fail(401, "手机号或密码错误");
        }
        User user = userService.getUserByPhone(request.getPhone());
        Integer role = user.getRole() == null ? Constants.ROLE_USER : user.getRole();
        LoginResponse data = new LoginResponse(token, user.getId(), user.getUsername(), user.getPhone(), role);
        return R.ok("登录成功", data);
    }
}
