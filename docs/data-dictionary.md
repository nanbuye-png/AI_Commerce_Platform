# AI Commerce Platform v1.0 数据字典

> 版本: v1.1  
> 更新日期: 2026-07-25  
> 关联文档: [database-design.md](./database-design.md) | [domain-model.md](./domain-model.md)

---

## 一、用户域 (User Domain)

### 1.1 user_account — 用户主表

| 列名 | 类型 | 长度 | 必填 | 说明 |
|------|------|:---:|:---:|------|
| id | BIGINT | — | Y | 主键，自增 |
| username | VARCHAR | 64 | Y | 用户名，唯一 |
| email | VARCHAR | 128 | Y | 邮箱，唯一 |
| phone | VARCHAR | 20 | N | 手机号，唯一 |
| password_hash | VARCHAR | 256 | Y | BCrypt 加密密码 |
| nickname | VARCHAR | 64 | N | 昵称 |
| avatar | VARCHAR | 256 | N | 头像 URL |
| status | VARCHAR | 20 | Y | ACTIVE / DISABLED |
| created_time | DATETIME | — | Y | 注册时间 |
| updated_time | DATETIME | — | Y | 最后更新时间 |
| deleted | TINYINT | 1 | Y | 逻辑删除: 0=正常, 1=删除 |

### 1.2 user_address — 用户地址

| 列名 | 类型 | 长度 | 必填 | 说明 |
|------|------|:---:|:---:|------|
| id | BIGINT | — | Y | 主键 |
| user_id | BIGINT | — | Y | 外键 → user_account |
| receiver_name | VARCHAR | 64 | Y | 收件人姓名 |
| receiver_phone | VARCHAR | 20 | Y | 收件人手机号 |
| province | VARCHAR | 32 | Y | 省 |
| city | VARCHAR | 32 | Y | 市 |
| district | VARCHAR | 32 | Y | 区 |
| detail_address | VARCHAR | 256 | Y | 详细地址 |
| is_default | TINYINT | 1 | Y | 是否默认地址 |
| created_time | DATETIME | — | Y | — |
| updated_time | DATETIME | — | Y | — |
| deleted | TINYINT | 1 | Y | — |

### 1.3 user_behavior — 用户行为（AI 推荐用）

| 列名 | 类型 | 长度 | 必填 | 说明 |
|------|------|:---:|:---:|------|
| id | BIGINT | — | Y | 主键 |
| user_id | BIGINT | — | Y | 外键 → user_account |
| behavior_type | VARCHAR | 20 | Y | VIEW / SEARCH / CLICK / PURCHASE |
| target_type | VARCHAR | 20 | Y | 目标类型（product 等） |
| target_id | BIGINT | — | Y | 目标 ID（商品ID 等） |
| extra_data | JSON | — | N | 扩展数据（搜索关键词等） |
| created_time | DATETIME | — | Y | — |

---

## 二、商家域 (Merchant Domain)

### 2.1 merchant_account — 商家账号

| 列名 | 类型 | 长度 | 必填 | 说明 |
|------|------|:---:|:---:|------|
| id | BIGINT | — | Y | 主键 |
| merchant_name | VARCHAR | 128 | Y | 商家名称 |
| contact_name | VARCHAR | 64 | Y | 联系人姓名 |
| contact_phone | VARCHAR | 20 | Y | 联系人电话 |
| email | VARCHAR | 128 | N | 邮箱 |
| status | VARCHAR | 20 | Y | PENDING / ACTIVE / DISABLED |
| created_time | DATETIME | — | Y | 入驻时间 |
| updated_time | DATETIME | — | Y | — |
| deleted | TINYINT | 1 | Y | — |

### 2.2 store — 店铺

| 列名 | 类型 | 长度 | 必填 | 说明 |
|------|------|:---:|:---:|------|
| id | BIGINT | — | Y | 主键 |
| merchant_id | BIGINT | — | Y | 外键 → merchant_account |
| store_name | VARCHAR | 128 | Y | 店铺名称 |
| logo | VARCHAR | 256 | N | Logo URL |
| description | TEXT | — | N | 店铺描述 |
| status | VARCHAR | 20 | Y | ACTIVE / CLOSED |
| created_time | DATETIME | — | Y | — |
| updated_time | DATETIME | — | Y | — |
| deleted | TINYINT | 1 | Y | — |

