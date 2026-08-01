import React, { useState } from 'react';
import { Outlet, useNavigate, useLocation } from 'react-router-dom';

interface SidebarItem { path: string; label: string; icon: string; }

const sidebarItems: SidebarItem[] = [
  { path: '/dashboard', label: '仪表盘', icon: '📊' },
  { path: '/users', label: '用户管理', icon: '👤' },
  { path: '/merchants', label: '商家管理', icon: '🏪' },
  { path: '/products', label: '商品审核', icon: '📋' },
  { path: '/orders', label: '订单监控', icon: '📦' },
  { path: '/ai-center', label: 'AI中心', icon: '🤖' },
  { path: '/audit-log', label: '审计日志', icon: '📝' },
  { path: '/settings', label: '系统设置', icon: '⚙️' },
];

const AdminLayout: React.FC = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const [collapsed, setCollapsed] = useState(false);
  const isActive = (path: string) => location.pathname.startsWith(path);

  return (
    <div style={{ display: 'flex', minHeight: '100vh' }}>
      <aside style={{
        width: collapsed ? 60 : 'var(--sidebar-width)', background: 'var(--color-bg-primary)',
        borderRight: '1px solid var(--color-border-light)', display: 'flex', flexDirection: 'column',
        transition: 'width var(--transition-normal)', position: 'fixed', top: 0, left: 0, bottom: 0, zIndex: 200, overflow: 'hidden',
      }}>
        <div style={{ height: 'var(--header-height)', display: 'flex', alignItems: 'center', padding: collapsed ? '0 12px' : '0 var(--spacing-lg)', borderBottom: '1px solid var(--color-border-light)', cursor: 'pointer' }}
          onClick={() => navigate('/dashboard')}>
          <span style={{ fontSize: collapsed ? 18 : 20, fontWeight: 700, color: 'var(--color-accent)' }}>{collapsed ? 'A' : 'Admin'}</span>
        </div>
        <nav style={{ flex: 1, padding: 'var(--spacing-sm)', overflow: 'auto' }}>
          {sidebarItems.map((item) => (
            <button key={item.path} onClick={() => navigate(item.path)} style={{
              display: 'flex', alignItems: 'center', gap: 10, width: '100%', padding: collapsed ? '10px 14px' : '10px 14px',
              borderRadius: 'var(--radius-sm)', border: 'none',
              background: isActive(item.path) ? 'var(--color-accent-light)' : 'transparent',
              color: isActive(item.path) ? 'var(--color-accent)' : 'var(--color-text-secondary)',
              fontSize: '14px', cursor: 'pointer', marginBottom: 2, textAlign: 'left', transition: 'all var(--transition-fast)',
              whiteSpace: 'nowrap', justifyContent: collapsed ? 'center' : 'flex-start',
            }}>
              <span style={{ fontSize: '18px' }}>{item.icon}</span>
              {!collapsed && <span>{item.label}</span>}
            </button>
          ))}
        </nav>
        <div style={{ padding: 'var(--spacing-sm)', borderTop: '1px solid var(--color-border-light)' }}>
          <button onClick={() => setCollapsed(!collapsed)} style={{ width: '100%', padding: '8px', borderRadius: 'var(--radius-sm)', border: 'none', background: 'transparent', color: 'var(--color-text-tertiary)', cursor: 'pointer', fontSize: '16px' }}>
            {collapsed ? '→' : '←'}
          </button>
        </div>
      </aside>
      <div style={{ flex: 1, marginLeft: collapsed ? 60 : 'var(--sidebar-width)', transition: 'margin-left var(--transition-normal)', display: 'flex', flexDirection: 'column', minHeight: '100vh' }}>
        <header style={{ height: 'var(--header-height)', background: 'var(--color-bg-primary)', borderBottom: '1px solid var(--color-border-light)', display: 'flex', alignItems: 'center', justifyContent: 'space-between', padding: '0 var(--spacing-xl)', position: 'sticky', top: 0, zIndex: 100 }}>
          <span style={{ fontSize: '15px', fontWeight: 500, color: 'var(--color-text-primary)' }}>平台管理控制台</span>
          <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--spacing-md)' }}>
            <span style={{ fontSize: '13px', color: 'var(--color-text-secondary)' }}>管理员</span>
            <div style={{ width: 32, height: 32, borderRadius: '50%', background: 'var(--color-accent-light)', display: 'flex', alignItems: 'center', justifyContent: 'center', color: 'var(--color-accent)', fontSize: '14px', fontWeight: 600 }}>A</div>
          </div>
        </header>
        <main style={{ flex: 1, padding: 'var(--spacing-xl)' }}><Outlet /></main>
      </div>
    </div>
  );
};
export default AdminLayout;