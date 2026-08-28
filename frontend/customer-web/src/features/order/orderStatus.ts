import type { OrderStatus } from './types/order';

/** 后端订单状态 → 前端订单状态 */
export function toFrontendStatus(status: string): OrderStatus {
  const map: Record<string, OrderStatus> = {
    PENDING_PAYMENT: 'pending_payment',
    PROCESSING: 'pending_ship',
    PAID: 'pending_ship',
    SHIPPED: 'shipped',
    COMPLETED: 'completed',
    CANCELLED: 'cancelled',
    REFUNDING: 'refunding',
    REFUNDED: 'refunded',
    CLOSED: 'cancelled',
  };
  return map[status] ?? 'pending_payment';
}
