# react-enterprise-frontend-engineer

> AI Commerce Platform — 前端工程开发规范 Skill  
> 版本：1.0  
> 目标：建立 Customer Web / Merchant Web / Admin Web 三端统一的 React 工程规范体系

---

## 一、React 工程架构

### 1.1 React + TypeScript 最佳实践

- **函数组件优先**：全部使用 Function Component + Hooks，禁止 Class Component
- **严格模式**：启用 `React.StrictMode`，开发阶段捕获渲染副作用
- **类型优先**：每个组件、Hook、工具函数必须具有完整 TypeScript 类型签名
- **单向数据流**：Props 向下传递，Events 向上冒泡，禁止子组件直接修改父组件状态
- **Hooks 规则**：严格遵循 [Rules of Hooks](https://reactjs.org/docs/hooks-rules.html)，不使用条件/循环包裹 Hooks

### 1.2 Vite 项目结构规范

```
<project>/
├── public/                  # 静态资源，直接映射到根路径
│   └── favicon.ico
├── src/
│   ├── api/                 # API 请求层
│   │   ├── request.ts       # 封装 axios/fetch 实例
│   │   └── modules/         # 按业务领域分模块
│   ├── assets/              # 图片、字体等静态资源
│   ├── components/          # 共享组件
│   │   ├── common/          # 通用基础组件
│   │   ├── business/        # 业务组件
│   │   └── layout/          # 布局组件
│   ├── features/            # Feature-based 业务模块
│   ├── hooks/               # 自定义 Hooks
│   ├── pages/               # 页面组件（路由级）
│   ├── router/              # 路由配置
│   ├── stores/              # Zustand Store 定义
│   ├── styles/              # 全局样式、主题变量
│   ├── types/               # 共享类型定义
│   └── utils/               # 工具函数
├── .env                     # 环境变量
├── .eslintrc.cjs
├── .prettierrc
├── tsconfig.json
├── tsconfig.app.json
├── tsconfig.node.json
└── vite.config.ts
```

### 1.3 Feature-based 目录设计

每个业务模块（feature）自包含，内部结构统一：

```
src/features/<feature-name>/
├── components/       # 该 feature 私有的组件
├── hooks/            # 该 feature 私有的 Hooks
├── stores/           # 该 feature 私有的 Store 片段
├── types/            # 该 feature 私有类型
├── utils/            # 该 feature 私有的工具函数
├── index.ts          # 统一导出入口
└── README.md         # 可选：模块说明
```

原则：
- 跨 feature 共享的代码提升至 `src/components/`、`src/hooks/`、`src/utils/`
- 禁止 feature 之间直接引用彼此的私有模块
- Feature 入口通过 `index.ts` 导出公开接口

### 1.4 页面与组件边界

| 层级 | 职责 | 路由关联 | 能否包含业务逻辑 |
|------|------|---------|----------------|
| `pages/` | 组装页面骨架，调用 feature，传递 Props | 直接对应路由 | 仅编排，无复杂逻辑 |
| `features/<name>/` | 完整业务模块，含组件/Hooks/Store | 不感知路由 | 包含业务逻辑 |
| `components/common/` | 纯展示组件，无业务依赖 | 无 | 无 |
| `components/business/` | 带业务语义的可复用组件 | 无 | 有限（通过 Props 注入） |

### 1.5 模块化设计

- **按领域拆分**：每个 feature 对应一个业务领域（如 `product`、`cart`、`order`、`payment`）
- **最小引用原则**：feature 依赖的第三方库应保持最少
- **导出清晰**：每个模块的 `index.ts` 明确定义公开 API
- **循环依赖禁止**：使用 `madge` 工具检测，构建阶段 CI 阻断

---

## 二、TypeScript 规范

### 2.1 类型定义规范

```typescript
// 文件位置约定
// src/types/          → 全局共享类型
// src/features/*/types/ → feature 私有类型
// src/components/*/types.ts → 组件 Props 类型

// ✅ 正确：接口 (interface) 用于对象、Props、API 响应
// ✅ 正确：类型别名 (type) 用于联合、工具类型
// ❌ 避免：export default 导出类型
// ❌ 避免：在类型文件中写实现代码
```

### 2.2 Interface 设计规范

```typescript
// ✅ 正确：使用 Interface 定义数据结构
interface User {
  readonly id: string;   // 不可变标识
  email: string;
  name: string;
  avatar?: string;       // 可选属性显式标注
  readonly createdAt: string;
}

// ✅ 正确：Interface 支持 extends 扩展
interface AdminUser extends User {
  role: 'admin' | 'super_admin';
  permissions: string[];
}

// ❌ 避免：I 前缀命名（IUser → User）
// ❌ 避免：使用 Interface 定义联合类型
// ❌ 避免：过多的可选属性（考虑拆分）
```

### 2.3 API Response 类型规范

```typescript
// 统一 API 响应结构
interface ApiResponse<T> {
  code: number;           // 业务状态码
  message: string;        // 提示信息
  data: T;               // 泛型数据
  timestamp: number;      // 时间戳
}

// 分页响应
interface PaginatedResponse<T> {
  code: number;
  message: string;
  data: {
    items: T[];
    total: number;
    page: number;
    pageSize: number;
    totalPages: number;
  };
  timestamp: number;
}

// 调用处使用泛型推导，无需手动标注泛型参数
// const response = await api.get<PaginatedResponse<Product>>('/products');
```

### 2.4 Props 类型规范

```typescript
// ✅ 正确：Props 类型与组件同文件
interface ProductCardProps {
  product: Product;
  variant?: 'compact' | 'full' | 'horizontal';
  onAddToCart: (productId: string, quantity: number) => void;
  onFavorite?: (productId: string) => void;
  className?: string;
  loading?: boolean;
}

// ✅ 正确：Callbacks 使用 on 前缀
// ✅ 正确：状态枚举使用 union string 而非 boolean
// ✅ 正确：所有 callback 参数显式类型标注
// ❌ 避免：Props 使用 Partial<T> 包裹整个对象
```

### 2.5 避免 any

```typescript
// ❌ 严格禁止：使用 any
const data: any = fetchData();

// ✅ 正确：优先使用 unknown + 类型守卫
const data: unknown = fetchData();
if (isProduct(data)) {
  console.log(data.name);
}

// ✅ 正确：可以使用 Record<string, unknown> 兜底
// ✅ 正确：可以用泛型参数替代 any
function get<T>(key: string): T | undefined { ... }

// ESLint 规则：@typescript-eslint/no-explicit-any 设置为 error
```

---

## 三、状态管理规范

### 3.1 Zustand 使用规范

```typescript
// ✅ 正确：单一 Store 示例
interface CartStore {
  items: CartItem[];
  totalAmount: number;
  loading: boolean;

  // Actions
  addItem: (item: CartItem) => void;
  removeItem: (productId: string) => void;
  clearCart: () => void;
  fetchCart: () => Promise<void>;
}

const useCartStore = create<CartStore>((set, get) => ({
  items: [],
  totalAmount: 0,
  loading: false,

  addItem: (item) => set((state) => ({
    items: [...state.items, item],
    totalAmount: state.totalAmount + item.price * item.quantity,
  })),

  removeItem: (productId) => set((state) => ({
    items: state.items.filter((i) => i.productId !== productId),
  })),

  clearCart: () => set({ items: [], totalAmount: 0 }),

  fetchCart: async () => {
    set({ loading: true });
    try {
      const cart = await api.getCart();
      set({ items: cart.items, totalAmount: cart.totalAmount });
    } finally {
      set({ loading: false });
    }
  },
}));
```

### 3.2 Store 拆分原则

- **按领域拆分**：每个业务领域一个独立 Store（auth / cart / product / order）
- **跨 Store 引用**：通过 `get()` 读取其他 Store，禁止直接 import 另一个 Store 文件
- **Store 大小控制**：单个 Store 不超过 200 行，超过则拆分为 slices
- **计算属性**：派生状态使用 Selector 而非存储在 Store 中

```typescript
// ✅ 正确：使用 Selector 派生
const cartItemCount = useCartStore((state) => state.items.length);

// ✅ 正确：Selector 自动跳过不相关的渲染
const isAuthenticated = useAuthStore((state) => !!state.token);
```

### 3.3 全局状态与局部状态边界

| 存储位置 | 适用场景 | 示例 |
|---------|---------|------|
| Zustand (全局) | 跨页面/组件共享的状态 | 认证信息、购物车、通知 |
| useState (局部) | 仅当前组件或子树需要 | 表单输入、折叠面板、弹窗 |
| useRef | 不需要触发渲染的数据 | 定时器 ID、DOM 引用 |
| URL / Search Params | 可分享/可书签的状态 | 搜索关键词、筛选条件、页码 |
| React Context | 组件树局部共享（非全局） | Theme、Locale |

### 3.4 异步状态管理

```typescript
interface AsyncState<T> {
  data: T | null;
  loading: boolean;
  error: string | null;
}

// ✅ 正确：使用统一的异步状态模式
// ✅ 正确：所有异步操作必须处理 loading / error 状态
// ✅ 正确：异步操作在组件卸载时取消（AbortController）

// 使用示例
const products = useProductStore((state) => state.products);
const loading = useProductStore((state) => state.loading);
const error = useProductStore((state) => state.error);

useEffect(() => {
  useProductStore.getState().fetchProducts();
}, []);
```

---

## 四、组件设计规范

### 4.1 可复用组件设计

```typescript
// ✅ 正确：组件 Props 支持 className 扩展（方便调用方定制样式）
interface ButtonProps extends React.ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: 'primary' | 'secondary' | 'ghost';
  size?: 'sm' | 'md' | 'lg';
  loading?: boolean;
  icon?: React.ReactNode;
}

// ✅ 正确：组件使用 forwardRef 支持 ref 透传
// ✅ 正确：可复用组件不依赖业务 Store，通过 Props 注入数据和回调
```

### 4.2 单一职责原则

- **一个组件一个职责**：如果一个组件需要做多件事，拆分为多个子组件
- **组件行数上限**：不超过 200 行（含 JSX），超过则拆分
- **重复代码**：同一段 JSX 出现 2 次以上，提取为独立组件
- **渲染逻辑与业务逻辑分离**：组件内只保留渲染逻辑和事件绑定，业务逻辑提取至 Hooks

### 4.3 Controlled Component

```typescript
// ✅ 正确：受控组件模式
interface SearchInputProps {
  value: string;
  onChange: (value: string) => void;
  placeholder?: string;
}

// ✅ 正确：非受控组件仅用于表单内部或无需外部控制的场景
// ❌ 避免：同一组件同时支持受控和非受控（增加复杂度）
```

### 4.4 Component Composition

```typescript
// ✅ 正确：使用组合而非继承
// ✅ 正确：利用 children / render props 提供扩展点
// ✅ 正确：使用 slots 模式（如 header, footer, sidebar）

interface CardCompositionProps {
  children: React.ReactNode;
  header?: React.ReactNode;
  footer?: React.ReactNode;
}

// ✅ 正确：利用 Compound Component 模式
<Select>
  <Select.Option value="1">选项一</Select.Option>
  <Select.Option value="2">选项二</Select.Option>
</Select>
```

### 4.5 Props 设计原则

```
Props 设计检查清单：
□ 是否每个 prop 都有明确的用途？
□ 是否用 union string 替代 boolean flag？
  ✅ variant: 'primary' | 'secondary'
  ❌ primary: boolean; secondary: boolean
□ 是否用 children 替代过多的 content props？
□ 回调是否以 on 前缀 + 过去式命名？
  ✅ onClose, onSubmit, onChange
  ❌ close, submit, change
□ 是否提供合适的默认值？
□ 是否避免 Props 泄露（{...rest} 谨慎使用）？
```

---

## 五、性能优化规范

### 5.1 React.memo

```typescript
// ✅ 正确：纯展示组件使用 React.memo
const ProductCard = React.memo(({ product, onAddToCart }: ProductCardProps) => {
  return (
    <div className="product-card">
      <img src={product.image} alt={product.name} />
      <h3>{product.name}</h3>
      <button onClick={() => onAddToCart(product.id, 1)}>加入购物车</button>
    </div>
  );
});

// ❌ 避免：Props 包含匿名函数/对象时使用 React.memo（破坏引用相等）
// 应在父组件使用 useCallback/useMemo 稳定引用
```

### 5.2 useMemo

```typescript
// ✅ 正确：昂贵计算使用 useMemo
const sortedProducts = useMemo(
  () => [...products].sort((a, b) => a.price - b.price),
  [products]
);

// ✅ 正确：派生状态使用 useMemo
const totalAmount = useMemo(
  () => items.reduce((sum, item) => sum + item.price * item.quantity, 0),
  [items]
);

// ❌ 避免：简单计算使用 useMemo（开销 > 收益）
// ❌ 避免：不必要的 useMemo（React 18 已优化简单计算）
```

### 5.3 useCallback

```typescript
// ✅ 正确：传递给子组件的回调使用 useCallback
const handleAddToCart = useCallback((productId: string) => {
  addToCart(productId);
}, [addToCart]);

// ✅ 正确：作为 useEffect 依赖的回调使用 useCallback
// ❌ 避免：不需要稳定引用的回调使用 useCallback
// ❌ 避免：在组件内部多次嵌套 useCallback
```

### 5.4 React.lazy + Suspense

```typescript
// ✅ 正确：按路由拆分代码
const ProductDetailPage = React.lazy(() => import('@/pages/ProductDetailPage'));
const CartPage = React.lazy(() => import('@/pages/CartPage'));

// ✅ 正确：全局 Suspense fallback
<React.Suspense fallback={<PageSkeleton />}>
  <Routes>
    <Route path="/product/:id" element={<ProductDetailPage />} />
    <Route path="/cart" element={<CartPage />} />
  </Routes>
</React.Suspense>

// 拆分粒度：一个路由一个 lazy chunk
// 不要 lazy 加载低于 50KB 的组件（收益不明显）
```

### 5.5 Virtual List

```typescript
// ✅ 正确：长列表（≥ 20 项）使用虚拟列表
// 推荐库：@tanstack/react-virtual / react-window

// 使用条件：
// - 列表项高度固定或可预估
// - 列表总项数 > 50
// - 可见区域外的项不需要完整渲染

// ❌ 避免：短列表使用虚拟列表（增加无谓复杂度）
```

### 5.6 图片懒加载

```typescript
// ✅ 正确：使用 Intersection Observer
// ✅ 正确：设置 loading="lazy" 属性
// ✅ 正确：提供低质量图片占位符（LQIP）

<img
  src={product.image}
  alt={product.name}
  loading="lazy"
  width={800}
  height={800}
  // 占位：背景色或低质量缩略图
  style={{ background: '#f0f0f0' }}
/>
```

### 5.7 Bundle 优化

- **Tree Shaking**：只导入需要的模块（`import { debounce } from 'lodash-es'`）
- **动态导入**：大体积库（如 `moment`、`echarts`）使用动态 import
- **分析工具**：定期使用 `vite-bundle-analyzer` 分析包体积
- **CDN 分离**：React、ReactDOM 等基础库通过 CDN 加载（使用 external）
- **构建配置**：
  ```typescript
  // vite.config.ts
  build: {
    rollupOptions: {
      output: {
        manualChunks: {
          vendor: ['react', 'react-dom'],
          antd: ['antd'],
          echarts: ['echarts'],
        },
      },
    },
  }
  ```

---

## 六、工程质量规范

### 6.1 ESLint 规范

```javascript
// .eslintrc.cjs — 核心规则
module.exports = {
  root: true,
  extends: [
    'eslint:recommended',
    'plugin:@typescript-eslint/recommended',
    'plugin:react/recommended',
    'plugin:react-hooks/recommended',
    'plugin:import/recommended',
    'plugin:import/typescript',
  ],
  rules: {
    // TypeScript
    '@typescript-eslint/no-explicit-any': 'error',
    '@typescript-eslint/no-unused-vars': ['error', { argsIgnorePattern: '^_' }],
    '@typescript-eslint/explicit-function-return-type': 'warn',
    '@typescript-eslint/consistent-type-imports': 'error',

    // React
    'react/react-in-jsx-scope': 'off',       // React 18+
    'react/prop-types': 'off',                // 使用 TypeScript
    'react/jsx-no-target-blank': 'error',
    'react-hooks/exhaustive-deps': 'warn',

    // Import
    'import/order': [
      'error',
      {
        groups: ['builtin', 'external', 'internal', 'parent', 'sibling'],
        'newlines-between': 'always',
        alphabetize: { order: 'asc' },
      },
    ],
    'import/no-duplicates': 'error',
  },
};
```

### 6.2 Prettier 规范

```json
// .prettierrc
{
  "semi": true,
  "singleQuote": true,
  "trailingComma": "all",
  "printWidth": 100,
  "tabWidth": 2,
  "endOfLine": "lf",
  "arrowParens": "always",
  "bracketSpacing": true,
  "jsxSingleQuote": false
}
```

### 6.3 命名规范

| 类型 | 风格 | 示例 |
|------|------|------|
| 组件名 | PascalCase | `ProductCard`、`SearchInput` |
| 文件/目录名 | kebab-case | `product-card.tsx`、`search-input.tsx` |
| 普通变量 | camelCase | `userName`、`productList` |
| 函数 | camelCase | `fetchProducts`、`handleSubmit` |
| 常量 | UPPER_SNAKE | `API_BASE_URL`、`MAX_RETRY_COUNT` |
| 类型 | PascalCase | `UserProfile`、`ApiResponse<T>` |
| 枚举 | PascalCase + 成员 UPPER | `enum SortOrder { ASC, DESC }` |
| CSS 类名 | kebab-case | `.product-card`、`.search-input` |
| CSS 变量 | kebab-case + `--` 前缀 | `--color-primary` |

### 6.4 文件组织规范

```typescript
// 文件结构约定
//
// Button/index.tsx      ← 主组件
// Button/types.ts       ← Props & 相关类型
// Button/styles.ts      ← 样式（CSS-in-JS）或 .module.css
// Button/__tests__/     ← 测试目录
// Button/index.ts       ← 统一导出

// ✅ 正确：index.ts 统一导出
export { Button } from './Button';
export type { ButtonProps } from './types';
```

### 6.5 Code Review 标准

```
Code Review 检查清单：

□ 类型安全
   □ 没有使用 any
   □ API 响应有完整类型定义
   □ Props 类型显式定义
   □ 事件处理函数有正确类型签名

□ 组件设计
   □ 组件职责单一
   □ Props 命名符合规范
   □ 覆盖 loading / empty / error / success 状态
   □ 没有不必要的 re-render

□ 状态管理
   □ 状态位置合理（全局 vs 局部）
   □ 异步操作正确处理 loading/error
   □ 没有直接修改 Store 外的状态

□ 性能
   □ 列表有合适的 key
   □ 大列表使用虚拟滚动
   □ 图片懒加载
   □ 路由懒加载

□ 代码质量
   □ 没有 TODO / FIXME 遗留
   □ 测试覆盖关键业务逻辑
   □ 没有死代码（未使用的变量/import）
   □ 遵循项目命名规范

□ 安全性
   □ 用户输入经过 XSS 防护
   □ API 请求使用统一拦截器
   □ 敏感信息不硬编码
```

---

## 附录：常见模式速查

### 自定义 Hook 模板

```typescript
interface UseAsyncResult<T> {
  data: T | null;
  loading: boolean;
  error: Error | null;
  execute: (...args: unknown[]) => Promise<void>;
  reset: () => void;
}

function useAsync<T>(
  asyncFn: (...args: unknown[]) => Promise<T>,
  immediate = false,
): UseAsyncResult<T> {
  // 实现...
}
```

### API 请求封装模板

```typescript
// src/api/request.ts
const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  timeout: 10000,
  headers: { 'Content-Type': 'application/json' },
});

// 请求拦截器：注入 Token
apiClient.interceptors.request.use((config) => {
  const token = useAuthStore.getState().token;
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// 响应拦截器：统一错误处理
apiClient.interceptors.response.use(
  (response) => response.data,
  (error) => {
    if (error.response?.status === 401) {
      useAuthStore.getState().logout();
    }
    return Promise.reject(error);
  },
);
```

---

*本 Skill 覆盖 React + TypeScript 企业级前端工程的全链路规范，适用于 AI Commerce Platform 的 customer-web、merchant-web、admin-web 三个前端项目。*