import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { inventoryApi, type InventoryVO } from '../../api/inventory';

const InventoryListPage: React.FC = () => {
  const navigate = useNavigate();
  const [items, setItems] = useState<InventoryVO[]>([]);
  const [loading, setLoading] = useState(true);
  const [total, setTotal] = useState(0);
  const [page, setPage] = useState(1);
  const [pageSize] = useState(20);
  const [keyword, setKeyword] = useState('');
  const [lowStockOnly, setLowStockOnly] = useState(false);
  const [totalPages, setTotalPages] = useState(0);

  const loadInventory = async (p = page, kw = keyword, lowOnly = lowStockOnly) => {
    setLoading(true);
    try {
      const params: { page?: number; pageSize?: number; skuCode?: string; productName?: string } = {
        page: p,
        pageSize,
      };
      if (kw.trim()) {
        params.skuCode = kw.trim();
        params.productName = kw.trim();
      }
      const res = await inventoryApi.list(params);
      let content = res.data?.content ?? [];
      if (lowOnly) {
        content = content.filter((it) => it.lowStock);
      }
      setItems(content);
      setTotal(res.data?.totalElements ?? 0);
      setTotalPages(res.data?.totalPages ?? 0);
    } catch (err) {
      console.error('加载库存失败:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    void loadInventory();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [page, lowStockOnly]);

  const handleSearch = () => {
    setPage(1);
    void loadInventory(1, keyword, lowStockOnly);
  };

  return (
    <div style={{ maxWidth: 1200, margin: '0 auto' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 'var(--spacing-lg)' }}>
        <h1 style={{ fontSize: 'var(--font-size-h1)', fontWeight: 600 }}>
          库存管理 <span style={{ fontSize: '14px', color: 'var(--color-text-secondary)', fontWeight: 400 }}>共 {total} 条</span>
        </h1>
      </div>

      <div style={{ display: 'flex', gap: 12, marginBottom: 'var(--spacing-lg)' }}>
        <input
          value={keyword}
          onChange={(e) => setKeyword(e.target.value)}
          onKeyDown={(e) => e.key === 'Enter' && handleSearch()}
          placeholder="搜索商品名称 / SKU 编码"
          style={{ flex: 1, maxWidth: 320, padding: '8px 12px', borderRadius: 'var(--radius-sm)', border: '1px solid var(--color-border)', fontSize: '14px' }}
        />
        <button
          onClick={handleSearch}
          style={{ padding: '8px 20px', borderRadius: 'var(--radius-sm)', border: 'none', background: 'var(--color-accent)', color: '#fff', fontSize: '14px', cursor: 'pointer' }}
        >
          搜索
        </button>
        <label style={{ display: 'flex', alignItems: 'center', gap: 6, fontSize: '13px', color: 'var(--color-text-secondary)', cursor: 'pointer' }}>
          <input
            type="checkbox"
            checked={lowStockOnly}
            onChange={(e) => setLowStockOnly(e.target.checked)}
            style={{ cursor: 'pointer' }}
          />
          仅看低库存
        </label>
      </div>

      <div style={{ background: 'var(--color-bg-primary)', borderRadius: 'var(--radius-md)', boxShadow: 'var(--shadow-sm)', overflow: 'hidden' }}>
        {loading ? (
          <p style={{ padding: 'var(--spacing-xl)', textAlign: 'center', color: 'var(--color-text-tertiary)' }}>加载中...</p>
        ) : items.length === 0 ? (
          <p style={{ padding: 'var(--spacing-xl)', textAlign: 'center', color: 'var(--color-text-tertiary)' }}>暂无库存记录</p>
        ) : (
          <table style={{ width: '100%', borderCollapse: 'collapse' }}>
            <thead>
              <tr style={{ borderBottom: '1px solid var(--color-border-light)', background: 'var(--color-bg-secondary)' }}>
                {['商品信息', 'SKU 编码', '可售库存', '锁定库存', '总库存', '状态', '操作'].map((h) => (
                  <th key={h} style={{ padding: '12px 16px', textAlign: 'left', fontSize: '13px', color: 'var(--color-text-secondary)', fontWeight: 500 }}>{h}</th>
                ))}
              </tr>
            </thead>
            <tbody>
              {items.map((it) => (
                <tr key={it.id} style={{ borderBottom: '1px solid var(--color-border-light)' }}>
                  <td style={{ padding: '12px 16px', fontSize: '14px', fontWeight: 500, color: 'var(--color-text-primary)' }}>{it.productName}</td>
                  <td style={{ padding: '12px 16px', fontSize: '13px', color: 'var(--color-text-secondary)' }}>{it.skuCode}</td>
                  <td style={{ padding: '12px 16px', fontSize: '14px', fontWeight: 600, color: it.availableStock > 0 ? 'var(--color-success)' : '#FF3B30' }}>{it.availableStock}</td>
                  <td style={{ padding: '12px 16px', fontSize: '13px', color: 'var(--color-text-secondary)' }}>{it.reservedStock}</td>
                  <td style={{ padding: '12px 16px', fontSize: '14px', fontWeight: 600 }}>{it.totalStock}</td>
                  <td style={{ padding: '12px 16px' }}>
                    {it.lowStock ? (
                      <span style={{ fontSize: '12px', padding: '2px 8px', borderRadius: '4px', background: '#FF3B3018', color: '#FF3B30' }}>低库存</span>
                    ) : (
                      <span style={{ fontSize: '12px', padding: '2px 8px', borderRadius: '4px', background: '#34C75918', color: '#34C759' }}>正常</span>
                    )}
                  </td>
                  <td style={{ padding: '12px 16px' }}>
                    <button
                      onClick={() => navigate(`/inventory/${it.id}`)}
                      style={{ padding: '4px 12px', borderRadius: 'var(--radius-sm)', border: '1px solid var(--color-border)', background: 'transparent', color: 'var(--color-text-secondary)', fontSize: '12px', cursor: 'pointer' }}
                    >
                      详情
                    </button>
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

export default InventoryListPage;