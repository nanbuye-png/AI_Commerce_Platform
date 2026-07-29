# AI Commerce Platform

AI 驱动的智能电商平台 —— 融合传统电商与 AI 智能服务。

## 项目结构

```
AI_Commerce_Platform/
├── backend/
│   └── commerce-platform/       # Spring Boot 后端服务
├── frontend/
│   ├── customer-web/            # C 端用户前台 (React)
│   ├── merchant-web/            # 商家管理后台 (React)
│   └── admin-web/               # 平台管理后台 (React)
├── ai-service/                  # AI 智能服务 (Python/FastAPI)
├── infrastructure/              # Docker/K8s 基础设施配置
├── scripts/                     # 工具脚本
└── reports/                     # 开发过程报告
```

## 技术栈

| 层级 | 技术 |
|------|------|
| 前端 | React 18 + TypeScript + Vite + Ant Design |
| 后端 | Spring Boot 3 + JDK 21 + PostgreSQL |
| AI 服务 | FastAPI + Python 3.10+ |
| 基础设施 | Docker + Kubernetes |

## 项目启动步骤

### 前置环境

- JDK 21
- Node.js 18+
- Maven 3.8+
- PostgreSQL（数据库名 `ai_commerce`，用户名/密码见 `application.yml`）

确保 `application.yml` 中的数据库连接配置正确。

### 1. 创建数据库

```sql
CREATE DATABASE ai_commerce;
```

### 2. 启动后端

```bash
cd backend/commerce-platform
mvn spring-boot:run
```

- 端口：`8080`
- 首次启动自动建表（`ddl-auto: create`）

### 3. 启动前端

```bash
# C 端用户前台
cd frontend/customer-web
npm install && npm run dev

# 商家管理后台
cd frontend/merchant-web
npm install && npm run dev

# 平台管理后台
cd frontend/admin-web
npm install && npm run dev
```

### 4. 注册账户

- **用户注册**: `POST /api/auth/register`
- **用户登录**: `POST /api/auth/login`

## 未来扩展计划

- 内置种子数据初始化（Admin / Merchant / Customer 预设账户）
- AI 商品推荐引擎接入
- 分布式部署支持（Kubernetes）
- CI/CD 流水线