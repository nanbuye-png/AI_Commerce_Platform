import React, { useState, useEffect } from 'react';
import { orderApi } from '../../api/orders';
import type { OrderVO } from '../../api/orders';

const statusLabels: Record<string, string> = {
  PENDING_PAYMENT: '待付款',
  PAID: '已支付',
  PROCESSING: '处理中',
  SHIPPED: '已发货',
  COMPLETED: '已完成',
  CANCELLED: '已取消',
  REFUNDING: '退款中',
  REFUNDED: '已退款',
};

const statusColors: Record<string, string> = {
  PENDING_PAYMENT: '#FF9F0A',
  PAID: '#0071E3',
  PROCESSING: '#34C759',
  SHIPPED: '#5AC8FA',
  COMPLETED: '#86868B',
  CANCELLED: '#A1A1A6',
  REFUNDING: '#FF69B4',
  REFUNDED: '#FF3B30',
};

const OrderListPage: React.FC = () => {
  const [orders, setOrders] = useState<OrderVO[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let cancelled = false;
    void orderApi.list({ page: 1, pageSize: 20 })
      .then((res) => {
        if (!cancelled) setOrders(res.data.content);
      })
      .catch((err: unknown) => {
        if (!cancelled) console.error('加载订单列表失败:', err);
      })
      .finally(() => {
        if (!cancelled) setLoading(false);
      });

    return () => {
      cancelled = true;
    };
  }, []);

  return (
    <div style={{ maxWidth: 1200, margin: '0 auto' }}>
      <h1 style={{ fontSize: 'var(--font-size-h1)', fontWeight: 600, marginBottom: 'var(--spacing-lg)' }}>订单监控</h1>
      <div style={{ background: 'var(--color-bg-primary)', borderRadius: 'var(--radius-md)', boxShadow: 'var(--shadow-sm)', overflow: 'hidden' }}>
        <table style={{ width: '100%', borderCollapse: 'collapse' }}>
          <thead>
            <tr style={{ borderBottom: '1px solid var(--color-border-light)', background: 'var(--color-bg-secondary)' }}>
              {['ID', '订单号', '买家ID', '商家ID', '金额', '状态', '创建时间'].map((h) => (
                <th key={h} style={{ padding: '12px 16px', textAlign: 'left', fontSize: '13px', color: 'var(--color-text-secondary)', fontWeight: 500 }}>{h}</th>
              ))}
            </tr>
          </thead>
          <tbody>
            {loading ? (
              <tr><td colSpan={7} style={{ padding: '24px', textAlign: 'center', color: 'var(--color-text-tertiary)' }}>加载中...</td></tr>
            ) : orders.length === 0 ? (
              <tr><td colSpan={7} style={{ padding: '24px', textAlign: 'center', color: 'var(--color-text-tertiary)' }}>暂无数据</td></tr>
            ) : (
              orders.map((o) => (
                <tr key={o.id} style={{ borderBottom: '1px solid var(--color-border-light)' }}>
                  <td style={{ padding: '12px 16px', fontSize: '13px', color: 'var(--color-text-primary)' }}>#{o.id}</td>
                  <td style={{ padding: '12px 16px', fontSize: '13px', color: 'var(--color-text-primary)', fontFamily: 'monospace' }}>{o.orderNo}</td>
                  <td style={{ padding: '12px 16px', fontSize: '13px', color: 'var(--color-text-secondary)' }}>#{o.buyerId}</td>
                  <td style={{ padding: '12px 16px', fontSize: '13px', color: 'var(--color-text-secondary)' }}>#{o.merchantId}</td>
                  <td style={{ padding: '12px 16px', fontSize: '14px', fontWeight: 600, color: 'var(--color-accent)' }}>¥{o.payAmount.toFixed(2)}</td>
                  <td style={{ padding: '12px 16px' }}>
                    <span style={{ fontSize: '12px', padding: '2px 8px', borderRadius: '4px', background: `${statusColors[o.orderStatus] || '#86868B'}18`, color: statusColors[o.orderStatus] || '#86868B' }}>
                      {statusLabels[o.orderStatus] || o.orderStatus}
                    </span>
                  </td>
                  <td style={{ padding: '12px 16px', fontSize: '12px', color: 'var(--color-text-tertiary)' }}>{o.createdTime ? new Date(o.createdTime).toLocaleDateString('zh-CN') : '-'}</td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
};

export default OrderListPage;