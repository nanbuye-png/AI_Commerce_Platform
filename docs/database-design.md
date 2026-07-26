# 数据库设计文档

## 概述

本平台采用 **MySQL 8.0** 作为主数据库，Redis 用于缓存，Elasticsearch 用于商品搜索，MinIO 用于文件存储。

## 数据库整体设计原则

### 基础配置

| 项目 | 配置 |
|------|------|
| 数据库 | MySQL 8.0 |
| 字符集 | `utf8mb4` |
| 排序规则 | `utf8mb4_unicode_ci` |
| 存储引擎 | InnoDB |

### 命名规范

| 项目 | 规范 | 示例 |
|------|------|------|
| 表名 | snake_case，单数形式 | `user_account`, `product_sku`, `order_info` |
| 列名 | snake_case | `created_time`, `user_id`, `is_default` |
| 主键 | BIGINT，统一列名 `id` | `id BIGINT AUTO_INCREMENT PRIMARY KEY` |
| 时间字段 | `created_time` / `updated_time` | 所有业务表必备 |
| 逻辑删除 | `deleted`（TINYINT，0=正常 1=删除） | 所有业务表必备（购物车/库存流水等物理删除表除外） |
| 状态字段 | VARCHAR(20) 存储枚举值 | `status VARCHAR(20)` |

### 主键策略

- 使用 **BIGINT 自增** 作为数据库主键
- 预留 **雪花ID** 兼容性（主键 BIGINT 范围兼容 64 位分布式 ID）
- 不使用 UUID 作为主键（避免性能问题）

### 设计约束

1. **当前阶段仅做设计，不创建 SQL 和数据库表**
2. 所有业务表包含 `created_time`, `updated_time`, `deleted` 三个通用字段
3. 逻辑删除优先于物理删除（`deleted` 标志位）
4. 外键字段建立索引以优化 JOIN 查询
5. 高频查询字段（订单号、用户名、邮箱）建立唯一索引
6. 乐观锁用于高并发场景（库存扣减使用 `version` 字段）
7. 金额字段统一使用 `DECIMAL(12,2)`，避免 float 精度损失
8. JSON 字段使用 MySQL `JSON` 类型

### 类型选型

| 场景 | 推荐类型 | 说明 |
|------|----------|------|
| 主键 | `BIGINT` | 自增 64 位整数，兼容雪花 ID |
| 金额 | `DECIMAL(12,2)` | 精确 decimal，避免精度损失 |
| 状态 | `VARCHAR(20)` | 可读性强，调试友好 |
| 时间 | `DATETIME` | 精度到秒 |
| JSON | `JSON` | MySQL 8.0 JSON 原生支持 |
| 文本 | `TEXT` | 不限长度文本 |
| 逻辑删除 | `TINYINT(1)` | 0=正常，1=删除 |
| 整数计数 | `INT` | 库存、数量等 |

## 数据库架构设计

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

## 索引策略

- 所有表主键自增 BIGINT
- 外键字段建立索引
- 高频查询字段（order_no、username、email）建立唯一索引
- created_time 建立普通索引用于排序
- status 字段建立普通索引用于筛选
- 复合索引针对高频组合查询场景

## Redis 缓存设计

| Key 模式 | 用途 | TTL |
|----------|------|-----|
| `session:{token}` | 用户会话 | 7 天 |
| `captcha:{phone/email}` | 验证码 | 5 分钟 |
| `product:{id}` | 商品详情 | 1 小时 |
| `cart:{user_id}` | 购物车数据 | 3 天 |
| `rate_limit:{ip}:{api}` | 接口限流 | 1 秒 |

## 核心表规划

### 用户域

#### `user_account` — 用户主表

| 列名 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键，自增 |
| username | VARCHAR(64) | 用户名，唯一 |
| email | VARCHAR(128) | 邮箱，唯一 |
| phone | VARCHAR(20) | 手机号，唯一 |
| password_hash | VARCHAR(256) | BCrypt 加密密码 |
| nickname | VARCHAR(64) | 昵称 |
| avatar | VARCHAR(256) | 头像 URL |
| status | VARCHAR(20) | ACTIVE / DISABLED |
| created_time | DATETIME | 注册时间 |
| updated_time | DATETIME | 最后更新时间 |
| deleted | TINYINT(1) | 逻辑删除标志 |

**索引:**
- `uk_username` UNIQUE (username)
- `uk_email` UNIQUE (email)
- `uk_phone` UNIQUE (phone)
- `idx_status` (status)
- `idx_created_time` (created_time)

