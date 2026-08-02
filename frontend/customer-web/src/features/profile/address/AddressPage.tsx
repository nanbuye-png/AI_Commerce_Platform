import React, { useEffect, useState } from 'react';
import { profileService, type Address } from '../../../services/profile';

const AddressPage: React.FC = () => {
  const [addresses, setAddresses] = useState<Address[]>([]);
  const [loading, setLoading] = useState(true);
  const [editing, setEditing] = useState<Address | null>(null);
  const [showForm, setShowForm] = useState(false);
  const [form, setForm] = useState({
    receiver: '',
    phone: '',
    province: '',
    city: '',
    district: '',
    detailAddress: '',
    postalCode: '',
    isDefault: false,
  });

  const load = () => {
    profileService.listAddresses()
      .then(setAddresses)
      .catch(() => {})
      .finally(() => setLoading(false));
  };

  useEffect(() => { load(); }, []);

  const startAdd = () => {
    setEditing(null);
    setForm({ receiver: '', phone: '', province: '', city: '', district: '', detailAddress: '', postalCode: '', isDefault: addresses.length === 0 });
    setShowForm(true);
  };

  const startEdit = (a: Address) => {
    setEditing(a);
    setForm({
      receiver: a.receiver,
      phone: a.phone,
      province: a.province ?? '',
      city: a.city ?? '',
      district: a.district ?? '',
      detailAddress: a.detailAddress ?? '',
      postalCode: a.postalCode ?? '',
      isDefault: !!a.isDefault,
    });
    setShowForm(true);
  };

  const save = async () => {
    if (!form.receiver || !form.phone) { alert('收件人和手机号不能为空'); return; }
    try {
      if (editing) {
        await profileService.updateAddress(editing.id, form);
      } else {
        await profileService.createAddress(form);
      }
      setShowForm(false);
      load();
    } catch { alert('保存失败'); }
  };

  const remove = async (id: number) => {
    if (!window.confirm('确定删除该收货地址吗？')) return;
    try {
      await profileService.deleteAddress(id);
      load();
    } catch { alert('删除失败'); }
  };

  const setDefault = async (id: number) => {
    try {
      await profileService.setDefaultAddress(id);
      load();
    } catch { alert('设置失败'); }
  };

  const inputStyle: React.CSSProperties = {
    width: '100%', padding: '8px 12px', borderRadius: 'var(--radius-sm)',
    border: '1px solid var(--color-border)', fontSize: '14px',
  };

  return (
    <div style={{ padding: 'var(--spacing-xl) var(--spacing-lg)', maxWidth: 700, margin: '0 auto' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 'var(--spacing-lg)' }}>
        <h1 style={{ fontSize: 'var(--font-size-h1)', fontWeight: 600 }}>收货地址</h1>
        <button onClick={startAdd} style={{
          padding: '8px 18px', borderRadius: 'var(--radius-sm)', border: 'none',
          background: 'var(--color-accent)', color: '#fff', fontSize: '14px', cursor: 'pointer',
        }}>+ 新增地址</button>
      </div>

      {loading && <p style={{ color: 'var(--color-text-tertiary)' }}>加载中...</p>}

      {!loading && addresses.length === 0 && !showForm && (
        <div style={{ textAlign: 'center', padding: 'var(--spacing-2xl)', color: 'var(--color-text-secondary)' }}>
          <p style={{ fontSize: '40px', marginBottom: 8 }}>📍</p>
          <p>还没有收货地址</p>
        </div>
      )}

      {showForm && (
        <div style={{ background: 'var(--color-bg-primary)', padding: 'var(--spacing-lg)', borderRadius: 'var(--radius-md)', boxShadow: 'var(--shadow-sm)', marginBottom: 'var(--spacing-lg)' }}>
          <h3 style={{ fontSize: '16px', fontWeight: 600, marginBottom: 'var(--spacing-md)' }}>
            {editing ? '编辑地址' : '新增地址'}
          </h3>
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 'var(--spacing-sm)' }}>
            <input placeholder="收件人 *" value={form.receiver} onChange={(e) => setForm({ ...form, receiver: e.target.value })} style={inputStyle} />
            <input placeholder="手机号 *" value={form.phone} onChange={(e) => setForm({ ...form, phone: e.target.value })} style={inputStyle} />
            <input placeholder="省份" value={form.province} onChange={(e) => setForm({ ...form, province: e.target.value })} style={inputStyle} />
            <input placeholder="城市" value={form.city} onChange={(e) => setForm({ ...form, city: e.target.value })} style={inputStyle} />
            <input placeholder="区/县" value={form.district} onChange={(e) => setForm({ ...form, district: e.target.value })} style={inputStyle} />
            <input placeholder="邮编" value={form.postalCode} onChange={(e) => setForm({ ...form, postalCode: e.target.value })} style={inputStyle} />
          </div>
          <input placeholder="详细地址" value={form.detailAddress} onChange={(e) => setForm({ ...form, detailAddress: e.target.value })} style={{ ...inputStyle, marginTop: 'var(--spacing-sm)' }} />
          <label style={{ display: 'flex', alignItems: 'center', gap: 6, marginTop: 'var(--spacing-sm)', fontSize: '14px', cursor: 'pointer' }}>
            <input type="checkbox" checked={form.isDefault} onChange={(e) => setForm({ ...form, isDefault: e.target.checked })} />
            设为默认地址
          </label>
          <div style={{ display: 'flex', gap: 'var(--spacing-sm)', marginTop: 'var(--spacing-md)' }}>
            <button onClick={save} style={{
              padding: '8px 24px', borderRadius: 'var(--radius-sm)', border: 'none',
              background: 'var(--color-accent)', color: '#fff', cursor: 'pointer',
            }}>保存</button>
            <button onClick={() => setShowForm(false)} style={{
              padding: '8px 24px', borderRadius: 'var(--radius-sm)', border: '1px solid var(--color-border)',
              background: 'transparent', cursor: 'pointer',
            }}>取消</button>
          </div>
        </div>
      )}

      <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--spacing-sm)' }}>
        {addresses.map((a) => (
          <div key={a.id} style={{
            display: 'flex', alignItems: 'center', gap: 'var(--spacing-md)',
            padding: 'var(--spacing-md) var(--spacing-lg)', background: 'var(--color-bg-primary)',
            borderRadius: 'var(--radius-md)', boxShadow: 'var(--shadow-sm)',
          }}>
            <div style={{ flex: 1 }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                <span style={{ fontSize: '15px', fontWeight: 600 }}>{a.receiver}</span>
                <span style={{ fontSize: '13px', color: 'var(--color-text-secondary)' }}>{a.phone}</span>
                {a.isDefault && (
                  <span style={{ fontSize: '11px', padding: '1px 6px', borderRadius: 3, background: 'var(--color-accent)', color: '#fff' }}>默认</span>
                )}
              </div>
              <p style={{ fontSize: '13px', color: 'var(--color-text-secondary)', marginTop: 4 }}>
                {[a.province, a.city, a.district, a.detailAddress].filter(Boolean).join(' ')}
              </p>
            </div>
            <div style={{ display: 'flex', gap: 8 }}>
              {!a.isDefault && (
                <button onClick={() => setDefault(a.id)} style={{ fontSize: '13px', color: 'var(--color-accent)', background: 'none', border: 'none', cursor: 'pointer' }}>设默认</button>
              )}
              <button onClick={() => startEdit(a)} style={{ fontSize: '13px', color: 'var(--color-text-secondary)', background: 'none', border: 'none', cursor: 'pointer' }}>编辑</button>
              <button onClick={() => remove(a.id)} style={{ fontSize: '13px', color: 'var(--color-error)', background: 'none', border: 'none', cursor: 'pointer' }}>删除</button>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
};

export default AddressPage;