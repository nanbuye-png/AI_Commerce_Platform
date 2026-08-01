import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { orderApi, type OrderVO } from '../../api/order';

const statusColors: Record<string, string> = {
  PENDING_PAYMENT: '#FF9F0A', PAID: '#0071E3', PROCESSING: '#0071E3',
  SHIPPED: '#34C759', COMPLETED: '#86868B', CANCELLED: '#A1A1A6',
  REFUNDING: '#FF3B30', REFUNDED: '#86868B', CLOSED: '#A1A1A6',
};
const statusLabels: Record<string, string> = {
  PENDING_PAYMENT: '待付款', PAID: '已付款', PROCESSING: '处理中',
  SHIPPED: '已发货', COMPLETED: '已完成', CANCELLED: '已取消',
  REFUNDING: '退款中', REFUNDED: '已退款', CLOSED: '已关闭',
};

const OrderListPage: React.FC = () => {
  const navigate = useNavigate();
  const [orders, setOrders] = useState<OrderVO[]>([]);
  const [loading, setLoading] = useState(true);
  const [total, setTotal] = useState(0);

  useEffect(() => {
    let cancelled = false;
    orderApi.list({ page: 1, pageSize: 20 }).then((res) => {
      if (cancelled) return;
      setOrders(res.data.content ?? []);
      setTotal(res.data.totalElements ?? 0);
    }).catch((err) => { if (!cancelled) console.error('加载订单失败:', err); })
      .finally(() => { if (!cancelled) setLoading(false); });
    return () => { cancelled = true; };
  }, []);

  return (
    <div style={{ maxWidth: 1200, margin: '0 auto' }}>
      <h1 style={{ fontSize: 'var(--font-size-h1)', fontWeight: 600, marginBottom: 'var(--spacing-lg)' }}>
        订单管理 <span style={{ fontSize: '14px', color: 'var(--color-text-secondary)', fontWeight: 400 }}>共 {total} 单</span>
      </h1>
      <div style={{ background: 'var(--color-bg-primary)', borderRadius: 'var(--radius-md)', boxShadow: 'var(--shadow-sm)', overflow: 'hidden' }}>
        {loading ? (
          <p style={{ padding: 'var(--spacing-xl)', textAlign: 'center', color: 'var(--color-text-tertiary)' }}>加载中...</p>
        ) : orders.length === 0 ? (
          <p style={{ padding: 'var(--spacing-xl)', textAlign: 'center', color: 'var(--color-text-tertiary)' }}>暂无订单</p>
        ) : (
          <table style={{ width: '100%', borderCollapse: 'collapse' }}>
            <thead>
              <tr style={{ borderBottom: '1px solid var(--color-border-light)', background: 'var(--color-bg-secondary)' }}>
                {['订单号', '商品', '金额', '状态', '时间', '操作'].map((h) => (
                  <th key={h} style={{ padding: '12px 16px', textAlign: 'left', fontSize: '13px', color: 'var(--color-text-secondary)', fontWeight: 500 }}>{h}</th>
                ))}
              </tr>
            </thead>
            <tbody>
              {orders.map((o) => (
                <tr key={o.id || o.orderNo} style={{ borderBottom: '1px solid var(--color-border-light)' }}>
                  <td style={{ padding: '12px 16px', fontSize: '13px' }}>{o.orderNo}</td>
                  <td style={{ padding: '12px 16px', fontSize: '13px' }}>
                    {(o.items ?? []).map((it) => `${it.productName} x${it.quantity}`).join('、') || '-'}
                  </td>
                  <td style={{ padding: '12px 16px', fontSize: '14px', fontWeight: 600, color: 'var(--color-accent)' }}>¥{Number(o.payAmount || o.totalAmount || 0).toFixed(2)}</td>
                  <td style={{ padding: '12px 16px' }}>
                    <span style={{ fontSize: '12px', padding: '2px 8px', borderRadius: '4px', background: `${statusColors[o.orderStatus] || '#86868B'}18`, color: statusColors[o.orderStatus] || '#86868B' }}>
                      {statusLabels[o.orderStatus] || o.orderStatus}
                    </span>
                  </td>
                  <td style={{ padding: '12px 16px', fontSize: '12px', color: 'var(--color-text-tertiary)' }}>
                    {o.createdTime ? new Date(o.createdTime).toLocaleString('zh-CN') : '-'}
                  </td>
                  <td style={{ padding: '12px 16px' }}>
                    <button onClick={() => navigate(`/orders/${o.orderNo}`)} style={{ padding: '4px 12px', borderRadius: 'var(--radius-sm)', border: '1px solid var(--color-border)', background: 'transparent', color: 'var(--color-text-secondary)', fontSize: '12px', cursor: 'pointer' }}>详情</button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </div>
  );
};

export default OrderListPage;