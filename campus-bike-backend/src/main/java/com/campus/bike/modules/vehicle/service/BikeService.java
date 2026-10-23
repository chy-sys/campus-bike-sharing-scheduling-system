package com.campus.bike.modules.vehicle.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.campus.bike.modules.vehicle.entity.Bike;

public interface BikeService {

    /**
     * 分页查询车辆
     */
    IPage<Bike> page(long pageNum, long pageSize, Long stationId, Integer status, String keyword);

    /**
     * 根据ID查询
     */
    Bike getById(Long id);

    /**
     * 新增车辆
     */
    Long create(Bike bike);

    /**
     * 修改车辆
     */
    void update(Bike bike);

    /**
     * 删除车辆
     */
    void remove(Long id);

    /**
     * 修改车辆状态（0故障 1可用 2使用中 3维修中）
     */
    void updateStatus(Long id, Integer status);
}
