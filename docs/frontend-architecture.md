# AI Commerce Platform Frontend Architecture

> **版本:** v1.0  
> **最后更新:** 2026-07-25  
> **相关文档:** [architecture.md](./architecture.md), [api-design.md](./api-design.md)

---

## 一、概述

AI Commerce Platform 前端采用 **React 三端分离** 架构，由以下四个部分组成：

| 部分 | 定位 | 角色 | 受众 |
|------|------|------|------|
| **customer-web** | C 端用户购物商城 | `USER` | 消费者 |
| **merchant-web** | 商家运营后台 | `MERCHANT` | 商家/店铺运营 |
| **admin-web** | 平台运营管理后台 | `ADMIN` | 平台管理员 |
| **shared** | 三端共享层 | — | 三端共用 |

---

## 二、整体前端技术架构

### 2.1 技术栈

| 技术 | 版本 | 用途 |
|------|------|------|
| React | 19.x | UI 框架 |
| TypeScript | 6.x (5.x 兼容) | 类型系统 |
| Vite | 8.x (5.x 兼容) | 构建工具 |
| Ant Design | 6.x (5.x 兼容) | UI 组件库 |
| React Router | 7.x (6.x 兼容) | SPA 路由 |
| Zustand | 5.x (4.x 兼容) | 状态管理 |
| Axios | 1.x | HTTP 客户端 |

### 2.2 三层架构

```
Frontend Applications
        │
        │
customer-web     merchant-web     admin-web
        │              │              │
        └──────────────┼──────────────┘
                       │
                    shared
                 (共享层)
```

### 2.3 三端调用链路

```
React Component (用户交互)
      │
      ▼
Zustand Store Action (状态变更)
      │
      ▼
Axios Service Layer (API 调用)
      │  ┌── 请求拦截: 自动附加 JWT Bearer Token
      │  └── 响应拦截: 统一处理 401/403/500
      ▼
API Gateway (认证鉴权 → 路由转发)
      │
      ▼
Backend Service (业务处理)
      │
      ▼
Axios 响应拦截器 (统一错误处理)
      │
      ▼
Store State 更新 (触发 UI 重渲染)
      │
      ▼
React Component Re-render (最终 UI)
```

---

## 三、customer-web 架构设计

### 3.1 定位

C 端用户购物商城，面向 **消费者**，角色为 `USER`。

### 3.2 目录规划

```
src/
├── pages/          # 页面组件
├── layouts/        # 布局组件
├── components/     # 业务组件 & 通用组件
│   └── common/     # 通用 UI 组件
├── hooks/          # 自定义 Hooks
├── stores/         # Zustand 状态管理
├── services/       # API 服务层
├── router/         # 路由配置
├── types/          # TypeScript 类型定义
├── utils/          # 工具函数
├── api/            # Axios 实例 & 拦截器
├── assets/         # 静态资源 (图片/图标)
├── App.tsx         # 根组件
├── App.css         # 根样式
├── index.css       # 全局样式
└── main.tsx        # 入口文件
```

### 3.3 路由设计

| 路径 | 页面 | 说明 |
|------|------|------|
| `/` | Home | 首页 (商品推荐、活动 Banner) |
| `/products` | ProductList | 商品列表 (分类/搜索/筛选/排序) |
| `/products/:id` | ProductDetail | 商品详情 |
| `/cart` | Cart | 购物车 |
| `/orders` | OrderList | 订单列表 |
| `/orders/:id` | OrderDetail | 订单详情 |
| `/profile` | Profile | 个人中心 (信息/地址/收藏) |
| `/ai` | AIChat | AI 购物助手 |
| `*` | NotFound | 404 页面 |

### 3.4 Layout 设计

**BasicLayout** — C 端通用布局:

```
┌──────────────────────────────────────────────┐
│  Header                                       │
│  ┌─────────┐ ┌──────────────┐ ┌────────────┐ │
│  │  Logo   │ │  搜索框      │ │登录│购物车  │ │
│  └─────────┘ └──────────────┘ └────────────┘ │
├──────────────────────────────────────────────┤
│  Content (React Router <Outlet />)            │
│                                               │
│  (路由匹配的子页面内容)                         │
│                                               │
├──────────────────────────────────────────────┤
│  Footer                                       │
│  版权信息 / 友情链接 / 关于我们                  │
└──────────────────────────────────────────────┘
```

