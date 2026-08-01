import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { orderApi, type OrderVO } from '../../api/order';
import { productApi } from '../../api/product';
import { inventoryApi } from '../../api/inventory';

const StatCard: React.FC<{ title: string; value: string; change: string; color: string }> = ({ title, value, change, color }) => (
  <div style={{ background: 'var(--color-bg-primary)', borderRadius: 'var(--radius-md)', padding: 'var(--spacing-lg)', boxShadow: 'var(--shadow-sm)' }}>
    <p style={{ fontSize: '13px', color: 'var(--color-text-secondary)', marginBottom: 8 }}>{title}</p>
    <p style={{ fontSize: '28px', fontWeight: 700, color: 'var(--color-text-primary)', marginBottom: 4 }}>{value}</p>
    <p style={{ fontSize: '12px', color }}>{change}</p>
  </div>
);

const statusLabels: Record<string, string> = {
  PENDING_PAYMENT: '待付款', PAID: '已付款', PROCESSING: '处理中',
  SHIPPED: '已发货', COMPLETED: '已完成', CANCELLED: '已取消',
  REFUNDING: '退款中', REFUNDED: '已退款', CLOSED: '已关闭',
};

const DashboardPage: React.FC = () => {
  const navigate = useNavigate();
  const [orderTotal, setOrderTotal] = useState(0);
  const [productTotal, setProductTotal] = useState(0);
  const [inventoryTotal, setInventoryTotal] = useState(0);
  const [recentOrders, setRecentOrders] = useState<OrderVO[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let cancelled = false;
    const loadData = async () => {
      try {
        const [orderRes, productRes, inventoryRes] = await Promise.allSettled([
          orderApi.list({ page: 1, pageSize: 5 }),
          productApi.list({ page: 1, pageSize: 1 }),
          inventoryApi.list({ page: 1, pageSize: 1 }),
        ]);

        if (cancelled) return;

        if (orderRes.status === 'fulfilled') {
          setOrderTotal(orderRes.value.data?.totalElements ?? 0);
          setRecentOrders(orderRes.value.data?.content ?? []);
        }
        if (productRes.status === 'fulfilled') {
          setProductTotal(productRes.value.data?.totalElements ?? 0);
        }
        // 库存接口可能因分页参数差异失败，静默处理
        if (inventoryRes.status === 'fulfilled') {
          setInventoryTotal(inventoryRes.value.data?.totalElements ?? 0);
        }
      } catch (err) {
        console.error('加载仪表盘数据失败:', err);
      } finally {
        if (!cancelled) setLoading(false);
      }
    };
    void loadData();
    return () => { cancelled = true; };
  }, []);

  if (loading) {
    return <div style={{ maxWidth: 1200, margin: '0 auto' }}><p style={{ textAlign: 'center', color: 'var(--color-text-tertiary)', padding: 'var(--spacing-xl)' }}>加载中...</p></div>;
  }

  return (
    <div style={{ maxWidth: 1200, margin: '0 auto' }}>
      <h1 style={{ fontSize: 'var(--font-size-h1)', fontWeight: 600, marginBottom: 'var(--spacing-xl)' }}>仪表盘</h1>
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(240px, 1fr))', gap: 'var(--spacing-md)', marginBottom: 'var(--spacing-xl)' }}>
        <StatCard title="订单总数" value={String(orderTotal)} change="全部状态订单" color="var(--color-info)" />
        <StatCard title="商品总数" value={String(productTotal)} change="在架商品" color="var(--color-accent)" />
        <StatCard title="库存记录" value={String(inventoryTotal)} change="SKU 库存" color="var(--color-success)" />
        <StatCard title="待处理退款" value="—" change="前往退款管理查看" color="var(--color-warning)" />
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: '1fr', gap: 'var(--spacing-lg)' }}>
        <div style={{ background: 'var(--color-bg-primary)', borderRadius: 'var(--radius-md)', padding: 'var(--spacing-lg)', boxShadow: 'var(--shadow-sm)' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 'var(--spacing-md)' }}>
            <h2 style={{ fontSize: '16px', fontWeight: 600 }}>最近订单</h2>
            <button
              onClick={() => navigate('/orders')}
              style={{ padding: '4px 12px', borderRadius: 'var(--radius-sm)', border: '1px solid var(--color-border)', background: 'transparent', color: 'var(--color-text-secondary)', fontSize: '12px', cursor: 'pointer' }}
            >
              查看全部
            </button>
          </div>
          {recentOrders.length === 0 ? (
            <p style={{ color: 'var(--color-text-tertiary)', fontSize: '13px' }}>暂无订单</p>
          ) : (
            recentOrders.map((o) => (
              <div key={o.id || o.orderNo} style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '10px 0', borderBottom: '1px solid var(--color-border-light)', cursor: 'pointer' }}
                onClick={() => navigate(`/orders/${o.orderNo}`)}
              >
                <div>
                  <div style={{ fontSize: '13px', color: 'var(--color-text-primary)', fontWeight: 500 }}>{o.orderNo}</div>
                  <div style={{ fontSize: '12px', color: 'var(--color-text-tertiary)' }}>
                    {o.createdTime ? new Date(o.createdTime).toLocaleString('zh-CN') : '-'}
                  </div>
                </div>
                <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
                  <span style={{ fontSize: '12px', padding: '2px 8px', borderRadius: '4px', background: '#0071E318', color: '#0071E3' }}>
                    {statusLabels[o.orderStatus] || o.orderStatus}
                  </span>
                  <span style={{ fontSize: '14px', fontWeight: 600, color: 'var(--color-accent)' }}>¥{Number(o.payAmount || o.totalAmount || 0).toFixed(2)}</span>
                </div>
              </div>
            ))
          )}
        </div>
      </div>
    </div>
  );
};

export default DashboardPage;