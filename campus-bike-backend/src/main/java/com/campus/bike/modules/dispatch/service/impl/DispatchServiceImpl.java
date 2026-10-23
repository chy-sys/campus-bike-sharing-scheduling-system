package com.campus.bike.modules.dispatch.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.bike.modules.dispatch.dto.DispatchSuggestion;
import com.campus.bike.modules.dispatch.entity.DispatchTask;
import com.campus.bike.modules.station.entity.Station;
import com.campus.bike.common.exception.BizException;
import com.campus.bike.modules.dispatch.mapper.DispatchTaskMapper;
import com.campus.bike.modules.station.mapper.StationMapper;
import com.campus.bike.modules.dispatch.service.DispatchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * 调度业务：站点负荷率分析 + 贪心配对生成调度建议
 */
@Service
public class DispatchServiceImpl implements DispatchService {

    /** 高水位：负荷率高于此值视为盈余站点 */
    private static final BigDecimal HIGH_WATER = new BigDecimal("0.8");
    /** 低水位：负荷率低于此值视为缺车站点 */
    private static final BigDecimal LOW_WATER = new BigDecimal("0.2");
    /** 目标水位：调度后期望达到的负荷率 */
    private static final BigDecimal TARGET_RATE = new BigDecimal("0.5");

    @Autowired
    private DispatchTaskMapper dispatchTaskMapper;

    @Autowired
    private StationMapper stationMapper;

    @Override
    public List<DispatchSuggestion> suggest() {
        List<Station> stations = stationMapper.selectList(
                new LambdaQueryWrapper<Station>().eq(Station::getStatus, 1));
        if (CollectionUtils.isEmpty(stations)) {
            return new ArrayList<>();
        }

        List<Balance> surplusList = new ArrayList<>();
        List<Balance> deficitList = new ArrayList<>();

        for (Station station : stations) {
            BigDecimal capacity = toDecimal(station.getCapacity());
            if (capacity.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }
            BigDecimal current = toDecimal(station.getCurrentCount());
            BigDecimal rate = current.divide(capacity, 2, RoundingMode.HALF_UP);
            // 相对目标水位的差值：正数表示可外调，负数表示需要调入
            int diff = current.subtract(capacity.multiply(TARGET_RATE))
                    .setScale(0, RoundingMode.HALF_UP).intValue();

            Balance balance = new Balance(station, rate, Math.abs(diff));
            if (diff > 0 && rate.compareTo(HIGH_WATER) > 0) {
                surplusList.add(balance);
            } else if (diff < 0 && rate.compareTo(LOW_WATER) < 0) {
                deficitList.add(balance);
            }
        }

        // 盈余多的站点优先外调，缺口大的站点优先补车
        surplusList.sort(Comparator.comparingInt(Balance::getAmount).reversed());
        deficitList.sort(Comparator.comparingInt(Balance::getAmount).reversed());

        List<DispatchSuggestion> suggestions = new ArrayList<>();
        int i = 0;
        int j = 0;
        while (i < surplusList.size() && j < deficitList.size()) {
            Balance surplus = surplusList.get(i);
            Balance deficit = deficitList.get(j);
            int count = Math.min(surplus.getAmount(), deficit.getAmount());
            if (count <= 0) {
                break;
            }
            suggestions.add(new DispatchSuggestion(
                    surplus.getStation().getId(),
                    surplus.getStation().getName(),
                    surplus.getRate(),
                    deficit.getStation().getId(),
                    deficit.getStation().getName(),
                    deficit.getRate(),
                    count));

            surplus.setAmount(surplus.getAmount() - count);
            deficit.setAmount(deficit.getAmount() - count);
            if (surplus.getAmount() == 0) {
                i++;
            }
            if (deficit.getAmount() == 0) {
                j++;
            }
        }
        return suggestions;
    }

