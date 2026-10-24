package com.campus.bike.modules.order.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 骑行订单
 */
@Data
@TableName("ride_order")
public class RideOrder {

    /** 状态：0骑行中 1已完成 2已取消 */
    public static final int STATUS_RIDING = 0;
    public static final int STATUS_FINISHED = 1;
    public static final int STATUS_CANCELLED = 2;

    @TableId(type = IdType.AUTO)
    private Long id;
    /** 订单号 */
    private String orderNo;
    private Long userId;
    private Long bikeId;
    private Long startStationId;
    /** 起点经纬度 */
    private BigDecimal startLongitude;
    private BigDecimal startLatitude;
    private Long endStationId;
    /** 终点经纬度 */
    private BigDecimal endLongitude;
    private BigDecimal endLatitude;
    private Date startTime;
    private Date endTime;
    /** 骑行时长（分钟） */
    private Integer duration;
    /** 订单金额（元） */
    private BigDecimal amount;
    /** 状态 */
    private Integer status;
    private Date createTime;
    private Date updateTime;
}
