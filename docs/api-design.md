# API 接口设计规范

## 一、API 总体规范

| 项目 | 规范 |
|------|------|
| **基础路径** | `/api/v1` |
| **协议** | RESTful API |
| **数据格式** | JSON（Content-Type: application/json） |
| **请求方式** | GET / POST / PUT / DELETE |
| **字符编码** | UTF-8 |

---

## 二、统一响应格式

### 成功响应

```json
{
  "code": 0,
  "message": "success",
  "data": {}
}
```

### 失败响应

```json
{
  "code": 10001,
  "message": "参数错误：用户名不能为空",
  "data": null
}
```

### 分页响应

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "records": [],
    "total": 100,
    "page": 1,
    "pageSize": 20,
    "totalPages": 5
  }
}
```

---

## 三、错误码设计

### 系统级错误码（10000–19999）

| 错误码 | 说明 | HTTP 状态码 |
|--------|------|-------------|
| 0 | 成功 | 200 |
| 10000 | 系统内部错误 | 500 |
| 10001 | 参数校验失败 | 400 |
| 10002 | 未登录 / Token 过期 | 401 |
| 10003 | 权限不足 | 403 |
| 10004 | 资源不存在 | 404 |
| 10005 | 请求频率超限 | 429 |

### 业务级错误码

| 范围 | 领域 | 说明 |
|------|------|------|
| 20000–29999 | 用户域 | 用户相关错误 |
| 30000–39999 | 商品域 | 商品相关错误 |
| 40000–49999 | 订单域 | 订单相关错误 |
| 50000–59999 | 支付域 | 支付相关错误 |
| 60000–69999 | AI 服务 | AI 服务错误 |

### 典型业务错误码示例

| 错误码 | 说明 |
|--------|------|
| 20001 | 用户不存在 |
| 20002 | 用户名或密码错误 |
| 20003 | 用户名已存在 |
| 30001 | 商品不存在 |
| 30002 | 商品已下架 |
| 30003 | 库存不足 |
| 40001 | 订单不存在 |
| 40002 | 订单状态不允许该操作 |
| 40003 | 订单已取消 |
| 50001 | 支付失败 |
| 50002 | 支付超时 |
| 60001 | AI 服务不可用 |
| 60002 | 对话上下文超限 |

---

## 四、认证接口 Auth API

**基础路径**: `/api/v1/auth`

### POST /login — 用户登录

- **功能**: 用户名/密码登录，返回 JWT Token
- **权限**: 公开

**请求**:
```json
{
  "username": "string（必填）",
  "password": "string（必填）"
}
```

**响应**:
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "token": "eyJhbGciOi...（Access Token）",
    "refresh_token": "eyJhbGciOi...（Refresh Token）",
    "expires_in": 7200,
    "user_info": {
      "id": 1001,
      "username": "john_doe",
      "nickname": "John",
      "avatar": "https://cdn.example.com/avatar/1001.jpg",
      "role": "USER"
    }
  }
}
```

---

### POST /register — 用户注册

- **功能**: 新用户注册
- **权限**: 公开

**请求**:
```json
{
  "username": "string（必填，4–32字符）",
  "password": "string（必填，6–64字符）",
  "nickname": "string（选填）",
  "email": "string（选填）",
  "phone": "string（选填）"
}
```

**响应**:
```json
{
  "code": 0,
  "message": "注册成功",
  "data": {
    "id": 1001,
    "username": "john_doe"
  }
}
```

---

### POST /logout — 退出登录

- **功能**: Token 失效，退出登录
- **权限**: USER / MERCHANT / ADMIN（需登录）

**请求**: 无 Body（Header 携带 Token）

**响应**:
```json
{
  "code": 0,
  "message": "退出成功",
  "data": null
}
```

---

### POST /refresh — 刷新 Token

- **功能**: 使用 refresh_token 换取新的 access_token
- **权限**: 公开（携带有效 refresh_token）

**请求**:
```json
{
  "refresh_token": "string（必填）"
}
```

