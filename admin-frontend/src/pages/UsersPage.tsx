import React from 'react';
import { Row, Col, Card, Statistic, Table, Space, Button, Input, Select } from 'antd';
import { SearchOutlined, ReloadOutlined } from '@ant-design/icons';
import { useQuery } from '@tanstack/react-query';
import { apiClient } from '../api/client';
import MainLayout from '../components/Layout/MainLayout';

interface User {
  userId: string;
  userName: string;
  email: string;
  appId: string;
  currentRoom?: string;
  totalMinutes: number;
  status: string;
  lastActive: string;
  deviceType: string;
}

const UsersPage: React.FC = () => {
  const [searchText, setSearchText] = React.useState('');
  const [filterStatus, setFilterStatus] = React.useState<string | undefined>();

  // Fetch users
  const { data: users = [], isLoading, refetch } = useQuery({
    queryKey: ['users'],
    queryFn: async () => {
      const response = await apiClient.get('/user/v1/users');
      return response.data;
    },
  });

  const filteredUsers = users.filter((user: User) => {
    const matchesSearch = 
      user.userName.toLowerCase().includes(searchText.toLowerCase()) ||
      user.email.toLowerCase().includes(searchText.toLowerCase());
    const matchesStatus = !filterStatus || user.status === filterStatus;
    return matchesSearch && matchesStatus;
  });

  const columns = [
    {
      title: 'Username',
      dataIndex: 'userName',
      key: 'userName',
    },
    {
      title: 'Email',
      dataIndex: 'email',
      key: 'email',
    },
    {
      title: 'Current Room',
      dataIndex: 'currentRoom',
      key: 'currentRoom',
      render: (room: string) => room || '-',
    },
    {
      title: 'Total Minutes',
      dataIndex: 'totalMinutes',
      key: 'totalMinutes',
      render: (minutes: number) => minutes.toFixed(1),
    },
    {
      title: 'Device',
      dataIndex: 'deviceType',
      key: 'deviceType',
    },
    {
      title: 'Status',
      dataIndex: 'status',
      key: 'status',
      render: (status: string) => (
        <span style={{
          padding: '4px 12px',
          borderRadius: '4px',
          background: status === 'ONLINE' ? '#f6ffed' : '#f5f5f5',
          color: status === 'ONLINE' ? '#52c41a' : '#8c8c8c',
        }}>
          {status}
        </span>
      ),
    },
    {
      title: 'Last Active',
      dataIndex: 'lastActive',
      key: 'lastActive',
      render: (date: string) => new Date(date).toLocaleString(),
    },
  ];

  return (
    <MainLayout>
      <Row gutter={16} style={{ marginBottom: '24px' }}>
        <Col xs={24} sm={12} lg={6}>
          <Card>
            <Statistic
              title="Total Users"
              value={users.length}
              precision={0}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card>
            <Statistic
              title="Online Now"
              value={users.filter((u: User) => u.status === 'ONLINE').length}
              precision={0}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card>
            <Statistic
              title="Avg Minutes/User"
              value={(
                users.reduce((sum: number, u: User) => sum + u.totalMinutes, 0) / users.length || 0
              ).toFixed(1)}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card>
            <Statistic
              title="Total Minutes"
              value={users.reduce((sum: number, u: User) => sum + u.totalMinutes, 0).toFixed(0)}
            />
          </Card>
        </Col>
      </Row>

      <Card
        title="Users"
        loading={isLoading}
        extra={<Button icon={<ReloadOutlined />} onClick={() => refetch()} />}
      >
        <Space style={{ marginBottom: '16px', width: '100%' }} direction="vertical" size="large">
          <Row gutter={16}>
            <Col xs={24} sm={12}>
              <Input
                placeholder="Search by username or email"
                prefix={<SearchOutlined />}
                value={searchText}
                onChange={(e) => setSearchText(e.target.value)}
              />
            </Col>
            <Col xs={24} sm={12}>
              <Select
                placeholder="Filter by status"
                allowClear
                value={filterStatus}
                onChange={setFilterStatus}
                style={{ width: '100%' }}
              >
                <Select.Option value="ONLINE">Online</Select.Option>
                <Select.Option value="OFFLINE">Offline</Select.Option>
              </Select>
            </Col>
          </Row>
        </Space>

        <Table
          columns={columns}
          dataSource={filteredUsers}
          rowKey="userId"
          pagination={{ pageSize: 10 }}
        />
      </Card>
    </MainLayout>
  );
};

export default UsersPage;
