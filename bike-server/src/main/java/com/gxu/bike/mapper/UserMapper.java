package com.gxu.bike.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gxu.bike.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * 合并原三个服务各自复制的 UserMapper，全局唯一
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
}
