package com.campus.bike.modules.user.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 修改个人资料
 */
@Data
public class UpdateProfileRequest {

    @NotBlank(message = "用户名不能为空")
    private String username;
}