---

## 四、merchant-web 架构设计

### 4.1 定位

商家运营后台，面向 **商家/店铺运营人员**，角色为 `MERCHANT`。

### 4.2 目录规划

```
src/
├── pages/
│   ├── Login.tsx          # 商家登录
│   ├── Dashboard.tsx       # 运营总览
│   ├── products/           # 商品管理页面
│   ├── orders/             # 订单管理页面
│   ├── inventory/          # 库存管理页面
│   ├── statistics/         # 数据统计页面
│   └── settings/           # 店铺设置页面
├── layouts/
├── components/
├── hooks/
├── stores/
├── services/
├── router/
├── types/
├── utils/
├── api/
├── assets/
├── App.tsx
├── App.css
├── index.css
└── main.tsx
```

### 4.3 路由设计

| 路径 | 页面 | 说明 |
|------|------|------|
| `/login` | Login | 商家登录 (白名单路由) |
| `/dashboard` | Dashboard | 运营总览 (销售额/订单数/流量) |
| `/products` | ProductManage | 商品管理列表 |
| `/products/create` | ProductCreate | 新增商品 |
| `/products/:id/edit` | ProductEdit | 编辑商品 |
| `/orders` | OrderManage | 订单管理列表 |
| `/inventory` | InventoryManage | 库存管理 |
| `/statistics` | Statistics | 数据统计 (销售报表/用户分析) |
| `/settings` | ShopSettings | 店铺设置 |

### 4.4 Layout 设计

**MerchantLayout** — 商家端布局:

```
┌───────────┬────────────────────────────────────┐
│  Sidebar  │  Header                            │
│           │  ┌─────────────────────────────┐   │
│  ──────   │  │ 面包屑 / 当前页面标题        │   │
│  概览     │  │                   用户名/退出│   │
│  商品     │  ├─────────────────────────────┤   │
│  订单     │  │                             │   │
│  库存     │  │  Content                    │   │
│  统计     │  │  (React Router <Outlet />)  │   │
│  设置     │  │                             │   │
│           │  │                             │   │
└───────────┴────────────────────────────────────┘
```

### 4.5 权限

- 角色: `MERCHANT`
- 未登录 → 重定向至 `/login`
- 非 `MERCHANT` 角色 → 403 提示

---

## 五、admin-web 架构设计

### 5.1 定位

平台运营管理后台，面向 **平台管理员**，角色为 `ADMIN`。

### 5.2 目录规划

```
src/
├── pages/
│   ├── Login.tsx           # 管理员登录
│   ├── Dashboard.tsx        # 平台总览
│   ├── users/               # 用户管理页面
│   ├── merchants/           # 商家管理页面
│   ├── products/            # 全平台商品管控
│   ├── orders/              # 全平台订单管理
│   ├── system/              # 系统设置
│   └── ai-management/       # AI 服务管理
├── layouts/
├── components/
├── hooks/
├── stores/
├── services/
├── router/
├── types/
├── utils/
├── api/
├── assets/
├── App.tsx
├── App.css
├── index.css
└── main.tsx
```

### 5.3 路由设计

| 路径 | 页面 | 说明 |
|------|------|------|
| `/login` | Login | 管理员登录 (白名单路由) |
| `/dashboard` | Dashboard | 平台总览 (总用户/总订单/GMV) |
| `/users` | UserManage | 用户管理 |
| `/merchants` | MerchantManage | 商家管理 (审核/入驻/管控) |
| `/products` | ProductManage | 全平台商品管控 |
| `/orders` | OrderManage | 全平台订单管理 |
| `/system` | SystemConfig | 系统设置 (参数/日志/备份) |
| `/ai-management` | AIManagement | AI 服务管理 (模型/额度/监控) |

### 5.4 Layout 设计

**AdminLayout** — 管理端布局:

```
┌───────────┬────────────────────────────────────┐
│  Sidebar  │  Header                            │
│           │  ┌─────────────────────────────┐   │
│  ──────   │  │ 面包屑 / 当前页面标题        │   │
│  概览     │  │                   管理员/退出│   │
│  用户     │  ├─────────────────────────────┤   │
│  商家     │  │                             │   │
│  商品     │  │  Content                    │   │
│  订单     │  │  (React Router <Outlet />)  │   │
│  系统     │  │                             │   │
│  AI管理   │  │                             │   │
└───────────┴────────────────────────────────────┘
```

