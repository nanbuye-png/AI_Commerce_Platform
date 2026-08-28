# Order Domain 架构设计

> **文档版本:** v1.0  
> **最后更新:** 2026-07-26  
> **状态:** 设计阶段  
> **对应 Sprint:** Sprint 11 Step 0 — Order Domain 架构设计

---

## 目录

1. [Order Domain Boundary](#一order-domain-boundary)
2. [DDD 聚合设计](#二ddd-聚合设计)
3. [Entity Design](#三entity-design)
4. [Repository Design](#四repository-design)
5. [Order Status Design](#五order-status-design)
6. [订单生命周期](#六订单生命周期)
7. [API Design](#七api-design)
8. [DTO Design](#八dto-design)
9. [Domain Service Design](#九domain-service-design)
10. [扩展能力](#十扩展能力)
11. [数据库表设计 (Database Schema)](#十一数据库表设计-database-schema)

---

## 一、Order Domain Boundary

### 1.1 Domain 职责

Order Domain 负责以下核心业务逻辑：

| 职责 | 说明 | Sprint 11 |
|------|------|-----------|
| **创建订单** | 接收 Customer 下单请求，校验商品、库存、价格，生成订单快照 | ✅ |
| **保存订单快照** | 保存下单时的商品名称、SKU 名称、价格、图片、收货地址等快照信息 | ✅ |
| **订单状态管理** | 管理订单状态的合法流转，禁止非法状态跳转 | ✅ |
| **金额计算** | 计算商品金额、运费、优惠、实付金额 | ✅ |
| **Customer 查询订单** | C 端用户查询自己的订单列表和详情 | ✅ |
| **Merchant 查询订单** | 商家查询本店铺的订单列表和详情 | ✅ |
| **Admin 管理订单** | 平台管理员查询和管理所有订单 | ✅ |

### 1.2 Domain 不负责

| 职责 | 归属 Domain | 说明 |
|------|-------------|------|
| **Product 数据维护** | Product Domain | Order 不维护商品基本信息，仅保存下单时快照 |
| **Inventory 数据维护** | Inventory Domain | Order 通过 InventoryService 锁定/释放/扣减库存 |
| **Payment** | Payment Domain（后续 Sprint）| 支付由独立 Payment Domain 处理 |
| **Logistics** | Logistics Domain（后续 Sprint）| 物流由独立 Logistics Domain 处理 |
| **AI** | AI Service（独立服务）| 不依赖 AI 推荐、智能定价等 |

### 1.3 跨域依赖关系

```
Order Domain (Sprint 11)
    │
    ├── 依赖 Product Domain
    │   ├── Product 数据 → 创建订单时读取商品/ SKU 信息
    │   ├── ProductSku → 获取 SKU 价格、规格信息
    │   └── 引用方式: skuId (Long 弱引用) + 快照存储商品信息
    │
    ├── 依赖 Inventory Domain
    │   ├── 创建订单 → 调用 InventoryService.reserve() 锁定库存
    │   ├── 取消订单 → 调用 InventoryService.release() 释放库存
    │   ├── 支付成功 → 调用 InventoryService.deduct() 扣减库存
    │   └── 引用方式: Application Service 层调用
    │
    └── 依赖 User Domain
        ├── Customer → buyerId (Long 弱引用)
        ├── Merchant → merchantId (Long 弱引用)
        └── 引用方式: 仅存 ID，无 JPA 关联
```

**依赖规则：**

| 方向 | 允许 | 说明 |
|------|------|------|
| Order → Product | ✅ | 读取商品/SKU 信息，保存快照 |
| Order → Inventory | ✅ | 调用库存锁定/释放/扣减 |
| Order → User | ✅ | buyerId/merchantId 弱引用 |
| Product → Order | ❌ | Product 不感知 Order |
| Inventory → Order | ❌ | Inventory 通过 orderId 弱引用 |
| User → Order | ❌ | User 不感知 Order |

---

## 二、DDD 聚合设计

### 2.1 聚合根确定

| 聚合根 | 子对象 | 一致性边界 | 说明 |
|--------|--------|------------|------|
| **Order** | OrderItem | 强一致 | OrderItem 随 Order 同生命周期，不允许独立修改 |
| **Order** | OrderAddress | 强一致 | 收货地址作为订单快照，随 Order 同生命周期，不允许独立修改 |
| — | OrderHistory | 最终一致 | 订单状态变更历史，独立追加，不参与事务一致性 |

### 2.2 为什么 Order 是唯一聚合根

1. **生命周期一致性**：OrderItem 和 OrderAddress 的生命周期完全绑定 Order。订单创建时一起创建，订单取消/完成时一起归档，不存在独立存在的场景。
2. **数据一致性要求**：订单金额 = Σ(OrderItem.price × OrderItem.quantity)。如果 OrderItem 可独立修改，会导致聚合边界内数据不一致。
3. **快照语义**：OrderItem 和 OrderAddress 本质是下单时的快照，创建后原则上不应修改（除售后场景修改售后数量）。
4. **事务边界**：一个订单的所有操作（创建、状态变更、取消）应在同一事务内完成，OrderItem 和 OrderAddress 不应独立开启事务。

### 2.3 为什么 OrderItem 与 OrderAddress 不允许独立修改

| 设计 | 问题 | 结论 |
|------|------|------|
| OrderItem 独立修改 | ① 如果允许修改 OrderItem 数量，需要重新计算订单金额，影响支付<br>② 如果允许新增 OrderItem，相当于在原订单追加商品，违反订单不可变性<br>③ 如果允许删除 OrderItem，需要对已支付订单退款，涉及 Payment Domain | ❌ 不允许独立修改 |
| OrderAddress 独立修改 | ① 收货地址是下单时的快照，后续修改应新建地址而非修改订单地址<br>② 已发货订单修改地址影响物流履约<br>③ 业务上应通过重新下单或售后流程处理 | ❌ 不允许独立修改 |

**例外场景（需通过 Domain Service 协调）：**

| 场景 | 处理方式 |
|------|----------|
| 支付前修改收货地址 | 通过 OrderDomainService 生成新的 OrderAddress 替换原地址（整体替换） |
| 售后部分退款 | 通过售后流程创建售后单，不影响原 OrderItem |
| 订单拆分发货 | 通过 Logistics Domain 处理，不影响 Order |

### 2.4 聚合关系图

```
Order  (Aggregate Root)
  │
  ├── OrderItem (1:N)
  │     ├── skuId (弱引用 → ProductSku)
  │     ├── productName (快照)
  │     ├── skuName (快照)
  │     ├── price (快照)
  │     ├── quantity
  │     └── subtotal
  │
  ├── OrderAddress (1:1)
  │     ├── receiverName (快照)
  │     ├── phone (快照)
  │     ├── province (快照)
  │     ├── city (快照)
  │     ├── district (快照)
  │     └── detailAddress (快照)
  │
  └── OrderHistory (1:N)  ← 最终一致，独立体系
        ├── fromStatus
        ├── toStatus
        ├── operatorType
        └── remark
```

### 2.5 聚合边界内操作原则

| 操作 | 规则 |
|------|------|
| 创建订单 | 一个事务内创建 Order + OrderItem(s) + OrderAddress |
| 状态流转 | 通过 Order 聚合根统一变更状态，校验合法性 |
| 取消订单 | 校验状态允许取消 → 更新 Order 状态 → 释放库存 |
| 查询订单 | 聚合根查询 + 加载 OrderItem 和 OrderAddress |

---

## 三、Entity Design

### 3.1 Order（聚合根）

| 字段 | 类型 | 说明 | 是否允许修改 | 是否快照字段 |
|------|------|------|-------------|-------------|
| id | Long | 主键 | 否 | — |
| orderNo | String | 订单编号（全局唯一，业务可读） | 否 | — |
| buyerId | Long | 买家 ID → User(id) | 否 | — |
| merchantId | Long | 商家 ID → Merchant(id) | 否 | — |
| storeId | Long | 店铺 ID → Store(id) | 否 | — |
| orderStatus | OrderStatus | 订单状态 | 是（状态流转）| — |
| paymentStatus | PaymentStatus | 支付状态 | 是（状态流转）| — |
| shippingStatus | ShippingStatus | 发货状态 | 是（状态流转）| — |
| totalAmount | BigDecimal | 订单商品总金额（含运费） | 否 | ✅ |
| productAmount | BigDecimal | 商品金额（不含运费） | 否 | ✅ |
| freightAmount | BigDecimal | 运费金额 | 否 | ✅ |
| discountAmount | BigDecimal | 优惠金额 | 否 | ✅ |
| payAmount | BigDecimal | 实付金额 | 否 | ✅ |
| currency | String | 货币单位（默认 CNY） | 否 | ✅ |
| buyerRemark | String | 买家备注 | 是（支付前可改）| — |
| merchantRemark | String | 商家备注 | 是（商家可改）| — |
| paymentTime | LocalDateTime | 支付时间 | 否 | — |
| deliveryTime | LocalDateTime | 发货时间 | 否 | — |
| receivedTime | LocalDateTime | 收货时间 | 否 | — |
| completedTime | LocalDateTime | 完成时间 | 否 | — |
| cancelledTime | LocalDateTime | 取消时间 | 否 | — |
| createdTime | LocalDateTime | 创建时间 | 否 | — |
| updatedTime | LocalDateTime | 更新时间 | 否 | — |
| deleted | Boolean | 软删除标志 | 否 | — |

**重点说明：**

| 要点 | 设计原因 |
|------|----------|
| orderNo 全局唯一 | 业务编号，用于对账、客服查询、物流追踪 |
| 金额字段全部为快照 | 下单后商品价格变动不影响已创建订单，所有金额锁定 |
| 状态时间字段 | 每个状态变更记录精确时间，用于运营分析和异常排查 |
| buyerId/merchantId 为弱引用 | 仅存 ID，不建立 JPA 关联，避免跨域耦合 |

### 3.2 OrderItem（订单条目）

| 字段 | 类型 | 说明 | 是否允许修改 | 是否快照字段 |
|------|------|------|-------------|-------------|
| id | Long | 主键 | 否 | — |
| orderId | Long | 外键 → Order(id) | 否 | — |
| skuId | Long | 外键 → ProductSku(id) | 否 | — |
| productId | Long | 外键 → Product(id) | 否 | — |
| **productName** | String | **商品名称（快照）** | 否 | ✅ |
| **skuName** | String | **SKU 规格名称（快照）** | 否 | ✅ |
| **skuCode** | String | **SKU 编码（快照）** | 否 | ✅ |
| **price** | BigDecimal | **下单时单价（快照）** | 否 | ✅ |
| **originalPrice** | BigDecimal | **下单时原价/划线价（快照）** | 否 | ✅ |
| **imageUrl** | String | **商品图片 URL（快照）** | 否 | ✅ |
| quantity | Integer | 购买数量 | 否（售后除外） | — |
| subtotal | BigDecimal | 小计金额 = price × quantity | 否 | ✅ |
| weight | BigDecimal | 单个商品重量（kg，快照） | 否 | ✅ |
| createdTime | LocalDateTime | 创建时间 | 否 | — |

**重点说明：**

| 快照字段 | 为什么保存快照 |
|----------|----------------|
| **productName** | 商品名称可能被商家修改，订单需要保留下单时的名称 |
| **skuName** | SKU 规格组合可能被调整，如"黑色 128G"改为"黑色 256G" |
| **price / originalPrice** | 价格可能变动，订单金额以下单时为准 |
| **imageUrl** | 商品图片可能更换，订单保留下单时的图片用于展示 |

**约束：**
- quantity > 0
- subtotal = price × quantity
- 不允许独立于 Order 创建/修改/删除

### 3.3 OrderAddress（订单收货地址）

| 字段 | 类型 | 说明 | 是否允许修改 | 是否快照字段 |
|------|------|------|-------------|-------------|
| id | Long | 主键 | 否 | — |
| orderId | Long | 外键 → Order(id)，唯一 | 否 | — |
| **receiverName** | String | **收件人姓名（快照）** | 否 | ✅ |
| **phone** | String | **联系电话（快照）** | 否 | ✅ |
| **province** | String | **省（快照）** | 否 | ✅ |
| **city** | String | **市（快照）** | 否 | ✅ |
| **district** | String | **区/县（快照）** | 否 | ✅ |
| **detailAddress** | String | **详细地址（快照）** | 否 | ✅ |
| **zipCode** | String | **邮编（快照）** | 否 | ✅ |
| createdTime | LocalDateTime | 创建时间 | 否 | — |

**重点说明：**

| 要点 | 设计原因 |
|------|----------|
| 全部字段为快照 | 用户的地址本信息可能变更，订单需要保留下单时的收货地址 |
| orderId 唯一 | 一个订单只有一个收货地址（暂不支持拆单到不同地址） |
| 修改策略 | 支付前可通过 Domain Service 整体替换 OrderAddress，已支付/已发货则通过售后流程处理 |

**约束：**
- orderId 唯一：一个订单仅有一条收货地址
- 不允许独立于 Order 创建/修改/删除

---

## 四、Repository Design

### 4.1 OrderRepository

| 方法 | 说明 |
|------|------|
| `save(Order order)` | 保存订单（含 OrderItem 级联保存） |
| `findById(Long id)` | 按主键查询 |
| `findByOrderNo(String orderNo)` | 按订单编号查询（唯一） |
| `findByBuyerId(Long buyerId, Pageable pageable)` | Customer 分页查询自己的订单 |
| `findByMerchantId(Long merchantId, Pageable pageable)` | Merchant 分页查询本店订单 |
| `findByOrderStatus(OrderStatus status, Pageable pageable)` | 按订单状态查询 |
| `countByMerchantIdAndOrderStatus(Long merchantId, OrderStatus status)` | 统计商家某个状态的订单数 |

### 4.2 OrderItemRepository

| 方法 | 说明 |
|------|------|
| `saveAll(List<OrderItem> items)` | 批量保存（随 Order 级联保存） |
| `findByOrderId(Long orderId)` | 查询指定订单的所有条目 |
| `findByOrderIdAndSkuId(Long orderId, Long skuId)` | 查询指定订单中指定 SKU 的条目 |

### 4.3 OrderAddressRepository

| 方法 | 说明 |
|------|------|
| `save(OrderAddress address)` | 保存地址（随 Order 级联保存） |
| `findByOrderId(Long orderId)` | 查询指定订单的收货地址 |

### 4.4 分页查询设计

**Customer 端订单列表查询：**

| 参数 | 说明 |
|------|------|
| buyerId | 自动从 Security Context 获取 |
| orderStatus | 可选筛选（ALL / PENDING_PAYMENT / PAID / ...） |
| startTime | 创建时间范围 |
| endTime | 创建时间范围 |
| page / pageSize | 分页 |

**Merchant 端订单列表查询：**

| 参数 | 说明 |
|------|------|
| merchantId | 自动从 Security Context 获取 |
| orderStatus | 可选筛选 |
| orderNo | 按订单号搜索 |
| buyerName | 按买家名称搜索 |
| startTime | 创建时间范围 |
| endTime | 创建时间范围 |
| page / pageSize | 分页 |

### 4.5 查询优化策略

| 策略 | 说明 |
|------|------|
| **延迟加载** | OrderItem / OrderAddress 使用懒加载，仅查询订单列表时不加载子实体 |
| **分页查询** | 所有列表查询使用 Pageable 分页，禁止全表扫描 |
| **复合索引** | merchantId + orderStatus + createdTime 复合索引覆盖商家订单列表查询 |
| **买家索引** | buyerId + orderStatus + createdTime 复合索引覆盖买家订单列表查询 |

---

## 五、Order Status Design

### 5.1 三状态模型

Order Domain 使用三个独立的状态枚举来管理订单的完整生命周期：

| 状态枚举 | 职责 | 独立原因 |
|----------|------|----------|
| **OrderStatus** | 订单主状态 | 控制订单的核心业务流程 |
| **PaymentStatus** | 支付状态 | 支付由 Payment Domain 驱动，独立跟踪 |
| **ShippingStatus** | 发货状态 | 物流由 Logistics Domain 驱动，独立跟踪 |

**为什么使用三状态模型而非单一状态：**

| 方案 | 问题 |
|------|------|
| ❌ 单一状态字段 PENDING_PAYMENT → PAID → SHIPPED → COMPLETED | 无法同时表达"已支付待发货"和"已支付部分发货"等组合状态 |
| ✅ 三状态模型 | OrderStatus 控制主流程，PaymentStatus 跟踪支付进度，ShippingStatus 跟踪发货进度 |

### 5.2 OrderStatus 设计

| 状态 | 说明 | 允许流转到 |
|------|------|-----------|
| **PENDING_PAYMENT** | 等待支付（订单已创建，库存已锁定） | PAID, CANCELLED, CLOSED |
| **PAID** | 已支付（支付成功，待发货） | PROCESSING, CANCELLED (申请退款), CLOSED |
| **PROCESSING** | 商家处理中（待发货或部分发货） | SHIPPED, CANCELLED (申请退款) |
| **SHIPPED** | 已发货（全部发货） | COMPLETED, REFUNDING |
| **COMPLETED** | 已完成（用户确认收货或自动确认） | REFUNDING, CLOSED |
| **CANCELLED** | 已取消（支付前取消或超时取消） | 终态（不可流转） |
| **CLOSED** | 已关闭（售后完成、超时关闭等） | 终态（不可流转） |
| **REFUNDING** | 退款中（售后处理中） | REFUNDED, CLOSED |
| **REFUNDED** | 已退款（售后完成） | 终态（不可流转） |

### 5.3 PaymentStatus 设计

| 状态 | 说明 | 允许流转到 |
|------|------|-----------|
| **UNPAID** | 未支付 | PAID, CANCELLED |
| **PAID** | 已支付 | REFUNDING |
| **REFUNDING** | 退款中 | REFUNDED |
| **REFUNDED** | 已退款 | 终态 |
| **PARTIAL_REFUNDED** | 部分退款 | — |

### 5.4 ShippingStatus 设计

| 状态 | 说明 | 允许流转到 |
|------|------|-----------|
| **UNSHIPPED** | 未发货 | SHIPPED, PARTIALLY_SHIPPED |
| **PARTIALLY_SHIPPED** | 部分发货 | SHIPPED |
| **SHIPPED** | 已发货 | RECEIVED |
| **RECEIVED** | 已收货 | 终态 |

### 5.5 完整状态流转图

```
                              OrderStatus 主流程
                              ═══════════════════

                    ┌──────────────────────────────────────┐
                    │           PENDING_PAYMENT             │  ← 创建订单
                    │           (等待支付)                   │
                    └────────┬──────────────┬───────────────┘
                             │              │
                ┌────────────┘              └────────────┐
                ▼                                        ▼
        ┌───────────────┐                        ┌──────────────┐
        │     PAID      │                        │   CANCELLED  │  ← 取消/超时
        │   (已支付)     │                        │   (已取消)    │  ← 终态
        └───────┬───────┘                        └──────────────┘
                │
                ▼
        ┌───────────────┐
        │  PROCESSING   │
        │  (处理中)      │
        └───────┬───────┘
                │
                ▼
        ┌───────────────┐
        │   SHIPPED     │
        │  (已发货)      │
        └───────┬───────┘
                │
        ┌────────┴────────┐
        ▼                  ▼
 ┌────────────┐    ┌──────────────┐
 │ COMPLETED  │    │  REFUNDING   │  ← 售后申请
 │ (已完成)    │    │  (退款中)     │
 └────────────┘    └───────┬──────┘
          ▲                │
          │                ▼
          │         ┌──────────────┐
          └─────────│   REFUNDED   │  ← 终态
                    │  (已退款)     │
                    └──────────────┘
                                   
                          ┌──────────────┐
                          │   CLOSED     │  ← 终态（超时关闭/售后关闭）
                          │  (已关闭)     │
                          └──────────────┘
```

```
               PaymentStatus 与 ShippingStatus 联合流转
               ════════════════════════════════════════

PaymentStatus:          UNPAID ──→ PAID ──→ REFUNDING ──→ REFUNDED
                                                          PARTIAL_REFUNDED

ShippingStatus:         UNSHIPPED ──→ PARTIALLY_SHIPPED ──→ SHIPPED ──→ RECEIVED
```

### 5.6 状态流转规则

| 规则 | 说明 |
|------|------|
| **正向流转不可逆** | PENDING_PAYMENT → PAID → PROCESSING → SHIPPED → COMPLETED 不可逆向回退 |
| **取消拦截** | 仅 PENDING_PAYMENT 和 PAID 状态可取消 | 
| **退款通道** | PAID / PROCESSING / SHIPPED / COMPLETED 均可进入 REFUNDING |
| **终态不可流转** | CANCELLED / REFUNDED / CLOSED 为终态，任何操作不可改变 |
| **支付状态依赖** | OrderStatus.PAID 必须伴随 PaymentStatus.PAID |
| **发货状态依赖** | OrderStatus.SHIPPED 必须伴随 ShippingStatus.SHIPPED |

### 5.7 禁止非法状态跳转

| 非法跳转 | 原因 |
|----------|------|
| PENDING_PAYMENT → SHIPPED | 未支付不可发货 |
| PENDING_PAYMENT → COMPLETED | 未支付未发货不可完成 |
| PAID → COMPLETED | 已支付未发货不可完成 |
| CANCELLED → PAID | 已取消不可恢复支付 |
| COMPLETED → SHIPPED | 已完成不可回退发货 |
| REFUNDED → PAID | 已退款不可恢复 |

---

## 六、订单生命周期

### 6.1 完整生命周期图

```
Customer                        Order Service                   Inventory Service           Payment Service
   │                                  │                               │                           │
   │ 1. 创建订单                       │                               │                           │
   │ ──────────────────────────────→  │                               │                           │
   │                                  │── 2. 校验商品/SKU 有效性 ──→ Product Domain                │
   │                                  │── 3. 校验库存可用性 ──────→ Inventory                     │
   │                                  │── 4. 锁定库存 ────────────→ reserve() ──→ [ACTIVE]        │
   │                                  │── 5. 计算金额（含运费）       │                           │
   │                                  │── 6. 保存订单快照             │                           │
   │   ←── 返回 orderNo  ───────────  │                               │                           │
   │                                  │                               │                           │
   │ 7. 支付                           │                               │                           │
   │ ──────────────────────────────────────────────────────────────────────────────────→ Payment │
   │                                  │                               │                           │
   │                                  │ 8. 支付回调通知               │                           │
   │                                  │ ←────────────────────────────────────────────────────── │
   │                                  │── 9. 扣减库存 ────────────→ deduct() ──→ [DEDUCTED]     │
   │                                  │── 10. 更新订单状态 → PAID                               │
   │                                  │                                                         │
   │                                  │ Merchant 发货                                          │
   │                                  │── 11. 更新订单状态 → PROCESSING → SHIPPED               │
   │                                  │── 12. 更新 ShippingStatus                              │
   │                                  │                                                         │
   │ 13. 确认收货                      │                                                         │
   │ ──────────────────────────────→  │                                                         │
   │                                  │── 14. 更新订单状态 → COMPLETED                          │
   │                                  │── 15. 更新 ShippingStatus → RECEIVED                   │
```

### 6.2 核心业务流程

#### 6.2.1 创建订单流程

```
1. Customer 提交下单请求
   ├── SKU ID 列表 + 数量
   ├── 收货地址 ID
   └── 买家备注

2. OrderDomainService.createOrder()
   ├── 校验请求参数合法性
   ├── 查询 SKU 信息（Product Domain）
   │   ├── SKU 是否存在
   │   ├── SKU 是否 ACTIVE
   │   └── 获取 SKU 价格、名称、图片（快照数据）
   │
   ├── 计算订单金额
   │   ├── productAmount = Σ(price × quantity)
   │   ├── freightAmount = 运费计算（按重量/件数）
   │   ├── discountAmount = 优惠金额（预留）
   │   ├── totalAmount = productAmount + freightAmount - discountAmount
   │   └── payAmount = totalAmount
   │
   ├── 调用 InventoryService.reserve()
   │   ├── 校验库存充足
   │   ├── 锁定库存（乐观锁）
   │   ├── 创建 Reservation（ACTIVE）
   │   └── 若库存不足 → 抛 BusinessException
   │
   ├── 构建 Order Aggregate
   │   ├── 生成 Order（orderNo, status=PENDING_PAYMENT, paymentStatus=UNPAID）
   │   ├── 构建 OrderItem 列表（含快照字段）
   │   ├── 构建 OrderAddress（从地址 ID 复制快照）
   │   └── 保存 Order + OrderItem(s) + OrderAddress（同一事务）
   │
   └── 返回 OrderVO（含 orderNo、payAmount 等）
```

#### 6.2.2 支付成功回调流程

```
1. PaymentService 回调 OrderService.onPaymentSuccess(orderNo)

2. OrderDomainService.processPayment()
   ├── 校验当前状态允许支付（PENDING_PAYMENT）
   ├── 更新 OrderStatus → PAID
   ├── 更新 PaymentStatus → PAID
   ├── 记录 paymentTime
   ├── 调用 InventoryService.deduct()
   │   ├── 更新 Reservation 状态 → DEDUCTED
   │   └── 扣减总库存
   └── 返回成功
```

#### 6.2.3 取消订单流程

```
1. Customer/Merchant/Admin 请求取消订单

2. OrderDomainService.cancelOrder()
   ├── 校验当前状态允许取消
   │   ├── PENDING_PAYMENT → 直接取消
   │   └── PAID → 需要退款流程
   │
   ├── 调用 InventoryService.release()
   │   ├── 更新 Reservation 状态 → RELEASED
   │   └── 释放锁定库存
   │
   ├── 更新 OrderStatus → CANCELLED
   ├── 更新 PaymentStatus
   │   ├── 已支付 → REFUNDING（进入退款流程）
   │   └── 未支付 → CANCELLED
   ├── 记录 cancelledTime
   └── 返回结果
```

### 6.3 库存交互说明

| 阶段 | 库存操作 | 操作方 | 说明 |
|------|----------|--------|------|
| 创建订单 | reserve() | Order → Inventory | 锁定库存，Order 不直接修改库存表 |
| 支付成功 | deduct() | Order → Inventory | 扣减锁定库存，Order 不直接修改库存表 |
| 取消订单 | release() | Order → Inventory | 释放锁定库存，Order 不直接修改库存表 |
| 超时未支付 | release() | 定时任务 | Inventory 定时扫描自动释放 |

**核心原则：Order 不直接修改库存表。** 库存的所有变更操作通过 InventoryService 完成，由 Inventory Domain 保证数据一致性和并发安全。

---

## 七、API Design

### 7.1 API 分组总览

| 分组 | 基础路径 | 角色 | 说明 |
|------|----------|------|------|
| Customer | `/api/orders` | USER | C 端订单管理（创建、查询、取消） |
| Merchant | `/api/merchant/orders` | MERCHANT | 商家订单管理（查询、发货、处理） |
| Admin | `/api/admin/orders` | ADMIN | 平台订单管控 |

### 7.2 Customer 端 API

**基础路径：** `/api/orders`

#### POST /api/orders — 创建订单

- **权限：** USER
- **功能：** 创建新订单

**Request：**
```json
{
  "sku_items": [
    { "sku_id": 3001, "quantity": 2 },
    { "sku_id": 3002, "quantity": 1 }
  ],
  "address_id": 5001,
  "buyer_remark": "请尽快发货"
}
```

**Response：**
```json
{
  "code": 0,
  "data": {
    "order_no": "ORD202607260001",
    "pay_amount": 599.00,
    "order_status": "PENDING_PAYMENT",
    "created_time": "2026-07-26T10:00:00"
  }
}
```

**异常返回：**

| HTTP Status | Code | 说明 |
|-------------|------|------|
| 400 | 32001 | 商品不存在 |
| 400 | 32002 | 商品已下架 |
| 400 | 32003 | 库存不足 |
| 400 | 32004 | 收货地址不存在 |

#### GET /api/orders — 我的订单列表

- **权限：** USER
- **功能：** 分页查询当前用户订单

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| order_status | string | 否 | ALL / PENDING_PAYMENT / PAID / SHIPPED / COMPLETED |
| page | int | 否 | 默认 1 |
| pageSize | int | 否 | 默认 20 |

**Response：** 分页 OrderVO 列表

#### GET /api/orders/{orderNo} — 订单详情

- **权限：** USER
- **功能：** 查看订单详情（含 OrderItem 和 OrderAddress）

**Response：** OrderVO（含 OrderItemVO 列表 + OrderAddressVO）

#### POST /api/orders/{orderNo}/cancel — 取消订单

- **权限：** USER
- **功能：** 取消未支付或已支付的订单

**Request：**
```json
{
  "reason": "商品不需要了"
}
```

**限制：**

| 状态 | 允许取消 | 处理方式 |
|------|----------|----------|
| PENDING_PAYMENT | ✅ 允许 | 直接取消，释放库存 |
| PAID | ✅ 允许 | 进入退款流程 |
| PROCESSING | ❌ 不允许 | 需联系客服 |
| SHIPPED | ❌ 不允许 | 需拒收后退款 |

#### POST /api/orders/{orderNo}/confirm-receipt — 确认收货

- **权限：** USER
- **功能：** 用户确认收货

**限制：** 仅 SHIPPED 状态可确认收货

#### GET /api/orders/{orderNo}/tracking — 订单物流追踪

- **权限：** USER
- **功能：** 查看订单物流信息（预留）

### 7.3 Merchant 端 API

**基础路径：** `/api/merchant/orders`

#### GET /api/merchant/orders — 商家订单列表

- **权限：** MERCHANT
- **功能：** 分页查询本店铺订单

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| order_no | string | 否 | 按订单号搜索 |
| order_status | string | 否 | 按状态筛选 |
| buyer_name | string | 否 | 按买家名搜索 |
| start_time | string | 否 | 下单时间起始 |
| end_time | string | 否 | 下单时间结束 |
| page | int | 否 | 默认 1 |
| pageSize | int | 否 | 默认 20 |

#### GET /api/merchant/orders/{orderNo} — 订单详情（商家视角）

- **权限：** MERCHANT
- **功能：** 查看订单详情

**额外字段：** 含买家联系方式（脱敏）

#### POST /api/merchant/orders/{orderNo}/ship — 发货

- **权限：** MERCHANT
- **功能：** 商家发货

**Request：**
```json
{
  "logistics_company": "SF",
  "logistics_no": "SF1234567890",
  "items": [
    { "order_item_id": 10001, "quantity": 2 }
  ]
}
```

**限制：**

| 状态 | 允许发货 |
|------|----------|
| PAID | ✅ 允许 |
| PROCESSING | ✅ 允许（部分发货） |
| SHIPPED | ❌ 已全部发货 |
| COMPLETED | ❌ 已完成 |

#### POST /api/merchant/orders/{orderNo}/remark — 商家备注

- **权限：** MERCHANT
- **功能：** 添加/修改商家备注

**Request：**
```json
{
  "remark": "客户要求发顺丰"
}
```

### 7.4 Admin 端 API

**基础路径：** `/api/admin/orders`

#### GET /api/admin/orders — 全平台订单列表

- **权限：** ADMIN
- **功能：** 查询全平台所有订单

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| merchant_id | int | 否 | 按商家筛选 |
| order_no | string | 否 | 按订单号搜索 |
| order_status | string | 否 | 按状态筛选 |
| start_time | string | 否 | 下单时间起始 |
| end_time | string | 否 | 下单时间结束 |
| page | int | 否 | 默认 1 |
| pageSize | int | 否 | 默认 20 |

#### GET /api/admin/orders/{orderNo} — 订单详情（管理员视角）

- **权限：** ADMIN
- **额外字段：** 含商家信息、买家信息（完整）、操作日志

#### POST /api/admin/orders/{orderNo}/cancel — 强制取消订单

- **权限：** ADMIN
- **功能：** 平台强制取消违规订单
- **限制：** 高危操作，需记录操作日志

#### POST /api/admin/orders/{orderNo}/close — 强制关闭订单

- **权限：** ADMIN
- **功能：** 强制关闭已完成的订单（售后异常处理）

### 7.5 Internal API（服务间调用）

| API | 说明 | 调用方 |
|-----|------|--------|
| `POST /api/internal/orders/payment-callback` | 支付成功回调 | Payment Service |
| `POST /api/internal/orders/logistics-update` | 物流状态更新 | Logistics Service |

---

## 八、DTO Design

### 8.1 CreateOrderRequest

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| skuItems | List\<SkuItem\> | ✅ | 购买 SKU 列表 |
| addressId | Long | ✅ | 收货地址 ID |
| buyerRemark | String | ❌ | 买家备注 |

**SkuItem：**

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| skuId | Long | ✅ | SKU ID |
| quantity | Integer | ✅ | 购买数量（> 0） |

### 8.2 CreateOrderResponse

| 字段 | 类型 | 说明 |
|------|------|------|
| orderNo | String | 订单编号 |
| payAmount | BigDecimal | 实付金额 |
| orderStatus | String | 订单状态 |
| createdTime | LocalDateTime | 创建时间 |

### 8.3 OrderVO

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 订单 ID |
| orderNo | String | 订单编号 |
| buyerId | Long | 买家 ID |
| buyerName | String | 买家名称 |
| merchantId | Long | 商家 ID |
| storeId | Long | 店铺 ID |
| storeName | String | 店铺名称 |
| orderStatus | String | 订单状态 |
| paymentStatus | String | 支付状态 |
| shippingStatus | String | 发货状态 |
| totalAmount | BigDecimal | 商品总金额 |
| freightAmount | BigDecimal | 运费金额 |
| discountAmount | BigDecimal | 优惠金额 |
| payAmount | BigDecimal | 实付金额 |
| buyerRemark | String | 买家备注 |
| merchantRemark | String | 商家备注 |
| items | List\<OrderItemVO\> | 订单条目列表 |
| address | OrderAddressVO | 收货地址 |
| paymentTime | LocalDateTime | 支付时间 |
| deliveryTime | LocalDateTime | 发货时间 |
| receivedTime | LocalDateTime | 收货时间 |
| createdTime | LocalDateTime | 创建时间 |

### 8.4 OrderItemVO

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 条目 ID |
| skuId | Long | SKU ID |
| productId | Long | 商品 ID |
| productName | String | 商品名称（快照） |
| skuName | String | SKU 规格名称（快照） |
| skuCode | String | SKU 编码 |
| price | BigDecimal | 单价（快照） |
| originalPrice | BigDecimal | 原价（快照） |
| imageUrl | String | 商品图片 URL（快照） |
| quantity | Integer | 购买数量 |
| subtotal | BigDecimal | 小计 |

### 8.5 OrderAddressVO

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 地址 ID |
| receiverName | String | 收件人姓名 |
| phone | String | 联系电话（部分脱敏） |
| province | String | 省 |
| city | String | 市 |
| district | String | 区/县 |
| detailAddress | String | 详细地址 |
| zipCode | String | 邮编 |

---

## 九、Domain Service Design

### 9.1 OrderDomainService（Domain Service）

**职责：** 订单领域核心逻辑，不依赖外部基础设施。

| 方法 | 说明 | 归属 |
|------|------|------|
| `createOrder(CreateOrderRequest)` | 创建订单：校验、计算金额、构建聚合 | Domain |
| `calculateAmount(List<SkuItem>)` | 计算订单金额（商品金额 + 运费 - 优惠） | Domain |
| `validateOrderStatus(Order, OrderStatus)` | 校验状态流转是否合法 | Domain |
| `validateSkuItems(List<SkuItem>)` | 校验 SKU 有效性（状态、价格） | Domain |

### 9.2 OrderApplicationService（Application Service）

**职责：** 编排 Domain Service + 外部依赖 + 事务管理。

| 方法 | 说明 | 归属 |
|------|------|------|
| `placeOrder(CreateOrderRequest)` | 下单：调用 Domain Service + 库存锁定 + 持久化 | Application |
| `payOrder(String orderNo)` | 支付回调：更新状态 + 扣减库存 | Application |
| `cancelOrder(String orderNo, String reason)` | 取消订单：校验 + 释放库存 + 状态更新 | Application |
| `shipOrder(String orderNo, ShipRequest)` | 发货：校验 + 更新状态 + 物流信息 | Application |
| `confirmReceipt(String orderNo)` | 确认收货：更新状态 | Application |
| `queryCustomerOrders(...)` | Customer 查询订单 | Application |
| `queryMerchantOrders(...)` | Merchant 查询订单 | Application |
| `queryAdminOrders(...)` | Admin 查询订单 | Application |

### 9.3 分层调用关系

```
┌─────────────────────────────────────────────┐
│            Controller 层                      │
│  (参数校验 / 权限校验 / 响应转换)               │
└─────────────────┬───────────────────────────┘
                  │
                  ▼
┌─────────────────────────────────────────────┐
│         OrderApplicationService               │
│  (事务管理 / 服务编排 / 跨域调用)               │
│  ┌─────────────────────────────────────────┐ │
│  │   OrderDomainService                    │ │
│  │   (领域逻辑 / 状态校验 / 金额计算)        │ │
│  └─────────────────────────────────────────┘ │
└─────────────────┬───────────────────────────┘
                  │
    ┌─────────────┼─────────────┐
    ▼             ▼             ▼
┌────────┐ ┌──────────┐ ┌────────────────┐
│ Order  │ │ Inventory│ │ ProductDomain  │
│ Repo   │ │ Service  │ │ (远程调用)      │
└────────┘ └──────────┘ └────────────────┘
```

### 9.4 Domain Service 与 Application Service 职责划分

| 逻辑 | 归属 | 理由 |
|------|------|------|
| 订单金额计算规则 | Domain Service | 纯业务逻辑，无外部依赖 |
| 状态流转校验 | Domain Service | 纯业务规则 |
| 订单聚合构建 | Domain Service | 领域对象组装 |
| 库存锁定调用 | Application Service | 跨域协调 |
| 事务管理 | Application Service | 基础设施关注点 |
| 事件发布 | Application Service | 跨域通知 |
| 权限校验 | Application Service | 安全关注点 |
| 流水记录 | Application Service | 审计关注点 |

---

## 十、扩展能力

### 10.1 订单状态历史

**预留方案：** OrderHistory 实体

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 主键 |
| orderId | Long | 订单 ID |
| fromStatus | String | 变更前状态 |
| toStatus | String | 变更后状态 |
| operatorType | String | 操作人类型（CUSTOMER / MERCHANT / ADMIN / SYSTEM）|
| operatorId | Long | 操作人 ID |
| remark | String | 备注说明 |
| createdTime | LocalDateTime | 变更时间 |

**设计要点：**
- Append-Only 模式，历史记录不可修改
- 索引：orderId + createdTime 复合索引
- 状态变更时自动记录

### 10.2 支付事件

**预留方案：** 支付 Domain Event

```
OrderPaymentEvent
├── eventType: PAYMENT_SUCCESS / PAYMENT_FAILED / PAYMENT_REFUND
├── orderNo: 订单号
├── transactionId: 支付流水号
├── payAmount: 支付金额
├── payMethod: 支付方式（ALIPAY / WECHAT / CARD）
└── timestamp: 时间戳
```

**集成方式：** Payment Service 通过 MQ 发布事件 → Order Service 消费更新订单状态

### 10.3 物流事件

**预留方案：** 物流 Domain Event

```
LogisticsEvent
├── eventType: SHIPPED / IN_TRANSIT / DELIVERED / SIGNED
├── orderNo: 订单号
├── logisticsCompany: 物流公司
├── logisticsNo: 物流单号
└── timestamp: 时间戳
```

**集成方式：** Logistics Service 通过 MQ 发布事件 → Order Service 更新发货状态

### 10.4 优惠券

**预留方案：**

| 设计 | 说明 |
|------|------|
| Coupon Domain | 独立领域管理优惠券的创建、发放、核销 |
| 与 Order 集成 | 下单时在 OrderApplicationService 中调用 CouponService 校验和锁定优惠券 |
| 金额计算 | Domain Service 中预留 discountAmount 字段，后续扩展优惠计算逻辑 |
| 快照保存 | 订单中保存优惠券快照信息：couponId、discountAmount、规则描述 |

### 10.5 售后

**预留方案：**

| 设计 | 说明 |
|------|------|
| AfterSale Domain | 独立领域管理退货/退款/换货 |
| 与 Order 集成 | 售后单通过 orderNo 关联订单 |
| 与 Payment 集成 | 退款通过 Payment Service 处理 |
| 与 Inventory 集成 | 退货入库通过 Inventory Service 处理 |
| 状态影响 | 售后处理中 Order 进入 REFUNDING 状态 |

### 10.6 多商家拆单

**预留方案：**

| 场景 | 处理方式 |
|------|----------|
| 购物车含多个商家商品 | 前端按 merchantId 分组，分批调用创建订单 API |
| 一个请求跨商家 | 后端拆单：OrderApplicationService 按 merchantId 拆分为多个 Order |
| 拆单策略 | 每个商家一个订单，独立支付、发货、售后 |

### 10.7 事件驱动（Domain Event）

**预留事件清单：**

| 事件 | 触发时机 | 订阅方 | 说明 |
|------|----------|--------|------|
| OrderCreatedEvent | 订单创建成功 | Inventory / Search / Notification | 通知库存已锁定、构建搜索索引、发送通知 |
| OrderPaidEvent | 支付成功 | Merchant / Logistics / Analytics | 通知商家发货、准备物流、收集数据 |
| OrderShippedEvent | 商家发货 | Customer / Analytics | 通知买家、更新数据分析 |
| OrderCompletedEvent | 确认收货 | Analytics / Merchant | 通知商家完成售后时效计算 |
| OrderCancelledEvent | 订单取消 | Inventory / Payment | 释放库存、发起退款 |
| OrderRefundedEvent | 退款完成 | Inventory / Product | 退货入库、恢复销量统计 |

**事件数据结构示例：**
```json
{
  "eventId": "UUID",
  "eventType": "ORDER_CREATED",
  "timestamp": "2026-07-26T10:00:00",
  "data": {
    "orderNo": "ORD202607260001",
    "buyerId": 1001,
    "merchantId": 2001,
    "totalAmount": 599.00,
    "items": [
      { "skuId": 3001, "productName": "蓝牙耳机", "quantity": 2, "price": 199.00 }
    ]
  }
}
```

### 10.8 扩展架构总图

```
                       ┌─────────────────────────┐
                       │     Order Domain         │
                       │                          │
                       │  ┌──────────────────┐   │
                       │  │ OrderAggregate    │   │
                       │  │  ├─ Order         │   │
                       │  │  ├─ OrderItem     │   │
                       │  │  └─ OrderAddress  │   │
                       │  └──────────────────┘   │
                       │                          │
                       │  Domain Events ──────→  MQ
                       └──────────┬───────────────┘
                                  │
        ┌─────────────────────────┼─────────────────────────┐
        │                         │                         │
        ▼                         ▼                         ▼
┌──────────────┐       ┌──────────────────┐       ┌────────────────┐
│ Inventory     │       │  Payment Domain   │       │ Logistics      │
│ Domain        │       │  (后续 Sprint)    │       │ Domain         │
│ 库存锁定/释放 │       │   支付处理         │       │ (后续 Sprint)   │
│ 库存扣减      │       │   退款处理         │       │  物流追踪       │
└──────────────┘       └──────────────────┘       └────────────────┘

        ┌─────────────────────────────────────────────────────┐
        │                  Future Domains                      │
        │                                                      │
        │  Coupon Domain    AfterSale Domain    Notification   │
        │  优惠券管理          售后管理          通知推送       │
        └─────────────────────────────────────────────────────┘
```

---

## 十一、数据库表设计 (Database Schema)

### 11.1 表结构总览

```
commerce-platform (MySQL 8.0)
├── order                  # 订单主表
├── order_item             # 订单条目（快照）
├── order_address          # 订单收货地址（快照）
└── order_history          # 订单状态变更历史（预留）
```

### 11.2 `order` — 订单主表

| 列名 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 主键 |
| order_no | VARCHAR(32) | NOT NULL, UNIQUE | 订单编号 |
| buyer_id | BIGINT | NOT NULL | → user(id) |
| merchant_id | BIGINT | NOT NULL | → merchant(id) |
| store_id | BIGINT | NOT NULL | → store(id) |
| order_status | VARCHAR(20) | NOT NULL | 订单主状态 |
| payment_status | VARCHAR(20) | NOT NULL | 支付状态 |
| shipping_status | VARCHAR(20) | NOT NULL | 发货状态 |
| total_amount | DECIMAL(12,2) | NOT NULL | 订单总金额 |
| product_amount | DECIMAL(12,2) | NOT NULL | 商品金额 |
| freight_amount | DECIMAL(12,2) | NOT NULL, DEFAULT 0 | 运费金额 |
| discount_amount | DECIMAL(12,2) | NOT NULL, DEFAULT 0 | 优惠金额 |
| pay_amount | DECIMAL(12,2) | NOT NULL | 实付金额 |
| currency | VARCHAR(3) | NOT NULL, DEFAULT 'CNY' | 货币单位 |
| buyer_remark | VARCHAR(500) | — | 买家备注 |
| merchant_remark | VARCHAR(500) | — | 商家备注 |
| payment_time | DATETIME | — | 支付时间 |
| delivery_time | DATETIME | — | 发货时间 |
| received_time | DATETIME | — | 收货时间 |
| completed_time | DATETIME | — | 完成时间 |
| cancelled_time | DATETIME | — | 取消时间 |
| created_time | DATETIME | NOT NULL | 创建时间 |
| updated_time | DATETIME | NOT NULL | 更新时间 |
| deleted | TINYINT(1) | NOT NULL, DEFAULT 0 | 软删除标志 |

**索引：**

| 索引名 | 列 | 类型 |
|--------|-----|------|
| uk_order_no | order_no | 唯一索引 |
| idx_buyer_id | buyer_id | 普通索引 |
| idx_merchant_id | merchant_id | 普通索引 |
| idx_buyer_status_created | (buyer_id, order_status, created_time) | 复合索引 |
| idx_merchant_status_created | (merchant_id, order_status, created_time) | 复合索引 |
| idx_order_status | order_status | 普通索引 |
| idx_created_time | created_time | 普通索引 |

### 11.3 `order_item` — 订单条目表

| 列名 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 主键 |
| order_id | BIGINT | NOT NULL | → order(id) |
| sku_id | BIGINT | NOT NULL | → product_sku(id) |
| product_id | BIGINT | NOT NULL | → product(id) |
| product_name | VARCHAR(256) | NOT NULL | 商品名称（快照） |
| sku_name | VARCHAR(256) | NOT NULL | SKU 规格名称（快照） |
| sku_code | VARCHAR(64) | NOT NULL | SKU 编码（快照） |
| price | DECIMAL(12,2) | NOT NULL | 单价（快照） |
| original_price | DECIMAL(12,2) | — | 原价（快照） |
| image_url | VARCHAR(512) | — | 商品图片 URL（快照） |
| quantity | INT | NOT NULL | 购买数量 |
| subtotal | DECIMAL(12,2) | NOT NULL | 小计 |
| weight | DECIMAL(10,3) | DEFAULT 0 | 重量（kg，快照） |
| created_time | DATETIME | NOT NULL | 创建时间 |

**索引：**

| 索引名 | 列 | 类型 |
|--------|-----|------|
| idx_order_id | order_id | 普通索引 |
| idx_sku_id | sku_id | 普通索引 |

### 11.4 `order_address` — 订单收货地址表

| 列名 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 主键 |
| order_id | BIGINT | UNIQUE, NOT NULL | → order(id) |
| receiver_name | VARCHAR(64) | NOT NULL | 收件人姓名（快照） |
| phone | VARCHAR(20) | NOT NULL | 联系电话（快照） |
| province | VARCHAR(32) | NOT NULL | 省（快照） |
| city | VARCHAR(32) | NOT NULL | 市（快照） |
| district | VARCHAR(32) | NOT NULL | 区/县（快照） |
| detail_address | VARCHAR(256) | NOT NULL | 详细地址（快照） |
| zip_code | VARCHAR(10) | — | 邮编（快照） |
| created_time | DATETIME | NOT NULL | 创建时间 |

**索引：**

| 索引名 | 列 | 类型 |
|--------|-----|------|
| uk_order_id | order_id | 唯一索引 |

### 11.5 与其他领域表的关系

```
product (Product Domain)        user (User Domain)
    │                               │
    │ sku_id / product_id            │ buyer_id / merchant_id
    ▼                               ▼
order_item ──→ order ←── order_address
                  │
                  │ order_id (仅 Long)
                  ▼
          inventory_reservation (Inventory Domain)
```

**外键约束：**
- 所有跨域引用使用 Long ID 弱引用，不建立物理外键约束
- order_item.order_id / order_address.order_id → order.id 建立物理外键（同 Domain）
- 订单不存在级联删除（订单数据不可删除）

---

> **文档版本:** v1.0  
> **最后更新:** 2026-07-26  
> **状态:** 设计阶段  
> **对应 Sprint:** Sprint 11 Step 0 — Order Domain 架构设计