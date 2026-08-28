import React, { useCallback, useEffect, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import { orderService, type RefundRecord, type ReturnRecord } from '../../services/order';

const refundStatus: Record<string, string> = { REQUESTED: '待审核', APPROVED: '已批准', PROCESSING: '处理中', COMPLETED: '已完成', REJECTED: '已拒绝', FAILED: '失败' };
const returnStatus: Record<string, string> = { REQUESTED: '待审核', APPROVED: '已批准', RETURNING: '寄回中', RECEIVED: '已收货', COMPLETED: '已完成', REJECTED: '已拒绝', FAILED: '失败' };

const AfterSalesPage: React.FC = () => {
  const [searchParams] = useSearchParams();
  const [refunds, setRefunds] = useState<RefundRecord[]>([]);
  const [returns, setReturns] = useState<ReturnRecord[]>([]);
  const [orderNo, setOrderNo] = useState(searchParams.get('orderNo') || '');
  const [reason, setReason] = useState('OTHER');
  const [shipment, setShipment] = useState<Record<number, { company: string; trackingNo: string }>>({});
  const [loading, setLoading] = useState(true);

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const [refundData, returnData] = await Promise.all([orderService.refunds(), orderService.returns()]);
      setRefunds(refundData || []);
      setReturns(returnData || []);
    } catch (error) {
      console.error('加载售后记录失败:', error);
    } finally { setLoading(false); }
  }, []);

  useEffect(() => {
    void load();

    const refreshWhenVisible = () => {
      if (document.visibilityState === 'visible') void load();
    };
    window.addEventListener('focus', refreshWhenVisible);
    document.addEventListener('visibilitychange', refreshWhenVisible);

    return () => {
      window.removeEventListener('focus', refreshWhenVisible);
      document.removeEventListener('visibilitychange', refreshWhenVisible);
    };
  }, [load]);

  const submit = async (type: 'refund' | 'return') => {
    if (!orderNo.trim()) { alert('请输入订单号'); return; }
    try {
      if (type === 'refund') await orderService.requestRefund(orderNo.trim(), reason);
      else await orderService.requestReturn(orderNo.trim(), reason);
      alert('申请已提交，等待商家审核');
      await load();
    } catch (error) { console.error('提交售后申请失败:', error); alert('提交失败，请检查订单状态'); }
  };

  const submitShipment = async (item: ReturnRecord) => {
    const value = shipment[item.id];
    if (!value?.company.trim() || !value.trackingNo.trim()) { alert('请填写物流公司和运单号'); return; }
    try {
      await orderService.submitReturnShipment(item.id, value.company.trim(), value.trackingNo.trim());
      alert('寄回信息已提交');
      await load();
    } catch (error) { console.error('提交寄回信息失败:', error); alert('提交失败，请稍后重试'); }
  };

  return (
    <div style={{ padding: 'var(--spacing-xl) var(--spacing-lg)', maxWidth: 900, margin: '0 auto' }}>
      <h1 style={{ fontSize: 'var(--font-size-h1)', marginBottom: 20 }}>退款/退货</h1>
      <section style={{ background: 'var(--color-bg-primary)', borderRadius: 10, padding: 20, boxShadow: 'var(--shadow-sm)', marginBottom: 20 }}>
        <h2 style={{ fontSize: 16, marginTop: 0 }}>提交售后申请</h2>
        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 10 }}>
          <input value={orderNo} onChange={(e) => setOrderNo(e.target.value)} placeholder="订单号" style={inputStyle} />
          <select value={reason} onChange={(e) => setReason(e.target.value)} style={inputStyle}>
            <option value="OTHER">其他</option><option value="QUALITY_ISSUE">质量问题</option><option value="DAMAGED">商品损坏</option><option value="WRONG_PRODUCT">商品错误</option>
          </select>
        </div>
        <div style={{ display: 'flex', gap: 10, marginTop: 12 }}>
          <button onClick={() => void submit('refund')} style={primaryButtonStyle}>申请退款</button>
          <button onClick={() => void submit('return')} style={buttonStyle}>申请退货</button>
        </div>
      </section>

      <section style={{ background: 'var(--color-bg-primary)', borderRadius: 10, padding: 20, boxShadow: 'var(--shadow-sm)', marginBottom: 20 }}>
        <h2 style={{ fontSize: 16, marginTop: 0 }}>退款记录</h2>
        {loading ? <p>加载中...</p> : refunds.length === 0 ? <p style={mutedStyle}>暂无退款记录</p> : refunds.map((item) => <div key={item.id} style={rowStyle}><span>订单 #{item.orderId} · ¥{Number(item.amount).toFixed(2)}</span><strong>{refundStatus[item.status] || item.status}</strong></div>)}
      </section>

      <section style={{ background: 'var(--color-bg-primary)', borderRadius: 10, padding: 20, boxShadow: 'var(--shadow-sm)' }}>
        <h2 style={{ fontSize: 16, marginTop: 0 }}>退货记录</h2>
        {loading ? <p>加载中...</p> : returns.length === 0 ? <p style={mutedStyle}>暂无退货记录</p> : returns.map((item) => {
          const value = shipment[item.id] || { company: item.logisticsCompany || '', trackingNo: item.trackingNo || '' };
          return <div key={item.id} style={{ ...rowStyle, flexWrap: 'wrap' }}><span>订单 #{item.orderId}</span><strong>{returnStatus[item.status] || item.status}</strong>{item.status === 'APPROVED' && <div style={{ width: '100%', display: 'flex', gap: 8, marginTop: 10 }}><input value={value.company} onChange={(e) => setShipment({ ...shipment, [item.id]: { ...value, company: e.target.value } })} placeholder="物流公司" style={inputStyle} /><input value={value.trackingNo} onChange={(e) => setShipment({ ...shipment, [item.id]: { ...value, trackingNo: e.target.value } })} placeholder="运单号" style={inputStyle} /><button onClick={() => void submitShipment(item)} style={primaryButtonStyle}>提交寄回</button></div>}</div>;
        })}
      </section>
    </div>
  );
};

const inputStyle: React.CSSProperties = { padding: '9px 10px', border: '1px solid var(--color-border)', borderRadius: 6, minWidth: 0 };
const buttonStyle: React.CSSProperties = { padding: '8px 16px', border: '1px solid var(--color-border)', borderRadius: 6, background: 'transparent', cursor: 'pointer' };
const primaryButtonStyle: React.CSSProperties = { ...buttonStyle, border: 'none', background: 'var(--color-accent)', color: '#fff' };
const rowStyle: React.CSSProperties = { display: 'flex', justifyContent: 'space-between', alignItems: 'center', gap: 12, padding: '12px 0', borderBottom: '1px solid var(--color-border-light)', fontSize: 14 };
const mutedStyle: React.CSSProperties = { color: 'var(--color-text-tertiary)', fontSize: 14 };

export default AfterSalesPage;
