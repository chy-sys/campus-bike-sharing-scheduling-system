package com.campus.bike.common.constant;

/**
 * 通用常量定义
 */
public final class Constants {

    private Constants() {
    }

    /** 日期时间格式 */
    public static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
    public static final String DATE_PATTERN = "yyyy-MM-dd";

    /** 是否：1 是 0 否 */
    public static final int YES = 1;
    public static final int NO = 0;

    /** 车辆状态 */
    public static final int BIKE_STATUS_FAULT = 0;
    public static final int BIKE_STATUS_AVAILABLE = 1;
    public static final int BIKE_STATUS_IN_USE = 2;
    public static final int BIKE_STATUS_REPAIRING = 3;

    /** 站点状态 */
    public static final int STATION_STATUS_DISABLED = 0;
    public static final int STATION_STATUS_ENABLED = 1;

    /** 请求头 */
    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String BEARER_PREFIX = "Bearer ";

    /** 当前登录用户ID（request attribute） */
    public static final String ATTR_USER_ID = "userId";
    public static final String ATTR_PHONE = "phone";
}
