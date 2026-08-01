import React, { useState } from 'react';
import { Form, Input, Button, Card, Typography, Alert, Space } from 'antd';
import { useNavigate, Link } from 'react-router-dom';
import { registerApi } from '../services/auth';
import type { ApiResult, UserInfo } from '@shared/types/auth';

const { Title, Text } = Typography;

interface RegisterFormValues {
  username: string;
  email: string;
  nickname?: string;
  password: string;
  confirmPassword: string;
}

const Register: React.FC = () => {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const onFinish = async (values: RegisterFormValues) => {
    setLoading(true);
    setError(null);

    try {
      const res: ApiResult<UserInfo> = await registerApi({
        username: values.username,
        email: values.email,
        password: values.password,
        nickname: values.nickname || values.username,
      });

      if (res.code === 0) {
        navigate('/login', { replace: true });
      } else {
        setError(res.message || '注册失败');
      }
    } catch (err: unknown) {
      if (err && typeof err === 'object' && 'response' in err) {
        const axiosErr = err as { response?: { data?: { message?: string } } };
        setError(axiosErr.response?.data?.message || '注册失败，请重试');
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
            <Title level={3}>商户注册</Title>
            <Text type="secondary">注册成为 AI Commerce Platform 商家</Text>
          </div>

          {error && (
            <Alert message={error} type="error" showIcon closable onClose={() => setError(null)} />
          )}

          <Form
            name="register"
            layout="vertical"
            onFinish={onFinish}
            autoComplete="off"
          >
            <Form.Item
              label="用户名"
              name="username"
              rules={[{ required: true, message: '请输入用户名' }]}
            >
              <Input placeholder="请输入用户名" size="large" />
            </Form.Item>

            <Form.Item
              label="邮箱"
              name="email"
              rules={[
                { required: true, message: '请输入邮箱' },
                { type: 'email', message: '请输入有效的邮箱地址' },
              ]}
            >
              <Input placeholder="请输入邮箱" size="large" />
            </Form.Item>

            <Form.Item
              label="昵称"
              name="nickname"
            >
              <Input placeholder="请输入昵称（可选）" size="large" />
            </Form.Item>

            <Form.Item
              label="密码"
              name="password"
              rules={[
                { required: true, message: '请输入密码' },
                { min: 6, message: '密码至少 6 位' },
              ]}
            >
              <Input.Password placeholder="请输入密码" size="large" />
            </Form.Item>

            <Form.Item
              label="确认密码"
              name="confirmPassword"
              dependencies={['password']}
              rules={[
                { required: true, message: '请再次输入密码' },
                ({ getFieldValue }) => ({
                  validator(_, value) {
                    if (!value || getFieldValue('password') === value) {
                      return Promise.resolve();
                    }
                    return Promise.reject(new Error('两次输入的密码不一致'));
                  },
                }),
              ]}
            >
              <Input.Password placeholder="请再次输入密码" size="large" />
            </Form.Item>

            <Form.Item>
              <Button type="primary" htmlType="submit" block size="large" loading={loading}>
                注册
              </Button>
            </Form.Item>
          </Form>

          <div style={{ textAlign: 'center' }}>
            <Text>
              已有账号？ <Link to="/login">立即登录</Link>
            </Text>
          </div>
        </Space>
      </Card>
    </div>
  );
};

export default Register;