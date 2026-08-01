import React, { useState } from 'react';
import { Form, Input, Button, Card, Typography, Alert, Space } from 'antd';
import { useNavigate } from 'react-router-dom';
import { loginApi } from '../services/auth';
import useAuthStore from '../stores/authStore';
import type { ApiResult, AuthResponse } from '@shared/types/auth';

const { Title, Text } = Typography;

const Login: React.FC = () => {
  const navigate = useNavigate();
  const { login } = useAuthStore();
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const onFinish = async (values: { account: string; password: string }) => {
    setLoading(true);
    setError(null);

    try {
      const res: ApiResult<AuthResponse> = await loginApi({
        account: values.account,
        password: values.password,
      });

      if (res.code === 0 && res.data) {
        login(res.data.token, {
          id: res.data.userId,
          username: res.data.username,
          nickname: res.data.username,
          email: '',
          role: res.data.role,
          status: 'ACTIVE',
        });
        void navigate('/dashboard', { replace: true });
      } else {
        setError(res.message || '登录失败');
      }
    } catch (err: unknown) {
      if (err && typeof err === 'object' && 'response' in err) {
        const axiosErr = err as { response?: { data?: { message?: string } } };
        setError(axiosErr.response?.data?.message || '登录失败，请检查账号密码');
      } else {
        setError('网络错误，请稍后重试');
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <div
      style={{
        display: 'flex',
        justifyContent: 'center',
        alignItems: 'center',
        minHeight: '100vh',
        background: '#f0f2f5',
      }}
    >
      <Card style={{ width: 400, boxShadow: '0 2px 8px rgba(0,0,0,0.1)' }}>
        <Space direction="vertical" size="large" style={{ width: '100%' }}>
          <div style={{ textAlign: 'center' }}>
            <Title level={3}>管理员登录</Title>
            <Text type="secondary">AI Commerce Platform</Text>
          </div>

          {error && (
            <Alert message={error} type="error" showIcon closable onClose={() => setError(null)} />
          )}

          <Form
            name="login"
            layout="vertical"
            onFinish={onFinish}
            autoComplete="off"
          >
            <Form.Item
              label="账号"
              name="account"
              rules={[{ required: true, message: '请输入用户名或邮箱' }]}
            >
              <Input placeholder="请输入用户名或邮箱" size="large" />
            </Form.Item>

            <Form.Item
              label="密码"
              name="password"
              rules={[{ required: true, message: '请输入密码' }]}
            >
              <Input.Password placeholder="请输入密码" size="large" />
            </Form.Item>

            <Form.Item>
              <Button type="primary" htmlType="submit" block size="large" loading={loading}>
                登录
              </Button>
            </Form.Item>
          </Form>
        </Space>
      </Card>
    </div>
  );
};

export default Login;