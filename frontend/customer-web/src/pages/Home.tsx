import React from 'react';
import { Typography, Card } from 'antd';

const { Title, Paragraph } = Typography;

const Home: React.FC = () => {
  return (
    <Card>
      <Title level={2}>AI Commerce Platform Customer Web</Title>
      <Paragraph>欢迎来到智能电商平台。</Paragraph>
    </Card>
  );
};

export default Home;