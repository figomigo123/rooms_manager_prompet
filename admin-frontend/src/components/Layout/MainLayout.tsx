import React, { useState } from 'react';
import { Layout, Menu, Avatar, Dropdown, Button } from 'antd';
import { LogoutOutlined, SettingOutlined, UserOutlined } from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import { useAuthStore } from '../store/authStore';
import logo from '../assets/logo.svg';
import './Layout.css';

const { Header, Sider, Content, Footer } = Layout;

const MainLayout: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [collapsed, setCollapsed] = useState(false);
  const navigate = useNavigate();
  const { user, logout } = useAuthStore();

  const menuItems = [
    {
      key: '/dashboard',
      label: 'Dashboard',
      icon: <SettingOutlined />,
    },
    {
      key: '/apps',
      label: 'Applications',
      icon: <UserOutlined />,
    },
    {
      key: '/rooms',
      label: 'Rooms',
      icon: <UserOutlined />,
    },
    {
      key: '/users',
      label: 'Users',
      icon: <UserOutlined />,
    },
    {
      key: '/billing',
      label: 'Billing',
      icon: <UserOutlined />,
    },
    {
      key: '/analytics',
      label: 'Analytics',
      icon: <SettingOutlined />,
    },
  ];

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const userMenu = [
    {
      key: 'profile',
      label: 'Profile Settings',
      icon: <UserOutlined />,
    },
    {
      type: 'divider',
    },
    {
      key: 'logout',
      label: 'Logout',
      icon: <LogoutOutlined />,
      onClick: handleLogout,
    },
  ];

  return (
    <Layout style={{ minHeight: '100vh' }}>
      <Sider
        theme="dark"
        collapsible
        collapsed={collapsed}
        onCollapse={(value) => setCollapsed(value)}
        width={200}
      >
        <div className="logo">
          <img src={logo} alt="Rooms Manager" />
          {!collapsed && <span>Rooms Manager</span>}
        </div>
        <Menu
          theme="dark"
          mode="inline"
          items={menuItems}
          onClick={(e) => navigate(e.key)}
        />
      </Sider>
      <Layout>
        <Header
          style={{
            background: '#fff',
            padding: '0 24px',
            boxShadow: '0 2px 8px rgba(0,0,0,0.1)',
            display: 'flex',
            justifyContent: 'space-between',
            alignItems: 'center',
          }}
        >
          <div>Rooms Manager Admin Dashboard</div>
          <Dropdown menu={{ items: userMenu }} trigger={['click']}>
            <Button
              type="text"
              icon={<Avatar icon={<UserOutlined />} />}
            >
              {user?.email}
            </Button>
          </Dropdown>
        </Header>
        <Content style={{ margin: '24px' }}>
          {children}
        </Content>
        <Footer style={{ textAlign: 'center' }}>
          Rooms Manager ©2024 - All Rights Reserved
        </Footer>
      </Layout>
    </Layout>
  );
};

export default MainLayout;