#### `user_address` — 用户地址

| 列名 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| user_id | BIGINT | 外键 → user_account |
| receiver_name | VARCHAR(64) | 收件人姓名 |
| receiver_phone | VARCHAR(20) | 收件人手机号 |
| province | VARCHAR(32) | 省 |
| city | VARCHAR(32) | 市 |
| district | VARCHAR(32) | 区 |
| detail_address | VARCHAR(256) | 详细地址 |
| is_default | TINYINT(1) | 是否默认地址 |
| created_time | DATETIME | — |
| updated_time | DATETIME | — |
| deleted | TINYINT(1) | — |

**索引:**
- `idx_user_id` (user_id)

#### `user_behavior` — 用户行为（AI 推荐用）

| 列名 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| user_id | BIGINT | 外键 → user_account |
| behavior_type | VARCHAR(20) | VIEW / SEARCH / CLICK / PURCHASE |
| target_type | VARCHAR(20) | 目标类型（product 等） |
| target_id | BIGINT | 目标 ID（商品ID 等） |
| extra_data | JSON | 扩展数据（搜索关键词等） |
| created_time | DATETIME | — |

**索引:**
- `idx_user_id_time` (user_id, created_time)
- `idx_behavior_type` (behavior_type)

---

### 商家域

#### `merchant_account` — 商家账号

| 列名 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| merchant_name | VARCHAR(128) | 商家名称 |
| contact_name | VARCHAR(64) | 联系人姓名 |
| contact_phone | VARCHAR(20) | 联系人电话 |
| email | VARCHAR(128) | 邮箱 |
| status | VARCHAR(20) | PENDING / ACTIVE / DISABLED |
| created_time | DATETIME | 入驻时间 |
| updated_time | DATETIME | — |
| deleted | TINYINT(1) | — |

**索引:**
- `uk_merchant_name` UNIQUE (merchant_name)
- `idx_status` (status)

#### `store` — 店铺

| 列名 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| merchant_id | BIGINT | 外键 → merchant_account |
| store_name | VARCHAR(128) | 店铺名称 |
| logo | VARCHAR(256) | Logo URL |
| description | TEXT | 店铺描述 |
| status | VARCHAR(20) | ACTIVE / CLOSED |
| created_time | DATETIME | — |
| updated_time | DATETIME | — |
| deleted | TINYINT(1) | — |

**索引:**
- `idx_merchant_id` (merchant_id)

#### `merchant_user` — 商家员工

| 列名 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| merchant_id | BIGINT | 外键 → merchant_account |
| username | VARCHAR(64) | 登录名 |
| password_hash | VARCHAR(256) | 加密密码 |
| role | VARCHAR(20) | ADMIN / OPERATOR / CUSTOMER_SERVICE |
| status | VARCHAR(20) | ACTIVE / DISABLED |
| created_time | DATETIME | — |
| updated_time | DATETIME | — |
| deleted | TINYINT(1) | — |

**索引:**
- `idx_merchant_id` (merchant_id)
- `uk_merchant_username` UNIQUE (merchant_id, username)

---

### 商品域

#### `category` — 商品分类

| 列名 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| parent_id | BIGINT | 自关联父分类ID，0=根节点 |
| category_name | VARCHAR(64) | 分类名称 |
| sort | INT | 排序权重 |
| created_time | DATETIME | — |
| updated_time | DATETIME | — |
| deleted | TINYINT(1) | — |

**索引:**
- `idx_parent_id` (parent_id)

#### `product` — 商品 SPU

| 列名 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| merchant_id | BIGINT | 外键 → merchant_account |
| store_id | BIGINT | 外键 → store |
| category_id | BIGINT | 外键 → category |
| product_name | VARCHAR(256) | 商品名称 |
| description | TEXT | 商品描述 |
| brand | VARCHAR(64) | 品牌 |
| status | VARCHAR(20) | DRAFT / ON_SHELF / OFF_SHELF |
| created_time | DATETIME | — |
| updated_time | DATETIME | — |
| deleted | TINYINT(1) | — |

**索引:**
- `idx_category_id` (category_id)
- `idx_store_id` (store_id)
- `idx_merchant_id_status` (merchant_id, status)
- `idx_product_name` FULLTEXT (product_name) — 基础搜索
- `idx_created_time` (created_time)

#### `product_sku` — 商品 SKU（库存唯一来源: Inventory）

