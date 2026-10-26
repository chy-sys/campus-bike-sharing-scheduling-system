package com.campus.bike.modules.user.dto;

import lombok.Data;

/**
 * 微信小程序登录参数：wx.login 获取到的临时凭证
 */
@Data
public class WeChatLoginRequest {

    @javax.validation.constraints.NotBlank(message = "code 不能为空")
    private String code;
}
