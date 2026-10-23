package com.campus.bike.modules.vehicle.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@TableName("bike")
public class Bike {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String bikenumber;
    private BigDecimal longitude;
    private BigDecimal latitude;
    private Integer battery;
    private Integer status;
    private Date createTime;
    private Date updateTime;
    private Long stationId;
}