    @Override
    public Long createTask(DispatchTask task) {
        if (task.getFromStationId() == null || task.getToStationId() == null) {
            throw new BizException("调出站点与调入站点不能为空");
        }
        if (task.getFromStationId().equals(task.getToStationId())) {
            throw new BizException("调出站点与调入站点不能相同");
        }
        if (task.getBikeCount() == null || task.getBikeCount() <= 0) {
            throw new BizException("调度车辆数必须大于0");
        }
        Station from = stationMapper.selectById(task.getFromStationId());
        Station to = stationMapper.selectById(task.getToStationId());
        if (from == null || to == null) {
            throw new BizException("站点不存在");
        }
        int available = from.getCurrentCount() == null ? 0 : from.getCurrentCount();
        if (available < task.getBikeCount()) {
            throw new BizException("调出站点车辆不足，当前仅 " + available + " 辆");
        }

        task.setId(null);
        task.setStatus(DispatchTask.STATUS_PENDING);
        task.setCreateTime(new Date());
        task.setUpdateTime(new Date());
        dispatchTaskMapper.insert(task);
        return task.getId();
    }

    @Override
    public IPage<DispatchTask> page(long pageNum, long pageSize, Integer status) {
        LambdaQueryWrapper<DispatchTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(status != null, DispatchTask::getStatus, status)
                .orderByDesc(DispatchTask::getId);
        return dispatchTaskMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
    }

    @Override
    public DispatchTask getById(Long id) {
        DispatchTask task = dispatchTaskMapper.selectById(id);
        if (task == null) {
            throw new BizException("调度任务不存在");
        }
        return task;
    }

    @Override
    public void start(Long id) {
        DispatchTask task = getById(id);
        if (!isStatus(task, DispatchTask.STATUS_PENDING)) {
            throw new BizException("只有待调度的任务可以开始");
        }
        DispatchTask update = new DispatchTask();
        update.setId(id);
        update.setStatus(DispatchTask.STATUS_RUNNING);
        update.setUpdateTime(new Date());
        dispatchTaskMapper.updateById(update);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void finish(Long id, Long operatorId) {
        DispatchTask task = getById(id);
        if (isStatus(task, DispatchTask.STATUS_FINISHED)) {
            throw new BizException("任务已完成");
        }
        if (isStatus(task, DispatchTask.STATUS_CANCELLED)) {
            throw new BizException("任务已取消，无法完成");
        }

        // 调出站点减车
        Station from = stationMapper.selectById(task.getFromStationId());
        int fromCount = from.getCurrentCount() == null ? 0 : from.getCurrentCount();
        from.setCurrentCount(Math.max(0, fromCount - task.getBikeCount()));
        from.setUpdateTime(new Date());
        stationMapper.updateById(from);

        // 调入站点加车（不超过容量上限）
        Station to = stationMapper.selectById(task.getToStationId());
        int toCount = to.getCurrentCount() == null ? 0 : to.getCurrentCount();
        int capacity = to.getCapacity() == null ? Integer.MAX_VALUE : to.getCapacity();
        to.setCurrentCount(Math.min(capacity, toCount + task.getBikeCount()));
        to.setUpdateTime(new Date());
        stationMapper.updateById(to);

        DispatchTask update = new DispatchTask();
        update.setId(id);
        update.setStatus(DispatchTask.STATUS_FINISHED);
        update.setOperatorId(operatorId);
        update.setFinishTime(new Date());
        update.setUpdateTime(new Date());
        dispatchTaskMapper.updateById(update);
    }

    @Override
    public void cancel(Long id) {
        DispatchTask task = getById(id);
        if (isStatus(task, DispatchTask.STATUS_FINISHED)) {
            throw new BizException("任务已完成，无法取消");
        }
        DispatchTask update = new DispatchTask();
        update.setId(id);
        update.setStatus(DispatchTask.STATUS_CANCELLED);
        update.setUpdateTime(new Date());
        dispatchTaskMapper.updateById(update);
    }

    private boolean isStatus(DispatchTask task, int status) {
        return task.getStatus() != null && task.getStatus() == status;
    }

    private BigDecimal toDecimal(Integer value) {
        return value == null ? BigDecimal.ZERO : new BigDecimal(value);
    }

    /**
     * 站点盈亏中间结果
     */
    private static class Balance {
        private final Station station;
        private final BigDecimal rate;
        private int amount;

        Balance(Station station, BigDecimal rate, int amount) {
            this.station = station;
            this.rate = rate;
            this.amount = amount;
        }

        public Station getStation() {
            return station;
        }

        public BigDecimal getRate() {
            return rate;
        }

        public int getAmount() {
            return amount;
        }

        public void setAmount(int amount) {
            this.amount = amount;
        }
    }
}
