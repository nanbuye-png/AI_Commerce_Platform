# Zustand 状态管理目录

本目录存放全局状态管理模块。

## 当前 Store

| Store | 用途 | 状态 |
|-------|------|------|
| authStore | 用户登录态、Token 和个人信息 | 已实现 |
| cartStore | 购物车状态与服务端同步 | 已实现 |
| aiStore | AI 对话会话状态 | 已实现 |

主题、语言等全局配置当前没有对外功能，因此不保留空的 `configStore` 占位。

## 使用规范

- 每个 Store 独立文件，使用 `create` 创建
- 优先使用 `shallow` 进行选择器优化
- 异步操作使用 `async/await`
