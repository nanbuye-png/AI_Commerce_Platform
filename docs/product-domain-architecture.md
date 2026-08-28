# Product Domain 架构设计

> **文档版本:** v1.0  
> **最后更新:** 2026-07-26  
> **状态:** 设计阶段  
> **对应 Sprint:** Sprint 9 Step 0 — Product Domain 架构设计

---

## 目录

1. [领域模型 (Domain Model)](#一领域模型-domain-model)
2. [数据库表设计 (Database Schema)](#二数据库表设计-database-schema)
3. [Entity 关系与聚合根](#三entity-关系与聚合根)
4. [级联策略 (Cascade Strategy)](#四级联策略-cascade-strategy)
5. [软删除策略 (Soft Delete Strategy)](#五软删除策略-soft-delete-strategy)
6. [Backend Package 结构](#六backend-package-结构)
7. [商品 API 设计](#七商品-api-设计)
8. [权限矩阵 (Permission Matrix)](#八权限矩阵-permission-matrix)

---

## 一、领域模型 (Domain Model)

### 1.1 Category（商品分类）

多级树形分类结构，支持无限层级。根节点的 `parent_id = 0`。

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 主键 |
| parentId | Long | 自关联父分类 ID，0 = 根节点 |
| categoryName | String | 分类名称 |
| sort | Integer | 排序权重（升序） |
| level | Integer | 层级（1=一级，2=二级，依此类推） |
| createdTime | LocalDateTime | — |
| updatedTime | LocalDateTime | — |
| deleted | Boolean | 软删除标志 |

### 1.2 Product（商品 SPU）

商品标准信息主体，是 Product Domain 的**聚合根**。

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 主键 |
| merchantId | Long | 外键 → Merchant |
| storeId | Long | 外键 → Store |
| categoryId | Long | 外键 → Category |
| productName | String | 商品名称（标题） |
| description | String | 商品简要描述 |
| brand | String | 品牌 |
| status | ProductStatus | 商品状态枚举 |
| salesCount | Integer | 销量（冗余字段，定时同步） |
| createdTime | LocalDateTime | — |
| updatedTime | LocalDateTime | — |
| deleted | Boolean | 软删除标志 |

**ProductStatus 枚举：**

| 值 | 说明 |
|-----|------|
| DRAFT | 草稿（商家编辑中，未提交） |
| PENDING_REVIEW | 待审核（商家提交审核） |
| REJECTED | 审核驳回 |
| ON_SHELF | 已上架（在售） |
| OFF_SHELF | 已下架（手动下架） |
| ARCHIVED | 已归档（不可恢复的删除状态） |

> **关于审核状态的设计考量：** PENDING_REVIEW / REJECTED 仅在平台启用商品审核流程时需要。若平台初期不启用审核，商家可直接将商品状态设置为 ON_SHELF。是否启用审核由 Admin 后台开关控制。

### 1.3 ProductImage（商品图片）

商品图片，隶属于 Product。支持多图，其中一张为首图。

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 主键 |
| productId | Long | 外键 → Product |
| imageType | ImageType | 图片类型 |
| url | String | 图片 URL |
| sort | Integer | 排序权重 |
| isCover | Boolean | 是否首图 |
| createdTime | LocalDateTime | — |
| deleted | Boolean | 软删除标志 |

**ImageType 枚举：**

| 值 | 说明 |
|-----|------|
| MAIN | 商品主图（展示在商品列表） |
| DETAIL | 商品详情图 |
| SKU | 规格属性图（关联特定 SKU） |

### 1.4 ProductSpec（商品规格/属性模板）

定义商品的可选规格维度（如颜色、尺寸、存储容量等）。

> **设计说明：** 为简化首期实现，ProductSpec 作为 Product 的值对象（Value Object），存储在 Product 表中或独立表中。首期采用**独立表**存储，方便后期扩展规格体系。

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 主键 |
| productId | Long | 外键 → Product |
| specName | String | 规格名称（如 "颜色"、"尺寸"、"存储容量"） |
| specValues | JSON | 可选项列表，如 `["黑色","白色","蓝色"]` |
| sort | Integer | 排序权重 |
| createdTime | LocalDateTime | — |
| updatedTime | LocalDateTime | — |
| deleted | Boolean | 软删除标志 |

### 1.5 ProductSku（商品 SKU）

库存量单位。Product 的每一个具体售卖规格。

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 主键 |
| productId | Long | 外键 → Product |
| skuCode | String | SKU 编码（全局唯一，业务可读） |
| attributesJson | JSON | 规格属性映射，如 `{"颜色":"黑色","存储容量":"128G"}` |
| price | BigDecimal | 售价 |
| originalPrice | BigDecimal | 原价/划线价 |
| weight | BigDecimal | 重量（kg，用于运费计算） |
| status | SkuStatus | ACTIVE / DISABLED |
| salesCount | Integer | SKU 级别销量 |
| createdTime | LocalDateTime | — |
| updatedTime | LocalDateTime | — |
| deleted | Boolean | 软删除标志 |

**SkuStatus 枚举：**

| 值 | 说明 |
|-----|------|
| ACTIVE | 启用（可售） |
| DISABLED | 禁用（不可售） |

> **库存说明：** ProductSku **不存储库存数量**。实际可售库存统一由 `inventory` 表管理。查询商品 SKU 库存时需要 JOIN inventory 表获取 `available_stock`。

---

## 二、数据库表设计 (Database Schema)

### 2.1 表结构总览

```
commerce-platform (MySQL 8.0)
├── category            # 商品分类
├── product             # 商品 SPU
├── product_image       # 商品图片
├── product_spec        # 商品规格模板
├── product_sku         # 商品 SKU
└── inventory           # 库存（库存域，强关联）
    └── inventory_record # 库存流水
```

### 2.2 `category` — 商品分类

| 列名 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 主键 |
| parent_id | BIGINT | NOT NULL, DEFAULT 0 | 父分类 ID，0=根节点 |
| category_name | VARCHAR(64) | NOT NULL | 分类名称 |
| sort | INT | NOT NULL, DEFAULT 0 | 排序权重 |
| level | INT | NOT NULL, DEFAULT 1 | 层级 |
| created_time | DATETIME | NOT NULL | — |
| updated_time | DATETIME | NOT NULL | — |
| deleted | TINYINT(1) | NOT NULL, DEFAULT 0 | 软删除标志 |

**索引：**

| 索引名 | 列 | 类型 |
|--------|-----|------|
| idx_parent_id | parent_id | 普通索引 |
| idx_level | level | 普通索引 |

### 2.3 `product` — 商品 SPU

| 列名 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 主键 |
| merchant_id | BIGINT | NOT NULL | → merchant_account(id) |
| store_id | BIGINT | NOT NULL | → store(id) |
| category_id | BIGINT | NOT NULL | → category(id) |
| product_name | VARCHAR(256) | NOT NULL | 商品名称 |
| description | TEXT | — | 商品简要描述 |
| brand | VARCHAR(64) | — | 品牌 |
| status | VARCHAR(20) | NOT NULL | DRAFT / PENDING_REVIEW / REJECTED / ON_SHELF / OFF_SHELF / ARCHIVED |
| sales_count | INT | NOT NULL, DEFAULT 0 | 销量（冗余，定时同步） |
| created_time | DATETIME | NOT NULL | — |
| updated_time | DATETIME | NOT NULL | — |
| deleted | TINYINT(1) | NOT NULL, DEFAULT 0 | 软删除标志 |

**索引：**

| 索引名 | 列 | 类型 |
|--------|-----|------|
| idx_merchant_id | merchant_id | 普通索引 |
| idx_store_id | store_id | 普通索引 |
| idx_category_id | category_id | 普通索引 |
| idx_merchant_status | (merchant_id, status) | 复合索引 |
| idx_status_created | (status, created_time) | 复合索引 |
| idx_product_name | product_name | FULLTEXT 全文索引（基础搜索） |

### 2.4 `product_image` — 商品图片

| 列名 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 主键 |
| product_id | BIGINT | NOT NULL | → product(id) |
| image_type | VARCHAR(20) | NOT NULL | MAIN / DETAIL / SKU |
| url | VARCHAR(512) | NOT NULL | 图片 URL |
| sort | INT | NOT NULL, DEFAULT 0 | 排序权重 |
| is_cover | TINYINT(1) | NOT NULL, DEFAULT 0 | 是否首图 |
| created_time | DATETIME | NOT NULL | — |
| deleted | TINYINT(1) | NOT NULL, DEFAULT 0 | 软删除标志 |

**索引：**

| 索引名 | 列 | 类型 |
|--------|-----|------|
| idx_product_id | product_id | 普通索引 |
| idx_product_cover | (product_id, is_cover) | 复合索引（快速查找首图） |

### 2.5 `product_spec` — 商品规格模板

| 列名 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 主键 |
| product_id | BIGINT | NOT NULL | → product(id) |
| spec_name | VARCHAR(64) | NOT NULL | 规格名称 |
| spec_values | JSON | NOT NULL | 可选项列表，如 `["黑色","白色"]` |
| sort | INT | NOT NULL, DEFAULT 0 | 排序权重 |
| created_time | DATETIME | NOT NULL | — |
| updated_time | DATETIME | NOT NULL | — |
| deleted | TINYINT(1) | NOT NULL, DEFAULT 0 | 软删除标志 |

**索引：**

| 索引名 | 列 | 类型 |
|--------|-----|------|
| idx_product_id | product_id | 普通索引 |

### 2.6 `product_sku` — 商品 SKU

| 列名 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 主键 |
| product_id | BIGINT | NOT NULL | → product(id) |
| sku_code | VARCHAR(64) | NOT NULL | SKU 编码，全局唯一 |
| attributes_json | JSON | NOT NULL | 规格映射，如 `{"颜色":"黑色","存储容量":"128G"}` |
| price | DECIMAL(12,2) | NOT NULL | 售价 |
| original_price | DECIMAL(12,2) | — | 原价/划线价 |
| weight | DECIMAL(10,3) | DEFAULT 0 | 重量（kg） |
| status | VARCHAR(20) | NOT NULL | ACTIVE / DISABLED |
| sales_count | INT | NOT NULL, DEFAULT 0 | SKU 级别销量 |
| created_time | DATETIME | NOT NULL | — |
| updated_time | DATETIME | NOT NULL | — |
| deleted | TINYINT(1) | NOT NULL, DEFAULT 0 | 软删除标志 |

**索引：**

| 索引名 | 列 | 类型 |
|--------|-----|------|
| idx_product_id | product_id | 普通索引 |
| uk_sku_code | sku_code | 唯一索引 |
| idx_sku_status | (product_id, status) | 复合索引 |

### 2.7 `inventory` — 库存（库存域，强关联参考）

| 列名 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 主键 |
| sku_id | BIGINT | UNIQUE, NOT NULL | → product_sku(id)，唯一 |
| available_stock | INT | NOT NULL, DEFAULT 0 | 可销售库存 |
| locked_stock | INT | NOT NULL, DEFAULT 0 | 预占库存 |
| reserved_stock | INT | NOT NULL, DEFAULT 0 | 预留库存 |
| safety_stock | INT | NOT NULL, DEFAULT 0 | 安全库存阈值 |
| version | INT | NOT NULL, DEFAULT 0 | 乐观锁版本号 |
| created_time | DATETIME | NOT NULL | — |
| updated_time | DATETIME | NOT NULL | — |

> 完整 inventory 表设计参见 [database-design.md](./database-design.md) 库存域章节。

---

## 三、Entity 关系与聚合根

### 3.1 实体关系总图

```
Category (商品分类)
    │
    │ 1:N (category_id)
    ▼
Product (商品 SPU)  ←── [聚合根]
    │
    ├── 1:N → ProductImage (商品图片)
    │         image_type: MAIN / DETAIL / SKU
    │         is_cover: 是否首图
    │
    ├── 1:N → ProductSpec (规格模板)
    │         spec_name: "颜色"
    │         spec_values: ["黑色","白色"]
    │
    └── 1:N → ProductSku (SKU)
                  │
                  └── 1:1 → Inventory (库存)
```

### 3.2 聚合根 (Aggregate Root)

**Product 是 Product Domain 的聚合根。**

聚合规则：

| 聚合根 | 所属实体 | 说明 |
|--------|----------|------|
| **Product** | ProductImage | 图片隶属于商品，不独立存在 |
| **Product** | ProductSpec | 规格模板隶属于商品，不独立存在 |
| **Product** | ProductSku | SKU 隶属于商品，不独立存在 |
| — | Category | 独立聚合根（全局分类体系） |
| — | Inventory | 独立聚合根（库存域管理） |

**约束：**

1. 所有对 ProductImage / ProductSpec / ProductSku 的操作**必须通过 Product 聚合根进行**
2. 不允许直接跨过 Product 操作子实体
3. 聚合根保证内部数据一致性

### 3.3 跨域引用关系

| 源实体 | 目标实体 | 领域 | 引用方式 |
|--------|----------|------|----------|
| Product.merchant_id | Merchant | 商家域 | 外键 ID 引用（弱引用） |
| Product.store_id | Store | 商家域 | 外键 ID 引用（弱引用） |
| Product.category_id | Category | 商品域 | 外键 ID 引用 |
| ProductSku.id | Inventory.sku_id | 库存域 | 外键 ID 引用（弱引用） |

> **弱引用原则：** 跨域引用仅存 ID，不存 JPA Entity 强关联，避免跨域事务耦合。领域间通过 Service 层或事件驱动通信。

### 3.4 聚合边界内操作原则

| 操作 | 规则 |
|------|------|
| 创建商品 | 一个事务内创建 Product + ProductImage + ProductSpec + ProductSku + Inventory |
| 更新商品 | 通过 Product 聚合根统一管理子实体变更 |
| 删除商品 | 软删除 Product + 级联软删除子实体 |
| 查询商品 | 聚合根查询 + 按需加载子实体（懒加载） |

---

## 四、级联策略 (Cascade Strategy)

### 4.1 JPA Cascade 配置

| 父实体 | 子实体 | Cascade 类型 | 说明 |
|--------|--------|-------------|------|
| Product | ProductImage | ALL | 同生命周期，级联所有操作 |
| Product | ProductSpec | ALL | 同生命周期，级联所有操作 |
| Product | ProductSku | ALL | 同生命周期，级联所有操作 |

### 4.2 级联操作明细

| 操作 | Product | ProductImage | ProductSpec | ProductSku | Inventory |
|------|---------|-------------|-------------|------------|-----------|
| **创建商品** | INSERT | Cascade INSERT | Cascade INSERT | Cascade INSERT | 事务后单独 INSERT |
| **更新商品名称/描述** | UPDATE | — | — | — | — |
| **新增图片** | — | INSERT | — | — | — |
| **删除图片** | — | DELETE (软删) | — | — | — |
| **新增规格** | — | — | INSERT | — | — |
| **新增 SKU** | — | — | — | INSERT | 事务后 INSERT |
| **禁用 SKU** | — | — | — | UPDATE | — |
| **下架商品** | UPDATE | — | — | — | — |
| **删除商品 (软删)** | UPDATE | Cascade UPDATE | Cascade UPDATE | Cascade UPDATE | — |

### 4.3 Inventory 特殊处理

**Inventory 不通过 JPA Cascade 管理。** 原因：

1. Inventory 属于**库存域**（独立领域），虽然有 `sku_id` 外键关联 ProductSku，但不属于 Product 聚合
2. 库存操作具有高并发特性，需要独立的事务和乐观锁控制
3. Inventory 的生命周期由 StockDomainService 管理，而非 ProductService

**Inventory 创建时机：** ProductSku 持久化后，通过 `SkuCreatedEvent` 事件触发 Inventory 创建

```
ProductService.createProduct()
  └── ProductRepository.save(product)  // Cascade 保存 Product + ProductImage + ProductSpec + ProductSku
  └── ApplicationEventPublisher.publishEvent(new SkuCreatedEvent(skuIdList))
      └── @TransactionalEventListener(phase = AFTER_COMMIT)
          └── StockDomainService.initializeStock(skuId)
```

---

## 五、软删除策略 (Soft Delete Strategy)

### 5.1 通用规则

- 所有 Product Domain 实体（Category / Product / ProductImage / ProductSpec / ProductSku）均使用**逻辑删除**
- `deleted` 字段取值：`0 = 正常`，`1 = 删除`
- Category 由于是独立聚合根，不跟随 Product 级联删除
- Inventory 为库存域实体，不参与商品域的软删除

### 5.2 删除行为矩阵

| 实体 | 删除方式 | 级联影响 | 查询过滤 |
|------|----------|----------|----------|
| Category | 逻辑删除 `deleted=1` | 不级联 Product | 增删改查自动过滤 `deleted=0` |
| Product | 逻辑删除 `deleted=1` | 级联软删除 ProductImage / ProductSpec / ProductSku | 增删改查自动过滤 `deleted=0` |
| ProductImage | 逻辑删除 `deleted=1` | 无 | 增删改查自动过滤 `deleted=0` |
| ProductSpec | 逻辑删除 `deleted=1` | 无 | 增删改查自动过滤 `deleted=0` |
| ProductSku | 逻辑删除 `deleted=1` | 不操作 Inventory | 增删改查自动过滤 `deleted=0` |

### 5.3 删除限制

| 场景 | 限制 |
|------|------|
| **分类下有商品** | 不允许删除（需先迁移商品分类或删除商品） |
| **商品有订单关联** | 不允许硬删除，仅允许软删除 → ARCHIVED |
| **商品有库存** | 软删除时将商品状态改为 ARCHIVED，库存数据保留 |
| **SKU 有库存** | 不允许删除 SKU（需先清空库存） |

### 5.4 @SQLRestriction / @Where 注解

所有 Product Domain Entity 在 JPA 层面添加软删除过滤：

```java
@SQLRestriction("deleted = 0")
// 或 @Where(clause = "deleted = 0")
```

所有 Spring Data JPA 的 Repository 查询自动追加 `WHERE deleted = 0`。

### 5.5 删除方法命名规范

| 方法 | 说明 |
|------|------|
| `softDeleteById(Long id)` | 软删除：设置 `deleted=1` |
| `restoreById(Long id)` | 恢复：设置 `deleted=0` |
| `hardDeleteById(Long id)` | 物理删除：仅管理员后台使用，受限制 |

---

## 六、Backend Package 结构

### 6.1 包路径总览

```
com.commerce.platform
├── CommercePlatformApplication.java              # Spring Boot 启动类
│
├── common/                                       # 公共模块
│   ├── config/                                   # 全局配置
│   ├── exception/                                # 全局异常处理
│   │   ├── GlobalExceptionHandler.java
│   │   └── BusinessException.java
│   ├── response/                                 # 统一响应格式
│   │   ├── ApiResponse.java
│   │   └── PageResponse.java
│   ├── constant/                                 # 全局常量
│   └── util/                                     # 通用工具类
│
├── auth/                                         # 认证模块
│   ├── config/                                   # Security 配置
│   ├── jwt/                                      # JWT 工具
│   ├── filter/                                   # 鉴权过滤器
│   └── context/                                  # 用户上下文
│
├── product/                                      # ★ 商品模块（Product Domain）
│   ├── domain/                                   # 领域层
│   │   ├── entity/                               # JPA Entity
│   │   │   ├── Category.java
│   │   │   ├── Product.java
│   │   │   ├── ProductImage.java
│   │   │   ├── ProductSpec.java
│   │   │   └── ProductSku.java
│   │   ├── enums/                                # 枚举定义
│   │   │   ├── ProductStatus.java
│   │   │   ├── SkuStatus.java
│   │   │   └── ImageType.java
│   │   └── repository/                           # Repository 接口
│   │       ├── CategoryRepository.java
│   │       ├── ProductRepository.java
│   │       ├── ProductImageRepository.java
│   │       ├── ProductSpecRepository.java
│   │       └── ProductSkuRepository.java
│   │
│   ├── dto/                                      # 数据传输层（DTO）
│   │   ├── request/                              # 请求 DTO
│   │   │   ├── customer/                         # C 端请求
│   │   │   │   ├── ProductListQuery.java
│   │   │   │   └── ProductDetailQuery.java
│   │   │   ├── merchant/                         # 商家端请求
│   │   │   │   ├── ProductCreateRequest.java
│   │   │   │   ├── ProductUpdateRequest.java
│   │   │   │   ├── ProductSkuRequest.java
│   │   │   │   └── ProductStatusRequest.java
│   │   │   └── admin/                            # 管理端请求
│   │   │       └── ProductAuditRequest.java
│   │   └── response/                             # 响应 DTO
│   │       ├── customer/                         # C 端响应
│   │       │   ├── CustomerProductVO.java
│   │       │   ├── CustomerSkuVO.java
│   │       │   └── CategoryTreeNodeVO.java
│   │       ├── merchant/                         # 商家端响应
│   │       │   ├── MerchantProductVO.java
│   │       │   └── MerchantSkuVO.java
│   │       └── admin/                            # 管理端响应
│   │           └── AdminProductVO.java
│   │
│   ├── service/                                  # 服务层
│   │   ├── ProductService.java                   # 商品聚合根服务（接口）
│   │   ├── impl/                                 # 实现类
│   │   │   └── ProductServiceImpl.java
│   │   ├── CategoryService.java                  # 分类服务
│   │   ├── impl/
│   │   │   └── CategoryServiceImpl.java
│   │   └── delegate/                             # 三方服务委托
│   │       └── InventoryDelegate.java            # 库存服务调用
│   │
│   ├── controller/                               # 控制器层
│   │   ├── CustomerProductController.java        # C 端商品接口
│   │   ├── MerchantProductController.java        # 商家端商品接口
│   │   └── AdminProductController.java           # 管理端商品接口
│   │
│   ├── mq/                                       # 消息队列
│   │   ├── event/                                # 事件定义
│   │   │   └── SkuCreatedEvent.java
│   │   └── producer/                             # 事件发布
│   │       └── ProductEventProducer.java
│   │
│   └── mapstruct/                                # 对象映射
│       ├── ProductConvert.java
│       └── CategoryConvert.java
│
├── order/                                        # 订单模块（后续 Sprint）
├── payment/                                      # 支付模块（后续 Sprint）
├── merchant/                                     # 商家模块（后续 Sprint）
└── inventory/                                    # 库存模块（后续 Sprint）
```

### 6.2 分层职责

| 层级 | 包路径 | 职责 | 依赖 |
|------|--------|------|------|
| **Entity** | `.domain.entity` | JPA 实体定义，ORM 映射 | 无 |
| **Repository** | `.domain.repository` | 数据访问接口（Spring Data JPA） | Entity |
| **DTO** | `.dto.request/response` | 数据传输对象，分离内外部模型 | 无 |
| **Service** | `.service` | 业务逻辑编排，事务管理 | Repository, DTO |
| **Controller** | `.controller` | HTTP 接口暴露，参数校验 | Service, DTO |
| **MQ** | `.mq` | 事件驱动，解耦跨域操作 | Service |
| **MapStruct** | `.mapstruct` | Entity ↔ DTO 映射 | Entity, DTO |

### 6.3 依赖方向

```
Controller → Service → Repository → Entity
                ↓
            MQ Event (跨域通知)
                ↓
           InventoryDelegate
```

**严格分层：** Controller 不直接调用 Repository。跨域调用通过事件或 Delegate 接口（interface + impl 分离），不直接依赖其他 Domain 的 Repository。

---

## 七、商品 API 设计

### 7.1 API 分组总览

| 分组 | 基础路径 | 角色 | 说明 |
|------|----------|------|------|
| Customer | `/api/v1/products` | USER / 公开 | C 端商品浏览、搜索、分类 |
| Merchant | `/api/v1/merchant/products` | MERCHANT | 商家商品管理（增删改查） |
| Admin | `/api/v1/admin/products` | ADMIN | 平台商品管控、审核 |

---

### 7.2 Customer 端 API（公开 / USER）

**基础路径：** `/api/v1/products`

#### GET /api/v1/products — 商品列表

- **权限：** 公开
- **分页：** 支持

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| page | int | 否 | 默认 1 |
| pageSize | int | 否 | 默认 20，最大 100 |
| keyword | string | 否 | 搜索关键词 |
| categoryId | int | 否 | 分类 ID |
| minPrice | decimal | 否 | 最低价格 |
| maxPrice | decimal | 否 | 最高价格 |
| brand | string | 否 | 品牌筛选 |
| sortBy | string | 否 | `price` / `sales` / `created_time` |
| sortOrder | string | 否 | `asc` / `desc` |

**响应：** 分页列表，每项包含商品 ID、名称、首图 URL、最低价、最高价、销量。

#### GET /api/v1/products/{id} — 商品详情

- **权限：** 公开

**响应：** 商品基本信息 + SKU 列表 + 图片列表 + 规格模板 + 实时库存

```json
{
  "code": 0,
  "data": {
    "id": 2001,
    "name": "无线蓝牙耳机",
    "description": "高品质降噪蓝牙耳机",
    "brand": "品牌名",
    "category_id": 10,
    "category_name": "数码产品",
    "store_id": 101,
    "store_name": "数码旗舰店",
    "images": [
      { "url": "https://...", "image_type": "MAIN", "is_cover": true, "sort": 1 },
      { "url": "https://...", "image_type": "DETAIL", "is_cover": false, "sort": 2 }
    ],
    "specs": [
      { "name": "颜色", "values": ["黑色","白色"] },
      { "name": "存储容量", "values": ["128G","256G"] }
    ],
    "skus": [
      {
        "id": 3001,
        "sku_code": "SKU2026001",
        "attributes": { "颜色": "黑色", "存储容量": "128G" },
        "price": 199.00,
        "original_price": 299.00,
        "available_stock": 50,
        "status": "ACTIVE"
      }
    ],
    "status": "ON_SHELF",
    "sales_count": 1520,
    "created_time": "2026-01-15T10:00:00"
  }
}
```

#### GET /api/v1/categories — 分类树

- **权限：** 公开
- **响应：** 树形结构分类列表

```json
{
  "code": 0,
  "data": [
    {
      "id": 1,
      "name": "数码产品",
      "parent_id": 0,
      "level": 1,
      "children": [
        {
          "id": 10,
          "name": "耳机",
          "parent_id": 1,
          "level": 2,
          "children": [...]
        }
      ]
    }
  ]
}
```

---

### 7.3 Merchant 端 API（MERCHANT）

**基础路径：** `/api/v1/merchant/products`

**权限要求：** MERCHANT 角色，且只能操作**本店铺**的商品。

#### GET /api/v1/merchant/products — 商家商品列表

- **功能：** 查看本店铺所有商品
- **支持：** 分页 + 状态筛选（DRAFT / PENDING_REVIEW / ON_SHELF / OFF_SHELF）

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| status | string | 否 | 商品状态筛选 |

#### POST /api/v1/merchant/products — 创建商品

- **功能：** 新增商品（含图片、规格、SKU）

```json
{
  "product_name": "无线蓝牙耳机",
  "description": "商品简要描述",
  "brand": "品牌名",
  "category_id": 10,
  "images": [
    { "url": "https://...", "image_type": "MAIN", "is_cover": true, "sort": 1 },
    { "url": "https://...", "image_type": "DETAIL", "is_cover": false, "sort": 2 }
  ],
  "specs": [
    { "spec_name": "颜色", "spec_values": ["黑色","白色"] }
  ],
  "skus": [
    {
      "sku_code": "SKU2026001",
      "attributes": { "颜色": "黑色", "存储容量": "128G" },
      "price": 199.00,
      "original_price": 299.00,
      "weight": 0.25,
      "stock": 100
    }
  ]
}
```

**响应：** 创建成功的商品 ID 和状态。

#### PUT /api/v1/merchant/products/{id} — 更新商品

- **功能：** 修改商品基本信息

```json
{
  "product_name": "string（选填）",
  "description": "string（选填）",
  "brand": "string（选填）",
  "category_id": 10
}
```

#### PUT /api/v1/merchant/products/{id}/status — 更新商品状态

- **功能：** 上架 / 下架 / 提交审核

```json
{
  "status": "ON_SHELF",
  "reason": "选填"
}
```

#### DELETE /api/v1/merchant/products/{id} — 删除商品

- **功能：** 软删除商品（状态变为 ARCHIVED）
- **限制：** 有未完成订单的商品不允许删除

#### GET /api/v1/merchant/products/{id} — 商品详情（商家视角）

**额外字段：** 包含 SKU 的库存信息（available_stock / locked_stock / reserved_stock）

#### PUT /api/v1/merchant/products/{id}/skus — 批量更新 SKU

```json
{
  "skus": [
    {
      "sku_id": 3001,
      "price": 189.00,
      "original_price": 299.00,
      "status": "ACTIVE"
    }
  ]
}
```

#### PUT /api/v1/merchant/products/{productId}/images — 批量更新图片

```json
{
  "images": [
    { "url": "https://...", "image_type": "MAIN", "is_cover": true, "sort": 1 }
  ]
}
```

---

### 7.4 Admin 端 API（ADMIN）

**基础路径：** `/api/v1/admin/products`

**权限要求：** ADMIN 角色。Admin 可查看和管理**全平台**商品。

#### GET /api/v1/admin/products — 全平台商品列表

- **功能：** 查看所有商家的商品
- **支持：** 分页 + 多维筛选（商家、分类、状态、时间范围）

| 参数 | 类型 | 说明 |
|------|------|------|
| merchant_id | int | 按商家筛选 |
| status | string | 商品状态 |
| category_id | int | 分类 |
| start_time | string | 创建时间起始 |
| end_time | string | 创建时间结束 |

#### GET /api/v1/admin/products/{id} — 商品详情（管理员视角）

**额外字段：** 含商家信息、审核日志、库存数据

#### GET /api/v1/admin/products/review — 待审核商品列表

- **功能：** 获取所有 PENDING_REVIEW 状态的商品

#### PUT /api/v1/admin/products/{id}/audit — 商品审核

- **功能：** 审核通过或驳回

```json
{
  "action": "APPROVED",
  "remark": "审核通过"
}
```

**审核后状态流转：**

| 审核动作 | 原状态 | 目标状态 |
|----------|--------|----------|
| 通过 (APPROVED) | PENDING_REVIEW | ON_SHELF |
| 驳回 (REJECTED) | PENDING_REVIEW | REJECTED |

#### PUT /api/v1/admin/products/{id}/force-offline — 强制下架

- **功能：** 平台强制下架违规商品

```json
{
  "reason": "商品信息违规（必填）"
}
```

#### DELETE /api/v1/admin/products/{id} — 强制删除商品

- **功能：** 物理删除（高危操作，需二次确认 + 操作日志）

---

## 八、权限矩阵 (Permission Matrix)

### 8.1 角色定义

| 角色编码 | 角色名 | 说明 |
|----------|--------|------|
| USER | C 端用户 | 普通消费者 |
| MERCHANT | 商家 | 入驻商家员工 |
| ADMIN | 平台管理员 | 平台运营人员 |
| ANONYMOUS | 未登录用户 | 游客（仅可访问公开接口） |

### 8.2 商品模块权限矩阵

| 功能 | API | ANONYMOUS | USER | MERCHANT | ADMIN |
|------|-----|-----------|------|----------|-------|
| **商品分类** | | | | | |
| 查看分类树 | `GET /categories` | ✅ | ✅ | ✅ | ✅ |
| 新增一级分类 | `POST /admin/categories` | ❌ | ❌ | ❌ | ✅ |
| 修改分类 | `PUT /admin/categories/{id}` | ❌ | ❌ | ❌ | ✅ |
| 删除分类 | `DELETE /admin/categories/{id}` | ❌ | ❌ | ❌ | ✅ |
| **C 端商品** | | | | | |
| 商品列表 | `GET /products` | ✅ | ✅ | ✅ | ✅ |
| 商品详情 | `GET /products/{id}` | ✅ | ✅ | ✅ | ✅ |
| 记录浏览历史 | `POST /products/{id}/view` | ❌ | ✅ | ❌ | ❌ |
| 收藏/取消收藏 | `POST /products/{id}/favorite` | ❌ | ✅ | ❌ | ❌ |
| **商家商品管理** | | | | | |
| 查看本店商品列表 | `GET /merchant/products` | ❌ | ❌ | ✅ | ✅ |
| 查看本店商品详情 | `GET /merchant/products/{id}` | ❌ | ❌ | ✅ | ✅ |
| 创建商品 | `POST /merchant/products` | ❌ | ❌ | ✅ | ✅ |
| 更新商品 | `PUT /merchant/products/{id}` | ❌ | ❌ | ✅ (仅本店) | ✅ |
| 更新商品状态 | `PUT /merchant/products/{id}/status` | ❌ | ❌ | ✅ (仅本店) | ✅ |
| 删除商品(软删) | `DELETE /merchant/products/{id}` | ❌ | ❌ | ✅ (仅本店) | ✅ |
| 批量更新 SKU | `PUT /merchant/products/{id}/skus` | ❌ | ❌ | ✅ (仅本店) | ✅ |
| 批量更新图片 | `PUT /merchant/products/{productId}/images` | ❌ | ❌ | ✅ (仅本店) | ✅ |
| **平台商品管控** | | | | | |
| 全平台商品列表 | `GET /admin/products` | ❌ | ❌ | ❌ | ✅ |
| 平台商品详情 | `GET /admin/products/{id}` | ❌ | ❌ | ❌ | ✅ |
| 待审核商品列表 | `GET /admin/products/review` | ❌ | ❌ | ❌ | ✅ |
| 商品审核 | `PUT /admin/products/{id}/audit` | ❌ | ❌ | ❌ | ✅ |
| 强制下架 | `PUT /admin/products/{id}/force-offline` | ❌ | ❌ | ❌ | ✅ |
| 强制删除(物理) | `DELETE /admin/products/{id}` | ❌ | ❌ | ❌ | ✅ (高危) |
| **分类管理** | | | | | |
| 全部分类管理 | `CRUD /admin/categories/**` | ❌ | ❌ | ❌ | ✅ |

### 8.3 数据权限（行级过滤）

| 角色 | 数据可见范围 |
|------|-------------|
| USER | 所有状态为 ON_SHELF 的商品 |
| MERCHANT | 本店铺所有商品（含 DRAFT / OFF_SHELF 等） |
| ADMIN | 全平台所有商品（含已删除，但不包含物理删除） |

### 8.4 权限校验位置

```
┌─────────────────────────────────────────────────────────┐
│  第一层: API Gateway                                    │
│  - 校验 JWT Token 合法性                                │
│  - 校验角色是否有权访问该 API 路径                       │
│  - 白名单路由 (GET /products, GET /categories) 放行     │
├─────────────────────────────────────────────────────────┤
│  第二层: Spring Security (Backend Method Security)      │
│  - @PreAuthorize("hasRole('MERCHANT')")                 │
│  - 方法级别的角色校验                                  │
├─────────────────────────────────────────────────────────┤
│  第三层: Service 层（业务级数据权限）                    │
│  - MerchantProductServiceImpl:                           │
│    - 查询时自动拼接 merchant_id = 当前商家ID            │
│    - 更新/删除时校验当前商家 = 商品所属商家             │
│  - AdminProductServiceImpl:                             │
│    - 无条件查询全部数据                                  │
│    - 物理删除记录操作日志                                │
└─────────────────────────────────────────────────────────┘
```

### 8.5 权限注解设计建议

```java
// Merchant 端 — 自动注入商家 ID 到查询
@PreAuthorize("hasRole('MERCHANT')")
@PostMapping("/merchant/products")
public ApiResponse<Long> createProduct(@Valid @RequestBody ProductCreateRequest request) {
    Long merchantId = SecurityContextHolder.getCurrentMerchantId();
    // 自动注入当前商家 ID
}

// Admin 端 — 仅 ADMIN 可访问
@PreAuthorize("hasRole('ADMIN')")
@PutMapping("/admin/products/{id}/audit")
public ApiResponse<Void> auditProduct(@PathVariable Long id, @Valid @RequestBody ProductAuditRequest request) {
    // 审核逻辑
}
```

---

## 附录

### A. 状态流转图

```
                       ┌──────────────┐
                       │    DRAFT     │  ← 商家新建商品
                       └──────┬───────┘
                              │
                    ┌─────────┴──────────┐
                    │  (开启审核?)        │
                    ▼                    ▼
             ┌──────────────┐    ┌──────────────┐
             │ PENDING_     │    │   ON_SHELF    │  ← 不开启审核时
             │ REVIEW      │    │   (直接上架)   │
             └──────┬───────┘    └──────┬────────┘
                    │                   │
          ┌─────────┴─────────┐         │
          ▼                   ▼         │
   ┌──────────┐        ┌──────────┐     │
   │APPROVED  │        │ REJECTED │     │
   │→ON_SHELF │        └──────────┘     │
   └──────────┘              │           │
                             │ 重新编辑   │
                             └──→ DRAFT  │
                                         │
                    ┌────────────────────┘
                    ▼
             ┌──────────────┐
             │   ON_SHELF    │
             └──────┬───────┘
                    │
           ┌────────┴────────┐
           ▼                  ▼
    ┌──────────┐       ┌──────────┐
    │OFF_SHELF │       │ ARCHIVED │  ← 软删除
    │(手动下架)│       └──────────┘
    └──────────┘
         │
         └──→ ON_SHELF (重新上架)
```

### B. 商品创建时序

```
Merchant                    Controller                  Service                   Repository
   │                            │                          │                        │
   │ POST /merchant/products    │                          │                        │
   │ ───────────────────────→   │                          │                        │
   │                            │ validate & convert        │                        │
   │                            │ ──────→ ProductService    │                        │
   │                            │         .createProduct()  │                        │
   │                            │                          │                        │
   │                            │                          │── save(Product) ─────→ │
   │                            │                          │── save(Image) ───────→ │
   │                            │                          │── save(Spec) ─────────→ │
   │                            │                          │── save(Sku) ──────────→ │
   │                            │                          │                        │
   │                            │                          │ Publish SkuCreatedEvent │
   │                            │                          │──→ Inventory Service    │
   │                            │                          │     .initStock()       │
   │                            │                          │                        │
   │  ←── ApiResponse(productId)│                          │                        │
```

### C. 设计决策记录 (ADR)

| 编号 | 决策 | 理由 |
|------|------|------|
| ADR-001 | Product 作为聚合根 | 商品图片、规格、SKU 与 Product 同生命周期，通过 Product 统一管理 |
| ADR-002 | Inventory 不纳入商品聚合 | 库存具有高并发独立操作特性，属于库存域，需独立事务和乐观锁 |
| ADR-003 | 商品规格使用独立表而非 JSON | 方便后期扩展（规格搜索、SKU 生成模板），JSON 在 Product 中也可保留作为冗余 |
| ADR-004 | 商品状态增加 PENDING_REVIEW | 为平台审核流程预留，初期可关闭审核开关直接上架 |
| ADR-005 | SKU 编码全局唯一 | 方便 ERP 对接、物流对接、库存对账 |
| ADR-006 | sales_count 在 Product 和 Sku 冗余 | 避免统计查询时大表 JOIN 聚合，定时任务从订单表同步 |
| ADR-007 | Merchant 端数据权限在 Service 层控制 | 不能依赖 API Gateway 的路由前缀做商户隔离，需在业务层二次校验 |
| ADR-008 | Admin 强制删除需二次确认 | 物理删除为高危操作，需操作日志记录 + 弹窗确认 |

---

> **文档版本:** v1.0  
> **最后更新:** 2026-07-26  
> **状态:** 设计阶段 — 仅定义架构设计，不创建 Entity/Repository/Service/Controller  
> **下一步:** Sprint 9 Step 1 — 商品模块代码实现