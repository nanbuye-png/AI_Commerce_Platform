# ai-commerce-ui-designer

> AI Commerce Platform — Customer Web 电商 UI 设计 Skill  
> 版本：1.0  
> 目标：建立 Apple Store 级视觉品质 + Shopify 级电商体验的 Customer Web 前端设计能力

---

## 一、Apple Store 级视觉规范

### 1.1 设计哲学

- **敬畏留白**：元素间距保持 24px / 32px / 48px / 64px/ 96px 层级，拒绝拥挤布局
- **内容优先**：商品图片应占视口 50% 以上，文字点缀而非主导
- **沉浸式全屏**：关键页面（首页、商品详情）采用 edge-to-edge 设计，导航栏使用模糊毛玻璃效果（backdrop-filter: blur(20px)）
- **单轴线引导**：页面视觉流从上至下、从左至右，避免多列混杂

### 1.2 色彩体系

| Token | 色值 | 用途 |
|-------|------|------|
| `--color-bg-primary` | `#FFFFFF` / `#000000`(dark) | 页面主背景 |
| `--color-bg-secondary` | `#F5F5F7` / `#1D1D1F`(dark) | 卡片、区段背景 |
| `--color-text-primary` | `#1D1D1F` / `#F5F5F7`(dark) | 标题、正文 |
| `--color-text-secondary` | `#86868B` / `#A1A1A6`(dark) | 辅助文字 |
| `--color-accent` | `#0071E3` / `#2997FF`(dark) | 按钮、链接、选中态 |
| `--color-border` | `#D2D2D7` / `#424245`(dark) | 分隔线、边框 |

- 按钮色彩：主按钮使用 `--color-accent` 纯色填充；次级按钮使用透明背景 + 1px 边框
- 警示/促销色：`#FF453A`（仅限促销标签、折扣标识，不可用于界面主色）

### 1.3 字体与排版

- 字族：`-apple-system, BlinkMacSystemFont, "SF Pro Display", "Helvetica Neue", sans-serif`
- 层级（移动端/桌面端）：

| 层级 | 字号 (Mobile) | 字号 (Desktop) | 字重 | 行高 |
|------|--------------|----------------|------|------|
| Hero Title | 36px | 56px | 700 | 1.1 |
| H1 | 28px | 40px | 600 | 1.2 |
| H2 | 22px | 32px | 600 | 1.25 |
| H3 | 18px | 24px | 600 | 1.3 |
| Body | 16px | 17px | 400 | 1.5 |
| Caption | 13px | 13px | 400 | 1.4 |
| Small | 11px | 12px | 400 | 1.3 |

- 行宽限制：正文每行不超过 680px（约 40-50 字符），超出应换行或截断

### 1.4 圆角与阴影

- 卡片圆角：`16px`（移动端 `12px`）
- 按钮圆角：`12px`（小按钮 `8px`）
- 图片圆角：`8px`
- Modal 圆角：`20px`
- 阴影层级：
  - 浅阴影（卡片）：`0 2px 8px rgba(0,0,0,0.04)`
  - 中阴影（下拉菜单）：`0 8px 24px rgba(0,0,0,0.08)`
  - 深阴影（Modal）：`0 24px 48px rgba(0,0,0,0.12)`

### 1.5 动效与过渡

- 默认过渡曲线：`cubic-bezier(0.25, 0.1, 0.25, 1)`
- 过渡时长：
  - 微交互（hover、tap）：150ms
  - 页面元素出现：300ms
  - 页面切换：400ms
  - 全屏 Modal / Sheet：500ms
- 滚动回弹效果：iOS-style 弹性滚动（`-webkit-overflow-scrolling: touch`）

---

## 二、Shopify 电商体验原则

### 2.1 购物漏斗优化

```
浏览 → 发现 → 查看 → 添加 → 结账
 ↑       ↓       ↑        ↓      ↑
 └── 再营销 ──┴── 推荐 ──┴── 恢复 ──┘
```

- **减少摩擦**：购买路径不超过 3 次点击
- **即时预览**：商品列表 hover／tap 后 200ms 内浮出 Quick Add 浮层
- **持久购物车**：用户退出后保留购物车内容，下次访问自动恢复
- **库存告知**：低库存（≤5 件）在商品卡片上显示「仅剩 X 件」进度条

