import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import type { CartItem, CartSummary } from '../types/cart';

interface CartState {
  items: CartItem[];

  /** 添加商品到购物车 */
  addItem: (item: Omit<CartItem, 'checked' | 'maxQuantity'>) => void;
  /** 删除商品 */
  removeItem: (productId: string, specInfo?: string) => void;
  /** 更新数量 */
  updateQuantity: (productId: string, quantity: number, specInfo?: string) => void;
  /** 切换选中 */
  toggleCheck: (productId: string, specInfo?: string) => void;
  /** 全选/取消全选 */
  toggleCheckAll: (checked: boolean) => void;
  /** 清空购物车 */
  clearCart: () => void;
  /** 清空已选 */
  clearChecked: () => void;
  /** 获取摘要 */
  getSummary: () => CartSummary;
  /** 获取购物车数量 */
  getTotalQuantity: () => number;
}

const useCartStore = create<CartState>()(
  persist(
    (set, get) => ({
      items: [],

      addItem: (newItem) => {
        set((state) => {
          const existingIndex = state.items.findIndex(
            (item) => item.productId === newItem.productId && item.specInfo === newItem.specInfo,
          );

          if (existingIndex >= 0) {
            const updated = [...state.items];
            const existing = updated[existingIndex];
            updated[existingIndex] = {
              ...existing,
              quantity: Math.min(existing.quantity + newItem.quantity, existing.maxQuantity || 99),
            };
            return { items: updated };
          }

          return {
            items: [
              ...state.items,
              {
                ...newItem,
                checked: true,
                maxQuantity: newItem.stock || 99,
              },
            ],
          };
        });
      },

      removeItem: (productId, specInfo) => {
        set((state) => ({
          items: state.items.filter(
            (item) => !(item.productId === productId && item.specInfo === specInfo),
          ),
        }));
      },

      updateQuantity: (productId, quantity, specInfo) => {
        set((state) => ({
          items: state.items.map((item) =>
            item.productId === productId && item.specInfo === specInfo
              ? { ...item, quantity: Math.max(1, Math.min(quantity, item.maxQuantity)) }
              : item,
          ),
        }));
      },

      toggleCheck: (productId, specInfo) => {
        set((state) => ({
          items: state.items.map((item) =>
            item.productId === productId && item.specInfo === specInfo
              ? { ...item, checked: !item.checked }
              : item,
          ),
        }));
      },

      toggleCheckAll: (checked) => {
        set((state) => ({
          items: state.items.map((item) => ({ ...item, checked })),
        }));
      },

      clearCart: () => {
        set({ items: [] });
      },

      clearChecked: () => {
        set((state) => ({
          items: state.items.filter((item) => !item.checked),
        }));
      },

      getSummary: () => {
        const { items } = get();
        const checked = items.filter((item) => item.checked);
        const totalQuantity = items.reduce((sum, item) => sum + item.quantity, 0);
        const totalAmount = items.reduce((sum, item) => sum + item.price * item.quantity, 0);
        const totalOriginalAmount = items.reduce(
          (sum, item) => sum + (item.originalPrice ?? item.price) * item.quantity,
          0,
        );
        const checkedCount = checked.length;
        const checkedAmount = checked.reduce((sum, item) => sum + item.price * item.quantity, 0);

        return {
          totalQuantity,
          totalAmount,
          totalOriginalAmount,
          discount: totalOriginalAmount - totalAmount,
          itemCount: items.length,
          checkedCount,
          checkedAmount,
        };
      },

      getTotalQuantity: () => {
        return get().items.reduce((sum, item) => sum + item.quantity, 0);
      },
    }),
    {
      name: 'cart-storage',
    },
  ),
);

export default useCartStore;