import React from 'react';
import type { AIRecommendation } from '../../../../services/ai/aiTypes';

interface AdvisorCardProps {
  recommendation: AIRecommendation;
  onClick?: () => void;
}

const AdvisorCard: React.FC<AdvisorCardProps> = ({ recommendation, onClick }) => {
  return (
    <div
      onClick={onClick}
      style={{
        padding: 'var(--spacing-md)',
        borderRadius: 'var(--radius-md)',
        background: 'var(--color-accent-light)',
        border: '1px solid var(--color-accent-light)',
        cursor: onClick ? 'pointer' : undefined,
        transition: 'box-shadow var(--transition-fast)',
      }}
      onMouseEnter={(e) => { (e.currentTarget as HTMLElement).style.boxShadow = 'var(--shadow-md)'; }}
      onMouseLeave={(e) => { (e.currentTarget as HTMLElement).style.boxShadow = 'none'; }}
    >
      <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 4 }}>
        <span style={{ fontSize: '14px', fontWeight: 600, color: 'var(--color-accent)' }}>
          AI 推荐
        </span>
        <span style={{ fontSize: '11px', color: 'var(--color-text-tertiary)', padding: '1px 6px', borderRadius: '3px', background: 'var(--color-bg-primary)' }}>
          {recommendation.type === 'similar' ? '相似商品' :
           recommendation.type === 'complementary' ? '搭配推荐' :
           recommendation.type === 'trending' ? '热门推荐' :
           recommendation.type === 'personalized' ? '个性化' : '搭配'}
        </span>
      </div>
      <p style={{ fontSize: '13px', color: 'var(--color-text-secondary)', lineHeight: 1.5 }}>
        {recommendation.reason}
      </p>
    </div>
  );
};

export default AdvisorCard;