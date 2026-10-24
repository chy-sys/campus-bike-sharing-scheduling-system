-- 补充字段：用户虚拟账户、报修图片与定位、订单骑行位置
-- 执行环境：MySQL 8，数据库 bike_db
-- 若提示列已存在，可忽略该条后继续执行下一条

-- 1. 用户虚拟账户余额（用于骑行扣费，模拟支付）
ALTER TABLE sys_user
    ADD COLUMN balance DECIMAL(10, 2) NOT NULL DEFAULT 0.00 COMMENT '虚拟账户余额';

-- 2. 报修工单：拍照上传的图片、精准定位
ALTER TABLE repair_order
    ADD COLUMN images VARCHAR(500) COMMENT '报修图片URL，多个用逗号分隔' AFTER description,
    ADD COLUMN longitude DECIMAL(10, 6) COMMENT '报修位置经度' AFTER images,
    ADD COLUMN latitude DECIMAL(10, 6) COMMENT '报修位置纬度' AFTER longitude;

-- 3. 骑行订单：起止位置（用于里程与轨迹展示）
ALTER TABLE ride_order
    ADD COLUMN start_longitude DECIMAL(10, 6) COMMENT '起点经度' AFTER start_station_id,
    ADD COLUMN start_latitude DECIMAL(10, 6) COMMENT '起点纬度' AFTER start_longitude,
    ADD COLUMN end_longitude DECIMAL(10, 6) COMMENT '终点经度' AFTER end_station_id,
    ADD COLUMN end_latitude DECIMAL(10, 6) COMMENT '终点纬度' AFTER end_longitude;
