package com.campus.bike.modules.repair.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.campus.bike.modules.repair.entity.RepairOrder;

public interface RepairOrderService {

    /**
     * 提交报修：车辆置为故障状态
     *
     * @param images    拍照上传的图片地址，多个用逗号分隔
     * @param longitude 报修位置经度
     * @param latitude  报修位置纬度
     */
    Long create(Long reporterId, Long bikeId, Integer faultType, String description,
                String images, java.math.BigDecimal longitude, java.math.BigDecimal latitude);

    /**
     * 受理工单
     */
    void assign(Long id, Long handlerId);

    /**
     * 处理完成：已修复则车辆恢复可用，形成闭环
     */
    void finish(Long id, Long handlerId, String handleResult, boolean fixed);

    /**
     * 分页查询：status 按状态过滤；reporterId 非空时只看该报修人的工单（用户端用）
     */
    IPage<RepairOrder> page(long pageNum, long pageSize, Integer status, Long reporterId);

    RepairOrder getById(Long id);
}
