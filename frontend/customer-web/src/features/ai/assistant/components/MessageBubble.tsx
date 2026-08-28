import React from 'react';
import type { AIMessage } from '../../../../services/ai/aiTypes';
import type { Product } from '../../../product/types/product';
import ProductCard from '../../../product/components/ProductCard';

interface MessageBubbleProps {
  message: AIMessage;
  onAddToCart?: (skuId: number, productName: string) => void;
  onConfirmAddToCart?: (skuId: number, quantity: number) => void;
}

const MessageBubble: React.FC<MessageBubbleProps> = ({ message, onAddToCart, onConfirmAddToCart }) => {
  const isUser = message.role === 'user';
  const searchResult = message.metadata?.productSearch;
  const now = message.createdAt;
  const products: Product[] = searchResult?.products.map((product) => ({
    id: String(product.id),
    name: product.productName,
    description: product.description ?? '',
    brand: product.brand ?? undefined,
    categoryId: product.categoryId ? String(product.categoryId) : '',
    categoryName: product.categoryName ?? undefined,
    images: [],
    thumbnail: product.coverImage ?? '',
    price: product.minPrice ?? 0,
    currency: 'CNY',
    rating: 0,
    reviewCount: 0,
    salesCount: product.salesCount ?? 0,
    stock: 0,
    status: 'ACTIVE',
    createdAt: product.createdTime ?? now,
    updatedAt: now,
  })) ?? [];

  return (
    <div
      style={{
        display: 'flex',
        justifyContent: isUser ? 'flex-end' : 'flex-start',
        marginBottom: 'var(--spacing-md)',
      }}
    >
      {!isUser && (
        <div
          style={{
            width: 32,
            height: 32,
            borderRadius: '50%',
            background: 'linear-gradient(135deg, #0071E3, #5AC8FA)',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            color: '#fff',
            fontSize: '12px',
            fontWeight: 700,
            marginRight: 8,
            flexShrink: 0,
            marginTop: 4,
          }}
        >
          AI
        </div>
      )}
      <div
        style={{
          maxWidth: isUser ? '75%' : 'min(90%, 680px)',
          padding: '10px 14px',
          borderRadius: isUser ? '16px 4px 16px 16px' : '4px 16px 16px 16px',
          background: isUser ? 'var(--color-accent)' : 'var(--color-bg-secondary)',
          color: isUser ? '#fff' : 'var(--color-text-primary)',
          fontSize: '14px',
          lineHeight: 1.5,
          wordBreak: 'break-word',
        }}
      >
        {searchResult && (
          <div style={{ marginBottom: message.content ? 10 : 0 }}>
            {searchResult.error ? (
              <p style={{ color: 'var(--color-error)', fontSize: 13 }}>{searchResult.error}</p>
            ) : products.length > 0 ? (
              <>
                <p style={{ fontSize: 13, color: 'var(--color-text-secondary)', marginBottom: 8 }}>
                  为你找到 {searchResult.total} 件商品
                </p>
                <div
                  style={{
                    display: 'grid',
                    gridTemplateColumns: 'repeat(auto-fit, minmax(120px, 1fr))',
                    gap: 8,
                  }}
                >
                  {products.map((product, index) => {
                    const source = searchResult.products[index];
                    return (
                      <div key={product.id}>
                        <ProductCard product={product} variant="compact" />
                        {source.defaultSkuId && onAddToCart && (
                          <button
                            type="button"
                            onClick={() => onAddToCart(source.defaultSkuId as number, source.productName)}
                            style={{
                              width: '100%',
                              marginTop: 6,
                              padding: '6px 8px',
                              border: 'none',
                              borderRadius: 6,
                              background: 'var(--color-accent)',
                              color: '#fff',
                              fontSize: 12,
                              cursor: 'pointer',
                            }}
                          >
                            加入购物车
                          </button>
                        )}
                      </div>
                    );
                  })}
                </div>
              </>
            ) : (
              <p style={{ fontSize: 13, color: 'var(--color-text-secondary)' }}>
                暂未找到符合条件的商品
              </p>
            )}
          </div>
        )}
        {message.content}
        {message.metadata?.addToCart?.type === 'add_to_cart_confirmation'
          && message.metadata.addToCart.skuId
          && message.metadata.addToCart.quantity
          && onConfirmAddToCart && (
            <button
              type="button"
              onClick={() => onConfirmAddToCart(
                message.metadata?.addToCart?.skuId as number,
                message.metadata?.addToCart?.quantity as number,
              )}
              style={{
                display: 'block',
                marginTop: 10,
                padding: '7px 12px',
                border: '1px solid var(--color-accent)',
                borderRadius: 6,
                background: 'transparent',
                color: 'var(--color-accent)',
                fontSize: 12,
                cursor: 'pointer',
              }}
            >
              确认加入购物车
            </button>
          )}
      </div>
      {isUser && (
        <div
          style={{
            width: 32,
            height: 32,
            borderRadius: '50%',
            background: 'var(--color-bg-secondary)',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            fontSize: '14px',
            marginLeft: 8,
            flexShrink: 0,
            marginTop: 4,
          }}
        >
          👤
        </div>
      )}
    </div>
  );
};

export default React.memo(MessageBubble);
