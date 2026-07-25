import type { AIRecommendation } from '../../../../services/ai/aiTypes';

export interface RecommendationListProps {
  recommendations: AIRecommendation[];
  title?: string;
  loading?: boolean;
  onProductClick?: (productId: string) => void;
  onRefresh?: () => void;
}

export interface RecommendationCardProps {
  recommendation: AIRecommendation;
  onProductClick?: (productId: string) => void;
}