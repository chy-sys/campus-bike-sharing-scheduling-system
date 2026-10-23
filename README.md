# 校园共享单车调度管理系统

基于 Spring Boot 单体后端 + Android 客户端的校园共享单车调度管理系统，实现用户注册登录、车辆与站点数据管理，后续扩展调度算法。

## 技术栈

**后端（bike-server）**

| 组件 | 版本 |
| --- | --- |
| Spring Boot | 2.7.18 |
| MyBatis-Plus | 3.5.5 |
| MySQL | 8.0.33 |
| Spring Security + JWT | jjwt 0.9.1 |
| Hutool | 5.8.25 |
| JDK | 8 |

**客户端（bike-android-app）**：Android + OkHttp + SharedPreferences

## 项目结构

```
bike-scheduling-parent/
├── pom.xml                 # 父工程，统一依赖版本
├── bike-server/            # 后端单体应用（端口 8001）
│   └── src/main/java/com/gxu/bike/
│       ├── BikeApplication.java       # 启动类
│       ├── common/                    # R 统一返回体 / ResultCode 状态码
│       ├── config/                    # SecurityConfig / MybatisPlusConfig
│       ├── controller/                # Auth / Bike / Station / Dispatch / Health / TestDb
│       ├── dto/                       # 登录注册 / 调度建议
│       ├── entity/                    # User / Bike / Station / DispatchTask
│       ├── exception/                 # BizException 业务异常
│       ├── handler/                   # 全局异常处理 / Security 401·403 JSON 化
│       ├── mapper/                    # User / Bike / Station / DispatchTask
│       ├── security/                  # JwtAuthenticationFilter
│       ├── service/                   # User / Bike / Station / Dispatch + 实现
│       └── utils/JwtUtil.java
└── bike-android-app/       # Android 客户端
```

## 快速开始

### 1. 初始化数据库

创建数据库 `bike_db`，执行以下建表语句（如已建表可跳过）：

```sql
CREATE TABLE sys_user (
  id          BIGINT PRIMARY KEY AUTO_INCREMENT,
  username    VARCHAR(50),
  phone       VARCHAR(20) UNIQUE,
  password    VARCHAR(100),
  status      INT DEFAULT 1,
  role        INT DEFAULT 0,
  create_time DATETIME,
  update_time DATETIME
);

CREATE TABLE station (
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

CREATE TABLE bike (
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

CREATE TABLE dispatch_task (
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

### 2. 修改数据库连接

编辑 `bike-server/src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/bike_db?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
    username: ${DB_USERNAME:root}
    password: ${DB_PASSWORD:1234}
```

账号密码支持环境变量注入（冒号后为默认值），不想配环境变量就直接用默认值，也可以启动时覆盖：

```bash
DB_USERNAME=root DB_PASSWORD=你的密码 mvn -pl bike-server spring-boot:run
```

### 3. 启动后端

```bash
mvn clean install
mvn -pl bike-server spring-boot:run
```

或直接在 IDEA 中运行 `com.gxu.bike.BikeApplication`。

启动后访问 `http://localhost:8001/health`，返回 `Bike Service is running!` 即为成功。

### 4. 运行 Android 客户端

把 `LoginActivity.java` 和 `RegisterActivity.java` 中的 `BASE_URL` 改成你电脑的**局域网 IP**（不能用 `localhost`，手机无法访问）：

```java
private static final String BASE_URL = "http://192.168.1.37:8001";
```

同时确保手机与电脑在同一局域网，且防火墙放行 8001 端口。

## 接口列表

除 `/auth/**`、`/health`、`/test/**` 外，其余接口需在请求头携带 `Authorization: Bearer {token}`。

### 认证

| 方法 | 路径 | 说明 | 鉴权 |
| --- | --- | --- | --- |
| POST | `/auth/register` | 用户注册（开发阶段验证码固定 `123456`） | 否 |
| POST | `/auth/login` | 用户登录，返回 JWT | 否 |

### 车辆管理

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/api/bikes` | 分页查询，支持 `pageNum` `pageSize` `stationId` `status` `keyword` |
| GET | `/api/bikes/{id}` | 车辆详情 |
| POST | `/api/bikes` | 新增车辆 |
| PUT | `/api/bikes` | 修改车辆 |
| DELETE | `/api/bikes/{id}` | 删除车辆 |
| PUT | `/api/bikes/{id}/status?status=1` | 修改车辆状态（0故障 1可用 2使用中 3维修中） |

### 站点管理

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/api/stations` | 分页查询，支持 `keyword` |
| GET | `/api/stations/all` | 全部站点（下拉选择用） |
| GET | `/api/stations/{id}` | 站点详情 |
| POST | `/api/stations` | 新增站点 |
| PUT | `/api/stations` | 修改站点 |
| DELETE | `/api/stations/{id}` | 删除站点 |

### 调度管理

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/api/dispatch/suggest` | **调度建议**：按站点负荷率自动生成调出/调入方案 |
| GET | `/api/dispatch/tasks` | 分页查询调度任务，支持 `status` 过滤 |
| GET | `/api/dispatch/tasks/{id}` | 任务详情 |
| POST | `/api/dispatch/tasks` | 创建调度任务 |
| PUT | `/api/dispatch/tasks/{id}/start` | 开始调度 |
| PUT | `/api/dispatch/tasks/{id}/finish` | 完成调度（同步更新两端站点车辆数） |
| PUT | `/api/dispatch/tasks/{id}/cancel` | 取消调度 |

### 调试接口（无需鉴权）

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/health` | 健康检查 |
| GET | `/test/users` | 查询全部用户 |
| GET | `/test/stations` | 查询全部站点 |

调度建议返回示例：

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": [
    {
      "fromStationId": 1,
      "fromStationName": "图书馆站点",
      "fromRate": 0.95,
      "toStationId": 3,
      "toStationName": "体育馆站点",
      "toRate": 0.08,
      "bikeCount": 12
    }
  ]
}
```

登录成功返回示例：

```json
{
  "code": 200,
  "msg": "登录成功",
  "data": {
    "token": "eyJhbGciOi...",
    "userId": 1,
    "userName": "张三",
    "phone": "13800138000"
  }
}
```

## 设计要点

- **统一响应**：所有接口返回 `{ code, msg, data }`，由 `R` 封装；异常由 `GlobalExceptionHandler` 统一转成同结构
- **认证链路**：登录签发 24 小时有效的 JWT；`JwtAuthenticationFilter` 解析 `Authorization: Bearer {token}` 并写入 SecurityContext，Session 被禁用（无状态）
- **密码安全**：BCrypt 加密存储，数据库中不保存明文
- **调度算法**：按站点负荷率（在站车辆 / 容量）判断盈亏——高于 80% 为盈余站点、低于 20% 为缺车站点，以 50% 为目标水位计算可调出/需调入量，再按量降序贪心配对生成调度方案
- **调度闭环**：任务完成后自动扣减调出站点车辆、增加调入站点车辆（事务保证），且不会超过站点容量
- 本项目已由微服务架构（user / core / dispatch + gateway）改造为单体架构，历史演进不再保留在版本库中