### 5.5 权限

- 角色: `ADMIN`
- 未登录 → 重定向至 `/login`
- 非 `ADMIN` 角色 → 403 提示

---

## 六、React Router 权限体系

### 6.1 Route Guard 设计

```
          用户访问页面
                │
                ▼
        ┌──────────────┐
        │  读取 Token   │ ← localStorage / Cookie
        └──────┬───────┘
               │
        ┌──────▼──────┐
        │ Token 存在?  │──────── 否 ──────→ 重定向至 /login
        └──────┬──────┘
               │ 是
               ▼
        ┌──────────────┐
        │ 解析用户角色  │ ← JWT payload → { sub, role, exp }
        └──────┬───────┘
               │
        ┌──────▼──────┐
        │  判断权限    │
        │  角色匹配?   │────── 否 ──────→ 403 Forbidden 页面
        └──────┬──────┘
               │ 是
               ▼
        ┌──────────────┐
        │   进入页面    │
        └──────────────┘
```

### 6.2 角色定义

| 角色 | 枚举值 | 可访问应用 | 描述 |
|------|--------|------------|------|
| USER | `ROLE_USER` | customer-web | 普通消费者 |
| MERCHANT | `ROLE_MERCHANT` | merchant-web | 商家/店铺运营 |
| ADMIN | `ROLE_ADMIN` | admin-web | 平台管理员 |

### 6.3 白名单路由 (免登录)

以下路由 **不需要** 验证登录状态:

| 应用 | 白名单路由 |
|------|-----------|
| customer-web | `/`, `/products`, `/products/:id`, `/login` |
| merchant-web | `/login` |
| admin-web | `/login` |

### 6.4 权限控制三层体系

```
┌──────────────────────────────────────────────────────┐
│  第一层: 路由层 (Route Guard)                         │
│  · 前端路由守卫，检查 Token 存在性                       │
│  · 角色不匹配 → 显示 403 页面                           │
│  · 白名单路由跳过校验                                   │
├──────────────────────────────────────────────────────┤
│  第二层: 接口层 (Axios 响应拦截器)                      │
│  · 401 → 清除 Token → 跳转 /login                      │
│  · 403 → 权限不足提示                                  │
│  · 500 → 系统错误提示                                  │
├──────────────────────────────────────────────────────┤
│  第三层: API Gateway (后端网关)                        │
│  · JWT 验证 + 角色权限校验                             │
│  · 请求频率限流                                        │
│  · 全量 API 日志审计                                   │
└──────────────────────────────────────────────────────┘
```

---

## 七、状态管理设计 (Zustand)

### 7.1 Store 概览

| Store | 所属应用 | 职责 |
|-------|----------|------|
| `authStore` | customer-web / merchant-web / admin-web | 认证状态管理 |
| `cartStore` | customer-web | 购物车状态管理 |
| `aiStore` | customer-web | AI 对话状态管理 |
| `appStore` | customer-web / merchant-web / admin-web | 全局应用配置 |

### 7.2 authStore

**存储内容:**

```typescript
// authStore 状态结构
interface AuthState {
  token: string | null;          // JWT Token
  refreshToken: string | null;   // Refresh Token
  userInfo: UserInfo | null;     // 用户基本信息
  role: 'USER' | 'MERCHANT' | 'ADMIN' | null;  // 用户角色
  isAuthenticated: boolean;      // 是否已认证
}

interface UserInfo {
  id: number;
  username: string;
  nickname: string;
  avatar: string;
}
```

**Actions:**
- `login(username, password)` — 登录
- `logout()` — 退出 (清除 Token & 状态)
- `refreshToken()` — 刷新 Token
- `setUserInfo(info)` — 更新用户信息

### 7.3 cartStore (仅 customer-web)

**存储内容:**

```typescript
interface CartState {
  items: CartItem[];          // 购物车条目列表
  totalCount: number;         // 商品总数量
  totalPrice: number;         // 商品总金额
  selectedIds: number[];      // 已选中条目 ID
  isOpen: boolean;            // 购物车弹窗/抽屉状态
}
```

