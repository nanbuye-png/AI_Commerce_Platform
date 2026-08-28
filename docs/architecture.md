# 系统架构文档

## 整体架构

```
┌─────────────────────────────────────────────────────────┐
│                     客户端层 (Client)                     │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  │
│  │ Customer Web  │  │ Merchant Web │  │  Admin Web   │  │
│  │ (React/Vite)  │  │ (React/Vite) │  │ (React/Vite) │  │
│  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘  │
└─────────┼──────────────────┼──────────────────┼──────────┘
          │                  │                  │
          └──────────────────┼──────────────────┘
                             │ HTTP (RESTful API)
                     ┌───────▼────────┐
                     │   API Gateway   │
                     │  (Spring Cloud  │
                     │   Gateway /     │
                     │   Nginx)        │
                     └───────┬────────┘
                             │
              ┌──────────────┼──────────────┐
              │              │              │
     ┌────────▼─────┐ ┌─────▼──────┐ ┌─────▼──────────┐
     │  Commerce    │ │  Auth      │ │  AI Service    │
     │  Platform    │ │  Service   │ │  (FastAPI)     │
     │  (Spring     │ │ (Spring    │ │  Python        │
     │   Boot)      │ │  Security) │ │                │
     └────────┬─────┘ └────────────┘ └───────┬─────────┘
              │                              │
     ┌────────▼──────────────────────────────▼─────────┐
     │                  Message Queue                   │
     │            (RabbitMQ / Kafka)                    │
     └────────┬──────────────────────────────┬─────────┘
              │                              │
     ┌────────▼─────────┐          ┌─────────▼──────────┐
     │   MySQL 8.0      │          │   Redis            │
     │   (主数据库)      │          │   (缓存)           │
     └────────┬─────────┘          └─────────┬──────────┘
              │                              │
              ▼                              ▼
     ┌────────▼─────────┐          ┌─────────▼──────────┐
     │   Elasticsearch  │          │   MinIO            │
     │   (搜索)         │          │   (文件存储)       │
     └──────────────────┘          └────────────────────┘
```

## 分层说明

| 层级 | 技术栈 | 职责 |
|------|--------|------|
| **Client** | React + TypeScript + Vite + Ant Design | 三个独立前端应用（C 端 / 商户端 / 管理端） |
| **Gateway** | Spring Cloud Gateway / Nginx | 路由转发、认证鉴权、限流 |
| **Service** | Spring Boot / FastAPI | 业务服务 + AI 智能服务 |
| **Infrastructure** | MySQL 8.0 / Redis / Elasticsearch / MinIO / RabbitMQ | 数据存储、缓存、搜索、文件存储、消息队列 |

## 模块划分

- **backend/commerce-platform** — 核心电商业务（商品、订单、用户、支付）
- **backend/auth-service** — 统一认证授权（OAuth2 / JWT）
- **ai-service** — AI 智能服务（对话、推荐、内容生成）
- **frontend/customer-web** — C 端用户前端
- **frontend/merchant-web** — 商户管理后台
- **frontend/admin-web** — 平台运营管理后台

## 业务领域架构

### 用户域 (User Domain)
| 子域 | 说明 |
|------|------|
| 账号管理 | 注册、登录、密码重置 |
| 地址管理 | 收货地址 CRUD |
| 收藏 | 商品收藏/取消收藏 |
| 浏览历史 | 浏览记录追踪 |
| 用户行为 | VIEW/SEARCH/CLICK/PURCHASE 行为记录，为 AI 推荐提供数据 |

### 商品域 (Product Domain)
| 子域 | 说明 |
|------|------|
| 分类管理 | 多级商品分类 |
| SPU 管理 | 商品标准信息管理 |
| SKU 管理 | 规格、价格、库存关联 |
| 商品图片 | 多图管理 |

### 交易域 (Trade Domain)
| 子域 | 说明 |
|------|------|
| 购物车 | 用户购物车管理 |
| 订单 | 订单生命周期管理（含状态机） |
| 支付 | 微信扫码支付集成 |
| 退款 | 退款申请与处理 |

### 商家域 (Merchant Domain)
| 子域 | 说明 |
|------|------|
| 商家入驻 | 商家注册与审核 |
| 店铺管理 | 店铺信息维护 |
| 员工管理 | 商家内部员工账号与角色 |

### 库存域 (Inventory Domain)
| 子域 | 说明 |
|------|------|
| 库存管理 | 当前库存与锁定库存管理 |
| 库存流水 | 每次库存变动的可追溯日志 |

