package com.campus.bike.modules.station.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.campus.bike.common.R;
import com.campus.bike.modules.station.entity.Station;
import com.campus.bike.modules.station.service.StationService;
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

import java.util.List;

@RestController
@RequestMapping("/api/stations")
public class StationController {

    @Autowired
    private StationService stationService;

    @GetMapping
    public R<IPage<Station>> page(@RequestParam(defaultValue = "1") long pageNum,
                                  @RequestParam(defaultValue = "10") long pageSize,
                                  @RequestParam(required = false) String keyword) {
        return R.ok(stationService.page(pageNum, pageSize, keyword));
    }

    @GetMapping("/all")
    public R<List<Station>> listAll() {
        return R.ok(stationService.listAll());
    }

    @GetMapping("/{id}")
    public R<Station> detail(@PathVariable Long id) {
        return R.ok(stationService.getById(id));
    }

    @PostMapping
    public R<Long> create(@RequestBody Station station) {
        return R.ok("新增成功", stationService.create(station));
    }

    @PutMapping
    public R<Void> update(@RequestBody Station station) {
        stationService.update(station);
        return R.ok("修改成功", null);
    }

    @DeleteMapping("/{id}")
    public R<Void> remove(@PathVariable Long id) {
        stationService.remove(id);
        return R.ok("删除成功", null);
    }
}
