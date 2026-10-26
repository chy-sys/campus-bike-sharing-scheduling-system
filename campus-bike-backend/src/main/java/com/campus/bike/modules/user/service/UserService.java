package com.campus.bike.modules.user.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
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

    /**
     * 根据微信 openid 查询用户（运维小程序免密登录）
     */
    User getUserByOpenid(String openid);

    /**
     * 微信免密登录：openid 已绑定账号时直接签发 token
     * @return token，未绑定或账号禁用返回 null
     */
    String loginByOpenid(String openid);

    /**
     * 将微信 openid 绑定到已有账号
     */
    void bindOpenid(String openid, String phone);

    /**
     * 用户列表（管理员）
     */
    IPage<User> page(long pageNum, long pageSize);

    /**
     * 审核并设置用户角色（管理员）
     */
    void updateRole(Long id, Integer role);

    /**
     * 修改个人资料
     */
    void updateProfile(Long userId, String username);

    /**
     * 修改密码
     */
    void updatePassword(Long userId, String oldPassword, String newPassword);
}