### 营销域 (Promotion Domain)
| 子域 | 说明 |
|------|------|
| 活动管理 | 满减/折扣活动配置 |
| 优惠券 | 优惠券发放与核销 |
| 秒杀 | 限时秒杀活动 |

### 搜索域 (Search Domain)
| 子域 | 说明 |
|------|------|
| 商品搜索 | Elasticsearch 全文检索 |
| 筛选排序 | 分类/品牌/价格筛选与排序 |

### AI 域 (AI Domain)
| 子域 | 说明 |
|------|------|
| 智能对话 | AI 购物助手（LLM + Agent） |
| 个性化推荐 | 基于用户行为的商品推荐 |
| AI 记忆 | 用户偏好与对话上下文记忆 |

### 领域间依赖关系

```
用户域 ──> 交易域（用户创建订单）
用户域 ──> AI 域（行为数据驱动推荐）
商品域 ──> 交易域（订单引用 SKU）
商品域 ──> 库存域（SKU 关联库存）
商品域 ──> 搜索域（商品索引到 ES）
商家域 ──> 商品域（商家管理商品）
交易域 ──> 支付域（订单发起支付）
AI 域 ──> 商品域（推荐结果返回商品）
```

## 数据库架构

### 存储组件职责

| 组件 | 技术 | 职责 |
|------|------|------|
| **主数据库** | MySQL 8.0 | 交易数据、用户数据、商品数据、订单数据等核心业务数据 |
| **缓存** | Redis | 会话缓存、购物车、热点商品数据、接口限流 |
| **搜索引擎** | Elasticsearch | 商品全文搜索、筛选、排序 |
| **对象存储** | MinIO | 商品图片、AI 生成图片、视频资源等静态文件 |

### 数据流向

```
┌──────────┐    ┌──────────┐    ┌──────────┐
│  Application  │───write───>│  MySQL   │
│     Service   │<───read────│  8.0    │
└──────────┘    └──────────┘
      │                │
      │ read           │ read
      ▼                ▼
┌──────────┐    ┌──────────┐
│  Redis   │    │    ES    │
│ (缓存)   │    │ (搜索)   │
└──────────┘    └──────────┘
      │
      │ write
      ▼
┌──────────┐
│  MinIO   │
│ (文件)   │
└──────────┘
```

### 数据库设计原则

| 项目 | 配置 |
|------|------|
| 数据库 | MySQL 8.0 |
| 字符集 | `utf8mb4` |
| 排序规则 | `utf8mb4_unicode_ci` |
| 存储引擎 | InnoDB |
| 主键策略 | BIGINT 自增（兼容雪花 ID） |
| 逻辑删除 | `deleted` TINYINT(1)，0=正常 1=删除 |
| 乐观锁 | `version` INT，用于高并发库存扣减 |
| 金额字段 | `DECIMAL(12,2)`，避免 float 精度损失 |
| 时间字段 | `created_time` / `updated_time`，所有业务表必备 |
| 状态字段 | `VARCHAR(20)` 存储枚举值 |

### 核心表一览

| 领域 | 表名 | 说明 |
|------|------|------|
| 用户域 | user_account | 用户主表 |
| 用户域 | user_address | 用户地址 |
| 用户域 | user_behavior | 用户行为 |
| 商家域 | merchant_account | 商家账号 |
| 商家域 | store | 店铺 |
| 商家域 | merchant_user | 商家员工 |
| 商品域 | category | 商品分类 |
| 商品域 | product | 商品 SPU |
| 商品域 | product_sku | 商品 SKU |
| 商品域 | product_image | 商品图片 |
| 库存域 | inventory | 库存（含乐观锁） |
| 库存域 | inventory_record | 库存流水 |
| 购物车域 | cart | 购物车 |
| 购物车域 | cart_item | 购物车条目 |
| 订单域 | order_info | 订单 |
| 订单域 | order_item | 订单条目 |
| 支付域 | payment | 支付记录 |
| 支付域 | refund | 退款记录 |
| AI 域 | conversation | 对话会话 |
| AI 域 | message | 对话消息 |
| AI 域 | ai_memory | AI 记忆 |
| AI 域 | recommendation_record | 推荐记录 |
| 媒体域 | media_file | 媒体文件 |
| 媒体域 | media_relation | 媒体关联 |
| Agent 域 | agent_task | Agent 任务 |
| Agent 域 | agent_task_step | 任务步骤 |

