# 校园共享单车调度管理系统

基于 Spring Boot 单体后端 + Android 用户端 + Web 管理后台的校园共享单车调度管理系统，涵盖用户注册登录、地图定位与扫码开锁、骑行订单计费、故障报修闭环、车辆调度建议与运营数据统计。

## 仓库结构

```
bike-scheduling-parent
├── campus-bike-backend    # 后端单体服务（Spring Boot，端口 8001）+ Web 管理后台页面
│   └── 详见 campus-bike-backend/README.md
├── bike-android-app       # Android 用户端（OkHttp + SharedPreferences）
└── campus-bike-miniapp   # 微信小程序运维端（原生小程序，微信开发者工具导入）
```

## 技术栈

**后端**：Spring Boot 2.7.18 · MyBatis-Plus 3.5.5 · MySQL 8 · Spring Security + JWT · Hutool · JDK 8

**客户端**：Android（Java） · OkHttp · SharedPreferences · ZXing（扫码） · 微信小程序（原生，无框架依赖）

**管理后台**：原生 HTML + JavaScript（内嵌于后端 `static/admin`，无需额外部署）

## 三端与角色

| 端 | 使用角色 | 说明 |
| --- | --- | --- |
| Android 用户端 | 普通用户（role=0） | 注册登录、地图找车、扫码开锁、骑行计费、故障报修、个人中心 |
| Web 管理后台 | 管理员（role=2） | 数据看板、账号审核、车辆站点管理、订单、调度任务、工单处理 |
| 微信小程序运维端 | 运维人员（role=1） | 微信授权/账号密码登录、调度任务、站点监控、车辆运维、工单处理、数据统计 |

## 六大业务模块

| # | 模块 | 说明 |
| --- | --- | --- |
| 1 | user 用户 | 注册登录、JWT 鉴权、角色权限、个人资料与密码 |
| 2 | vehicle 车辆 | 车辆信息、状态管理、位置 |
| 3 | station 站点 | 站点信息、容量与在站车辆 |
| 4 | order 订单 | 骑行订单、计费、虚拟账户结算 |
| 5 | repair 报修 | 故障工单（含拍照）、受理与处理闭环 |
| 6 | dispatch 调度 | 调度任务、溢缺预警、调度建议 |

## 快速开始

### 后端

```bash
cd campus-bike-backend
mvn clean install
mvn spring-boot:run
```

建库建表语句见 [campus-bike-backend/README.md](campus-bike-backend/README.md)，已建库需再执行 `src/main/resources/db/alter_tables.sql`。

启动后访问 `http://localhost:8001/health`，返回 `Bike Service is running!` 即为成功。

### Web 管理后台

浏览器打开 `http://localhost:8001/admin/index.html`，用管理员账号登录（`sys_user.role = 2`）。

### Android 客户端

| 页面 | 功能 | 对应接口 |
| --- | --- | --- |
| `LoginActivity` | 登录（启动页） | `/auth/login` |
| `RegisterActivity` | 注册 | `/auth/register` |
| `MainActivity` | 主页，功能入口 | — |
| `RideActivity` | **扫码开锁**、手动选车开锁、还车结算、我的订单 | `/api/orders`、`/api/bikes`、`/api/stations` |
| `RepairActivity` | 提交报修（可**拍照上传**）、我的报修记录 | `/api/repairs?mine=true`、`/api/files/upload` |
| `MapActivity` | 地图展示站点分布、**附近站点按距离排序** | `/api/stations/all` |
| `ProfileActivity` | 个人资料、账户余额、骑行记录与账单、改资料/改密码 | `/api/users`、`/api/orders` |
| `StationActivity` | 站点列表 | `/api/stations/all` |
| `VehicleActivity` | 车辆查询、状态筛选、状态变更 | `/api/bikes` |
| `DispatchActivity` | 调度建议（只读展示） | `/api/dispatch/suggest` |
| `DispatchTaskActivity` | 调度任务：采用建议创建、开始/完成/取消 | `/api/dispatch/tasks` |

把 `ApiClient.java`、`LoginActivity.java`、`RegisterActivity.java` 中的后端地址改成你电脑的**局域网 IP**（不能用 `localhost`，手机无法访问）：

```java
private static final String BASE_URL = "http://172.27.104.169:8001";
```

确保手机与电脑在同一局域网，且防火墙放行 8001 端口。

### 微信小程序运维端

用**微信开发者工具**导入 `campus-bike-miniapp` 目录即可（AppID 可选「测试号」）。

| 页面 | 功能 | 对应接口 |
| --- | --- | --- |
| `pages/login` | 微信授权登录、账号密码登录（首次授权需绑定手机号） | `/auth/wechat/login`、`/auth/wechat/bind`、`/auth/login` |
| `pages/index` | 工作台：运营概览（周转率、故障率、办结率等） | `/api/stats/overview` |
| `pages/dispatch` | 调度建议一键采用、任务创建 / 开始 / 完成 / 取消 | `/api/dispatch/suggest`、`/api/dispatch/tasks` |
| `pages/station` | 站点实时监控、在站车辆与负荷率 | `/api/stations/all` |
| `pages/bike` | 车辆列表、点击变更状态（可用 / 故障 / 送修） | `/api/bikes`、`/api/bikes/{id}/status` |
| `pages/repair` | 工单受理、办结（已修复 / 无法修复）、查看现场照片 | `/api/repairs` |

配置步骤：

1. `app.js` 中 `globalData.baseUrl` 改为后端地址（真机填电脑局域网 IP，与手机同一 WiFi）
2. 开发者工具「详情 → 本地设置」勾选**不校验合法域名**，否则局域网请求会被拦截
3. 微信登录需在 `application.yml` 配置小程序凭证（也可用环境变量 `WX_APPID` / `WX_SECRET`）：

   ```yaml
   wx:
     appid: ${WX_APPID:}
     secret: ${WX_SECRET:}
   ```

   未配置时点「微信一键登录」会提示「未配置微信凭证」，此时用账号密码登录即可，其余功能不受影响
4. 登录账号需为运维人员（`sys_user.role = 1`），普通用户访问统计等接口会被拦截

### 高德地图 Key（可选）

地图页需要高德的 **Web 端（JS API）Key**，申请后填入 `app/src/main/res/values/strings.xml`：

```xml
<string name="amap_js_key">你的Web端Key</string>
```

未配置 Key 时地图区域会提示配置，附近站点列表仍可正常使用。

## 接口约定

- 除 `/auth/**`、`/health`、`/test/**`、`/uploads/**`、`/admin/**` 外均需携带 `Authorization: Bearer {token}`
- 小程序登录：`POST /auth/wechat/login` 传 `code`，已绑定返回 token，未绑定返回 `{needBind: true, openid}`；再调 `POST /auth/wechat/bind` 绑定手机号
- 统一响应结构：`{ code, msg, data }`，`code = 200` 表示成功
- 角色：`0` 普通用户、`1` 运维人员、`2` 管理员，登录接口返回 `role`

完整接口列表见 [campus-bike-backend/README.md](campus-bike-backend/README.md)。