**响应**:
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "token": "eyJhbGciOi...",
    "expires_in": 7200
  }
}
```

---

## 五、用户接口 User API

**基础路径**: `/api/v1/users`

### GET /profile — 获取用户个人信息

- **权限**: USER / MERCHANT / ADMIN（需登录）

**响应**:
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": 1001,
    "username": "john_doe",
    "nickname": "John",
    "avatar": "https://cdn.example.com/avatar/1001.jpg",
    "email": "john@example.com",
    "phone": "13800138000",
    "gender": "MALE",
    "birthday": "1990-01-01",
    "created_time": "2026-01-01T12:00:00"
  }
}
```

---

### PUT /profile — 修改用户个人信息

- **权限**: USER / MERCHANT / ADMIN（需登录）

**请求**:
```json
{
  "nickname": "string（选填）",
  "avatar": "string（选填，图片 URL）",
  "email": "string（选填）",
  "phone": "string（选填）",
  "gender": "MALE | FEMALE | OTHER（选填）",
  "birthday": "string（选填，格式 YYYY-MM-DD）"
}
```

---

### GET /addresses — 获取收货地址列表

- **权限**: USER（需登录）

**响应**:
```json
{
  "code": 0,
  "message": "success",
  "data": [
    {
      "id": 1,
      "receiver_name": "张三",
      "receiver_phone": "13800138000",
      "province": "广东省",
      "city": "深圳市",
      "district": "南山区",
      "detail": "科技园路1号",
      "is_default": true
    }
  ]
}
```

---

### POST /addresses — 新增收货地址

- **权限**: USER（需登录）

**请求**:
```json
{
  "receiver_name": "string（必填）",
  "receiver_phone": "string（必填）",
  "province": "string（必填）",
  "city": "string（必填）",
  "district": "string（必填）",
  "detail": "string（必填）",
  "is_default": false
}
```

---

### PUT /addresses/{id} — 修改收货地址

- **权限**: USER（需登录）

---

### DELETE /addresses/{id} — 删除收货地址

- **权限**: USER（需登录）

---

## 六、商品接口 Product API

**基础路径**: `/api/v1/products`

### GET /products — 商品列表

- **权限**: 公开

**查询参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| page | int | 否 | 页码（默认 1） |
| pageSize | int | 否 | 每页条数（默认 20，最大 100） |
| keyword | string | 否 | 搜索关键词（商品名称） |
| categoryId | int | 否 | 分类 ID |
| minPrice | decimal | 否 | 最低价格 |
| maxPrice | decimal | 否 | 最高价格 |
| sortBy | string | 否 | 排序字段：price / sales / created_time |
| sortOrder | string | 否 | 排序方向：asc / desc（默认 desc） |

**响应**:
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "records": [
      {
        "id": 2001,
        "name": "无线蓝牙耳机",
        "description": "高品质降噪蓝牙耳机...",
        "category_id": 10,
        "category_name": "数码产品",
        "store_id": 101,
        "store_name": "数码旗舰店",
        "min_price": 199.00,
        "max_price": 299.00,
        "main_image": "https://cdn.example.com/product/2001/main.jpg",
        "sales": 1520,
        "status": "ON_SALE",
        "created_time": "2026-01-15T10:00:00"
      }
    ],
    "total": 100,
    "page": 1,
    "pageSize": 20,
    "totalPages": 5
  }
}
```

---

### GET /products/{id} — 商品详情

- **权限**: 公开

> **库存来源:** 商品详情中的 SKU `available_stock` 从 `inventory` 表实时查询，非 SKU 表自有字段。

**响应**:
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": 2001,
    "name": "无线蓝牙耳机",
    "description": "高品质降噪蓝牙耳机，支持蓝牙 5.3...",
    "detail_html": "<p>详细描述 HTML...</p>",
    "category_id": 10,
    "category_name": "数码产品",
    "store_id": 101,
    "store_name": "数码旗舰店",
    "skus": [
      {
        "id": 3001,
        "spec_name": "黑色 / 标准版",
        "spec_values": { "颜色": "黑色", "版本": "标准版" },
        "price": 199.00,
        "original_price": 299.00,
        "available_stock": 50,
        "image": "https://cdn.example.com/product/2001/black.jpg"
      },
      {
        "id": 3002,
        "spec_name": "白色 / 标准版",
        "spec_values": { "颜色": "白色", "版本": "标准版" },
        "price": 199.00,
        "original_price": 299.00,
        "available_stock": 30,
        "image": "https://cdn.example.com/product/2001/white.jpg"
      }
    ],
    "images": [
      "https://cdn.example.com/product/2001/1.jpg",
      "https://cdn.example.com/product/2001/2.jpg"
    ],
    "status": "ON_SALE",
    "sales": 1520,
    "rating": 4.8,
    "created_time": "2026-01-15T10:00:00"
  }
}
```

