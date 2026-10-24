package com.campus.bike.modules.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private Long userId;
    private String userName;
    private String phone;
    /** 角色：0普通用户 1运维人员 2管理员 */
    private Integer role;
}
