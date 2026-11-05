# 旧物新生 · 二手家具交易平台

同城二手家具交易与回收平台。产品需求见 [`docs/PRD-MVP-v1.0.md`](docs/PRD-MVP-v1.0.md)。

## 技术栈

| 端 | 技术 |
| --- | --- |
| 后端 | Java 21 · Spring Boot 3.5 · Spring Security 6 + JWT · MyBatis-Plus 3.5 · H2（开发）/ MySQL 8（生产） |
| 前端 | Vite 6 · Vue 3.5 · TypeScript · Vant 4 · Pinia · Vue Router |
| 文档 | springdoc-openapi（Swagger UI） |

**架构决策**：单体应用，按业务域分包，不引入 Spring Cloud（理由见 PRD 与团队讨论记录）。模块间只通过 Service 接口或领域事件交互，禁止跨包直接访问 Mapper——这样将来要拆服务时，包即服务。

## 目录结构

```
Recycle/
├── docs/
│   └── PRD-MVP-v1.0.md          # 产品需求文档
├── backend/                      # Spring Boot 单体
│   ├── pom.xml
│   ├── mvnw / mvnw.cmd           # Maven 包装器，无需预装 Maven
│   └── src/main/
│       ├── java/com/recycle/
│       │   ├── common/           # 统一返回 R、分页、异常处理
│       │   ├── config/           # Security、CORS、MyBatis-Plus、OpenAPI
│       │   ├── user/             # 注册登录、JWT 签发与校验
│       │   ├── item/             # 商品发布 / 状态机 / 图片
│       │   ├── search/           # 类目树 / 搜索
│       │   ├── order/            # 订单（当前：费用试算）
│       │   ├── payment/          # 待建
│       │   └── logistics/        # 待建
│       └── resources/
│           ├── application.yml    # dev：H2 内存库，自动建表灌数据
│           ├── application-prod.yml
│           └── db/schema.sql      # 建表脚本（唯一事实来源）
└── web/                          # Vite + Vue3 H5
    └── src/
        ├── api/                  # axios 封装 + 类型定义
        ├── views/                # 首页 / 搜索 / 详情 / 发布 / 确认订单
        └── router/
```

## 快速开始

### 1. 后端

```bash
cd backend
./mvnw spring-boot:run        # Windows: .\mvnw.cmd spring-boot:run
```

- 服务地址：http://localhost:8080
- 接口文档：http://localhost:8080/swagger-ui.html
- H2 控制台：http://localhost:8080/h2-console
  - JDBC URL：`jdbc:h2:mem:recycle`，用户名 `sa`，密码留空

**演示账号**（dev profile 自动创建，密码统一 `123456`）：

| 手机号 | 昵称 |
| --- | --- |
| 13800000001 | 旧物新生 |
| 13800000002 | 搬家急出 |

默认 `dev` profile 使用 **H2 内存库**，启动时自动执行 `db/schema.sql` + `db/data.sql`，自带 8 件演示商品，改代码重启即重建，不依赖本地装 MySQL/Redis。

### 2. 前端

```bash
cd web
npm install
npm run dev
```

访问 http://localhost:5173 ，`/api` 已代理到 8080。

### 3. 切到 MySQL

```bash
cd backend
./mvnw spring-boot:run -Dspring-boot.run.profiles=prod
```

先建库 `CREATE DATABASE recycle DEFAULT CHARSET utf8mb4;`，再手工执行 `src/main/resources/db/schema.sql`。连接信息改 `application-prod.yml`。

## 已实现（本次脚手架）

- [x] 统一响应体 `R<T>`、全局异常处理、参数校验
- [x] 登录鉴权：注册 / 登录 / 刷新 / 登出，Spring Security 6 + 双 JWT（access 30 分钟 / refresh 7 天）
- [x] 商品发布（字段校验、联系方式拦截、图片落库、自动上架）
- [x] 商品列表 / 关键词搜索 / 多维筛选 / 排序 / 详情（浏览计数）
- [x] 类目树（静态配置）
- [x] 下单前费用试算：起步价 + 里程 + 体积档 + 楼层 + 拆装 + 5% 服务费（PRD 7.1 全部规则）
- [x] **订单闭环**：下单锁库存 → 支付（模拟网关）→ 卖家发货 → 确认收货 → 订单完成、商品标记已售出
- [x] 待支付 30 分钟超时自动关单并释放商品（定时任务，条件更新防重复）
- [x] 订单列表（我买到的 / 我卖出的）+ 订单详情（状态、费用明细、倒计时）
- [x] 前端 H5：浏览 → 搜索 → 详情 → 发布 → 下单 → 支付 → 收货 全链路

