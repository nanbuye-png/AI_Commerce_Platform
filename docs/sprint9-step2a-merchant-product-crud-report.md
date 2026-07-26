# Sprint 9 Step 2A — Merchant Product CRUD 实现报告

> **日期:** 2026-07-26  
> **状态:** ✅ 完成

---

## 1. 新建 DTO

| DTO | 类型 | 说明 |
|-----|------|------|
| `CreateProductRequest` | Request | 创建商品：name/description/brand/categoryId + images/specs/skus |
| `UpdateProductRequest` | Request | 更新商品（全选填字段，全量替换子实体） |
| `ProductQueryRequest` | Request | 列表查询：page/pageSize/status/keyword |
| `ProductImageRequest` | Request | 图片子实体：url/imageType/sort/isCover |
| `ProductSpecRequest` | Request | 规格子实体：specName/specValues/sort |
| `ProductSkuRequest` | Request | SKU子实体：skuCode/attributesJson/price/originalPrice/weight/stock |
| `ProductDetailResponse` | Response | 商品详情（含图片/规格/SKU嵌套） |
| `ProductListResponse` | Response | 商品列表（含coverImage首图URL） |

**目录：** `product/dto/merchant/`

所有 Request DTO 使用 Jakarta Validation 注解（`@NotBlank` / `@NotNull` / `@Size`）。

---

## 2. Service

| 接口 | 实现 |
|------|------|
| `ProductService` | `ProductServiceImpl` |

### API 方法

| 方法 | `@Transactional` | 说明 |
|------|------------------|------|
| `createProduct()` | ✅ 读写事务 | 自动生成 productCode，初始状态 DRAFT |
| `updateProduct()` | ✅ 读写事务 | 全量替换 images/specs/skus |
| `deleteProduct()` | ✅ 读写事务 | 软删除（`deleted=true`） |
| `getProductDetail()` | ✅ 只读事务 | 含子实体完整信息 |
| `listMyProducts()` | ✅ 只读事务 | 分页，支持 status 筛选 |

### 关键实现细节

| 特性 | 实现 |
|------|------|
| **productCode 生成** | `ProductCodeGenerator` 接口 + `SimpleProductCodeGenerator` 临时实现（PROD + 时间戳 + 6位序列号） |
| **storeId** | 当前使用 merchantId 替代，预留 TODO |
| **SKU 编码重复检查** | 创建和更新时均检查请求内 SKU 编码唯一性 |
| **productCode 唯一性** | 通过 `productRepository.findByProductCode()` 校验 |
| **全量替换策略** | update 时 clear() + addAll()，JPA orphanRemoval=true 自动删除旧记录 |

---

## 3. Controller

`MerchantProductController` — `@RequestMapping("/api/merchant/products")`

| HTTP 方法 | 路径 | 方法 | 说明 |
|-----------|------|------|------|
| POST | `/api/merchant/products` | `createProduct()` | 创建商品 |
| PUT | `/api/merchant/products/{id}` | `updateProduct()` | 更新商品 |
| DELETE | `/api/merchant/products/{id}` | `deleteProduct()` | 删除商品 |
| GET | `/api/merchant/products/{id}` | `getProductDetail()` | 商品详情 |
| GET | `/api/merchant/products` | `listMyProducts()` | 商品列表 |

**返回格式：** 项目统一 `Result<T>`（code / message / data）

---

## 4. 权限验证

### 路由层（SecurityConfig）

```
/api/merchant/** → hasAuthority("ROLE_MERCHANT")
```

已有配置，无需修改。

### Controller 方法层（@PreAuthorize）

```java
@PreAuthorize("hasRole('MERCHANT')")
```

所有 5 个接口均受保护。

### Service 层（数据隔离）

每个接口在 Service 层校验 `merchantId` 与 `product.getMerchantId()` 是否一致，防止跨商家访问：

```java
if (!product.getMerchantId().equals(merchantId)) {
    throw new BusinessException(PRODUCT_UNAUTHORIZED, "无权操作此商品");
}
```

---

## 5. 事务管理

| 方法 | 事务注解 | 管理实体 |
|------|----------|----------|
| `createProduct()` | `@Transactional` | Product + ProductImage + ProductSpec + ProductSku（Cascade ALL） |
| `updateProduct()` | `@Transactional` | 同上，全量替换子实体 |
| `deleteProduct()` | `@Transactional` | Product（软删除） |
| `getProductDetail()` | `@Transactional(readOnly = true)` | Product + LAZY 加载子实体 |
| `listMyProducts()` | `@Transactional(readOnly = true)` | Product 分页查询 |

---

## 6. 异常处理

| 错误码 | 常量 | 触发条件 |
|--------|------|----------|
| 30001 | PRODUCT_NOT_FOUND | 商品不存在（`findById` 为空） |
| 30002 | PRODUCT_UNAUTHORIZED | 非本店商品操作 |
| 30003 | PRODUCT_CODE_DUPLICATE | 商品编码冲突 |
| 30004 | PRODUCT_INVALID_STATUS | SKU 编码重复 |

使用项目统一 `BusinessException`，`GlobalExceptionHandler` 自动捕获并返回 `Result<T>`。

---

## 7. 编译结果

```
mvn compile → BUILD SUCCESS (51 source files)
```

| 检查项 | 结果 |
|--------|------|
| Maven Compile | ✅ 成功（0 错误） |
| 循环依赖 | ❌ 无 |
| Hibernate Mapping 错误 | ❌ 无 |
| Package 命名冲突 | ❌ 无 |

---

## 8. 文件清单

```
backend/commerce-platform/src/main/java/com/commerce/platform/product/
├── dto/merchant/
│   ├── CreateProductRequest.java
│   ├── UpdateProductRequest.java
│   ├── ProductQueryRequest.java
│   ├── ProductImageRequest.java
│   ├── ProductSpecRequest.java
│   ├── ProductSkuRequest.java
│   ├── ProductDetailResponse.java
│   └── ProductListResponse.java
├── service/
│   ├── ProductService.java
│   ├── ProductCodeGenerator.java
│   └── impl/
│       ├── ProductServiceImpl.java
│       └── SimpleProductCodeGenerator.java
├── controller/
│   └── MerchantProductController.java
└── repository/
    └── ProductRepository.java (新增 3 个查询方法)
```

---

## 9. 未实现的（符合任务要求）

| 项目 | 状态 | 说明 |
|------|------|------|
| Customer 商品接口 | ❌ | 后续 Sprint 实现 |
| Admin 审核接口 | ❌ | 后续 Sprint 实现 |
| Inventory 操作 | ❌ | 后续 Sprint 实现 |
| Order 模块 | ❌ | 不涉及 |
| Search 模块 | ❌ | 不涉及 |
| MQ 事件 | ❌ | 不涉及 |

---

> **文档版本:** v1.0  
> **最后更新:** 2026-07-26  
> **下一步:** Sprint 9 Step 2B — Customer 商品展示 API