# ADR-0003: Inventory Domain 架构决策

> **状态：** 已批准  
> **提出日期：** 2026-07-26  
> **对应 Sprint：** Sprint 10 Step 0 — Inventory Domain 架构设计  
> **相关文档：** [inventory-domain-architecture.md](../inventory-domain-architecture.md)

---

## 目录

1. [标题](#标题)
2. [背景](#背景)
3. [决策 1：Inventory 独立成域](#决策-1inventory-独立成域)
4. [决策 2：不将库存放入 Product 域](#决策-2不将库存放入-product-域)
5. [决策 3：独立设计 InventoryReservation](#决策-3独立设计-inventoryreservation)
6. [决策 4：采用事件驱动](#决策-4采用事件驱动)
7. [决策 5：库存三字段模型](#决策-5库存三字段模型)
8. [决策 6：InventoryMovement Append-Only](#决策-6inventorymovement-append-only)
9. [决策 7：MovementType 完整预留](#决策-7movementtype-完整预留)
10. [决策 8：Inventory 不依赖 Order](#决策-8inventory-不依赖-order)
11. [后续扩展方向](#后续扩展方向)

---

## 标题

**Inventory Domain 独立为业务域**

## 背景

在 Sprint 9 中，Product Domain（商品域）已完成了商品 SPU、SKU、分类、图片、规格等核心模型的架构设计与代码实现。但库存管理（Inventory）在设计上被明确剥离为独立领域。

在电商架构演进中，库存管理面临着高并发扣减、多订单锁定、分布式事务、超卖预防、多渠道库存同步等复杂挑战，需要独立的事务边界、乐观锁机制和审计追溯能力。随着后续购物车、订单、支付等模块的接入，库存将成为核心瓶颈点。

因此需要在 Sprint 10 中正式将 Inventory 设计为独立业务域，为后续模块提供统一库存能力。

---

## 决策 1：Inventory 独立成域

### 决策

将 Inventory 作为电商平台的**独立业务域**，拥有独立的 Entity、Repository、Service、Controller 分层，独立的 Flyway Migration 文件，以及独立的 package 路径 `com.commerce.platform.inventory`。

### 理由

| 理由 | 说明 |
|------|------|
| **高并发隔离** | 库存操作是高并发场景（秒杀、抢购），需要独立的事务和乐观锁控制，不应与商品写操作共享事务边界 |
| **独立生命周期** | 库存的创建、扣减、释放、调整等操作与商品信息变更无关，属于不同的业务频率和一致性要求 |
| **领域自治** | 库存涉及多个上游业务方（订单、购物车、售后），需要提供统一的领域服务接口而非分散在各个域中 |
| **扩展性** | 未来可能引入多仓库、多渠道库存、安全库存预警、ERP 对接等能力，独立域更易扩展 |
| **错误隔离** | 库存服务故障不应影响商品信息的正常读取 |

### 后果

- 正：Product Domain 不需要关心库存一致性，简化商品服务逻辑
- 正：Inventory 可独立部署（未来可拆为独立微服务）
- 负：查询商品详情时需要跨域 JOIN 或 RPC 获取库存信息
- 负：跨域事务一致性需要事件驱动或 Saga 模式保证

---

## 决策 2：不将库存放入 Product 域

### 方案对比

| 方案 | 描述 | 评估 |
|------|------|------|
| **方案 A（否决）** | 在 ProductSku 上增加 `stock`、`locked_stock` 字段 | ❌ 库存与商品耦合，高并发相互影响 |
| **方案 B（否决）** | 在 Product Domain 中增加 Inventory Entity | ❌ 语义不内聚，Product Service 需要管理库存逻辑 |
| **方案 C（采用）** | Inventory 独立为业务域 | ✅ 领域边界清晰，独立扩展 |

### 理由

1. **分离关注点**：Product 负责"有什么商品"，Inventory 负责"还有多少库存"
2. **性能隔离**：商品信息的读多写少 vs 库存的高频写操作，共享表会导致行锁竞争
3. **业务语义**：库存的调整（入库、出库、盘点、报损）与商品编辑是完全不同的业务场景
4. **已在 Sprint 9 中确认**：ProductSku 不存储库存数量，通过 `sku_id` 弱引用 Inventory

### 参考

ADR-002（Product Domain ADR）已明确："Inventory 不纳入商品聚合"。本 ADR 进一步确认了 Inventory 作为独立域的设计。

---

## 决策 3：独立设计 InventoryReservation

### 决策

必须设计独立的 `inventory_reservation` 表，禁止在 `inventory` 表中使用 `locked_count` 或类似字段记录所有锁定关系。

### 理由

| 需求场景 | InventoryReservation 独立表 | inventory.locked_stock 字段 |
|----------|---------------------------|---------------------------|
| 一个订单多个 SKU | ✅ 支持多条 Reservation | ❌ 无法区分订单 |
| 超时自动释放 | ✅ 可按 created_time 查询超时记录 | ❌ 无法定位需释放的记录 |
| 支付成功扣减 | ✅ 可精确匹配 Reservation → Deduct | ❌ 无法关联支付流水 |
| 部分退款/售后释放 | ✅ 可针对单条 Reservation 释放 | ❌ 无法精确定位 |
| 审计追溯 | ✅ 完整锁定/释放历史 | ❌ 仅保留当前锁定数量 |

### 设计要点

- `inventory_reservation` 与 `inventory` 通过 `(inventory_id, sku_id)` 关联
- 每条 Reservation 记录订单 ID、SKU ID、锁定数量、状态（ACTIVE / DEDUCTED / RELEASED / EXPIRED）、过期时间
- 支持按订单 ID 查询所有 Reservation 记录
- 支持定时任务扫描超时未支付的 Reservation 并自动释放

---

## 决策 4：采用事件驱动

### 决策

Inventory Domain 通过**领域事件**（Domain Event）与下游域（Order、Payment）通信，避免 Inventory 直接调用 Order 或 Payment 的 Repository 或 Service。

### 事件分类

| 事件 | 用途 | 范围 | 目标 |
|------|------|------|------|
| `InventoryReservedEvent` | 订单创建后库存锁定成功 | 应用内事件 → MQ | 通知 Order 可继续流转 |
| `InventoryReleasedEvent` | 订单取消/支付失败后释放库存 | 应用内事件 → MQ | 通知 Order 释放完成 |
| `InventoryDeductedEvent` | 支付成功后扣减库存 | 应用内事件 → MQ | 通知 Order 库存已扣减，可发货 |
| `InventoryAdjustedEvent` | 商家/管理员手工调整库存 | 应用内事件 | 记录审计日志 |
| `InventoryLowStockEvent` | 库存低于安全阈值 | MQ | 通知商家补货 |

### 理由

1. **解耦**：Inventory 不需要知道订单或支付的具体实现
2. **异步化**：库存扣减等操作可以通过 MQ 异步处理，削峰填谷
3. **可追溯**：事件日志可作为审计和问题排查的依据
4. **扩展性**：未来新的订阅方（如 Search 域需要更新库存状态）只需监听事件

### 约束

- 关键路径（如订单创建锁库存）需要同步返回结果，异常时需回滚
- 非关键路径（如库存调整通知）可采用完全异步

---

## 决策 5：库存三字段模型

### 决策

Inventory 实体必须采用三字段模型：

- `availableStock`（可售库存）
- `reservedStock`（已锁定库存）
- `totalStock`（总库存）

满足不变约束：**totalStock = availableStock + reservedStock**

### 理由

1. **业务语义清晰**：三个字段分别对应"还能卖"、"已被锁定"、"总共有"
2. **避免计算误差**：通过约束自检，避免 `stock - locked` 出现负数
3. **支持并发安全**：锁库存仅修改 `reservedStock`，扣减在支付阶段执行
4. **支持复杂场景**：部分锁定、部分退款等场景不需要额外的数据扫描

### 操作规则

| 操作 | SQL 变更 |
|------|----------|
| 入库 | `availableStock += quantity; totalStock += quantity` |
| 锁定 | `reservedStock += quantity; availableStock -= quantity` |
| 释放 | `reservedStock -= quantity; availableStock += quantity` |
| 扣减 | `reservedStock -= quantity; totalStock -= quantity` |
| 调整 | 根据调整类型修改对应字段 |

---

## 决策 6：InventoryMovement Append-Only

### 决策

`inventory_movement` 表采用 **Append-Only** 模式：

- 仅新增（INSERT）
- 不更新（UPDATE）
- 不物理删除（DELETE）
- 不软删除（deleted 标记）

作为库存审计流水，永久保存。

### 理由

1. **审计合规**：库存变动记录需永久保存，便于对账和问题追溯
2. **数据完整性**：禁止修改历史记录，确保流水线不可篡改
3. **性能可接受**：库存流水以 INSERT 为主，单行数据量小，无 UPDATE 热点

### 说明

- 允许新增业务类型（MovementType），但禁止修改历史记录
- 流水记录需包含：变动前值、变动后值、变动数量、操作人、业务单号、备注
- 查询时可按时间范围、SKU ID、业务类型进行筛选

---

## 决策 7：MovementType 完整预留

### 决策

`MovementType` 枚举必须预先设计完整，提前预留所有已知业务类型：

| 类型 | 说明 | 当前 Sprint | 预留 |
|------|------|-------------|------|
| `INBOUND` | 入库（采购/调拨入库） | ✅ 启用 | — |
| `OUTBOUND` | 出库（发货/调拨出库） | ✅ 启用 | — |
| `RESERVE` | 锁定（订单创建预占库存） | ✅ 启用 | — |
| `RELEASE` | 释放（取消订单释放库存） | ✅ 启用 | — |
| `DEDUCT` | 扣减（支付成功扣减库存） | ✅ 启用 | — |
| `ADJUST` | 调整（商家/管理员手动调整） | ✅ 启用 | — |
| `RETURN` | 退货入库（售后退货） | — | ✅ 预留 |
| `DAMAGE` | 报损（库存损坏报损出库） | — | ✅ 预留 |

### 理由

1. **避免模型变更**：后续 Sprint 开放在已有枚举上增加业务逻辑即可，不修改模型定义
2. **保证历史数据兼容**：当前写入的流水记录在未来仍可被正确解析
3. **领域完整性**：提前梳理完整的库存变更类型，确保架构设计不过度简化

---

## 决策 8：Inventory 不依赖 Order

### 决策

依赖方向为：**Order → Inventory**，不允许 **Inventory → Order**。

```
Product
   │
   ▼
Inventory
   ▲
   │
   Order
```

### 规则

| 规则 | 说明 |
|------|------|
| Inventory 不引用 Order Entity | 不建立 JPA 关联，不存 Order Repository |
| Order 通过 Application Service 调用 Inventory | OrderService 中注入 InventoryService 或 InventoryGateway |
| Inventory 返回结果通过事件通知 Order | InventoryReservedEvent → Order 监听处理 |
| 禁止循环依赖 | Order 和 Inventory 之间不存在双向引用 |

### 理由

1. **分层依赖**：库存是底层能力，订单是上层业务。上层依赖下层，下层不感知上层
2. **复用性**：Inventory 需要被 Cart、AI 等多个域调用，不应绑定 Order 的语义
3. **独立演进**：Order 域和 Inventory 域可独立修改和部署

### 跨域调用方式

```
OrderService.createOrder()
  └── InventoryService.reserveStock(skuId, quantity)  // 同步调用
      └── return success/failure
  └── 若成功则继续订单创建
  └── 若失败则回滚订单

OrderService.cancelOrder()
  └── InventoryService.releaseReservation(orderId)  // 同步调用
  └── 无论成功失败都继续取消流程
```

---

## 后续扩展方向

| 方向 | 说明 | 优先级 |
|------|------|--------|
| **多仓库支持** | Inventory 增加 warehouse_id，支持多仓库存管理 | 中 |
| **安全库存预警** | 低于安全库存阈值时自动通知商家补货 | 中 |
| **库存占用超时自动释放** | 定时任务扫描超时 Reservation 并释放 | 高 |
| **分仓库存分配策略** | 根据收货地址自动分配最优仓库 | 低 |
| **ERP 库存同步** | 对接外部 ERP 系统的库存变更 | 低 |
| **库存预留池** | 活动/秒杀场景下提前预留库存 | 中 |
| **库存快照** | 每日/每周库存快照用于数据分析和对账 | 低 |

---

> **文档版本:** v1.0  
> **最后更新:** 2026-07-26  
> **状态:** 已批准 — 仅设计文档，不创建 Entity/Repository/Service/Controller  
> **关联 ADR:** ADR-002（Product Domain 确认 Inventory 独立）