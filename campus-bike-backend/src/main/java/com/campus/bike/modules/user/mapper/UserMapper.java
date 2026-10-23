package com.campus.bike.modules.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.bike.modules.user.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * 合并原三个服务各自复制的 UserMapper，全局唯一
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
}
