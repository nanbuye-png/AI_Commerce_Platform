# Git 使用规范

## 分支策略

```
main          ── 生产分支，只允许从 release/ 合并
  ├── release/    ── 预发布分支，从 develop 检出
  └── develop     ── 主开发分支
        ├── feature/   ── 功能分支
        ├── bugfix/    ── Bug 修复分支
        └── hotfix/    ── 紧急修复分支（直接基于 main）
```

## 分支命名

- `feature/xxx` — 新功能，如 `feature/user-login`
- `bugfix/xxx` — 修复，如 `bugfix/order-amount-precision`
- `hotfix/xxx` — 紧急修复，如 `hotfix/payment-timeout`
- `release/x.x.x` — 发布，如 `release/1.0.0`

## 提交信息格式

```
<type>: <subject>

[optional body]
```

### 类型
| 类型 | 说明 |
|------|------|
| feat | 新功能 |
| fix | 修复 Bug |
| docs | 文档更新 |
| style | 代码格式 |
| refactor | 重构 |
| test | 测试 |
| chore | 构建/CI/杂项 |

### 示例
```
feat: 添加用户注册接口
fix: 修复订单金额计算精度问题
docs: 更新 API 文档
refactor: 重构用户认证逻辑
```

## 工作流

1. 从 `develop` 检出 `feature/xxx` 分支
2. 开发完成后提交 PR → `develop`
3. 代码评审通过后合并
4. 发布时从 `develop` 检出 `release/x.x.x`
5. 测试通过后合并到 `main` 和 `develop`

## 配置 .gitmessage

```bash
git config commit.template .gitmessage