### 2.2 移动优先 (Mobile First)

- 所有页面以 375px 宽度为基准设计，向上兼容 768px / 1024px / 1440px
- 关键交互按钮置于拇指热区（屏幕下半部分 60% 区域）
- 禁用 hover-only 交互，所有功能必须支持 touch 事件
- 底部导航栏高度 56px，包含首页、搜索、购物车、个人中心四个入口

### 2.3 信任建立

- **透明定价**：所有价格包含税费标注（`¥299.00 含税`）
- **退换保障**：商品详情页顶部展示 30 天退换保障标签
- **真实评价**：默认展示「最有帮助的评价」，支持图片/视频评价
- **安全结账**：结账页面展示 SSL 锁图标 + 支付方式 Logo 行

### 2.4 搜索与发现

- 搜索框始终可见（顶部导航栏固定）
- 输入即搜索：300ms 防抖后展示联想结果
- 搜索无结果时提供：拼写建议、热门搜索词、分类浏览入口
- 搜索结果页支持多维度筛选（价格区间、品牌、评分、颜色、尺寸）

---

## 三、商品展示设计原则

### 3.1 商品列表页

- **图文比例**：图片占卡片面积 65%-70%，文字信息集中在下部 30%-35%
- **图片规范**：
  - 主图尺寸：`1:1` 正方形，最小宽度 800px
  - 展示模式：`object-fit: cover`，不允许拉伸变形
  - 默认展示正面图，hover/swipe 切换第二张图
- **信息层级**（从上到下）：
  1. 品牌/标题（限 2 行，超出省略）
  2. 价格（主价格粗体 + 原价划掉 + 折扣标签）
  3. 评分星级 + 评价数
  4. 颜色/规格选择点（如有）
  5. Quick Add 按钮（仅移动端）
- **布局**：桌面端 4 列网格，平板 3 列，移动端 2 列，间距 16px

### 3.2 商品详情页

- **架构**：上半部分固定为图片轮播（占视口 60%），下半部分可滚动内容
- **图片轮播**：
  - 支持手势滑动切换
  - 指示器使用小圆点（当前页高亮）
  - 点击图片进入全屏画廊模式
- **信息区块**（按顺序）：
  1. 标题 + 价格（含分期付款信息）
  2. 规格选择器（尺寸/颜色/配置 → 联动价格变化）
  3. 数量选择器
  4. 加入购物车按钮（全宽，固定底部）
  5. 商品描述（可折叠 Accordion）
  6. 规格参数表格
  7. 用户评价区
  8. 推荐商品区（横向滑动）
- **粘性购买栏**：滚动时底部固定「加入购物车」按钮，显示当前价格

### 3.3 商品图片规范

- 图片格式：WebP 优先，回退 JPEG/PNG
- 图片 CDN：支持 `?w=__&q=__` 参数动态裁剪
- 懒加载：Intersection Observer 实现，`loading="lazy"`
- Blur-up 占位：加载前显示 20px 模糊缩略图（LQIP）

---

## 四、AI 功能入口设计规范

### 4.1 AI 功能入口通用原则

- **渐进式呈现**：AI 功能作为增值体验，不干扰主购物流程
- **可视化标识**：统一使用渐变圆环/星芒图标 + "AI" 微标作为视觉锚点
- **引导文案**：使用第二人称提问式文案（"为您推荐？" "帮您挑选？"），避免技术术语

### 4.2 各场景 AI 入口

| 场景 | 入口位置 | 交互形式 | 触发条件 |
|------|---------|---------|---------|
| 智能搜索 | 搜索框右侧 | AI 图标按钮 | 点击展开 AI 搜索对话框 |
| 商品推荐 | 首页推荐区顶部 | 横向滚动推荐卡片 | 自动加载 + "换一批" 按钮 |
| 搭配建议 | 商品详情页底部 | "AI 搭配" 入口卡片 | 点击展开搭配列表 |
| 智能客服 | 页面右下角 | 浮动气泡按钮 | 点击打开对话窗口 |
| 拍照搜图 | 搜索框旁 | 相机图标按钮 | 点击唤起相机/相册 |
| 个性化首页 | 首页顶部 | "为你推荐" 标签 | 登录后自动展示 |

