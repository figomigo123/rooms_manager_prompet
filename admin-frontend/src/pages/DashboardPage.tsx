import React from 'react';
import { Row, Col, Card, Statistic, LineChart, BarChart, PieChart, Table, Select } from 'antd';
import { Line, Bar, Pie } from 'react-chartjs-2';
import { useQuery } from '@tanstack/react-query';
import { apiClient } from '../api/client';
import MainLayout from '../components/Layout/MainLayout';
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  BarElement,
  ArcElement,
  Title,
  Tooltip,
  Legend,
} from 'chart.js';

ChartJS.register(
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  BarElement,
  ArcElement,
  Title,
  Tooltip,
  Legend
);

const DashboardPage: React.FC = () => {
  const [timeRange, setTimeRange] = React.useState('7days');

  // Fetch dashboard stats
  const { data: stats } = useQuery({
    queryKey: ['dashboard-stats', timeRange],
    queryFn: async () => {
      const response = await apiClient.get(`/analytics/v1/dashboard-stats?range=${timeRange}`);
      return response.data;
    },
  });

  // Fetch usage trends
  const { data: usageTrends } = useQuery({
    queryKey: ['usage-trends', timeRange],
    queryFn: async () => {
      const response = await apiClient.get(`/analytics/v1/usage-trends?range=${timeRange}`);
      return response.data;
    },
  });

  const lineChartData = {
    labels: usageTrends?.dates || [],
    datasets: [
      {
        label: 'Minutes Used',
        data: usageTrends?.minutes || [],
        borderColor: '#1677ff',
        backgroundColor: 'rgba(22, 119, 255, 0.1)',
        tension: 0.4,
      },
      {
        label: 'Active Users',
        data: usageTrends?.activeUsers || [],
        borderColor: '#52c41a',
        backgroundColor: 'rgba(82, 196, 26, 0.1)',
        tension: 0.4,
      },
    ],
  };

  const pieChartData = {
    labels: ['Audio', 'Video', 'Screen Share'],
    datasets: [
      {
        data: [stats?.audioMinutes || 0, stats?.videoMinutes || 0, stats?.screenMinutes || 0],
        backgroundColor: ['#1677ff', '#52c41a', '#faad14'],
      },
    ],
  };

  return (
    <MainLayout>
      <Row gutter={16} style={{ marginBottom: '24px' }}>
        <Col xs={24}>
          <Select
            value={timeRange}
            onChange={setTimeRange}
            style={{ width: 200 }}
          >
            <Select.Option value="24hours">Last 24 Hours</Select.Option>
            <Select.Option value="7days">Last 7 Days</Select.Option>
            <Select.Option value="30days">Last 30 Days</Select.Option>
            <Select.Option value="90days">Last 90 Days</Select.Option>
          </Select>
        </Col>
      </Row>

      <Row gutter={16} style={{ marginBottom: '24px' }}>
        <Col xs={24} sm={12} lg={6}>
          <Card>
            <Statistic
              title="Total Users"
              value={stats?.totalUsers || 0}
              precision={0}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card>
            <Statistic
              title="Active Users"
              value={stats?.activeUsers || 0}
              precision={0}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card>
            <Statistic
              title="Total Minutes"
              value={stats?.totalMinutes || 0}
              precision={0}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card>
            <Statistic
              title="Total Revenue"
              value={stats?.totalRevenue || 0}
              prefix="$"
              precision={2}
            />
          </Card>
        </Col>
      </Row>

      <Row gutter={16} style={{ marginBottom: '24px' }}>
        <Col xs={24} lg={12}>
          <Card title="Usage Trends">
            <Line data={lineChartData} options={{ responsive: true, maintainAspectRatio: true }} />
          </Card>
        </Col>
        <Col xs={24} lg={12}>
          <Card title="Minutes by Type">
            <Pie data={pieChartData} options={{ responsive: true, maintainAspectRatio: true }} />
          </Card>
        </Col>
      </Row>

      <Row gutter={16}>
        <Col xs={24}>
          <Card title="Top Applications">
            <Table
              columns={[
                {
                  title: 'App Name',
                  dataIndex: 'appName',
                  key: 'appName',
                },
                {
                  title: 'Users',
                  dataIndex: 'users',
                  key: 'users',
                },
                {
                  title: 'Minutes',
                  dataIndex: 'minutes',
                  key: 'minutes',
                },
                {
                  title: 'Revenue",
                  dataIndex: 'revenue',
                  key: 'revenue',
                  render: (value: number) => `$${value.toFixed(2)}`,
                },
              ]}
              dataSource={stats?.topApps || []}
              pagination={false}
              rowKey="appId"
            />
          </Card>
        </Col>
      </Row>
    </MainLayout>
  );
};

export default DashboardPage;
