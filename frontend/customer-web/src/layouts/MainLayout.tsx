import React, { useState } from 'react';
import { Outlet, useNavigate, useLocation } from 'react-router-dom';
import useAuthStore from '../stores/authStore';

const MainLayout: React.FC = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const { userInfo, logout } = useAuthStore();
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);
  const [headerSearchKeyword, setHeaderSearchKeyword] = useState('');
  const [footerTopic, setFooterTopic] = useState<'about' | 'help' | 'privacy' | 'terms' | null>(null);

  const footerContent = {
    about: { title: '关于我们', body: 'AI Commerce Platform 是一个面向真实交易流程的 AI 全栈电商演示平台。' },
    help: { title: '帮助中心', body: '你可以通过商品搜索、购物车、订单、退款退货和 AI 助手完成完整购物流程。遇到登录失效时，请重新登录后再试。' },
    privacy: { title: '隐私政策', body: '平台仅在完成登录、订单和售后服务所必需的范围内处理账户与交易信息，不会在 AI 请求中记录 JWT、内部令牌或完整用户提示词。' },
    terms: { title: '用户协议', body: '请在提交订单、支付和申请售后前确认商品及订单信息。涉及加购等写操作时，AI 助手会要求你进行二次确认。' },
  } as const;

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
            value={headerSearchKeyword}
            onChange={(event) => setHeaderSearchKeyword(event.target.value)}
            onKeyDown={(event) => {
              if (event.key === 'Enter' && headerSearchKeyword.trim()) {
                void navigate(`/search?q=${encodeURIComponent(headerSearchKeyword.trim())}`);
              }
            }}
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
          {([
            ['about', '关于我们'],
            ['help', '帮助中心'],
            ['privacy', '隐私政策'],
            ['terms', '用户协议'],
          ] as const).map(([topic, label]) => (
            <button
              key={topic}
              type="button"
              onClick={() => setFooterTopic(topic)}
              style={{ margin: '0 12px', padding: 0, border: 'none', background: 'none', color: 'inherit', cursor: 'pointer', fontSize: 'inherit' }}
            >
              {label}
            </button>
          ))}
        </div>
        <div>© {new Date().getFullYear()} AI Commerce Platform. All rights reserved.</div>
      </footer>

      {footerTopic && (
        <div
          role="presentation"
          onClick={() => setFooterTopic(null)}
          style={{ position: 'fixed', inset: 0, zIndex: 400, display: 'flex', alignItems: 'center', justifyContent: 'center', padding: 24, background: 'rgba(0,0,0,0.35)' }}
        >
          <section
            role="dialog"
            aria-modal="true"
            aria-labelledby="footer-topic-title"
            onClick={(event) => event.stopPropagation()}
            style={{ width: 'min(480px, 100%)', borderRadius: 'var(--radius-md)', background: 'var(--color-bg-primary)', padding: 'var(--spacing-xl)', boxShadow: 'var(--shadow-lg)' }}
          >
            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', gap: 16, marginBottom: 'var(--spacing-md)' }}>
              <h2 id="footer-topic-title" style={{ margin: 0, fontSize: '20px', color: 'var(--color-text-primary)' }}>{footerContent[footerTopic].title}</h2>
              <button type="button" aria-label="关闭" onClick={() => setFooterTopic(null)} style={{ border: 'none', background: 'none', cursor: 'pointer', fontSize: 20, color: 'var(--color-text-tertiary)' }}>✕</button>
            </div>
            <p style={{ margin: 0, lineHeight: 1.8, color: 'var(--color-text-secondary)' }}>{footerContent[footerTopic].body}</p>
          </section>
        </div>
      )}

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