**Actions:**
- `addItem(skuId, quantity)` — 加入购物车
- `removeItem(itemId)` — 移除条目
- `updateQuantity(itemId, quantity)` — 更新数量
- `toggleSelect(itemId)` — 切换选中
- `selectAll()` / `unselectAll()` — 全选/取消全选
- `clearCart()` — 清空购物车

### 7.4 aiStore (仅 customer-web)

**存储内容:**

```typescript
interface AIState {
  conversationId: string | null;      // 当前对话 ID
  messages: AIMessage[];              // 对话消息列表
  isStreaming: boolean;               // 是否正在流式输出
  isThinking: boolean;                // AI 是否正在思考
  suggestions: string[];              // AI 推荐商品/建议
}

interface AIMessage {
  id: string;
  role: 'user' | 'assistant' | 'system';
  content: string;
  timestamp: number;
}
```

**Actions:**
- `startConversation()` — 开始新对话
- `sendMessage(content)` — 发送消息
- `receiveMessage(message)` — 接收 AI 回复
- `clearConversation()` — 清空对话
- `loadConversation(id)` — 加载历史对话

### 7.5 appStore (全局)

**存储内容:**

```typescript
interface AppState {
  theme: 'light' | 'dark';         // 主题
  locale: 'zh-CN' | 'en-US';       // 语言
  collapsed: boolean;              // 侧边栏折叠状态 (merchant/admin)
  globalLoading: boolean;          // 全局加载状态
}
```

**Actions:**
- `toggleTheme()` — 切换主题
- `setLocale(locale)` — 设置语言
- `toggleSidebar()` — 切换侧边栏
- `setGlobalLoading(loading)` — 设置全局加载

---

## 八、Axios 网络层设计

### 8.1 目录结构

```
services/
├── auth.ts        # 认证相关 API
├── product.ts     # 商品相关 API
├── cart.ts        # 购物车相关 API
├── order.ts       # 订单相关 API
├── payment.ts     # 支付相关 API
└── ai.ts          # AI 相关 API
```

每个应用 (customer-web / merchant-web / admin-web) 各自维护自己的 `services/` 目录，按需引入对应的 API 模块。

### 8.2 统一 Axios 实例

文件: `src/api/request.ts`

```typescript
import axios from 'axios';

const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080',
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json',
  },
});
```

### 8.3 请求拦截器

```
                    发起请求
                       │
                       ▼
            ┌─────────────────────┐
            │  获取 Token          │
            │  (authStore.token)   │
            └──────────┬──────────┘
                       │
            ┌──────────▼──────────┐
            │  Token 存在?         │
            └──────┬─────────┬────┘
                   │ 是      │ 否
                   ▼         ▼
          附加 Authorization  直接发送
          Bearer <token>
                   │         │
                   └────┬────┘
                        ▼
                   发送请求
```

### 8.4 响应拦截器

```
                   接收响应
                       │
                       ▼
            ┌─────────────────────┐
            │  检查 HTTP 状态码    │
            └──────────┬──────────┘
                       │
          ┌────────────┼────────────┐
          │            │            │
          ▼            ▼            ▼
        2xx          401          403
          │            │            │
          ▼            ▼            ▼
     返回 data    清除 Token   权限不足提示
                 跳转 /login   (Toast/Modal)
          │
          ▼
        4xx/5xx
          │
          ▼
    统一错误提示
    (Toast/Notification)
```

**HTTP 状态码映射:**

| 状态码 | 处理策略 |
|--------|----------|
| **200** | 正常返回 `response.data` |
| **400** | 参数错误，展示后端返回的 `message` |
| **401** | 未认证 → 清除 `authStore.token` → 跳转 `/login` |
| **403** | 权限不足 → 显示 "您没有权限执行此操作" |
| **404** | 资源不存在 → 显示 404 提示 |
| **409** | 业务冲突 → 显示后端 `message` (如库存不足) |
| **500** | 服务器错误 → 显示 "系统繁忙，请稍后重试" |

---

## 九、shared 共享层设计

### 9.1 目录结构

