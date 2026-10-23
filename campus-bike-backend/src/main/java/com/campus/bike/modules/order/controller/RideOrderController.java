package com.campus.bike.modules.order.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.campus.bike.common.R;
import com.campus.bike.common.constant.Constants;
import com.campus.bike.modules.order.dto.EndRideRequest;
import com.campus.bike.modules.order.dto.StartRideRequest;
import com.campus.bike.modules.order.entity.RideOrder;
import com.campus.bike.modules.order.service.RideOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@RestController
@RequestMapping("/api/orders")
public class RideOrderController {

    @Autowired
    private RideOrderService rideOrderService;

    @PostMapping("/start")
    public R<Long> start(@Valid @RequestBody StartRideRequest request, HttpServletRequest httpRequest) {
        Long userId = currentUserId(httpRequest);
        return R.ok("开锁成功", rideOrderService.startRide(userId, request.getBikeId(), request.getStartStationId()));
    }

    @PutMapping("/{id}/end")
    public R<RideOrder> end(@PathVariable Long id,
                            @Valid @RequestBody EndRideRequest request,
                            HttpServletRequest httpRequest) {
        RideOrder order = rideOrderService.endRide(id, currentUserId(httpRequest), request.getEndStationId());
        return R.ok("还车成功，本次费用 " + order.getAmount() + " 元", order);
    }

    @GetMapping
    public R<IPage<RideOrder>> page(@RequestParam(defaultValue = "1") long pageNum,
                                    @RequestParam(defaultValue = "10") long pageSize,
                                    @RequestParam(required = false) Integer status,
                                    HttpServletRequest httpRequest) {
        return R.ok(rideOrderService.page(pageNum, pageSize, currentUserId(httpRequest), status));
    }

    @GetMapping("/{id}")
    public R<RideOrder> detail(@PathVariable Long id) {
        return R.ok(rideOrderService.getById(id));
    }

    @PutMapping("/{id}/cancel")
    public R<Void> cancel(@PathVariable Long id, HttpServletRequest httpRequest) {
        rideOrderService.cancel(id, currentUserId(httpRequest));
        return R.ok("订单已取消", null);
    }

    private Long currentUserId(HttpServletRequest request) {
        Object userId = request.getAttribute(Constants.ATTR_USER_ID);
        if (userId == null) {
            return null;
        }
        return Long.valueOf(String.valueOf(userId));
    }
}
