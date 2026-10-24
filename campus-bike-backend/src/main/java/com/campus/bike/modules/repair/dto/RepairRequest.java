package com.campus.bike.modules.repair.dto;

import lombok.Data;

import java.math.BigDecimal;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class RepairRequest {

    @NotNull(message = "车辆ID不能为空")
    private Long bikeId;

    @NotNull(message = "故障类型不能为空")
    private Integer faultType;

    @NotBlank(message = "故障描述不能为空")
    private String description;

    /** 拍照上传后的图片地址，多个用逗号分隔 */
    private String images;

    /** 报修定位 */
    private BigDecimal longitude;
    private BigDecimal latitude;
}
