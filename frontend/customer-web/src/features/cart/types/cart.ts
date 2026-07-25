/* ============================================================
   Cart Domain Types
   ============================================================ */

/** 购物车单品 */
export interface CartItem {
  readonly productId: string;
  readonly name: string;
  readonly thumbnail: string;
  readonly price: number;
  readonly originalPrice?: number;
  readonly quantity: number;
  readonly specInfo?: string;
  readonly stock: number;
  readonly checked: boolean;
  readonly maxQuantity: number;
}

/** 购物车摘要 */
export interface CartSummary {
  readonly totalQuantity: number;
  readonly totalAmount: number;
  readonly totalOriginalAmount: number;
  readonly discount: number;
  readonly itemCount: number;
  readonly checkedCount: number;
  readonly checkedAmount: number;
}

/** 购物车操作结果 */
export interface CartOperationResult {
  success: boolean;
  message?: string;
}