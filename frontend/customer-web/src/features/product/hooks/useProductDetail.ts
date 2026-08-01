import { useState, useCallback } from 'react';
import type { Product } from '../types/product';

interface UseProductDetailReturn {
  product: Product | null;
  loading: boolean;
  error: string | null;
  selectedSpecs: Record<string, string>;
  quantity: number;
  setSelectedSpec: (groupName: string, optionValue: string) => void;
  setQuantity: (qty: number) => void;
  fetchProduct: (productId: string) => Promise<void>;
  refresh: () => void;
}

export function useProductDetail(): UseProductDetailReturn {
  const [product, setProduct] = useState<Product | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [selectedSpecs, setSelectedSpecs] = useState<Record<string, string>>({});
  const [quantity, setQuantity] = useState(1);
  const [currentProductId, setCurrentProductId] = useState<string | null>(null);

  const setSelectedSpec = useCallback((groupName: string, optionValue: string) => {
    setSelectedSpecs((prev) => ({ ...prev, [groupName]: optionValue }));
  }, []);

  const fetchProduct = useCallback(async (productId: string) => {
    setLoading(true);
    setError(null);
    setCurrentProductId(productId);
    try {
      // TODO: Implement actual API call
      // const result = await productApi.getProductById(productId);
      // setProduct(result);
      // Note: setProduct is intentionally left for future implementation
      void setProduct;
      await Promise.resolve();
    } catch (err) {
      setError(err instanceof Error ? err.message : '获取商品详情失败');
    } finally {
      setLoading(false);
    }
  }, []);

  const refresh = useCallback(() => {
    if (currentProductId) {
      void fetchProduct(currentProductId);
    }
  }, [currentProductId, fetchProduct]);

  return {
    product,
    loading,
    error,
    selectedSpecs,
    quantity,
    setSelectedSpec,
    setQuantity,
    fetchProduct,
    refresh,
  };
}