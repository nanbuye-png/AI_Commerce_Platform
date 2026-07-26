# Sprint 10 Step 2C — Inventory Movement & Audit API Report

> **文档版本:** v1.0  
> **最后更新:** 2026-07-26  
> **状态:** 完成  
> **对应:** Sprint 10 Step 2C — Inventory Movement & Audit API

---

## 目录

1. [新增文件清单](#一新增文件清单)
2. [DTO 设计](#二dto-设计)
3. [Enum 设计](#三enum-设计)
4. [Service 设计](#四service-设计)
5. [Merchant API](#五merchant-api)
6. [Admin API](#六admin-api)
7. [审计模型](#七审计模型)
8. [Enterprise Verification](#八enterprise-verification)
9. [编译结果](#九编译结果)
10. [架构护栏检查](#十架构护栏检查)

---

## 一、新增文件清单

本次新增 **12 个文件**（2 Enum + 3 DTO + 2 Service + 2 Controller + 1 Entity 更新 + 1 Migration + 1 Report）：

### 新增

| 文件 | 路径 | 类型 |
|------|------|------|
| `MovementSourceType` | `domain/enums/MovementSourceType.java` | Enum（MERCHANT/ORDER/ADMIN/SYSTEM） |
| `MovementReasonCode` | `domain/enums/MovementReasonCode.java` | Enum（8种原因码） |
| `InventoryMovementQueryRequest` | `dto/movement/InventoryMovementQueryRequest.java` | Request DTO |
| `InventoryMovementResponse` | `dto/movement/InventoryMovementResponse.java` | Response DTO |
| `InventoryMovementDetailResponse` | `dto/movement/InventoryMovementDetailResponse.java` | Detail Response DTO |
| `InventoryMovementService` | `service/InventoryMovementService.java` | Service 接口 |
| `InventoryMovementServiceImpl` | `service/impl/InventoryMovementServiceImpl.java` | Service 实现 |
| `MerchantInventoryMovementController` | `controller/MerchantInventoryMovementController.java` | Merchant Controller |
| `AdminInventoryMovementController` | `controller/AdminInventoryMovementController.java` | Admin Controller |
| `V4__inventory_movement_audit.sql` | `resources/db/migration/V4__inventory_movement_audit.sql` | Flyway Migration |

### 修改

| 文件 | 变更 |
|------|------|
| `InventoryMovement` Entity | 新增 6 个审计字段（sourceType, sourceId, reasonCode, beforeReserved, afterReserved, operatorName） |

---

## 二、DTO 设计

### InventoryMovementQueryRequest

| 字段 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| page | Integer | 1 | 页码 |
| pageSize | Integer | 20 | 每页条数 |
| productSkuId | Long | — | 按 SKU 筛选 |
| movementType | String | — | 变动类型 |
| sourceType | String | — | 来源类型 |
| reasonCode | String | — | 原因码 |
| startTime | LocalDateTime | — | 开始时间 |
| endTime | LocalDateTime | — | 结束时间 |

### InventoryMovementResponse（列表）

| 字段 | 类型 | 说明 |
|------|------|------|
| movementNo | String | 流水编号 |
| productSkuId | Long | SKU ID |
| movementType | String | 变动类型 |
| sourceType | String | 来源类型 |
| sourceId | String | 来源 ID |
| reasonCode | String | 原因码 |
| quantity | Integer | 变动数量 |
| beforeAvailable | Integer | 变动前可售库存 |
| afterAvailable | Integer | 变动后可售库存 |
| operatorName | String | 操作人名称 |
| remark | String | 备注 |
| createdTime | LocalDateTime | 创建时间 |

### InventoryMovementDetailResponse（详情 - 完整审计链）

| 字段 | 类型 | 说明 |
|------|------|------|
| movementNo | String | 流水编号 |
| productSkuId | Long | SKU ID |
| inventoryId | Long | 库存记录 ID |
| movementType | String | 变动类型 |
| sourceType | String | 来源类型 |
| sourceId | String | 来源 ID |
| reasonCode | String | 原因码 |
| quantity | Integer | 变动数量 |
| beforeAvailable | Integer | 变动前可售库存 |
| afterAvailable | Integer | 变动后可售库存 |
| beforeReserved | Integer | 变动前已锁定库存 |
| afterReserved | Integer | 变动后已锁定库存 |
| operatorId | Long | 操作人 ID |
| operatorName | String | 操作人名称 |
| businessId | String | 业务单号 |
| remark | String | 备注 |
| createdTime | LocalDateTime | 创建时间 |

---

## 三、Enum 设计

### MovementSourceType（来源类型）

| 值 | 说明 | 典型场景 |
|-----|------|----------|
| MERCHANT | 商家操作 | 入库、手工调整 |
| ORDER | 订单流程 | 锁定、释放、扣减 |
| ADMIN | 平台管理员 | 强制调整、审核操作 |
| SYSTEM | 系统自动操作 | 定时任务、数据同步 |

### MovementReasonCode（原因码）

| 值 | 说明 | 预留 |
|-----|------|------|
| NORMAL_INBOUND | 正常入库 | — |
| MANUAL_ADJUST | 手动调整 | — |
| ORDER_RESERVE | 订单锁定 | — |
| ORDER_RELEASE | 订单释放 | — |
| ORDER_DEDUCT | 订单扣减 | — |
| RETURN | 退货入库 | ✅ 预留 |
| DAMAGE | 报损 | ✅ 预留 |
| SYSTEM_SYNC | 系统同步 | ✅ 预留 |

---

## 四、Service 设计

### InventoryMovementService 接口

| 方法 | 角色 | 说明 |
|------|------|------|
| `listMovements(merchantId, query)` | MERCHANT | 分页查询本店库存流水 |
| `listAllMovements(query)` | ADMIN | 分页查询全平台库存流水 |
| `getMovementDetail(movementId)` | ADMIN | 查询流水详情（完整审计链） |
| `exportMovements(query)` | — | 预留：导出流水 |

---

## 五、Merchant API

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| **GET** | `/api/merchant/inventory/movements` | MERCHANT | 查询本店库存流水 |

支持查询参数：page, pageSize, productSkuId, movementType, sourceType, reasonCode, startTime, endTime

---

## 六、Admin API

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| **GET** | `/api/admin/inventory/movements` | ADMIN | 查询全平台库存流水 |
| **GET** | `/api/admin/inventory/movements/{id}` | ADMIN | 查询流水详情（完整审计链） |

Admin 可查看全平台流水。Merchant 仅查看自己店铺。

---

## 七、审计模型

### 7.1 完整审计链

InventoryMovement 现包含 **17 个字段**，形成完整审计链路：

```
编号类:
├── movementNo       ← 流水编号（全局唯一）

关联类:
├── productSkuId     ← 哪个 SKU
├── inventoryId      ← 哪条库存记录

业务类:
├── movementType     ← INBOUND / RESERVE / DEDUCT ...
├── sourceType       ← MERCHANT / ORDER / ADMIN / SYSTEM
├── sourceId         ← 订单号 / 入库单号
├── reasonCode       ← NORMAL_INBOUND / ORDER_RESERVE ...

数量类:
├── quantity         ← ±数量
├── beforeAvailable  ← 变动前可售库存（快照）
├── afterAvailable   ← 变动后可售库存（快照）
├── beforeReserved   ← 变动前已锁定库存（快照）
├── afterReserved    ← 变动后已锁定库存（快照）

人员类:
├── operatorId       ← 操作人 ID
├── operatorName     ← 操作人名称

追溯类:
├── businessId       ← 业务单号
├── remark           ← 备注
├── createdTime      ← 创建时间
```

### 7.2 V4 Migration 变更

```sql
ALTER TABLE inventory_movement
    ADD COLUMN source_type VARCHAR(20),          -- 来源类型
    ADD COLUMN source_id VARCHAR(64),            -- 来源 ID
    ADD COLUMN reason_code VARCHAR(30),          -- 原因码
    ADD COLUMN before_reserved INTEGER,          -- 变动前锁定库存
    ADD COLUMN after_reserved INTEGER,           -- 变动后锁定库存
    ADD COLUMN operator_name VARCHAR(64);        -- 操作人名称（冗余，无需 JOIN）
```

---

## 八、Enterprise Verification

| # | 要求 | 状态 | 验证方式 |
|---|------|------|----------|
| ① | **Movement 永远 Append-Only** | ✅ 通过 | 所有字段 `updatable=false`，无 delete 操作 |
| ② | **所有查询分页** | ✅ 通过 | PageRequest + PageResult |
| ③ | **DTO 不泄漏 Entity** | ✅ 通过 | Response DTO 独立于 Entity |
| ④ | **Controller 无业务逻辑** | ✅ 通过 | 全部委托 InventoryMovementService |
| ⑤ | **Inventory 不依赖 Order** | ✅ 通过 | sourceId 和 businessId 仅存 String |
| ⑥ | **SourceType 完整** | ✅ 通过 | MERCHANT/ORDER/ADMIN/SYSTEM |
| ⑦ | **ReasonCode 完整** | ✅ 通过 | 8 种原因码（含 RETURN/DAMAGE/SYSTEM_SYNC 预留） |
| ⑧ | **审计链完整** | ✅ 通过 | 17 个字段形成完整审计链路 |

**无设计偏离。**

---

## 九、编译结果

### 编译命令

```
cd backend/commerce-platform && mvn clean compile
```

### 编译输出

```
[INFO] Compiling 107 source files with javac [debug release 17] to target\classes
[INFO] BUILD SUCCESS
[INFO] Total time:  6.442 s
```

| 指标 | 值 |
|------|-----|
| 编译状态 | ✅ BUILD SUCCESS |
| 编译文件数 | 107 source files（新增 12 个，累计 107） |
| 错误 | 0 |
| 警告 | 0 |

---

## 十、架构护栏检查

### 必须遵守 ✅

| 护栏 | 结果 |
|------|------|
| 保持 DDD 分层 | ✅ Controller → Service → Repository |
| InventoryMovement 永远 Append-Only | ✅ 所有字段 `updatable=false` |
| 所有查询统一分页 | ✅ PageRequest + PageResult |
| 不修改 Step 2A/2B 业务逻辑 | ✅ 仅新增 InventoryMovement Entity 字段 |
| Inventory 不依赖 Order | ✅ sourceId/businessId 仅存 String |

### 禁止 ❌

| 禁止项 | 结果 |
|--------|------|
| UPDATE InventoryMovement | ✅ 未违反 |
| DELETE InventoryMovement | ✅ 未违反 |
| Controller 调 Repository | ✅ 未违反 |
| 返回 Entity | ✅ 全部返回 DTO |

### 范围限制

| 限制项 | 状态 |
|--------|------|
| 不实现 BI | ✅ 已遵守 |
| 不实现 Excel 导出 | ✅ 已遵守（exportMovements 仅预留） |
| 不实现 Scheduler | ✅ 已遵守 |
| 不实现 MQ | ✅ 已遵守 |
| 不实现 Order Domain | ✅ 已遵守 |
| 不修改 Product Domain | ✅ 已遵守 |
| V1/V2/V3 未改动 | ✅ 已遵守 |

---

> **文档版本:** v1.0  
> **最后更新:** 2026-07-26  
> **状态:** 完成 — Inventory Movement 审计增强 + Merchant/Admin 流水查询 API 已全部创建并通过编译  
> **下一步:** Sprint 10 Step 3 — Customer 库存查询 + 跨域集成