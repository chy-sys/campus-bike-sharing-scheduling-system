# 校园共享单车调度管理系统

基于 Spring Boot 单体后端 + Android 客户端的校园共享单车调度管理系统，涵盖用户注册登录、车辆与站点管理、骑行订单计费、故障报修闭环与车辆调度建议。

## 仓库结构

```
bike-scheduling-parent
├── campus-bike-backend    # 后端单体服务（Spring Boot，端口 8001）
│   └── 详见 campus-bike-backend/README.md
└── bike-android-app       # Android 客户端（OkHttp + SharedPreferences）
```

## 技术栈

**后端**：Spring Boot 2.7.18 · MyBatis-Plus 3.5.5 · MySQL 8 · Spring Security + JWT · Hutool · JDK 8

**客户端**：Android（Java） · OkHttp · SharedPreferences

## 六大业务模块

| # | 模块 | 说明 |
| --- | --- | --- |
| 1 | user 用户 | 注册登录、JWT 鉴权 |
| 2 | vehicle 车辆 | 车辆信息、状态管理 |
| 3 | station 站点 | 站点信息、容量与在站车辆 |
| 4 | order 订单 | 骑行订单、计费、结算 |
| 5 | repair 报修 | 故障工单、受理与处理闭环 |
| 6 | dispatch 调度 | 调度任务、溢缺预警、调度建议 |

## 快速开始

### 后端

```bash
cd campus-bike-backend
mvn clean install
mvn spring-boot:run
```

建库建表语句见 [campus-bike-backend/README.md](campus-bike-backend/README.md)。

启动后访问 `http://localhost:8001/health`，返回 `Bike Service is running!` 即为成功。

### Android 客户端

| 页面 | 功能 | 对应接口 |
| --- | --- | --- |
| `LoginActivity` | 登录（启动页） | `/auth/login` |
| `RegisterActivity` | 注册 | `/auth/register` |
| `MainActivity` | 主页，功能入口 | — |
| `RideActivity` | 开锁用车、还车结算、我的订单 | `/api/orders`、`/api/bikes`、`/api/stations` |
| `RepairActivity` | 提交报修、我的报修记录 | `/api/repairs?mine=true` |
| `StationActivity` | 站点列表 | `/api/stations/all` |
| `DispatchActivity` | 调度建议 | `/api/dispatch/suggest` |

把 `ApiClient.java`、`LoginActivity.java`、`RegisterActivity.java` 中的后端地址改成你电脑的**局域网 IP**（不能用 `localhost`，手机无法访问）：

```java
private static final String BASE_URL = "http://192.168.1.37:8001";
```

确保手机与电脑在同一局域网，且防火墙放行 8001 端口。

## 接口约定

- 除 `/auth/**`、`/health`、`/test/**` 外均需携带 `Authorization: Bearer {token}`
- 统一响应结构：`{ code, msg, data }`，`code = 200` 表示成功

完整接口列表见 [campus-bike-backend/README.md](campus-bike-backend/README.md)。
