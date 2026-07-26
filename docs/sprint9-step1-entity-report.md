# Sprint 9 Step 1 — Entity 实现报告

> **日期:** 2026-07-26  
> **状态:** ✅ 完成

---

## 1. 新建 Entity

| Entity | 表名 | 文件路径 | 状态 |
|--------|------|----------|------|
| Category | `category` | `product/entity/Category.java` | ✅ |
| Product | `product` | `product/entity/Product.java` | ✅ |
| ProductImage | `product_image` | `product/entity/ProductImage.java` | ✅ |
| ProductSpec | `product_spec` | `product/entity/ProductSpec.java` | ✅ |
| ProductSku | `product_sku` | `product/entity/ProductSku.java` | ✅ |

### Entity 继承关系

```
BaseEntity (common/entity/BaseEntity)
  ├── id (Long, @Id @GeneratedValue IDENTITY)
  ├── createdTime (LocalDateTime)
  └── updatedTime (LocalDateTime)
       │
       ├── Category
       ├── Product  ← [聚合根, @Version 乐观锁]
       ├── ProductImage  (@ManyToOne → Product)
       ├── ProductSpec   (@ManyToOne → Product)
       └── ProductSku    (@ManyToOne → Product)
```

---

## 2. Repository

| Repository | 对应 Entity | 文件路径 | 状态 |
|------------|-------------|----------|------|
| CategoryRepository | Category | `product/repository/CategoryRepository.java` | ✅ |
| ProductRepository | Product | `product/repository/ProductRepository.java` | ✅ |
| ProductImageRepository | ProductImage | `product/repository/ProductImageRepository.java` | ✅ |
| ProductSpecRepository | ProductSpec | `product/repository/ProductSpecRepository.java` | ✅ |
| ProductSkuRepository | ProductSku | `product/repository/ProductSkuRepository.java` | ✅ |

所有 Repository 继承 `JpaRepository<Entity, Long>`，仅实现基础 CRUD，无自定义复杂查询。

---

## 3. Enum

| Enum | 值 | 文件路径 |
|------|----|----------|
| ProductStatus | `DRAFT`, `PENDING_REVIEW`, `REJECTED`, `ON_SHELF`, `OFF_SHELF`, `ARCHIVED` | `product/enums/ProductStatus.java` |
| ImageType | `MAIN`, `DETAIL`, `SKU` | `product/enums/ImageType.java` |

**持久化策略：** 均使用 `@Enumerated(EnumType.STRING)`（非 ORDINAL）。

---

## 4. Entity Relationship

```
Product (聚合根)
  │
  ├── 1:N → ProductImage
  │       @ManyToOne(fetch = LAZY) → Product
  │       cascade = ALL, orphanRemoval = true
  │       @OrderBy("sort ASC")
  │
  ├── 1:N → ProductSpec
  │       @ManyToOne(fetch = LAZY) → Product
  │       cascade = ALL, orphanRemoval = true
  │       @OrderBy("sort ASC")
  │
  └── 1:N → ProductSku
          @ManyToOne(fetch = LAZY) → Product
          cascade = ALL, orphanRemoval = true
```

**软删除控制：** 所有实体添加 `@SQLRestriction("deleted = false")` 注解，JPA 查询自动过滤已删除数据。

**Category 为独立聚合根**（不与 Product 建立 JPA 强关联，仅通过 `category_id` 外键 ID 弱引用）。

---

## 5. productCode 实现情况

| 属性 | 实现 |
|------|------|
| 字段名 | `productCode` |
| 数据库列 | `product_code` |
| 唯一约束 | `@Column(unique = true)` |
| 非空 | `nullable = false` |
| 不可修改 | `updatable = false` |
| 长度 | `length = 64` |
| 编码策略 | 当前仅定义字段，预留统一编码生成器（后续 Sprint 实现） |

---

## 6. @Version 乐观锁实现情况

```java
@Version
@Column(nullable = false)
@Builder.Default
private Long version = 0L;
```

| 属性 | 值 |
|------|-----|
| 注解 | `@Version`（JPA 标准） |
| 类型 | `Long` |
| 数据库默认值 | `0` |
| 作用 | 防止多个商家后台同时编辑商品导致数据覆盖 |

---

## 7. 编译结果

| 检查项 | 结果 |
|--------|------|
| Maven Compile | ✅ 成功（无错误） |
| Hibernate Mapping 错误 | ❌ 无 |
| 循环依赖 | ❌ 无 |
| Package 命名冲突 | ❌ 无 |

---

## 8. 数据库初始化

| 配置项 | 值 |
|--------|-----|
| `ddl-auto` | `validate`（生产模式） |
| Flyway | 已启用，新增 `V2__create_product_tables.sql` |
| 表结构 | 5 张新表（category / product / product_image / product_spec / product_sku）|
| 索引 | 12 个索引（含 1 个唯一索引 `uk_sku_code`）|
| 外键 | `product_image.product_id → product.id`（CASCADE DELETE）|
| 外键 | `product_spec.product_id → product.id`（CASCADE DELETE）|
| 外键 | `product_sku.product_id → product.id`（CASCADE DELETE）|

---

## 9. 未实现的（符合任务要求）

| 项目 | 状态 | 说明 |
|------|------|------|
| Service | ❌ 不实现 | 后续 Sprint 实现 |
| Controller | ❌ 不实现 | 后续 Sprint 实现 |
| 商品 CRUD | ❌ 不实现 | 后续 Sprint 实现 |
| MapStruct | ❌ 不引入 | 项目未使用 MapStruct |
| 订单模块修改 | ❌ 不修改 | 不涉及 |
| 库存模块修改 | ❌ 不修改 | 不涉及 |
| 支付模块修改 | ❌ 不修改 | 不涉及 |

---

## 10. 文件清单

```
backend/commerce-platform/src/main/java/com/commerce/platform/product/
├── enums/
│   ├── ImageType.java
│   └── ProductStatus.java
├── entity/
│   ├── Category.java
│   ├── Product.java
│   ├── ProductImage.java
│   ├── ProductSpec.java
│   └── ProductSku.java
└── repository/
    ├── CategoryRepository.java
    ├── ProductRepository.java
    ├── ProductImageRepository.java
    ├── ProductSpecRepository.java
    └── ProductSkuRepository.java

backend/commerce-platform/src/main/resources/db/migration/
└── V2__create_product_tables.sql
```

---

> **文档版本:** v1.0  
> **最后更新:** 2026-07-26  
> **下一步:** Sprint 9 Step 2 — 商品模块 Service + Controller 实现