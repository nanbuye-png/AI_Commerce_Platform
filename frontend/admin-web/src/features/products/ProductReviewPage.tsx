import React, { useState, useEffect } from 'react';
import { productApi } from '../../api/products';
import type { ProductVO } from '../../api/products';

const statusLabels: Record<string, string> = {
  PENDING_REVIEW: '待审核',
  ON_SHELF: '已上架',
  OFF_SHELF: '已下架',
  REJECTED: '已驳回',
};

const statusColors: Record<string, string> = {
  PENDING_REVIEW: '#FF9F0A',
  ON_SHELF: '#34C759',
  OFF_SHELF: '#A1A1A6',
  REJECTED: '#FF3B30',
};

const ProductReviewPage: React.FC = () => {
  const [products, setProducts] = useState<ProductVO[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let cancelled = false;
    void productApi.listPending({ page: 1, size: 20 })
      .then((res) => {
        if (!cancelled) setProducts(res.data.list);
      })
      .catch((err: unknown) => {
        if (!cancelled) console.error('加载商品列表失败:', err);
      })
      .finally(() => {
        if (!cancelled) setLoading(false);
      });

    return () => {
      cancelled = true;
    };
  }, []);

  const loadProducts = async () => {
    const res = await productApi.listPending({ page: 1, size: 20 });
    setProducts(res.data.list);
  };

  const handleApprove = async (id: number) => {
    try {
      await productApi.approve(id);
      await loadProducts();
    } catch (err) {
      console.error('审核失败:', err);
      alert('操作失败');
    }
  };

  const handleReject = async (id: number) => {
    try {
      await productApi.reject(id);
      await loadProducts();
    } catch (err) {
      console.error('驳回失败:', err);
      alert('操作失败');
    }
  };

  const handleOffShelf = async (id: number) => {
    try {
      await productApi.offShelf(id);
      await loadProducts();
    } catch (err) {
      console.error('下架失败:', err);
      alert('操作失败');
    }
  };

  return (
    <div style={{ maxWidth: 1200, margin: '0 auto' }}>
      <h1 style={{ fontSize: 'var(--font-size-h1)', fontWeight: 600, marginBottom: 'var(--spacing-lg)' }}>商品管理</h1>
      <div style={{ background: 'var(--color-bg-primary)', borderRadius: 'var(--radius-md)', boxShadow: 'var(--shadow-sm)', overflow: 'hidden' }}>
        <table style={{ width: '100%', borderCollapse: 'collapse' }}>
          <thead>
            <tr style={{ borderBottom: '1px solid var(--color-border-light)', background: 'var(--color-bg-secondary)' }}>
              {['ID', '商品名称', '商家ID', '状态', '创建时间', '操作'].map((h) => (
                <th key={h} style={{ padding: '12px 16px', textAlign: 'left', fontSize: '13px', color: 'var(--color-text-secondary)', fontWeight: 500 }}>{h}</th>
              ))}
            </tr>
          </thead>
          <tbody>
            {loading ? (
              <tr><td colSpan={6} style={{ padding: '24px', textAlign: 'center', color: 'var(--color-text-tertiary)' }}>加载中...</td></tr>
            ) : products.length === 0 ? (
              <tr><td colSpan={6} style={{ padding: '24px', textAlign: 'center', color: 'var(--color-text-tertiary)' }}>暂无数据</td></tr>
            ) : (
              products.map((p) => (
                <tr key={p.id} style={{ borderBottom: '1px solid var(--color-border-light)' }}>
                  <td style={{ padding: '12px 16px', fontSize: '13px', color: 'var(--color-text-primary)' }}>#{p.id}</td>
                  <td style={{ padding: '12px 16px', fontSize: '13px', color: 'var(--color-text-primary)' }}>{p.productName}</td>
                  <td style={{ padding: '12px 16px', fontSize: '13px', color: 'var(--color-text-secondary)' }}>#{p.merchantId}</td>
                  <td style={{ padding: '12px 16px' }}>
                    <span style={{ fontSize: '12px', padding: '2px 8px', borderRadius: '4px', background: `${statusColors[p.status] || '#86868B'}18`, color: statusColors[p.status] || '#86868B' }}>
                      {statusLabels[p.status] || p.status}
                    </span>
                  </td>
                  <td style={{ padding: '12px 16px', fontSize: '12px', color: 'var(--color-text-tertiary)' }}>{p.createdTime ? new Date(p.createdTime).toLocaleDateString('zh-CN') : '-'}</td>
                  <td style={{ padding: '12px 16px', display: 'flex', gap: '6px', flexWrap: 'wrap' }}>
                    {p.status === 'PENDING_REVIEW' && (
                      <>
                        <button onClick={() => handleApprove(p.id)} style={{ padding: '4px 10px', borderRadius: 'var(--radius-sm)', border: 'none', background: '#34C759', color: '#fff', fontSize: '12px', cursor: 'pointer' }}>通过</button>
                        <button onClick={() => handleReject(p.id)} style={{ padding: '4px 10px', borderRadius: 'var(--radius-sm)', border: 'none', background: '#FF3B30', color: '#fff', fontSize: '12px', cursor: 'pointer' }}>驳回</button>
                      </>
                    )}
                    {p.status === 'ON_SHELF' && (
                      <button onClick={() => handleOffShelf(p.id)} style={{ padding: '4px 10px', borderRadius: 'var(--radius-sm)', border: '1px solid var(--color-border)', background: 'transparent', color: '#FF3B30', fontSize: '12px', cursor: 'pointer' }}>下架</button>
                    )}
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
};

export default ProductReviewPage;