---

### GET /categories — 商品分类

- **权限**: 公开

**响应**:
```json
{
  "code": 0,
  "message": "success",
  "data": [
    {
      "id": 1,
      "name": "数码产品",
      "parent_id": 0,
      "level": 1,
      "sort_order": 1,
      "children": [
        {
          "id": 10,
          "name": "耳机",
          "parent_id": 1,
          "level": 2,
          "sort_order": 1
        }
      ]
    }
  ]
}
```

---

## 七、购物车接口 Cart API

**基础路径**: `/api/v1/cart`

### GET /cart — 查看购物车

- **权限**: USER（需登录）

**响应**:
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "cart_id": 5001,
    "items": [
      {
        "id": 6001,
        "product_id": 2001,
        "sku_id": 3001,
        "product_name": "无线蓝牙耳机",
        "product_image": "https://cdn.example.com/product/2001/main.jpg",
        "spec_name": "黑色 / 标准版",
        "price": 199.00,
        "quantity": 2,
        "selected": true,
        "available_stock": 50,
        "added_time": "2026-07-20T10:00:00"
      }
    ],
    "total_amount": 398.00,
    "total_count": 2
  }
}
```

---

### POST /cart/items — 加入购物车

- **权限**: USER（需登录）

**请求**:
```json
{
  "sku_id": 3001,
  "quantity": 1
}
```

---

### PUT /cart/items/{id} — 修改商品数量

- **权限**: USER（需登录）

**请求**:
```json
{
  "quantity": 3
}
```

---

### DELETE /cart/items/{id} — 删除购物车商品

- **权限**: USER（需登录）

---

### PUT /cart/items/{id}/select — 勾选/取消勾选

- **权限**: USER（需登录）

**请求**:
```json
{
  "selected": true
}
```

---

## 八、订单接口 Order API

**基础路径**: `/api/v1/orders`

### POST /orders — 创建订单

- **权限**: USER（需登录）

**业务流程**: 购物车 → 选择商品 → 确认地址 → 库存预占 → 生成订单

**请求**:
```json
{
  "address_id": 1,
  "items": [
    {
      "sku_id": 3001,
      "quantity": 2
    },
    {
      "sku_id": 3002,
      "quantity": 1
    }
  ],
  "remark": "请尽快发货（选填）"
}
```

**响应**:
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "order_id": 8001,
    "order_no": "202607240001",
    "total_amount": 597.00,
    "status": "PENDING_PAYMENT",
    "created_time": "2026-07-24T22:30:00"
  }
}
```

---

### GET /orders — 订单列表

- **权限**: USER（需登录）

**查询参数**:

| 参数 | 类型 | 说明 |
|------|------|------|
| page | int | 页码 |
| pageSize | int | 每页条数 |
| status | string | 订单状态：PENDING_PAYMENT / PAID / PROCESSING / SHIPPED / COMPLETED / CANCELLED |

---

### GET /orders/{id} — 订单详情

- **权限**: USER（需登录，仅查看自己的订单）

