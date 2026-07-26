# Sprint 9 Step 2C — Admin Product Review & Lifecycle 实现报告

> **日期:** 2026-07-26  
> **状态:** ✅ 完成

---

## 1. Admin DTO

| DTO | 位置 | 说明 |
|-----|------|------|
| `ProductAuditRequest` | `product/dto/admin/` | 审核请求：auditRemark（@NotBlank） |
| `AdminProductQueryRequest` | `product/dto/admin/` | 列表查询：page/size/status/merchantId/categoryId/keyword |
| `AdminProductListResponse` | `product/dto/admin/` | 列表响应：含 merchantId/merchantName 等 Admin 专用字段 |
| `AdminProductDetailResponse` | `product/dto/admin/` | 详情响应：含 merchantId/merchantName/version/全部子实体 |

Admin DTO 独立管理，包含 `merchantId` 等 Admin 可见字段，Customer 端不可见。

---

## 2. ProductAuditService

| 方法 | @Transactional | 说明 |
|------|----------------|------|
| `listPendingProducts()` | ✅ readOnly | 查询所有 PENDING_REVIEW 状态商品 |
| `getProductDetail()` | ✅ readOnly | Admin 可查看所有状态商品 |
| `approveProduct()` | ✅ | PENDING_REVIEW → ON_SHELF + 记录审核日志 + 发布事件 |
| `rejectProduct()` | ✅ | PENDING_REVIEW → REJECTED + 记录审核日志 + 发布事件 |
| `forceOffShelf()` | ✅ | ON_SHELF → OFF_SHELF + 记录审核日志 + 发布事件 |
| `restoreProduct()` | ✅ | OFF_SHELF → ON_SHELF + 记录审核日志 |

---

## 3. 状态机控制

### 合法流转规则

```
DRAFT ──────────→ PENDING_REVIEW  (商家提交审核)
PENDING_REVIEW ──→ ON_SHELF       (审核通过)
PENDING_REVIEW ──→ REJECTED       (审核驳回)
ON_SHELF ────────→ OFF_SHELF      (强制下架)
OFF_SHELF ───────→ ON_SHELF       (恢复上架)
OFF_SHELF ───────→ ARCHIVED       (商家删除)
```

### 禁止的非法流转（均抛 BusinessException）

| 非法流转 | 错误码 |
|----------|--------|
| REJECTED → ON_SHELF | 30101 |
| DRAFT → ON_SHELF | 30101 |
| ARCHIVED → ON_SHELF | 30101 |
| 重复审核 PENDING_REVIEW | 30102 |
| 已下架再次下架 | 30103 |

---

## 4. Controller API

`AdminProductController` — `@RequestMapping("/api/admin/products")`

| HTTP | 路径 | 方法 | 说明 |
|------|------|------|------|
| GET | `/api/admin/products/pending` | `listPendingProducts()` | 待审核列表 |
| GET | `/api/admin/products/{id}` | `getProductDetail()` | 商品详情 |
| PUT | `/api/admin/products/{id}/approve` | `approveProduct()` | 审核通过 |
| PUT | `/api/admin/products/{id}/reject` | `rejectProduct()` | 审核驳回 |
| PUT | `/api/admin/products/{id}/off-shelf` | `forceOffShelf()` | 强制下架 |
| PUT | `/api/admin/products/{id}/restore` | `restoreProduct()` | 恢复上架 |

---

## 5. 审核记录（预留）

### Entity: `ProductAuditRecord`

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 主键 |
| productId | Long | 商品 ID |
| reviewerId | Long | 审核人 ID |
| action | String | APPROVE / REJECT / FORCE_OFF_SHELF / RESTORE |
| beforeStatus | String | 操作前状态 |
| afterStatus | String | 操作后状态 |
| auditRemark | String | 审核备注 |
| createdTime | LocalDateTime | 创建时间 |

### Repository: `ProductAuditRecordRepository`

继承 `JpaRepository`，仅基础操作，无复杂查询。

---

## 6. 事件预留

### 事件类

