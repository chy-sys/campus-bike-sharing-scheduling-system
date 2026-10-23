package com.campus.bike.modules.station.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.bike.modules.station.entity.Station;
import com.campus.bike.common.exception.BizException;
import com.campus.bike.modules.station.mapper.StationMapper;
import com.campus.bike.modules.station.service.StationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.List;

@Service
public class StationServiceImpl implements StationService {

    @Autowired
    private StationMapper stationMapper;

    @Override
    public IPage<Station> page(long pageNum, long pageSize, String keyword) {
        LambdaQueryWrapper<Station> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like(Station::getName, keyword).or().like(Station::getArea, keyword);
        }
        wrapper.orderByDesc(Station::getId);
        return stationMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
    }

    @Override
    public List<Station> listAll() {
        return stationMapper.selectList(new LambdaQueryWrapper<Station>().orderByAsc(Station::getId));
    }

    @Override
    public Station getById(Long id) {
        Station station = stationMapper.selectById(id);
        if (station == null) {
            throw new BizException("站点不存在");
        }
        return station;
    }

    @Override
    public Long create(Station station) {
        station.setId(null);
        station.setCreateTime(new Date());
        station.setUpdateTime(new Date());
        if (station.getStatus() == null) {
            station.setStatus(1);
        }
        stationMapper.insert(station);
        return station.getId();
    }

    @Override
    public void update(Station station) {
        if (stationMapper.selectById(station.getId()) == null) {
            throw new BizException("站点不存在");
        }
        station.setUpdateTime(new Date());
        stationMapper.updateById(station);
    }

    @Override
    public void remove(Long id) {
        if (stationMapper.selectById(id) == null) {
            throw new BizException("站点不存在");
        }
        stationMapper.deleteById(id);
    }
}
