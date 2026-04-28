import React from 'react';
import { Row, Col, Card, Statistic, Table, Button, Modal, Form, Input, InputNumber, Select, message } from 'antd';
import { EditOutlined, DeleteOutlined, FileTextOutlined } from '@ant-design/icons';
import { useQuery, useMutation } from '@tanstack/react-query';
import { apiClient } from '../api/client';
import MainLayout from '../components/Layout/MainLayout';

interface Invoice {
  invoiceId: string;
  appId: string;
  appName: string;
  amount: number;
  minutesUsed: number;
  status: string;
  dueDate: string;
  issuedDate: string;
  paidDate?: string;
}

const BillingPage: React.FC = () => {
  const [isModalOpen, setIsModalOpen] = React.useState(false);
  const [form] = Form.useForm();

  // Fetch invoices
  const { data: invoices = [], isLoading, refetch } = useQuery({
    queryKey: ['invoices'],
    queryFn: async () => {
      const response = await apiClient.get('/billing/v1/invoices');
      return response.data;
    },
  });

  // Fetch billing summary
  const { data: summary } = useQuery({
    queryKey: ['billing-summary'],
    queryFn: async () => {
      const response = await apiClient.get('/billing/v1/summary');
      return response.data;
    },
  });

  const columns = [
    {
      title: 'Invoice ID',
      dataIndex: 'invoiceId',
      key: 'invoiceId',
    },
    {
      title: 'Application',
      dataIndex: 'appName',
      key: 'appName',
    },
    {
      title: 'Minutes Used',
      dataIndex: 'minutesUsed',
      key: 'minutesUsed',
      render: (minutes: number) => minutes.toFixed(1),
    },
    {
      title: 'Amount',
      dataIndex: 'amount',
      key: 'amount',
      render: (amount: number) => `$${amount.toFixed(2)}`,
    },
    {
      title: 'Status',
      dataIndex: 'status',
      key: 'status',
      render: (status: string) => (
        <span style={{
          padding: '4px 12px',
          borderRadius: '4px',
          background: status === 'PAID' ? '#f6ffed' : status === 'PENDING' ? '#fff7e6' : '#fff1f0',
          color: status === 'PAID' ? '#52c41a' : status === 'PENDING' ? '#faad14' : '#ff4d4f',
        }}>
          {status}
        </span>
      ),
    },
    {
      title: 'Due Date',
      dataIndex: 'dueDate',
      key: 'dueDate',
      render: (date: string) => new Date(date).toLocaleDateString(),
    },
    {
      title: 'Actions',
      key: 'actions',
      render: (_: any, record: Invoice) => (
        <Button
          type="primary"
          size="small"
          icon={<FileTextOutlined />}
          onClick={() => downloadInvoice(record.invoiceId)}
        >
          Download
        </Button>
      ),
    },
  ];

  const downloadInvoice = (invoiceId: string) => {
    apiClient.get(`/billing/v1/invoices/${invoiceId}/pdf`, { responseType: 'blob' })
      .then((response) => {
        const url = window.URL.createObjectURL(new Blob([response.data]));
        const link = document.createElement('a');
        link.href = url;
        link.setAttribute('download', `invoice-${invoiceId}.pdf`);
        document.body.appendChild(link);
        link.click();
      })
      .catch(() => message.error('Failed to download invoice'));
  };

  return (
    <MainLayout>
      <Row gutter={16} style={{ marginBottom: '24px' }}>
        <Col xs={24} sm={12} lg={6}>
          <Card>
            <Statistic
              title="Total Revenue"
              value={summary?.totalRevenue || 0}
              prefix="$"
              precision={2}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card>
            <Statistic
              title="Pending Payments"
              value={summary?.pendingAmount || 0}
              prefix="$"
              precision={2}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card>
            <Statistic
              title="Total Invoices"
              value={invoices.length}
              precision={0}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card>
            <Statistic
              title="Avg Invoice Amount"
              value={(summary?.totalRevenue || 0) / (invoices.length || 1)}
              prefix="$"
              precision={2}
            />
          </Card>
        </Col>
      </Row>

      <Card title="Invoices" loading={isLoading}>
        <Table
          columns={columns}
          dataSource={invoices}
          rowKey="invoiceId"
          pagination={{ pageSize: 10 }}
        />
      </Card>
    </MainLayout>
  );
};

export default BillingPage;
