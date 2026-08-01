import { useState, useCallback } from 'react';
import type { Product, ProductFilters, ProductSortBy } from '../types/product';

interface UseProductsReturn {
  products: Product[];
  loading: boolean;
  error: string | null;
  filters: ProductFilters;
  total: number;
  page: number;
  pageSize: number;
  totalPages: number;
  setFilters: (filters: ProductFilters) => void;
  setSortBy: (sortBy: ProductSortBy) => void;
  setPage: (page: number) => void;
  setKeyword: (keyword: string) => void;
  fetchProducts: () => Promise<void>;
  refresh: () => void;
}

export function useProducts(initialFilters?: ProductFilters): UseProductsReturn {
  const [products, setProducts] = useState<Product[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [total, setTotal] = useState(0);
  const [page, setPage] = useState(1);
  const [pageSize] = useState(20);
  const [totalPages, setTotalPages] = useState(0);
  const [filters, setFiltersState] = useState<ProductFilters>(initialFilters ?? {});

  const setFilters = useCallback((newFilters: ProductFilters) => {
    setFiltersState(newFilters);
    setPage(1);
  }, []);

  const setSortBy = useCallback((sortBy: ProductSortBy) => {
    setFiltersState((prev) => ({ ...prev, sortBy }));
  }, []);

  const setKeyword = useCallback((keyword: string) => {
    setFiltersState((prev) => ({ ...prev, keyword }));
    setPage(1);
  }, []);

  const fetchProducts = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const request = { filters, page, pageSize };
      void request;
      // TODO: Implement actual API call
      // const result = await productApi.getProducts({ ...filters, page, pageSize });
      // setProducts(result.items);
      // setTotal(result.total);
      // setTotalPages(result.totalPages);
      void setProducts;
      void setTotal;
      void setTotalPages;
      await Promise.resolve();
    } catch (err) {
      setError(err instanceof Error ? err.message : '获取商品列表失败');
    } finally {
      setLoading(false);
    }
  }, [filters, page, pageSize]);

  const refresh = useCallback(() => {
    void fetchProducts();
  }, [fetchProducts]);

  return {
    products,
    loading,
    error,
    filters,
    total,
    page,
    pageSize,
    totalPages,
    setFilters,
    setSortBy,
    setPage,
    setKeyword,
    fetchProducts,
    refresh,
  };
}