package com.campus.bike.modules.user.service;

import com.campus.bike.modules.user.entity.User;

public interface UserService {
    /**
     * 用户注册
     * @param phone 手机号
     * @param password 明文密码
     * @param username 用户名
     * @return true成功 false手机号已存在
     */
    boolean register(String phone, String password, String username);

    /**
     * 用户登录
     * @param phone 手机号
     * @param password 明文密码
     * @return jwt token，失败返回null
     */
    String login(String phone, String password);

    /**
     * 根据手机号查询用户
     * @param phone 手机号
     * @return 用户实体
     */
    User getUserByPhone(String phone);
}
