import React, { useEffect, useState, useRef } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { productApi, type ProductDetailVO, type ProductUpdateRequest } from '../../api/product';
import { uploadApi } from '../../api/upload';

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
  const [images, setImages] = useState<{ url: string; isCover: boolean }[]>([]);
  const [uploading, setUploading] = useState(false);
  const fileInputRef = useRef<HTMLInputElement>(null);
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
        // 加载已有图片
        if (p.images && p.images.length > 0) {
          setImages(p.images.map((img) => ({ url: img.url, isCover: img.isCover ?? false })));
        }
        const sku = p.skus?.[0];
        if (sku) {
          setSkuCode(sku.skuCode);
          setPrice(String(sku.price));
          setOriginalPrice(sku.originalPrice ? String(sku.originalPrice) : '');
          setStock(sku.weight ? String(sku.weight) : '');
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

  const handleFileSelect = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;
    setUploading(true);
    setError(null);
    try {
      const url = await uploadApi.uploadImage(file);
      if (url) {
        setImages((prev) => [...prev, { url, isCover: prev.length === 0 }]);
      } else {
        setError('图片上传失败，请重试');
      }
    } catch (err) {
      console.error('图片上传失败:', err);
      setError('图片上传失败，请重试');
    } finally {
      setUploading(false);
      if (fileInputRef.current) fileInputRef.current.value = '';
    }
  };

  const removeImage = (idx: number) => {
    setImages(images.filter((_, i) => i !== idx));
  };

  const setCover = (idx: number) => {
    setImages(images.map((img, i) => ({ ...img, isCover: i === idx })));
  };

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
      images: images.length
        ? images.map((img, i) => ({
            url: img.url,
            imageType: 'MAIN' as const,
            sort: i,
            isCover: img.isCover ?? i === 0,
          }))
        : undefined,
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

        {/* 图片 */}
        <div style={{ marginBottom: 'var(--spacing-lg)' }}>
          <label style={{ display: 'block', fontSize: '14px', fontWeight: 500, marginBottom: 6 }}>商品图片</label>
          <div style={{ display: 'flex', gap: 8 }}>
            <input
              ref={fileInputRef}
              type="file"
              accept="image/*"
              onChange={handleFileSelect}
              disabled={uploading}
              style={{ flex: 1, padding: '8px', borderRadius: 'var(--radius-sm)', border: '1px solid var(--color-border)', fontSize: '13px' }}
            />
            <button
              onClick={() => fileInputRef.current?.click()}
              disabled={uploading}
              style={{ padding: '0 16px', borderRadius: 'var(--radius-sm)', border: '1px solid var(--color-border)', background: uploading ? 'var(--color-bg-secondary)' : 'transparent', cursor: uploading ? 'wait' : 'pointer', whiteSpace: 'nowrap' }}
            >
              {uploading ? '上传中...' : '选择文件'}
            </button>
          </div>
          {images.length > 0 && (
            <div style={{ display: 'flex', gap: 8, marginTop: 8, flexWrap: 'wrap' }}>
              {images.map((img, i) => (
                <div key={i} style={{ position: 'relative', width: 64, height: 64, borderRadius: 'var(--radius-sm)', overflow: 'hidden', border: img.isCover ? '2px solid var(--color-accent)' : '1px solid var(--color-border)' }}>
                  <img src={img.url} alt="" style={{ width: '100%', height: '100%', objectFit: 'cover' }} />
                  {!img.isCover && (
                    <button
                      onClick={() => setCover(i)}
                      title="设为主图"
                      style={{ position: 'absolute', bottom: 0, left: 0, width: '100%', padding: '2px 0', background: 'rgba(0,0,0,0.5)', color: '#fff', border: 'none', cursor: 'pointer', fontSize: '10px', lineHeight: '14px' }}
                    >
                      主图
                    </button>
                  )}
                  <button
                    onClick={() => removeImage(i)}
                    style={{ position: 'absolute', top: 0, right: 0, width: 18, height: 18, background: 'rgba(0,0,0,0.6)', color: '#fff', border: 'none', cursor: 'pointer', fontSize: '11px', lineHeight: '18px' }}
                  >
                    ×
                  </button>
                </div>
              ))}
            </div>
          )}
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
          <button onClick={handleSubmit} disabled={saving || uploading} style={{ padding: '10px 24px', borderRadius: 'var(--radius-sm)', background: 'var(--color-accent)', color: '#fff', fontSize: '14px', fontWeight: 500, border: 'none', cursor: saving || uploading ? 'wait' : 'pointer' }}>
            {saving ? '提交中...' : '保存'}
          </button>
          <button onClick={() => navigate('/products')} style={{ padding: '10px 24px', borderRadius: 'var(--radius-sm)', border: '1px solid var(--color-border)', background: 'transparent', color: 'var(--color-text-secondary)', fontSize: '14px', cursor: 'pointer' }}>取消</button>
        </div>
      </div>
    </div>
  );
};

export default ProductEditPage;