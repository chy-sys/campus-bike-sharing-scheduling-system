package com.campus.bike.modules.repair.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.campus.bike.common.R;
import com.campus.bike.common.constant.Constants;
import com.campus.bike.modules.repair.dto.RepairHandleRequest;
import com.campus.bike.modules.repair.dto.RepairRequest;
import com.campus.bike.modules.repair.entity.RepairOrder;
import com.campus.bike.modules.repair.service.RepairOrderService;
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
@RequestMapping("/api/repairs")
public class RepairOrderController {

    @Autowired
    private RepairOrderService repairOrderService;

    @PostMapping
    public R<Long> create(@Valid @RequestBody RepairRequest request, HttpServletRequest httpRequest) {
        Long userId = currentUserId(httpRequest);
        return R.ok("报修提交成功",
                repairOrderService.create(userId, request.getBikeId(), request.getFaultType(), request.getDescription()));
    }

    @GetMapping
    public R<IPage<RepairOrder>> page(@RequestParam(defaultValue = "1") long pageNum,
                                      @RequestParam(defaultValue = "10") long pageSize,
                                      @RequestParam(required = false) Integer status) {
        return R.ok(repairOrderService.page(pageNum, pageSize, status));
    }

    @GetMapping("/{id}")
    public R<RepairOrder> detail(@PathVariable Long id) {
        return R.ok(repairOrderService.getById(id));
    }

    @PutMapping("/{id}/assign")
    public R<Void> assign(@PathVariable Long id, HttpServletRequest httpRequest) {
        repairOrderService.assign(id, currentUserId(httpRequest));
        return R.ok("已受理", null);
    }

    @PutMapping("/{id}/finish")
    public R<Void> finish(@PathVariable Long id,
                          @RequestBody RepairHandleRequest request,
                          HttpServletRequest httpRequest) {
        boolean fixed = request.getFixed() == null || request.getFixed();
        repairOrderService.finish(id, currentUserId(httpRequest), request.getHandleResult(), fixed);
        return R.ok("工单已处理", null);
    }

    private Long currentUserId(HttpServletRequest request) {
        Object userId = request.getAttribute(Constants.ATTR_USER_ID);
        return userId == null ? null : Long.valueOf(String.valueOf(userId));
    }
}
