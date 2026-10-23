package com.campus.bike.modules.repair.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.bike.common.constant.Constants;
import com.campus.bike.common.exception.BizException;
import com.campus.bike.modules.repair.entity.RepairOrder;
import com.campus.bike.modules.repair.mapper.RepairOrderMapper;
import com.campus.bike.modules.repair.service.RepairOrderService;
import com.campus.bike.modules.vehicle.entity.Bike;
import com.campus.bike.modules.vehicle.mapper.BikeMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * 报修工单业务：报修 → 受理 → 处理完成闭环
 */
@Service
public class RepairOrderServiceImpl implements RepairOrderService {

    @Autowired
    private RepairOrderMapper repairOrderMapper;
    @Autowired
    private BikeMapper bikeMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(Long reporterId, Long bikeId, Integer faultType, String description) {
        Bike bike = bikeMapper.selectById(bikeId);
        if (bike == null) {
            throw new BizException("车辆不存在");
        }

        // 车辆置为故障，避免被再次租借
        Bike bikeUpdate = new Bike();
        bikeUpdate.setId(bikeId);
        bikeUpdate.setStatus(Constants.BIKE_STATUS_FAULT);
        bikeUpdate.setUpdateTime(new Date());
        bikeMapper.updateById(bikeUpdate);

        Date now = new Date();
        RepairOrder order = new RepairOrder();
        order.setBikeId(bikeId);
        order.setReporterId(reporterId);
        order.setFaultType(faultType);
        order.setDescription(description);
        order.setStatus(RepairOrder.STATUS_PENDING);
        order.setCreateTime(now);
        order.setUpdateTime(now);
        repairOrderMapper.insert(order);
        return order.getId();
    }

    @Override
    public void assign(Long id, Long handlerId) {
        RepairOrder order = getById(id);
        if (!Integer.valueOf(RepairOrder.STATUS_PENDING).equals(order.getStatus())) {
            throw new BizException("只有待处理的工单可以受理");
        }
        RepairOrder update = new RepairOrder();
        update.setId(id);
        update.setStatus(RepairOrder.STATUS_HANDLING);
        update.setHandlerId(handlerId);
        update.setUpdateTime(new Date());
        repairOrderMapper.updateById(update);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void finish(Long id, Long handlerId, String handleResult, boolean fixed) {
        RepairOrder order = getById(id);
        if (Integer.valueOf(RepairOrder.STATUS_FIXED).equals(order.getStatus())
                || Integer.valueOf(RepairOrder.STATUS_UNFIXABLE).equals(order.getStatus())) {
            throw new BizException("工单已处理完成");
        }

        Date now = new Date();
        // 已修复则车辆恢复可用
        if (fixed) {
            Bike bikeUpdate = new Bike();
            bikeUpdate.setId(order.getBikeId());
            bikeUpdate.setStatus(Constants.BIKE_STATUS_AVAILABLE);
            bikeUpdate.setUpdateTime(now);
            bikeMapper.updateById(bikeUpdate);
        }

        RepairOrder update = new RepairOrder();
        update.setId(id);
        update.setStatus(fixed ? RepairOrder.STATUS_FIXED : RepairOrder.STATUS_UNFIXABLE);
        update.setHandlerId(handlerId);
        update.setHandleResult(handleResult);
        update.setFinishTime(now);
        update.setUpdateTime(now);
        repairOrderMapper.updateById(update);
    }

    @Override
    public IPage<RepairOrder> page(long pageNum, long pageSize, Integer status) {
        LambdaQueryWrapper<RepairOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(status != null, RepairOrder::getStatus, status)
                .orderByDesc(RepairOrder::getId);
        return repairOrderMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
    }

    @Override
    public RepairOrder getById(Long id) {
        RepairOrder order = repairOrderMapper.selectById(id);
        if (order == null) {
            throw new BizException("报修工单不存在");
        }
        return order;
    }
}
