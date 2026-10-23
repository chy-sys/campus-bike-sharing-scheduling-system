# campus-bike-backend

校园共享单车调度管理系统 —— 后端单体服务。Spring Boot 单体架构，一个进程、一个端口对外提供全部能力。

## 技术栈

| 组件 | 版本 |
| --- | --- |
| Spring Boot | 2.7.18 |
| MyBatis-Plus | 3.5.5 |
| MySQL | 8.0.33 |
| Spring Security + JWT | jjwt 0.9.1 |
| Hutool | 5.8.25 |
| JDK | 8 |

## 目录结构

```
campus-bike-backend
├── src
│   └── main
│       ├── java
│       │   └── com.campus.bike
│       │       ├── CampusBikeApplication.java    // 唯一启动类
│       │       ├── common        // 公共层：统一返回结果、全局异常处理、工具类、常量
│       │       ├── config        // 配置层：MyBatis、Security、跨域、高德地图
│       │       ├── system        // 系统基础层：认证鉴权、健康检查
│       │       └── modules       // 核心业务层：六大模块
│       │           ├── user          // 1.用户模块：注册登录
│       │           ├── vehicle       // 2.车辆模块：车辆信息、状态
│       │           ├── station       // 3.站点模块：站点信息、容量
│       │           ├── order         // 4.订单模块：骑行订单、计费、结算
│       │           ├── repair        // 5.报修模块：故障工单、处理闭环
│       │           └── dispatch      // 6.调度模块：调度任务、溢缺预警、调度建议
│       └── resources
│           ├── application.yml
│           ├── mapper             // MyBatis XML 映射文件
│           └── static
├── pom.xml
├── .gitignore
└── README.md
```

## 快速开始

### 1. 建库建表

创建数据库 `bike_db`，执行：

CREATE TABLE IF NOT EXISTS sys_user (
  id          BIGINT PRIMARY KEY AUTO_INCREMENT,
  username    VARCHAR(50),
  phone       VARCHAR(20) UNIQUE,
  password    VARCHAR(100),
  status      INT DEFAULT 1,
  role        INT DEFAULT 0,
  create_time DATETIME,
  update_time DATETIME
);
CREATE TABLE IF NOT EXISTS station (
  id            BIGINT PRIMARY KEY AUTO_INCREMENT,
  name          VARCHAR(100),
  area          VARCHAR(100),
  longitude     DECIMAL(10, 6),
  latitude      DECIMAL(10, 6),
  capacity      INT,
  current_count INT DEFAULT 0,
  status        INT DEFAULT 1,
  create_time   DATETIME,
  update_time   DATETIME
);
CREATE TABLE IF NOT EXISTS bike (
  id          BIGINT PRIMARY KEY AUTO_INCREMENT,
  bikenumber  VARCHAR(50),
  longitude   DECIMAL(10, 6),
  latitude    DECIMAL(10, 6),
  battery     INT,
  status      INT DEFAULT 1,
  station_id  BIGINT,
  create_time DATETIME,
  update_time DATETIME
);
CREATE TABLE IF NOT EXISTS ride_order (
  id               BIGINT PRIMARY KEY AUTO_INCREMENT,
  order_no         VARCHAR(32),
  user_id          BIGINT,
  bike_id          BIGINT,
  start_station_id BIGINT,
  end_station_id   BIGINT,
  start_time       DATETIME,
  end_time         DATETIME,
  duration         INT COMMENT '骑行时长（分钟）',
  amount           DECIMAL(10, 2) COMMENT '订单金额（元）',
  status           INT DEFAULT 0 COMMENT '0骑行中 1已完成 2已取消',
  create_time      DATETIME,
  update_time      DATETIME
);
CREATE TABLE IF NOT EXISTS repair_order (
  id            BIGINT PRIMARY KEY AUTO_INCREMENT,
  bike_id       BIGINT,
  reporter_id   BIGINT,
  fault_type    INT COMMENT '1刹车失灵 2轮胎损坏 3链条故障 4电量不足 5其他',
  description   VARCHAR(500),
  status        INT DEFAULT 0 COMMENT '0待处理 1处理中 2已修复 3无法修复',
  handler_id    BIGINT,
  handle_result VARCHAR(500),
  create_time   DATETIME,
  update_time   DATETIME,
  finish_time   DATETIME
);
CREATE TABLE IF NOT EXISTS dispatch_task (
  id              BIGINT PRIMARY KEY AUTO_INCREMENT,
  from_station_id BIGINT NOT NULL,
  to_station_id   BIGINT NOT NULL,
  bike_count      INT NOT NULL,
  status          INT DEFAULT 0 COMMENT '0待调度 1进行中 2已完成 3已取消',
  operator_id     BIGINT,
  remark          VARCHAR(200),
  create_time     DATETIME,
  update_time     DATETIME,
  finish_time     DATETIME
);
```

### 2. 修改配置

`src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/bike_db?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
    username: ${DB_USERNAME:root}
    password: ${DB_PASSWORD:1234}
