package com.gxu.bike.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.gxu.bike.entity.Station;

import java.util.List;

public interface StationService {

    /**
     * 分页查询站点
     */
    IPage<Station> page(long pageNum, long pageSize, String keyword);

    /**
     * 查询全部站点（不分页，供下拉选择使用）
     */
    List<Station> listAll();

    /**
     * 根据ID查询
     */
    Station getById(Long id);

    /**
     * 新增站点
     */
    Long create(Station station);

    /**
     * 修改站点
     */
    void update(Station station);

    /**
     * 删除站点
     */
    void remove(Long id);
}
