import React, { useCallback, useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { orderApi, type OrderVO } from '../../api/orders';

const labels: Record<string, string> = { PENDING_PAYMENT: '待付款', PAID: '已支付', PROCESSING: '处理中', SHIPPED: '已发货', COMPLETED: '已完成', CANCELLED: '已取消', REFUNDING: '退款中', REFUNDED: '已退款', CLOSED: '已关闭' };

const OrderDetailPage: React.FC = () => {
  const { orderNo = '' } = useParams<{ orderNo: string }>();
  const navigate = useNavigate();
  const [order, setOrder] = useState<OrderVO | null>(null);
  const [loading, setLoading] = useState(true);

  const load = useCallback(async () => {
    setLoading(true);
    try { setOrder((await orderApi.getDetail(orderNo)).data); }
    catch (error) { console.error('加载订单详情失败:', error); }
    finally { setLoading(false); }
  }, [orderNo]);
  useEffect(() => { void load(); }, [load]);

  const action = async (fn: () => Promise<unknown>, success: string) => {
    try { await fn(); alert(success); await load(); }
    catch (error) { console.error(success, error); alert('操作失败，请检查订单状态'); }
  };

  if (loading) return <div style={{ padding: 40, textAlign: 'center' }}>加载中...</div>;
  if (!order) return <div style={{ padding: 40, textAlign: 'center' }}>订单不存在</div>;
  return <div style={{ maxWidth: 900, margin: '0 auto' }}>
    <button onClick={() => navigate('/orders')} style={buttonStyle}>← 返回订单</button>
    <h1 style={{ fontSize: 'var(--font-size-h1)', margin: '18px 0' }}>订单详情</h1>
    <section style={cardStyle}>
      <p><b>订单号：</b>{order.orderNo}</p><p><b>状态：</b>{labels[order.orderStatus] || order.orderStatus}</p>
      <p><b>买家：</b>#{order.buyerId} / <b>商家：</b>#{order.merchantId}</p>
      <p><b>实付金额：</b><strong style={{ color: 'var(--color-accent)' }}>¥{Number(order.payAmount || 0).toFixed(2)}</strong></p>
      <p><b>物流：</b>{order.logisticsCompany || '-'} / {order.trackingNo || '-'}</p>
      <p><b>创建时间：</b>{order.createdTime ? new Date(order.createdTime).toLocaleString('zh-CN') : '-'}</p>
      <h2 style={{ fontSize: 16 }}>商品明细</h2>
      {(order.items || []).map((item, index) => <div key={index} style={rowStyle}><span>{item.productName || '商品'} × {item.quantity || 1}</span><span>¥{(Number(item.price || 0) * Number(item.quantity || 1)).toFixed(2)}</span></div>)}
      <div style={{ display: 'flex', gap: 8, marginTop: 20 }}>
        {order.orderStatus === 'CANCELLED' && <button onClick={() => void action(() => orderApi.close(order.orderNo, '管理员关闭订单'), '订单已关闭')} style={buttonStyle}>关闭订单</button>}
        {['PENDING_PAYMENT', 'PAID', 'PROCESSING'].includes(order.orderStatus) && <button onClick={() => window.confirm('确定强制取消订单吗？') && void action(() => orderApi.cancel(order.orderNo, '管理员强制取消'), '订单已取消')} style={dangerButtonStyle}>强制取消</button>}
      </div>
    </section>
  </div>;
};

const cardStyle: React.CSSProperties = { background: 'var(--color-bg-primary)', borderRadius: 10, boxShadow: 'var(--shadow-sm)', padding: 24 };
const rowStyle: React.CSSProperties = { display: 'flex', justifyContent: 'space-between', padding: '10px 0', borderTop: '1px solid var(--color-border-light)', fontSize: 14 };
const buttonStyle: React.CSSProperties = { padding: '8px 16px', border: '1px solid var(--color-border)', borderRadius: 6, background: 'transparent', cursor: 'pointer' };
const dangerButtonStyle: React.CSSProperties = { ...buttonStyle, border: 'none', background: '#ff3b30', color: '#fff' };
export default OrderDetailPage;
