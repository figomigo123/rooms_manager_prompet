# React Admin Dashboard - Rooms Manager

Comprehensive admin dashboard for managing the Agora-like SaaS communication platform.

## Features

### 📊 Dashboard
- **Real-time Statistics**: Total users, active users, total minutes, revenue
- **Usage Trends**: Line chart showing minutes used and active users over time
- **Minutes by Type**: Pie chart showing audio, video, and screen share distribution
- **Top Applications**: Table showing top performing apps
- **Time Range Filtering**: 24 hours, 7 days, 30 days, 90 days

### 🏢 Applications Management
- **Create Applications**: Register new apps
- **View App Details**: See app statistics and metrics
- **Update Applications**: Edit app settings
- **Delete Applications**: Remove apps with confirmation
- **Statistics**: Total rooms, users, monthly cost per app
- **Plan Management**: FREE, STARTER, PREMIUM plans

### 🚪 Rooms Management
- **Create Rooms**: Set up new communication rooms
- **View Room Details**: Room statistics and configuration
- **Monitor Room Stats**: Users, bitrate, latency, producers/consumers
- **Delete Rooms**: Remove inactive rooms
- **Features Toggle**: Audio, video, screen sharing, whiteboard, chat
- **Capacity Monitoring**: Track room usage and capacity

### 👥 Users Management
- **View All Users**: List of registered users
- **Search Users**: By username or email
- **Filter Users**: Online/Offline status
- **User Statistics**: Total minutes, device type, last active
- **User Details**: Email, current room, total usage

### 💳 Billing & Invoicing
- **Invoice Management**: View all invoices
- **Revenue Tracking**: Total revenue, pending payments
- **Invoice Download**: PDF export
- **Payment Status**: Track paid, pending, overdue invoices
- **Usage Details**: Minutes per invoice

## Tech Stack

- **React 18** - UI framework
- **TypeScript** - Type safety
- **Vite** - Build tool
- **Ant Design (antd)** - UI components
- **React Router** - Navigation
- **Axios** - HTTP client
- **TanStack Query** - Data fetching & caching
- **Zustand** - State management
- **Chart.js** - Data visualization
- **Tailwind CSS** - Styling

## Project Structure

```
admin-frontend/
├── src/
│   ├── pages/
│   │   ├── DashboardPage.tsx          # Main dashboard
│   │   ├── AppsPage.tsx               # App management
│   │   ├── RoomsPage.tsx              # Room management
│   │   ├── UsersPage.tsx              # User management
│   │   └── BillingPage.tsx            # Billing & invoices
│   ├── components/
│   │   └── Layout/
│   │       ├── MainLayout.tsx         # Main layout wrapper
│   │       └── Layout.css             # Layout styles
│   ├── api/
│   │   └── client.ts                  # Axios HTTP client
│   ├── store/
│   │   └── authStore.ts               # Auth state management
│   ├── App.tsx                        # App routing
│   ├── main.tsx                       # Entry point
│   └── index.css                      # Global styles
├── index.html                         # HTML template
├── vite.config.ts                     # Vite config
├── tsconfig.json                      # TypeScript config
├── Dockerfile                         # Docker build
├── nginx.conf                         # Nginx config
├── package.json                       # Dependencies
└── README.md                          # This file
```

## Getting Started

### Prerequisites
- Node.js 16+
- npm or yarn

### Installation

```bash
cd admin-frontend
npm install
```

### Development

```bash
npm run dev
```

Open [http://localhost:3000](http://localhost:3000) in your browser.

### Build for Production

```bash
npm run build
```

### Type Checking

```bash
npm run type-check
```

## API Integration

The dashboard connects to multiple backend services:

### Auth Service (Port 8001)
```
GET /auth/v1/apps              - List all applications
POST /auth/v1/apps/register   - Create application
DELETE /auth/v1/apps/{appId}  - Delete application
```

### Room Service (Port 8002)
```
GET /room/v1/rooms            - List all rooms
POST /room/v1/rooms          - Create room
GET /room/v1/rooms/{roomId}  - Get room details
DELETE /room/v1/rooms/{roomId} - Delete room
```

### MediaSoup Service (Port 8003)
```
GET /room/v1/media/rooms/{roomId}/stats      - Room statistics
GET /room/v1/media/rooms/{roomId}/top-speakers - Top 10 speakers
POST /room/v1/media/rooms/{roomId}/mute      - Mute user
```

### Analytics Service (Port 8005)
```
GET /analytics/v1/dashboard-stats - Dashboard statistics
GET /analytics/v1/usage-trends    - Usage trends data
```

### Billing Service (Port 8006)
```
GET /billing/v1/invoices           - List invoices
GET /billing/v1/invoices/{id}/pdf  - Download PDF
GET /billing/v1/summary            - Billing summary
```

## Environment Variables

Create a `.env.local` file:

```
REACT_APP_AUTH_URL=http://localhost:8001
REACT_APP_ROOM_URL=http://localhost:8002
REACT_APP_MEDIASOUP_URL=http://localhost:8003
REACT_APP_ANALYTICS_URL=http://localhost:8005
REACT_APP_BILLING_URL=http://localhost:8006
```

## Docker

Build and run with Docker:

```bash
docker build -t rooms-manager-admin .
docker run -p 8080:80 rooms-manager-admin
```

Or with docker-compose:

```bash
docker-compose up admin-frontend
```

## Features in Detail

### Dashboard
- **Real-time KPIs**: Updates every 30 seconds
- **Customizable Time Range**: Filter data by period
- **Export Reports**: Download usage reports
- **Performance Monitoring**: CPU, memory, latency metrics

### Apps Management
- **CRUD Operations**: Full lifecycle management
- **API Keys**: Generate and manage app secrets
- **Usage Limits**: Set quotas per app
- **Billing Integration**: Track revenue per app

### Room Management
- **Room Settings**: Configure features per room
- **Monitoring**: Real-time room statistics
- **User Tracking**: See who's in each room
- **Recording Management**: Start/stop recordings

### User Management
- **Search & Filter**: Find users quickly
- **Usage Tracking**: Monitor user activities
- **Device Information**: See device types
- **Export Lists**: Download user reports

### Billing
- **Invoice Management**: Create and track invoices
- **Payment Processing**: Integrate with payment gateways
- **Reporting**: Monthly and yearly reports
- **Refunds**: Process refunds and credits

## Performance Optimization

- **React Query**: Automatic caching and refetching
- **Lazy Loading**: Components load on demand
- **Memoization**: Prevent unnecessary re-renders
- **Code Splitting**: Vite automatically handles this
- **Image Optimization**: WebP with fallbacks

## Security Features

- **JWT Tokens**: Secure authentication
- **CORS Protection**: Prevent unauthorized access
- **Input Validation**: Client-side validation
- **Error Handling**: Graceful error messages
- **Auto Logout**: Session expiration

## Troubleshooting

### CORS Errors
Ensure backend services have CORS enabled:

```yaml
# In Spring Boot application.yml
cors:
  allowed-origins:
    - http://localhost:3000
    - http://localhost:8080
```

### Connection Refused
Check if backend services are running:

```bash
curl http://localhost:8001/auth/v1/health
curl http://localhost:8002/room/v1/health
```

### Blank Dashboard
Check browser console for errors (F12) and verify API URLs in `.env.local`

## Contributing

Feel free to submit issues and enhancement requests!

## License

MIT - See LICENSE file for details
