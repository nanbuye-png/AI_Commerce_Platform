# 开发指南

## 环境要求

| 工具 | 版本要求 |
|------|----------|
| JDK | 21+ |
| Node.js | 18+ |
| Python | 3.10+ |
| Docker | 24+ |
| PostgreSQL | 15+ |

## 快速启动

### 1. 后端（Spring Boot）

```bash
cd backend/commerce-platform
./mvnw spring-boot:run
```

### 2. 前端

```bash
# 安装依赖（任一前端项目）
cd frontend/customer-web
npm install
npm run dev
```

### 3. AI 服务

```bash
cd ai-service
pip install -r requirements.txt
uvicorn app.main:app --reload
```

### 4. 基础设施（Docker）

```bash
docker compose -f infrastructure/docker-compose.yml up -d
```

## 项目结构说明

```
AI_Commerce_Platform/
├── backend/                 # 后端服务
│   └── commerce-platform/   # 核心电商业务
├── frontend/               # 前端应用
│   ├── customer-web/       # C 端用户
│   ├── merchant-web/       # 商户管理
│   ├── admin-web/          # 平台运营
│   └── shared/             # 共享工具
├── ai-service/             # AI 智能服务
├── docs/                   # 项目文档
└── infrastructure/         # 基础设施配置
```

## 代码规范

- **前端**: ESLint + Prettier，提交前自动格式化
- **后端**: 遵循阿里巴巴 Java 开发手册
- **Python**: PEP 8，使用 Black 格式化

## 提交规范

遵循 Conventional Commits：

- `feat:` 新功能
- `fix:` 修复
- `docs:` 文档更新
- `refactor:` 重构
- `test:` 测试
- `chore:` 构建/工具