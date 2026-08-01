import React, { useEffect, useState, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import { productApi, type ProductCreateRequest } from '../../api/product';
import { categoryApi, type CategoryNode } from '../../api/category';
import { uploadApi } from '../../api/upload';

const ProductCreatePage: React.FC = () => {
  const navigate = useNavigate();
  const [categories, setCategories] = useState<CategoryNode[]>([]);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // 表单字段
  const [productName, setProductName] = useState('');
  const [description, setDescription] = useState('');
  const [brand, setBrand] = useState('');
  const [categoryId, setCategoryId] = useState<number>(0);
  // 图片
  const [images, setImages] = useState<{ url: string; isCover: boolean }[]>([]);
  const [uploading, setUploading] = useState(false);
  const fileInputRef = useRef<HTMLInputElement>(null);
  // SKU（单 SKU 简化）
  const [skuCode, setSkuCode] = useState('');
  const [price, setPrice] = useState('');
  const [originalPrice, setOriginalPrice] = useState('');
  const [stock, setStock] = useState('');
  // 规格
  const [specName, setSpecName] = useState('');
  const [specValues, setSpecValues] = useState('');
  const [specs, setSpecs] = useState<{ specName: string; specValues: string }[]>([]);

  useEffect(() => {
    let cancelled = false;
    categoryApi
      .listTree()
      .then((res) => {
        if (!cancelled) setCategories(res.data ?? []);
      })
      .catch((err) => console.error('加载分类失败:', err));
    return () => {
      cancelled = true;
    };
  }, []);

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

  const addSpec = () => {
    if (!specName.trim() || !specValues.trim()) return;
    setSpecs([...specs, { specName: specName.trim(), specValues: specValues.trim() }]);
    setSpecName('');
    setSpecValues('');
  };

  const removeSpec = (idx: number) => {
    setSpecs(specs.filter((_, i) => i !== idx));
  };

  const handleSubmit = async () => {
    if (!productName.trim()) {
      setError('请输入商品名称');
      return;
    }
    if (!categoryId) {
      setError('请选择分类');
      return;
    }
    if (!skuCode.trim() || !price) {
      setError('至少填写一个 SKU 编码与售价');
      return;
    }

    const payload: ProductCreateRequest = {
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
      specs: specs.length
        ? specs.map((s, i) => ({ specName: s.specName, specValues: s.specValues, sort: i }))
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
      const res = await productApi.create(payload);
      console.info('商品创建成功 id=', res.data);
      navigate('/products');
    } catch (err) {
      console.error('创建失败:', err);
      setError('创建失败，请稍后重试');
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

  return (
    <div style={{ maxWidth: 800, margin: '0 auto' }}>
      <h1 style={{ fontSize: 'var(--font-size-h1)', fontWeight: 600, marginBottom: 'var(--spacing-lg)' }}>添加商品</h1>

      {error && (
        <div style={{ padding: '10px 14px', background: 'rgba(255,59,48,0.08)', color: '#FF3B30', borderRadius: 'var(--radius-md)', marginBottom: 'var(--spacing-md)', fontSize: '14px' }}>{error}</div>
      )}

      <div style={{ background: 'var(--color-bg-primary)', borderRadius: 'var(--radius-md)', padding: 'var(--spacing-xl)', boxShadow: 'var(--shadow-sm)' }}>
        {/* 基本信息 */}
        <div style={{ marginBottom: 'var(--spacing-md)' }}>
          <label style={{ display: 'block', fontSize: '14px', fontWeight: 500, marginBottom: 6 }}>商品名称 *</label>
          <input value={productName} onChange={(e) => setProductName(e.target.value)} placeholder="请输入商品名称" style={inputStyle} />
        </div>
        <div style={{ marginBottom: 'var(--spacing-md)' }}>
          <label style={{ display: 'block', fontSize: '14px', fontWeight: 500, marginBottom: 6 }}>商品描述</label>
          <textarea value={description} onChange={(e) => setDescription(e.target.value)} rows={4} placeholder="请输入商品描述" style={{ ...inputStyle, height: 'auto', padding: '10px 12px', resize: 'vertical' }} />
        </div>
        <div style={{ marginBottom: 'var(--spacing-md)' }}>
          <label style={{ display: 'block', fontSize: '14px', fontWeight: 500, marginBottom: 6 }}>品牌</label>
          <input value={brand} onChange={(e) => setBrand(e.target.value)} placeholder="请输入品牌（可选）" style={inputStyle} />
        </div>
        <div style={{ marginBottom: 'var(--spacing-md)' }}>
          <label style={{ display: 'block', fontSize: '14px', fontWeight: 500, marginBottom: 6 }}>分类 *</label>
          <select value={categoryId} onChange={(e) => setCategoryId(Number(e.target.value))} style={inputStyle}>
            <option value={0}>选择分类</option>
            {categories.map((cat) => (
              <optgroup key={cat.id} label={cat.categoryName}>
                {(cat.children ?? []).map((child) => (
                  <option key={child.id} value={child.id}>{child.categoryName}</option>
                ))}
              </optgroup>
            ))}
          </select>
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

        {/* SKU */}
        <div style={{ marginBottom: 'var(--spacing-lg)' }}>
          <label style={{ display: 'block', fontSize: '14px', fontWeight: 500, marginBottom: 6 }}>SKU（售价与库存） *</label>
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr 1fr 1fr', gap: 8 }}>
            <input value={skuCode} onChange={(e) => setSkuCode(e.target.value)} placeholder="SKU编码" style={inputStyle} />
            <input value={price} onChange={(e) => setPrice(e.target.value)} type="number" placeholder="售价" style={inputStyle} />
            <input value={originalPrice} onChange={(e) => setOriginalPrice(e.target.value)} type="number" placeholder="原价(可选)" style={inputStyle} />
            <input value={stock} onChange={(e) => setStock(e.target.value)} type="number" placeholder="库存(可选)" style={inputStyle} />
          </div>
        </div>

        {/* 规格 */}
        <div style={{ marginBottom: 'var(--spacing-lg)' }}>
          <label style={{ display: 'block', fontSize: '14px', fontWeight: 500, marginBottom: 6 }}>商品规格（可选）</label>
          <div style={{ display: 'flex', gap: 8 }}>
            <input value={specName} onChange={(e) => setSpecName(e.target.value)} placeholder="规格名（如 颜色）" style={inputStyle} />
            <input value={specValues} onChange={(e) => setSpecValues(e.target.value)} placeholder="规格值（如 黑色,白色）" style={inputStyle} />
            <button onClick={addSpec} style={{ padding: '0 16px', borderRadius: 'var(--radius-sm)', border: '1px solid var(--color-border)', background: 'transparent', cursor: 'pointer', whiteSpace: 'nowrap' }}>添加</button>
          </div>
          {specs.length > 0 && (
            <div style={{ display: 'flex', gap: 8, marginTop: 8, flexWrap: 'wrap' }}>
              {specs.map((s, i) => (
                <span key={i} style={{ display: 'inline-flex', alignItems: 'center', gap: 6, padding: '6px 12px', borderRadius: 'var(--radius-full)', background: 'var(--color-bg-secondary)', fontSize: '13px' }}>
                  {s.specName}: {s.specValues}
                  <button onClick={() => removeSpec(i)} style={{ border: 'none', background: 'none', cursor: 'pointer', color: '#FF3B30' }}>×</button>
                </span>
              ))}
            </div>
          )}
        </div>

        <div style={{ display: 'flex', gap: 'var(--spacing-sm)', marginTop: 'var(--spacing-lg)' }}>
          <button onClick={handleSubmit} disabled={saving || uploading} style={{ padding: '10px 24px', borderRadius: 'var(--radius-sm)', background: 'var(--color-accent)', color: '#fff', fontSize: '14px', fontWeight: 500, border: 'none', cursor: saving || uploading ? 'wait' : 'pointer' }}>
            {saving ? '提交中...' : '保存'}
          </button>
          <button onClick={() => navigate('/products')} style={{ padding: '10px 24px', borderRadius: 'var(--radius-sm)', border: '1px solid var(--color-border)', background: 'transparent', color: 'var(--color-text-secondary)', fontSize: '14px', cursor: 'pointer' }}>取消</button>
        </div>
      </div>
    </div>
  );
};

export default ProductCreatePage;