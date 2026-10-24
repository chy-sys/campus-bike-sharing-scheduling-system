package com.campus.bike.modules.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.bike.common.constant.Constants;
import com.campus.bike.common.exception.BizException;
import com.campus.bike.modules.user.entity.User;
import com.campus.bike.modules.user.mapper.UserMapper;
import com.campus.bike.modules.user.service.UserService;
import com.campus.bike.common.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

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
        // 注册默认是普通用户，运维/管理员由后台审核提升
        user.setRole(Constants.ROLE_USER);
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
        // 生成 Token（携带角色）
        Integer role = user.getRole() == null ? Constants.ROLE_USER : user.getRole();
        return jwtUtil.generateToken(user.getId(), user.getPhone(), role);
    }

    @Override
    public User getUserByPhone(String phone) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getPhone, phone);
        return userMapper.selectOne(wrapper);
    }

    @Override
    public IPage<User> page(long pageNum, long pageSize) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(User::getId);
        IPage<User> page = userMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        // 不返回密码
        page.getRecords().forEach(user -> user.setPassword(null));
        return page;
    }

    @Override
    public void updateRole(Long id, Integer role) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BizException("用户不存在");
        }
        if (role == null || role < Constants.ROLE_USER || role > Constants.ROLE_ADMIN) {
            throw new BizException("角色取值非法");
        }
        User update = new User();
        update.setId(id);
        update.setRole(role);
        update.setUpdateTime(new Date());
        userMapper.updateById(update);
    }

    @Override
    public void updateProfile(Long userId, String username) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BizException("用户不存在");
        }
        User update = new User();
        update.setId(userId);
        update.setUsername(username);
        update.setUpdateTime(new Date());
        userMapper.updateById(update);
    }

    @Override
    public void updatePassword(Long userId, String oldPassword, String newPassword) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BizException("用户不存在");
        }
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BizException("原密码不正确");
        }
        User update = new User();
        update.setId(userId);
        update.setPassword(passwordEncoder.encode(newPassword));
        update.setUpdateTime(new Date());
        userMapper.updateById(update);
    }
}