> **详细设计参见:** [database-design.md](./database-design.md)、[data-dictionary.md](./data-dictionary.md)、[er-model.md](./er-model.md)

## API Gateway 层

### 网关职责

| 职责 | 说明 |
|------|------|
| **路由转发** | 根据 URL 前缀将请求路由到对应后端服务 |
| **认证鉴权** | 统一验证 JWT Token，解析用户身份与角色 |
| **限流熔断** | 基于用户/IP 的请求频率限制，保护后端服务 |
| **日志采集** | 记录全量 API 请求日志，用于监控与审计 |
| **跨域处理** | 统一 CORS 配置，允许前端跨域访问 |

### 路由规则

| 路径前缀 | 目标服务 | 说明 |
|----------|----------|------|
| `/api/v1/auth/*` | Auth Service | 认证服务 |
| `/api/v1/users/*` | Commerce Platform | 用户模块 |
| `/api/v1/products/*` | Commerce Platform | 商品模块 |
| `/api/v1/cart/*` | Commerce Platform | 购物车模块 |
| `/api/v1/orders/*` | Commerce Platform | 订单模块 |
| `/api/v1/payment/*` | Commerce Platform | 支付模块 |
| `/api/v1/merchant/*` | Commerce Platform | 商家模块 |
| `/api/v1/admin/*` | Commerce Platform | 管理模块 |
| `/api/v1/ai/*` | AI Service (FastAPI) | AI 智能服务 |
| `/api/v1/files/*` | Commerce Platform → MinIO | 文件上传 |
| `/health` | 各服务 | 健康检查（直通） |

### 调用链路

```
Frontend (React + Vite)
    │
    │  HTTPS + Authorization: Bearer <JWT>
    ▼
API Gateway (Spring Cloud Gateway / Nginx)
    │
    ├── 认证鉴权（JWT 解析 → 用户 ID + 角色）
    ├── 限流检查（Redis 令牌桶）
    ├── 路由转发
    │
    ▼
┌──────────────────────┬──────────────────────┐
│  Commerce Platform   │    AI Service        │
│  (Spring Boot)       │    (FastAPI)         │
│                      │                      │
│  ├── 用户模块         │  ├── AI 对话         │
│  ├── 商品模块         │  ├── 智能推荐        │
│  ├── 购物车模块       │  └── Agent 任务      │
│  ├── 订单模块         │                      │
│  ├── 支付模块         │                      │
│  ├── 商家模块         │                      │
│  ├── 管理模块         │                      │
│  └── 文件模块         │                      │
└──────────┬───────────┴──────────┬───────────┘
           │                      │
           ▼                      ▼
    ┌──────────┐          ┌──────────┐
    │  MySQL   │          │  Redis   │
    │  8.0     │          │  (缓存)  │
    └──────────┘          └──────────┘
           │                      │
           ▼                      ▼
    ┌──────────┐          ┌──────────┐
    │    ES    │          │  MinIO   │
    │  (搜索)  │          │  (文件)  │
    └──────────┘          └──────────┘
```

> **API 接口详细设计参见:** [api-design.md](./api-design.md)

## Database Migration Strategy (Flyway)

生产环境所有数据库变更必须通过 **Flyway** 进行版本化管理。

**禁止:** 人工直接修改生产数据库。

### 迁移文件命名

```
src/main/resources/db/migration/
├── V1__init.sql              # 初始建表
├── V2__add_merchant_fields.sql
├── V3__add_payment_index.sql
└── ...
```

### 迁移规范

| 项目 | 规范 |
|------|------|
| 版本号 | `V{序号}__{描述}.sql`，双下划线分隔 |
| 不可逆 | 版本一旦执行不可修改，新变更使用新版本号 |
| 幂等 | 同一版本号不可重复执行 |
| 事务 | DDL 语句单独一个版本文件 |
| 兼容 | 新增字段设 DEFAULT 值，不删除旧字段 |
| Hibernate | `spring.jpa.hibernate.ddl-auto=validate`，仅做 Entity 校验，不做 Schema 变更 |
| Flyway 全域 | Flyway 是**唯一**的数据库 Schema 变更工具，团队开发和生产环境均使用 Flyway |

