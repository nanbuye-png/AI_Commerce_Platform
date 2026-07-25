# Sprint 1 Step 5 Completion Report

> **日期:** 2026-07-25  
> **模块:** React 前端认证基础能力  
> **状态:** 完成

---

## 1. 前端项目结构

```
frontend/
├── shared/                          # 三端共享层
│   ├── types/
│   │   └── auth.ts                  # 认证相关类型定义（新增）
│   └── utils/
│       └── token.ts                 # Token 存储工具（新增）
│
└── customer-web/                    # C 端应用
    └── src/
        ├── api/
        │   └── request.ts           # Axios 封装（拦截器增强）
        ├── stores/
        │   ├── README.md            # Store 规划文档（已存在）
        │   └── authStore.ts         # Zustand 认证状态管理（新增）
        ├── services/
        │   └── auth.ts              # 认证 API 服务（新增）
        ├── pages/
        │   ├── Login.tsx            # 登录页面（新增）
        │   ├── Register.tsx         # 注册页面（新增）
        │   ├── Home.tsx             # 首页（已存在）
        │   └── NotFound.tsx         # 404 页面（已存在）
        ├── router/
        │   ├── index.tsx            # 路由配置（更新）
        │   └── ProtectedRoute.tsx   # 路由守卫组件（新增）
        └── main.tsx                 # 入口文件（已存在，无需修改）
```

---

## 2. 新增文件列表

| 文件 | 说明 |
|------|------|
| `frontend/shared/types/auth.ts` | 认证相关类型定义（LoginRequest, RegisterRequest, AuthResponse, UserInfo, ApiResult） |
| `frontend/shared/utils/token.ts` | Token 存取工具（setToken/getToken/removeToken） |
| `frontend/customer-web/src/stores/authStore.ts` | Zustand 状态管理 Store（token/userInfo/isAuthenticated + login/logout/setUser/init） |
| `frontend/customer-web/src/services/auth.ts` | 认证 API 封装（loginApi/registerApi） |
| `frontend/customer-web/src/pages/Login.tsx` | 登录页面（account + password 表单） |
| `frontend/customer-web/src/pages/Register.tsx` | 注册页面（username + email + password + nickname 表单） |
| `frontend/customer-web/src/router/ProtectedRoute.tsx` | 路由守卫（未登录跳转 /login） |

---

## 3. 修改文件列表

| 文件 | 修改内容 |
|------|----------|
| `frontend/customer-web/src/api/request.ts` | 新增请求拦截器（自动注入 `Authorization: Bearer token`）；新增响应拦截器（401 → 清除 Token + 跳转登录，403 → 无权限提示，500 → 系统错误） |
| `frontend/customer-web/src/router/index.tsx` | 新增 `/login` 和 `/register` 路由；受保护路由包裹 `ProtectedRoute` |
| `frontend/customer-web/vite.config.ts` | 新增 `@shared` 路径别名 |
| `frontend/customer-web/tsconfig.app.json` | 新增 `baseUrl` + `paths` 配置（`@shared/* → ../shared/*`）|

---

## 4. 登录流程说明

```
用户访问 /login
       │
       ▼
  填写 account + password
       │
       ▼
  点击登录按钮
       │
       ▼
  POST /api/auth/login
       │
       ▼
  成功: 返回 { token, userId, username, role }
       │
       ├── authStore.login(token, userInfo)       → 保存 Token 到 localStorage
       ├── Axios 请求拦截器自动附加 Authorization 头
       └── navigate('/')                          → 跳转首页
       │
       ▼
  失败: 显示错误提示（后端返回的 message）
```

**路由守卫:** 未登录用户访问受保护页面（如首页），自动跳转 `/login`。

---

## 5. Token 存储方案

| 项目 | 说明 |
|------|------|
| 存储方式 | `localStorage` |
| Key 名称 | `access_token` |
| 存储函数 | `setToken(token)` — 登录后调用 |
| 读取函数 | `getToken()` — 请求拦截器读取 |
| 清除函数 | `removeToken()` — 退出/401 时调用 |
| 状态同步 | `authStore.init()` — 应用启动时从 localStorage 恢复 token 到 Zustand |

**数据流:**
```
登录成功 → setToken(localStorage) + authStore.login(内存)
请求发出 → getToken(localStorage) → Authorization: Bearer token
401 响应 → removeToken() → authStore.logout() → 跳转 /login
```

---

## 6. API 联调结果

| 接口 | 方法 | 路径 | 状态 |
|------|------|------|------|
| 用户注册 | POST | `/api/auth/register` | 前后端对接完成 |
| 用户登录 | POST | `/api/auth/login` | 前后端对接完成 |

**请求/响应格式对照:**

**注册:**
- 请求: `{ username, email, password, nickname?, phone? }` → `POST /api/auth/register`
- 响应: `{ code: 0, message: "success", data: { id, username, nickname, email, role, status } }`

**登录:**
- 请求: `{ account, password }` → `POST /api/auth/login`
- 响应: `{ code: 0, message: "success", data: { token, userId, username, role } }`

---

## 7. Build 测试结果

```
> customer-web@0.0.0 build
> tsc -b && vite build

✓ 1542 modules transformed.
✓ built in 5.26s

dist/index.html                  0.38 kB │ gzip:   0.26 kB
dist/assets/index-DcM11iq3.js  795.89 kB │ gzip: 261.33 kB
```

**结果: Build Success ✅**

- TypeScript 编译: 通过
- Vite 生产构建: 通过
- 输出目录: `frontend/customer-web/dist/`

---

## 8. 当前问题

| 问题 | 状态 | 说明 |
|------|------|------|
| 无 | ✅ | 暂无已知问题 |

**注意事项:**
1. `tsc` 使用 `baseUrl` + `paths` 需要配置 `ignoreDeprecations: "6.0"`（TypeScript 6.x 兼容）
2. `verbatimModuleSyntax` 开启时，类型导入必须使用 `import type` 语法
3. 当前 `/login` 和 `/register` 为独立页面（不在 BasicLayout 内），与架构设计一致
4. 构建警告 `chunks larger than 500 kB` 为 Ant Design 打包体积，不影响功能，后续可用动态导入优化