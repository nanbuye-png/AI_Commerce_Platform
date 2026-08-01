# AI Commerce Platform

面向 Customer、Merchant 和 Admin 三类客户端的电商平台。当前仓库以 Spring Boot Commerce Core 为交易真相源，由 Commerce Core AI Gateway 受控访问 Python FastAPI AI Service。

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
- Customer Web 已通过 Commerce Core AI Gateway 接入 SSE 流式聊天，公开端点只接受 Customer JWT。
- Commerce Core 与 AI Service 使用共享的内部服务令牌认证，令牌不会下发到浏览器。
- 自然语言商品搜索已贯通：AI Service 解析关键词、价格和分页意图，通过受保护的 Commerce Tool 查询上架商品，并在聊天流中返回可点击商品结果。
- AI Service 支持确定性的 Mock Provider 和可配置的 OpenAI-compatible 流式 Provider；默认仍使用 Mock，RAG 尚未接入。

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
AI_SERVICE_BASE_URL
AI_INTERNAL_API_TOKEN
AI_CONNECT_TIMEOUT
AI_REQUEST_TIMEOUT
COMMERCE_CORE_BASE_URL
COMMERCE_CORE_TIMEOUT_SECONDS
AI_LLM_PROVIDER
AI_LLM_BASE_URL
AI_LLM_API_KEY
AI_DEFAULT_MODEL
AI_LLM_TIMEOUT_SECONDS
AI_LLM_TEMPERATURE
AI_LLM_MAX_TOKENS
AI_LLM_CONTEXT_MAX_CHARS
```

`AI_INTERNAL_API_TOKEN` 必须使用高熵随机值，并以相同值同时注入 Commerce Core 和 AI Service。生产部署还应在网络层限制 AI Service 的 internal 路由只允许 Commerce Core 访问。

### 1. 创建数据库

```sql
CREATE DATABASE ai_commerce_platform;
```

### 2. 配置服务间认证

先从模板创建各服务自己的本地配置文件：

```powershell
Copy-Item backend/commerce-platform/.env.example backend/commerce-platform/.env
Copy-Item ai-service/.env.example ai-service/.env
```

Linux/macOS 使用 `cp` 执行同样操作。真实 `.env` 已被 Git 忽略；请修改数据库密码，并为两个文件设置完全相同的内部令牌。以下仅为占位形式，不要使用固定示例值：

```text
AI_INTERNAL_API_TOKEN=<shared-random-secret>
```

Commerce Core 默认通过 `AI_SERVICE_BASE_URL=http://localhost:8000` 访问 AI Service。

### 3. 启动后端

```bash
cd backend/commerce-platform
mvn spring-boot:run
```

后端默认监听 `8080`。启动时 Flyway 应用版本化迁移，随后 Hibernate 校验实体与 Schema 是否一致。

### 4. 启动前端

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

三端开发端口已固定：customer-web `5173`、merchant-web `5174`、admin-web `5175`，与后端 CORS 白名单一致。

> **遇到深层路由（`/products`、`/search` 等）打开 404？** 请确认是通过 `npm run dev` 或 `npm run preview` 访问的——Vite 对 SPA history 路由自带 fallback，深层路径刷新会返回 `index.html`。如果使用 `python http.server`、`nginx` 默认配置等普通静态服务器直接托管 `dist` 目录，访问 `/products` 会因不存在 `products.html` 而返回真实 404；此时应改用 `npm run preview`，或在服务器配置中将所有非静态资源路径重写到 `index.html`（如 nginx 加 `try_files $uri /index.html;`）。

### 5. 启动 AI Service

```bash
cd ai-service
python -m venv .venv
# Windows PowerShell: .venv\Scripts\Activate.ps1
# Linux/macOS: source .venv/bin/activate
python -m pip install -r requirements.txt -r requirements-dev.txt
python -m uvicorn app.main:app --reload
```

