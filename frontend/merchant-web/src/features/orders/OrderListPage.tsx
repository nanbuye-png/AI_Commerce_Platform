import React, { useCallback, useEffect, useState } from 'react';
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
  const [page, setPage] = useState(1);
  const [pageSize] = useState(20);
  const [totalPages, setTotalPages] = useState(0);
  const [statusFilter, setStatusFilter] = useState('');

  const loadOrders = useCallback(async (p = page, st = statusFilter) => {
    setLoading(true);
    try {
      const params: { page: number; pageSize: number; status?: string } = { page: p, pageSize };
      if (st) params.status = st;
      const res = await orderApi.list(params);
      setOrders(res.data.content ?? []);
      setTotal(res.data.totalElements ?? 0);
      setTotalPages(res.data.totalPages ?? 0);
    } catch (err) {
      console.error('加载订单失败:', err);
    } finally {
      setLoading(false);
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [page, statusFilter]);

  useEffect(() => {
    void loadOrders();
  }, [loadOrders]);

  // 自动刷新：每 10 秒拉取一次最新订单（新订单实时可见）
  useEffect(() => {
    const timer = setInterval(() => {
      void loadOrders();
    }, 10000);
    return () => clearInterval(timer);
  }, [loadOrders]);

  return (
    <div style={{ maxWidth: 1200, margin: '0 auto' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 'var(--spacing-lg)' }}>
        <h1 style={{ fontSize: 'var(--font-size-h1)', fontWeight: 600 }}>
          订单管理 <span style={{ fontSize: '14px', color: 'var(--color-text-secondary)', fontWeight: 400 }}>共 {total} 单</span>
        </h1>
        <select
          value={statusFilter}
          onChange={(e) => { setStatusFilter(e.target.value); setPage(1); void loadOrders(1, e.target.value); }}
          style={{ padding: '8px 12px', borderRadius: 'var(--radius-sm)', border: '1px solid var(--color-border)', fontSize: '14px' }}
        >
          <option value="">全部状态</option>
          <option value="PENDING_PAYMENT">待付款</option>
          <option value="PAID">已付款</option>
          <option value="PROCESSING">处理中</option>
          <option value="SHIPPED">已发货</option>
          <option value="COMPLETED">已完成</option>
          <option value="CANCELLED">已取消</option>
          <option value="REFUNDING">退款中</option>
          <option value="REFUNDED">已退款</option>
          <option value="CLOSED">已关闭</option>
        </select>
      </div>

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

      {totalPages > 1 && (
        <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', gap: 12, marginTop: 'var(--spacing-lg)' }}>
          <button
            disabled={page <= 1}
            onClick={() => setPage((p) => Math.max(1, p - 1))}
            style={{ padding: '6px 14px', borderRadius: 'var(--radius-sm)', border: '1px solid var(--color-border)', background: 'transparent', color: page <= 1 ? 'var(--color-text-tertiary)' : 'var(--color-text-secondary)', fontSize: '13px', cursor: page <= 1 ? 'not-allowed' : 'pointer' }}
          >
            上一页
          </button>
          <span style={{ fontSize: '13px', color: 'var(--color-text-secondary)' }}>{page} / {totalPages}</span>
          <button
            disabled={page >= totalPages}
            onClick={() => setPage((p) => Math.min(totalPages, p + 1))}
            style={{ padding: '6px 14px', borderRadius: 'var(--radius-sm)', border: '1px solid var(--color-border)', background: 'transparent', color: page >= totalPages ? 'var(--color-text-tertiary)' : 'var(--color-text-secondary)', fontSize: '13px', cursor: page >= totalPages ? 'not-allowed' : 'pointer' }}
          >
            下一页
          </button>
        </div>
      )}
    </div>
  );
};

export default OrderListPage;