| 列名 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| product_id | BIGINT | 外键 → product |
| sku_code | VARCHAR(64) | SKU 编码，唯一 |
| attributes_json | JSON | 规格属性，如 `{"color":"黑色","size":"XL"}` |
| price | DECIMAL(12,2) | 售价（单位: 元） |
| original_price | DECIMAL(12,2) | 原价/划线价 |
| status | VARCHAR(20) | ACTIVE / DISABLED |
| created_time | DATETIME | — |
| updated_time | DATETIME | — |
| deleted | TINYINT(1) | — |

> **库存分离设计:** SKU 表不存储库存数量。库存统一由 `inventory` 表管理（`available_stock` / `locked_stock` / `reserved_stock`）。

**索引:**
- `idx_product_id` (product_id)
- `uk_sku_code` UNIQUE (sku_code)

#### `product_image` — 商品图片

| 列名 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| product_id | BIGINT | 外键 → product |
| url | VARCHAR(512) | 图片 URL |
| sort | INT | 排序权重 |
| is_cover | TINYINT(1) | 是否首图 |
| created_time | DATETIME | — |

**索引:**
- `idx_product_id` (product_id)

---

### 库存域

#### `inventory` — 库存（与 SKU 一一对应，库存唯一来源）

| 列名 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| sku_id | BIGINT | 外键 → product_sku，**唯一** |
| available_stock | INT | 可销售库存 |
| locked_stock | INT | 订单预占库存（下单未支付） |
| reserved_stock | INT | 特殊业务预留库存（活动预留等） |
| safety_stock | INT | 安全库存预警阈值 |
| version | INT | 乐观锁版本号，防并发超卖 |
| created_time | DATETIME | — |
| updated_time | DATETIME | — |

> **库存公式:** 物理库存 = available_stock + locked_stock + reserved_stock

**索引:**
- `uk_sku_id` UNIQUE (sku_id)

#### `inventory_record` — 库存流水

| 列名 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| inventory_id | BIGINT | 外键 → inventory |
| change_type | VARCHAR(20) | INBOUND / SALE_LOCK / SALE_CONFIRM / RETURN / RELEASE / RESERVE / UNRESERVE |
| change_qty | INT | 变动数量（正=增加，负=减少） |
| before_qty | INT | 变动前数量 |
| after_qty | INT | 变动后数量 |
| related_no | VARCHAR(64) | 关联单号（订单号/入库单号） |
| created_time | DATETIME | — |

**索引:**
- `idx_inventory_id` (inventory_id)
- `idx_related_no` (related_no)
- `idx_created_time` (created_time)

---

### 购物车域

#### `cart` — 购物车

| 列名 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| user_id | BIGINT | 外键 → user_account，唯一 |

**索引:**
- `uk_user_id` UNIQUE (user_id)

#### `cart_item` — 购物车条目

| 列名 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| cart_id | BIGINT | 外键 → cart |
| sku_id | BIGINT | 外键 → product_sku |
| quantity | INT | 加入数量 |
| selected | TINYINT(1) | 是否选中参与结算 |
| created_time | DATETIME | — |
| updated_time | DATETIME | — |

**索引:**
- `idx_cart_id` (cart_id)

---

### 订单域

#### `order_info` — 订单

| 列名 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| order_no | VARCHAR(32) | 全局唯一订单号 |
| user_id | BIGINT | 外键 → user_account |
| store_id | BIGINT | 外键 → store |
| total_amount | DECIMAL(12,2) | 商品总金额 |
| freight | DECIMAL(12,2) | 运费 |
| pay_amount | DECIMAL(12,2) | 实付金额 |
| status | VARCHAR(20) | PENDING_PAYMENT / PAID / PROCESSING / SHIPPED / COMPLETED / CANCELLED / REFUNDING / REFUNDED |
| receiver_snapshot | JSON | 收货地址快照 |
| remark | VARCHAR(256) | 用户备注 |
| created_time | DATETIME | 下单时间 |
| updated_time | DATETIME | — |
| deleted | TINYINT(1) | — |

**索引:**
- `uk_order_no` UNIQUE (order_no)
- `idx_user_id_created` (user_id, created_time)
- `idx_store_id_status` (store_id, status)
- `idx_status_created` (status, created_time)

#### `order_item` — 订单条目

| 列名 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| order_id | BIGINT | 外键 → order_info |
| sku_id | BIGINT | 外键 → product_sku |
| product_name | VARCHAR(256) | 商品名称快照 |
| sku_snapshot | JSON | 规格快照 |
| unit_price | DECIMAL(12,2) | 单价 |
| quantity | INT | 数量 |
| subtotal | DECIMAL(12,2) | 小计 |

