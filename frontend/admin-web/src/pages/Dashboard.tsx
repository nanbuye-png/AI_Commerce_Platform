import React from 'react';
import { Typography, Card } from 'antd';

const { Title, Paragraph } = Typography;

const Dashboard: React.FC = () => {
  return (
    <Card>
      <Title level={2}>AI Commerce Admin Console</Title>
      <Paragraph>管理控制台首页（待实现）</Paragraph>
    </Card>
  );
};

export default Dashboard;