# AI Commerce Platform Domain Model

本文档定义了 AI Commerce Platform 的核心业务领域模型，涵盖用户、商家、商品、库存、交易、支付、营销、搜索、AI 智能等业务领域。

---

## 一、用户领域 (User Domain)

### User（用户）
用户基础实体，存储账号信息与个人资料。

| 字段 | 类型 | 说明 |
|------|------|------|
| 用户ID | Long | 主键，自增 |
| 用户名 | String | 唯一，登录凭证 |
| 邮箱 | String | 唯一，用于找回密码/通知 |
| 手机号 | String | 唯一，用于登录/验证码 |
| 密码信息 | String | 加密存储（BCrypt） |
| 状态 | Enum | ACTIVE / DISABLED / DELETED |
| 创建时间 | DateTime | 注册时间 |
| 更新时间 | DateTime | 最后更新时间 |

### UserAddress（用户地址）
```
User 1 ──── N UserAddress
```

| 字段 | 类型 | 说明 |
|------|------|------|
| 地址ID | Long | 主键 |
| 用户ID | Long | 外键 → User |
| 收件人 | String | 收货人姓名 |
| 手机号 | String | 收货人电话 |
| 省/市/区 | String | 地区信息 |
| 详细地址 | String | 街道/门牌号 |
| 是否默认 | Boolean | 默认地址标志 |
| 创建时间 | DateTime | — |

### UserFavorite（用户收藏）
```
User 1 ──── N UserFavorite
```

| 字段 | 类型 | 说明 |
|------|------|------|
| 收藏ID | Long | 主键 |
| 用户ID | Long | 外键 → User |
| 商品ID | Long | 外键 → Product |
| 创建时间 | DateTime | 收藏时间 |

### UserBrowseHistory（用户浏览历史）
```
User 1 ──── N UserBrowseHistory
```

| 字段 | 类型 | 说明 |
|------|------|------|
| 浏览ID | Long | 主键 |
| 用户ID | Long | 外键 → User |
| 商品ID | Long | 外键 → Product |
| 浏览时间 | DateTime | 浏览时刻 |
| 停留时长 | Integer | 秒数 |

### UserBehavior（用户行为 — AI 推荐用）
```
User 1 ──── N UserBehavior
```

用于 AI 推荐系统，记录用户在平台上的各类行为。

| 字段 | 类型 | 说明 |
|------|------|------|
| 行为ID | Long | 主键 |
| 用户ID | Long | 外键 → User |
| 商品ID | Long | 外键 → Product |
| 行为类型 | Enum | VIEW / SEARCH / CLICK / PURCHASE |
| 行为时间 | DateTime | 发生时刻 |
| 扩展数据 | JSON | 行为上下文（如搜索关键词） |

---

## 二、商家领域 (Merchant Domain)

### Merchant（商家账号）
平台的入驻商家主体。

| 字段 | 类型 | 说明 |
|------|------|------|
| 商家ID | Long | 主键 |
| 商家名称 | String | 企业/品牌名称 |
| 联系人 | String | 负责人姓名 |
| 联系电话 | String | — |
| 邮箱 | String | — |
| 状态 | Enum | PENDING / ACTIVE / DISABLED |
| 创建时间 | DateTime | 入驻时间 |
| 更新时间 | DateTime | — |

### Store（店铺）
一个商家可拥有多个店铺（品牌店/旗舰店）。

```
Merchant 1 ──── N Store
```

| 字段 | 类型 | 说明 |
|------|------|------|
| 店铺ID | Long | 主键 |
| 商家ID | Long | 外键 → Merchant |
| 店铺名称 | String | — |
| 店铺Logo | String | 图片URL |
| 店铺描述 | String | — |
| 状态 | Enum | ACTIVE / CLOSED |
| 创建时间 | DateTime | — |
| 更新时间 | DateTime | — |

### MerchantUser（商家员工）
商家内部的员工账号，用于管理店铺。

```
Merchant 1 ──── N MerchantUser
```

| 字段 | 类型 | 说明 |
|------|------|------|
| 员工ID | Long | 主键 |
| 商家ID | Long | 外键 → Merchant |
| 用户名 | String | 登录名 |
| 密码 | String | 加密存储 |
| 角色 | Enum | ADMIN / OPERATOR / CUSTOMER_SERVICE |
| 状态 | Enum | ACTIVE / DISABLED |
| 创建时间 | DateTime | — |
| 更新时间 | DateTime | — |

---

## 三、商品领域 (Product Domain)

### Category（商品分类）