| 事件 | 字段 |
|------|------|
| `ProductApprovedEvent` | productId / reviewerId / auditRemark |
| `ProductRejectedEvent` | productId / reviewerId / auditRemark |
| `ProductOffShelfEvent` | productId / reviewerId / reason |

### 发布机制

通过 `ApplicationEventPublisher.publishEvent()` 发布，未接入 MQ。后续可通过 `@EventListener` 或 MQ 扩展。

---

## 7. 权限控制

| 层 | 机制 |
|-----|------|
| SecurityConfig | `api/admin/**` → hasAuthority("ROLE_ADMIN") |
| @PreAuthorize | `hasRole('ADMIN')` 方法级 |

---

## 8. 异常体系

| 错误码 | 常量 | 触发条件 |
|--------|------|----------|
| 30001 | PRODUCT_NOT_FOUND | 商品不存在 |
| 30101 | PRODUCT_ILLEGAL_STATUS | 非法状态流转 |
| 30102 | PRODUCT_ALREADY_AUDITED | 商品已审核（重复操作） |
| 30103 | PRODUCT_ALREADY_OFFLINE | 商品已下架 |

---

## 9. 编译结果

```
mvn compile → BUILD SUCCESS (71 source files)
```

| 检查项 | 结果 |
|--------|------|
| Maven Compile | ✅ 成功（0 错误） |
| 循环依赖 | ❌ 无 |
| Hibernate Mapping 错误 | ❌ 无 |
| Package 命名冲突 | ❌ 无 |

---

## 10. 文件清单

```
backend/commerce-platform/src/main/java/com/commerce/platform/product/
├── dto/admin/
│   ├── ProductAuditRequest.java                 ★ 新增
│   ├── AdminProductQueryRequest.java            ★ 新增
│   ├── AdminProductListResponse.java            ★ 新增
│   └── AdminProductDetailResponse.java          ★ 新增
├── entity/
│   └── ProductAuditRecord.java                  ★ 新增（审核记录实体）
├── repository/
│   └── ProductAuditRecordRepository.java        ★ 新增
├── mq/event/
│   ├── ProductApprovedEvent.java                ★ 新增
│   ├── ProductRejectedEvent.java                ★ 新增
│   └── ProductOffShelfEvent.java                ★ 新增
├── service/
│   ├── ProductAuditService.java                 ★ 新增
│   └── impl/
│       └── ProductAuditServiceImpl.java         ★ 新增
└── controller/
    └── AdminProductController.java              ★ 新增
```

---

## 11. Sprint 9 全部 API 汇总

| 分组 | API | 路径 | Step |
|------|-----|------|------|
| **Merchant** | 创建商品 | POST `/api/merchant/products` | 2A |
| **Merchant** | 更新商品 | PUT `/api/merchant/products/{id}` | 2A |
| **Merchant** | 删除商品 | DELETE `/api/merchant/products/{id}` | 2A |
| **Merchant** | 商品详情 | GET `/api/merchant/products/{id}` | 2A |
| **Merchant** | 商品列表 | GET `/api/merchant/products` | 2A |
| **Customer** | 商品列表 | GET `/api/products` | 2B |
| **Customer** | 商品详情 | GET `/api/products/{id}` | 2B |
| **Customer** | 分类树 | GET `/api/categories/tree` | 2B |
| **Admin** | 待审核列表 | GET `/api/admin/products/pending` | 2C |
| **Admin** | 商品详情 | GET `/api/admin/products/{id}` | 2C |
| **Admin** | 审核通过 | PUT `/api/admin/products/{id}/approve` | 2C |
| **Admin** | 审核驳回 | PUT `/api/admin/products/{id}/reject` | 2C |
| **Admin** | 强制下架 | PUT `/api/admin/products/{id}/off-shelf` | 2C |
| **Admin** | 恢复上架 | PUT `/api/admin/products/{id}/restore` | 2C |

---

> **文档版本:** v1.0  
> **最后更新:** 2026-07-26  
> **状态:** Sprint 9 全部完成（Step 0 → 1 → 2A → 2B → 2C）  
> **编译:** 71 个源文件，BUILD SUCCESS，0 错误