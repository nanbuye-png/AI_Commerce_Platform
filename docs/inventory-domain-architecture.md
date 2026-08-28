# Inventory Domain 架构设计

> **文档版本:** v1.0  
> **最后更新:** 2026-07-26  
> **状态:** 设计阶段  
> **对应 Sprint:** Sprint 10 Step 0 — Inventory Domain 架构设计

---

## 目录

1. [领域模型 (Domain Model)](#一领域模型-domain-model)
2. [Enterprise Inventory Design](#二enterprise-inventory-design)
3. [数据库设计 (Database Schema)](#三数据库设计-database-schema)
4. [聚合设计 (Aggregate Design)](#四聚合设计-aggregate-design)
5. [库存生命周期 (Inventory Lifecycle)](#五库存生命周期-inventory-lifecycle)
6. [API 规划 (API Planning)](#六api-规划-api-planning)
7. [事件规划 (Event Planning)](#七事件规划-event-planning)
8. [权限模型 (Permission Model)](#八权限模型-permission-model)
9. [与其他领域关系 (Domain Relationships)](#九与其他领域关系-domain-relationships)
10. [ADR 引用 (ADR Reference)](#十adr-引用-adr-reference)
11. [Sprint 10 后续计划](#十一sprint-10-后续计划)

---

## 一、领域模型 (Domain Model)

### 1.1 Inventory（库存）

库存核心实体，每行记录代表一个 SKU 的库存状态。

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 主键 |
| skuId | Long | 外键 → ProductSku(id)，唯一 |
| availableStock | Integer | 可售库存（current stock） |
| reservedStock | Integer | 已锁定库存（reserved stock） |
| totalStock | Integer | 总库存（total stock = available + reserved） |
| safetyStock | Integer | 安全库存阈值 |
| version | Integer | 乐观锁版本号 |
| createdTime | LocalDateTime | — |
| updatedTime | LocalDateTime | — |

**约束：**

- `sku_id` 全局唯一（一个 SKU 对应一条库存记录）
- `totalStock >= reservedStock >= 0`
- `availableStock = totalStock - reservedStock`
- 禁止可用库存为负数（通过 SQL CHECK 或应用层校验）

### 1.2 InventoryReservation（库存预占/锁定）

记录订单对库存的锁定信息。

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 主键 |
| inventoryId | Long | 外键 → Inventory(id) |
| skuId | Long | 外键 → ProductSku(id) |
| orderId | Long | 订单 ID |
| orderItemId | Long | 订单条目 ID |
| quantity | Integer | 锁定数量 |
| status | ReservationStatus | 预占状态 |
| expiredTime | LocalDateTime | 过期时间（超时自动释放） |
| createdTime | LocalDateTime | — |
| updatedTime | LocalDateTime | — |

**ReservationStatus 枚举：**

| 值 | 说明 |
|-----|------|
| ACTIVE | 锁定中（预占生效） |
| DEDUCTED | 已扣减（订单支付成功，库存已扣减） |
| RELEASED | 已释放（订单取消/支付失败/售后） |
| EXPIRED | 已过期（超时未支付自动释放） |

**约束：**

- 一条 Reservation 记录一个订单中的一个 SKU 锁定信息
- 支持一个订单多个 SKU 对应多条 Reservation
- 支持全量释放、部分释放

### 1.3 InventoryMovement（库存流水）

库存变动的审计日志，采用 Append-Only 模式。

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 主键 |
| skuId | Long | 外键 → ProductSku(id) |
| inventoryId | Long | 外键 → Inventory(id) |
| movementType | MovementType | 变动类型 |
| quantity | Integer | 变动数量（正数=增加，负数=减少） |
| beforeAvailable | Integer | 变动前可售库存 |
| afterAvailable | Integer | 变动后可售库存 |
| beforeReserved | Integer | 变动前已锁定库存 |
| afterReserved | Integer | 变动后已锁定库存 |
| beforeTotal | Integer | 变动前总库存 |
| afterTotal | Integer | 变动后总库存 |
| businessId | String | 业务单号（订单号/入库单号等） |
| operatorId | Long | 操作人 ID |
| remark | String | 备注 |
| createdTime | LocalDateTime | — |

**MovementType 枚举：**

| 值 | 说明 | Sprint 10 | 预留 |
|-----|------|-----------|------|
| INBOUND | 入库（采购/调拨） | ✅ | — |
| OUTBOUND | 出库（发货/调拨出库） | ✅ | — |
| RESERVE | 锁定（订单预占） | ✅ | — |
| RELEASE | 释放（取消订单） | ✅ | — |
| DEDUCT | 扣减（支付成功） | ✅ | — |
| ADJUST | 调整（手动修正） | ✅ | — |
| RETURN | 退货入库 | — | ✅ |
| DAMAGE | 报损 | — | ✅ |

---

## 二、Enterprise Inventory Design

### 2.1 库存三字段模型

Inventory 必须采用三字段模型替代传统的单字段模型。

#### 三字段定义

| 字段 | 名称 | 含义 | 说明 |
|------|------|------|------|
| `availableStock` | 可售库存 | 当前可销售给客户的库存数量 | 顾客下单时以此为准 |
| `reservedStock` | 已锁定库存 | 已被订单预占但尚未支付的库存数量 | 订单创建时锁定 |
| `totalStock` | 总库存 | 仓库中该 SKU 的总实物库存 | `totalStock = availableStock + reservedStock` |

#### 为什么不是单字段

| 方案 | 描述 | 问题 |
|------|------|------|
| ❌ 单字段 `stock` | 只有一个库存数量 | 无法区分已下单未支付的库存和可售库存 |
| ❌ 两字段 `stock + locked` | 库存 + 锁定数 | 需要通过计算 `stock - locked` 得到可售，存在并发负库存风险 |
| ✅ 三字段 | 可售 + 锁定 + 总库存 | 语义清晰，约束自检，支持复杂场景 |

#### 操作原子性保证

所有库存变更操作必须在同一事务中完成三字段的原子更新：

```sql
-- 锁定库存（订单创建）
UPDATE inventory
SET available_stock = available_stock - #{quantity},
    reserved_stock = reserved_stock + #{quantity},
    version = version + 1
WHERE sku_id = #{skuId}
  AND available_stock >= #{quantity}
  AND version = #{version};
```

```sql
-- 扣减库存（支付成功）
UPDATE inventory
SET reserved_stock = reserved_stock - #{quantity},
    total_stock = total_stock - #{quantity},
    version = version + 1
WHERE sku_id = #{skuId}
  AND reserved_stock >= #{quantity}
  AND version = #{version};
```

#### 并发安全

- 所有更新操作通过 **乐观锁（version 字段）** 保证并发安全
- 更新条件中包含库存数量校验（如 `available_stock >= quantity`），利用数据库行锁保证原子性
- 更新行数 = 0 代表并发冲突或库存不足，需重试或报错

### 2.2 InventoryMovement 审计策略

#### Append-Only 模式

InventoryMovement 表遵守以下规则：

| 操作 | 是否允许 | 原因 |
|------|----------|------|
| INSERT | ✅ | 每次库存变动均新增流水记录 |
| UPDATE | ❌ | 禁止修改已写入的流水记录 |
| DELETE（物理） | ❌ | 流水永久保留，不可删除 |
| DELETE（软删除） | ❌ | 不设 deleted 字段 |

#### 设计理由

1. **审计合规**：库存变动是财务审计的核心数据，需要完整、不可篡改的变更历史
2. **对账依据**：当订单、支付、库存三方对账不一致时，流水表是唯一的仲裁依据
3. **问题追溯**：线上问题排查时，流水表可还原任意时刻的库存状态

#### 查询策略

- 流水表数据量大，需要按时间范围 + SKU ID 建立复合索引
- 定期归档（如按季度）旧数据到归档表或大数据平台
- 业务查询限制时间范围（如最多查询 3 个月）

### 2.3 InventoryReservation 独立模型

#### 为什么独立

见 ADR-0003 决策 3。核心要点：

- 一个订单多个 SKU → 多条 Reservation
- 定时任务超时释放 → 需要独立的过期时间字段
- 部分退款/售后释放 → 需要针对单条 Reservation 精确操作
- 支付扣减 → 需要将 Reservation 状态从 ACTIVE → DEDUCTED

#### 设计要点

```
InventoryReservation
├── inventory_id  → 库存记录 ID
├── sku_id        → SKU ID
├── order_id      → 订单 ID
├── order_item_id → 订单条目 ID
├── quantity      → 锁定数量
├── status        → ACTIVE | DEDUCTED | RELEASED | EXPIRED
├── expired_time  → 过期时间（created_time + 支付超时时间）
└── created_time  → 创建时间
```

#### 生命周期

```
创建订单 → RESERVE → [ACTIVE]
  ├── 支付成功 → DEDUCT → [DEDUCTED]
  ├── 取消订单 → RELEASE → [RELEASED]
  ├── 超时未支付 → 定时任务 → [EXPIRED]
  └── 售后部分退款 → 部分 RELEASE → [RELEASED]
```

### 2.4 MovementType 完整预留

见 ADR-0003 决策 7。已预留 8 种 MovementType，当前 Sprint 启用 6 种，预留 2 种。

### 2.5 Domain Dependency

```
Product Domain (Sprint 9)
    │
    │ sku_id (弱引用，仅存 ID)
    ▼
Inventory Domain (Sprint 10)
    ▲
    │ 通过 Application Service 或 Domain Event 调用
    │
Order Domain (Sprint 11+)
```

**依赖规则：**

| 方向 | 允许 | 说明 |
|------|------|------|
| Inventory → Product | ✅ | 通过 sku_id 弱引用，不依赖 Product Entity |
| Inventory → Order | ❌ | Inventory 不感知 Order 的存在 |
| Order → Inventory | ✅ | OrderService 调用 InventoryService |
| Product → Inventory | ✅ | ProductService 创建 Sku 后通过事件初始化库存 |

### 2.6 API Version Strategy

Inventory API 路径策略与 Product Domain 保持一致：

| 角色 | 路径 | 说明 |
|------|------|------|
| Merchant | `/api/merchant/inventory/**` | 商家库存管理 |
| Customer | `/api/inventory/**` | 客户库存查询（仅是否可售） |
| Admin | `/api/admin/inventory/**` | 平台库存管控 |

**版本策略：**

- 当前不使用 `/api/v1/` 前缀
- 采用角色前缀而非版本号前缀
- 仅在出现重大兼容性变更时升级为 `/api/v2/**`（如数据库分库分表、跨服务协议变更）

---

## 三、数据库设计 (Database Schema)

### 3.1 `inventory` — 库存表

| 列名 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 主键 |
| sku_id | BIGINT | UNIQUE, NOT NULL | → product_sku(id) |
| available_stock | INT | NOT NULL, DEFAULT 0 | 可售库存 |
| reserved_stock | INT | NOT NULL, DEFAULT 0 | 已锁定库存 |
| total_stock | INT | NOT NULL, DEFAULT 0 | 总库存 |
| safety_stock | INT | NOT NULL, DEFAULT 0 | 安全库存阈值 |
| version | INT | NOT NULL, DEFAULT 0 | 乐观锁版本号 |
| created_time | DATETIME | NOT NULL | — |
| updated_time | DATETIME | NOT NULL | — |

**索引：**

| 索引名 | 列 | 类型 |
|--------|-----|------|
| uk_sku_id | sku_id | 唯一索引 |
| idx_safety_stock | (safety_stock, available_stock) | 复合索引（库存预警查询） |

**约束：**

- `CHECK (total_stock >= 0 AND reserved_stock >= 0 AND available_stock >= 0)`
- `CHECK (total_stock = available_stock + reserved_stock)` — 应用层/触发器保证

### 3.2 `inventory_reservation` — 库存预占表

| 列名 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 主键 |
| inventory_id | BIGINT | NOT NULL | → inventory(id) |
| sku_id | BIGINT | NOT NULL | → product_sku(id) |
| order_id | BIGINT | NOT NULL | 订单 ID |
| order_item_id | BIGINT | NOT NULL | 订单条目 ID |
| quantity | INT | NOT NULL | 锁定数量 |
| status | VARCHAR(20) | NOT NULL | ACTIVE / DEDUCTED / RELEASED / EXPIRED |
| expired_time | DATETIME | NOT NULL | 过期时间 |
| created_time | DATETIME | NOT NULL | — |
| updated_time | DATETIME | NOT NULL | — |

**索引：**

| 索引名 | 列 | 类型 |
|--------|-----|------|
| idx_inventory_id | inventory_id | 普通索引 |
| idx_sku_id | sku_id | 普通索引 |
| idx_order_id | order_id | 普通索引 |
| idx_status_expired | (status, expired_time) | 复合索引（超时释放扫描） |
| idx_created_time | created_time | 普通索引 |

**约束：**

- `FK inventory_id → inventory(id)`，级联删除不设置（库存记录不可删除）
- `CHECK (quantity > 0)`
- `CHECK (expired_time > created_time)`

### 3.3 `inventory_movement` — 库存流水表

| 列名 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 主键 |
| sku_id | BIGINT | NOT NULL | → product_sku(id) |
| inventory_id | BIGINT | NOT NULL | → inventory(id) |
| movement_type | VARCHAR(20) | NOT NULL | 变动类型 |
| quantity | INT | NOT NULL | 变动数量 |
| before_available | INT | NOT NULL | 变动前可售库存 |
| after_available | INT | NOT NULL | 变动后可售库存 |
| before_reserved | INT | NOT NULL | 变动前已锁定库存 |
| after_reserved | INT | NOT NULL | 变动后已锁定库存 |
| before_total | INT | NOT NULL | 变动前总库存 |
| after_total | INT | NOT NULL | 变动后总库存 |
| business_id | VARCHAR(64) | — | 业务单号 |
| operator_id | BIGINT | — | 操作人 ID |
| remark | VARCHAR(256) | — | 备注 |
| created_time | DATETIME | NOT NULL | — |

**索引：**

| 索引名 | 列 | 类型 |
|--------|-----|------|
| idx_sku_id | sku_id | 普通索引 |
| idx_inventory_id | inventory_id | 普通索引 |
| idx_movement_type | movement_type | 普通索引 |
| idx_business_id | business_id | 普通索引 |
| idx_sku_created | (sku_id, created_time) | 复合索引（按 SKU 查流水） |
| idx_created_time | created_time | 普通索引 |

**约束：**

- Append-Only：仅 INSERT，不 UPDATE/DELETE
- 无 deleted 字段（软删除不适用）
- `CHECK (quantity != 0)`

### 3.4 外键关系总图

```
product_sku (Product Domain)
    │
    │ 1:1
    ▼
inventory
    │
    ├── 1:N → inventory_reservation (inventory_id)
    │        ↑ order_id (仅存 ID，无外键约束)
    │
    └── 1:N → inventory_movement (inventory_id)
```

### 3.5 Flyway Migration 规划

```sql
-- V3__create_inventory_tables.sql

CREATE TABLE inventory (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    sku_id BIGINT NOT NULL,
    available_stock INT NOT NULL DEFAULT 0,
    reserved_stock INT NOT NULL DEFAULT 0,
    total_stock INT NOT NULL DEFAULT 0,
    safety_stock INT NOT NULL DEFAULT 0,
    version INT NOT NULL DEFAULT 0,
    created_time DATETIME NOT NULL,
    updated_time DATETIME NOT NULL,
    UNIQUE KEY uk_sku_id (sku_id),
    INDEX idx_safety_stock (safety_stock, available_stock)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE inventory_reservation (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    inventory_id BIGINT NOT NULL,
    sku_id BIGINT NOT NULL,
    order_id BIGINT NOT NULL,
    order_item_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    status VARCHAR(20) NOT NULL,
    expired_time DATETIME NOT NULL,
    created_time DATETIME NOT NULL,
    updated_time DATETIME NOT NULL,
    INDEX idx_inventory_id (inventory_id),
    INDEX idx_sku_id (sku_id),
    INDEX idx_order_id (order_id),
    INDEX idx_status_expired (status, expired_time),
    INDEX idx_created_time (created_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE inventory_movement (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    sku_id BIGINT NOT NULL,
    inventory_id BIGINT NOT NULL,
    movement_type VARCHAR(20) NOT NULL,
    quantity INT NOT NULL,
    before_available INT NOT NULL,
    after_available INT NOT NULL,
    before_reserved INT NOT NULL,
    after_reserved INT NOT NULL,
    before_total INT NOT NULL,
    after_total INT NOT NULL,
    business_id VARCHAR(64),
    operator_id BIGINT,
    remark VARCHAR(256),
    created_time DATETIME NOT NULL,
    INDEX idx_sku_id (sku_id),
    INDEX idx_inventory_id (inventory_id),
    INDEX idx_movement_type (movement_type),
    INDEX idx_business_id (business_id),
    INDEX idx_sku_created (sku_id, created_time),
    INDEX idx_created_time (created_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

---

## 四、聚合设计 (Aggregate Design)

### 4.1 聚合根确定

| 聚合根 | 子对象 | 一致性边界 | 说明 |
|--------|--------|------------|------|
| **Inventory** | InventoryReservation | 弱一致性 | Reservation 通过 Repository 独立管理 |
| **Inventory** | InventoryMovement | 最终一致性 | Movement 通过 Append-Only 追加，不参与业务事务 |
| — | InventoryReservation | 独立聚合 | 跨聚合通过 InventoryService 协调 |
| — | InventoryMovement | 独立聚合 | 仅追加，不修改 |

### 4.2 聚合边界

```
Inventory (Aggregate Root)
│
├── InventoryReservation
│   ├── 同一事务: 锁库存 + 创建 Reservation
│   ├── 同一事务: 扣减库存 + 更新 Reservation → DEDUCTED
│   └── 同一事务: 释放库存 + 更新 Reservation → RELEASED
│
└── InventoryMovement
    └── 事务后追加: 流水记录不参与库存变更的原子性
    └── 通过领域事件异步写入（或同一事务内写入）
```

### 4.3 聚合一致性策略

| 操作 | 一致性要求 | 策略 |
|------|-----------|------|
| 入库 | 强一致 | Inventory INSERT/UPDATE + Movement INSERT 同一事务 |
| 锁定库存 | 强一致 | Inventory UPDATE + Reservation INSERT + Movement INSERT 同一事务 |
| 释放库存 | 强一致 | Inventory UPDATE + Reservation UPDATE + Movement INSERT 同一事务 |
| 扣减库存 | 强一致 | Inventory UPDATE + Reservation UPDATE + Movement INSERT 同一事务 |
| 超时释放 | 最终一致 | 定时任务扫描 + 小批量处理 + 失败重试 |

### 4.4 领域事件清单

| 事件 | 触发时机 | 一致性 | 发布方式 |
|------|----------|--------|----------|
| `InventoryReservedEvent` | 库存锁定成功 | 事务后 | 同步 ApplicationEvent + MQ |
| `InventoryReleasedEvent` | 库存释放完成 | 事务后 | 同步 ApplicationEvent + MQ |
| `InventoryDeductedEvent` | 库存扣减完成 | 事务后 | 同步 ApplicationEvent + MQ |
| `InventoryAdjustedEvent` | 手动调整库存 | 事务后 | 应用内事件 |
| `InventoryLowStockEvent` | 低于安全库存 | 定时检测 | MQ |

### 4.5 聚合间通信

```
InventoryService
    │
    ├── direct call → InventoryRepository（本聚合）
    ├── direct call → InventoryReservationRepository（本聚合）
    └── publish → InventoryMovementRepository（本聚合，事务后追加）

跨域通信：
    │
    ├── InventoryReservedEvent → OrderService.onReserved()
    ├── InventoryReleasedEvent → OrderService.onReleased()
    └── InventoryDeductedEvent → OrderService.onDeducted()
```

---

## 五、库存生命周期 (Inventory Lifecycle)

### 5.1 库存状态流转图

```
                        ┌─────────────────┐
                        │  Merchant 入库    │
                        │  (INBOUND)       │
                        └────────┬────────┘
                                 │
                                 ▼
                    ┌──────────────────────┐
                    │    Available Stock    │  ← totalStock = available + reserved
                    │    (可售库存)          │
                    └────────┬─────────────┘
                             │
                    ┌────────┴────────┐
                    │                │
                    ▼                ▼
         ┌────────────────┐  ┌────────────────┐
         │ Customer 下单   │  │ Admin 调整      │
         │ (RESERVE)      │  │ (ADJUST)        │
         └───────┬────────┘  └────────────────┘
                 │
                 ▼
        ┌────────────────────┐
        │   Reserved Stock    │
        │   (已锁定库存)       │
        └──┬──────────────┬──┘
           │              │
    ┌──────┴──────┐  ┌───┴────────┐
    │              │  │            │
    ▼              ▼  │            │
┌──────────┐ ┌────────┐│            │
│ 支付成功  │ │取消订单││            │
│ (DEDUCT) │ │RELEASE ││            │
└────┬─────┘ └───┬────┘│            │
     │           │     │            │
     ▼           ▼     │            │
┌──────────┐ ┌────────┐│            │
│ 发货      │ │库存恢复││            │
│ (OUTBOUND)│ │可用↑   ││            │
└──────────┘ └────────┘│            │
                       │            │
                       ▼            ▼
              ┌────────────┐  ┌────────────┐
              │ 超时释放    │  │ 售后释放    │
              │ (EXPIRED)  │  │ (RETURN)   │
              └─────┬──────┘  └─────┬──────┘
                    │               │
                    └───────┬───────┘
                            ▼
                   ┌────────────────┐
                   │  Available ↑   │
                   │  Reserved ↓    │
                   └────────────────┘
```

### 5.2 正向流程（下单 → 发货）

```
Merchant 入库
   │
   │ inventory.available_stock += qty
   │ inventory.total_stock += qty
   │ movement: INBOUND
   ▼
[Available Stock]

Customer 下单
   │
   │ inventory.available_stock -= qty
   │ inventory.reserved_stock += qty
   │ reservation: INSERT (status=ACTIVE)
   │ movement: RESERVE
   ▼
[Reserved Stock]

订单支付
   │
   │ inventory.reserved_stock -= qty
   │ inventory.total_stock -= qty
   │ reservation: UPDATE status=DEDUCTED
   │ movement: DEDUCT
   ▼
[Deducted]

发货
   │
   │ movement: OUTBOUND（出库记录）
   ▼
[完成]
```

### 5.3 逆向流程（取消订单）

```
取消订单（支付前）
   │
   │ inventory.reserved_stock -= qty
   │ inventory.available_stock += qty
   │ reservation: UPDATE status=RELEASED
   │ movement: RELEASE
   ▼
[库存恢复，Available Stock ↑]

取消订单（支付后）
   │
   │ inventory.total_stock += qty  (回滚扣减)
   │ inventory.available_stock += qty
   │ reservation: UPDATE status=RELEASED
   │ movement: RELEASE
   ▼
[库存恢复，Total Stock ↑]
```

### 5.4 超时自动释放流程

```
定时任务（每分钟执行）
   │
   │ SELECT * FROM inventory_reservation
   │ WHERE status='ACTIVE' AND expired_time < NOW()
   │ FOR UPDATE (小批量分页)
   │
   │ FOR EACH expired reservation:
   │   inventory.reserved_stock -= qty
   │   inventory.available_stock += qty
   │   reservation: UPDATE status=EXPIRED
   │   movement: RELEASE
   │   publish: InventoryReleasedEvent
   ▼
[库存释放完成]
```

---

## 六、API 规划 (API Planning)

### 6.1 API 分组总览

| 分组 | 基础路径 | 角色 | 说明 |
|------|----------|------|------|
| Customer | `/api/inventory` | USER / 公开 | 仅查看是否有货，不可查看库存数量 |
| Merchant | `/api/merchant/inventory` | MERCHANT | 商家库存管理（查询、调整、流水） |
| Admin | `/api/admin/inventory` | ADMIN | 平台库存管控（总览、调整、流水） |

### 6.2 Customer 端 API

**基础路径：** `/api/inventory`

#### GET /api/inventory/skus/{skuId}/availability — SKU 是否有货

- **权限：** 公开
- **功能：** 查询指定 SKU 是否可售（库存 > 0）
- **响应：**

```json
{
  "code": 0,
  "data": {
    "sku_id": 3001,
    "available": true
  }
}
```

#### POST /api/inventory/batch-availability — 批量查询是否有货

- **权限：** 公开
- **功能：** 批量查询多个 SKU 是否有货
- **请求：**

```json
{
  "sku_ids": [3001, 3002, 3003]
}
```

- **响应：**

```json
{
  "code": 0,
  "data": {
    "3001": true,
    "3002": false,
    "3003": true
  }
}
```

**安全约束：**

- ❌ 不返回可用库存数量
- ❌ 不返回总库存
- ✅ 仅返回 boolean 类型的是否可售

### 6.3 Merchant 端 API

**基础路径：** `/api/merchant/inventory`

#### GET /api/merchant/inventory/skus/{skuId} — 查询 SKU 库存

- **权限：** MERCHANT
- **功能：** 查询本店铺指定 SKU 的库存详情
- **响应：**

```json
{
  "code": 0,
  "data": {
    "sku_id": 3001,
    "sku_code": "SKU2026001",
    "available_stock": 50,
    "reserved_stock": 10,
    "total_stock": 60,
    "safety_stock": 20
  }
}
```

#### GET /api/merchant/inventory — 库存列表（分页）

- **权限：** MERCHANT
- **功能：** 查询本店铺所有 SKU 的库存概览
- **参数：** 分页 + sku_code/product_name 搜索 + 库存预警筛选

#### POST /api/merchant/inventory/inbound — 入库

- **权限：** MERCHANT
- **功能：** 商家入库操作
- **请求：**

```json
{
  "sku_id": 3001,
  "quantity": 100,
  "remark": "采购入库"
}
```

#### POST /api/merchant/inventory/adjust — 库存调整

- **权限：** MERCHANT
- **功能：** 商家手动调整库存（盘盈/盘亏）
- **请求：**

```json
{
  "sku_id": 3001,
  "adjust_type": "INCREASE",
  "quantity": 10,
  "remark": "盘点调整"
}
```

#### GET /api/merchant/inventory/movements — 库存流水查询

- **权限：** MERCHANT
- **功能：** 查询库存变动流水
- **参数：** sku_id、movement_type、时间范围、分页

### 6.4 Admin 端 API

**基础路径：** `/api/admin/inventory`

#### GET /api/admin/inventory — 库存总览（全平台）

- **权限：** ADMIN
- **功能：** 全平台所有商家 SKU 库存列表
- **参数：** merchant_id、sku_code、库存状态筛选

#### GET /api/admin/inventory/skus/{skuId} — 库存详情（管理员视角）

- **权限：** ADMIN
- **额外字段：** 含商家信息、商品名称、sku_code

#### POST /api/admin/inventory/adjust — 平台强制调整库存

- **权限：** ADMIN
- **功能：** 平台管理员强制调整任何 SKU 的库存
- **限制：** 高危操作，需记录操作日志和操作人

#### GET /api/admin/inventory/movements — 全平台库存流水

- **权限：** ADMIN
- **功能：** 全平台所有库存变动流水查询

#### GET /api/admin/inventory/alerts — 库存预警列表

- **权限：** ADMIN
- **功能：** 查看所有低于安全库存阈值的 SKU

### 6.5 内部 API（服务间调用）

这些接口不对外暴露，仅限 Order Service 等内部服务通过 Feign/RPC 调用。

#### POST /api/internal/inventory/reserve — 锁定库存

```json
{
  "order_id": 10001,
  "items": [
    { "sku_id": 3001, "quantity": 2 },
    { "sku_id": 3002, "quantity": 1 }
  ]
}
```

#### POST /api/internal/inventory/release — 释放库存

```json
{
  "order_id": 10001,
  "items": [
    { "sku_id": 3001, "quantity": 2 }
  ]
}
```

#### POST /api/internal/inventory/deduct — 扣减库存

```json
{
  "order_id": 10001,
  "items": [
    { "sku_id": 3001, "quantity": 2 }
  ]
}
```

---

## 七、事件规划 (Event Planning)

### 7.1 领域事件总览

| 事件 | 触发时机 | 内容 | 同步/异步 | 目标 |
|------|----------|------|-----------|------|
| `InventoryReservedEvent` | 锁定库存成功后 | orderId, skuId, quantity | 同步 ApplicationEvent → 异步 MQ | Order：确认库存已锁定 |
| `InventoryReleasedEvent` | 释放库存完成后 | orderId, skuId, quantity, reason | 同步 ApplicationEvent → 异步 MQ | Order：确认库存已释放 |
| `InventoryDeductedEvent` | 扣减库存完成后 | orderId, skuId, quantity | 同步 ApplicationEvent → 异步 MQ | Order：可进入发货流程 |
| `InventoryAdjustedEvent` | 手动调整库存后 | skuId, adjustType, quantity, operator | 应用内事件 | Audit：记录审计日志 |
| `InventoryLowStockEvent` | 可售库存低于安全阈值 | skuId, availableStock, safetyStock | 异步 MQ | Merchant：补货通知 |

### 7.2 事件发送策略

| 事件 | 发送方式 | 说明 |
|------|----------|------|
| `InventoryReservedEvent` | 同步 → MQ | 同步 ApplicationEvent 用于事务内回调；MQ 用于跨服务最终一致 |
| `InventoryReleasedEvent` | 同步 → MQ | 同上 |
| `InventoryDeductedEvent` | 同步 → MQ | 同上 |
| `InventoryAdjustedEvent` | 应用内同步 | 无需跨服务通知，仅记录审计日志 |
| `InventoryLowStockEvent` | 异步 MQ | 非关键路径，完全异步通知商家 |

### 7.3 事件数据结构

```java
// InventoryReservedEvent
{
  "eventId": "UUID",
  "eventType": "INVENTORY_RESERVED",
  "timestamp": "2026-07-26T10:00:00",
  "data": {
    "orderId": 10001,
    "orderItemId": 1000101,
    "skuId": 3001,
    "quantity": 2,
    "reservationId": 5001
  }
}

// InventoryReleasedEvent
{
  "eventId": "UUID",
  "eventType": "INVENTORY_RELEASED",
  "timestamp": "2026-07-26T10:30:00",
  "data": {
    "orderId": 10001,
    "skuId": 3001,
    "quantity": 2,
    "reservationId": 5001,
    "reason": "ORDER_CANCELLED"
  }
}

// InventoryDeductedEvent
{
  "eventId": "UUID",
  "eventType": "INVENTORY_DEDUCTED",
  "timestamp": "2026-07-26T10:05:00",
  "data": {
    "orderId": 10001,
    "skuId": 3001,
    "quantity": 2,
    "reservationId": 5001
  }
}
```

### 7.4 事件订阅方规划

| 事件 | 当前订阅方 | 未来可能订阅方 |
|------|-----------|---------------|
| `InventoryReservedEvent` | Order Domain | — |
| `InventoryReleasedEvent` | Order Domain | — |
| `InventoryDeductedEvent` | Order Domain, Search Domain | 物流域 |
| `InventoryAdjustedEvent` | Audit | — |
| `InventoryLowStockEvent` | Merchant Notification | — |

---

## 八、权限模型 (Permission Model)

### 8.1 角色定义

| 角色编码 | 角色名 | 说明 |
|----------|--------|------|
| USER | C 端用户 | 普通消费者 |
| MERCHANT | 商家 | 入驻商家员工 |
| ADMIN | 平台管理员 | 平台运营人员 |
| ANONYMOUS | 未登录用户 | 游客（仅可访问公开接口） |

### 8.2 库存模块权限矩阵

| 功能 | API | ANONYMOUS | USER | MERCHANT | ADMIN |
|------|-----|-----------|------|----------|-------|
| **客户查询** | | | | | |
| 单品是否有货 | `GET /inventory/skus/{skuId}/availability` | ✅ | ✅ | ✅ | ✅ |
| 批量是否有货 | `POST /inventory/batch-availability` | ✅ | ✅ | ✅ | ✅ |
| **商家库存管理** | | | | | |
| 查询 SKU 库存详情 | `GET /merchant/inventory/skus/{skuId}` | ❌ | ❌ | ✅ (仅本店) | ✅ |
| 库存列表 | `GET /merchant/inventory` | ❌ | ❌ | ✅ (仅本店) | ✅ |
| 入库 | `POST /merchant/inventory/inbound` | ❌ | ❌ | ✅ (仅本店) | ✅ |
| 库存调整 | `POST /merchant/inventory/adjust` | ❌ | ❌ | ✅ (仅本店) | ✅ |
| 库存流水 | `GET /merchant/inventory/movements` | ❌ | ❌ | ✅ (仅本店) | ✅ |
| **平台库存管控** | | | | | |
| 全平台库存总览 | `GET /admin/inventory` | ❌ | ❌ | ❌ | ✅ |
| 库存详情（管理员） | `GET /admin/inventory/skus/{skuId}` | ❌ | ❌ | ❌ | ✅ |
| 强制调整库存 | `POST /admin/inventory/adjust` | ❌ | ❌ | ❌ | ✅ (高危) |
| 全平台库存流水 | `GET /admin/inventory/movements` | ❌ | ❌ | ❌ | ✅ |
| 库存预警列表 | `GET /admin/inventory/alerts` | ❌ | ❌ | ❌ | ✅ |

### 8.3 数据权限（行级过滤）

| 角色 | 数据可见范围 |
|------|-------------|
| USER | 仅库存 availability（是否有货） |
| MERCHANT | 本店铺所有 SKU 的库存详情（可售 + 锁定 + 总库存） |
| ADMIN | 全平台所有 SKU 的库存详情 |

### 8.4 接口调用时序（下单场景）

```
Customer
   │
   │ POST /api/orders（创建订单）
   ▼
OrderService.createOrder()
   │
   │ InventoryService.reserveStock(skuId, qty)  ← 内部 RPC
   ▼
InventoryService
   │
   │ 1. 校验库存（available >= qty）
   │ 2. 乐观锁更新 inventory
   │ 3. INSERT inventory_reservation
   │ 4. INSERT inventory_movement (RESERVE)
   │ 5. publish InventoryReservedEvent
   │
   ▼
   return success/failure

OrderService
   │
   │ 若 success → 继续创建订单
   │ 若 failure → 回滚订单（提示库存不足）
   ▼
Customer 收到结果
```

---

## 九、与其他领域关系 (Domain Relationships)

### 9.1 领域依赖总图

```
┌──────────────┐     ┌──────────────┐     ┌──────────────┐
│   Product    │────>│  Inventory   │<────│    Order     │
│   Domain     │     │   Domain     │     │   Domain     │
│  (Sprint 9)  │     │ (Sprint 10)  │     │ (Sprint 11+) │
│              │     │              │     │              │
│  ProductSku  │     │  Inventory   │     │  Order       │
│  .id  ───────┼──>  │  .sku_id     │     │  .order_id──>│
│              │     │              │     │  (Reserv.) │
└──────────────┘     └──────┬───────┘     └──────────────┘
                            │
                            ▼
                    ┌───────────────┐
                    │    Cart       │
                    │   Domain      │
                    │ (Sprint 11+)  │
                    └───────────────┘
                            │
                            ▼
                    ┌───────────────┐
                    │    Search     │
                    │   Domain      │
                    │ (Sprint 12+)  │
                    └───────────────┘
```

### 9.2 与 Product Domain 的关系

| 方面 | 说明 |
|------|------|
| **依赖方向** | Product → Inventory（ProductSku 创建后初始化库存） |
| **引用方式** | Inventory 通过 `sku_id` 弱引用 ProductSku，不持有 JPA Entity 关联 |
| **生命周期** | ProductSku 创建 → 事件驱动 → Inventory 创建；ProductSku 删除 → 不删除 Inventory |
| **事务边界** | 各自独立事务。Product 的 Cascade Save 不影响 Inventory |
| **查询协作** | 查询商品详情时，CustomerProductService 通过 InventoryService 获取库存状态 |

### 9.3 与 Order Domain 的关系

| 方面 | 说明 |
|------|------|
| **依赖方向** | Order → Inventory（订单创建时调用 Inventory 锁定库存） |
| **调用方式** | OrderService 通过 InventoryService 接口同步调用 |
| **结果反馈** | Inventory 通过 Domain Event（Reserved/Released/Deducted）通知 Order |
| **禁止** | ✅ Order 调用 Inventory；❌ Inventory 调用 Order |
| **循环依赖** | 禁止形成双向依赖 |

### 9.4 与 Cart Domain 的关系

| 方面 | 说明 |
|------|------|
| **依赖方向** | Cart → Inventory（购物车展示 SKU 是否有货） |
| **实时性** | 购物车中 SKU 的库存状态需要实时查询（至少秒级） |
| **缓存策略** | 可售库存可缓存（Redis），锁定库存实时查询 |

### 9.5 与 Payment Domain 的关系

| 方面 | 说明 |
|------|------|
| **依赖方向** | Payment → Inventory（支付成功后触发库存扣减） |
| **调用方式** | PaymentService 支付成功 → 事件 → InventoryService.deductStock() |
| **事务一致性** | 支付 + 扣库存不在同一事务中。支付成功 → MQ → 扣库存（最终一致） |

### 9.6 与 Search Domain 的关系

| 方面 | 说明 |
|------|------|
| **依赖方向** | Search → Inventory（搜索需要展示库存状态） |
| **索引同步** | InventoryDeductedEvent → Search Domain 更新 ES 索引中的库存状态 |
| **降级策略** | ES 索引中库存字段不作为排序/筛选条件，仅作展示（避免 ES 与 MySQL 库存不一致导致错误） |

---

## 十、ADR 引用 (ADR Reference)

| 编号 | 决策 | 理由 | 链接 |
|------|------|------|------|
| ADR-003 | Inventory 独立成域 | 高并发隔离、独立生命周期、领域自治 | [ADR-0003](./adr/ADR-0003-inventory-domain.md) |
| ADR-003.1 | 不将库存放入 Product 域 | 分离关注点、性能隔离、业务语义差异 | [ADR-0003-决策2](./adr/ADR-0003-inventory-domain.md#决策-2不将库存放入-product-域) |
| ADR-003.2 | 独立设计 InventoryReservation | 支持多 SKU 订单、超时释放、售后释放 | [ADR-0003-决策3](./adr/ADR-0003-inventory-domain.md#决策-3独立设计-inventoryreservation) |
| ADR-003.3 | 采用事件驱动 | 解耦、异步化、可追溯、扩展性 | [ADR-0003-决策4](./adr/ADR-0003-inventory-domain.md#决策-4采用事件驱动) |
| ADR-003.4 | 库存三字段模型 | 语义清晰、并发安全、支持复杂场景 | [ADR-0003-决策5](./adr/ADR-0003-inventory-domain.md#决策-5库存三字段模型) |
| ADR-003.5 | InventoryMovement Append-Only | 审计合规、数据完整性 | [ADR-0003-决策6](./adr/ADR-0003-inventory-domain.md#决策-6inventorymovement-append-only) |
| ADR-003.6 | MovementType 完整预留 | 避免模型变更、保证兼容性 | [ADR-0003-决策7](./adr/ADR-0003-inventory-domain.md#决策-7movementtype-完整预留) |
| ADR-003.7 | Inventory 不依赖 Order | 分层依赖、复用性、独立演进 | [ADR-0003-决策8](./adr/ADR-0003-inventory-domain.md#决策-8inventory-不依赖-order) |

**关联 ADR：**

| 编号 | 关联 | 说明 |
|------|------|------|
| ADR-002（Product Domain） | 强关联 | Product Domain 确认 Inventory 独立为域，不纳入商品聚合 |
| ADR-001（Product Domain） | 弱关联 | Product 聚合根设计，Inventory 通过事件响应 ProductSku 创建 |

---

## 十一、Sprint 10 后续计划

### 11.1 当前 Sprint（Sprint 10 Step 0）

| 任务 | 状态 | 产出 |
|------|------|------|
| 领域模型设计 | ✅ 完成 | 本文档 §1 |
| Enterprise Inventory Design | ✅ 完成 | 本文档 §2 |
| 数据库设计 | ✅ 完成 | 本文档 §3 |
| 聚合设计 | ✅ 完成 | 本文档 §4 |
| 库存生命周期 | ✅ 完成 | 本文档 §5 |
| API 规划 | ✅ 完成 | 本文档 §6 |
| 事件规划 | ✅ 完成 | 本文档 §7 |
| 权限模型 | ✅ 完成 | 本文档 §8 |
| 与其他领域关系 | ✅ 完成 | 本文档 §9 |
| ADR-0003 | ✅ 完成 | [ADR-0003](./adr/ADR-0003-inventory-domain.md) |

### 11.2 Sprint 10 Step 1 — 代码实现

| 任务 | 说明 |
|------|------|
| Flyway Migration | 创建 V3__create_inventory_tables.sql |
| Entity 创建 | Inventory / InventoryReservation / InventoryMovement |
| Enum 创建 | MovementType / ReservationStatus |
| Repository 创建 | InventoryRepository / InventoryReservationRepository / InventoryMovementRepository |
| Service 创建 | InventoryService（库存核心操作） |
| 库存初始化 | SkuCreatedEvent 监听器 → Inventory initializeStock() |

### 11.3 Sprint 10 Step 2 — Merchant 库存 API

| 任务 | 说明 |
|------|------|
| Merchant 库存查询 | `GET /api/merchant/inventory/skus/{skuId}` |
| Merchant 入库 | `POST /api/merchant/inventory/inbound` |
| Merchant 库存调整 | `POST /api/merchant/inventory/adjust` |
| Merchant 库存流水 | `GET /api/merchant/inventory/movements` |

### 11.4 Sprint 10 Step 3 — Customer 库存 API & 跨域集成

| 任务 | 说明 |
|------|------|
| Customer 是否有货 | `GET /api/inventory/skus/{skuId}/availability` |
| Customer 批量查询 | `POST /api/inventory/batch-availability` |
| 商品详情库存集成 | CustomerProductService 注入 InventoryService |
| 内部库存操作 API | reserve / release / deduct |

### 11.5 Sprint 10 Step 4 — Admin 库存管控 & 事件集成

| 任务 | 说明 |
|------|------|
| Admin 库存总览 | `GET /api/admin/inventory` |
| Admin 强制调整 | `POST /api/admin/inventory/adjust` |
| Admin 库存流水 | `GET /api/admin/inventory/movements` |
| Admin 库存预警 | `GET /api/admin/inventory/alerts` |
| 领域事件发布 | InventoryReservedEvent / InventoryReleasedEvent / InventoryDeductedEvent |
| 超时释放定时任务 | 定时扫描 EXPIRED Reservation |

### 11.6 后续 Sprint 规划

| Sprint | 任务 |
|--------|------|
| Sprint 11 | Order Domain 集成 Inventory（下单锁定、取消释放） |
| Sprint 12 | Payment Domain 集成 Inventory（支付扣减） |
| Sprint 13 | Cart Domain 接入库存查询 |
| Sprint 14 | Search Domain 接入库存状态同步 |
| Sprint 15 | 多仓库支持、安全库存预警、超时自动释放 |

---

> **文档版本:** v1.0  
> **最后更新:** 2026-07-26  
> **状态:** 设计阶段 — 仅定义架构设计，不创建 Entity/Repository/Service/Controller  
> **下一步:** Sprint 10 Step 1 — 库存模块代码实现