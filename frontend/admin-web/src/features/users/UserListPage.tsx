import React, { useState, useEffect } from 'react';
import { userApi } from '../../api/users';
import type { UserVO } from '../../api/users';

const roleLabels: Record<string, string> = {
  CUSTOMER: '用户',
  MERCHANT: '商家',
  ADMIN: '管理员',
  SUPER_ADMIN: '超级管理员',
};

const statusLabels: Record<string, string> = {
  ACTIVE: '正常',
  INACTIVE: '停用',
  LOCKED: '锁定',
};

const statusColors: Record<string, string> = {
  ACTIVE: '#34C759',
  INACTIVE: '#A1A1A6',
  LOCKED: '#FF3B30',
};

const UserListPage: React.FC = () => {
  const [users, setUsers] = useState<UserVO[]>([]);
  const [loading, setLoading] = useState(false);
  const [roleFilter, setRoleFilter] = useState('');

  const loadUsers = async () => {
    setLoading(true);
    try {
      const params: any = { page: 0, pageSize: 20 };
      if (roleFilter) params.role = roleFilter;
      const res = await userApi.list(params);
      const data = res?.data || res;
      setUsers(data?.content || []);
    } catch (err) {
      console.error('加载用户列表失败:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadUsers();
  }, [roleFilter]);

  const handleStatusChange = async (id: number, newStatus: string) => {
    try {
      await userApi.updateStatus(id, newStatus);
      loadUsers();
    } catch (err) {
      console.error('更新状态失败:', err);
      alert('操作失败');
    }
  };

  return (
    <div style={{ maxWidth: 1200, margin: '0 auto' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 'var(--spacing-lg)' }}>
        <h1 style={{ fontSize: 'var(--font-size-h1)', fontWeight: 600, margin: 0 }}>用户管理</h1>
        <select
          value={roleFilter}
          onChange={(e) => setRoleFilter(e.target.value)}
          style={{ padding: '8px 12px', borderRadius: 'var(--radius-sm)', border: '1px solid var(--color-border)', fontSize: '14px' }}
        >
          <option value="">全部角色</option>
          <option value="CUSTOMER">用户</option>
          <option value="MERCHANT">商家</option>
          <option value="ADMIN">管理员</option>
        </select>
      </div>

      <div style={{ background: 'var(--color-bg-primary)', borderRadius: 'var(--radius-md)', boxShadow: 'var(--shadow-sm)', overflow: 'hidden' }}>
        <table style={{ width: '100%', borderCollapse: 'collapse' }}>
          <thead>
            <tr style={{ borderBottom: '1px solid var(--color-border-light)', background: 'var(--color-bg-secondary)' }}>
              {['ID', '用户名', '邮箱', '角色', '状态', '注册时间', '操作'].map((h) => (
                <th key={h} style={{ padding: '12px 16px', textAlign: 'left', fontSize: '13px', color: 'var(--color-text-secondary)', fontWeight: 500 }}>{h}</th>
              ))}
            </tr>
          </thead>
          <tbody>
            {loading ? (
              <tr><td colSpan={7} style={{ padding: '24px', textAlign: 'center', color: 'var(--color-text-tertiary)' }}>加载中...</td></tr>
            ) : users.length === 0 ? (
              <tr><td colSpan={7} style={{ padding: '24px', textAlign: 'center', color: 'var(--color-text-tertiary)' }}>暂无数据</td></tr>
            ) : (
              users.map((u) => (
                <tr key={u.id} style={{ borderBottom: '1px solid var(--color-border-light)' }}>
                  <td style={{ padding: '12px 16px', fontSize: '13px', color: 'var(--color-text-primary)' }}>#{u.id}</td>
                  <td style={{ padding: '12px 16px', fontSize: '13px', color: 'var(--color-text-primary)' }}>{u.username}</td>
                  <td style={{ padding: '12px 16px', fontSize: '13px', color: 'var(--color-text-secondary)' }}>{u.email}</td>
                  <td style={{ padding: '12px 16px', fontSize: '13px', color: 'var(--color-text-secondary)' }}>{roleLabels[u.role] || u.role}</td>
                  <td style={{ padding: '12px 16px' }}>
                    <span style={{ fontSize: '12px', padding: '2px 8px', borderRadius: '4px', background: `${statusColors[u.status] || '#86868B'}18`, color: statusColors[u.status] || '#86868B' }}>
                      {statusLabels[u.status] || u.status}
                    </span>
                  </td>
                  <td style={{ padding: '12px 16px', fontSize: '12px', color: 'var(--color-text-tertiary)' }}>{u.createdTime ? new Date(u.createdTime).toLocaleDateString('zh-CN') : '-'}</td>
                  <td style={{ padding: '12px 16px' }}>
                    <select
                      value={u.status}
                      onChange={(e) => handleStatusChange(u.id, e.target.value)}
                      style={{ padding: '4px 8px', borderRadius: 'var(--radius-sm)', border: '1px solid var(--color-border)', fontSize: '12px' }}
                    >
                      <option value="ACTIVE">正常</option>
                      <option value="INACTIVE">停用</option>
                      <option value="LOCKED">锁定</option>
                    </select>
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
};

export default UserListPage;