| 字段 | 类型 | 说明 |
|------|------|------|
| 分类ID | Long | 主键 |
| 父分类ID | Long | 自关联，支持多级分类 |
| 名称 | String | — |
| 排序 | Integer | 排序权重 |
| 创建时间 | DateTime | — |
| 更新时间 | DateTime | — |

### Product（商品 SPU）
标准产品单位（Standard Product Unit）。

```
Category 1 ──── N Product
```

| 字段 | 类型 | 说明 |
|------|------|------|
| 商品ID | Long | 主键 |
| 店铺ID | Long | 外键 → Store |
| 分类ID | Long | 外键 → Category |
| 名称 | String | 商品标题 |
| 描述 | Text | 商品详情 |
| 品牌 | String | — |
| 状态 | Enum | DRAFT / ON_SHELF / OFF_SHELF |
| 创建时间 | DateTime | — |
| 更新时间 | DateTime | — |

### ProductSKU（商品 SKU）
库存量单位（Stock Keeping Unit）。表示具体销售规格。

```
Product 1 ──── N ProductSKU
```

例如：iPhone 16 → SKU: 128G 黑色 / 256G 白色

| 字段 | 类型 | 说明 |
|------|------|------|
| SKU ID | Long | 主键 |
| 商品ID | Long | 外键 → Product |
| 规格描述 | JSON | 如 `{"color":"黑色","storage":"128G"}` |
| 价格 | BigDecimal | 售价 |
| 原价 | BigDecimal | 划线价 |
| 状态 | Enum | ACTIVE / DISABLED |
| 创建时间 | DateTime | — |
| 更新时间 | DateTime | — |

> **库存唯一来源: Inventory 域** — ProductSKU 只负责商品规格信息（规格描述、价格），**不存储库存数量**。库存统一由 Inventory 实体管理（`available_stock` / `locked_stock` / `reserved_stock`）。

### ProductImage（商品图片）

```
Product 1 ──── N ProductImage
```

| 字段 | 类型 | 说明 |
|------|------|------|
| 图片ID | Long | 主键 |
| 商品ID | Long | 外键 → Product |
| 图片URL | String | — |
| 排序 | Integer | 展示顺序 |
| 是否首图 | Boolean | 封面图标志 |
| 创建时间 | DateTime | — |

---

## 四、库存领域 (Inventory Domain)

### Inventory（库存）
与 SKU 一一对应。

```
ProductSKU 1 ──── 1 Inventory
```

| 字段 | 类型 | 说明 |
|------|------|------|
| 库存ID | Long | 主键 |
| SKU ID | Long | 外键 → ProductSKU，唯一 |
| 可售库存 (available_stock) | Integer | 当前可销售库存 |
| 锁定库存 (locked_stock) | Integer | 订单创建后预占库存（下单未支付时锁定） |
| 预留库存 (reserved_stock) | Integer | 特殊业务预留库存（如活动预留、预售预留） |
| 安全库存 | Integer | 预警阈值（低于该值触发补货提醒） |
| 版本号 | Integer | 乐观锁，防并发超卖 |
| 创建时间 | DateTime | — |
| 更新时间 | DateTime | — |

**库存数据流:**

```
available_stock = 物理库存 - locked_stock - reserved_stock

下单时:
  locked_stock += 购买数量

支付成功后:
  available_stock -= 购买数量
  locked_stock -= 购买数量

订单取消/超时:
  locked_stock -= 购买数量（释放预占）

退款完成:
  available_stock += 退款数量（恢复库存）
```

### InventoryRecord（库存流水）
每次库存变动的记录日志。

```
Inventory 1 ──── N InventoryRecord
```

| 字段 | 类型 | 说明 |
|------|------|------|
| 记录ID | Long | 主键 |
| 库存ID | Long | 外键 → Inventory |
| 变动类型 | Enum | INBOUND / SALE / RETURN / PROMOTION |
| 变动数量 | Integer | 正=增加，负=减少 |
| 变动前 | Integer | — |
| 变动后 | Integer | — |
| 关联单号 | String | 订单号/入库单号 |
| 创建时间 | DateTime | — |

---

## 五、购物车领域 (Cart Domain)

### Cart（购物车）
每个用户拥有一个购物车。

```
User 1 ──── 1 Cart
```

| 字段 | 类型 | 说明 |
|------|------|------|
| 购物车ID | Long | 主键 |
| 用户ID | Long | 外键 → User，唯一 |

### CartItem（购物车条目）

```
Cart 1 ──── N CartItem
```