**索引:**
- `idx_order_id` (order_id)

---

### 支付域

#### `payment` — 支付记录

| 列名 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| order_id | BIGINT | 外键 → order_info，唯一 |
| payment_no | VARCHAR(32) | 平台内部唯一支付编号 |
| pay_amount | DECIMAL(12,2) | 支付金额 |
| pay_method | VARCHAR(20) | WECHAT_QR / ALIPAY |
| status | VARCHAR(20) | PENDING / SUCCESS / FAILED |
| transaction_id | VARCHAR(64) | 第三方交易流水号，**唯一约束** |
| pay_time | DATETIME | 支付完成时间 |
| created_time | DATETIME | — |
| updated_time | DATETIME | — |

> **支付幂等设计:** `transaction_id` 建立 UNIQUE 约束。支付回调时先按 `transaction_id` 查询是否已处理，已处理则直接返回成功，防止重复扣款。

**索引:**
- `uk_order_id` UNIQUE (order_id)
- `uk_payment_no` UNIQUE (payment_no)
- `uk_transaction_id` UNIQUE (transaction_id)
- `idx_status` (status)

#### `refund` — 退款记录

| 列名 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| order_id | BIGINT | 外键 → order_info |
| payment_id | BIGINT | 外键 → payment |
| refund_amount | DECIMAL(12,2) | 退款金额 |
| reason | VARCHAR(256) | 退款原因 |
| status | VARCHAR(20) | APPLYING / APPROVED / REJECTED / SUCCESS |
| refund_transaction_id | VARCHAR(64) | 第三方退款单号 |
| created_time | DATETIME | — |
| completed_time | DATETIME | 退款完成时间 |

**索引:**
- `idx_order_id` (order_id)
- `idx_payment_id` (payment_id)

---

### AI 域

#### `conversation` — AI 对话会话

| 列名 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| user_id | BIGINT | 外键 → user_account |
| title | VARCHAR(128) | 会话主题 |
| context_json | JSON | 对话上下文 |
| created_time | DATETIME | — |
| updated_time | DATETIME | — |

**索引:**
- `idx_user_id` (user_id)

#### `message` — 对话消息

| 列名 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| conversation_id | BIGINT | 外键 → conversation |
| role | VARCHAR(20) | USER / ASSISTANT / SYSTEM |
| content | TEXT | 消息正文 |
| message_type | VARCHAR(20) | TEXT / PRODUCT_RECOMMEND / IMAGE |
| extra_data | JSON | 附加结构化数据 |
| created_time | DATETIME | — |

**索引:**
- `idx_conversation_id` (conversation_id)
- `idx_created_time` (created_time)

#### `ai_memory` — AI 记忆

| 列名 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| user_id | BIGINT | 外键 → user_account |
| memory_type | VARCHAR(20) | PREFERENCE / INTENT / CONTEXT |
| content | JSON | 记忆数据 |
| created_time | DATETIME | — |

**索引:**
- `idx_user_id_type` (user_id, memory_type)

#### `recommendation_record` — 推荐记录

| 列名 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| user_id | BIGINT | 外键 → user_account |
| products | JSON | 推荐结果 [{id, score}] |
| reason | VARCHAR(256) | 推荐理由 |
| scene | VARCHAR(20) | HOME / CART / PRODUCT_DETAIL / SEARCH |
| feedback | VARCHAR(20) | LIKE / DISLIKE / NO_FEEDBACK |
| created_time | DATETIME | — |

**索引:**
- `idx_user_id` (user_id)

---

### 媒体域

#### `media_file` — 媒体文件

| 列名 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| original_name | VARCHAR(256) | 原始文件名 |
| file_path | VARCHAR(512) | MinIO 存储路径 |
| file_type | VARCHAR(20) | IMAGE / VIDEO |
| file_size | BIGINT | 文件大小（字节） |
| mime_type | VARCHAR(64) | MIME 类型 |
| created_time | DATETIME | — |

#### `media_relation` — 媒体关联

| 列名 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| media_id | BIGINT | 外键 → media_file |
| target_type | VARCHAR(20) | PRODUCT / USER / AI_RESOURCE |
| target_id | BIGINT | 目标实体 ID |
| created_time | DATETIME | — |

---

### Agent 任务域

#### `agent_task` — AI Agent 任务