**响应**:
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "order_id": 8001,
    "order_no": "202607240001",
    "status": "PENDING_PAYMENT",
    "total_amount": 597.00,
    "discount_amount": 0.00,
    "freight": 0.00,
    "pay_amount": 597.00,
    "address": {
      "receiver_name": "张三",
      "receiver_phone": "13800138000",
      "province": "广东省",
      "city": "深圳市",
      "district": "南山区",
      "detail": "科技园路1号"
    },
    "items": [
      {
        "product_id": 2001,
        "sku_id": 3001,
        "product_name": "无线蓝牙耳机",
        "product_image": "https://cdn.example.com/2001/main.jpg",
        "spec_name": "黑色 / 标准版",
        "price": 199.00,
        "quantity": 2,
        "subtotal": 398.00
      }
    ],
    "created_time": "2026-07-24T22:30:00",
    "payment_time": null,
    "ship_time": null,
    "completed_time": null
  }
}
```

---

### PUT /orders/{id}/cancel — 取消订单

- **权限**: USER（需登录，仅取消自己的订单）
- **条件**: 仅 PENDING_PAYMENT 状态可取消

---

## 九、支付接口 Payment API

**基础路径**: `/api/v1/payment`

> **幂等设计:** `transaction_id` 建立 UNIQUE 约束。支付回调时先按 `transaction_id` 查询是否已处理，已处理则直接返回成功，防止重复扣款。

### POST /pay — 创建支付

- **权限**: USER（需登录）

**请求**:
```json
{
  "order_id": 8001,
  "payment_channel": "WECHAT_PAY"
}
```

**响应**:
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "payment_id": 9001,
    "order_id": 8001,
    "payment_no": "PAY202607240001",
    "pay_amount": 597.00,
    "payment_channel": "WECHAT_PAY",
    "qr_code_url": "https://pay.example.com/qr/xxx",
    "expire_time": "2026-07-24T23:00:00"
  }
}
```

---

### GET /payment/{order_id} — 查询支付状态

- **权限**: USER（需登录）

**响应**:
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "order_id": 8001,
    "transaction_id": "WX202607240001",
    "status": "SUCCESS",
    "payment_channel": "WECHAT_PAY",
    "pay_amount": 597.00,
    "payment_time": "2026-07-24T22:35:00"
  }
}
```

---

### POST /payment/callback — 支付回调（幂等）

- **权限**: 公开（由支付渠道服务端回调，需签名验证）
- **幂等保证**: transaction_id 唯一索引

**请求**:
```json
{
  "order_id": 8001,
  "transaction_id": "WX202607240001",
  "status": "SUCCESS",
  "pay_amount": 597.00,
  "sign": "md5签名..."
}
```

---

## 十、商家接口 Merchant API

**基础路径**: `/api/v1/merchant`

**权限要求**: MERCHANT 角色

### 商品管理

#### GET /merchant/products — 商家商品列表

- **说明**: 查看本店铺所有商品
- **支持**: 分页、状态筛选

---

#### POST /merchant/products — 新增商品

**请求**:
```json
{
  "name": "string（必填）",
  "description": "string（选填）",
  "detail_html": "string（选填）",
  "category_id": 10,
  "skus": [
    {
      "spec_name": "黑色 / 标准版",
      "spec_values": { "颜色": "黑色", "版本": "标准版" },
      "price": 199.00,
      "original_price": 299.00,
      "stock": 100,
      "image": "https://cdn.example.com/2001/black.jpg"
    }
  ],
  "images": ["https://...", "https://..."]
}
```

---

#### PUT /merchant/products/{id} — 修改商品

- **权限**: MERCHANT（仅修改本店铺商品）

---

#### DELETE /merchant/products/{id} — 下架/删除商品

- **权限**: MERCHANT（仅操作本店铺商品）

---

### 库存管理

#### GET /merchant/inventory — 库存查看

- **说明**: 查看本店铺所有 SKU 库存（available_stock / locked_stock / reserved_stock）

---

#### PUT /merchant/inventory/{skuId} — 库存调整

**请求**:
```json
{
  "quantity": 200,
  "change_type": "SET | ADD | REDUCE",
  "remark": "补货"
}
```

---

### 订单管理

#### GET /merchant/orders — 商家订单列表

- **说明**: 查看用户在本店铺下的订单

**查询参数**:

| 参数 | 说明 |
|------|------|
| status | 订单状态筛选 |
| page / pageSize | 分页 |

---

#### PUT /merchant/orders/{id}/ship — 发货

**请求**:
```json
{
  "logistics_company": "顺丰速运",
  "tracking_number": "SF1234567890"
}
```

---

## 十一、后台管理 Admin API

**基础路径**: `/api/v1/admin`

**权限要求**: ADMIN 角色

### GET /admin/users — 用户管理

- **说明**: 分页查询平台所有用户
- **支持**: 用户名搜索、状态筛选

---

### GET /admin/merchants — 商家管理

- **说明**: 分页查询所有商家，支持入驻审核
- **支持**: 状态筛选（PENDING / APPROVED / REJECTED）

---

### PUT /admin/merchants/{id}/audit — 商家审核

**请求**:
```json
{
  "status": "APPROVED | REJECTED",
  "remark": "审核备注"
}
```

---

### GET /admin/products/review — 商品审核

- **说明**: 查看待审核商品列表

---

### PUT /admin/products/{id}/audit — 商品审核操作

**请求**:
```json
{
  "status": "APPROVED | REJECTED",
  "remark": "审核备注"
}
```

---

### GET /admin/dashboard — 系统统计

**响应**:
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "today_order_count": 1520,
    "today_order_amount": 87950.00,
    "total_users": 125000,
    "total_merchants": 320,
    "total_products": 5800,
    "order_trend": [
      { "date": "2026-07-18", "count": 1200, "amount": 68000.00 },
      { "date": "2026-07-19", "count": 1350, "amount": 72000.00 }
    ]
  }
}
```

