package com.gxu.bike.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 合并原 user/core/dispatch 三份 User 实体（同表 sys_user），字段取并集
 */
@Data
@TableName("sys_user")
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String username;
    private String phone;
    private Date createTime;
    private Date updateTime;
    private String password;
    private Integer status;
    private Integer role;
}
