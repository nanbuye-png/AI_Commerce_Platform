import React, { useCallback, useEffect, useState } from 'react';
import { auditLogApi, type AuditLogVO } from '../../api/auditLogs';

const actionLabels: Record<string, string> = {
  APPROVE: '审核通过',
  REJECT: '审核驳回',
  FORCE_OFF_SHELF: '强制下架',
  RESTORE: '恢复上架',
};

const statusLabels: Record<string, string> = {
  DRAFT: '草稿',
  PENDING_REVIEW: '待审核',
  REJECTED: '已驳回',
  ON_SHELF: '已上架',
  OFF_SHELF: '已下架',
};

const AuditLogPage: React.FC = () => {
  const [logs, setLogs] = useState<AuditLogVO[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [page, setPage] = useState(1);
  const [pages, setPages] = useState(0);
  const [total, setTotal] = useState(0);

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const result = await auditLogApi.list({ page, pageSize: 20 });
      setLogs(result.data?.list ?? []);
      setPages(result.data?.pages ?? 0);
      setTotal(result.data?.total ?? 0);
      setError(null);
    } catch (loadError) {
      console.error('加载审计日志失败:', loadError);
      setError('审计日志暂时不可用');
    } finally {
      setLoading(false);
    }
  }, [page]);

  useEffect(() => { void load(); }, [load]);

  return (
    <div style={{ maxWidth: 1200, margin: '0 auto' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 'var(--spacing-lg)' }}>
        <div>
          <h1 style={{ fontSize: 'var(--font-size-h1)', fontWeight: 600, margin: 0 }}>审计日志</h1>
          <p style={{ margin: '6px 0 0', color: 'var(--color-text-tertiary)', fontSize: 13 }}>商品审核与上下架操作记录，共 {total} 条</p>
        </div>
        <button onClick={() => void load()} disabled={loading} style={buttonStyle}>{loading ? '刷新中...' : '刷新'}</button>
      </div>
      {error && <p style={{ color: 'var(--color-error)', marginBottom: 12 }}>{error}</p>}
      <div style={{ background: 'var(--color-bg-primary)', borderRadius: 'var(--radius-md)', boxShadow: 'var(--shadow-sm)', overflow: 'hidden' }}>
        <table style={{ width: '100%', borderCollapse: 'collapse' }}>
          <thead><tr style={{ borderBottom: '1px solid var(--color-border-light)', background: 'var(--color-bg-secondary)' }}>
            {['操作人', '操作类型', '目标商品', '状态变化', '备注', '时间'].map((heading) => <th key={heading} style={headStyle}>{heading}</th>)}
          </tr></thead>
          <tbody>
            {loading ? <tr><td colSpan={6} style={emptyStyle}>加载中...</td></tr> : logs.length === 0 ? <tr><td colSpan={6} style={emptyStyle}>暂无真实审计记录</td></tr> : logs.map((log) => (
              <tr key={log.id} style={{ borderBottom: '1px solid var(--color-border-light)' }}>
                <td style={cellStyle}>管理员 #{log.reviewerId}</td>
                <td style={cellStyle}><span style={tagStyle}>{actionLabels[log.action] || log.action}</span></td>
                <td style={cellStyle}>商品 #{log.productId}</td>
                <td style={cellStyle}>{statusLabels[log.beforeStatus] || log.beforeStatus} → {statusLabels[log.afterStatus] || log.afterStatus}</td>
                <td style={{ ...cellStyle, color: 'var(--color-text-secondary)' }}>{log.auditRemark || '-'}</td>
                <td style={{ ...cellStyle, color: 'var(--color-text-tertiary)', fontSize: 12 }}>{log.createdTime ? new Date(log.createdTime).toLocaleString('zh-CN') : '-'}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
      {pages > 1 && <div style={{ display: 'flex', justifyContent: 'flex-end', alignItems: 'center', gap: 12, marginTop: 16, fontSize: 13 }}>
        <span style={{ color: 'var(--color-text-secondary)' }}>第 {page} / {pages} 页</span>
        <button onClick={() => setPage((current) => Math.max(1, current - 1))} disabled={page <= 1} style={buttonStyle}>上一页</button>
        <button onClick={() => setPage((current) => Math.min(pages, current + 1))} disabled={page >= pages} style={buttonStyle}>下一页</button>
      </div>}
    </div>
  );
};

const headStyle: React.CSSProperties = { padding: '12px 16px', textAlign: 'left', fontSize: 13, color: 'var(--color-text-secondary)', fontWeight: 500 };
const cellStyle: React.CSSProperties = { padding: '12px 16px', fontSize: 13 };
const emptyStyle: React.CSSProperties = { padding: 28, textAlign: 'center', color: 'var(--color-text-tertiary)' };
const tagStyle: React.CSSProperties = { fontSize: 12, padding: '2px 8px', borderRadius: 4, background: 'var(--color-accent-light)', color: 'var(--color-accent)' };
const buttonStyle: React.CSSProperties = { padding: '7px 14px', border: '1px solid var(--color-border)', borderRadius: 'var(--radius-sm)', background: 'transparent', color: 'var(--color-text-secondary)', cursor: 'pointer' };

export default AuditLogPage;