---

## 十二、库存接口 Inventory API

**基础路径**: `/api/v1/inventory`

> 库存操作由 Backend 统一管理。所有库存变更都会记录到 `inventory_record` 表。

### GET /inventory/{skuId} — 查询 SKU 实时库存

- **权限**: USER / MERCHANT / ADMIN

**响应**:
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "skuId": 1001,
    "availableStock": 95,
    "lockedStock": 5,
    "reservedStock": 0,
    "safetyStock": 10,
    "isLowStock": false
  }
}
```

---

### POST /inventory/reserve — 库存预占（内部）

- **权限**: 内部调用（由订单服务触发，不对外暴露）
- **说明**: 下单时预占库存，locked_stock 增加

**请求**:
```json
{
  "skuId": 1001,
  "quantity": 2,
  "orderNo": "ORD20260725001"
}
```

---

### POST /inventory/release — 释放库存（内部）

- **权限**: 内部调用（由订单服务触发）
- **说明**: 订单取消/超时时释放预占库存，locked_stock 减少

**请求**:
```json
{
  "skuId": 1001,
  "quantity": 2,
  "orderNo": "ORD20260725001",
  "reason": "ORDER_CANCELLED"
}
```

---

## 十三、AI 接口 AI API

**基础路径**: `/api/v1/ai`

> **约束:** Frontend 不得直接调用 AI Service。所有 AI 请求必须经过 API Gateway 转发。

### POST /ai/chat — AI 购物助手对话（普通响应）

- **权限**: USER（需登录）
- **Content-Type**: application/json

**请求**:
```json
{
  "message": "我想买一款适合跑步的耳机，预算 300 以内",
  "conversation_id": "conv_abc123（选填，用于继续上一次对话）"
}
```

**响应**:
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "conversation_id": "conv_abc123",
    "message_id": "msg_001",
    "answer": "为您推荐以下几款适合跑步的运动耳机...",
    "products": [
      {
        "id": 2001,
        "name": "无线蓝牙耳机",
        "price": 199.00,
        "main_image": "https://cdn.example.com/2001/main.jpg",
        "reason": "IPX5 防水，佩戴稳固，续航 8 小时"
      }
    ],
    "recommendations": [
      {
        "type": "RELATED",
        "products": []
      }
    ]
  }
}
```

---

### POST /ai/chat/stream — AI 购物助手对话（流式响应 SSE）