### 2.3 merchant_user — 商家员工

| 列名 | 类型 | 长度 | 必填 | 说明 |
|------|------|:---:|:---:|------|
| id | BIGINT | — | Y | 主键 |
| merchant_id | BIGINT | — | Y | 外键 → merchant_account |
| username | VARCHAR | 64 | Y | 登录名 |
| password_hash | VARCHAR | 256 | Y | 加密密码 |
| role | VARCHAR | 20 | Y | ADMIN / OPERATOR / CUSTOMER_SERVICE |
| status | VARCHAR | 20 | Y | ACTIVE / DISABLED |
| created_time | DATETIME | — | Y | — |
| updated_time | DATETIME | — | Y | — |
| deleted | TINYINT | 1 | Y | — |

---

## 三、商品域 (Product Domain)

### 3.1 category — 商品分类

| 列名 | 类型 | 长度 | 必填 | 说明 |
|------|------|:---:|:---:|------|
| id | BIGINT | — | Y | 主键 |
| parent_id | BIGINT | — | Y | 自关联父分类ID，0=根节点 |
| category_name | VARCHAR | 64 | Y | 分类名称 |
| sort | INT | — | Y | 排序权重 |
| created_time | DATETIME | — | Y | — |
| updated_time | DATETIME | — | Y | — |
| deleted | TINYINT | 1 | Y | — |

### 3.2 product — 商品 SPU

| 列名 | 类型 | 长度 | 必填 | 说明 |
|------|------|:---:|:---:|------|
| id | BIGINT | — | Y | 主键 |
| merchant_id | BIGINT | — | Y | 外键 → merchant_account |
| store_id | BIGINT | — | Y | 外键 → store |
| category_id | BIGINT | — | Y | 外键 → category |
| product_name | VARCHAR | 256 | Y | 商品名称 |
| description | TEXT | — | N | 商品描述 |
| brand | VARCHAR | 64 | N | 品牌 |
| status | VARCHAR | 20 | Y | DRAFT / ON_SHELF / OFF_SHELF |
| created_time | DATETIME | — | Y | — |
| updated_time | DATETIME | — | Y | — |
| deleted | TINYINT | 1 | Y | — |

### 3.3 product_sku — 商品 SKU（库存唯一来源: Inventory）

| 列名 | 类型 | 长度 | 必填 | 说明 |
|------|------|:---:|:---:|------|
| id | BIGINT | — | Y | 主键 |
| product_id | BIGINT | — | Y | 外键 → product |
| sku_code | VARCHAR | 64 | Y | SKU 编码，唯一 |
| attributes_json | JSON | — | Y | 规格属性 `{"color":"黑色","size":"XL"}` |
| price | DECIMAL | 12,2 | Y | 售价（单位: 元） |
| original_price | DECIMAL | 12,2 | N | 原价/划线价 |
| status | VARCHAR | 20 | Y | ACTIVE / DISABLED |
| created_time | DATETIME | — | Y | — |
| updated_time | DATETIME | — | Y | — |
| deleted | TINYINT | 1 | Y | — |

> ⚠️ SKU 表 **不存储库存数量**。库存统一由 `inventory` 表管理。

### 3.4 product_image — 商品图片

| 列名 | 类型 | 长度 | 必填 | 说明 |
|------|------|:---:|:---:|------|
| id | BIGINT | — | Y | 主键 |
| product_id | BIGINT | — | Y | 外键 → product |
| url | VARCHAR | 512 | Y | 图片 URL |
| sort | INT | — | Y | 排序权重 |
| is_cover | TINYINT | 1 | Y | 是否首图 |
| created_time | DATETIME | — | Y | — |

---

## 四、库存域 (Inventory Domain)

### 4.1 inventory — 库存（与 SKU 一一对应，库存唯一来源）