AI Service 默认监听 `8000`，健康检查为 `GET /api/v1/health`。内部流式端点为 `POST /api/v1/internal/ai/chat/stream`，必须携带 `X-Internal-Token`，不应由浏览器直接调用。AI Service 默认通过 `COMMERCE_CORE_BASE_URL=http://localhost:8080` 调用 Commerce Core，超时由 `COMMERCE_CORE_TIMEOUT_SECONDS` 配置。

LLM 默认使用无需外部 API Key 的确定性 Mock Provider，因此只验证本地完整链路时不需要填写 `AI_LLM_API_KEY`。要连接 OpenAI 或实现相同 `/chat/completions` 流式协议的服务，显式设置：

```text
AI_LLM_PROVIDER=openai-compatible
AI_LLM_BASE_URL=https://api.openai.com/v1
AI_LLM_API_KEY=<provider-secret>
AI_DEFAULT_MODEL=gpt-4o-mini
```

启用 `openai-compatible` 时缺少 API Key 会使 AI Service 启动失败。Provider 密钥只注入 AI Service，不应进入 Commerce Core、任何 Web 构建变量或版本库。上游响应会被解析为平台自己的 SSE 契约，商品搜索上下文按 `AI_LLM_CONTEXT_MAX_CHARS` 截断，客户端关闭时共享 HTTP 连接池会随应用生命周期释放。

要连接 DeepSeek（OpenAI 兼容协议），在 `ai-service/.env` 显式设置：

```text
AI_LLM_PROVIDER=openai-compatible
AI_LLM_BASE_URL=https://api.deepseek.com/v1
AI_LLM_API_KEY=<your-deepseek-api-key>
AI_DEFAULT_MODEL=deepseek-chat
```

## 初始管理员与商户注册

- 后端首次启动自动创建初始管理员（默认 `admin` / `admin123`，可在 `backend/commerce-platform/.env` 覆盖），仅供 admin-web 登录，**不开放 ADMIN 自助注册**。
- 商户通过 merchant-web `/register` 页面公开注册（自动携带 `role=MERCHANT`）。
- 商户创建商品后状态为 `PENDING_REVIEW`（待审核），需 admin-web → 商品管理 → 审核通过（`ON_SHELF`）后，用户端首页/列表/详情才可见。

需要手动填写的密钥集合（一键启动）：`backend/commerce-platform/.env` 的 `DB_PASSWORD`、三个 `JWT_*_WEB_SECRET`、`AI_INTERNAL_API_TOKEN`；`ai-service/.env` 的 `AI_INTERNAL_API_TOKEN`（与后端一致）与 `AI_LLM_API_KEY`。启动顺序：PostgreSQL → backend → ai-service → 三个前端。

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
- **Customer AI 流式聊天**: `POST /api/customer/ai/chat/stream`
- **AI Service 内部聊天**: `POST /api/v1/internal/ai/chat/stream`
- **Commerce Core 内部商品搜索**: `POST /api/internal/ai/products/search`

两个内部端点都要求相同的 `X-Internal-Token`。商品搜索仅返回上架商品，并支持 `keyword`、`minPrice`、`maxPrice`、`page` 和 `size`；令牌校验采用常量时间比较。

AI 流式聊天使用 `text/event-stream`：

- `meta` + `product_search`：返回结构化查询、商品列表和总数；
- `meta` + `product_search_error`：Commerce Tool 不可用时的可恢复提示，后续文本流仍会继续；
- `message`：携带增量 token；
- `done`：返回 `conversation_id` 和 `message_id`；
- `error`：流开始后 Provider 失败，流随即终止且不会发送 `done`。

Customer Web 使用带 Bearer JWT 的 `fetch POST` 读取并增量渲染响应，展示商品卡片、搜索降级和生成错误，也支持中止当前生成。

## 下一阶段

- 建立可复现的容器化部署、密钥注入、readiness 和跨服务 SSE smoke test；
- 增加跨服务 trace、TTFT/错误率/成本指标和离线质量评测基线，再推进 RAG；
- 将 Commerce 关键业务事件正式接入 OutboxService，并处理三套前端入口包拆分。