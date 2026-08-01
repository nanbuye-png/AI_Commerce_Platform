import React, { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { productApi, type ProductDetailVO, type ProductUpdateRequest } from '../../api/product';

const ProductEditPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const [productName, setProductName] = useState('');
  const [description, setDescription] = useState('');
  const [brand, setBrand] = useState('');
  const [categoryId, setCategoryId] = useState<number>(0);
  const [skuCode, setSkuCode] = useState('');
  const [price, setPrice] = useState('');
  const [originalPrice, setOriginalPrice] = useState('');
  const [stock, setStock] = useState('');

  useEffect(() => {
    if (!id) return;
    let cancelled = false;
    productApi
      .getById(Number(id))
      .then((res) => {
        if (cancelled) return;
        const p: ProductDetailVO = res.data;
        setProductName(p.productName);
        setDescription(p.description ?? '');
        setBrand(p.brand ?? '');
        setCategoryId(p.categoryId);
        const sku = p.skus?.[0];
        if (sku) {
          setSkuCode(sku.skuCode);
          setPrice(String(sku.price));
          setOriginalPrice(sku.originalPrice ? String(sku.originalPrice) : '');
        }
      })
      .catch((err) => {
        console.error('加载商品失败:', err);
        setError('商品不存在或加载失败');
      })
      .finally(() => {
        if (!cancelled) setLoading(false);
      });
    return () => {
      cancelled = true;
    };
  }, [id]);

  const handleSubmit = async () => {
    if (!id) return;
    if (!productName.trim() || !categoryId || !skuCode.trim() || !price) {
      setError('商品名称、分类、SKU 编码与售价均不能为空');
      return;
    }

    const payload: ProductUpdateRequest = {
      productName: productName.trim(),
      description: description.trim() || undefined,
      brand: brand.trim() || undefined,
      categoryId,
      skus: [
        {
          skuCode: skuCode.trim(),
          attributesJson: '{}',
          price: Number(price),
          ...(originalPrice ? { originalPrice: Number(originalPrice) } : {}),
          ...(stock ? { stock: Number(stock) } : {}),
        },
      ],
    };

    setSaving(true);
    setError(null);
    try {
      await productApi.update(Number(id), payload);
      navigate('/products');
    } catch (err) {
      console.error('更新失败:', err);
      setError('更新失败，请稍后重试');
    } finally {
      setSaving(false);
    }
  };

  const inputStyle: React.CSSProperties = {
    width: '100%',
    height: 40,
    padding: '0 12px',
    borderRadius: 'var(--radius-sm)',
    border: '1px solid var(--color-border)',
    fontSize: '14px',
    outline: 'none',
    background: 'var(--color-bg-primary)',
  };

  if (loading) {
    return <p style={{ textAlign: 'center', padding: 'var(--spacing-3xl)', color: 'var(--color-text-tertiary)' }}>加载中...</p>;
  }

  return (
    <div style={{ maxWidth: 800, margin: '0 auto' }}>
      <h1 style={{ fontSize: 'var(--font-size-h1)', fontWeight: 600, marginBottom: 'var(--spacing-lg)' }}>编辑商品</h1>

      {error && (
        <div style={{ padding: '10px 14px', background: 'rgba(255,59,48,0.08)', color: '#FF3B30', borderRadius: 'var(--radius-md)', marginBottom: 'var(--spacing-md)', fontSize: '14px' }}>{error}</div>
      )}

      <div style={{ background: 'var(--color-bg-primary)', borderRadius: 'var(--radius-md)', padding: 'var(--spacing-xl)', boxShadow: 'var(--shadow-sm)' }}>
        <div style={{ marginBottom: 'var(--spacing-md)' }}>
          <label style={{ display: 'block', fontSize: '14px', fontWeight: 500, marginBottom: 6 }}>商品名称 *</label>
          <input value={productName} onChange={(e) => setProductName(e.target.value)} style={inputStyle} />
        </div>
        <div style={{ marginBottom: 'var(--spacing-md)' }}>
          <label style={{ display: 'block', fontSize: '14px', fontWeight: 500, marginBottom: 6 }}>商品描述</label>
          <textarea value={description} onChange={(e) => setDescription(e.target.value)} rows={4} style={{ ...inputStyle, height: 'auto', padding: '10px 12px', resize: 'vertical' }} />
        </div>
        <div style={{ marginBottom: 'var(--spacing-md)' }}>
          <label style={{ display: 'block', fontSize: '14px', fontWeight: 500, marginBottom: 6 }}>品牌</label>
          <input value={brand} onChange={(e) => setBrand(e.target.value)} style={inputStyle} />
        </div>
        <div style={{ marginBottom: 'var(--spacing-md)' }}>
          <label style={{ display: 'block', fontSize: '14px', fontWeight: 500, marginBottom: 6 }}>分类 ID *</label>
          <input value={categoryId || ''} onChange={(e) => setCategoryId(Number(e.target.value))} type="number" style={inputStyle} />
          <p style={{ fontSize: '12px', color: 'var(--color-text-tertiary)', marginTop: 4 }}>当前分类 ID，可参考创建页的分类树</p>
        </div>

        <div style={{ marginBottom: 'var(--spacing-lg)' }}>
          <label style={{ display: 'block', fontSize: '14px', fontWeight: 500, marginBottom: 6 }}>SKU *</label>
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr 1fr 1fr', gap: 8 }}>
            <input value={skuCode} onChange={(e) => setSkuCode(e.target.value)} placeholder="SKU编码" style={inputStyle} />
            <input value={price} onChange={(e) => setPrice(e.target.value)} type="number" placeholder="售价" style={inputStyle} />
            <input value={originalPrice} onChange={(e) => setOriginalPrice(e.target.value)} type="number" placeholder="原价(可选)" style={inputStyle} />
            <input value={stock} onChange={(e) => setStock(e.target.value)} type="number" placeholder="库存(可选)" style={inputStyle} />
          </div>
        </div>

        <div style={{ display: 'flex', gap: 'var(--spacing-sm)' }}>
          <button onClick={handleSubmit} disabled={saving} style={{ padding: '10px 24px', borderRadius: 'var(--radius-sm)', background: 'var(--color-accent)', color: '#fff', fontSize: '14px', fontWeight: 500, border: 'none', cursor: saving ? 'wait' : 'pointer' }}>
            {saving ? '提交中...' : '保存'}
          </button>
          <button onClick={() => navigate('/products')} style={{ padding: '10px 24px', borderRadius: 'var(--radius-sm)', border: '1px solid var(--color-border)', background: 'transparent', color: 'var(--color-text-secondary)', fontSize: '14px', cursor: 'pointer' }}>取消</button>
        </div>
      </div>
    </div>
  );
};

export default ProductEditPage;