## 待开发（按 PRD 优先级）

| 优先级 | 事项 | 位置 |
| --- | --- | --- |
| P0 | 真实支付对接（微信 / 支付宝）+ 回调 + 担保交易 | `payment`（`PaymentGateway` 接口已就绪，加实现即可） |
| P0 | 物流预约：时段容量、运力派单、改约 | `logistics` |
| P1 | 商品审核后台（目前发布即上架） | `item` + 管理端 |
| P1 | 图片上传（目前前端填 URL） | 接入 OSS |
| P2 | 类目落表、碳减排字段计算、回收表单 | `search` / `item` |

## 鉴权约定

**双令牌：短 access + 长 refresh**

| 令牌 | 有效期 | 存放 | 用途 |
| --- | --- | --- | --- |
| accessToken | 30 分钟 | JWT，无状态，**不查库** | 每个请求放 `Authorization: Bearer` |
| refreshToken | 7 天 | 数据库 `refresh_token` 表（只存 SHA-256 摘要） | 只用于换新令牌，一次性 |

- 接口分两类：**浏览开放**（GET 商品/类目/搜索，匿名可看）、**写入需登录**（POST/PUT/DELETE、`/orders/**`）。
- 新增接口若未显式加放行规则，默认**需要登录**（fail-safe，不会误开放）。
- 未登录 / access 过期 → HTTP 401 + `{"code":40100,"message":"请先登录"}`；前端拦截器自动用 refresh 换一次新令牌并重放原请求，换不到才跳登录页。
- **刷新即轮换**：旧 refresh 用过立即作废，发新的。已作废的令牌再次出现 → 判定为疑似盗用，**撤销该用户全部刷新令牌**，所有设备强制重新登录。
- **登出**：`POST /api/v1/auth/logout` 撤销 refresh token；access token 无状态，到期自然失效。
- 密钥通过 `JWT_SECRET` 环境变量注入，默认值仅供本地开发，**上线必须替换**。

> 为什么不用 Redis 存 token：当前没有「秒级踢人」需求，access token 无状态最省一次 IO；
> 需要撤销时用数据库表即可，不引入新中间件。将来量级上来，把 `RefreshTokenService`
> 换成 Redis 实现即可，调用方不用改。

## 订单状态机

```
PENDING_PAYMENT 待支付 ──支付──▶ PAID 待发货 ──卖家发货──▶ SHIPPED 配送中 ──买家确认──▶ COMPLETED 已完成
        │
        ├── 买家取消 ──▶ CANCELLED 已取消（商品释放回在售）
        └── 30 分钟超时 ─▶ CLOSED 已关闭（商品释放回在售）
```

- 所有流转都用**带状态条件的 UPDATE**，靠受影响行数判断是否抢到，避免并发重复推进。
- 支付走 `PaymentGateway` 接口，当前实现是 `MockPaymentGateway`；真实商户号下来后新增实现类即可，`OrderService` 不用改。
- 待支付超时可配置：`app.order.pay-timeout-minutes`（默认 30）。联调时可设成 `0` 验证关单。

## 费用与结算

一笔订单的钱分给三方，**服务费只收一次，由卖家承担**：

```
买家实付 = 商品价 + 运费 + 楼层费 + 拆装费
卖家实收 = 商品价 − 平台服务费（商品价 × 5%）
平台收入 = 平台服务费
运力方   = 运费 + 楼层费 + 拆装费（由买家直接支付，不经过卖家）
```

举例（商品 850 元、6km、3 楼无电梯）：

| 项 | 金额 |
| --- | --- |
| 商品价 | 850 |
| 运费（起步 30 含 3km + 超出 3km×2 + 中件档 30） | 66 |
| 楼层费（3 楼无电梯，(3−1)×10） | 20 |
| **买家实付** | **936** |
| 平台服务费（850 × 5%） | 42.50 |
| **卖家实收** | **807.50** |

> 早期版本把服务费加在买家身上，会出现「买家多付 5%、卖家少收 5%」的重复收费，已修正：
> 服务费从卖家所得中扣，买家实付不含服务费。

## 约定

- **跨模块调用走 Service**：如 `OrderService` 调 `ItemService.getById()`，不直接注入对方 Mapper。
- **状态用枚举名落库**：如 `item.status = 'ON_SALE'`，排查 SQL 时可读。
- **费用规则集中在 `FeeCalculator`**，改费率只动一个类。
- 数据库字段：`item.weight_kg` / `carbon_saved_kg` / `recycle_flag` 为二期碳核算预留，先落库不计算。
