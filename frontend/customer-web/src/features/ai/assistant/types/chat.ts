export interface ChatPanelProps {
  onSendMessage?: (message: string) => void;
  onProductClick?: (productId: string) => void;
  placeholder?: string;
}

export interface QuickAction {
  id: string;
  label: string;
  icon: string;
  prompt: string;
}

export const defaultQuickActions: QuickAction[] = [
  { id: 'hot', label: '热门推荐', icon: '🔥', prompt: '推荐几款当前热销的商品' },
  { id: 'gift', label: '挑选礼物', icon: '🎁', prompt: '帮我挑选一份礼物' },
  { id: 'deal', label: '优惠活动', icon: '💰', prompt: '最近有什么优惠活动吗' },
  { id: 'compare', label: '商品对比', icon: '⚖️', prompt: '帮我对比几款商品' },
];