```
frontend/shared/
├── components/       # 公共 UI 组件
├── types/            # 公共 TypeScript 类型
├── utils/            # 工具函数
├── constants/        # 公共常量
├── api/              # 公共请求封装
└── README.md
```

### 9.2 各模块职责

#### components/ — 公共 UI 组件

可被三端共同引用的通用 UI 组件：

- `Loading` — 加载中组件
- `Empty` — 空状态组件
- `ErrorBoundary` — 错误边界组件
- `ConfirmButton` — 确认操作按钮
- `StatusTag` — 状态标签 (订单状态、支付状态等)
- `PaginationWrapper` — 分页容器

#### types/ — 公共 TypeScript 类型

三端共享的类型定义：

- `user.ts` — 用户相关类型 (`UserInfo`, `Address`, etc.)
- `product.ts` — 商品相关类型 (`Product`, `SKU`, `Category`, etc.)
- `order.ts` — 订单相关类型 (`Order`, `OrderItem`, `OrderStatus`, etc.)
- `payment.ts` — 支付相关类型 (`PaymentInfo`, `RefundInfo`, etc.)
- `common.ts` — 通用类型 (`Result<T>`, `PageResult<T>`, `PageRequest`, etc.)
- `enums.ts` — 枚举定义 (`OrderStatusEnum`, `PaymentStatusEnum`, etc.)

#### utils/ — 工具函数

- `date.ts` — 日期格式化
- `format.ts` — 金额格式化、手机号脱敏
- `validation.ts` — 通用校验函数
- `storage.ts` — localStorage/sessionStorage 封装
- `token.ts` — JWT 解析与验证

#### constants/ — 公共常量

- `api-paths.ts` — API 路径常量
- `status-codes.ts` — 业务状态码映射
- `config.ts` — 公共配置

#### api/ — 公共请求封装

- `request.ts` — Axios 实例基础封装 (仅创建实例，不含应用特定拦截器)

### 9.3 三端共享原则

| 层级 | 是否共享 | 说明 |
|------|----------|------|
| ✅ 类型定义 (`types/`) | **共享** | TypeScript 类型三端一致 |
| ✅ 工具函数 (`utils/`) | **共享** | 纯函数，无副作用 |
| ✅ UI 组件 (`components/`) | **共享** | 纯展示组件，无业务逻辑 |
| ✅ API 基础封装 (`api/`) | **共享** | 仅 Axios 实例创建，拦截器各自定义 |
| ✅ 常量 (`constants/`) | **共享** | 业务编码对照等全局常量 |
| ❌ 页面 (`pages/`) | **不共享** | 每端页面独立演进 |
| ❌ 业务逻辑 (`stores/`, `services/`, `hooks/`) | **不共享** | 三端业务逻辑各不相同 |
| ❌ 权限规则 | **不共享** | 各端权限/角色不同 |
| ❌ 路由配置 | **不共享** | 路由结构完全不同 |

**设计原因:** 保持三端独立演进，避免耦合。共享层只提供纯粹、无状态的基础能力。

---

## 十、AI 前端交互设计

### 10.1 交互流程 (customer-web)

```
用户输入消息
      │
      ▼
┌─────────────────────┐
│  AI Chat Component  │  (React 组件)
│  展示对话/发送消息    │
└────────┬────────────┘
         │
         │ POST /api/v1/ai/chat
         │ (SSE / 轮询 / WebSocket)
         ▼
┌─────────────────────┐
│  Axios Service      │  (ai.ts)
│  请求拦截器 + JWT    │
└────────┬────────────┘
         │
         ▼
┌─────────────────────┐
│  Backend Commerce   │
│  Platform            │
│  (转发 AI 请求)      │
└────────┬────────────┘
         │
         ▼
┌─────────────────────┐
│  AI Service         │  (FastAPI)
│  LLM / Agent 处理   │
└─────────────────────┘
```

### 10.2 关键约束

- **Frontend 禁止直接访问 AI Service** — 所有 AI 请求必须经过 Backend Commerce Platform 中转，由后端统一管理 API Key 和认证
- **消息流式输出** — 支持 SSE (Server-Sent Events) 流式返回 AI 回复，提升用户体验
- **对话持久化** — 对话历史保存到后端，用户可随时恢复历史会话

### 10.3 AI Chat Component 设计要点

