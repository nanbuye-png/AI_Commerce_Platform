import React, { useState } from 'react';
import type { ProductImage } from '../types/product';

interface ProductGalleryProps {
  images: ProductImage[];
  thumbnail?: string;
}

const ProductGallery: React.FC<ProductGalleryProps> = ({ images, thumbnail }) => {
  const displayImages = images.length > 0 ? images : (thumbnail ? [{ id: 'thumb', url: thumbnail, alt: '' }] : []);
  const [activeIndex, setActiveIndex] = useState(0);
  const [fullscreen, setFullscreen] = useState(false);

  if (displayImages.length === 0) {
    return (
      <div
        style={{
          aspectRatio: '1/1',
          background: 'var(--color-bg-secondary)',
          borderRadius: 'var(--radius-md)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          color: 'var(--color-text-tertiary)',
          fontSize: '14px',
        }}
      >
        暂无图片
      </div>
    );
  }

  const activeImage = displayImages[activeIndex];

  return (
    <>
      {/* Main Image */}
      <div
        onClick={() => setFullscreen(true)}
        style={{
          aspectRatio: '1/1',
          background: 'var(--color-bg-secondary)',
          borderRadius: 'var(--radius-md)',
          overflow: 'hidden',
          cursor: 'pointer',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          marginBottom: 'var(--spacing-sm)',
          position: 'relative',
        }}
      >
        <img
          src={activeImage.url}
          alt={activeImage.alt || '商品图片'}
          style={{
            width: '100%',
            height: '100%',
            objectFit: 'cover',
            transition: 'opacity var(--transition-normal)',
          }}
        />

        {/* Navigation arrows */}
        {displayImages.length > 1 && (
          <>
            <button
              onClick={(e) => { e.stopPropagation(); setActiveIndex((i) => (i === 0 ? displayImages.length - 1 : i - 1)); }}
              style={{
                position: 'absolute',
                left: 8,
                top: '50%',
                transform: 'translateY(-50%)',
                width: 36,
                height: 36,
                borderRadius: '50%',
                background: 'rgba(255,255,255,0.8)',
                border: 'none',
                cursor: 'pointer',
                fontSize: '16px',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
              }}
            >
              ‹
            </button>
            <button
              onClick={(e) => { e.stopPropagation(); setActiveIndex((i) => (i === displayImages.length - 1 ? 0 : i + 1)); }}
              style={{
                position: 'absolute',
                right: 8,
                top: '50%',
                transform: 'translateY(-50%)',
                width: 36,
                height: 36,
                borderRadius: '50%',
                background: 'rgba(255,255,255,0.8)',
                border: 'none',
                cursor: 'pointer',
                fontSize: '16px',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
              }}
            >
              ›
            </button>
          </>
        )}

        {/* Dots */}
        {displayImages.length > 1 && (
          <div style={{ position: 'absolute', bottom: 8, left: '50%', transform: 'translateX(-50%)', display: 'flex', gap: 6 }}>
            {displayImages.map((_, i) => (
              <div
                key={i}
                onClick={(e) => { e.stopPropagation(); setActiveIndex(i); }}
                style={{
                  width: i === activeIndex ? 20 : 6,
                  height: 6,
                  borderRadius: 3,
                  background: i === activeIndex ? 'var(--color-accent)' : 'rgba(255,255,255,0.6)',
                  cursor: 'pointer',
                  transition: 'all var(--transition-fast)',
                }}
              />
            ))}
          </div>
        )}
      </div>

      {/* Thumbnails */}
      {displayImages.length > 1 && (
        <div style={{ display: 'flex', gap: 'var(--spacing-sm)', overflow: 'auto' }}>
          {displayImages.map((img, i) => (
            <div
              key={img.id}
              onClick={() => setActiveIndex(i)}
              style={{
                width: 64,
                height: 64,
                borderRadius: 'var(--radius-sm)',
                overflow: 'hidden',
                cursor: 'pointer',
                border: i === activeIndex ? '2px solid var(--color-accent)' : '2px solid transparent',
                opacity: i === activeIndex ? 1 : 0.6,
                transition: 'all var(--transition-fast)',
                flexShrink: 0,
              }}
            >
              <img src={img.url} alt={img.alt || ''} style={{ width: '100%', height: '100%', objectFit: 'cover' }} />
            </div>
          ))}
        </div>
      )}

      {/* Fullscreen Overlay */}
      {fullscreen && (
        <div
          onClick={() => setFullscreen(false)}
          style={{
            position: 'fixed',
            inset: 0,
            zIndex: 500,
            background: 'rgba(0,0,0,0.9)',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            cursor: 'pointer',
          }}
        >
          <img
            src={activeImage.url}
            alt={activeImage.alt || '商品图片'}
            style={{ maxWidth: '90vw', maxHeight: '90vh', objectFit: 'contain' }}
          />
          <button
            onClick={(e) => { e.stopPropagation(); setFullscreen(false); }}
            style={{
              position: 'absolute',
              top: 20,
              right: 20,
              width: 40,
              height: 40,
              borderRadius: '50%',
              background: 'rgba(255,255,255,0.2)',
              color: '#fff',
              border: 'none',
              cursor: 'pointer',
              fontSize: '20px',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
            }}
          >
            ✕
          </button>
        </div>
      )}
    </>
  );
};

export default React.memo(ProductGallery);