- **权限**: USER（需登录）
- **Content-Type**: text/event-stream
- **说明**: 使用 Server-Sent Events (SSE) 逐 token 返回 AI 回复内容，提升用户体验

**请求**:
```json
{
  "message": "我想买一款适合跑步的耳机，预算 300 以内",
  "conversation_id": "conv_abc123（选填）"
}
```

**SSE 事件流**:
```
event: message
data: {"type": "token", "content": "为您"}

event: message
data: {"type": "token", "content": "推荐"}

event: message
data: {"type": "token", "content": "以下几款..."}

event: message
data: {"type": "products", "products": [...]}

event: done
data: {"conversation_id": "conv_abc123", "message_id": "msg_001"}
```

> **SSE 事件类型:** `token` — AI 逐词输出 | `products` — 推荐商品列表 | `done` — 对话完成

---

### GET /ai/conversations — 获取历史会话列表

- **权限**: USER（需登录）

**响应**:
```json
{
  "code": 0,
  "message": "success",
  "data": [
    {
      "conversation_id": "conv_abc123",
      "title": "跑步耳机推荐",
      "last_message": "为您推荐以下几款...",
      "created_time": "2026-07-24T20:00:00",
      "updated_time": "2026-07-24T22:00:00"
    }
  ]
}
```

---

### GET /ai/conversations/{id}/messages — 获取会话消息历史

- **权限**: USER（需登录）

**响应**:
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "conversation_id": "conv_abc123",
    "messages": [
      { "role": "USER", "content": "我想买跑步耳机", "created_time": "..." },
      { "role": "ASSISTANT", "content": "为您推荐...", "created_time": "..." }
    ]
  }
}
```

---

### POST /ai/recommend — 商品智能推荐

- **权限**: USER（需登录）
- **说明**: 基于用户行为（浏览/搜索/购买）的个性化推荐

**请求**:
```json
{
  "limit": 10
}
```

**响应**:
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "recommendations": [
      {
        "id": 2001,
        "name": "无线蓝牙耳机",
        "price": 199.00,
        "main_image": "https://...",
        "reason": "根据您最近的浏览记录推荐"
      }
    ]
  }
}
```

---

### POST /ai/agent/task — 创建 AI 购物任务

- **权限**: USER（需登录）

**请求**:
```json
{
  "task_type": "COMPARISON | BARGAIN_FIND | OUTFIT_SUGGESTION",
  "input": {
    "product_ids": [2001, 2002],
    "preferences": "性价比优先"
  }
}
```

**响应**:
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "task_id": "task_xyz789",
    "status": "PROCESSING",
    "created_time": "2026-07-24T22:30:00"
  }
}
```

---

### GET /ai/agent/task/{task_id} — 查询 AI Agent 任务状态

- **权限**: USER（需登录）

**响应**:
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "task_id": "task_xyz789",
    "task_type": "COMPARISON",
    "status": "COMPLETED",
    "result": {
      "comparison": [
        { "product_id": 2001, "name": "...", "pros": [], "cons": [] },
        { "product_id": 2002, "name": "...", "pros": [], "cons": [] }
      ],
      "suggestion": "推荐选择 2001..."
    },
    "created_time": "2026-07-24T22:30:00",
    "completed_time": "2026-07-24T22:31:30"
  }
}
```

---

## 十四、文件接口 File API

**基础路径**: `/api/v1/files`

### POST /files/upload — 文件上传

- **权限**: USER / MERCHANT / ADMIN（需登录）
- **Content-Type**: multipart/form-data

**请求参数**:

| 参数 | 类型 | 说明 |
|------|------|------|
| file | File | 上传文件（图片/视频） |
| type | string | 业务类型：product_image / avatar / ai_resource |

**限制**:
- 图片: jpg / png / webp，最大 5MB
- 视频: mp4，最大 50MB