| 列名 | 类型 | 长度 | 必填 | 说明 |
|------|------|:---:|:---:|------|
| id | BIGINT | — | Y | 主键 |
| sku_id | BIGINT | — | Y | 外键 → product_sku，唯一 |
| available_stock | INT | — | Y | 可销售库存 |
| locked_stock | INT | — | Y | 订单预占库存（下单未支付时锁定） |
| reserved_stock | INT | — | Y | 特殊业务预留库存（活动预留等） |
| safety_stock | INT | — | N | 安全库存预警阈值 |
| version | INT | — | Y | 乐观锁版本号 |
| created_time | DATETIME | — | Y | — |
| updated_time | DATETIME | — | Y | — |

### 4.2 inventory_record — 库存流水

| 列名 | 类型 | 长度 | 必填 | 说明 |
|------|------|:---:|:---:|------|
| id | BIGINT | — | Y | 主键 |
| inventory_id | BIGINT | — | Y | 外键 → inventory |
| change_type | VARCHAR | 20 | Y | INBOUND / SALE_LOCK / SALE_CONFIRM / RETURN / RELEASE / RESERVE / UNRESERVE |
| change_qty | INT | — | Y | 变动数量（正=增，负=减） |
| before_qty | INT | — | Y | 变动前数量 |
| after_qty | INT | — | Y | 变动后数量 |
| related_no | VARCHAR | 64 | N | 关联单号 |
| created_time | DATETIME | — | Y | — |

---

## 五、购物车域 (Cart Domain)

### 5.1 cart — 购物车

| 列名 | 类型 | 长度 | 必填 | 说明 |
|------|------|:---:|:---:|------|
| id | BIGINT | — | Y | 主键 |
| user_id | BIGINT | — | Y | 外键 → user_account，唯一 |

### 5.2 cart_item — 购物车条目

| 列名 | 类型 | 长度 | 必填 | 说明 |
|------|------|:---:|:---:|------|
| id | BIGINT | — | Y | 主键 |
| cart_id | BIGINT | — | Y | 外键 → cart |
| sku_id | BIGINT | — | Y | 外键 → product_sku |
| quantity | INT | — | Y | 加入数量 |
| selected | TINYINT | 1 | Y | 是否选中参与结算 |
| created_time | DATETIME | — | Y | — |
| updated_time | DATETIME | — | Y | — |

---

## 六、订单域 (Order Domain)

### 6.1 order_info — 订单

| 列名 | 类型 | 长度 | 必填 | 说明 |
|------|------|:---:|:---:|------|
| id | BIGINT | — | Y | 主键 |
| order_no | VARCHAR | 32 | Y | 全局唯一订单号 |
| user_id | BIGINT | — | Y | 外键 → user_account |
| store_id | BIGINT | — | Y | 外键 → store |
| total_amount | DECIMAL | 12,2 | Y | 商品总金额 |
| freight | DECIMAL | 12,2 | Y | 运费 |
| pay_amount | DECIMAL | 12,2 | Y | 实付金额 |
| status | VARCHAR | 20 | Y | PENDING_PAYMENT / PAID / PROCESSING / SHIPPED / COMPLETED / CANCELLED / REFUNDING / REFUNDED |
| receiver_snapshot | JSON | — | N | 收货地址快照 |
| remark | VARCHAR | 256 | N | 用户备注 |
| created_time | DATETIME | — | Y | 下单时间 |
| updated_time | DATETIME | — | Y | — |
| deleted | TINYINT | 1 | Y | — |

### 6.2 order_item — 订单条目

| 列名 | 类型 | 长度 | 必填 | 说明 |
|------|------|:---:|:---:|------|
| id | BIGINT | — | Y | 主键 |
| order_id | BIGINT | — | Y | 外键 → order_info |
| sku_id | BIGINT | — | Y | 外键 → product_sku |
| product_name | VARCHAR | 256 | Y | 商品名称快照 |
| sku_snapshot | JSON | — | N | 规格快照 |
| unit_price | DECIMAL | 12,2 | Y | 单价 |
| quantity | INT | — | Y | 数量 |
| subtotal | DECIMAL | 12,2 | Y | 小计 |

---

## 七、支付域 (Payment Domain)

### 7.1 payment — 支付记录

