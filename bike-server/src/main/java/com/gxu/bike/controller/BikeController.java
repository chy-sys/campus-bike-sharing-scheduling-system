package com.gxu.bike.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.gxu.bike.common.R;
import com.gxu.bike.entity.Bike;
import com.gxu.bike.service.BikeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/bikes")
public class BikeController {

    @Autowired
    private BikeService bikeService;

    @GetMapping
    public R<IPage<Bike>> page(@RequestParam(defaultValue = "1") long pageNum,
                               @RequestParam(defaultValue = "10") long pageSize,
                               @RequestParam(required = false) Long stationId,
                               @RequestParam(required = false) Integer status,
                               @RequestParam(required = false) String keyword) {
        return R.ok(bikeService.page(pageNum, pageSize, stationId, status, keyword));
    }

    @GetMapping("/{id}")
    public R<Bike> detail(@PathVariable Long id) {
        return R.ok(bikeService.getById(id));
    }

    @PostMapping
    public R<Long> create(@RequestBody Bike bike) {
        return R.ok("新增成功", bikeService.create(bike));
    }

    @PutMapping
    public R<Void> update(@RequestBody Bike bike) {
        bikeService.update(bike);
        return R.ok("修改成功", null);
    }

    @DeleteMapping("/{id}")
    public R<Void> remove(@PathVariable Long id) {
        bikeService.remove(id);
        return R.ok("删除成功", null);
    }

    @PutMapping("/{id}/status")
    public R<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        bikeService.updateStatus(id, status);
        return R.ok("状态更新成功", null);
    }
}
