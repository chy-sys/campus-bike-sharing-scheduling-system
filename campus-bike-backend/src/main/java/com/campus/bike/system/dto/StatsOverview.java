package com.campus.bike.system.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 运营数据总览：供运维小程序与 Web 管理后台展示
 */
@Data
public class StatsOverview {

    /** 用户总数 */
    private Long userTotal;
    /** 站点总数 */
    private Long stationTotal;

    /** 车辆总数 */
    private Long bikeTotal;
    /** 可用车辆 */
    private Long bikeAvailable;
    /** 故障车辆 */
    private Long bikeFault;

    /** 订单总数 */
    private Long orderTotal;
    /** 今日订单 */
    private Long orderToday;
    /** 订单总金额 */
    private BigDecimal orderAmountTotal;

    /** 调度任务总数 */
    private Long dispatchTotal;
    /** 已完成的调度任务 */
    private Long dispatchFinished;
    /** 今日调度次数 */
    private Long dispatchToday;

    /** 报修工单总数 */
    private Long repairTotal;
    /** 已办结工单（已修复 + 无法修复） */
    private Long repairFinished;

    /** 车辆周转率 = 已完成订单数 / 车辆总数 */
    private BigDecimal bikeTurnoverRate;
    /** 车辆故障率 = 故障车辆 / 车辆总数 */
    private BigDecimal faultRate;
    /** 工单办结率 = 已办结工单 / 工单总数 */
    private BigDecimal repairFinishRate;
}