| 字段 | 类型 | 说明 |
|------|------|------|
| 条目ID | Long | 主键 |
| 购物车ID | Long | 外键 → Cart |
| SKU ID | Long | 外键 → ProductSKU |
| 数量 | Integer | 加入数量 |
| 是否选中 | Boolean | 结算时是否参与 |
| 创建时间 | DateTime | — |
| 更新时间 | DateTime | — |

---

## 六、订单领域 (Order Domain)

### Order（订单）

```
User 1 ──── N Order
```

| 字段 | 类型 | 说明 |
|------|------|------|
| 订单ID | Long | 主键 |
| 订单号 | String | 全局唯一，业务可读 |
| 用户ID | Long | 外键 → User |
| 店铺ID | Long | 外键 → Store |
| 订单金额 | BigDecimal | 商品总价 |
| 运费 | BigDecimal | — |
| 实付金额 | BigDecimal | 订单金额 + 运费 - 优惠 |
| 状态 | Enum | 见 OrderStatus |
| 收货地址 | JSON | 下单时快照 |
| 备注 | String | 用户备注 |
| 创建时间 | DateTime | — |
| 更新时间 | DateTime | — |

### OrderItem（订单条目）

```
Order 1 ──── N OrderItem
```

每个 OrderItem 关联一个 ProductSKU。

| 字段 | 类型 | 说明 |
|------|------|------|
| 条目ID | Long | 主键 |
| 订单ID | Long | 外键 → Order |
| SKU ID | Long | 外键 → ProductSKU |
| 商品名称 | String | 下单时快照 |
| 规格快照 | JSON | 下单时快照 |
| 单价 | BigDecimal | — |
| 数量 | Integer | — |
| 小计 | BigDecimal | — |

### OrderStatus（订单状态枚举）

| 状态 | 英文 | 说明 |
|------|------|------|
| 待支付 | PENDING_PAYMENT | 订单已创建，等待用户支付 |
| 已支付 | PAID | 支付成功，等待商家处理 |
| 处理中 | PROCESSING | 商家备货/拣货中 |
| 已发货 | SHIPPED | 商品已出库，物流配送中 |
| 已完成 | COMPLETED | 用户确认收货/自动确认，订单终态 |
| 已取消 | CANCELLED | 订单取消（用户取消/超时取消/商家取消），终态 |
| 退款中 | REFUNDING | 退款申请处理中 |
| 已退款 | REFUNDED | 退款已完成，终态 |

### 订单状态流转规则

**正向流程（正常购买）:**

```
PENDING_PAYMENT  (待支付)
       │
       │ 用户完成支付
       ▼
    PAID          (已支付)
       │
       │ 商家确认开始处理
       ▼
  PROCESSING      (处理中)
       │
       │ 商家发货
       ▼
   SHIPPED        (已发货)
       │
       │ 用户确认收货 / 自动确认收货（7天）
       ▼
  COMPLETED       (已完成)
```

**异常流程 — 取消:**

```
PENDING_PAYMENT  →  CANCELLED   (支付前取消: 用户主动取消 / 30分钟超时未支付自动取消)
PAID             →  CANCELLED   (支付后、发货前取消: 需商家同意)
PROCESSING       →  CANCELLED   (处理中取消: 需商家同意)
```

**异常流程 — 退款:**

```
PAID             →  REFUNDING  →  REFUNDED   (支付后申请退款，商家同意 → 直接退款)
PROCESSING       →  REFUNDING  →  REFUNDED   (处理中申请退款)
SHIPPED          →  REFUNDING  →  REFUNDED   (已发货后退款: 需退货流程，商家确认收货后退款)
```

**状态流转约束表:**

| 当前状态 | 允许流转到 |
|----------|-----------|
| PENDING_PAYMENT | PAID, CANCELLED |
| PAID | PROCESSING, CANCELLED, REFUNDING |
| PROCESSING | SHIPPED, CANCELLED, REFUNDING |
| SHIPPED | COMPLETED, REFUNDING |
| COMPLETED | (终态) |
| CANCELLED | (终态) |
| REFUNDING | REFUNDED |
| REFUNDED | (终态) |

---

## 七、支付领域 (Payment Domain)

### Payment（支付记录）

```
Order 1 ──── 1 Payment
```

