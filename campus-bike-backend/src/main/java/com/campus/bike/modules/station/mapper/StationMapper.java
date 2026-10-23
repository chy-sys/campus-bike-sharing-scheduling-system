package com.campus.bike.modules.station.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.bike.modules.station.entity.Station;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface StationMapper extends BaseMapper<Station> {
}