| 要点 | 说明 |
|------|------|
| 消息气泡 | 用户消息靠右，AI 消息靠左，区分角色 |
| 流式渲染 | 接收 SSE 事件，逐字渲染 AI 回复 |
| 建议快捷回复 | AI 回复后可提供快捷操作建议 (如 "查看该商品") |
| 错误处理 | 网络错误/超时显示重试按钮 |
| 上下文保持 | 同一 conversationId 保持对话上下文 |

---

## 十一、前端安全设计

### 11.1 安全措施总览

| 安全措施 | 说明 | 实现方式 |
|----------|------|----------|
| **JWT 存储** | Token 存储于内存 (`authStore`) + localStorage 持久化 | Zustand + storage utils |
| **自动刷新** | Token 过期前自动刷新 | Axios 响应拦截器 401 + refreshToken |
| **路由保护** | 未登录跳转登录页 | Route Guard 组件 |
| **接口鉴权** | 每次请求附加 JWT | Axios 请求拦截器 |
| **XSS 防护** | 防止跨站脚本 | React JSX 自动转义 + DOMPurify 清洗富文本 |
| **CSRF 防护** | 不依赖 Cookie 认证 | Bearer Token 天然免疫 CSRF |
| **敏感信息脱敏** | 手机号/Token 不全量展示 | utils/format.ts 脱敏函数 |
| **HTTPS 强制** | 生产环境强制 HTTPS | Nginx / CDN 配置 |
| **点击劫持防护** | 禁止 iframe 嵌套 | `X-Frame-Options: DENY` |
| **CSP** | 内容安全策略 | HTTP Header `Content-Security-Policy` |

### 11.2 JWT 存储策略

```
┌─────────────────────────────────────────┐
│  authStore (Zustand) — 内存存储          │
│  · token                                 │
│  · refreshToken                          │
│  · userInfo                              │
│  · role                                  │
├─────────────────────────────────────────┤
│  localStorage                            │
│  · persist: 应用刷新后恢复 token          │
│  · 加密存储 (可选)                       │
├─────────────────────────────────────────┤
│  API 请求 (Axios Header)                  │
│  · Authorization: Bearer <token>         │
└─────────────────────────────────────────┘
```

### 11.3 XSS 防护策略

| 层级 | 防护措施 |
|------|----------|
| React 框架层 | JSX 默认转义变量中的 HTML 标签 |
| 富文本层 | 使用 `DOMPurify` 清洗用户输入的 HTML 内容 |
| HTTP 层 | 后端响应设置 `Content-Security-Policy` 头 |
| 输入层 | 表单输入验证，拒绝可疑字符 |

### 11.4 敏感信息处理

| 敏感数据 | 处理方式 |
|----------|----------|
| 手机号 | 显示为 `138****1234` |
| 身份证 | 显示为 `320***********1234` |
| JWT Token | 仅存储在内存/authStore，不在 URL 或 console 中暴露 |
| 支付信息 | 支付密码/验证码不在前端日志/console 中打印 |

---

## 十二、构建与部署

### 12.1 构建命令

```bash
# customer-web
cd customer-web && npm run build    # 输出: customer-web/dist/

# merchant-web
cd merchant-web && npm run build    # 输出: merchant-web/dist/

# admin-web
cd admin-web && npm run build       # 输出: admin-web/dist/
```

### 12.2 环境变量

每个应用使用 `.env` 文件管理环境配置:

```env
# .env (默认)
VITE_API_BASE_URL=http://localhost:8080

# .env.production
VITE_API_BASE_URL=https://api.example.com
```

### 12.3 部署架构

```
         DNS (example.com)
              │
    ┌─────────┼─────────┐
    │         │         │
    ▼         ▼         ▼
shop.     merchant.   admin.
example.  example.    example.
com       com         com
    │         │         │
    ▼         ▼         ▼
┌─────────────────────────────────────┐
│         CDN / Nginx (静态资源)        │
│  customer-web/dist                   │
│  merchant-web/dist                   │
│  admin-web/dist                      │
└─────────────────────────────────────┘
              │
              ▼
    ┌─────────────────┐
    │   API Gateway   │
    └─────────────────┘
```

---

## 附录 A: 三端对比总览

