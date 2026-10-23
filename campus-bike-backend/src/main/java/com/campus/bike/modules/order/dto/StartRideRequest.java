package com.campus.bike.modules.order.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class StartRideRequest {

    @NotNull(message = "车辆ID不能为空")
    private Long bikeId;

    @NotNull(message = "起始站点ID不能为空")
    private Long startStationId;
}
