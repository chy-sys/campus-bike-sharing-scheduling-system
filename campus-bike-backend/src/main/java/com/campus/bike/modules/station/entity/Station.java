package com.campus.bike.modules.station.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@TableName("station")
public class Station {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String area;
    private BigDecimal longitude;
    private BigDecimal latitude;
    private Integer capacity;
    /** 对应数据库列 current_count */
    @TableField("current_count")
    private Integer currentCount;
    private Integer status;
    private Date createTime;
    private Date updateTime;
}
