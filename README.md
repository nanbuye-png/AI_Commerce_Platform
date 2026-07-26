# AI Commerce Platform

AI 驱动的智能电商平台 —— 融合传统电商与 AI 智能服务。

## 项目结构

```
AI_Commerce_Platform/
├── backend/                 # 后端服务（Spring Boot）
│   └── commerce-platform/   # 核心电商业务
│       ├── product/         # 商品域（Sprint 9）
│       ├── inventory/       # 库存域（Sprint 10）
│       └── order/           # 订单域（Sprint 11）
├── frontend/               # 前端应用（React + Vite）
│   ├── customer-web/       # C 端用户前台
│   ├── merchant-web/       # 商户管理后台
│   ├── admin-web/          # 平台运营管理后台
│   └── shared/             # 共享工具库
├── ai-service/             # AI 智能服务（FastAPI / Python）
├── infrastructure/         # 基础设施配置（Docker/K8s）
└── ...
```

## 已完成 Sprint

| Sprint | 内容 | 状态 |
|--------|------|------|
| 9 | Product Domain — 商品 SPU/SKU/规格/图片/分类/审核 | ✅ 完成 |
| 10 | Inventory Domain — 库存三字段模型/预占/流水审计 | ✅ 完成 |
| 10.5 | Domain Integration Review — Product + Inventory 统一验收 | ✅ 完成 |
| 11 | Order Domain — 订单创建/查询/商家发货/状态管理 | ✅ 完成 |

## 快速开始

详见 [docs/dev-guide.md](docs/dev-guide.md)

## 技术栈

| 层级 | 技术 |
|------|------|
| 前端 | React 18 + TypeScript + Vite + Ant Design |
| 后端 | Spring Boot 3 + JDK 21 + PostgreSQL |
| AI 服务 | FastAPI + Python 3.10+ |
| 基础设施 | Docker + Kubernetes |