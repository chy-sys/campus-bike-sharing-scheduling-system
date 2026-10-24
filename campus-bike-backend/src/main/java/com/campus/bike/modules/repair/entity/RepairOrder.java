package com.campus.bike.modules.repair.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 故障报修工单
 */
@Data
@TableName("repair_order")
public class RepairOrder {

    /** 状态：0待处理 1处理中 2已修复 3无法修复 */
    public static final int STATUS_PENDING = 0;
    public static final int STATUS_HANDLING = 1;
    public static final int STATUS_FIXED = 2;
    public static final int STATUS_UNFIXABLE = 3;

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long bikeId;
    /** 报修人 */
    private Long reporterId;
    /** 故障类型：1刹车失灵 2轮胎损坏 3链条故障 4电量不足 5其他 */
    private Integer faultType;
    private String description;
    /** 报修图片地址，多个用逗号分隔 */
    private String images;
    /** 报修位置 */
    private BigDecimal longitude;
    private BigDecimal latitude;
    /** 状态 */
    private Integer status;
    /** 处理人 */
    private Long handlerId;
    /** 处理结果 */
    private String handleResult;
    private Date createTime;
    private Date updateTime;
    private Date finishTime;
}
