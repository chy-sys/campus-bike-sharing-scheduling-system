package com.gxu.bike.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 调度建议：算法计算出的"从哪个站调多少车到哪个站"
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DispatchSuggestion {

    private Long fromStationId;
    private String fromStationName;
    /** 调出站点当前负荷率 */
    private BigDecimal fromRate;
    private Long toStationId;
    private String toStationName;
    /** 调入站点当前负荷率 */
    private BigDecimal toRate;
    /** 建议调度车辆数 */
    private Integer bikeCount;
}
