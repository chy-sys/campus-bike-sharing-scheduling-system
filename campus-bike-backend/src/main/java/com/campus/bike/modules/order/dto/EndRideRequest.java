package com.campus.bike.modules.order.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class EndRideRequest {

    @NotNull(message = "还车站点ID不能为空")
    private Long endStationId;
}
