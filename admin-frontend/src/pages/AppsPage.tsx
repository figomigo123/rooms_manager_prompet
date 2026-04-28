import React, { useEffect } from 'react';
import { Row, Col, Card, Statistic, Table, Space, Button, Modal, Form, Input, Select, message } from 'antd';
import { PlusOutlined, EditOutlined, DeleteOutlined } from '@ant-design/icons';
import { useQuery, useMutation } from '@tanstack/react-query';
import { apiClient } from '../api/client';
import MainLayout from '../components/Layout/MainLayout';

interface Application {
  appId: string;
  appName: string;
  owner: string;
  plan: string;
  createdAt: string;
  status: string;
  totalUsers: number;
  totalRooms: number;
  monthlyMinutes: number;
  monthlyCost: number;
}

const AppsPage: React.FC = () => {
  const [isModalOpen, setIsModalOpen] = React.useState(false);
  const [form] = Form.useForm();

  // Fetch apps
  const { data: apps = [], isLoading, refetch } = useQuery({
    queryKey: ['apps'],
    queryFn: async () => {
      const response = await apiClient.get('/auth/v1/apps');
      return response.data;
    },
  });

  // Create app mutation
  const createAppMutation = useMutation({
    mutationFn: async (values: any) => {
      const response = await apiClient.post('/auth/v1/apps/register', values);
      return response.data;
    },
    onSuccess: () => {
      message.success('Application created successfully');
      setIsModalOpen(false);
      form.resetFields();
      refetch();
    },
    onError: () => {
      message.error('Failed to create application');
    },
  });

  // Delete app mutation
  const deleteAppMutation = useMutation({
    mutationFn: async (appId: string) => {
      await apiClient.delete(`/auth/v1/apps/${appId}`);
    },
    onSuccess: () => {
      message.success('Application deleted successfully');
      refetch();
    },
    onError: () => {
      message.error('Failed to delete application');
    },
  });

  const columns = [
    {
      title: 'App Name',
      dataIndex: 'appName',
      key: 'appName',
    },
    {
      title: 'Owner',
      dataIndex: 'owner',
      key: 'owner',
    },
    {
      title: 'Plan',
      dataIndex: 'plan',
      key: 'plan',
      render: (plan: string) => (
        <span style={{
          padding: '4px 12px',
          borderRadius: '4px',
          background: plan === 'PREMIUM' ? '#f6ffed' : '#f0f5ff',
          color: plan === 'PREMIUM' ? '#52c41a' : '#1677ff',
        }}>
          {plan}
        </span>
      ),
    },
    {
      title: 'Total Rooms',
      dataIndex: 'totalRooms',
      key: 'totalRooms',
    },
    {
      title: 'Total Users',
      dataIndex: 'totalUsers',
      key: 'totalUsers',
    },
    {
      title: 'Monthly Cost',
      dataIndex: 'monthlyCost',
      key: 'monthlyCost',
      render: (cost: number) => `$${cost.toFixed(2)}`,
    },
    {
      title: 'Status',
      dataIndex: 'status',
      key: 'status',
      render: (status: string) => (
        <span style={{
          padding: '4px 12px',
          borderRadius: '4px',
          background: status === 'ACTIVE' ? '#f6ffed' : '#fff1f0',
          color: status === 'ACTIVE' ? '#52c41a' : '#ff4d4f',
        }}>
          {status}
        </span>
      ),
    },
    {
      title: 'Actions',
      key: 'actions',
      render: (_: any, record: Application) => (
        <Space>
          <Button type="primary" size="small" icon={<EditOutlined />} />
          <Button
            danger
            size="small"
            icon={<DeleteOutlined />}
            onClick={() => deleteAppMutation.mutate(record.appId)}
            loading={deleteAppMutation.isPending}
          />
        </Space>
      ),
    },
  ];

  const handleCreateApp = async (values: any) => {
    createAppMutation.mutate(values);
  };

  return (
    <MainLayout>
      <div style={{ marginBottom: '24px' }}>
        <Button
          type="primary"
          icon={<PlusOutlined />}
          size="large"
          onClick={() => setIsModalOpen(true)}
        >
          Create New Application
        </Button>
      </div>

      <Row gutter={16} style={{ marginBottom: '24px' }}>
        <Col xs={24} sm={12} lg={6}>
          <Card>
            <Statistic
              title="Total Applications"
              value={apps.length}
              precision={0}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card>
            <Statistic
              title="Active Apps"
              value={apps.filter((a: Application) => a.status === 'ACTIVE').length}
              precision={0}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card>
            <Statistic
              title="Total Users"
              value={apps.reduce((sum: number, a: Application) => sum + a.totalUsers, 0)}
              precision={0}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card>
            <Statistic
              title="Monthly Revenue"
              value={apps.reduce((sum: number, a: Application) => sum + a.monthlyCost, 0)}
              precision={2}
              prefix="$"
            />
          </Card>
        </Col>
      </Row>

      <Card title="Applications" loading={isLoading}>
        <Table
          columns={columns}
          dataSource={apps}
          rowKey="appId"
          pagination={{ pageSize: 10 }}
        />
      </Card>

      <Modal
        title="Create New Application"
        open={isModalOpen}
        onOk={form.submit}
        onCancel={() => setIsModalOpen(false)}
        confirmLoading={createAppMutation.isPending}
      >
        <Form
          form={form}
          layout="vertical"
          onFinish={handleCreateApp}
        >
          <Form.Item
            label="Application Name"
            name="appName"
            rules={[{ required: true, message: 'Please enter app name' }]}
          >
            <Input placeholder="My App" />
          </Form.Item>
          <Form.Item
            label="Owner Email"
            name="owner"
            rules={[{ required: true, type: 'email' }]}
          >
            <Input placeholder="owner@example.com" />
          </Form.Item>
          <Form.Item
            label="Plan"
            name="plan"
            rules={[{ required: true }]}
          >
            <Select>
              <Select.Option value="FREE">Free</Select.Option>
              <Select.Option value="STARTER">Starter</Select.Option>
              <Select.Option value="PREMIUM">Premium</Select.Option>
            </Select>
          </Form.Item>
        </Form>
      </Modal>
    </MainLayout>
  );
};

export default AppsPage;
