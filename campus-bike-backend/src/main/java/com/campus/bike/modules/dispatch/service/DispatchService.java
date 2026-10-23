package com.campus.bike.modules.dispatch.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.campus.bike.modules.dispatch.dto.DispatchSuggestion;
import com.campus.bike.modules.dispatch.entity.DispatchTask;

import java.util.List;

public interface DispatchService {

    /**
     * 生成调度建议：按站点负荷率计算盈余与缺口，贪心配对
     */
    List<DispatchSuggestion> suggest();

    /**
     * 创建调度任务
     */
    Long createTask(DispatchTask task);

    /**
     * 分页查询调度任务
     */
    IPage<DispatchTask> page(long pageNum, long pageSize, Integer status);

    /**
     * 根据ID查询
     */
    DispatchTask getById(Long id);

    /**
     * 开始调度
     */
    void start(Long id);

    /**
     * 完成调度：同时更新两端站点的在站车辆数
     */
    void finish(Long id, Long operatorId);

    /**
     * 取消调度
     */
    void cancel(Long id);
}