| 列名 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| user_id | BIGINT | 外键 → user_account |
| task_type | VARCHAR(20) | COMPARISON / BARGAIN_FIND / OUTFIT_SUGGESTION |
| task_params | JSON | 任务参数 |
| status | VARCHAR(20) | PENDING / PROCESSING / COMPLETED / FAILED |
| result | JSON | 任务结果 |
| created_time | DATETIME | — |
| completed_time | DATETIME | — |

#### `agent_task_step` — Agent 任务步骤

| 列名 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| task_id | BIGINT | 外键 → agent_task |
| step_order | INT | 步骤序号 |
| step_name | VARCHAR(64) | 步骤名称 |
| step_status | VARCHAR(20) | PENDING / RUNNING / COMPLETED / FAILED |
| step_input | JSON | 步骤输入 |
| step_output | JSON | 步骤输出 |
| created_time | DATETIME | — |
| completed_time | DATETIME | — |

---

### 搜索索引 (Elasticsearch)

#### `search_index` — 商品搜索索引

| 字段 | 类型 | 说明 |
|------|------|------|
| product_id | keyword | 商品 ID |
| product_name | text (ik_max_word) | 商品名称，中文分词 |
| category_name | keyword | 分类名称 |
| brand | keyword | 品牌 |
| price_min | double | 最低价格 |
| price_max | double | 最高价格 |
| tags | keyword[] | 标签 |
| created_time | date | 上架时间 |
| sales_count | integer | 销量（排序用） |

---

## 表关系总览

```
┌─────────────────────────────────────────────────────────┐
│                     关系总览图                            │
│                                                          │
│  ┌──────────────────┐                                    │
│  │   user_account   │                                    │
│  └───┬─────┬─────┬──┘                                    │
│      │     │     │                                       │
│      ▼     ▼     ▼                                       │
│  ┌──────┐┌───────┐┌──────────────┐                      │
│  │ cart ││ order ││ conversation │                      │
│  └──┬───┘│_info  │└──────┬───────┘                      │
│     │    └─┬──┬──┘       │                              │
│     ▼      │  │          ▼                              │
│  cart_item │  │       message                           │
│     │      │  │                                         │
│     │      │  └────→ payment                            │
│     │      │         │                                  │
│     │      │         └──→ refund                        │
│     │      │                                            │
│     └──────┼────→ product_sku ←── inventory             │
│            │           │             │                  │
│            │           │             └── inventory_record│
│            │           │                                │
│            ▼           ▼                                │
│          product ──── category                          │
│            │                                            │
│            ├── product_image                            │
│            └── search_index (ES)                        │
│                                                         │
│  ┌────────────────┐                                     │
│  │merchant_account│                                     │
│  └──┬──────────┬──┘                                     │
│     │          │                                        │
│     ▼          ▼                                        │
│   store   merchant_user                                 │
│     │                                                   │
│     └──→ product                                        │
│                                                          │
│  ┌───────────┐                                          │
│  │agent_task │                                          │
│  └─────┬─────┘                                          │
│        └── agent_task_step                              │
└─────────────────────────────────────────────────────────┘
```

---

## 数据库迁移策略 (Flyway)

### 生产环境变更规范

生产环境所有数据库变更必须通过 **Flyway** 进行版本化管理。

**禁止:** 人工直接修改生产数据库。

### 迁移文件命名规范

```
src/main/resources/db/migration/
├── V1__init.sql              # 初始建表
├── V2__add_merchant_fields.sql
├── V3__add_payment_index.sql
├── V4__add_inventory_reserved.sql
└── ...
```

### 迁移规范

| 项目 | 规范 |
|------|------|
| 版本号 | `V{序号}__{描述}.sql`，双下划线分隔 |
| 不可逆 | 版本一旦执行不可修改，新变更使用新版本号 |
| 幂等 | 同一版本号不可重复执行 |
| 回滚 | 生产环境不使用 `UNDO`，出问题时写新版本修复 |
| 事务 | DDL 语句单独一个版本文件（MySQL DDL 隐式提交） |
| 兼容 | 新增字段设 `DEFAULT` 值，不删除旧字段（标记 `@Deprecated`） |

### 本地开发

**从 Sprint 9 开始，所有环境（开发、测试、生产）均使用 Flyway + validate 方案，禁止在本地开发中使用 `ddl-auto=update`。**

---

> **文档版本:** v1.2  
> **最后更新:** 2026-07-26  
> **变更:** Sprint 9 Step 0/1 — 统一数据库迁移策略：所有环境使用 Flyway + validate，移除本地开发使用 ddl-auto=update 的推荐  
> **状态:** 设计阶段 — 仅定义设计规范，不创建 SQL 文件。
