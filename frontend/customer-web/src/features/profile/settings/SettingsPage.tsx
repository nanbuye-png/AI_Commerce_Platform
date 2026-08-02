import React, { useEffect, useState } from 'react';
import { profileService, type UserProfile } from '../../../services/profile';

const SettingsPage: React.FC = () => {
  const [profile, setProfile] = useState<UserProfile | null>(null);
  const [nickname, setNickname] = useState('');
  const [phone, setPhone] = useState('');
  const [oldPassword, setOldPassword] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    profileService.getProfile()
      .then((p) => {
        setProfile(p);
        setNickname(p.nickname ?? '');
        setPhone(p.phone ?? '');
      })
      .catch(() => {});
  }, []);

  const saveProfile = async () => {
    setSaving(true);
    try {
      const updated = await profileService.updateProfile({ nickname, phone });
      setProfile(updated);
      alert('资料已更新');
    } catch { alert('保存失败'); } finally { setSaving(false); }
  };

  const changePassword = async () => {
    if (!oldPassword || !newPassword) { alert('请填写原密码和新密码'); return; }
    if (newPassword.length < 6) { alert('新密码长度不能少于6位'); return; }
    try {
      await profileService.changePassword({ oldPassword, newPassword });
      alert('密码修改成功');
      setOldPassword('');
      setNewPassword('');
    } catch { alert('修改失败，请检查原密码'); }
  };

  const inputStyle: React.CSSProperties = {
    width: '100%', padding: '8px 12px', borderRadius: 'var(--radius-sm)',
    border: '1px solid var(--color-border)', fontSize: '14px',
  };

  return (
    <div style={{ padding: 'var(--spacing-xl) var(--spacing-lg)', maxWidth: 600, margin: '0 auto' }}>
      <h1 style={{ fontSize: 'var(--font-size-h1)', fontWeight: 600, marginBottom: 'var(--spacing-lg)' }}>账号设置</h1>

      {/* Profile Info */}
      <div style={{ background: 'var(--color-bg-primary)', borderRadius: 'var(--radius-md)', boxShadow: 'var(--shadow-sm)', padding: 'var(--spacing-lg)', marginBottom: 'var(--spacing-lg)' }}>
        <h3 style={{ fontSize: '16px', fontWeight: 600, marginBottom: 'var(--spacing-md)' }}>个人资料</h3>
        <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--spacing-sm)' }}>
          <div>
            <label style={{ fontSize: '13px', color: 'var(--color-text-secondary)', display: 'block', marginBottom: 4 }}>用户名</label>
            <input value={profile?.username ?? ''} disabled style={{ ...inputStyle, background: 'var(--color-bg-secondary)' }} />
          </div>
          <div>
            <label style={{ fontSize: '13px', color: 'var(--color-text-secondary)', display: 'block', marginBottom: 4 }}>邮箱</label>
            <input value={profile?.email ?? ''} disabled style={{ ...inputStyle, background: 'var(--color-bg-secondary)' }} />
          </div>
          <div>
            <label style={{ fontSize: '13px', color: 'var(--color-text-secondary)', display: 'block', marginBottom: 4 }}>昵称</label>
            <input value={nickname} onChange={(e) => setNickname(e.target.value)} style={inputStyle} />
          </div>
          <div>
            <label style={{ fontSize: '13px', color: 'var(--color-text-secondary)', display: 'block', marginBottom: 4 }}>手机号</label>
            <input value={phone} onChange={(e) => setPhone(e.target.value)} style={inputStyle} />
          </div>
          <button
            onClick={saveProfile}
            disabled={saving}
            style={{
              alignSelf: 'flex-start', padding: '8px 24px', borderRadius: 'var(--radius-sm)', border: 'none',
              background: 'var(--color-accent)', color: '#fff', cursor: saving ? 'not-allowed' : 'pointer',
            }}
          >
            {saving ? '保存中...' : '保存资料'}
          </button>
        </div>
      </div>

      {/* Change Password */}
      <div style={{ background: 'var(--color-bg-primary)', borderRadius: 'var(--radius-md)', boxShadow: 'var(--shadow-sm)', padding: 'var(--spacing-lg)' }}>
        <h3 style={{ fontSize: '16px', fontWeight: 600, marginBottom: 'var(--spacing-md)' }}>修改密码</h3>
        <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--spacing-sm)' }}>
          <input type="password" placeholder="原密码" value={oldPassword} onChange={(e) => setOldPassword(e.target.value)} style={inputStyle} />
          <input type="password" placeholder="新密码（至少6位）" value={newPassword} onChange={(e) => setNewPassword(e.target.value)} style={inputStyle} />
          <button
            onClick={changePassword}
            style={{
              alignSelf: 'flex-start', padding: '8px 24px', borderRadius: 'var(--radius-sm)', border: 'none',
              background: 'var(--color-text-primary)', color: '#fff', cursor: 'pointer',
            }}
          >
            修改密码
          </button>
        </div>
      </div>
    </div>
  );
};

export default SettingsPage;