package com.campus.bike.modules.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.bike.common.constant.Constants;
import com.campus.bike.common.exception.BizException;
import com.campus.bike.modules.order.entity.RideOrder;
import com.campus.bike.modules.order.mapper.RideOrderMapper;
import com.campus.bike.modules.order.service.RideOrderService;
import com.campus.bike.modules.station.entity.Station;
import com.campus.bike.modules.station.mapper.StationMapper;
import com.campus.bike.modules.vehicle.entity.Bike;
import com.campus.bike.modules.vehicle.mapper.BikeMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.UUID;

/**
 * 骑行订单业务：开锁 → 骑行中 → 还车计费结算
 */
@Service
public class RideOrderServiceImpl implements RideOrderService {

    @Autowired
    private RideOrderMapper rideOrderMapper;
    @Autowired
    private BikeMapper bikeMapper;
    @Autowired
    private StationMapper stationMapper;

    @Value("${bike.billing.base-price:1.50}")
    private BigDecimal basePrice;
    @Value("${bike.billing.base-minutes:15}")
    private int baseMinutes;
    @Value("${bike.billing.unit-price:1.00}")
    private BigDecimal unitPrice;
    @Value("${bike.billing.unit-minutes:15}")
    private int unitMinutes;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long startRide(Long userId, Long bikeId, Long startStationId) {
        Bike bike = bikeMapper.selectById(bikeId);
        if (bike == null) {
            throw new BizException("车辆不存在");
        }
        if (!Integer.valueOf(Constants.BIKE_STATUS_AVAILABLE).equals(bike.getStatus())) {
            throw new BizException("该车辆当前不可使用");
        }

        // 同一用户不能存在未结束的订单
        Long riding = rideOrderMapper.selectCount(new LambdaQueryWrapper<RideOrder>()
                .eq(RideOrder::getUserId, userId)
                .eq(RideOrder::getStatus, RideOrder.STATUS_RIDING));
        if (riding != null && riding > 0) {
            throw new BizException("您有未结束的骑行订单，请先还车");
        }

        // 起始站点车辆数减一
        Station station = stationMapper.selectById(startStationId);
        if (station == null) {
            throw new BizException("起始站点不存在");
        }
        int current = station.getCurrentCount() == null ? 0 : station.getCurrentCount();
        station.setCurrentCount(Math.max(0, current - 1));
        station.setUpdateTime(new Date());
        stationMapper.updateById(station);

        // 车辆置为使用中
        Bike bikeUpdate = new Bike();
        bikeUpdate.setId(bikeId);
        bikeUpdate.setStatus(Constants.BIKE_STATUS_IN_USE);
        bikeUpdate.setUpdateTime(new Date());
        bikeMapper.updateById(bikeUpdate);

        Date now = new Date();
        RideOrder order = new RideOrder();
        order.setOrderNo(UUID.randomUUID().toString().replace("-", "").substring(0, 20).toUpperCase());
        order.setUserId(userId);
        order.setBikeId(bikeId);
        order.setStartStationId(startStationId);
        order.setStartTime(now);
        order.setStatus(RideOrder.STATUS_RIDING);
        order.setAmount(BigDecimal.ZERO);
        order.setCreateTime(now);
        order.setUpdateTime(now);
        rideOrderMapper.insert(order);
        return order.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RideOrder endRide(Long orderId, Long userId, Long endStationId) {
        RideOrder order = getById(orderId);
        if (!Integer.valueOf(RideOrder.STATUS_RIDING).equals(order.getStatus())) {
            throw new BizException("订单已结束，无法重复还车");
        }
        if (userId != null && !userId.equals(order.getUserId())) {
            throw new BizException("只能结束本人的订单");
        }

        Station station = stationMapper.selectById(endStationId);
        if (station == null) {
            throw new BizException("还车站点不存在");
        }

        Date now = new Date();
        long minutes = Math.max(1, (now.getTime() - order.getStartTime().getTime()) / 60000);

        // 目的站点车辆数加一（不超过容量）
        int current = station.getCurrentCount() == null ? 0 : station.getCurrentCount();
        int capacity = station.getCapacity() == null ? Integer.MAX_VALUE : station.getCapacity();
        station.setCurrentCount(Math.min(capacity, current + 1));
        station.setUpdateTime(now);
        stationMapper.updateById(station);

        // 车辆归还到目的站点并置为可用
        Bike bikeUpdate = new Bike();
        bikeUpdate.setId(order.getBikeId());
        bikeUpdate.setStationId(endStationId);
        bikeUpdate.setStatus(Constants.BIKE_STATUS_AVAILABLE);
        bikeUpdate.setUpdateTime(now);
        bikeMapper.updateById(bikeUpdate);

        RideOrder update = new RideOrder();
        update.setId(orderId);
        update.setEndStationId(endStationId);
        update.setEndTime(now);
        update.setDuration((int) minutes);
        update.setAmount(calcFee(minutes));
        update.setStatus(RideOrder.STATUS_FINISHED);
        update.setUpdateTime(now);
        rideOrderMapper.updateById(update);

        return rideOrderMapper.selectById(orderId);
    }

    @Override
    public IPage<RideOrder> page(long pageNum, long pageSize, Long userId, Integer status) {
        LambdaQueryWrapper<RideOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(userId != null, RideOrder::getUserId, userId)
                .eq(status != null, RideOrder::getStatus, status)
                .orderByDesc(RideOrder::getId);
        return rideOrderMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
    }

    @Override
    public RideOrder getById(Long id) {
        RideOrder order = rideOrderMapper.selectById(id);
        if (order == null) {
            throw new BizException("订单不存在");
        }
        return order;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancel(Long orderId, Long userId) {
        RideOrder order = getById(orderId);
        if (!Integer.valueOf(RideOrder.STATUS_RIDING).equals(order.getStatus())) {
            throw new BizException("订单已结束，无法取消");
        }
        if (userId != null && !userId.equals(order.getUserId())) {
            throw new BizException("只能取消本人的订单");
        }

        // 车辆归还起始站点
        Station station = stationMapper.selectById(order.getStartStationId());
        if (station != null) {
            int current = station.getCurrentCount() == null ? 0 : station.getCurrentCount();
            int capacity = station.getCapacity() == null ? Integer.MAX_VALUE : station.getCapacity();
            station.setCurrentCount(Math.min(capacity, current + 1));
            station.setUpdateTime(new Date());
            stationMapper.updateById(station);
        }

        Bike bikeUpdate = new Bike();
        bikeUpdate.setId(order.getBikeId());
        bikeUpdate.setStatus(Constants.BIKE_STATUS_AVAILABLE);
        bikeUpdate.setUpdateTime(new Date());
        bikeMapper.updateById(bikeUpdate);

        RideOrder update = new RideOrder();
        update.setId(orderId);
        update.setStatus(RideOrder.STATUS_CANCELLED);
        update.setEndTime(new Date());
        update.setUpdateTime(new Date());
        rideOrderMapper.updateById(update);
    }

    @Override
    public BigDecimal calcFee(long minutes) {
        if (minutes <= baseMinutes) {
            return basePrice.setScale(2, RoundingMode.HALF_UP);
        }
        long extra = minutes - baseMinutes;
        long units = (extra + unitMinutes - 1) / unitMinutes;
        return basePrice.add(unitPrice.multiply(new BigDecimal(units)))
                .setScale(2, RoundingMode.HALF_UP);
    }
}
