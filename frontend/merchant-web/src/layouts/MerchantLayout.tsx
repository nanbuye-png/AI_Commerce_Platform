import React, { useState } from 'react';
import { Outlet, useNavigate, useLocation } from 'react-router-dom';
import useAuthStore from '../stores/authStore';

interface SidebarItem {
  path: string;
  label: string;
  icon: string;
}

const sidebarItems: SidebarItem[] = [
  { path: '/dashboard', label: '仪表盘', icon: '📊' },
  { path: '/products', label: '商品管理', icon: '📦' },
  { path: '/inventory', label: '库存管理', icon: '📦' },
  { path: '/orders', label: '订单管理', icon: '📋' },
  { path: '/refunds', label: '退款管理', icon: '💰' },
  { path: '/returns', label: '退货管理', icon: '📦' },
  { path: '/ai-assistant', label: 'AI 商品助手', icon: '🤖' },
];

const MerchantLayout: React.FC = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const [collapsed, setCollapsed] = useState(false);
  const { userInfo, logout } = useAuthStore();

  const handleLogout = () => {
    logout();
    void navigate('/login', { replace: true });
  };

  const isActive = (path: string) => location.pathname.startsWith(path);

  return (
    <div style={{ display: 'flex', minHeight: '100vh' }}>
      {/* Sidebar */}
      <aside
        style={{
          width: collapsed ? 60 : 'var(--sidebar-width)',
          background: 'var(--color-bg-primary)',
          borderRight: '1px solid var(--color-border-light)',
          display: 'flex',
          flexDirection: 'column',
          transition: 'width var(--transition-normal)',
          position: 'fixed',
          top: 0,
          left: 0,
          bottom: 0,
          zIndex: 200,
          overflow: 'hidden',
        }}
      >
        {/* Logo */}
        <div
          style={{
            height: 'var(--header-height)',
            display: 'flex',
            alignItems: 'center',
            padding: collapsed ? '0 12px' : '0 var(--spacing-lg)',
            borderBottom: '1px solid var(--color-border-light)',
            cursor: 'pointer',
          }}
          onClick={() => navigate('/dashboard')}
        >
          <span style={{ fontSize: collapsed ? 18 : 20, fontWeight: 700, color: 'var(--color-accent)', whiteSpace: 'nowrap' }}>
            {collapsed ? 'A' : 'AI Commerce'}
          </span>
        </div>

        {/* Nav */}
        <nav style={{ flex: 1, padding: 'var(--spacing-sm)', overflow: 'auto' }}>
          {sidebarItems.map((item) => (
            <button
              key={item.path}
              onClick={() => navigate(item.path)}
              style={{
                display: 'flex',
                alignItems: 'center',
                gap: 10,
                width: '100%',
                padding: collapsed ? '10px 14px' : '10px 14px',
                borderRadius: 'var(--radius-sm)',
                border: 'none',
                background: isActive(item.path) ? 'var(--color-accent-light)' : 'transparent',
                color: isActive(item.path) ? 'var(--color-accent)' : 'var(--color-text-secondary)',
                fontSize: '14px',
                cursor: 'pointer',
                marginBottom: 2,
                textAlign: 'left',
                transition: 'all var(--transition-fast)',
                whiteSpace: 'nowrap',
                justifyContent: collapsed ? 'center' : 'flex-start',
              }}
              onMouseEnter={(e) => {
                if (!isActive(item.path)) (e.currentTarget as HTMLElement).style.background = 'var(--color-bg-secondary)';
              }}
              onMouseLeave={(e) => {
                if (!isActive(item.path)) (e.currentTarget as HTMLElement).style.background = 'transparent';
              }}
            >
              <span style={{ fontSize: '18px' }}>{item.icon}</span>
              {!collapsed && <span>{item.label}</span>}
            </button>
          ))}
        </nav>

        {/* Collapse button */}
        <div style={{ padding: 'var(--spacing-sm)', borderTop: '1px solid var(--color-border-light)' }}>
          <button
            onClick={() => setCollapsed(!collapsed)}
            style={{
              width: '100%',
              padding: '8px',
              borderRadius: 'var(--radius-sm)',
              border: 'none',
              background: 'transparent',
              color: 'var(--color-text-tertiary)',
              cursor: 'pointer',
              fontSize: '16px',
            }}
          >
            {collapsed ? '→' : '←'}
          </button>
        </div>
      </aside>

      {/* Main area */}
      <div
        style={{
          flex: 1,
          marginLeft: collapsed ? 60 : 'var(--sidebar-width)',
          transition: 'margin-left var(--transition-normal)',
          display: 'flex',
          flexDirection: 'column',
          minHeight: '100vh',
        }}
      >
        {/* Top bar */}
        <header
          style={{
            height: 'var(--header-height)',
            background: 'var(--color-bg-primary)',
            borderBottom: '1px solid var(--color-border-light)',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
            padding: '0 var(--spacing-xl)',
            position: 'sticky',
            top: 0,
            zIndex: 100,
          }}
        >
          <span style={{ fontSize: '15px', fontWeight: 500, color: 'var(--color-text-primary)' }}>
            商家管理后台
          </span>
          <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--spacing-md)' }}>
            <button style={{ padding: 6, background: 'none', border: 'none', cursor: 'pointer', fontSize: '16px' }}>🔔</button>
            <div style={{ width: 32, height: 32, borderRadius: '50%', background: 'var(--color-accent-light)', display: 'flex', alignItems: 'center', justifyContent: 'center', color: 'var(--color-accent)', fontSize: '14px', fontWeight: 600 }}>
              {(userInfo?.nickname || userInfo?.username || 'M').charAt(0).toUpperCase()}
            </div>
            <button
              onClick={handleLogout}
              style={{ padding: '6px 14px', borderRadius: 'var(--radius-sm)', border: '1px solid var(--color-border)', background: 'transparent', color: 'var(--color-text-secondary)', fontSize: '13px', cursor: 'pointer' }}
            >
              退出登录
            </button>
          </div>
        </header>

        {/* Content */}
        <main style={{ flex: 1, padding: 'var(--spacing-xl)' }}>
          <Outlet />
        </main>
      </div>
    </div>
  );
};

export default MerchantLayout;