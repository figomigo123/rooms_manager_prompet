import React, { useState } from 'react';
import { Row, Col, Card, Statistic, Table, Space, Button, Modal, Form, Input, Select, message, Tabs } from 'antd';
import { PlusOutlined, EditOutlined, DeleteOutlined, EyeOutlined, AudioOutlined, VideoCameraOutlined } from '@ant-design/icons';
import { useQuery, useMutation } from '@tanstack/react-query';
import { apiClient } from '../api/client';
import MainLayout from '../components/Layout/MainLayout';

interface Room {
  roomId: string;
  appId: string;
  roomName: string;
  description: string;
  ownerId: string;
  currentUsers: number;
  maxUsers: number;
  status: string;
  createdAt: string;
  settings: {
    audioEnabled: boolean;
    videoEnabled: boolean;
    screenshareEnabled: boolean;
    whiteboardEnabled: boolean;
    chatEnabled: boolean;
  };
}

interface RoomStats {
  roomId: string;
  totalUsers: number;
  audioProducers: number;
  videoProducers: number;
  totalBitrate: string;
  averageLatency: string;
}

const RoomsPage: React.FC = () => {
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [selectedRoom, setSelectedRoom] = useState<Room | null>(null);
  const [form] = Form.useForm();

  // Fetch rooms
  const { data: rooms = [], isLoading, refetch } = useQuery({
    queryKey: ['rooms'],
    queryFn: async () => {
      const response = await apiClient.get('/room/v1/rooms');
      return response.data;
    },
  });

  // Fetch room stats
  const { data: roomStats } = useQuery({
    queryKey: ['roomStats', selectedRoom?.roomId],
    queryFn: async () => {
      if (!selectedRoom) return null;
      const response = await apiClient.get(`/room/v1/media/rooms/${selectedRoom.roomId}/stats`);
      return response.data;
    },
    enabled: !!selectedRoom,
  });

  // Create room mutation
  const createRoomMutation = useMutation({
    mutationFn: async (values: any) => {
      const response = await apiClient.post('/room/v1/rooms', values);
      return response.data;
    },
    onSuccess: () => {
      message.success('Room created successfully');
      setIsModalOpen(false);
      form.resetFields();
      refetch();
    },
    onError: () => {
      message.error('Failed to create room');
    },
  });

  // Delete room mutation
  const deleteRoomMutation = useMutation({
    mutationFn: async (roomId: string) => {
      await apiClient.delete(`/room/v1/rooms/${roomId}`);
    },
    onSuccess: () => {
      message.success('Room deleted successfully');
      refetch();
    },
    onError: () => {
      message.error('Failed to delete room');
    },
  });

  const columns = [
    {
      title: 'Room Name',
      dataIndex: 'roomName',
      key: 'roomName',
    },
    {
      title: 'Owner',
      dataIndex: 'ownerId',
      key: 'ownerId',
    },
    {
      title: 'Users',
      dataIndex: 'currentUsers',
      key: 'currentUsers',
      render: (users: number, record: Room) => `${users}/${record.maxUsers}`,
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
      title: 'Features',
      key: 'features',
      render: (_: any, record: Room) => (
        <Space size="small">
          {record.settings.audioEnabled && <AudioOutlined title="Audio" />}
          {record.settings.videoEnabled && <VideoCameraOutlined title="Video" />}
        </Space>
      ),
    },
    {
      title: 'Actions',
      key: 'actions',
      render: (_: any, record: Room) => (
        <Space>
          <Button
            type="primary"
            size="small"
            icon={<EyeOutlined />}
            onClick={() => setSelectedRoom(record)}
          />
          <Button
            danger
            size="small"
            icon={<DeleteOutlined />}
            onClick={() => deleteRoomMutation.mutate(record.roomId)}
            loading={deleteRoomMutation.isPending}
          />
        </Space>
      ),
    },
  ];

  const handleCreateRoom = async (values: any) => {
    createRoomMutation.mutate(values);
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
          Create New Room
        </Button>
      </div>

      <Row gutter={16} style={{ marginBottom: '24px' }}>
        <Col xs={24} sm={12} lg={6}>
          <Card>
            <Statistic
              title="Total Rooms"
              value={rooms.length}
              precision={0}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card>
            <Statistic
              title="Active Rooms"
              value={rooms.filter((r: Room) => r.status === 'ACTIVE').length}
              precision={0}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card>
            <Statistic
              title="Total Users"
              value={rooms.reduce((sum: number, r: Room) => sum + r.currentUsers, 0)}
              precision={0}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card>
            <Statistic
              title="Capacity Used"
              value={(
                (rooms.reduce((sum: number, r: Room) => sum + r.currentUsers, 0) /
                rooms.reduce((sum: number, r: Room) => sum + r.maxUsers, 0)) *
                100
              ).toFixed(1)}
              suffix="%"
            />
          </Card>
        </Col>
      </Row>

      <Row gutter={16}>
        <Col xs={24} lg={16}>
          <Card title="Rooms" loading={isLoading}>
            <Table
              columns={columns}
              dataSource={rooms}
              rowKey="roomId"
              pagination={{ pageSize: 10 }}
            />
          </Card>
        </Col>
        <Col xs={24} lg={8}>
          {selectedRoom && (
            <Card title={`Room: ${selectedRoom.roomName}`}>
              <Tabs
                items={[
                  {
                    key: 'details',
                    label: 'Details',
                    children: (
                      <div>
                        <p><strong>Room ID:</strong> {selectedRoom.roomId}</p>
                        <p><strong>Owner:</strong> {selectedRoom.ownerId}</p>
                        <p><strong>Users:</strong> {selectedRoom.currentUsers}/{selectedRoom.maxUsers}</p>
                        <p><strong>Status:</strong> {selectedRoom.status}</p>
                        <p><strong>Created:</strong> {new Date(selectedRoom.createdAt).toLocaleString()}</p>
                      </div>
                    ),
                  },
                  {
                    key: 'stats',
                    label: 'Statistics',
                    children: roomStats && (
                      <div>
                        <p><strong>Audio Producers:</strong> {roomStats.audioProducers}</p>
                        <p><strong>Video Producers:</strong> {roomStats.videoProducers}</p>
                        <p><strong>Total Bitrate:</strong> {roomStats.totalBitrate}</p>
                        <p><strong>Avg Latency:</strong> {roomStats.averageLatency}</p>
                      </div>
                    ),
                  },
                ]}
              />
            </Card>
          )}
        </Col>
      </Row>

      <Modal
        title="Create New Room"
        open={isModalOpen}
        onOk={form.submit}
        onCancel={() => setIsModalOpen(false)}
        confirmLoading={createRoomMutation.isPending}
      >
        <Form
          form={form}
          layout="vertical"
          onFinish={handleCreateRoom}
        >
          <Form.Item
            label="Room Name"
            name="roomName"
            rules={[{ required: true, message: 'Please enter room name' }]}
          >
            <Input placeholder="Team Meeting" />
          </Form.Item>
          <Form.Item
            label="Description"
            name="description"
          >
            <Input.TextArea placeholder="Room description" />
          </Form.Item>
          <Form.Item
            label="Max Users"
            name="maxUsers"
            rules={[{ required: true }]}
          >
            <Select>
              <Select.Option value={10}>10</Select.Option>
              <Select.Option value={50}>50</Select.Option>
              <Select.Option value={100}>100</Select.Option>
              <Select.Option value={200}>200</Select.Option>
            </Select>
          </Form.Item>
          <Form.Item label="Enable Audio" name="audioEnabled" valuePropName="checked">
            <Input type="checkbox" />
          </Form.Item>
          <Form.Item label="Enable Video" name="videoEnabled" valuePropName="checked">
            <Input type="checkbox" />
          </Form.Item>
        </Form>
      </Modal>
    </MainLayout>
  );
};

export default RoomsPage;
