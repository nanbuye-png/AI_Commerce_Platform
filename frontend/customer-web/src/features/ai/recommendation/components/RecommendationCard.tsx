import React from 'react';
import type { AIRecommendation } from '../../../../services/ai/aiTypes';

interface RecommendationCardProps {
  recommendation: AIRecommendation;
  onProductClick?: (productId: string) => void;
}

const typeLabels: Record<string, string> = {
  similar: '相似商品',
  complementary: '搭配推荐',
  trending: '热门推荐',
  personalized: '为你推荐',
  outfit: '搭配方案',
};

const RecommendationCard: React.FC<RecommendationCardProps> = ({ recommendation, onProductClick }) => {
  return (
    <div
      onClick={() => onProductClick?.(recommendation.productId)}
      style={{
        padding: 'var(--spacing-md)',
        borderRadius: 'var(--radius-md)',
        background: 'var(--color-bg-primary)',
        border: '1px solid var(--color-border-light)',
        cursor: onProductClick ? 'pointer' : undefined,
        transition: 'box-shadow var(--transition-fast)',
      }}
      onMouseEnter={(e) => { (e.currentTarget as HTMLElement).style.boxShadow = 'var(--shadow-sm)'; }}
      onMouseLeave={(e) => { (e.currentTarget as HTMLElement).style.boxShadow = 'none'; }}
    >
      <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 8 }}>
        <span style={{
          fontSize: '11px',
          padding: '1px 6px',
          borderRadius: '3px',
          background: 'var(--color-accent-light)',
          color: 'var(--color-accent)',
          fontWeight: 600,
        }}>
          {typeLabels[recommendation.type] || '推荐'}
        </span>
        <span style={{ fontSize: '11px', color: 'var(--color-text-tertiary)' }}>
          匹配度 {Math.round(recommendation.score * 100)}%
        </span>
      </div>
      <p style={{ fontSize: '13px', color: 'var(--color-text-secondary)', lineHeight: 1.5 }}>
        {recommendation.reason}
      </p>
    </div>
  );
};

export default React.memo(RecommendationCard);