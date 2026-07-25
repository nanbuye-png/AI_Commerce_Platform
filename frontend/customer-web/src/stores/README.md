# Zustand 状态管理目录

本目录存放全局状态管理模块。

## 规划中的 Store

| Store | 用途 | 状态 |
|-------|------|------|
| userStore | 用户登录态、个人信息 | 待实现 |
| cartStore | 购物车状态 | 待实现 |
| aiSessionStore | AI 对话会话状态 | 待实现 |
| configStore | 全局配置（主题、语言等） | 待实现 |

## 使用规范

- 每个 Store 独立文件，使用 `create` 创建
- 优先使用 `shallow` 进行选择器优化
- 异步操作使用 `async/await`