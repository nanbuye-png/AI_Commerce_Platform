# AI Commerce Platform

面向 Customer、Merchant 和 Admin 三类客户端的电商平台。当前仓库以 Spring Boot Commerce Core 为交易真相源，Python FastAPI 服务作为后续 AI 编排边界。

## 项目结构

```
AI_Commerce_Platform/
├── backend/
│   └── commerce-platform/       # Spring Boot 后端服务
├── frontend/
│   ├── customer-web/            # C 端用户前台 (React)
│   ├── merchant-web/            # 商家管理后台 (React)
│   ├── admin-web/               # 平台管理后台 (React)
│   └── shared/                  # 前端共享代码
├── ai-service/                  # AI 智能服务 (Python/FastAPI)
├── infrastructure/              # Docker/K8s 基础设施配置
├── scripts/                     # 工具脚本
└── reports/                     # 开发过程报告
```

## 技术栈

| 层级 | 技术 |
|------|------|
| 前端 | React 19 + TypeScript + Vite + Ant Design |
| 后端 | Spring Boot 3 + Java 21 + PostgreSQL 16 |
| AI 服务 | FastAPI + Python 3.12 |
| 基础设施 | Docker + Kubernetes |

## 当前基线

- Commerce Core 使用 Flyway 管理 Schema，Hibernate 只执行 `validate`。
- Customer、Merchant、Admin 使用独立 JWT 密钥和角色边界。
- 三套前端均启用 typed ESLint，并通过生产构建。
- AI Service 当前只提供健康检查，尚未接入 LLM、RAG 或 Commerce Tool。

## 本地启动

### 前置环境

- JDK 21
- Node.js 22+
- Maven 3.8+
- Python 3.12+
- PostgreSQL 16（默认数据库名 `ai_commerce_platform`）

后端通过环境变量读取数据库和 JWT 配置，不应把生产密钥写入仓库：

```text
DB_URL
DB_USERNAME
DB_PASSWORD
JWT_SECRET
JWT_CUSTOMER_WEB_SECRET
JWT_MERCHANT_WEB_SECRET
JWT_ADMIN_WEB_SECRET
```

### 1. 创建数据库

```sql
CREATE DATABASE ai_commerce_platform;
```

### 2. 启动后端

```bash
cd backend/commerce-platform
mvn spring-boot:run
```

后端默认监听 `8080`。启动时 Flyway 应用版本化迁移，随后 Hibernate 校验实体与 Schema 是否一致。

### 3. 启动前端

```bash
cd frontend/customer-web
npm ci
npm run dev

cd frontend/merchant-web
npm ci
npm run dev

cd frontend/admin-web
npm ci
npm run dev
```

### 4. 启动 AI Service

```bash
cd ai-service
python -m venv .venv
# Windows PowerShell: .venv\Scripts\Activate.ps1
# Linux/macOS: source .venv/bin/activate
python -m pip install -r requirements.txt -r requirements-dev.txt
python -m uvicorn app.main:app --reload
```

AI Service 默认监听 `8000`，健康检查为 `GET /api/v1/health`。

## 质量门禁

GitHub Actions 会并行验证 Commerce Core、三套前端和 AI Service。本地可运行等价命令：

```bash
mvn -f backend/commerce-platform/pom.xml test

npm --prefix frontend/customer-web run lint
npm --prefix frontend/customer-web run build
npm --prefix frontend/merchant-web run lint
npm --prefix frontend/merchant-web run build
npm --prefix frontend/admin-web run lint
npm --prefix frontend/admin-web run build

python -m pip install -r ai-service/requirements.txt -r ai-service/requirements-dev.txt
python -m pytest -c ai-service/pytest.ini ai-service/tests
```

## API 入口

- **用户注册**: `POST /api/auth/register`
- **用户登录**: `POST /api/auth/login`

## 下一阶段

- 在 Commerce Core 增加 AI Gateway / AI Application 边界；
- 打通 Customer Web 到 Python AI Service 的受控流式链路；
- 以自然语言商品搜索作为首个 AI 垂直切片；
- 逐步补齐 RAG、Tool 授权、审计、评测和可观测性。