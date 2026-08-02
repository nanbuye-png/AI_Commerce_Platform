/**
 * 订单 API 服务 - customer-web
 * 封装 C 端订单接口：创建订单、订单列表、订单详情
 */
import request from '../api/request';

/** 后端统一响应 */
export interface ApiResult<T = unknown> {
  code: number;
  message: string;
  data: T;
}

/** 后端分页结构 */
export interface PageResult<T> {
  list: T[];
  page: number;
  size: number;
  total: number;
  pages: number;
}

/** 创建订单请求 */
export interface CreateOrderItem {
  skuId: number;
  quantity: number;
}

export interface CreateOrderParams {
  skuItems: CreateOrderItem[];
  addressId: number;
  remark?: string;
}

/** 创建订单响应 */
export interface CreateOrderResult {
  orderNo: string;
  payAmount: number | string;
  orderStatus: string;
  createdTime?: string;
}

/** 订单商品条目 */
export interface OrderItemVO {
  productId?: number;
  productName?: string;
  productImage?: string;
  price?: number | string;
  quantity?: number;
}

/** 订单 VO */
export interface OrderVO {
  id?: number;
  orderNo: string;
  orderStatus: string;
  paymentStatus?: string;
  shippingStatus?: string;
  totalAmount?: number | string;
  productAmount?: number | string;
  freightAmount?: number | string;
  discountAmount?: number | string;
  payAmount?: number | string;
  buyerRemark?: string;
  items?: OrderItemVO[];
  createdTime?: string;
  canPay?: boolean;
  canCancel?: boolean;
  canConfirm?: boolean;
  displayStatus?: string;
}

/** 支付详情响应（后端 PaymentDetailResponse） */
export interface PaymentDetail {
  paymentNo: string;
  orderNo: string;
  amount: number | string;
  expireTime: string;
  status: string;
  qrToken: string;
}

export const orderService = {
  /**
   * 创建订单
   * POST /api/orders
   */
  async createOrder(params: CreateOrderParams): Promise<CreateOrderResult> {
    const res = await request.post<ApiResult<CreateOrderResult>, ApiResult<CreateOrderResult>>('/api/orders', {
      items: params.skuItems,
      addressId: params.addressId,
      ...(params.remark ? { remark: params.remark } : {}),
    });
    return res.data;
  },

  /**
   * 查询我的订单列表
   * GET /api/orders?page=1&pageSize=20
   */
  async myOrders(params: { page?: number; pageSize?: number; status?: string } = {}): Promise<PageResult<OrderVO>> {
    const res = await request.get<ApiResult<PageResult<OrderVO>>, ApiResult<PageResult<OrderVO>>>('/api/orders', {
      params: {
        page: params.page ?? 1,
        pageSize: params.pageSize ?? 20,
        ...(params.status ? { status: params.status } : {}),
      },
    });
    return res.data;
  },

  /**
   * 查询订单详情
   * GET /api/orders/{orderNo}
   */
  async orderDetail(orderNo: string): Promise<OrderVO> {
    const res = await request.get<ApiResult<OrderVO>, ApiResult<OrderVO>>(`/api/orders/${orderNo}`);
    return res.data;
  },

  /**
   * 按订单号查询支付详情（商家已发起收款时返回）
   * GET /api/payments/orders/{orderNo}
   */
  async paymentByOrder(orderNo: string): Promise<PaymentDetail> {
    const res = await request.get<ApiResult<PaymentDetail>, ApiResult<PaymentDetail>>(`/api/payments/orders/${orderNo}`);
    return res.data;
  },

  /**
   * 按二维码 Token 查询支付详情
   * GET /api/payments/qr/{qrToken}
   */
  async paymentDetail(qrToken: string): Promise<PaymentDetail> {
    const res = await request.get<ApiResult<PaymentDetail>, ApiResult<PaymentDetail>>(`/api/payments/qr/${qrToken}`);
    return res.data;
  },

  /**
   * 确认支付（模拟扫码支付）
   * POST /api/payments/qr/{qrToken}/pay
   */
  async payByToken(qrToken: string): Promise<void> {
    await request.post<ApiResult<void>, ApiResult<void>>(`/api/payments/qr/${qrToken}/pay`);
  },

  /**
   * 取消支付
   * POST /api/payments/qr/{qrToken}/cancel
   */
  async cancelPayment(qrToken: string): Promise<void> {
    await request.post<ApiResult<void>, ApiResult<void>>(`/api/payments/qr/${qrToken}/cancel`);
  },
};
