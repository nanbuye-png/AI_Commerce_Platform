import React, { useEffect, useState } from 'react';
import { useSearchParams, useNavigate } from 'react-router-dom';
import SearchBar from './components/SearchBar';
import ProductGrid from '../product/components/ProductGrid';
import ProductSkeleton from '../product/components/ProductSkeleton';
import { productService, type ProductView } from '../../services/product';
import { profileService } from '../../services/profile';
import { getToken } from '../../utils/token';
import type { Product } from '../product/types/product';

/** 将 ProductView 映射为 ProductGrid 期望的 Product 类型 */
function toProduct(p: ProductView): Product {
  return {
    id: String(p.id),
    name: p.name,
    description: p.description,
    brand: p.brand,
    categoryId: p.categoryName ?? '',
    categoryName: p.categoryName,
    images: p.images.map((url, idx) => ({
      id: `${p.id}-img-${idx}`,
      url,
      alt: p.name,
      isPrimary: idx === 0,
    })),
    thumbnail: p.thumbnail,
    price: p.price,
    originalPrice: p.originalPrice,
    currency: 'CNY',
    rating: p.rating,
    reviewCount: p.reviewCount,
    salesCount: p.salesCount,
    stock: p.stock,
    status: 'ACTIVE',
    createdAt: '',
    updatedAt: '',
  };
}

const SearchPage: React.FC = () => {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const keyword = searchParams.get('q') || '';
  const [products, setProducts] = useState<Product[]>([]);
  const [total, setTotal] = useState(0);
  const [loading, setLoading] = useState(false);
  const [favoritedIds, setFavoritedIds] = useState<Set<string>>(new Set());

  // 加载已收藏商品集合（登录用户）
  useEffect(() => {
    if (!getToken()) return;
    let cancelled = false;
    profileService
      .listFavorites(1, 100)
      .then((res) => {
        if (!cancelled) {
          setFavoritedIds(new Set((res.list ?? []).map((f) => String(f.productId))));
        }
      })
      .catch(() => {});
    return () => {
      cancelled = true;
    };
  }, []);

  /** 收藏/取消收藏 */
  const handleFavorite = async (productId: string) => {
    if (!getToken()) {
      navigate('/login');
      return;
    }
    const pid = Number(productId);
    const isFav = favoritedIds.has(productId);
    try {
      if (isFav) {
        await profileService.removeFavorite(pid);
      } else {
        const p = products.find((x) => x.id === productId);
        await profileService.addFavorite({
          productId: pid,
          productName: p?.name,
          productImage: p?.thumbnail || undefined,
          price: p?.price,
        });
      }
      setFavoritedIds((prev) => {
        const next = new Set(prev);
        if (isFav) {
          next.delete(productId);
        } else {
          next.add(productId);
        }
        return next;
      });
    } catch (err) {
      console.error('收藏操作失败:', err);
    }
  };

  useEffect(() => {
    if (!keyword) {
      setProducts([]);
      setTotal(0);
      return;
    }

    let cancelled = false;
    setLoading(true);
    setProducts([]);

    productService
      .listProducts({ page: 1, size: 20, keyword })
      .then((res) => {
        if (!cancelled) {
          setProducts(res.items.map(toProduct));
          setTotal(res.total);
        }
      })
      .catch((err: unknown) => {
        if (!cancelled) console.error('搜索失败:', err);
      })
      .finally(() => {
        if (!cancelled) setLoading(false);
      });

    return () => {
      cancelled = true;
    };
  }, [keyword]);

  const hotWords = ['手机', '电脑', '耳机', '手表', '书包', '运动鞋'];

  return (
    <div style={{ padding: 'var(--spacing-xl) var(--spacing-lg)', maxWidth: 1200, margin: '0 auto' }}>
      {/* Search Header */}
      <div style={{ marginBottom: 'var(--spacing-xl)' }}>
        <SearchBar
          placeholder="搜索商品..."
          autoFocus={!keyword}
          size="lg"
        />
      </div>

      {/* Search Results */}
      {keyword ? (
        <>
          <div
            style={{
              display: 'flex',
              justifyContent: 'space-between',
              alignItems: 'center',
              marginBottom: 'var(--spacing-lg)',
            }}
          >
            <h1 style={{ fontSize: 'var(--font-size-h2)', fontWeight: 600, color: 'var(--color-text-primary)' }}>
              搜索：{keyword}
            </h1>
            <span style={{ fontSize: '14px', color: 'var(--color-text-secondary)' }}>
              找到 {total} 件商品
            </span>
          </div>

          {loading ? (
            <div
              style={{
                display: 'grid',
                gridTemplateColumns: 'repeat(auto-fill, minmax(220px, 1fr))',
                gap: 'var(--spacing-md)',
              }}
            >
              {Array.from({ length: 6 }, (_, i) => (
                <ProductSkeleton key={i} />
              ))}
            </div>
          ) : (
            <ProductGrid
              products={products}
              loading={false}
              onFavorite={handleFavorite}
              favoritedIds={favoritedIds}
            />
          )}
        </>
      ) : (
        /* Empty search state */
        <div
          style={{
            textAlign: 'center',
            padding: 'var(--spacing-3xl) var(--spacing-lg)',
          }}
        >
          <div style={{ fontSize: '48px', marginBottom: 'var(--spacing-md)' }}>🔍</div>
          <h2 style={{ fontSize: 'var(--font-size-h3)', fontWeight: 600, color: 'var(--color-text-primary)', marginBottom: 'var(--spacing-sm)' }}>
            搜索商品
          </h2>
          <p style={{ fontSize: '14px', color: 'var(--color-text-secondary)' }}>
            输入关键词，找到你想要的商品
          </p>
          {/* Trending Searches */}
          <div style={{ marginTop: 'var(--spacing-xl)' }}>
            <h3 style={{ fontSize: '14px', color: 'var(--color-text-secondary)', marginBottom: 'var(--spacing-sm)' }}>
              热门搜索
            </h3>
            <div style={{ display: 'flex', gap: 'var(--spacing-sm)', justifyContent: 'center', flexWrap: 'wrap' }}>
              {hotWords.map((word) => (
                <span
                  key={word}
                  onClick={() => navigate(`/search?q=${encodeURIComponent(word)}`)}
                  style={{
                    padding: '4px 12px',
                    borderRadius: 'var(--radius-full)',
                    background: 'var(--color-bg-secondary)',
                    color: 'var(--color-text-primary)',
                    fontSize: '13px',
                    cursor: 'pointer',
                  }}
                >
                  {word}
                </span>
              ))}
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default SearchPage;