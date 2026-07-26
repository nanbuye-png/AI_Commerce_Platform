# Database Migration Strategy — 确认报告

> **日期:** 2026-07-26  
> **状态:** ✅ 确认完成

---

## 1. 当前 Hibernate 配置

| 配置项 | 当前值 | 说明 |
|--------|--------|------|
| `spring.jpa.hibernate.ddl-auto` | **`validate`** | ✅ 仅做 Entity ↔ 数据库结构校验 |
| `spring.jpa.show-sql` | `false` | — |
| `spring.jpa.properties.hibernate.dialect` | `org.hibernate.dialect.PostgreSQLDialect` | PostgreSQL 方言 |

**结论：** Hibernate `ddl-auto` 已配置为 `validate`。

---

## 2. 当前 Flyway 配置

| 配置项 | 当前值 |
|--------|--------|
| `spring.flyway.enabled` | `true` |
| `spring.flyway.locations` | `classpath:db/migration` |
| `spring.flyway.baseline-on-migrate` | `true` |
| `spring.flyway.baseline-version` | `0` |

**结论：** Flyway 已启用，迁移目录配置正确。

---

## 3. Migration 列表

| 版本号 | 文件名 | 说明 | 状态 |
|--------|--------|------|------|
| V1 | `V1__init_schema.sql` | 初始 Schema（users / roles / permissions 等） | ✅ |
| V2 | `V2__create_product_tables.sql` | Product Domain 5 张表 | ✅ 新建 |
| — | — | — | — |
| V3 | 预留 | 后续 Sprint（库存/订单等模块） | — |
| V4 | 预留 | 后续 Sprint | — |
| V5 | 预留 | 后续 Sprint | — |

**版本号连续性检查：**
- V1 → V2 ✅ 连续（无跳号）
- 无重复版本号 ✅
- 无重复文件名 ✅

---

## 4. 统一策略确认

| 项目 | 策略 |
|------|------|
| **数据库迁移工具** | Flyway（唯一工具） |
| **Hibernate 角色** | `validate`，仅负责校验 Entity 与数据库结构一致性 |
| **禁止配置** | `create` / `create-drop` / `update` — 不允许在任何环境使用 |
| **本地开发** | 必须启动本地 PostgreSQL + Flyway migrate，**禁止**使用 `ddl-auto=update` |

---

## 5. 是否存在 update/create/create-drop 配置

**扫描结果：** 项目中不存在 `application-dev.yml` 或 `application-prod.yml` 配置文件。

`application.yml` 中 `spring.jpa.hibernate.ddl-auto` 的值：

| 可能的值 | 是否存在 |
|----------|----------|
| `validate` | ✅ 当前值 |
| `update` | ❌ 不存在 |
| `create` | ❌ 不存在 |
| `create-drop` | ❌ 不存在 |

**结论：** ✅ 项目中无 `update` / `create` / `create-drop` 配置。

---

## 6. 后续开发规范（Sprint 9 起）

任何数据库结构修改必须按照以下流程：

```
1. 创建新的 Flyway Migration
   └── src/main/resources/db/migration/V3__xxx.sql

2. 保持版本号连续递增
   └── 禁止跳号 / 禁止重复版本号

3. 不修改历史 Migration 文件
   └── 已执行的 Migration 禁止修改

4. 不依赖 ddl-auto=update
   └── ddl-auto=validate 保持不变

5. 示例命名
   ├── V3__product_extend.sql
   ├── V4__inventory.sql
   ├── V5__order.sql
   └── V6__payment.sql
```

**禁止：**
- ❌ 修改已执行的 Migration
- ❌ 重复版本号 / 跳号
- ❌ 任何环境使用 `ddl-auto=update`
- ❌ 手动直接修改生产数据库
- ❌ 不使用 Flyway 直接改表结构

---

## 7. 文档更新

| 文档 | 修改内容 | 状态 |
|------|----------|------|
| `docs/architecture.md` | Database Migration Strategy 章节 - 移除"本地开发可用 update"推荐，新增 Flyway 全域 + Hibernate validate 规范 | ✅ |
| `docs/database-design.md` | 本地开发章节 - 明确"所有环境使用 Flyway + validate，禁止本地开发使用 ddl-auto=update" | ✅ |

---

## 8. Spring Boot 启动验证

| 检查项 | 结果 |
|--------|------|
| Maven Compile | ✅ 编译通过（0 错误） |
| Flyway Migration | ✅ V1 / V2 版本号连续，配置正确 |
| Hibernate validate | ✅ `ddl-auto=validate` |
| `update` / `create` / `create-drop` | ❌ 不存在 |
| 文档已同步 | ✅ architecture.md + database-design.md |

---

## 附录：相关配置文件路径

| 文件 | 路径 |
|------|------|
| 主配置文件 | `backend/commerce-platform/src/main/resources/application.yml` |
| Flyway V1 | `backend/commerce-platform/src/main/resources/db/migration/V1__init_schema.sql` |
| Flyway V2 | `backend/commerce-platform/src/main/resources/db/migration/V2__create_product_tables.sql` |
| 架构文档 | `docs/architecture.md`（第 293–322 行） |
| 数据库设计文档 | `docs/database-design.md`（第 657–696 行） |

---

> **文档版本:** v1.0  
> **最后更新:** 2026-07-26  
> **状态:** 正式确认 — 所有环境统一使用 Flyway + validate 方案