import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { profileService, type Favorite } from '../../../services/profile';

const FavoritePage: React.FC = () => {
  const navigate = useNavigate();
  const [favorites, setFavorites] = useState<Favorite[]>([]);
  const [loading, setLoading] = useState(true);

  const load = () => {
    profileService.listFavorites(1, 50)
      .then((res) => setFavorites(res.list ?? []))
      .catch(() => {})
      .finally(() => setLoading(false));
  };

  useEffect(() => { load(); }, []);

  const remove = async (productId: number) => {
    try {
      await profileService.removeFavorite(productId);
      load();
    } catch { alert('取消收藏失败'); }
  };

  return (
    <div style={{ padding: 'var(--spacing-xl) var(--spacing-lg)', maxWidth: 700, margin: '0 auto' }}>
      <h1 style={{ fontSize: 'var(--font-size-h1)', fontWeight: 600, marginBottom: 'var(--spacing-lg)' }}>
        收藏夹 <span style={{ fontSize: '14px', color: 'var(--color-text-secondary)', fontWeight: 400 }}>({favorites.length})</span>
      </h1>

      {loading && <p style={{ color: 'var(--color-text-tertiary)' }}>加载中...</p>}

      {!loading && favorites.length === 0 && (
        <div style={{ textAlign: 'center', padding: 'var(--spacing-2xl)', color: 'var(--color-text-secondary)' }}>
          <p style={{ fontSize: '40px', marginBottom: 8 }}>❤️</p>
          <p>还没有收藏的商品</p>
        </div>
      )}

      <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--spacing-sm)' }}>
        {favorites.map((f) => (
          <div
            key={f.id}
            style={{
              display: 'flex', alignItems: 'center', gap: 'var(--spacing-md)',
              padding: 'var(--spacing-sm)', background: 'var(--color-bg-primary)',
              borderRadius: 'var(--radius-md)', boxShadow: 'var(--shadow-sm)', cursor: 'pointer',
            }}
            onClick={() => navigate(`/products/${f.productId}`)}
          >
            <div style={{
              width: 80, height: 80, borderRadius: 'var(--radius-sm)', flexShrink: 0,
              background: 'var(--color-bg-secondary)', display: 'flex', alignItems: 'center', justifyContent: 'center',
              fontSize: '11px', color: 'var(--color-text-tertiary)', overflow: 'hidden',
            }}>
              {f.productImage ? <img src={f.productImage} alt="" style={{ width: '100%', height: '100%', objectFit: 'cover' }} /> : '图'}
            </div>
            <div style={{ flex: 1, minWidth: 0 }}>
              <p style={{ fontSize: '14px', fontWeight: 500, color: 'var(--color-text-primary)', marginBottom: 4, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                {f.productName}
              </p>
              <p style={{ fontSize: '16px', fontWeight: 700, color: 'var(--color-accent)' }}>
                {f.price != null ? `¥${Number(f.price).toFixed(2)}` : ''}
              </p>
            </div>
            <button
              onClick={(e) => { e.stopPropagation(); remove(f.productId); }}
              style={{
                padding: '6px 12px', fontSize: '13px', color: 'var(--color-text-tertiary)',
                background: 'none', border: '1px solid var(--color-border)', borderRadius: 'var(--radius-sm)', cursor: 'pointer',
              }}
            >
              取消收藏
            </button>
          </div>
        ))}
      </div>
    </div>
  );
};

export default FavoritePage;