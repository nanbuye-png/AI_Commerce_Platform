import React, { useCallback, useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { productApi, type ProductVO } from '../../api/product';

const statusLabels: Record<string, string> = {
  DRAFT: '草稿',
  PENDING_REVIEW: '待审核',
  REJECTED: '已驳回',
  ON_SHELF: '已上架',
  OFF_SHELF: '已下架',
  ARCHIVED: '已归档',
};

const statusColors: Record<string, string> = {
  DRAFT: '#86868B',
  PENDING_REVIEW: '#FF9F0A',
  REJECTED: '#FF3B30',
  ON_SHELF: '#34C759',
  OFF_SHELF: '#A1A1A6',
  ARCHIVED: '#A1A1A6',
};

const ProductListPage: React.FC = () => {
  const navigate = useNavigate();
  const [products, setProducts] = useState<ProductVO[]>([]);
  const [loading, setLoading] = useState(true);
  const [total, setTotal] = useState(0);
  const [page, setPage] = useState(1);
  const [pageSize] = useState(20);
  const [totalPages, setTotalPages] = useState(0);
  const [statusFilter, setStatusFilter] = useState('');
  const [keyword, setKeyword] = useState('');

  const loadProducts = useCallback(async (p = page, st = statusFilter, kw = keyword) => {
    setLoading(true);
    try {
      const params: { page: number; pageSize: number; status?: string; keyword?: string } = { page: p, pageSize };
      if (st) params.status = st;
      if (kw.trim()) params.keyword = kw.trim();
      const res = await productApi.list(params);
      setProducts(res.data.content ?? []);
      setTotal(res.data.totalElements ?? 0);
      setTotalPages(res.data.totalPages ?? 0);
    } catch (err) {
      console.error('加载商品失败:', err);
    } finally {
      setLoading(false);
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [page, statusFilter, keyword]);

  useEffect(() => {
    void loadProducts();
  }, [loadProducts]);

  const handleDelete = async (id: number) => {
    if (!window.confirm('确定删除该商品吗？删除后不可恢复。')) return;
    try {
      await productApi.delete(id);
      await loadProducts();
    } catch (err) {
      console.error('删除失败:', err);
      alert('删除失败，请稍后重试');
    }
  };

  const handleSearch = () => {
    setPage(1);
    void loadProducts(1, statusFilter, keyword);
  };

  return (
    <div style={{ maxWidth: 1200, margin: '0 auto' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 'var(--spacing-lg)' }}>
        <h1 style={{ fontSize: 'var(--font-size-h1)', fontWeight: 600 }}>
          商品管理 <span style={{ fontSize: '14px', color: 'var(--color-text-secondary)', fontWeight: 400 }}>共 {total} 件</span>
        </h1>
        <button
          onClick={() => navigate('/products/create')}
          style={{ padding: '10px 20px', borderRadius: 'var(--radius-sm)', background: 'var(--color-accent)', color: '#fff', fontSize: '14px', fontWeight: 500, border: 'none', cursor: 'pointer' }}
        >
          + 添加商品
        </button>
      </div>

      <div style={{ display: 'flex', gap: 12, marginBottom: 'var(--spacing-lg)' }}>
        <input
          value={keyword}
          onChange={(e) => setKeyword(e.target.value)}
          onKeyDown={(e) => e.key === 'Enter' && handleSearch()}
          placeholder="搜索商品名称 / 编码"
          style={{ flex: 1, maxWidth: 280, padding: '8px 12px', borderRadius: 'var(--radius-sm)', border: '1px solid var(--color-border)', fontSize: '14px' }}
        />
        <select
          value={statusFilter}
          onChange={(e) => { setStatusFilter(e.target.value); setPage(1); void loadProducts(1, e.target.value, keyword); }}
          style={{ padding: '8px 12px', borderRadius: 'var(--radius-sm)', border: '1px solid var(--color-border)', fontSize: '14px' }}
        >
          <option value="">全部状态</option>
          <option value="DRAFT">草稿</option>
          <option value="PENDING_REVIEW">待审核</option>
          <option value="REJECTED">已驳回</option>
          <option value="ON_SHELF">已上架</option>
          <option value="OFF_SHELF">已下架</option>
          <option value="ARCHIVED">已归档</option>
        </select>
        <button
          onClick={handleSearch}
          style={{ padding: '8px 20px', borderRadius: 'var(--radius-sm)', border: 'none', background: 'var(--color-accent)', color: '#fff', fontSize: '14px', cursor: 'pointer' }}
        >
          搜索
        </button>
      </div>

      <div style={{ background: 'var(--color-bg-primary)', borderRadius: 'var(--radius-md)', boxShadow: 'var(--shadow-sm)', overflow: 'hidden' }}>
        {loading ? (
          <p style={{ padding: 'var(--spacing-xl)', textAlign: 'center', color: 'var(--color-text-tertiary)' }}>加载中...</p>
        ) : products.length === 0 ? (
          <p style={{ padding: 'var(--spacing-xl)', textAlign: 'center', color: 'var(--color-text-tertiary)' }}>暂无商品，点击右上角"添加商品"创建</p>
        ) : (
          <table style={{ width: '100%', borderCollapse: 'collapse' }}>
            <thead>
              <tr style={{ borderBottom: '1px solid var(--color-border-light)', background: 'var(--color-bg-secondary)' }}>
                {['商品信息', '销量', '状态', '创建时间', '操作'].map((h) => (
                  <th key={h} style={{ padding: '12px 16px', textAlign: 'left', fontSize: '13px', color: 'var(--color-text-secondary)', fontWeight: 500 }}>{h}</th>
                ))}
              </tr>
            </thead>
            <tbody>
              {products.map((p) => (
                <tr key={p.id} style={{ borderBottom: '1px solid var(--color-border-light)' }}>
                  <td style={{ padding: '12px 16px', display: 'flex', alignItems: 'center', gap: 12 }}>
                    <div style={{ width: 48, height: 48, borderRadius: 'var(--radius-sm)', background: 'var(--color-bg-secondary)', overflow: 'hidden', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: '10px', color: 'var(--color-text-tertiary)' }}>
                      {p.coverImage ? <img src={p.coverImage} alt="" style={{ width: '100%', height: '100%', objectFit: 'cover' }} /> : '图'}
                    </div>
                    <div>
                      <p style={{ fontSize: '14px', fontWeight: 500, color: 'var(--color-text-primary)' }}>{p.productName}</p>
                      <p style={{ fontSize: '12px', color: 'var(--color-text-tertiary)' }}>{p.productCode}</p>
                    </div>
                  </td>
                  <td style={{ padding: '12px 16px', fontSize: '14px', fontWeight: 600, color: 'var(--color-text-primary)' }}>
                    {p.salesCount ?? 0}
                  </td>
                  <td style={{ padding: '12px 16px' }}>
                    <span style={{ fontSize: '12px', padding: '2px 8px', borderRadius: '4px', background: `${statusColors[p.status] || '#86868B'}18`, color: statusColors[p.status] || '#86868B' }}>
                      {statusLabels[p.status] || p.status}
                    </span>
                  </td>
                  <td style={{ padding: '12px 16px', fontSize: '12px', color: 'var(--color-text-tertiary)' }}>
                    {p.createdTime ? new Date(p.createdTime).toLocaleString('zh-CN') : '-'}
                  </td>
                  <td style={{ padding: '12px 16px', display: 'flex', gap: 6 }}>
                    <button onClick={() => navigate(`/products/${p.id}/edit`)} style={{ padding: '4px 12px', borderRadius: 'var(--radius-sm)', border: '1px solid var(--color-border)', background: 'transparent', color: 'var(--color-text-secondary)', fontSize: '12px', cursor: 'pointer' }}>编辑</button>
                    <button onClick={() => handleDelete(p.id)} style={{ padding: '4px 12px', borderRadius: 'var(--radius-sm)', border: '1px solid var(--color-border)', background: 'transparent', color: '#FF3B30', fontSize: '12px', cursor: 'pointer' }}>删除</button>
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

export default ProductListPage;