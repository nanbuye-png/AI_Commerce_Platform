# 开发指南

## 环境要求

| 工具 | 版本要求 |
|------|----------|
| JDK | 21 |
| Maven | 3.8+ |
| PostgreSQL | 15+ |
| Node.js | 18+（前端开发）|

## 快速启动

### 1. 数据库准备

```bash
# 创建 PostgreSQL 数据库
psql -U postgres
CREATE DATABASE ai_commerce;

# 确认数据库连接信息与 application.yml 一致：
#   url: jdbc:postgresql://localhost:5432/ai_commerce
#   username: postgres
#   password: 123456
```

### 2. 后端（Spring Boot）

```bash
cd backend/commerce-platform

# 仅编译验证（无需数据库）
mvn clean compile

# 启动完整服务（需要 PostgreSQL 已运行）
mvn spring-boot:run

# Flyway 会在启动时自动执行数据库迁移
# 迁移文件位置：src/main/resources/db/migration/
# 当前迁移版本：V1 ~ V5
```

### 3. 前端（可选，当前以后端开发为主）

```bash
# 安装依赖（任一前端项目）
cd frontend/customer-web
npm install
npm run dev
```

### 4. AI 服务（可选）

```bash
cd ai-service
pip install -r requirements.txt
uvicorn app.main:app --reload
```

## 项目模块说明

```
backend/commerce-platform/src/main/java/com/commerce/platform/
├── common/         # 公共模块：BaseEntity, Result, BusinessException, PageResult
├── product/        # 商品域（Sprint 9）：商品 SPU/SKU/规格/图片/分类/审核
├── inventory/      # 库存域（Sprint 10）：库存三字段模型/预占/流水审计
└── order/          # 订单域（Sprint 11）：订单创建/查询/商家发货/状态管理
```

## 数据库迁移（Flyway）

```bash
# 所有表由 Flyway 管理，禁止 Hibernate 自动建表
# spring.jpa.hibernate.ddl-auto=validate

# 迁移文件命名规范：
# V{版本号}__{描述}.sql
# 示例：V5__create_order_tables.sql

# 已发布迁移（不可修改已有版本）：
V1__init.sql                    # Sprint 1 - 基础表
V2__create_product_tables.sql   # Sprint 9 - 商品域
V3__inventory_domain.sql        # Sprint 10 - 库存域
V4__inventory_movement_audit.sql # Sprint 10 - 库存流水
V5__create_order_tables.sql     # Sprint 11 - 订单域
```

## 编译验证

```bash
cd backend/commerce-platform
mvn clean compile

# 预期输出：BUILD SUCCESS
# 当前源文件数：132
```

## API 测试

启动服务后，API 路径格式：

| 角色 | 基础路径 | 说明 |
|------|----------|------|
| Customer | `/api/orders` | C 端订单 |
| Merchant | `/api/merchant/orders` | 商家订单 |
| Admin | `/api/admin/orders` | 管理后台 |
| Customer | `/api/products` | 商品浏览 |
| Merchant | `/api/merchant/products` | 商家商品管理 |
| Merchant | `/api/merchant/inventory` | 商家库存管理 |

## 代码规范

- **后端**: 遵循阿里巴巴 Java 开发手册，Entity 只存数据不写业务逻辑
- **DDD 分层**: Controller → ApplicationService → DomainService → Repository → Entity
- **跨域引用**: 仅存 Long ID 弱引用，不建立 JPA 跨域关联

## 提交规范

遵循 Conventional Commits：

- `feat:` 新功能
- `fix:` 修复
- `docs:` 文档更新
- `refactor:` 重构
- `test:` 测试
- `chore:` 构建/工具