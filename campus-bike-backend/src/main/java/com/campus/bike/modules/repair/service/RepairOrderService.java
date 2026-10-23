package com.campus.bike.modules.repair.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.campus.bike.modules.repair.entity.RepairOrder;

public interface RepairOrderService {

    /**
     * 提交报修：车辆置为故障状态
     */
    Long create(Long reporterId, Long bikeId, Integer faultType, String description);

    /**
     * 受理工单
     */
    void assign(Long id, Long handlerId);

    /**
     * 处理完成：已修复则车辆恢复可用，形成闭环
     */
    void finish(Long id, Long handlerId, String handleResult, boolean fixed);

    IPage<RepairOrder> page(long pageNum, long pageSize, Integer status);

    RepairOrder getById(Long id);
}
