package com.campus.bike.modules.user.dto;

import lombok.Data;

/**
 * 微信绑定参数：首次授权登录后，用手机号+密码关联已有账号
 */
@Data
public class WeChatBindRequest {

    /** 微信登录返回的 openid */
    @javax.validation.constraints.NotBlank(message = "openid 不能为空")
    private String openid;

    @javax.validation.constraints.NotBlank(message = "手机号不能为空")
    private String phone;

    @javax.validation.constraints.NotBlank(message = "密码不能为空")
    private String password;
}