```

支持环境变量注入（冒号后为默认值）：

```bash
DB_USERNAME=root DB_PASSWORD=你的密码 mvn spring-boot:run
```

### 3. 启动

```bash
mvn clean install
mvn spring-boot:run
```

或在 IDEA 中运行 `com.campus.bike.CampusBikeApplication`。

访问 `http://localhost:8001/health` 返回 `Bike Service is running!` 即启动成功。

## 接口总览

除 `/auth/**`、`/health`、`/test/**` 外，其余接口需携带 `Authorization: Bearer {token}`。

### 1. 用户模块 `/auth`

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| POST | `/auth/register` | 注册（开发阶段验证码固定 `123456`） |
| POST | `/auth/login` | 登录，返回 JWT |

### 2. 车辆模块 `/api/bikes`

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/api/bikes` | 分页查询，支持 `pageNum` `pageSize` `stationId` `status` `keyword` |
| GET | `/api/bikes/{id}` | 详情 |
| POST | `/api/bikes` | 新增 |
| PUT | `/api/bikes` | 修改 |
| DELETE | `/api/bikes/{id}` | 删除 |
| PUT | `/api/bikes/{id}/status?status=1` | 改状态（0故障 1可用 2使用中 3维修中） |

### 3. 站点模块 `/api/stations`

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/api/stations` | 分页查询 |
| GET | `/api/stations/all` | 全部站点 |
| GET | `/api/stations/{id}` | 详情 |
| POST / PUT / DELETE | `/api/stations` | 增改删 |

### 4. 订单模块 `/api/orders`

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| POST | `/api/orders/start` | 开锁用车：校验车辆可用、用户无未结束订单 |
| PUT | `/api/orders/{id}/end` | 还车结算：计算时长与费用 |
| GET | `/api/orders` | 我的订单分页 |
| GET | `/api/orders/{id}` | 订单详情 |
| PUT | `/api/orders/{id}/cancel` | 取消订单，车辆归还起始站点 |

计费规则可在 `application.yml` 配置：

```yaml
bike:
  billing:
    base-price: 1.50     # 起步价
    base-minutes: 15     # 起步价包含分钟
    unit-price: 1.00     # 超出后每单位加价
    unit-minutes: 15     # 每单位分钟
```

### 5. 报修模块 `/api/repairs`

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| POST | `/api/repairs` | 提交报修，车辆自动置为故障 |
| GET | `/api/repairs` | 分页查询，支持 `status` |
| GET | `/api/repairs/{id}` | 详情 |
| PUT | `/api/repairs/{id}/assign` | 受理工单 |
| PUT | `/api/repairs/{id}/finish` | 处理完成（`fixed=true` 车辆恢复可用） |

### 6. 调度模块 `/api/dispatch`

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/api/dispatch/suggest` | 调度建议（溢缺预警） |
| GET | `/api/dispatch/tasks` | 调度任务分页 |
| GET | `/api/dispatch/tasks/{id}` | 任务详情 |
| POST | `/api/dispatch/tasks` | 创建任务 |
| PUT | `/api/dispatch/tasks/{id}/start` | 开始调度 |
| PUT | `/api/dispatch/tasks/{id}/finish` | 完成调度（同步更新两端站点车辆数） |
| PUT | `/api/dispatch/tasks/{id}/cancel` | 取消 |

### 调试接口（无需鉴权）

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/health` | 健康检查 |
| GET | `/test/users` | 全部用户 |
| GET | `/test/stations` | 全部站点 |

## 设计要点

- **统一响应**：所有接口返回 `{ code, msg, data }`，异常由 `GlobalExceptionHandler` 统一转成同结构
- **认证链路**：登录签发 24 小时 JWT；`JwtAuthenticationFilter` 解析 `Authorization: Bearer {token}` 写入 SecurityContext，Session 禁用（无状态）
- **密码安全**：BCrypt 加密存储
- **调度算法**：按站点负荷率（在站车辆 / 容量）判断盈亏——高于 80% 为盈余站点、低于 20% 为缺车站点，以 50% 为目标水位计算可调出/需调入量，再按量降序贪心配对生成方案
- **调度闭环**：任务完成后事务内扣减调出站点车辆、增加调入站点车辆，不超过站点容量
- **订单闭环**：开锁扣减起始站点车辆并把车置为使用中；还车结算费用、更新车辆归属站点并加回车辆数
- **报修闭环**：报修即置车辆故障；处理完成且 `fixed=true` 时车辆恢复可用
