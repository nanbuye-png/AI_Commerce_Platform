import React, { useCallback, useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { inventoryApi, type InventoryDetailVO, type InventoryMovementVO } from '../../api/inventory';

const movementTypeLabels: Record<string, string> = {
  INBOUND: '入库',
  OUTBOUND: '出库',
  SALE: '销售扣减',
  REFUND: '退款回补',
  ADJUST_INCREASE: '调增',
  ADJUST_DECREASE: '调减',
  RESERVED: '锁定',
  UNRESERVED: '解锁',
};

const movementTypeColors: Record<string, string> = {
  INBOUND: '#34C759',
  OUTBOUND: '#FF3B30',
  SALE: '#FF3B30',
  REFUND: '#34C759',
  ADJUST_INCREASE: '#34C759',
  ADJUST_DECREASE: '#FF3B30',
  RESERVED: '#FF9F0A',
  UNRESERVED: '#0071E3',
};

const InventoryDetailPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [detail, setDetail] = useState<InventoryDetailVO | null>(null);
  const [movements, setMovements] = useState<InventoryMovementVO[]>([]);
  const [loading, setLoading] = useState(true);
  const [actionLoading, setActionLoading] = useState(false);

  // 调整/入库弹窗状态
  const [showAdjust, setShowAdjust] = useState(false);
  const [adjustType, setAdjustType] = useState<'INCREASE' | 'DECREASE'>('INCREASE');
  const [inboundMode, setInboundMode] = useState(false);
  const [quantity, setQuantity] = useState('');
  const [remark, setRemark] = useState('');

  const loadDetail = useCallback(async () => {
    if (!id) return;
    setLoading(true);
    try {
      const res = await inventoryApi.getDetail(Number(id));
      setDetail(res.data);
      const mvRes = await inventoryApi.movements(Number(id), { page: 1, pageSize: 20 });
      setMovements(mvRes.data?.content ?? []);
    } catch (err) {
      console.error('加载库存详情失败:', err);
    } finally {
      setLoading(false);
    }
  }, [id]);

  useEffect(() => {
    void loadDetail();
  }, [loadDetail]);

  const handleSubmit = async () => {
    const qty = Number(quantity);
    if (!qty || qty <= 0) {
      alert('请输入有效的调整数量');
      return;
    }
    if (!detail) return;
    setActionLoading(true);
    try {
      if (adjustType === 'INCREASE') {
        await inventoryApi.adjust(detail.id, { adjustType: 'INCREASE', quantity: qty, remark: remark || undefined });
      } else {
        await inventoryApi.adjust(detail.id, { adjustType: 'DECREASE', quantity: qty, remark: remark || undefined });
      }
      alert(adjustType === 'INCREASE' ? '库存已调增' : '库存已调减');
      setShowAdjust(false);
      setQuantity('');
      setRemark('');
      await loadDetail();
    } catch (err) {
      console.error('调整库存失败:', err);
      alert('操作失败，请稍后重试');
    } finally {
      setActionLoading(false);
    }
  };

  const handleInbound = async () => {
    const qty = Number(quantity);
    if (!qty || qty <= 0) {
      alert('请输入有效的入库数量');
      return;
    }
    if (!detail) return;
    setActionLoading(true);
    try {
      await inventoryApi.inbound(detail.id, { adjustType: 'INCREASE', quantity: qty, remark: remark || undefined });
      alert('入库成功');
      setShowAdjust(false);
      setQuantity('');
      setRemark('');
      await loadDetail();
    } catch (err) {
      console.error('入库失败:', err);
      alert('操作失败，请稍后重试');
    } finally {
      setActionLoading(false);
    }
  };

  if (loading) {
    return <div style={{ padding: '24px', textAlign: 'center', color: 'var(--color-text-tertiary)' }}>加载中...</div>;
  }

  if (!detail) {
    return <div style={{ padding: '24px', textAlign: 'center', color: 'var(--color-text-tertiary)' }}>库存记录不存在</div>;
  }

  const lowStock = detail.availableStock <= detail.lowStockThreshold;

  return (
    <div style={{ maxWidth: 1000, margin: '0 auto' }}>
      <button
        onClick={() => navigate('/inventory')}
        style={{ marginBottom: 'var(--spacing-md)', padding: '6px 12px', borderRadius: 'var(--radius-sm)', border: '1px solid var(--color-border)', background: 'transparent', color: 'var(--color-text-secondary)', fontSize: '13px', cursor: 'pointer' }}
      >
        ← 返回库存列表
      </button>

      <h1 style={{ fontSize: 'var(--font-size-h1)', fontWeight: 600, marginBottom: 'var(--spacing-lg)' }}>库存详情</h1>

      <div style={{ background: 'var(--color-bg-primary)', borderRadius: 'var(--radius-md)', boxShadow: 'var(--shadow-sm)', padding: 'var(--spacing-xl)', marginBottom: 'var(--spacing-lg)' }}>
        <div style={{ display: 'grid', gridTemplateColumns: '140px 1fr', gap: '12px', fontSize: '14px' }}>
          <div style={{ color: 'var(--color-text-secondary)' }}>商品名称</div>
          <div style={{ color: 'var(--color-text-primary)', fontWeight: 500 }}>{detail.productName}</div>

          <div style={{ color: 'var(--color-text-secondary)' }}>SKU 编码</div>
          <div style={{ color: 'var(--color-text-primary)' }}>{detail.skuCode}</div>

          <div style={{ color: 'var(--color-text-secondary)' }}>可售库存</div>
          <div style={{ fontWeight: 600, color: detail.availableStock > 0 ? 'var(--color-success)' : '#FF3B30' }}>{detail.availableStock}</div>

          <div style={{ color: 'var(--color-text-secondary)' }}>锁定库存</div>
          <div style={{ color: 'var(--color-text-primary)' }}>{detail.reservedStock}</div>

          <div style={{ color: 'var(--color-text-secondary)' }}>总库存</div>
          <div style={{ fontWeight: 600, color: 'var(--color-text-primary)' }}>{detail.totalStock}</div>

          <div style={{ color: 'var(--color-text-secondary)' }}>低库存阈值</div>
          <div style={{ color: 'var(--color-text-primary)' }}>{detail.lowStockThreshold}</div>

          <div style={{ color: 'var(--color-text-secondary)' }}>状态</div>
          <div>
            {lowStock ? (
              <span style={{ fontSize: '12px', padding: '2px 8px', borderRadius: '4px', background: '#FF3B3018', color: '#FF3B30' }}>低库存预警</span>
            ) : (
              <span style={{ fontSize: '12px', padding: '2px 8px', borderRadius: '4px', background: '#34C75918', color: '#34C759' }}>库存充足</span>
            )}
          </div>
        </div>

        <div style={{ marginTop: 'var(--spacing-xl)', display: 'flex', gap: '12px', paddingTop: 'var(--spacing-lg)', borderTop: '1px solid var(--color-border-light)' }}>
          <button
            onClick={() => { setAdjustType('INCREASE'); setInboundMode(false); setShowAdjust(true); }}
            style={{ padding: '8px 20px', borderRadius: 'var(--radius-sm)', border: 'none', background: '#34C759', color: '#fff', fontSize: '14px', cursor: 'pointer', fontWeight: 500 }}
          >
            调增库存
          </button>
          <button
            onClick={() => { setAdjustType('DECREASE'); setInboundMode(false); setShowAdjust(true); }}
            style={{ padding: '8px 20px', borderRadius: 'var(--radius-sm)', border: 'none', background: '#FF9F0A', color: '#fff', fontSize: '14px', cursor: 'pointer', fontWeight: 500 }}
          >
            调减库存
          </button>
          <button
            onClick={() => { setInboundMode(true); setShowAdjust(true); }}
            style={{ padding: '8px 20px', borderRadius: 'var(--radius-sm)', border: '1px solid var(--color-border)', background: 'transparent', color: 'var(--color-text-secondary)', fontSize: '14px', cursor: 'pointer', fontWeight: 500 }}
          >
            入库
          </button>
        </div>
      </div>

      <div style={{ background: 'var(--color-bg-primary)', borderRadius: 'var(--radius-md)', boxShadow: 'var(--shadow-sm)', overflow: 'hidden' }}>
        <div style={{ padding: 'var(--spacing-lg)', borderBottom: '1px solid var(--color-border-light)' }}>
          <h2 style={{ fontSize: '16px', fontWeight: 600, margin: 0 }}>库存流水</h2>
        </div>
        {movements.length === 0 ? (
          <p style={{ padding: 'var(--spacing-xl)', textAlign: 'center', color: 'var(--color-text-tertiary)' }}>暂无库存流水</p>
        ) : (
          <table style={{ width: '100%', borderCollapse: 'collapse' }}>
            <thead>
              <tr style={{ borderBottom: '1px solid var(--color-border-light)', background: 'var(--color-bg-secondary)' }}>
                {['流水号', '类型', '数量', '变动前', '变动后', '备注', '时间'].map((h) => (
                  <th key={h} style={{ padding: '12px 16px', textAlign: 'left', fontSize: '13px', color: 'var(--color-text-secondary)', fontWeight: 500 }}>{h}</th>
                ))}
              </tr>
            </thead>
            <tbody>
              {movements.map((m) => (
                <tr key={m.movementNo} style={{ borderBottom: '1px solid var(--color-border-light)' }}>
                  <td style={{ padding: '12px 16px', fontSize: '13px', color: 'var(--color-text-secondary)' }}>{m.movementNo}</td>
                  <td style={{ padding: '12px 16px' }}>
                    <span style={{ fontSize: '12px', padding: '2px 8px', borderRadius: '4px', background: `${movementTypeColors[m.movementType] || '#86868B'}18`, color: movementTypeColors[m.movementType] || '#86868B' }}>
                      {movementTypeLabels[m.movementType] || m.movementType}
                    </span>
                  </td>
                  <td style={{ padding: '12px 16px', fontSize: '14px', fontWeight: 600, color: m.quantity > 0 ? '#34C759' : '#FF3B30' }}>{m.quantity > 0 ? `+${m.quantity}` : m.quantity}</td>
                  <td style={{ padding: '12px 16px', fontSize: '13px' }}>{m.beforeAvailable}</td>
                  <td style={{ padding: '12px 16px', fontSize: '13px' }}>{m.afterAvailable}</td>
                  <td style={{ padding: '12px 16px', fontSize: '13px', color: 'var(--color-text-secondary)' }}>{m.remark || '-'}</td>
                  <td style={{ padding: '12px 16px', fontSize: '12px', color: 'var(--color-text-tertiary)' }}>{m.createdTime ? new Date(m.createdTime).toLocaleString('zh-CN') : '-'}</td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>

      {showAdjust && (
        <div style={{ position: 'fixed', inset: 0, background: 'rgba(0,0,0,0.4)', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 1000, padding: '20px' }}
          onClick={(e) => { if (e.target === e.currentTarget) setShowAdjust(false); }}
        >
          <div style={{ background: 'var(--color-bg-primary)', borderRadius: 'var(--radius-md)', boxShadow: 'var(--shadow-lg)', padding: 'var(--spacing-xl)', width: '100%', maxWidth: 420 }}>
            <h3 style={{ fontSize: '16px', fontWeight: 600, marginBottom: 'var(--spacing-lg)' }}>
              {inboundMode ? '库存入库' : adjustType === 'INCREASE' ? '调增库存' : '调减库存'}
            </h3>

            <div style={{ marginBottom: 'var(--spacing-md)' }}>
              <label style={{ display: 'block', fontSize: '13px', color: 'var(--color-text-secondary)', marginBottom: 6 }}>数量</label>
              <input
                type="number"
                min={1}
                value={quantity}
                onChange={(e) => setQuantity(e.target.value)}
                placeholder="请输入数量"
                style={{ width: '100%', padding: '8px 12px', borderRadius: 'var(--radius-sm)', border: '1px solid var(--color-border)', fontSize: '14px', boxSizing: 'border-box' }}
              />
            </div>

            <div style={{ marginBottom: 'var(--spacing-lg)' }}>
              <label style={{ display: 'block', fontSize: '13px', color: 'var(--color-text-secondary)', marginBottom: 6 }}>备注（可选）</label>
              <input
                value={remark}
                onChange={(e) => setRemark(e.target.value)}
                placeholder="请输入备注"
                style={{ width: '100%', padding: '8px 12px', borderRadius: 'var(--radius-sm)', border: '1px solid var(--color-border)', fontSize: '14px', boxSizing: 'border-box' }}
              />
            </div>

            <div style={{ display: 'flex', gap: 12, justifyContent: 'flex-end' }}>
              <button
                onClick={() => setShowAdjust(false)}
                style={{ padding: '8px 20px', borderRadius: 'var(--radius-sm)', border: '1px solid var(--color-border)', background: 'transparent', color: 'var(--color-text-secondary)', fontSize: '14px', cursor: 'pointer' }}
              >
                取消
              </button>
              <button
                onClick={inboundMode ? handleInbound : handleSubmit}
                disabled={actionLoading}
                style={{ padding: '8px 24px', borderRadius: 'var(--radius-sm)', border: 'none', background: 'var(--color-accent)', color: '#fff', fontSize: '14px', cursor: 'pointer', fontWeight: 500 }}
              >
                {actionLoading ? '提交中...' : '确认提交'}
              </button>
            </div>
            <p style={{ fontSize: '12px', color: 'var(--color-text-tertiary)', marginTop: 'var(--spacing-md)' }}>
              {inboundMode ? '入库会增加总库存与可售库存。' : '调增/调减直接修改可售库存；如需增加总库存请使用入库功能。'}
            </p>
          </div>
        </div>
      )}
    </div>
  );
};

export default InventoryDetailPage;