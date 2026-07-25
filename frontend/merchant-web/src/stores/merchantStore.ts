import { create } from 'zustand';

export interface MerchantInfo {
  id: string;
  name: string;
  logo?: string;
  phone: string;
  email: string;
  status: 'active' | 'inactive' | 'suspended';
  createdAt: string;
}

export interface ShopInfo {
  id: string;
  name: string;
  description: string;
  logo?: string;
  banner?: string;
  rating: number;
  productCount: number;
  orderCount: number;
  revenue: number;
}

interface MerchantState {
  merchant: MerchantInfo | null;
  shop: ShopInfo | null;
  setMerchant: (merchant: MerchantInfo) => void;
  setShop: (shop: ShopInfo) => void;
  clear: () => void;
}

const useMerchantStore = create<MerchantState>((set) => ({
  merchant: null,
  shop: null,
  setMerchant: (merchant) => set({ merchant }),
  setShop: (shop) => set({ shop }),
  clear: () => set({ merchant: null, shop: null }),
}));

export default useMerchantStore;