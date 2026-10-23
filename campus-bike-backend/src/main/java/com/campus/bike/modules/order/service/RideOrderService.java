package com.campus.bike.modules.order.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.campus.bike.modules.order.entity.RideOrder;

public interface RideOrderService {

    /**
     * 开始骑行：校验车辆可用、用户无未结束订单，扣减起始站点车辆数
     */
    Long startRide(Long userId, Long bikeId, Long startStationId);

    /**
     * 结束骑行：计算时长与费用，更新车辆归属站点，增加目的站点车辆数
     */
    RideOrder endRide(Long orderId, Long userId, Long endStationId);

    /**
     * 分页查询我的订单
     */
    IPage<RideOrder> page(long pageNum, long pageSize, Long userId, Integer status);

    /**
     * 订单详情
     */
    RideOrder getById(Long id);

    /**
     * 取消订单：车辆归还起始站点
     */
    void cancel(Long orderId, Long userId);

    /**
     * 计算骑行费用（分钟 → 金额）
     */
    java.math.BigDecimal calcFee(long minutes);
}