| 字段 | 类型 | 说明 |
|------|------|------|
| 支付ID | Long | 主键 |
| 订单ID | Long | 外键 → Order，唯一 |
| 支付编号 (payment_no) | String | 平台内部唯一支付编号 |
| 支付金额 | BigDecimal | — |
| 支付方式 | Enum | WECHAT_QR / ALIPAY / ... |
| 支付状态 | Enum | PENDING / SUCCESS / FAILED |
| 第三方交易流水号 (transaction_id) | String | 微信/支付宝流水号，**唯一约束** |
| 支付时间 | DateTime | — |
| 创建时间 | DateTime | — |
| 更新时间 | DateTime | — |

> **支付幂等设计:** `transaction_id` 建立 **UNIQUE 约束**。支付回调时先按 `transaction_id` 查询是否已处理，已处理则直接返回成功，保证重复回调的幂等性。

### Refund（退款记录）

```
Order 1 ──── N Refund
```

| 字段 | 类型 | 说明 |
|------|------|------|
| 退款ID | Long | 主键 |
| 订单ID | Long | 外键 → Order |
| 支付ID | Long | 外键 → Payment |
| 退款金额 | BigDecimal | — |
| 退款原因 | String | — |
| 退款状态 | Enum | APPLYING / APPROVED / REJECTED / SUCCESS |
| 第三方退款单号 | String | — |
| 创建时间 | DateTime | — |
| 完成时间 | DateTime | — |

**v1.0 支持：** 微信扫码支付。

---

## 八、营销领域 (Promotion Domain)

> ⚠ 当前阶段仅做设计，暂不实现代码。

### Promotion（营销活动）

| 字段 | 类型 | 说明 |
|------|------|------|
| 活动ID | Long | 主键 |
| 活动名称 | String | — |
| 活动类型 | Enum | DISCOUNT / FULL_REDUCTION / GIFT |
| 开始时间 | DateTime | — |
| 结束时间 | DateTime | — |
| 状态 | Enum | DRAFT / ACTIVE / ENDED |
| 规则配置 | JSON | 满减/折扣参数 |

### Coupon（优惠券）

| 字段 | 类型 | 说明 |
|------|------|------|
| 优惠券ID | Long | 主键 |
| 名称 | String | — |
| 类型 | Enum | DISCOUNT / CASH |
| 面值 | BigDecimal | 折扣额/金额 |
| 门槛 | BigDecimal | 满多少可用 |
| 库存 | Integer | 发放总量 |
| 有效期 | DateTime ~ DateTime | — |
| 状态 | Enum | ACTIVE / EXPIRED |

### SeckillActivity（秒杀活动）

| 字段 | 类型 | 说明 |
|------|------|------|
| 秒杀ID | Long | 主键 |
| SKU ID | Long | 外键 → ProductSKU |
| 秒杀价 | BigDecimal | — |
| 秒杀库存 | Integer | — |
| 开始时间 | DateTime | — |
| 结束时间 | DateTime | — |

---

## 九、搜索领域 (Search Domain)

### SearchIndex（搜索索引）

商品搜索基于 Elasticsearch 实现。

| 索引字段 | 来源 | 说明 |
|----------|------|------|
| 商品ID | Product | — |
| 商品名称 | Product | 分词搜索 |
| 分类名称 | Category | 分类过滤 |
| 品牌 | Product | 品牌筛选 |
| 价格区间 | ProductSKU | 范围过滤 |
| 标签 | Product | 标签筛选 |
| 上架时间 | Product | 排序 |

**支持功能：**
- 商品关键词搜索
- 分类搜索
- 筛选（品牌、价格、标签）
- 排序（销量、价格、时间）

---

## 十、AI 智能领域 (AI Domain)

### Conversation（对话会话）

```
User 1 ──── N Conversation
```

| 字段 | 类型 | 说明 |
|------|------|------|
| 会话ID | Long | 主键 |
| 用户ID | Long | 外键 → User |
| 标题 | String | 会话主题 |
| 上下文 | JSON | 对话上下文快照 |
| 创建时间 | DateTime | — |
| 更新时间 | DateTime | — |

### Message（对话消息）

```
Conversation 1 ──── N Message
```

| 字段 | 类型 | 说明 |
|------|------|------|
| 消息ID | Long | 主键 |
| 会话ID | Long | 外键 → Conversation |
| 角色 | Enum | USER / ASSISTANT / SYSTEM |
| 内容 | Text | 消息正文 |
| 消息类型 | Enum | TEXT / PRODUCT_RECOMMEND / IMAGE |
| 扩展数据 | JSON | 附带的结构化数据 |
| 创建时间 | DateTime | — |

### AI Memory（AI 记忆）
长期记忆，用于个性化服务。