### 4.3 AI 对话界面规范

- **气泡布局**：用户右对齐（蓝色/主色背景），AI 左对齐（灰色背景）
- **打字机效果**：AI 回复逐字显示，速度 30-50ms/字
- **快捷操作**：AI 回复下方展示 2-3 个快捷按钮（如"查看商品"、"加入购物车"）
- **输入框**：固定在底部，支持文字、语音输入
- **历史记录**：保留最近 30 天对话，在对话列表可回溯

### 4.4 AI 结果展示规范

- 推荐结果使用标准商品卡片，与普通商品展示风格一致
- AI 推荐理由以「为什么推荐这个」折叠文字展示在卡片下方
- 推荐结果数：移动端 ≤ 6 个，桌面端 ≤ 12 个
- 用户可点击「不感兴趣」反馈，影响后续推荐

---

## 五、React 组件设计规范

### 5.1 组件目录结构

```
src/
├── components/
│   ├── common/          # 通用基础组件
│   │   ├── Button/
│   │   ├── Card/
│   │   ├── Image/
│   │   ├── Badge/
│   │   └── Skeleton/
│   ├── product/         # 商品相关组件
│   │   ├── ProductCard/
│   │   ├── ProductList/
│   │   ├── ProductDetail/
│   │   ├── ImageCarousel/
│   │   ├── SizeSelector/
│   │   └── PriceDisplay/
│   ├── cart/            # 购物车
│   │   ├── CartItem/
│   │   ├── CartSummary/
│   │   └── CartIcon/
│   ├── checkout/        # 结账
│   │   ├── CheckoutForm/
│   │   ├── PaymentMethod/
│   │   └── OrderSummary/
│   ├── ai/              # AI 功能
│   │   ├── AISearchDialog/
│   │   ├── AIRecommendation/
│   │   ├── AIOutfitMatch/
│   │   ├── AIChatBot/
│   │   └── AIVoiceInput/
│   └── layout/          # 布局组件
│       ├── Header/
│       ├── Footer/
│       ├── BottomNav/
│       └── PageContainer/
├── hooks/
├── styles/
├── types/
└── utils/
```

### 5.2 组件设计原则

- **单一职责**：每个组件只做一件事，超过 200 行考虑拆分
- **Props 接口**：所有 Props 使用 TypeScript 接口定义，导出复用
- **受控组件**：表单类组件优先受控（Controlled Component），状态提升至父级
- **错误边界**：每个页面级组件包裹 ErrorBoundary
- **加载态**：每个数据组件必须覆盖 loading / empty / error / success 四种状态

### 5.3 组件 Props 命名规范

```typescript
// ✅ 正确命名
interface ProductCardProps {
  product: Product;
  variant?: 'compact' | 'full' | 'horizontal';
  onAddToCart: (productId: string, quantity: number) => void;
  onFavorite?: (productId: string) => void;
  className?: string;
  loading?: boolean;
}

// ❌ 避免：用 index 命名
// ❌ 避免：boolean 不加 is/has/should 前缀
// ❌ 避免：回调不加 on 前缀
```

### 5.4 样式方案

- 使用 CSS Modules 或 Tailwind CSS，禁止全局样式污染
- 类名命名：BEM 变体（`block__element--modifier`）
- 主题变量统一通过 CSS Custom Properties 注入
- 响应式断点：
  ```css
  --bp-mobile: 375px;
  --bp-tablet: 768px;
  --bp-desktop: 1024px;
  --bp-wide: 1440px;
  ```

### 5.5 性能优化

- React.memo 包裹纯展示组件（ProductCard、PriceDisplay 等）
- useMemo / useCallback 用于昂贵计算和回调传递
- 列表渲染使用唯一稳定的 `key`（商品 ID，禁止使用 index）
- 虚拟列表：商品列表超过 20 项使用 `react-virtualized` 或 `@tanstack/react-virtual`

---

## 六、Design System 规范

### 6.1 Design Token 体系

