# Sprint 9 Step 2B — Customer Product Browse 实现报告

> **日期:** 2026-07-26  
> **状态:** ✅ 完成

---

## 1. Customer DTO

| DTO | 位置 | 说明 |
|-----|------|------|
| `ProductSearchRequest` | `product/dto/customer/` | 搜索参数：page/size/keyword/categoryId/sortBy/sortOrder |
| `ProductCardResponse` | `product/dto/customer/` | 商品卡片（列表用）：含 minPrice/maxPrice/coverImage，无内部字段 |
| `ProductDetailResponse` | `product/dto/customer/` | 商品详情：含 images/specs/skus 嵌套 VO，无 merchantId/审核信息 |
| `CategoryTreeResponse` | `product/dto/customer/` | 递归树结构：children 自引用 |

Customer DTO 独立管理，不复用 Merchant DTO，不暴露内部字段。

---

## 2. CustomerProductService

| 方法 | @Transactional | 说明 |
|------|----------------|------|
| `listProducts()` | ✅ readOnly | 仅 ON_SHELF，支持 keyword + categoryId + sort |
| `getProductDetail()` | ✅ readOnly | 仅 ON_SHELF，非 ON_SHELF 抛 30005 |
| `getCategoryTree()` | ✅ readOnly | 递归构建树形分类（从 root 开始） |

**关键约束：**
- ✅ 仅返回 `ProductStatus.ON_SHELF` 的商品
- ✅ DRAFT / PENDING_REVIEW / REJECTED / OFF_SHELF / ARCHIVED 全部不可见
- ✅ 详情页仅允许查看 ON_SHELF 商品

---

## 3. Controller API

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/products` | 商品列表（分页 + 筛选 + 排序） |
| GET | `/api/products/{id}` | 商品详情 |
| GET | `/api/categories/tree` | 分类树 |

**权限：** 公开接口（无需认证），SecurityConfig 中已配置 `permitAll` 路径。

---

## 4. 商品列表

```
GET /api/products?page=1&size=20&keyword=耳机&categoryId=10&sortBy=price&sortOrder=asc
```

| 参数 | 默认值 | 说明 |
|------|--------|------|
| page | 1 | 页码 |
| size | 20 | 每页大小 |
| keyword | — | 商品名称模糊搜索 |
| categoryId | — | 分类筛选 |
| sortBy | createdTime | 排序字段：createdTime / price / sales |
| sortOrder | desc | asc / desc |

**返回：** `Result<PageResult<ProductCardResponse>>`

---

## 5. PageResult 统一分页

```java
public class PageResult<T> {
    private List<T> list;
    private int page;
    private int size;
    private long total;
    private int pages;

    public static <T> PageResult<T> of(Page<T> page) { ... }
}
```

**位置：** `common/entity/PageResult.java`

整个项目后续统一使用此分页模型。

---

## 6. 分类树

```
GET /api/categories/tree
```

**返回示例：**
```json
{
  "code": 0,
  "data": [
    {
      "id": 1,
      "categoryName": "数码产品",
      "parentId": 0,
      "level": 1,
      "sort": 1,
      "children": [
        {
          "id": 10,
          "categoryName": "耳机",
          "parentId": 1,
          "level": 2,
          "sort": 1,
          "children": []
        }
      ]
    }
  ]
}
```

**实现：** 递归从 `parentId = 0` 开始，逐级查询子分类，支持无限层级。

---

## 7. 异常处理

| 错误码 | 常量 | 触发条件 |
|--------|------|----------|
| 30001 | PRODUCT_NOT_FOUND | 商品不存在（列表空/分类不存在，通过返回空数据而非异常处理） |
| 30005 | PRODUCT_OFFLINE | 商品不存在或已下架（详情页） |

使用项目统一 `BusinessException` + `GlobalExceptionHandler`，不返回 Hibernate Exception。

---

## 8. 编译结果

```
mvn compile → BUILD SUCCESS (59 source files)
```

| 检查项 | 结果 |
|--------|------|
| Maven Compile | ✅ 成功（0 错误） |
| 循环依赖 | ❌ 无 |
| Hibernate Mapping 错误 | ❌ 无 |
| Package 命名冲突 | ❌ 无 |

---

## 9. 文件清单

```
backend/commerce-platform/src/main/java/com/commerce/platform/
├── common/entity/
│   └── PageResult.java                                ★ 新增（统一分页）
└── product/
    ├── dto/customer/
    │   ├── ProductSearchRequest.java                  ★ 新增
    │   ├── ProductCardResponse.java                   ★ 新增
    │   ├── ProductDetailResponse.java                 ★ 新增
    │   └── CategoryTreeResponse.java                  ★ 新增
    ├── service/
    │   ├── CustomerProductService.java                ★ 新增
    │   └── impl/
    │       └── CustomerProductServiceImpl.java        ★ 新增
    ├── controller/
    │   └── CustomerProductController.java             ★ 新增
    └── repository/
        ├── ProductRepository.java                     ← 新增 5 个查询方法
        └── CategoryRepository.java                    ← 新增 1 个查询方法
```

---

## 10. 未实现的（符合任务要求）

| 项目 | 状态 | 说明 |
|------|------|------|
| 购物车 | ❌ | 后续 Sprint |
| 收藏 | ❌ | 后续 Sprint |
| 评价 | ❌ | 后续 Sprint |
| 订单 | ❌ | 后续 Sprint |
| 库存展示 | ❌ | 后续 Sprint |
| AI 推荐 | ❌ | 后续 Sprint |
| Admin 审核 | ❌ | 后续 Sprint |

---

> **文档版本:** v1.0  
> **最后更新:** 2026-07-26  
> **下一步:** Sprint 9 Step 2C — Admin 商品审核 API