# 5 分钟演示脚本

## 启动

```bash
cp .env.compose.example .env
docker compose up --build
```

打开 Customer Web `http://localhost:5173`、Merchant Web `http://localhost:5174` 和 Admin Web `http://localhost:5175`。

演示账号均为 `Demo123!`：`demo_customer`、`demo_merchant`、`demo_admin`。

## 主链路

1. Customer 登录后输入“帮我找 500 到 1000 元的耳机”。
2. 展示 SSE 文本流和真实商品卡片，说明商品来自 Commerce Core 数据库。
3. 点击商品卡片的“加入购物车”，展示 `add_to_cart_confirmation`，强调此时尚未写入购物车。
4. 点击确认，展示 `add_to_cart_result`，打开购物车核对 SKU 和数量。
5. Admin 登录打开 AI 中心，展示真实 Provider、模型、TTFT、总耗时和工具成功率。

## 重置

```bash
docker compose down -v
```
