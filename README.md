# AI Commerce Platform

面向 Customer、Merchant 和 Admin 三类客户端的 AI 电商平台。以 Spring Boot Commerce Core 为交易真相源，AI Service 通过受保护的 Commerce Tool 提供自然语言商品搜索等智能能力，三套 React 前端分别服务 C 端用户、商家后台和平台管理后台。

## 技术栈

| 层级 | 技术 |
|------|------|
| 前端 | React 19 + TypeScript + Vite + Ant Design |
| 后端 | Spring Boot 3 + Java 21 + PostgreSQL 16 |
| AI 服务 | FastAPI + Python 3.12 |
| 基础设施 | Docker + Kubernetes |

## 快速启动

### 前置环境

- JDK 21
- Node.js 22+
- Maven 3.8+
- Python 3.12+
- PostgreSQL 16（默认数据库名 `ai_commerce_platform`）

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

Linux/macOS 使用 `cp` 执行同样操作。真实 `.env` 已被 Git 忽略；请修改数据库密码，并为两个文件设置完全相同的内部令牌：

```text
AI_INTERNAL_API_TOKEN=<shared-random-secret>
```

### 3. 启动后端

```bash
cd backend/commerce-platform
mvn spring-boot:run
```

后端默认监听 `8080`。

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

三端开发端口已固定：customer-web `5173`、merchant-web `5174`、admin-web `5175`。

### 5. 启动 AI Service

```bash
cd ai-service
python -m venv .venv
# Windows PowerShell: .venv\Scripts\Activate.ps1
# Linux/macOS: source .venv/bin/activate
python -m pip install -r requirements.txt -r requirements-dev.txt
python -m uvicorn app.main:app --reload
```

AI Service 默认监听 `8000`。LLM 默认使用无需外部 API Key 的确定性 Mock Provider；要连接 OpenAI 或兼容协议的服务，设置：

```text
AI_LLM_PROVIDER=openai-compatible
AI_LLM_BASE_URL=https://api.openai.com/v1
AI_LLM_API_KEY=<provider-secret>
AI_DEFAULT_MODEL=gpt-4o-mini
```

启动顺序：PostgreSQL → backend → ai-service → 三个前端。

## 未来计划

- 将 Commerce 关键业务事件正式接入 OutboxService，并处理三套前端入口包拆分
- 建立可复现的容器化部署、密钥注入、readiness 和跨服务 SSE smoke test
- 增加跨服务 trace、TTFT/错误率/成本指标和离线质量评测基线，再推进 RAG