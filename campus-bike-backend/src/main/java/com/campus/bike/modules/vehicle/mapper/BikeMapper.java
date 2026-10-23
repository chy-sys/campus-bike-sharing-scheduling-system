package com.campus.bike.modules.vehicle.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.bike.modules.vehicle.entity.Bike;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BikeMapper extends BaseMapper<Bike> {
}
