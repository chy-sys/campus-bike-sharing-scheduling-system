package com.campus.bike.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.bike.common.R;
import com.campus.bike.common.constant.Constants;
import com.campus.bike.common.exception.BizException;
import com.campus.bike.modules.dispatch.entity.DispatchTask;
import com.campus.bike.modules.dispatch.mapper.DispatchTaskMapper;
import com.campus.bike.modules.order.entity.RideOrder;
import com.campus.bike.modules.order.mapper.RideOrderMapper;
import com.campus.bike.modules.repair.entity.RepairOrder;
import com.campus.bike.modules.repair.mapper.RepairOrderMapper;
import com.campus.bike.modules.station.mapper.StationMapper;
import com.campus.bike.modules.user.mapper.UserMapper;
import com.campus.bike.modules.vehicle.entity.Bike;
import com.campus.bike.modules.vehicle.mapper.BikeMapper;
import com.campus.bike.system.dto.StatsOverview;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * 数据统计：车辆周转率、故障率、调度次数、工单办结率等核心运维指标
 */
@RestController
@RequestMapping("/api/stats")
public class StatsController {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private BikeMapper bikeMapper;
    @Autowired
    private StationMapper stationMapper;
    @Autowired
    private RideOrderMapper rideOrderMapper;
    @Autowired
    private DispatchTaskMapper dispatchTaskMapper;
    @Autowired
    private RepairOrderMapper repairOrderMapper;

    @GetMapping("/overview")
    public R<StatsOverview> overview(HttpServletRequest request) {
        requireStaffOrAdmin(request);

        Date todayStart = todayStart();

        StatsOverview stats = new StatsOverview();
        stats.setUserTotal(userMapper.selectCount(null));
        stats.setStationTotal(stationMapper.selectCount(null));

        stats.setBikeTotal(bikeMapper.selectCount(null));
        stats.setBikeAvailable(countBike(Constants.BIKE_STATUS_AVAILABLE));
        stats.setBikeFault(countBike(Constants.BIKE_STATUS_FAULT));

        stats.setOrderTotal(rideOrderMapper.selectCount(null));
        stats.setOrderToday(countOrderToday(todayStart));
        stats.setOrderAmountTotal(sumOrderAmount());

        stats.setDispatchTotal(dispatchTaskMapper.selectCount(null));
        stats.setDispatchFinished(countDispatch(DispatchTask.STATUS_FINISHED));
        stats.setDispatchToday(countDispatchToday(todayStart));

        stats.setRepairTotal(repairOrderMapper.selectCount(null));
        stats.setRepairFinished(countRepairFinished());

        stats.setBikeTurnoverRate(ratio(stats.getOrderTotal(), stats.getBikeTotal()));
        stats.setFaultRate(ratio(stats.getBikeFault(), stats.getBikeTotal()));
        stats.setRepairFinishRate(ratio(stats.getRepairFinished(), stats.getRepairTotal()));

        return R.ok(stats);
    }

    private long countBike(int status) {
        LambdaQueryWrapper<Bike> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Bike::getStatus, status);
        return bikeMapper.selectCount(wrapper);
    }

    private long countOrderToday(Date todayStart) {
        LambdaQueryWrapper<RideOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.ge(RideOrder::getCreateTime, todayStart);
        return rideOrderMapper.selectCount(wrapper);
    }

    private long countDispatch(int status) {
        LambdaQueryWrapper<DispatchTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DispatchTask::getStatus, status);
        return dispatchTaskMapper.selectCount(wrapper);
    }

    private long countDispatchToday(Date todayStart) {
        LambdaQueryWrapper<DispatchTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.ge(DispatchTask::getCreateTime, todayStart);
        return dispatchTaskMapper.selectCount(wrapper);
    }

    /** 已办结：已修复(2) 或 无法修复(3) */
    private long countRepairFinished() {
        LambdaQueryWrapper<RepairOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(RepairOrder::getStatus,
                java.util.Arrays.asList(RepairOrder.STATUS_FIXED, RepairOrder.STATUS_UNFIXABLE));
        return repairOrderMapper.selectCount(wrapper);
    }

    private BigDecimal sumOrderAmount() {
        LambdaQueryWrapper<RideOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RideOrder::getStatus, RideOrder.STATUS_FINISHED);
        List<RideOrder> orders = rideOrderMapper.selectList(wrapper);
        return orders.stream()
                .map(RideOrder::getAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal ratio(Long numerator, Long denominator) {
        if (denominator == null || denominator == 0L || numerator == null) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(numerator)
                .divide(BigDecimal.valueOf(denominator), 4, RoundingMode.HALF_UP);
    }

    private Date todayStart() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    /** 运维人员与管理员可查看统计 */
    private void requireStaffOrAdmin(HttpServletRequest request) {
        Object role = request.getAttribute(Constants.ATTR_ROLE);
        int value = role == null ? Constants.ROLE_USER : ((Number) role).intValue();
        if (value != Constants.ROLE_ADMIN && value != Constants.ROLE_STAFF) {
            throw new BizException("无权限查看统计，仅运维人员或管理员可访问");
        }
    }
}
