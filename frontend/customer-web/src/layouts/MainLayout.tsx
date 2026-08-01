import React, { useState } from 'react';
import { Outlet, useNavigate, useLocation } from 'react-router-dom';
import useAuthStore from '../stores/authStore';

const MainLayout: React.FC = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const { userInfo, logout } = useAuthStore();
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);

  const navItems = [
    { path: '/', label: '首页' },
    { path: '/products', label: '商品' },
    { path: '/categories', label: '分类' },
    { path: '/ai', label: 'AI' },
  ];

  const isActive = (path: string) => {
    if (path === '/') return location.pathname === '/';
    return location.pathname.startsWith(path);
  };

  const handleLogout = () => {
    logout();
    void navigate('/login', { replace: true });
  };

  return (
    <div style={{ minHeight: '100vh', display: 'flex', flexDirection: 'column' }}>
      {/* Header */}
      <header
        style={{
          position: 'sticky',
          top: 0,
          zIndex: 200,
          height: 'var(--header-height)',
          background: 'rgba(255,255,255,0.85)',
          backdropFilter: 'blur(20px)',
          WebkitBackdropFilter: 'blur(20px)',
          borderBottom: '1px solid var(--color-border-light)',
          display: 'flex',
          alignItems: 'center',
          padding: '0 var(--spacing-lg)',
        }}
      >
        {/* Logo */}
        <div
          onClick={() => navigate('/')}
          style={{
            fontWeight: 700,
            fontSize: '18px',
            color: 'var(--color-text-primary)',
            cursor: 'pointer',
            marginRight: 'var(--spacing-xl)',
            whiteSpace: 'nowrap',
          }}
        >
          AI Commerce
        </div>

        {/* Desktop Nav */}
        <nav
          style={{
            display: 'none',
            gap: 'var(--spacing-lg)',
            flex: 1,
          }}
          className="desktop-nav"
        >
          {navItems.map((item) => (
            <button
              key={item.path}
              onClick={() => navigate(item.path)}
              style={{
                fontSize: '14px',
                fontWeight: isActive(item.path) ? 600 : 400,
                color: isActive(item.path) ? 'var(--color-accent)' : 'var(--color-text-primary)',
                background: 'none',
                border: 'none',
                cursor: 'pointer',
                padding: '8px 4px',
                transition: 'color var(--transition-fast)',
              }}
            >
              {item.label}
            </button>
          ))}
        </nav>

        {/* Search */}
        <div style={{ flex: 1, maxWidth: 360, margin: '0 auto', display: 'none' }} className="desktop-search">
          <input
            placeholder="搜索商品..."
            onFocus={() => navigate('/search')}
            style={{
              width: '100%',
              height: 36,
              borderRadius: 'var(--radius-sm)',
              border: '1px solid var(--color-border-light)',
              background: 'var(--color-bg-secondary)',
              padding: '0 12px',
              fontSize: '14px',
              outline: 'none',
            }}
          />
        </div>

        {/* Actions */}
        <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--spacing-sm)', marginLeft: 'auto' }}>
          <button onClick={() => navigate('/cart')} style={{ padding: '8px', background: 'none', border: 'none', cursor: 'pointer', fontSize: '16px' }}>
            🛒
          </button>
          <button onClick={() => navigate('/profile')} style={{ padding: '8px', background: 'none', border: 'none', cursor: 'pointer', fontSize: '16px' }}>
            👤
          </button>
          {userInfo && (
            <button
              onClick={handleLogout}
              style={{
                padding: '6px 12px',
                fontSize: '13px',
                color: 'var(--color-text-secondary)',
                background: 'none',
                border: '1px solid var(--color-border)',
                borderRadius: 'var(--radius-sm)',
                cursor: 'pointer',
              }}
            >
              退出
            </button>
          )}
        </div>

        {/* Mobile menu toggle */}
        <button
          onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
          style={{ padding: '8px', background: 'none', border: 'none', cursor: 'pointer', fontSize: '20px', display: 'none' }}
          className="mobile-menu-toggle"
        >
          ☰
        </button>
      </header>

      {/* Mobile Nav */}
      {mobileMenuOpen && (
        <div style={{ background: 'var(--color-bg-primary)', borderBottom: '1px solid var(--color-border-light)', padding: 'var(--spacing-md)' }}>
          {navItems.map((item) => (
            <button
              key={item.path}
              onClick={() => { void navigate(item.path); setMobileMenuOpen(false); }}
              style={{
                display: 'block',
                width: '100%',
                padding: '12px 16px',
                textAlign: 'left',
                fontSize: '16px',
                fontWeight: isActive(item.path) ? 600 : 400,
                color: isActive(item.path) ? 'var(--color-accent)' : 'var(--color-text-primary)',
                background: 'none',
                border: 'none',
                cursor: 'pointer',
              }}
            >
              {item.label}
            </button>
          ))}
        </div>
      )}

      {/* Content */}
      <main style={{ flex: 1 }}>
        <Outlet />
      </main>

      {/* Footer */}
      <footer
        style={{
          background: 'var(--color-bg-secondary)',
          padding: 'var(--spacing-xl) var(--spacing-lg)',
          textAlign: 'center',
          fontSize: '13px',
          color: 'var(--color-text-secondary)',
          borderTop: '1px solid var(--color-border-light)',
        }}
      >
        <div style={{ marginBottom: 'var(--spacing-sm)' }}>
          <span style={{ margin: '0 12px', cursor: 'pointer' }}>关于我们</span>
          <span style={{ margin: '0 12px', cursor: 'pointer' }}>帮助中心</span>
          <span style={{ margin: '0 12px', cursor: 'pointer' }}>隐私政策</span>
          <span style={{ margin: '0 12px', cursor: 'pointer' }}>用户协议</span>
        </div>
        <div>© {new Date().getFullYear()} AI Commerce Platform. All rights reserved.</div>
      </footer>

      {/* AI Assistant入口占位 */}
      <button
        onClick={() => navigate('/ai')}
        style={{
          position: 'fixed',
          bottom: 24,
          right: 24,
          width: 52,
          height: 52,
          borderRadius: '50%',
          background: 'linear-gradient(135deg, #0071E3, #5AC8FA)',
          color: '#fff',
          fontSize: '20px',
          border: 'none',
          cursor: 'pointer',
          boxShadow: '0 4px 16px rgba(0,113,227,0.3)',
          zIndex: 300,
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          transition: 'transform var(--transition-fast), box-shadow var(--transition-fast)',
        }}
        onMouseEnter={(e) => {
          (e.target as HTMLElement).style.transform = 'scale(1.1)';
          (e.target as HTMLElement).style.boxShadow = '0 6px 24px rgba(0,113,227,0.4)';
        }}
        onMouseLeave={(e) => {
          (e.target as HTMLElement).style.transform = 'scale(1)';
          (e.target as HTMLElement).style.boxShadow = '0 4px 16px rgba(0,113,227,0.3)';
        }}
      >
        AI
      </button>

      {/* Bottom Nav for mobile */}
      <nav
        style={{
          display: 'none',
          position: 'fixed',
          bottom: 0,
          left: 0,
          right: 0,
          height: 'var(--bottom-nav-height)',
          background: 'rgba(255,255,255,0.95)',
          backdropFilter: 'blur(10px)',
          borderTop: '1px solid var(--color-border-light)',
          zIndex: 200,
          justifyContent: 'space-around',
          alignItems: 'center',
        }}
        className="bottom-nav"
      >
        {[
          { path: '/', label: '首页', icon: '🏠' },
          { path: '/products', label: '商品', icon: '🛍️' },
          { path: '/cart', label: '购物车', icon: '🛒' },
          { path: '/profile', label: '我的', icon: '👤' },
        ].map((item) => (
          <button
            key={item.path}
            onClick={() => navigate(item.path)}
            style={{
              display: 'flex',
              flexDirection: 'column',
              alignItems: 'center',
              gap: 2,
              padding: '4px 12px',
              background: 'none',
              border: 'none',
              cursor: 'pointer',
              fontSize: '10px',
              color: isActive(item.path) ? 'var(--color-accent)' : 'var(--color-text-secondary)',
            }}
          >
            <span style={{ fontSize: '20px' }}>{item.icon}</span>
            <span>{item.label}</span>
          </button>
        ))}
      </nav>

      {/* Responsive CSS injected via style tag */}
      <style>{`
        @media (min-width: 768px) {
          .desktop-nav { display: flex !important; }
          .desktop-search { display: block !important; }
          .mobile-menu-toggle { display: none !important; }
          .bottom-nav { display: none !important; }
        }
        @media (max-width: 767px) {
          .desktop-nav { display: none !important; }
          .desktop-search { display: none !important; }
          .mobile-menu-toggle { display: block !important; }
          .bottom-nav { display: flex !important; }
          main { padding-bottom: var(--bottom-nav-height); }
        }
      `}</style>
    </div>
  );
};

export default MainLayout;