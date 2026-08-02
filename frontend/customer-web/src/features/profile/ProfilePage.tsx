import React from 'react';
import { useNavigate } from 'react-router-dom';
import useAuthStore from '../../stores/authStore';

const ProfilePage: React.FC = () => {
  const navigate = useNavigate();
  const { userInfo, logout } = useAuthStore();

  const menuItems = [
    { label: '我的订单', icon: '📦', path: '/orders' },
    { label: '收货地址', icon: '📍', path: '/profile/address' },
    { label: '优惠券', icon: '🎫', path: '/profile/coupons' },
    { label: '收藏夹', icon: '❤️', path: '/profile/favorites' },
    { label: '浏览历史', icon: '🕐', path: '/profile/history' },
    { label: '账号设置', icon: '⚙️', path: '/profile/settings' },
  ];

  return (
    <div style={{ padding: 'var(--spacing-xl) var(--spacing-lg)', maxWidth: 800, margin: '0 auto' }}>
      {/* User Profile Card */}
      <div
        style={{
          background: 'var(--color-bg-primary)',
          borderRadius: 'var(--radius-lg)',
          boxShadow: 'var(--shadow-sm)',
          padding: 'var(--spacing-xl)',
          marginBottom: 'var(--spacing-xl)',
        }}
      >
        <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--spacing-lg)' }}>
          <div
            style={{
              width: 72,
              height: 72,
              borderRadius: '50%',
              background: 'var(--color-accent-light)',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              fontSize: '28px',
              color: 'var(--color-accent)',
              fontWeight: 600,
            }}
          >
            {userInfo?.nickname?.charAt(0)?.toUpperCase() || 'U'}
          </div>
          <div>
            <h2 style={{ fontSize: 'var(--font-size-h2)', fontWeight: 600, color: 'var(--color-text-primary)', marginBottom: 'var(--spacing-xs)' }}>
              {userInfo?.nickname || '用户'}
            </h2>
            <p style={{ fontSize: '14px', color: 'var(--color-text-secondary)' }}>
              {userInfo?.email || ''}
            </p>
          </div>
        </div>
      </div>

      {/* Menu Items */}
      <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--spacing-sm)' }}>
        {menuItems.map((item) => (
          <div
            key={item.label}
            onClick={() => navigate(item.path)}
            style={{
              display: 'flex',
              alignItems: 'center',
              gap: 'var(--spacing-md)',
              padding: 'var(--spacing-md) var(--spacing-lg)',
              background: 'var(--color-bg-primary)',
              borderRadius: 'var(--radius-md)',
              cursor: 'pointer',
              boxShadow: 'var(--shadow-sm)',
              transition: 'box-shadow var(--transition-fast)',
            }}
            onMouseEnter={(e) => { (e.currentTarget as HTMLElement).style.boxShadow = 'var(--shadow-md)'; }}
            onMouseLeave={(e) => { (e.currentTarget as HTMLElement).style.boxShadow = 'var(--shadow-sm)'; }}
          >
            <span style={{ fontSize: '18px' }}>{item.icon}</span>
            <span style={{ fontSize: '15px', color: 'var(--color-text-primary)' }}>{item.label}</span>
          </div>
        ))}
      </div>

      {/* Logout Button */}
      <div style={{ marginTop: 'var(--spacing-xl)', textAlign: 'center' }}>
        <button
          onClick={logout}
          style={{
            padding: '12px 32px',
            fontSize: '15px',
            color: 'var(--color-error)',
            background: 'transparent',
            border: '1px solid var(--color-error)',
            borderRadius: 'var(--radius-sm)',
            cursor: 'pointer',
          }}
        >
          退出登录
        </button>
      </div>
    </div>
  );
};

export default ProfilePage;