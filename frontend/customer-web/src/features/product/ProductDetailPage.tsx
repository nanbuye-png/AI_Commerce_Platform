import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import ProductGallery from './components/ProductGallery';
import ProductPrice from './components/ProductPrice';
import ProductSkeleton from './components/ProductSkeleton';
import { productService, type ProductView } from '../../services/product';
import { cartService } from '../../services/cart';
import { profileService } from '../../services/profile';
import { getToken } from '../../utils/token';

const ProductDetailPage: React.FC = () => {
  const { productId } = useParams<{ productId: string }>();
  const navigate = useNavigate();
  const [product, setProduct] = useState<ProductView | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [adding, setAdding] = useState(false);
  const [added, setAdded] = useState(false);
  const [buying, setBuying] = useState(false);
  const [isFavorited, setIsFavorited] = useState(false);
  const [favLoading, setFavLoading] = useState(false);

  /** 收藏 / 取消收藏 */
  const handleFavorite = async () => {
    if (!product) return;
    if (!getToken()) {
      navigate('/login');
      return;
    }
    setFavLoading(true);
    try {
      if (isFavorited) {
        await profileService.removeFavorite(product.id);
      } else {
        await profileService.addFavorite({
          productId: product.id,
          productName: product.name,
          productImage: product.thumbnail || undefined,
          price: product.price,
        });
      }
      setIsFavorited(!isFavorited);
    } catch (err) {
      console.error('收藏操作失败:', err);
      alert('收藏操作失败，请稍后重试');
    } finally {
      setFavLoading(false);
    }
  };

  const handleAddToCart = async () => {
    if (!product) return;
    if (!getToken()) {
      navigate('/login');
      return;
    }
    const sku = product.skus?.[0];
    if (!sku) {
      alert('该商品暂无可购买规格');
      return;
    }
    if ((sku.stock ?? 0) < 1) {
      alert('商品库存不足，暂无法加入购物车');
      return;
    }
    setAdding(true);
    try {
      await cartService.addItem({
        skuId: sku.id,
        productId: product.id,
        productName: product.name,
        productImage: product.thumbnail || undefined,
        price: sku.price || product.price,
        quantity: 1,
      });
      setAdded(true);
      setTimeout(() => setAdded(false), 2000);
    } catch (err) {
      console.error('加入购物车失败:', err);
      alert('加入购物车失败，请稍后重试');
    } finally {
      setAdding(false);
    }
  };

  /** 立即购买：先校验库存，再跳转结算页 */
  const handleBuyNow = async () => {
    if (!product) return;
    if (!getToken()) {
      navigate('/login');
      return;
    }
    const sku = product.skus?.[0];
    if (!sku) {
      alert('该商品暂无可购买规格');
      return;
    }
    setBuying(true);
    try {
      // 调用后端库存校验接口（库存不足时返回 false）
      const ok = await profileService.checkStock(sku.id, 1);
      if (!ok) {
        const stock = await profileService.getStock(sku.id);
        alert(`商品库存不足，当前仅剩 ${stock} 件`);
        return;
      }
      navigate(`/checkout?skuId=${sku.id}&productId=${product.id}&quantity=1&price=${sku.price || product.price}&name=${encodeURIComponent(product.name)}&image=${encodeURIComponent(product.thumbnail || '')}`);
    } catch (err) {
      console.error('库存校验失败:', err);
      alert('库存校验失败，请稍后重试');
    } finally {
      setBuying(false);
    }
  };

  useEffect(() => {
    if (!productId) {
      setLoading(false);
      setError('缺少商品 ID');
      return;
    }

    let cancelled = false;
    setLoading(true);
    setError(null);

    productService
      .getProductDetail(productId)
      .then((res) => {
        if (cancelled) return;
        setProduct(res);
        // 记录浏览历史（登录用户）
        if (getToken()) {
          profileService.addBrowseHistory({
            productId: res.id,
            productName: res.name,
            productImage: res.thumbnail || undefined,
            price: res.price,
          }).catch(() => {});
          // 检查当前商品是否已收藏
          profileService.listFavorites(1, 100)
            .then((favRes) => {
              if (!cancelled) {
                setIsFavorited(favRes.list?.some((f) => f.productId === res.id) ?? false);
              }
            })
            .catch(() => {});
        }
      })
      .catch((err: unknown) => {
        if (!cancelled) {
          console.error('加载商品详情失败:', err);
          setError('商品不存在或已下架');
        }
      })
      .finally(() => {
        if (!cancelled) setLoading(false);
      });

    return () => {
      cancelled = true;
    };
  }, [productId]);

  if (loading) {
    return (
      <div style={{ padding: 'var(--spacing-xl) var(--spacing-lg)', maxWidth: 960, margin: '0 auto' }}>
        <ProductSkeleton variant="detail" />
      </div>
    );
  }

  if (error || !product) {
    return (
      <div style={{ padding: 'var(--spacing-xl) var(--spacing-lg)', maxWidth: 960, margin: '0 auto', textAlign: 'center' }}>
        <h2 style={{ fontSize: 'var(--font-size-h2)', color: 'var(--color-text-secondary)', marginBottom: 'var(--spacing-md)' }}>
          {error ?? '商品信息加载中...'}
        </h2>
        <button
          onClick={() => navigate('/')}
          style={{
            padding: '10px 24px',
            borderRadius: 'var(--radius-sm)',
            background: 'var(--color-accent)',
            color: '#fff',
            fontSize: '14px',
            fontWeight: 500,
            border: 'none',
            cursor: 'pointer',
          }}
        >
          返回首页
        </button>
      </div>
    );
  }

  const stock = product.skus?.[0]?.stock ?? 0;

  return (
    <div style={{ padding: 'var(--spacing-xl) var(--spacing-lg)', maxWidth: 960, margin: '0 auto' }}>
      {/* Breadcrumb */}
      <div style={{ fontSize: '13px', color: 'var(--color-text-tertiary)', marginBottom: 'var(--spacing-lg)', display: 'flex', gap: 4, alignItems: 'center' }}>
        <span onClick={() => navigate('/')} style={{ cursor: 'pointer' }}>首页</span>
        <span>/</span>
        <span onClick={() => navigate('/products')} style={{ cursor: 'pointer' }}>商品</span>
        {product.categoryName && (
          <>
            <span>/</span>
            <span>{product.categoryName}</span>
          </>
        )}
      </div>

      {/* Product Main Area */}
      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 'var(--spacing-xl)', marginBottom: 'var(--spacing-xl)' }}>
        {/* Gallery */}
        <div>
          <ProductGallery
            images={product.images.map((url, idx) => ({
              id: `${product.id}-img-${idx}`,
              url,
              alt: product.name,
              isPrimary: idx === 0,
            }))}
            thumbnail={product.thumbnail}
          />
        </div>

        {/* Info */}
        <div>
          <h1 style={{ fontSize: 'var(--font-size-h2)', fontWeight: 600, color: 'var(--color-text-primary)', marginBottom: 'var(--spacing-sm)' }}>
            {product.name}
          </h1>
          {product.brand && (
            <p style={{ fontSize: '14px', color: 'var(--color-text-secondary)', marginBottom: 'var(--spacing-sm)' }}>
              品牌：{product.brand}
            </p>
          )}
          <p style={{ fontSize: '14px', color: 'var(--color-text-tertiary)', marginBottom: 'var(--spacing-md)' }}>
            {product.description}
          </p>

          {/* Price */}
          <div style={{ padding: 'var(--spacing-md)', background: 'var(--color-bg-secondary)', borderRadius: 'var(--radius-md)', marginBottom: 'var(--spacing-lg)' }}>
            <ProductPrice
              price={product.price}
              originalPrice={product.originalPrice}
              size="lg"
            />
          </div>

          {/* Ratings & Sales */}
          <div style={{ display: 'flex', gap: 'var(--spacing-lg)', marginBottom: 'var(--spacing-lg)', fontSize: '14px', color: 'var(--color-text-secondary)' }}>
            <span>★ {product.rating.toFixed(1)} ({product.reviewCount}条评价)</span>
            <span>已售 {product.salesCount}</span>
            <span style={{ color: stock > 0 ? 'var(--color-success)' : 'var(--color-error)' }}>
              库存 {stock > 0 ? stock : '无货'}
            </span>
          </div>

          {/* Specifications Selector */}
          {product.specs?.map((specGroup) => (
            <div key={specGroup.name} style={{ marginBottom: 'var(--spacing-md)' }}>
              <h3 style={{ fontSize: '14px', fontWeight: 500, color: 'var(--color-text-primary)', marginBottom: 'var(--spacing-sm)' }}>
                {specGroup.name}
              </h3>
              <div style={{ display: 'flex', gap: 'var(--spacing-sm)', flexWrap: 'wrap' }}>
                {specGroup.options.map((opt) => (
                  <button
                    key={opt.value}
                    style={{
                      padding: '8px 16px',
                      borderRadius: 'var(--radius-sm)',
                      border: '1px solid var(--color-border)',
                      background: 'transparent',
                      color: 'var(--color-text-primary)',
                      fontSize: '13px',
                      cursor: 'pointer',
                      transition: 'all var(--transition-fast)',
                    }}
                  >
                    {opt.name}
                  </button>
                ))}
              </div>
            </div>
          ))}

          {/* Action Buttons */}
          <div style={{ display: 'flex', gap: 'var(--spacing-sm)' }}>
            <button
              onClick={handleFavorite}
              disabled={favLoading}
              title={isFavorited ? '取消收藏' : '收藏'}
              style={{
                width: 48,
                height: 48,
                borderRadius: 'var(--radius-sm)',
                background: isFavorited ? 'rgba(255, 59, 48, 0.1)' : 'var(--color-bg-secondary)',
                color: isFavorited ? '#FF3B30' : 'var(--color-text-tertiary)',
                fontSize: '20px',
                fontWeight: 600,
                border: isFavorited ? '1px solid #FF3B30' : '1px solid var(--color-border)',
                cursor: favLoading ? 'not-allowed' : 'pointer',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                flexShrink: 0,
              }}
            >
              {isFavorited ? '♥' : '♡'}
            </button>
            <button
              onClick={handleAddToCart}
              disabled={adding || stock <= 0}
              style={{
                flex: 1,
                height: 48,
                borderRadius: 'var(--radius-sm)',
                background: 'var(--color-accent)',
                color: '#fff',
                fontSize: '16px',
                fontWeight: 500,
                border: 'none',
                cursor: adding || stock <= 0 ? 'not-allowed' : 'pointer',
                opacity: adding || stock <= 0 ? 0.6 : 1,
              }}
            >
              {added ? '已加入 ✓' : '加入购物车'}
            </button>
            <button
              onClick={handleBuyNow}
              disabled={buying || stock <= 0}
              style={{
                flex: 1,
                height: 48,
                borderRadius: 'var(--radius-sm)',
                background: 'var(--color-text-primary)',
                color: '#fff',
                fontSize: '16px',
                fontWeight: 500,
                border: 'none',
                cursor: buying || stock <= 0 ? 'not-allowed' : 'pointer',
                opacity: buying || stock <= 0 ? 0.6 : 1,
              }}
            >
              {buying ? '校验中...' : '立即购买'}
            </button>
          </div>
        </div>
      </div>

      {/* AI Advisor */}
      <div
        style={{
          padding: 'var(--spacing-xl)',
          background: 'linear-gradient(135deg, var(--color-accent-light) 0%, #f0f7ff 100%)',
          borderRadius: 'var(--radius-lg)',
          marginBottom: 'var(--spacing-xl)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between',
          flexWrap: 'wrap',
          gap: 'var(--spacing-md)',
        }}
      >
        <div>
          <h3 style={{ fontSize: '16px', fontWeight: 600, color: 'var(--color-accent)', marginBottom: 4 }}>
            AI 购买顾问
          </h3>
          <p style={{ fontSize: '14px', color: 'var(--color-text-secondary)' }}>
            想了解更多？问问 AI 助手这款商品是否适合你
          </p>
        </div>
        <button
          onClick={() => navigate('/ai')}
          style={{
            padding: '10px 24px',
            borderRadius: 'var(--radius-sm)',
            background: 'var(--color-accent)',
            color: '#fff',
            fontSize: '14px',
            fontWeight: 500,
            border: 'none',
            cursor: 'pointer',
          }}
        >
          咨询 AI
        </button>
      </div>
    </div>
  );
};

export default ProductDetailPage;