```typescript
// design-tokens.ts — 所有设计原子值统一管理

export const tokens = {
  spacing: {
    xs:   '4px',
    sm:   '8px',
    md:   '16px',
    lg:   '24px',
    xl:   '32px',
    '2xl':'48px',
    '3xl':'64px',
    '4xl':'96px',
  },
  borderRadius: {
    sm:    '8px',
    md:    '12px',
    lg:    '16px',
    xl:    '20px',
    full:  '9999px',
  },
  fontSize: {
    small:  '11px/12px',
    caption:'13px/14px',
    body:   '16px/17px',
    h3:     '18px/24px',
    h2:     '22px/32px',
    h1:     '28px/40px',
    hero:   '36px/56px',
  },
  fontWeight: {
    regular: 400,
    medium:  500,
    semibold:600,
    bold:    700,
  },
  shadow: {
    sm:  '0 2px 8px rgba(0,0,0,0.04)',
    md:  '0 8px 24px rgba(0,0,0,0.08)',
    lg:  '0 24px 48px rgba(0,0,0,0.12)',
  },
  transition: {
    fast:   '150ms cubic-bezier(0.25, 0.1, 0.25, 1)',
    normal: '300ms cubic-bezier(0.25, 0.1, 0.25, 1)',
    slow:   '500ms cubic-bezier(0.25, 0.1, 0.25, 1)',
  },
} as const;
```

### 6.2 组件库体系层级

```
Foundation Layer
├── Design Tokens (colors, spacing, typography, shadows)
├── Themes (light / dark mode)
└── Reset / Base Styles

Primitive Layer (src/components/common/)
├── Button, Input, Select, Checkbox, Radio
├── Card, Badge, Tag, Avatar
├── Modal, Sheet, Drawer, Toast
├── Skeleton, Spinner, EmptyState
└── Icon, Image, LazyImage

Composite Layer (src/components/product/ 等)
├── ProductCard, ProductList, ProductDetail
├── CartItem, CartSummary
├── AISearchDialog, AIRecommendation
└── Header, Footer, BottomNav

Page Layer (src/pages/)
├── HomePage, ProductListingPage, ProductDetailPage
├── CartPage, CheckoutPage, OrderPage
├── SearchPage, ProfilePage
└── AI／ChatPage
```

### 6.3 响应式设计系统

| 断点 | 设备 | 布局网格 | 列数 | 容器最大宽 |
|------|------|---------|------|-----------|
| 0-374px | 小屏手机 | 自适应 | 2 | 100% |
| 375px-767px | 手机 | 16px gutter | 2-4 | 100% |
| 768px-1023px | 平板 | 24px gutter | 4-8 | 720px |
| 1024px-1439px | 桌面 | 24px gutter | 8-12 | 960px |
| 1440px+ | 大屏 | 32px gutter | 12 | 1280px |

### 6.4 无障碍 (A11y) 规范

- 所有图片添加 `alt` 文本（商品图使用商品名称）
- 按钮和链接具备 `:focus-visible` 轮廓（2px solid `--color-accent`）
- 颜色对比度 ≥ 4.5:1（AA 标准）
- 交互元素最小触摸目标 44×44px
- 使用语义化 HTML（`<nav>`, `<main>`, `<section>`, `<article>`）
- 动态内容更新使用 `aria-live` 区域

### 6.5 暗色模式

- 基于 CSS Custom Properties + `prefers-color-scheme` 媒体查询
- 所有组件必须同时支持 light / dark 模式
- 暗色模式不做色相反转，而是重新定义明度映射
- 图片在暗色模式下不反转，保持原始色彩

---

## 七、快速参考清单

### 页面设计检查项

- [ ] 页面是否遵循移动优先设计？
- [ ] 关键操作是否在拇指热区？
- [ ] 商品图片是否占卡片 65%+？
- [ ] 购买路径 ≤ 3 次点击？
- [ ] 价格信息是否包含税费/运费说明？
- [ ] AI 入口是否以增值方式呈现，不干扰主流程？

### 组件开发检查项

- [ ] TypeScript Props 接口是否完整导出？
- [ ] 是否覆盖 loading / empty / error / success 四种状态？
- [ ] 是否有 ErrorBoundary 包裹？
- [ ] 是否支持 className 扩展？
- [ ] 是否经过 React.memo 优化（纯展示组件）？
- [ ] 是否同时支持 light / dark 主题？

---

*本 Skill 整合了 Apple 人机交互指南、Shopify 电商最佳实践及主流电商 UI 设计模式，适用于 AI Commerce Platform Customer Web 前端开发。*