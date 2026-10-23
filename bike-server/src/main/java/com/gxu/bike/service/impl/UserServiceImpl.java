package com.gxu.bike.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gxu.bike.entity.User;
import com.gxu.bike.mapper.UserMapper;
import com.gxu.bike.service.UserService;
import com.gxu.bike.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtUtil jwtUtil;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public boolean register(String phone, String password, String username) {
        // 检查手机号是否已存在
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getPhone, phone);
        if (userMapper.selectCount(wrapper) > 0) {
            return false;
        }

        User user = new User();
        user.setPhone(phone);
        user.setPassword(passwordEncoder.encode(password));
        user.setUsername(username);
        user.setStatus(1);
        user.setCreateTime(new Date());
        user.setUpdateTime(new Date());
        return userMapper.insert(user) > 0;
    }

    @Override
    public String login(String phone, String password) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getPhone, phone);
        User user = userMapper.selectOne(wrapper);
        if (user == null || user.getStatus() == 0) {
            return null;
        }
        // 验证密码
        if (!passwordEncoder.matches(password, user.getPassword())) {
            return null;
        }
        // 生成 Token
        return jwtUtil.generateToken(user.getId(), user.getPhone());
    }

    @Override
    public User getUserByPhone(String phone) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getPhone, phone);
        return userMapper.selectOne(wrapper);
    }
}
