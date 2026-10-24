package com.campus.bike.modules.user.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 修改密码
 */
@Data
public class UpdatePasswordRequest {

    @NotBlank(message = "原密码不能为空")
    private String oldPassword;

    @NotBlank(message = "新密码不能为空")
    private String newPassword;
}
