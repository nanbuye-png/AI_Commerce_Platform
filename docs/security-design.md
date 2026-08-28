# 安全设计

## 边界

- 浏览器只访问 Commerce Core。AI Service 只在 Compose 内部网络中可达，避免把内部令牌和工具接口暴露给用户端。
- Customer Web 发送的 `userId` 不可信。Commerce Core 从 JWT `Principal` 获取身份，并覆盖请求体中的同名字段。
- 商品价格、商品名称、图片、库存和可售状态只由 Commerce Core 根据 `skuId` 回查，模型不能决定交易字段。
- 加购分为“请求确认”和“确认执行”两次请求；没有确认时不会调用写接口。

## 不可信输入

模型输出只用于生成解释和选择受限场景，不能直接写入订单或购物车。工具失败会转换成固定公开消息，不向浏览器暴露上游响应、内部令牌或服务细节。

## 可观测性

`X-Request-Id` 从浏览器进入 Commerce Core，再传播到 AI Service 和 Commerce Tool。日志仅记录 request ID、conversation ID、场景、耗时和错误类型；Token 统计明确标记为 estimated。
