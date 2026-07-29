import request from './request';

export interface InventoryVO {
  id: number;
  productSkuId: number;
  availableStock: number;
  reservedStock: number;
  totalStock: number;
  lowStock: boolean;
}

export interface InventoryDetailVO {
  id: number;
  productSkuId: number;
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

export const inventoryApi = {
  list: (params?: { page?: number; pageSize?: number }) =>
    request.get<PageResponse<InventoryVO>>('/merchant/inventory', { params }),

  getDetail: (id: number) =>
    request.get<InventoryDetailVO>(`/merchant/inventory/${id}`),

  adjust: (id: number, data: InventoryAdjustRequest) =>
    request.post(`/merchant/inventory/${id}/adjust`, data),

  movements: (id: number, params?: { page?: number; pageSize?: number }) =>
    request.get<PageResponse<InventoryMovementVO>>(`/merchant/inventory/${id}/movements`, { params }),
};