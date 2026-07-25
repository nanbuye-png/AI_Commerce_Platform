export type OrderStatus = 'pending_payment' | 'pending_ship' | 'shipped' | 'completed' | 'cancelled' | 'refunding';

export interface OrderItem {
  productId: string;
  name: string;
  thumbnail: string;
  price: number;
  quantity: number;
  specInfo?: string;
}

export interface Order {
  id: string;
  orderNo: string;
  status: OrderStatus;
  items: OrderItem[];
  totalAmount: number;
  discount: number;
  actualAmount: number;
  address: string;
  createdAt: string;
  paidAt?: string;
  shippedAt?: string;
  completedAt?: string;
}

export const orderStatusLabels: Record<OrderStatus, string> = {
  pending_payment: '待付款',
  pending_ship: '待发货',
  shipped: '已发货',
  completed: '已完成',
  cancelled: '已取消',
  refunding: '退款中',
};

export const orderStatusColors: Record<OrderStatus, string> = {
  pending_payment: '#FF9F0A',
  pending_ship: '#0071E3',
  shipped: '#34C759',
  completed: '#86868B',
  cancelled: '#A1A1A6',
  refunding: '#FF453A',
};