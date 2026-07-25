import React from 'react';
import { Layout, Typography } from 'antd';
import { Outlet } from 'react-router-dom';

const { Header, Content, Footer } = Layout;
const { Title } = Typography;

const BasicLayout: React.FC = () => {
  return (
    <Layout style={{ minHeight: '100vh' }}>
      <Header style={{ background: '#001529', display: 'flex', alignItems: 'center' }}>
        <Title level={3} style={{ color: '#fff', margin: 0 }}>AI Commerce Platform</Title>
      </Header>
      <Content style={{ padding: '24px' }}>
        <Outlet />
      </Content>
      <Footer style={{ textAlign: 'center' }}>
        AI Commerce Platform ©{new Date().getFullYear()}
      </Footer>
    </Layout>
  );
};

export default BasicLayout;