import request from './request';

export interface InventoryVO {
  id: number;
  productSkuId: number;
  skuCode: string;
  productName: string;
  availableStock: number;
  reservedStock: number;
  totalStock: number;
  lowStock: boolean;
}

export interface InventoryDetailVO {
  id: number;
  productSkuId: number;
  skuCode: string;
  productName: string;
  availableStock: number;
  reservedStock: number;
  totalStock: number;
  lowStockThreshold: number;
}

export interface InventoryMovementVO {
  movementNo: string;
  productSkuId: number;
  movementType: string;
  quantity: number;
  beforeAvailable: number;
  afterAvailable: number;
  operatorId?: number;
  remark?: string;
  createdTime: string;
}

export interface InventoryAdjustRequest {
  adjustType: 'INCREASE' | 'DECREASE';
  quantity: number;
  remark?: string;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

interface Result<T> {
  code: number;
  message: string;
  data: T;
}

export const inventoryApi = {
  list: (params?: { page?: number; pageSize?: number; skuCode?: string; productName?: string }) =>
    request.get<Result<PageResponse<InventoryVO>>, Result<PageResponse<InventoryVO>>>('/api/merchant/inventory', { params }),

  getDetail: (id: number) =>
    request.get<Result<InventoryDetailVO>, Result<InventoryDetailVO>>(`/api/merchant/inventory/${id}`),

  adjust: (id: number, data: InventoryAdjustRequest) =>
    request.put<Result<void>, Result<void>>(`/api/merchant/inventory/${id}/adjust`, data),

  inbound: (id: number, data: InventoryAdjustRequest) =>
    request.post<Result<void>, Result<void>>(`/api/merchant/inventory/${id}/inbound`, data),

  movements: (id: number, params?: { page?: number; pageSize?: number }) =>
    request.get<Result<PageResponse<InventoryMovementVO>>, Result<PageResponse<InventoryMovementVO>>>(`/api/merchant/inventory/${id}/movements`, { params }),
};
