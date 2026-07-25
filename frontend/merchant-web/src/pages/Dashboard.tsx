import React from 'react';
import { Typography, Card } from 'antd';

const { Title, Paragraph } = Typography;

const Dashboard: React.FC = () => {
  return (
    <Card>
      <Title level={2}>AI Commerce Merchant Console</Title>
      <Paragraph>商家控制台首页（待实现）</Paragraph>
    </Card>
  );
};

export default Dashboard;