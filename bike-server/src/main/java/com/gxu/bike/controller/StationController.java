package com.gxu.bike.controller;

import com.gxu.bike.entity.Station;
import com.gxu.bike.mapper.StationMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/test")
public class StationController {

    @Autowired
    private StationMapper stationMapper;

    @GetMapping("/stations")
    public List<Station> getStations() {
        return stationMapper.selectList(null);
    }
}