| 列名 | 类型 | 长度 | 必填 | 说明 |
|------|------|:---:|:---:|------|
| id | BIGINT | — | Y | 主键 |
| order_id | BIGINT | — | Y | 外键 → order_info，唯一 |
| payment_no | VARCHAR | 32 | Y | 平台内部唯一支付编号 |
| pay_amount | DECIMAL | 12,2 | Y | 支付金额 |
| pay_method | VARCHAR | 20 | Y | WECHAT_QR / ALIPAY |
| status | VARCHAR | 20 | Y | PENDING / SUCCESS / FAILED |
| transaction_id | VARCHAR | 64 | N | 第三方交易流水号，UNIQUE 约束 |
| pay_time | DATETIME | — | N | 支付完成时间 |
| created_time | DATETIME | — | Y | — |
| updated_time | DATETIME | — | Y | — |

> **幂等约束:** `transaction_id` 唯一索引，支付回调时防重。

### 7.2 refund — 退款记录

| 列名 | 类型 | 长度 | 必填 | 说明 |
|------|------|:---:|:---:|------|
| id | BIGINT | — | Y | 主键 |
| order_id | BIGINT | — | Y | 外键 → order_info |
| payment_id | BIGINT | — | Y | 外键 → payment |
| refund_amount | DECIMAL | 12,2 | Y | 退款金额 |
| reason | VARCHAR | 256 | N | 退款原因 |
| status | VARCHAR | 20 | Y | APPLYING / APPROVED / REJECTED / SUCCESS |
| refund_transaction_id | VARCHAR | 64 | N | 第三方退款单号 |
| created_time | DATETIME | — | Y | — |
| completed_time | DATETIME | — | N | 退款完成时间 |

---

## 八、AI 域 (AI Domain)

### 8.1 conversation — 对话会话

| 列名 | 类型 | 长度 | 必填 | 说明 |
|------|------|:---:|:---:|------|
| id | BIGINT | — | Y | 主键 |
| user_id | BIGINT | — | Y | 外键 → user_account |
| title | VARCHAR | 128 | N | 会话主题 |
| context_json | JSON | — | N | 对话上下文 |
| created_time | DATETIME | — | Y | — |
| updated_time | DATETIME | — | Y | — |

### 8.2 message — 对话消息

| 列名 | 类型 | 长度 | 必填 | 说明 |
|------|------|:---:|:---:|------|
| id | BIGINT | — | Y | 主键 |
| conversation_id | BIGINT | — | Y | 外键 → conversation |
| role | VARCHAR | 20 | Y | USER / ASSISTANT / SYSTEM |
| content | TEXT | — | Y | 消息正文 |
| message_type | VARCHAR | 20 | Y | TEXT / PRODUCT_RECOMMEND / IMAGE |
| extra_data | JSON | — | N | 附加结构化数据 |
| created_time | DATETIME | — | Y | — |

### 8.3 ai_memory — AI 记忆

| 列名 | 类型 | 长度 | 必填 | 说明 |
|------|------|:---:|:---:|------|
| id | BIGINT | — | Y | 主键 |
| user_id | BIGINT | — | Y | 外键 → user_account |
| memory_type | VARCHAR | 20 | Y | PREFERENCE / INTENT / CONTEXT |
| content | JSON | — | Y | 记忆数据 |
| created_time | DATETIME | — | Y | — |

### 8.4 recommendation_record — 推荐记录

| 列名 | 类型 | 长度 | 必填 | 说明 |
|------|------|:---:|:---:|------|
| id | BIGINT | — | Y | 主键 |
| user_id | BIGINT | — | Y | 外键 → user_account |
| products | JSON | — | Y | 推荐结果 [{id, score}] |
| reason | VARCHAR | 256 | N | 推荐理由 |
| scene | VARCHAR | 20 | Y | HOME / CART / PRODUCT_DETAIL / SEARCH |
| feedback | VARCHAR | 20 | N | LIKE / DISLIKE / NO_FEEDBACK |
| created_time | DATETIME | — | Y | — |

---

## 九、媒体域 (Media Domain)

### 9.1 media_file — 媒体文件

| 列名 | 类型 | 长度 | 必填 | 说明 |
|------|------|:---:|:---:|------|
| id | BIGINT | — | Y | 主键 |
| original_name | VARCHAR | 256 | Y | 原始文件名 |
| file_path | VARCHAR | 512 | Y | MinIO 存储路径 |
| file_type | VARCHAR | 20 | Y | IMAGE / VIDEO |
| file_size | BIGINT | — | Y | 文件大小（字节） |
| mime_type | VARCHAR | 64 | N | MIME 类型 |
| created_time | DATETIME | — | Y | — |