> **详细设计参见:** [database-design.md](./database-design.md#数据库迁移策略-flyway)

## Frontend Architecture

> **详细设计参见:** [frontend-architecture.md](./frontend-architecture.md)

### 三端应用关系

```
┌──────────────────────────────────────────────────────────────┐
│                    Frontend Applications                      │
│                            │                                 │
│    ┌───────────────────────┼───────────────────────┐        │
│    │                       │                       │        │
│    ▼                       ▼                       ▼        │
│ ┌──────────┐         ┌──────────┐         ┌──────────┐     │
│ │customer- │         │merchant- │         │ admin-   │     │
│ │   web    │         │   web    │         │   web    │     │
│ │          │         │          │         │          │     │
│ │ C端购物  │         │商家运营  │         │平台管理  │     │
│ │ 商城     │         │  后台    │         │  后台    │     │
│ │          │         │          │         │          │     │
│ │ 角色:    │         │ 角色:    │         │ 角色:    │     │
│ │ USER     │         │ MERCHANT │         │ ADMIN    │     │
│ └────┬─────┘         └────┬─────┘         └────┬─────┘     │
│      │                    │                    │            │
│      └────────────────────┼────────────────────┘            │
│                           │                                 │
│                    ┌──────▼──────┐                          │
│                    │   shared    │                          │
│                    │  (共享层)    │                          │
│                    │ components/ │                          │
│                    │ types/      │                          │
│                    │ utils/      │                          │
│                    │ constants/  │                          │
│                    │ api/        │                          │
│                    └──────┬──────┘                          │
└───────────────────────────┼─────────────────────────────────┘
                            │
                            │ HTTPS + JWT (Bearer Token)
                            ▼
                    ┌───────────────┐
                    │  API Gateway  │
                    └───────────────┘
```

### 调用流程

```
React Component
      │
      │ 1. 用户操作触发
      ▼
Zustand Store Action
      │
      │ 2. 设置 loading → 调用 Service API
      ▼
Axios Service Layer
      │
      │ 3. 请求拦截器附加 JWT → 发送 HTTPS 请求
      ▼
API Gateway
      │
      │ 4. JWT 验证 → 角色鉴权 → 路由转发
      ▼
Backend Service
      │
      │ 5. 业务处理 → 返回 JSON 响应
      ▼
Axios 响应拦截器
      │
      │ 6. 统一错误处理 (401/403/500)
      ▼
Store State 更新
      │
      │ 7. 清除 loading → 设置 data
      ▼
React Component Re-render
      │
      │ 8. 展示最新数据
      ▼
    用户看到结果
```

### 权限模型

| 角色 | 可访问应用 | 典型权限 |
|------|------------|----------|
| **USER** | customer-web | 浏览商品、搜索、购物车、下单、支付、AI 购物助手、个人中心 |
| **MERCHANT** | merchant-web | 商品管理、订单处理、库存管理、数据统计、店铺设置 |
| **ADMIN** | admin-web | 用户管理、商家审核、全平台商品管控、系统配置、AI 服务管理 |

**权限控制层级:**

```
┌─────────────────────────────────────────┐
│  第一层: 路由层 (Route Guard)             │
│  - 前端路由守卫，未登录重定向到 /login     │
│  - 角色不匹配显示 403 页面                │
│  - 白名单路由 (/, /products, /login)     │
│    跳过校验                              │
├─────────────────────────────────────────┤
│  第二层: 接口层 (Axios 响应拦截器)         │
│  - 401 → 清除 Token → 跳转登录           │
│  - 403 → 权限不足提示                    │
│  - 后端 API 自身也有权限校验              │
├─────────────────────────────────────────┤
│  第三层: API Gateway (后端网关)           │
│  - JWT 验证                              │
│  - 角色权限校验                           │
│  - 限流控制                              │
└─────────────────────────────────────────┘
```

### 技术栈

| 技术 | 版本 | 用途 |
|------|------|------|
| React | 19 | UI 框架 |
| TypeScript | 5.x | 类型系统 |
| Vite | 5.x | 构建工具 |
| Ant Design | 5.x | UI 组件库 |
| React Router | 6.x | SPA 路由 |
| Zustand | 4.x | 状态管理 |
| Axios | 1.x | HTTP 客户端 |

### 前端安全

| 安全措施 | 说明 |
|----------|------|
| JWT Bearer Token | 所有 API 请求通过 Authorization Header 携带 JWT |
| Route Guard | 前端路由级权限校验 |
| XSS 防护 | React JSX 自动转义 + DOMPurify 清洗富文本 |
| HTTPS 强制 | 生产环境强制 HTTPS |
| CSRF 防护 | 不依赖 Cookie 认证，天然免疫 CSRF |
| 敏感信息脱敏 | 手机号、Token 等不在前端明文全量展示 |


