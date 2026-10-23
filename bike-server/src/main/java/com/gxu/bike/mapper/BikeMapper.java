package com.gxu.bike.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gxu.bike.entity.Bike;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BikeMapper extends BaseMapper<Bike> {
}