**响应**:
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "file_id": 10001,
    "file_url": "https://cdn.example.com/files/product/2026/07/abc123.jpg",
    "file_name": "headphone01.jpg",
    "file_size": 204800,
    "width": 800,
    "height": 800
  }
}
```

---

## 十五、系统接口

### GET /health — 健康检查

- **权限**: 公开
- **用途**: 负载均衡健康探测 / 监控

**响应**:
```json
{
  "status": "UP",
  "service": "commerce-platform",
  "version": "1.0.0",
  "timestamp": "2026-07-24T22:30:00"
}
```

---

### GET /metrics — 系统指标

- **权限**: ADMIN
- **用途**: Prometheus 监控数据采集

---

## 十六、API 安全规范

### 认证方式 — JWT

**所有需认证接口**必须在 Header 携带 Token：

```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

### Token 说明

| 项 | 说明 |
|------|------|
| Access Token 有效期 | 2 小时 |
| Refresh Token 有效期 | 7 天 |
| 刷新方式 | POST /api/v1/auth/refresh |

### 角色权限

| 角色 | 标识 | 权限范围 |
|------|------|----------|
| **USER** | 普通用户 | 浏览商品、下单、购物车、AI 助手、个人信息管理 |
| **MERCHANT** | 商家 | 商品管理、库存管理、订单发货、店铺管理 |
| **ADMIN** | 平台管理员 | 用户管理、商家审核、商品审核、系统统计 |

### 安全约束

| 规则 | 说明 |
|------|------|
| 密码加密 | BCrypt 加密存储 |
| 敏感操作 | 登录/支付等关键接口需记录审计日志 |
| 请求限流 | 同一用户 1 分钟内最多 60 次 API 调用 |
| CORS | 仅允许白名单域名跨域访问 |
| HTTPS | 生产环境强制 HTTPS |

---

## 十七、API 全量统计

### 按模块汇总

| 模块 | 接口数 | 基础路径 |
|------|--------|----------|
| Auth（认证） | 4 | /api/v1/auth |
| User（用户） | 6 | /api/v1/users |
| Product（商品） | 3 | /api/v1/products |
| Cart（购物车） | 5 | /api/v1/cart |
| Order（订单） | 4 | /api/v1/orders |
| Payment（支付） | 3 | /api/v1/payment |
| Merchant（商家） | 7 | /api/v1/merchant |
| Admin（管理后台） | 6 | /api/v1/admin |
| Inventory（库存） | 3 | /api/v1/inventory |
| AI（智能服务） | 7 | /api/v1/ai |
| File（文件） | 1 | /api/v1/files |
| System（系统） | 2 | / |
| **合计** | **51** | |

### 按 HTTP 方法统计

| 方法 | 数量 |
|------|------|
| GET | 21 |
| POST | 18 |
| PUT | 10 |
| DELETE | 2 |

---

## 十八、请求调用链路

```
Frontend (React)
    │
    │  Authorization: Bearer <token>
    ▼
API Gateway
    │
    ├── /api/v1/auth/* ──────> Auth Service
    ├── /api/v1/users/* ─────> Commerce Platform（用户模块）
    ├── /api/v1/products/* ──> Commerce Platform（商品模块）
    ├── /api/v1/cart/* ──────> Commerce Platform（交易模块）
    ├── /api/v1/orders/* ────> Commerce Platform（交易模块）
    ├── /api/v1/payment/* ───> Commerce Platform（支付模块）
    ├── /api/v1/inventory/* ─> Commerce Platform（库存模块）
    ├── /api/v1/merchant/* ──> Commerce Platform（商家模块）
    ├── /api/v1/admin/* ─────> Commerce Platform（管理模块）
    ├── /api/v1/ai/* ────────> AI Service（FastAPI）— 禁止前端直连
    └── /api/v1/files/* ─────> Commerce Platform（文件模块）→ MinIO
```

---

> **文档版本:** v1.1  
> **最后更新:** 2026-07-25  
> **变更:** Sprint 0 Step 3.5 — 新增 Inventory API 模块（十二），AI 对话增加 SSE 流式接口 POST /ai/chat/stream（13.2），支付 API 增加 payment_no + transaction_id 幂等说明，商品详情 stock → available_stock（来自 inventory），章节重编号（十二~十八），API 总数 47→51  
> **状态:** 设计阶段