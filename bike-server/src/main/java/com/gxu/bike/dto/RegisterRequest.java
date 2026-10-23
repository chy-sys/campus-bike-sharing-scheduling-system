package com.gxu.bike.dto;

import lombok.Data;

@Data
public class RegisterRequest {
    private String phone;
    private String password;
    private String username;
    private String verifyCode;
}
