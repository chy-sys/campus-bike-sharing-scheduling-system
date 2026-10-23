package com.gxu.bike.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gxu.bike.entity.Bike;
import com.gxu.bike.exception.BizException;
import com.gxu.bike.mapper.BikeMapper;
import com.gxu.bike.service.BikeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Date;

@Service
public class BikeServiceImpl implements BikeService {

    @Autowired
    private BikeMapper bikeMapper;

    @Override
    public IPage<Bike> page(long pageNum, long pageSize, Long stationId, Integer status, String keyword) {
        LambdaQueryWrapper<Bike> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(stationId != null, Bike::getStationId, stationId)
                .eq(status != null, Bike::getStatus, status)
                .like(StringUtils.hasText(keyword), Bike::getBikenumber, keyword)
                .orderByDesc(Bike::getId);
        return bikeMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
    }

    @Override
    public Bike getById(Long id) {
        Bike bike = bikeMapper.selectById(id);
        if (bike == null) {
            throw new BizException("车辆不存在");
        }
        return bike;
    }

    @Override
    public Long create(Bike bike) {
        bike.setId(null);
        bike.setCreateTime(new Date());
        bike.setUpdateTime(new Date());
        if (bike.getStatus() == null) {
            bike.setStatus(1);
        }
        bikeMapper.insert(bike);
        return bike.getId();
    }

    @Override
    public void update(Bike bike) {
        if (bikeMapper.selectById(bike.getId()) == null) {
            throw new BizException("车辆不存在");
        }
        bike.setUpdateTime(new Date());
        bikeMapper.updateById(bike);
    }

    @Override
    public void remove(Long id) {
        if (bikeMapper.selectById(id) == null) {
            throw new BizException("车辆不存在");
        }
        bikeMapper.deleteById(id);
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        Bike bike = new Bike();
        bike.setId(id);
        bike.setStatus(status);
        bike.setUpdateTime(new Date());
        if (bikeMapper.updateById(bike) == 0) {
            throw new BizException("车辆不存在");
        }
    }
}
