import React, { useEffect } from 'react';
import { createPortal } from 'react-dom';

interface ModalProps {
  open: boolean;
  onClose: () => void;
  title?: string;
  children: React.ReactNode;
  footer?: React.ReactNode;
  width?: string;
}

const Modal: React.FC<ModalProps> = ({ open, onClose, title, children, footer, width = '480px' }) => {
  useEffect(() => {
    if (open) {
      document.body.style.overflow = 'hidden';
    } else {
      document.body.style.overflow = '';
    }
    return () => {
      document.body.style.overflow = '';
    };
  }, [open]);

  if (!open) return null;

  return createPortal(
    <div
      style={{
        position: 'fixed',
        inset: 0,
        zIndex: 400,
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
      }}
    >
      {/* Backdrop */}
      <div
        onClick={onClose}
        style={{
          position: 'absolute',
          inset: 0,
          background: 'rgba(0, 0, 0, 0.4)',
        }}
      />
      {/* Content */}
      <div
        style={{
          position: 'relative',
          width,
          maxWidth: '90vw',
          maxHeight: '80vh',
          background: 'var(--color-bg-primary)',
          borderRadius: 'var(--radius-xl)',
          boxShadow: 'var(--shadow-lg)',
          display: 'flex',
          flexDirection: 'column',
          animation: 'modalIn 0.3s ease-out',
        }}
      >
        {/* Header */}
        {title && (
          <div
            style={{
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'space-between',
              padding: 'var(--spacing-lg) var(--spacing-xl)',
              borderBottom: '1px solid var(--color-border-light)',
            }}
          >
            <h2 style={{ fontSize: 'var(--font-size-h3)', fontWeight: 600, color: 'var(--color-text-primary)' }}>{title}</h2>
            <button
              onClick={onClose}
              style={{
                width: 32,
                height: 32,
                borderRadius: '50%',
                border: 'none',
                background: 'var(--color-bg-secondary)',
                cursor: 'pointer',
                fontSize: '16px',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                color: 'var(--color-text-secondary)',
              }}
            >
              ✕
            </button>
          </div>
        )}

        {/* Body */}
        <div style={{ padding: 'var(--spacing-xl)', overflow: 'auto', flex: 1 }}>
          {children}
        </div>

        {/* Footer */}
        {footer && (
          <div
            style={{
              display: 'flex',
              justifyContent: 'flex-end',
              gap: 'var(--spacing-sm)',
              padding: 'var(--spacing-md) var(--spacing-xl)',
              borderTop: '1px solid var(--color-border-light)',
            }}
          >
            {footer}
          </div>
        )}
      </div>
      <style>{`
        @keyframes modalIn {
          from { opacity: 0; transform: scale(0.95) translateY(20px); }
          to { opacity: 1; transform: scale(1) translateY(0); }
        }
      `}</style>
    </div>,
    document.body,
  );
};

export default Modal;