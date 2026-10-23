package com.campus.bike.modules.repair.dto;

import lombok.Data;

@Data
public class RepairHandleRequest {

    /** 处理结果描述 */
    private String handleResult;

    /** 是否已修复：true 车辆恢复可用，false 标记无法修复 */
    private Boolean fixed;
}