| 字段 | 类型 | 说明 |
|------|------|------|
| 记忆ID | Long | 主键 |
| 用户ID | Long | 外键 → User |
| 记忆类型 | Enum | PREFERENCE / INTENT / CONTEXT |
| 内容 | JSON | 记忆结构化数据 |
| 创建时间 | DateTime | — |

### RecommendationRecord（推荐记录）

```
User 1 ──── N RecommendationRecord
```

| 字段 | 类型 | 说明 |
|------|------|------|
| 推荐ID | Long | 主键 |
| 用户ID | Long | 外键 → User |
| 推荐商品 | JSON | 推荐结果列表 [{id, score}] |
| 推荐理由 | String | 如"基于您浏览过的相似商品" |
| 推荐场景 | Enum | HOME / CART / PRODUCT_DETAIL / SEARCH |
| 用户反馈 | Enum | LIKE / DISLIKE / NO_FEEDBACK |
| 创建时间 | DateTime | — |

### AI 购物助手流程

```
用户需求
   ↓
Agent 分析 (LLM)
   ↓
商品检索 (Elasticsearch)
   ↓
推荐结果 (重排序 + 个性化)
   ↓
返回用户
```

---

## 十一、整体领域关系图

```
┌─────────────────────────────────────────────────────┐
│                     User Domain                       │
│  User ──1:N──> UserAddress                           │
│  User ──1:N──> UserFavorite ──N:1── Product         │
│  User ──1:N──> UserBrowseHistory ──N:1── Product    │
│  User ──1:N──> UserBehavior ──N:1── Product         │
└────────────────────┬──────────────────────────────────┘
                     │
                     │ 1:1
                     ▼
┌─────────────────────────────────────────────────────┐
│                     Cart Domain                      │
│  User ──1:1──> Cart ──1:N──> CartItem ──N:1── SKU  │
└────────────────────┬──────────────────────────────────┘
                     │
                     │ 1:N
                     ▼
┌─────────────────────────────────────────────────────┐
│                    Order Domain                      │
│  User ──1:N──> Order ──1:N──> OrderItem             │
│                          OrderItem ──N:1── SKU       │
│  Order ──1:1──> Payment                              │
│  Order ──1:N──> Refund                                │
│  Order.Status: PENDING_PAYMENT → PAID →             │
│                PROCESSING → SHIPPED → COMPLETED     │
│                CANCELLED / REFUNDING → REFUNDED      │
└────────────────────┬──────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────┐
│                    Product Domain                    │
│  Category ──1:N──> Product ──1:N──> ProductImage    │
│                     Product ──1:N──> ProductSKU      │
│                                SKU ──1:1──> Inventory│
│                                         Inventory ──1:N──> InventoryRecord
└────────────────────┬──────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────┐
│                  Merchant Domain                     │
│  Merchant ──1:N──> Store ──1:N──> Product           │
│  Merchant ──1:N──> MerchantUser                     │
└────────────────────┬──────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────┐
│                 Search Domain                        │
│  Product ────> SearchIndex (Elasticsearch)           │
│  Category ────> SearchIndex (分类过滤)               │
└─────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────┐
│                 AI Domain                            │
│  User ──1:N──> Conversation ──1:N──> Message        │
│  User ──1:N──> AI Memory                            │
│  User ──1:N──> RecommendationRecord                 │
│  RecommendationRecord ──> Product (推荐结果)         │
│  UserBehavior ──> AI Training (行为数据)             │
└─────────────────────────────────────────────────────┘
```

### 关键链路总结

| 链路 | 路径 |
|------|------|
| **用户 → 交易** | User → Order → Payment |
| **用户 → 购物车** | User → Cart → CartItem → ProductSKU |
| **商品 → 库存** | Product → ProductSKU → Inventory |
| **用户 → 推荐** | User → UserBehavior → RecommendationRecord |
| **商品 → 搜索** | Product → SearchIndex (ES) |
| **商家 → 商品** | Merchant → Store → Product |
| **AI 对话** | User → Conversation → Message → Product (推荐) |

---

> **文档版本:** v1.1  
> **最后更新:** 2026-07-25  
> **变更:** Sprint 0 Step 3.5 — 库存模型修正（ProductSKU 移除 stock 字段，Inventory 新增 reserved_stock），订单状态机补全（PENDING_PAYMENT/PROCESSING 细化 + 状态流转约束表），支付幂等设计（payment_no + transaction_id UNIQUE），通用时间字段统一（Category/Merchant/MerchantUser/Store/ProductSKU/Inventory/Payment 新增 created_time/updated_time）  
> **状态:** 设计阶段 — 仅定义领域模型，不涉及具体实现代码。