| 维度 | customer-web | merchant-web | admin-web |
|------|-------------|-------------|-----------|
| **受众** | 消费者 | 商家/店铺运营 | 平台管理员 |
| **角色** | `USER` | `MERCHANT` | `ADMIN` |
| **Layout** | Header + Content + Footer | Sidebar + Header + Content | Sidebar + Header + Content |
| **路由页数** | 8 个 | 9 个 | 8 个 |
| **权限校验** | 白名单部分免登 | 全部需登录 | 全部需登录 |
| **特有 Store** | authStore + cartStore + aiStore + appStore | authStore + appStore | authStore + appStore |
| **登录页** | 无独立登录页 (内嵌) | `/login` | `/login` |

## 附录 B: 目录结构速查

```
frontend/
├── customer-web/           # C 端购物商城
│   ├── src/
│   │   ├── pages/          # Home, ProductList, ProductDetail, Cart, OrderList, OrderDetail, Profile, AIChat, NotFound
│   │   ├── layouts/        # BasicLayout
│   │   ├── components/     # 业务组件 + common/ 通用组件
│   │   ├── hooks/          # 自定义 Hooks
│   │   ├── stores/         # authStore, cartStore, aiStore, appStore
│   │   ├── services/       # auth.ts, product.ts, cart.ts, order.ts, payment.ts, ai.ts
│   │   ├── router/         # router/index.tsx (Route Guard)
│   │   ├── types/          # 页面级类型定义
│   │   ├── utils/          # 工具函数
│   │   ├── api/            # request.ts (Axios 实例 + 拦截器)
│   │   ├── assets/         # 静态资源
│   │   ├── App.tsx
│   │   ├── App.css
│   │   ├── index.css
│   │   └── main.tsx
│   ├── .env.example
│   ├── .prettierrc
│   ├── .prettierignore
│   ├── eslint.config.js
│   ├── index.html
│   ├── package.json
│   ├── tsconfig.json
│   └── vite.config.ts
│
├── merchant-web/           # 商家运营后台
│   ├── src/
│   │   ├── pages/          # Login, Dashboard, ProductManage, ProductCreate, ProductEdit, OrderManage, InventoryManage, Statistics, ShopSettings
│   │   ├── layouts/        # MerchantLayout
│   │   ├── components/
│   │   ├── hooks/
│   │   ├── stores/         # authStore, appStore
│   │   ├── services/       # auth.ts, product.ts, order.ts, statistics.ts
│   │   ├── router/         # router/index.tsx (Route Guard)
│   │   ├── types/
│   │   ├── utils/
│   │   ├── api/            # request.ts
│   │   ├── assets/
│   │   ├── App.tsx
│   │   ├── App.css
│   │   ├── index.css
│   │   └── main.tsx
│   ├── .env.example
│   ├── ...配置文件
│
├── admin-web/              # 平台运营管理后台
│   ├── src/
│   │   ├── pages/          # Login, Dashboard, UserManage, MerchantManage, ProductManage, OrderManage, SystemConfig, AIManagement
│   │   ├── layouts/        # AdminLayout
│   │   ├── components/
│   │   ├── hooks/
│   │   ├── stores/         # authStore, appStore
│   │   ├── services/       # auth.ts, user.ts, merchant.ts, product.ts, order.ts, system.ts, ai.ts
│   │   ├── router/         # router/index.tsx (Route Guard)
│   │   ├── types/
│   │   ├── utils/
│   │   ├── api/            # request.ts
│   │   ├── assets/
│   │   ├── App.tsx
│   │   ├── App.css
│   │   ├── index.css
│   │   └── main.tsx
│   ├── .env.example
│   ├── ...配置文件
│
└── shared/                 # 三端共享层
    ├── components/         # Loading, Empty, ErrorBoundary, ConfirmButton, StatusTag, PaginationWrapper
    ├── types/              # user.ts, product.ts, order.ts, payment.ts, common.ts, enums.ts
    ├── utils/              # date.ts, format.ts, validation.ts, storage.ts, token.ts
    ├── constants/          # api-paths.ts, status-codes.ts, config.ts
    ├── api/                # request.ts (基础 Axios 实例封装)
    └── README.md
```

---

> **下一步:** 基于此架构设计，进入 Sprint 1 开始实现各端页面与业务逻辑。