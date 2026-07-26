# Sprint 10 Step 2A — Merchant Inventory Management Report

> **文档版本:** v1.0  
> **最后更新:** 2026-07-26  
> **状态:** 完成  
> **对应:** Sprint 10 Step 2A — Merchant Inventory Management

---

## 目录

1. [新增文件清单](#一新增文件清单)
2. [DTO 设计](#二dto-设计)
3. [Service 设计](#三service-设计)
4. [Controller 设计](#四controller-设计)
5. [API 清单](#五api-清单)
6. [InventoryMovement 审计记录](#六inventorymovement-审计记录)
7. [Enterprise Design Verification](#七enterprise-design-verification)
8. [编译结果](#八编译结果)
9. [架构护栏检查](#九架构护栏检查)

---

## 一、新增文件清单

本次新增 **8 个文件**（DTO 5个 + Service 2个 + Controller 1个）：

| 文件 | 路径 | 类型 |
|------|------|------|
| `InventoryAdjustRequest` | `dto/merchant/InventoryAdjustRequest.java` | Request DTO |
| `InventoryQueryRequest` | `dto/merchant/InventoryQueryRequest.java` | Request DTO |
| `InventoryDetailResponse` | `dto/merchant/InventoryDetailResponse.java` | Response DTO |
| `InventoryListResponse` | `dto/merchant/InventoryListResponse.java` | Response DTO |
| `InventoryMovementResponse` | `dto/merchant/InventoryMovementResponse.java` | Response DTO |
| `InventoryService` | `service/InventoryService.java` | Service 接口 |
| `InventoryServiceImpl` | `service/impl/InventoryServiceImpl.java` | Service 实现 |
| `MerchantInventoryController` | `controller/MerchantInventoryController.java` | Controller |

---

## 二、DTO 设计

### 2.1 Request DTO

#### InventoryAdjustRequest

| 字段 | 类型 | 验证 | 说明 |
|------|------|------|------|
| adjustType | String | @NotNull | INCREASE（增加）/ DECREASE（减少） |
| quantity | Integer | @NotNull, @Min(1) | 调整数量（正整数） |
| remark | String | — | 备注 |

#### InventoryQueryRequest

| 字段 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| page | Integer | 1 | 页码 |
| pageSize | Integer | 20 | 每页条数 |
| skuCode | String | — | SKU 编码模糊搜索 |
| productName | String | — | 商品名称模糊搜索 |

### 2.2 Response DTO

#### InventoryDetailResponse

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 库存记录 ID |
| productSkuId | Long | SKU ID |
| skuCode | String | SKU 编码 |
| productName | String | 商品名称 |
| availableStock | Integer | 可售库存 |
| reservedStock | Integer | 已锁定库存 |
| totalStock | Integer | 总库存（自动计算） |
| lowStockThreshold | Integer | 低库存阈值 |

#### InventoryListResponse

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 库存记录 ID |
| productSkuId | Long | SKU ID |
| skuCode | String | SKU 编码 |
| productName | String | 商品名称 |
| availableStock | Integer | 可售库存 |
| reservedStock | Integer | 已锁定库存 |
| totalStock | Integer | 总库存 |
| lowStock | Boolean | 是否低于安全库存 |

#### InventoryMovementResponse

| 字段 | 类型 | 说明 |
|------|------|------|
| movementNo | String | 流水编号 |
| productSkuId | Long | SKU ID |
| movementType | String | 变动类型 |
| quantity | Integer | 变动数量 |
| beforeAvailable | Integer | 变动前可售库存 |
| afterAvailable | Integer | 变动后可售库存 |
| operatorId | Long | 操作人 ID |
| remark | String | 备注 |
| createdTime | LocalDateTime | 创建时间 |

**设计原则：** 不暴露内部审计字段（version、reservation 内部状态等）。

---

## 三、Service 设计

### 3.1 InventoryService 接口

| 方法 | 事务 | 说明 |
|------|------|------|
| `listInventory(merchantId, query)` | 只读 | 分页查询商家库存列表 |
| `getInventoryDetail(merchantId, inventoryId)` | 只读 | 查询库存详情 |
| `adjustInventory(merchantId, inventoryId, request)` | @Transactional | 调整库存 |
| `inboundInventory(merchantId, inventoryId, request)` | @Transactional | 入库操作 |
| `listInventoryMovements(merchantId, inventoryId, page, pageSize)` | 只读 | 查询库存流水 |

### 3.2 库存调整规则

#### adjustInventory（调整库存）

| 调整类型 | 操作 | 校验 |
|----------|------|------|
| INCREASE（增加） | `availableStock += quantity` | 无 |
| DECREASE（减少） | `availableStock -= quantity` | `availableStock >= quantity` 否则抛 BusinessException |

**自动计算：** `totalStock = availableStock + reservedStock`

#### inboundInventory（入库）

| 操作 | 校验 |
|------|------|
| `availableStock += quantity` | 无 |
| `totalStock = availableStock + reservedStock` | 自动计算 |

### 3.3 事务一致性

- `adjustInventory` 和 `inboundInventory` 均标注 `@Transactional(rollbackFor = Exception.class)`
- Inventory 更新 + InventoryMovement 创建在**同一事务**中完成
- 任何步骤失败 → 全部回滚

### 3.4 InventoryMovement 流水生成

每次库存变更自动生成 Movement，记录字段：

| 字段 | 值示例 |
|------|--------|
| movementNo | `MV202607262144301A2B3C`（格式：MV + 时间戳 + 6位随机字符） |
| movementType | ADJUST / INBOUND |
| quantity | ±10 |
| beforeAvailable | 50（变动前快照） |
| afterAvailable | 60（变动后快照） |
| operatorId | 当前商家 ID |
| remark | 用户传入的备注 |

**Append-Only 原则：** 仅 INSERT，支持追溯审计。

---

## 四、Controller 设计

### MerchantInventoryController

| 项目 | 说明 |
|------|------|
| 基础路径 | `/api/merchant/inventory` |
| 统一响应 | `Result<T>` |
| 权限 | `@PreAuthorize("hasRole('MERCHANT')")` |
| 数据隔离 | Controller 层提取 Authentication principal 中的 `merchantId` |
| 业务逻辑 | **Controller 不含任何业务逻辑**，全部委托给 InventoryService |

### Controller 方法结构

```
getMerchantId(Authentication)  →  merchantId
         │
         ▼
inventoryService.xxx(merchantId, ...)  →  Result<T>(response)
```

---

## 五、API 清单

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| **GET** | `/api/merchant/inventory` | MERCHANT | 查询我的库存列表（分页） |
| **GET** | `/api/merchant/inventory/{id}` | MERCHANT | 查询库存详情 |
| **PUT** | `/api/merchant/inventory/{id}/adjust` | MERCHANT | 调整库存（增加/减少） |
| **POST** | `/api/merchant/inventory/{id}/inbound` | MERCHANT | 入库 |
| **GET** | `/api/merchant/inventory/{id}/movements` | MERCHANT | 查询库存流水（分页） |

### API 请求/响应示例

#### GET /api/merchant/inventory/{id}

**响应：**
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": 1,
    "productSkuId": 1001,
    "availableStock": 50,
    "reservedStock": 10,
    "totalStock": 60,
    "lowStockThreshold": 20
  }
}
```

#### PUT /api/merchant/inventory/{id}/adjust

**请求体：**
```json
{
  "adjustType": "INCREASE",
  "quantity": 10,
  "remark": "盘点调整"
}
```

**响应：**
```json
{
  "code": 0,
  "message": "success",
  "data": null
}
```

#### GET /api/merchant/inventory/{id}/movements?page=1&pageSize=20

**响应：**
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "content": [
      {
        "movementNo": "MV202607262144301A2B3C",
        "productSkuId": 1001,
        "movementType": "INBOUND",
        "quantity": 100,
        "beforeAvailable": 0,
        "afterAvailable": 100,
        "operatorId": 1,
        "remark": "采购入库",
        "createdTime": "2026-07-26T21:44:30"
      }
    ],
    "totalElements": 1,
    "totalPages": 1,
    "number": 0,
    "size": 20
  }
}
```

---

## 六、InventoryMovement 审计记录

### 6.1 流水生成规则

| 操作 | MovementType | quantity 符号 | beforeAvailable 来源 | afterAvailable 来源 |
|------|-------------|---------------|---------------------|---------------------|
| 入库（inbound） | INBOUND | +（正数） | 变更前的 availableStock | 变更后的 availableStock |
| 增加调整（adjust INCREASE） | ADJUST | +（正数） | 变更前的 availableStock | 变更后的 availableStock |
| 减少调整（adjust DECREASE） | ADJUST | -（负数） | 变更前的 availableStock | 变更后的 availableStock |

### 6.2 Append-Only 保证

| 检查项 | 实现方式 | 结果 |
|--------|----------|------|
| 仅 INSERT | InventoryMovementRepository.save() | ✅ |
| 不 UPDATE | 所有字段标记 `updatable=false`（Entity 层面） | ✅ |
| 不 DELETE | 不提供 delete 方法调用 | ✅ |
| 永久保存 | 无 deleted 字段 | ✅ |

---

## 七、Enterprise Design Verification

### 验证项清单

| # | 设计要求 | 状态 | 验证方式 |
|---|---------|------|----------|
| ① | **三字段库存模型未破坏** — available/reserved/total 三个独立字段 | ✅ 通过 | Service 仅修改 `availableStock`，禁止直接修改 `totalStock` |
| ② | **totalStock 自动计算** — totalStock = availableStock + reservedStock | ✅ 通过 | `adjustInventory()` 和 `inboundInventory()` 中均有 `setTotalStock(available + reserved)` |
| ③ | **Merchant 不允许直接修改 totalStock** | ✅ 通过 | Merchant API 仅提供 adjust/inbound 操作，内部自动计算 totalStock |
| ④ | **所有库存变化均生成 Movement** | ✅ 通过 | `adjustInventory()` 和 `inboundInventory()` 均通过 `createMovement()` 生成流水 |
| ⑤ | **Append-Only 保持** | ✅ 通过 | Movement 仅 save()，无 delete/update 操作 |
| ⑥ | **不允许负库存** | ✅ 通过 | DECREASE 操作校验 `availableStock >= quantity`，否则抛 BusinessException |
| ⑦ | **全部通过 Service** | ✅ 通过 | Controller 无业务逻辑，全部委托 InventoryService |
| ⑧ | **Controller 无业务逻辑** | ✅ 通过 | Controller 仅做参数提取 + 结果包装 |

### 设计偏离声明

**无偏离。** 所有 Enterprise Design 要求均已落实。

---

## 八、编译结果

### 编译命令

```
cd backend/commerce-platform && mvn clean compile
```

### 编译输出

```
[INFO] Compiling 87 source files with javac [debug release 17] to target\classes
[INFO] BUILD SUCCESS
[INFO] Total time:  6.140 s
```

### 编译指标

| 指标 | 值 |
|------|-----|
| 编译状态 | ✅ BUILD SUCCESS |
| 编译文件数 | 87 source files（新增 8 个文件，累计 87 个） |
| Java 版本 | 17 |
| 错误 | 0 |
| 警告 | 0 |

---

## 九、架构护栏检查

### 必须遵守 ✅

| 护栏 | 检查结果 | 说明 |
|------|----------|------|
| 保持 DDD 分层 | ✅ 通过 | Controller → Service → Repository 三层分离 |
| Inventory 为独立业务域 | ✅ 通过 | 全部在 `inventory` package 中 |
| 使用 Flyway + Hibernate validate | ✅ 通过 | V3 已创建，Entity/JPA 注解就绪 |
| 所有库存变更必须记录 InventoryMovement | ✅ 通过 | adjust/inbound 均调用 `createMovement()` |
| 所有库存修改必须经过 Service | ✅ 通过 | Controller 不操作 Repository |

### 禁止 ❌

| 禁止项 | 检查结果 | 说明 |
|--------|----------|------|
| Controller 操作 Repository | ✅ 未违反 | 全部委托 Service |
| 修改 Product Domain | ✅ 未违反 | 未改动任何 Product 文件 |
| 修改已执行 Flyway Migration | ✅ 未违反 | V1/V2 未改动 |
| 跳过库存流水直接修改库存 | ✅ 未违反 | 每步变更均生成 Movement |

### 范围限制

| 限制项 | 状态 |
|--------|------|
| 不实现 Reservation（Step 2B） | ✅ 已遵守 |
| 不实现库存锁定 | ✅ 已遵守 |
| 不实现库存释放 | ✅ 已遵守 |
| 不实现自动扣减库存 | ✅ 已遵守 |
| 不实现 MQ | ✅ 已遵守 |
| 不进入 Customer Inventory | ✅ 已遵守 |
| 不进入 Order Domain | ✅ 已遵守 |

---

> **文档版本:** v1.0  
> **最后更新:** 2026-07-26  
> **状态:** 完成 — Merchant 库存管理 DTO + Service + Controller 已全部创建并通过编译  
> **下一步:** Sprint 10 Step 2B — Reservation 库存锁定实现