### 9.2 media_relation — 媒体关联

| 列名 | 类型 | 长度 | 必填 | 说明 |
|------|------|:---:|:---:|------|
| id | BIGINT | — | Y | 主键 |
| media_id | BIGINT | — | Y | 外键 → media_file |
| target_type | VARCHAR | 20 | Y | PRODUCT / USER / AI_RESOURCE |
| target_id | BIGINT | — | Y | 目标实体 ID |
| created_time | DATETIME | — | Y | — |

---

## 十、Agent 任务域 (Agent Task Domain)

### 10.1 agent_task — Agent 任务

| 列名 | 类型 | 长度 | 必填 | 说明 |
|------|------|:---:|:---:|------|
| id | BIGINT | — | Y | 主键 |
| user_id | BIGINT | — | Y | 外键 → user_account |
| task_type | VARCHAR | 20 | Y | COMPARISON / BARGAIN_FIND / OUTFIT_SUGGESTION |
| task_params | JSON | — | Y | 任务参数 |
| status | VARCHAR | 20 | Y | PENDING / PROCESSING / COMPLETED / FAILED |
| result | JSON | — | N | 任务结果 |
| created_time | DATETIME | — | Y | — |
| completed_time | DATETIME | — | N | — |

### 10.2 agent_task_step — Agent 任务步骤

| 列名 | 类型 | 长度 | 必填 | 说明 |
|------|------|:---:|:---:|------|
| id | BIGINT | — | Y | 主键 |
| task_id | BIGINT | — | Y | 外键 → agent_task |
| step_order | INT | — | Y | 步骤序号 |
| step_name | VARCHAR | 64 | Y | 步骤名称 |
| step_status | VARCHAR | 20 | Y | PENDING / RUNNING / COMPLETED / FAILED |
| step_input | JSON | — | N | 步骤输入 |
| step_output | JSON | — | N | 步骤输出 |
| created_time | DATETIME | — | Y | — |
| completed_time | DATETIME | — | N | — |

---

## 表汇总

| 序号 | 表名 | 域 | 说明 |
|:---:|------|------|------|
| 1 | user_account | 用户域 | 用户主表 |
| 2 | user_address | 用户域 | 用户地址 |
| 3 | user_behavior | 用户域 | 用户行为 |
| 4 | merchant_account | 商家域 | 商家账号 |
| 5 | store | 商家域 | 店铺 |
| 6 | merchant_user | 商家域 | 商家员工 |
| 7 | category | 商品域 | 商品分类 |
| 8 | product | 商品域 | 商品 SPU |
| 9 | product_sku | 商品域 | 商品 SKU（不含库存） |
| 10 | product_image | 商品域 | 商品图片 |
| 11 | inventory | 库存域 | 库存（唯一来源） |
| 12 | inventory_record | 库存域 | 库存流水 |
| 13 | cart | 购物车域 | 购物车 |
| 14 | cart_item | 购物车域 | 购物车条目 |
| 15 | order_info | 订单域 | 订单 |
| 16 | order_item | 订单域 | 订单条目 |
| 17 | payment | 支付域 | 支付记录 |
| 18 | refund | 支付域 | 退款记录 |
| 19 | conversation | AI 域 | 对话会话 |
| 20 | message | AI 域 | 对话消息 |
| 21 | ai_memory | AI 域 | AI 记忆 |
| 22 | recommendation_record | AI 域 | 推荐记录 |
| 23 | media_file | 媒体域 | 媒体文件 |
| 24 | media_relation | 媒体域 | 媒体关联 |
| 25 | agent_task | Agent 域 | Agent 任务 |
| 26 | agent_task_step | Agent 域 | Agent 任务步骤 |

---

> **文档版本:** v1.1 | **最后更新:** 2026-07-25 | **变更:** Sprint 0 Step 3.5 — 同步 database-design.md 所有变更（product_sku 移除 stock、category/merchant/store/merchant_user/product_sku/inventory/payment 统一补全 updated_time、inventory 新增 reserved_stock + created_time + updated_time、payment 新增 payment_no + transaction_id UNIQUE）