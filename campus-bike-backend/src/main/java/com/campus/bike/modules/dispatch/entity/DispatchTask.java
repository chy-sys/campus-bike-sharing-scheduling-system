package com.campus.bike.modules.dispatch.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 调度任务：把车辆从盈余站点调往缺车站点
 */
@Data
@TableName("dispatch_task")
public class DispatchTask {

    /** 状态：0待调度 1进行中 2已完成 3已取消 */
    public static final int STATUS_PENDING = 0;
    public static final int STATUS_RUNNING = 1;
    public static final int STATUS_FINISHED = 2;
    public static final int STATUS_CANCELLED = 3;

    @TableId(type = IdType.AUTO)
    private Long id;
    /** 调出站点 */
    private Long fromStationId;
    /** 调入站点 */
    private Long toStationId;
    /** 调度车辆数 */
    private Integer bikeCount;
    /** 状态 */
    private Integer status;
    /** 调度员（用户ID） */
    private Long operatorId;
    private String remark;
    private Date createTime;
    private Date updateTime;
    private Date finishTime;
}
