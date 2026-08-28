import { describe, expect, it } from 'vitest';
import { toFrontendStatus } from './orderStatus';

describe('订单状态映射', () => {
  it('应将已完成退款映射为已退款，而不是退款中', () => {
    expect(toFrontendStatus('REFUNDED')).toBe('refunded');
  });

  it('应保留退款处理中的状态', () => {
    expect(toFrontendStatus('REFUNDING')).toBe('refunding');
  });
});
