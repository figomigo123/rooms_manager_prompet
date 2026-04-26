# System Prompt: Building Agora-Like Communication SaaS Platform

## Project Overview
You are building a scalable, enterprise-grade real-time communication SaaS platform similar to Agora that enables applications to integrate audio, video, screen sharing, whiteboard, and chat capabilities. The platform must support massive scalability (200+ users per room), multi-room management, comprehensive usage tracking, and flexible access controls.

---

## Core Architecture Requirements

### Tech Stack
- **Backend**: Spring Boot Microservices + MongoDB
- **Admin Dashboard**: React TypeScript
- **Real-time Media**: MediaSoup or LiveKit (media server choice)
- **DevOps**: Docker & Docker Compose
- **Test Client**: Flutter Mobile App
- **API Integration**: RESTful + WebSocket for real-time events

---

## System Components

### 1. **Media Server Selection: MediaSoup vs LiveKit**

#### MediaSoup (Recommended for Custom Control)
**Pros:**
- Pure JavaScript/Node.js media server
- Complete control over SFU (Selective Forwarding Unit) logic
- Lower latency, custom optimization possible
- Lightweight, can scale horizontally
- Better for advanced features (selective streaming, bitrate control)

**Cons:**
- Requires more infrastructure management
- Complex deployment & monitoring
- Need Node.js expertise for customization

**Best For Your Case:** ✅ **MediaSoup** - You need fine-grained control for 200+ users, selective audio/video streaming, and custom speaker management (10 speakers + listeners model)

#### LiveKit (Recommended for Managed Simplicity)
**Pros:**
- Fully managed SFU with scaling built-in
- Simple REST API + WebSocket
- Multi-datacenter support
- Better observability & monitoring
- Less operational overhead

**Cons:**
- Less control over media routing
- Higher latency than MediaSoup
- Can be more expensive at scale

**Recommendation:** Start with **MediaSoup** for maximum control. Migrate to LiveKit later if operational complexity becomes burden.

---

## 2. **Microservices Architecture**

```
┌─────────────────────────────────────────────────────┐
│           API Gateway (Spring Cloud Gateway)        │
└──────────────┬──────────────────────────────────────┘
               │
    ┌──────────┼──────────┬──────────┬──────────┐
    │          │          │          │          │
┌───▼──┐  ┌───▼──┐  ┌───▼──┐  ┌───▼──┐  ┌───▼──┐
│ Auth │  │ Room │  │ User │  │Media │  │Usage │
│Service│  │Service│  │Service│  │Service│  │Service│
└─────┘  └─────┘  └─────┘  └─────┘  └─────┘
    │          │          │          │          │
    └──────────┼──────────┴──────────┴──────────┘
               │
        ┌──────▼─────────┐
        │   MongoDB      │
        │   (Shared DB)  │
        └────────────────┘
```

### 2.1 **Auth Service** (Port: 8001)
**Responsibilities:**
- App authentication (API keys, OAuth2)
- User authentication within apps
- JWT token generation & validation
- Role-based access control (RBAC)
- Permission management

**Key Entities:**
```java
// Application (Tenant)
- appId
- appSecret
- appName
- owner
- plan (FREE, PROFESSIONAL, ENTERPRISE)
- apiKey
- webhookUrl
- createdAt

// AppUser
- userId
- appId
- email
- name
- role (ADMIN, MODERATOR, USER)
- permissions
- metadata
- createdAt
```

**API Endpoints:**
```
POST   /auth/v1/apps/register       // Create new app
POST   /auth/v1/apps/keys           // Generate API key
GET    /auth/v1/apps/{appId}        // Get app details
POST   /auth/v1/users/login         // User login
POST   /auth/v1/users/verify-token  // Verify JWT
POST   /auth/v1/roles/{roleId}      // Create custom role
```

### 2.2 **Room Service** (Port: 8002)
**Responsibilities:**
- Room creation & management
- Room configuration (audio/video/whiteboard/chat settings)
- Room state management
- Owner & admin management
- Room access control

**Key Entities:**
```java
// Room
- roomId (UUID)
- appId
- roomName
- ownerId (AppUser)
- description
- settings: {
    maxUsers: 200,
    audioEnabled: true,
    videoEnabled: true,
    screenshareEnabled: true,
    whiteboardEnabled: true,
    chatEnabled: true,
    recordingEnabled: false
  }
- admins: [userId]
- status: ACTIVE, INACTIVE, FULL
- createdAt
- expiresAt (optional)

// RoomAdmin
- roomId
- userId
- grantedBy
- permissions (KICK_USER, MUTE_AUDIO, MUTE_VIDEO, etc.)
- grantedAt
```

**API Endpoints:**
```
POST   /room/v1/rooms              // Create room
GET    /room/v1/rooms/{roomId}     // Get room details
PUT    /room/v1/rooms/{roomId}     // Update room settings
DELETE /room/v1/rooms/{roomId}     // Delete room
POST   /room/v1/rooms/{roomId}/admins        // Add admin
DELETE /room/v1/rooms/{roomId}/admins/{uid}  // Remove admin
GET    /room/v1/rooms/{roomId}/users        // List room users
POST   /room/v1/rooms/{roomId}/kick/{uid}   // Kick user from room
```

### 2.3 **User Service** (Port: 8003)
**Responsibilities:**
- User profile management
- User presence tracking
- User permissions within rooms
- User metadata & preferences

**Key Entities:**
```java
// RoomUser (Active User in Room)
- roomUserId
- roomId
- userId
- appId
- displayName
- role: OWNER, ADMIN, SPEAKER, LISTENER
- joinedAt
- leftAt (nullable)
- mediaState: {
    audioEnabled: true,
    videoEnabled: false,
    screenshareActive: false,
    whiteboardActive: false
  }
- isSpeaker: boolean
- isMuted: boolean
- isHandRaised: boolean

// UserPermission
- userId
- roomId
- canMute: boolean
- canStartVideo: boolean
- canScreenShare: boolean
- canWhiteboard: boolean
- canChat: boolean
```

**API Endpoints:**
```
GET    /user/v1/users/{userId}                    // Get user profile
PUT    /user/v1/users/{userId}                    // Update profile
GET    /user/v1/users/{userId}/rooms              // List user's rooms
GET    /user/v1/rooms/{roomId}/users              // List room users
POST   /user/v1/rooms/{roomId}/users/{userId}/media // Update media state
POST   /user/v1/rooms/{roomId}/users/{userId}/hand // Raise/lower hand
```

### 2.4 **Media Service** (Port: 8004)
**Responsibilities:**
- MediaSoup server orchestration
- Media stream management
- Producer/Consumer handling
- Bitrate control & optimization
- SFU routing logic

**Key Entities:**
```java
// MediaRoom (MediaSoup Router)
- roomId
- mediasoupRouterId
- status: ACTIVE, INACTIVE
- createdAt

// MediaStream
- streamId
- roomId
- userId
- kind: AUDIO, VIDEO
- mediaType: CAMERA, SCREEN, WHITEBOARD
- producerId (MediaSoup)
- active: boolean
- bitrate: int
- codec: string

// AudioSpeaker (Limited to 10)
- roomId
- userId
- streamId
- priority: int (0-10)
- addedAt
```

**WebSocket Events:**
```
CONNECT              // User joins room
DISCONNECT           // User leaves room
PRODUCER_ADDED       // New media stream
PRODUCER_REMOVED     // Stream ended
CONSUMER_ADDED       // User receives stream
CONSUMER_REMOVED     // Stop receiving stream
AUDIO_SPEAKER_UPDATED// 10 speakers changed
VIDEO_QUALITY_CHANGED// Bitrate adjusted
CHAT_MESSAGE         // New message
```

**API Endpoints:**
```
POST   /media/v1/rooms/{roomId}/join          // Get join credentials
POST   /media/v1/rooms/{roomId}/transport    // Create WebRTC transport
POST   /media/v1/producers                    // Create producer
POST   /media/v1/consumers                    // Create consumer
POST   /media/v1/speakers/{roomId}            // Update top 10 speakers
GET    /media/v1/rooms/{roomId}/stats        // Get room stats
```

### 2.5 **Usage Service** (Port: 8005)
**Responsibilities:**
- Track user minutes per room
- Track minutes per user
- Track total app minutes
- Generate usage reports
- Calculate billing

**Key Entities:**
```java
// UserMinutes
- id
- appId
- userId
- roomId
- startTime
- endTime
- durationMinutes: float
- mediaTypes: [AUDIO, VIDEO, SCREEN_SHARE]
- recordedAt

// AppUsageMetrics (Aggregated)
- appId
- date
- totalActiveUsers: int
- totalRoomMinutes: float
- totalAudioMinutes: float
- totalVideoMinutes: float
- totalScreenShareMinutes: float
- peakConcurrentUsers: int
- averageRoomDuration: float

// UserUsageMetrics
- userId
- appId
- month
- totalMinutes: float
- totalAudioMinutes: float
- totalVideoMinutes: float
- roomsJoined: int
```

**API Endpoints:**
```
GET    /usage/v1/apps/{appId}/metrics        // App usage metrics
GET    /usage/v1/apps/{appId}/users/{userId}/usage  // User usage
GET    /usage/v1/apps/{appId}/billing        // Billing data
POST   /usage/v1/track                       // Record usage event
GET    /usage/v1/reports/{appId}             // Generate usage report
```

---

## 3. **Real-Time Features Implementation**

### 3.1 **Audio System**
```
Architecture:
┌─────────────────────────────────────────────┐
│         10 Active Speakers (Producers)      │
└─────────────────────────────────────────────┘
                     │
         ┌───────────┼───────────┐
         │           │           │
    [Listener 1] [Listener 2] [Listener N]
    
- Auto-select top 10 speakers by loudness
- All listeners receive mixed audio from 10 speakers
- Other speakers' audio muted locally (can raise hand)
- Update speakers list every 2-3 seconds based on activity
```

**Implementation:**
```javascript
// MediaSoup SFU Logic
- One Router per room
- One Transport per user
- One Producer per user (audio)
- One Consumer per listener (receives mixed audio from speakers)
- Use RTC Statistics for loudness detection
- Implement audio mixing at MediaSoup pipe/router level
```

### 3.2 **Video System**
```
Architecture:
┌─────────────────────────────────────────────┐
│      Multiple Video Producers (Unlimited)   │
└─────────────────────────────────────────────┘
                     │
         ┌───────────┼───────────┐
         │           │           │
    [User 1]     [User 2]     [User N]
    
- Each user can enable/disable video independently
- Multiple screen shares supported
- Adaptive bitrate based on network
- Simulcast for quality selection
```

### 3.3 **Chat System**
```javascript
// Real-time chat with WebSocket
- XMPP or Socket.IO for messaging
- Message persistence in MongoDB
- Emoji support
- Message reactions
- Typing indicators
```

### 3.4 **Hand Raise System**
```javascript
{
  roomId,
  userId,
  raisedAt,
  loweredAt,
  priority (auto-calculated by server)
}

// WebSocket event: USER_HAND_RAISED
// Admin action: ACCEPT_HAND -> Grant microphone
// Admin action: REJECT_HAND -> Clear from queue
```

### 3.5 **Whiteboard**
```
- Use collaborative library (Excalidraw API or Fabric.js)
- One whiteboard instance per room
- Real-time sync via WebSocket
- Persist drawings in MongoDB
- Convert to PDF/image on demand
```

---

## 4. **Authentication & Authorization Flow**

### 4.1 **App Authentication**
```
Client Request:
POST /auth/v1/login
{
  "appId": "app_123",
  "appSecret": "secret_xyz"
}

Response:
{
  "appToken": "jwt_token",
  "expiresIn": 3600,
  "appId": "app_123"
}

// All subsequent API calls include:
Authorization: Bearer {appToken}
```

### 4.2 **User Authentication**
```
// Option 1: App provides user info
POST /auth/v1/users/register
{
  "appId": "app_123",
  "userId": "user_456",
  "email": "user@example.com",
  "name": "John Doe",
  "role": "USER",
  "metadata": { ... }
}

// Option 2: User authenticates directly
POST /auth/v1/users/login
{
  "appId": "app_123",
  "email": "user@example.com",
  "password": "password123"
}

Response:
{
  "userToken": "jwt_user_token",
  "userId": "user_456",
  "expiresIn": 86400
}

// All room-level operations include:
Authorization: Bearer {userToken}
X-App-Id: app_123
```

### 4.3 **Room Join Authorization**
```
1. User requests room access
   POST /room/v1/rooms/{roomId}/join
   Headers: {userToken, appId}

2. Room Service checks:
   - Is room active?
   - Is user authorized?
   - Is room full (>200)?
   - User permissions?

3. Media Service issues join token
   POST /media/v1/rooms/{roomId}/join-token
   Response: {
     "token": "join_token",
     "mediasoupUrl": "wss://media-server.example.com",
     "routerId": "router_123"
   }

4. Client connects to MediaSoup with token
```

---

## 5. **Database Schema (MongoDB)**

```javascript
// Collections Structure

// ============ AUTH SERVICE ============
db.applications
{
  _id: ObjectId,
  appId: string (unique),
  appSecret: string (hashed),
  appName: string,
  owner: ObjectId (ref: users),
  plan: "FREE" | "PROFESSIONAL" | "ENTERPRISE",
  limits: {
    maxRooms: 100,
    maxUsersPerRoom: 200,
    maxMinutesMonth: 10000
  },
  webhookUrl: string,
  createdAt: Date,
  updatedAt: Date
}

db.app_users
{
  _id: ObjectId,
  userId: string,
  appId: ObjectId (ref: applications),
  email: string,
  passwordHash: string,
  name: string,
  role: "ADMIN" | "MODERATOR" | "USER",
  permissions: [string],
  metadata: {},
  status: "ACTIVE" | "INACTIVE" | "SUSPENDED",
  createdAt: Date,
  updatedAt: Date
}

db.api_keys
{
  _id: ObjectId,
  appId: ObjectId (ref: applications),
  keyHash: string,
  name: string,
  permissions: [string],
  rateLimit: number,
  lastUsedAt: Date,
  createdAt: Date,
  expiresAt: Date
}

// ============ ROOM SERVICE ============
db.rooms
{
  _id: ObjectId,
  roomId: string (unique),
  appId: ObjectId (ref: applications),
  roomName: string,
  description: string,
  ownerId: ObjectId (ref: app_users),
  admins: [ObjectId (ref: app_users)],
  settings: {
    maxUsers: 200,
    audioEnabled: true,
    videoEnabled: true,
    screenshareEnabled: true,
    whiteboardEnabled: true,
    chatEnabled: true,
    recordingEnabled: false,
    allowedRoles: ["USER", "ADMIN"]
  },
  status: "ACTIVE" | "INACTIVE" | "ARCHIVED",
  createdAt: Date,
  updatedAt: Date,
  expiresAt: Date,
  password: string (optional, hashed)
}

db.room_admins
{
  _id: ObjectId,
  roomId: ObjectId (ref: rooms),
  userId: ObjectId (ref: app_users),
  permissions: {
    canKickUsers: true,
    canMuteUsers: true,
    canStartRecording: true,
    canManageWhiteboard: true,
    canManageAdmins: true
  },
  grantedBy: ObjectId (ref: app_users),
  grantedAt: Date
}

// ============ USER SERVICE ============
db.room_users
{
  _id: ObjectId,
  roomId: ObjectId (ref: rooms),
  userId: ObjectId (ref: app_users),
  displayName: string,
  role: "OWNER" | "ADMIN" | "SPEAKER" | "LISTENER",
  joinedAt: Date,
  leftAt: Date (nullable),
  mediaState: {
    audioEnabled: boolean,
    videoEnabled: boolean,
    screenshareActive: boolean,
    whiteboardActive: boolean
  },
  isSpeaker: boolean,
  isMuted: boolean,
  isHandRaised: boolean,
  handRaisedAt: Date (nullable),
  connectionStats: {
    latency: number,
    packetLoss: number,
    jitter: number
  }
}

// ============ MEDIA SERVICE ============
db.media_rooms
{
  _id: ObjectId,
  roomId: ObjectId (ref: rooms),
  mediasoupRouterId: string,
  status: "ACTIVE" | "INACTIVE",
  createdAt: Date,
  closedAt: Date
}

db.media_streams
{
  _id: ObjectId,
  streamId: string,
  roomId: ObjectId (ref: rooms),
  userId: ObjectId (ref: room_users),
  kind: "AUDIO" | "VIDEO",
  mediaType: "CAMERA" | "SCREEN" | "WHITEBOARD",
  producerId: string (MediaSoup),
  active: boolean,
  bitrate: number,
  codec: string,
  createdAt: Date,
  closedAt: Date
}

db.audio_speakers
{
  _id: ObjectId,
  roomId: ObjectId (ref: rooms),
  speakers: [
    {
      userId: ObjectId (ref: app_users),
      priority: number (0-10),
      volume: number (-80 to 0 dB),
      addedAt: Date
    }
  ],
  updatedAt: Date
}

// ============ USAGE SERVICE ============
db.user_session_minutes
{
  _id: ObjectId,
  appId: ObjectId (ref: applications),
  userId: ObjectId (ref: app_users),
  roomId: ObjectId (ref: rooms),
  startTime: Date,
  endTime: Date,
  durationMinutes: number,
  mediaTypes: ["AUDIO", "VIDEO"],
  recordedAt: Date
}

db.app_usage_daily
{
  _id: ObjectId,
  appId: ObjectId (ref: applications),
  date: Date,
  totalActiveUsers: number,
  totalRoomMinutes: number,
  totalAudioMinutes: number,
  totalVideoMinutes: number,
  totalScreenShareMinutes: number,
  peakConcurrentUsers: number,
  recordedAt: Date
}

db.user_usage_monthly
{
  _id: ObjectId,
  appId: ObjectId (ref: applications),
  userId: ObjectId (ref: app_users),
  month: Date,
  totalMinutes: number,
  totalAudioMinutes: number,
  totalVideoMinutes: number,
  roomsJoined: number,
  recordedAt: Date
}
```

---

## 6. **Scaling Strategy**

### 6.1 **Horizontal Scaling**
```
Multiple MediaSoup Instances:
┌──────────────────────────────────────┐
│         API Gateway / Load Balancer   │
└──────────────────────────────────────┘
    │           │           │
    ▼           ▼           ▼
┌─────────┐ ┌─────────┐ ┌─────────┐
│MediaSoup│ │MediaSoup│ │MediaSoup│
│Instance1│ │Instance2│ │Instance3│
└─────────┘ └─────────┘ └─────────┘

Each instance handles ~50-100 concurrent rooms
Use Redis for session state sharing
```

### 6.2 **Database Optimization**
```
Indexes:
- applications: appId (unique)
- app_users: (appId, userId, email)
- rooms: (appId, roomId, status)
- room_users: (roomId, userId, joinedAt)
- user_session_minutes: (appId, userId, date)
- media_streams: (roomId, kind)

Sharding Strategy:
- Shard by appId for multi-tenancy isolation
- Shard user_session_minutes by date + appId
```

### 6.3 **Cache Layer (Redis)**
```
Cache Patterns:
- Room metadata: room:{roomId} (TTL: 1 hour)
- Active users: room:users:{roomId} (TTL: 5 min)
- Audio speakers: speakers:{roomId} (TTL: 5 sec)
- User permissions: user:perms:{userId}:{roomId} (TTL: 30 min)
- API rate limits: ratelimit:{appId}:{endpoint}

Real-time pub/sub:
- room:events:{roomId}
- app:events:{appId}
- user:events:{userId}
```

---

## 7. **Billing & Usage Tracking**

### 7.1 **Pricing Model**
```
Option 1: Per-Minute Billing
- Audio: $0.01 per user minute
- Video: $0.02 per user minute
- Screen Share: $0.015 per user minute

Option 2: Tiered Plan
- FREE: 100 room minutes/month
- PROFESSIONAL: 10,000 room minutes/month
- ENTERPRISE: Custom

Option 3: Hybrid
- Included minutes + overage charges
```

### 7.2 **Usage Tracking Flow**
```
1. User joins room
   ▼
2. Create session record with startTime
   ▼
3. Every 30 seconds: update usage metrics
   ▼
4. User leaves room
   ▼
5. Calculate endTime, duration, cost
   ▼
6. Record in user_session_minutes
   ▼
7. Aggregate to monthly_usage
   ▼
8. Generate invoice
```

---

## 8. **Real-time Events (WebSocket)**

```javascript
// Connection
wss://api.example.com/ws?token={userToken}&roomId={roomId}

// Client → Server Events
{
  "type": "JOIN_ROOM",
  "data": { roomId, userId }
}

{
  "type": "MEDIA_STATE_CHANGE",
  "data": { audioEnabled: true, videoEnabled: false }
}

{
  "type": "SEND_MESSAGE",
  "data": { text: "Hello", emoji: "👋" }
}

{
  "type": "RAISE_HAND",
  "data": { userId }
}

// Server → Client Events
{
  "type": "USER_JOINED",
  "data": { userId, userName, joinedAt }
}

{
  "type": "PRODUCER_ADDED",
  "data": { streamId, userId, kind: "AUDIO" | "VIDEO" }
}

{
  "type": "SPEAKERS_UPDATED",
  "data": { speakers: [{userId, priority}] }
}

{
  "type": "HAND_RAISED",
  "data": { userId, priority }
}

{
  "type": "MESSAGE",
  "data": { userId, text, emoji, timestamp }
}

{
  "type": "USER_LEFT",
  "data": { userId, reason: "DISCONNECT" | "KICK" }
}

{
  "type": "ROOM_CLOSED",
  "data": { roomId, reason }
}
```

---

## 9. **Security Best Practices**

### 9.1 **API Security**
```
✓ Use HTTPS/WSS only
✓ JWT with short expiration (15-30 min)
✓ Refresh token rotation
✓ API key hashing (bcrypt)
✓ Rate limiting per app/user
✓ CORS configuration
✓ CSRF protection for web clients
```

### 9.2 **Data Security**
```
✓ Encrypt sensitive data at rest (MongoDB encryption)
✓ Password hashing (bcrypt, 12 rounds)
✓ Secrets in environment variables
✓ Database access control
✓ Audit logging
```

### 9.3 **WebRTC Security**
```
✓ DTLS-SRTP for media encryption
✓ TURN server with authentication
✓ ICE restart on connection loss
✓ Media stream validation
```

---

## 10. **Monitoring & Analytics**

### 10.1 **Metrics to Track**
```
System Health:
- Room creation rate
- Active rooms count
- Concurrent users
- Connection failure rate
- Media stream quality

Business Metrics:
- Total revenue
- MRR (Monthly Recurring Revenue)
- Usage by plan
- Customer churn rate
- Feature adoption
```

### 10.2 **Logging Strategy**
```
- ELK Stack (Elasticsearch, Logstash, Kibana)
- Structured logging (JSON format)
- Log levels: ERROR, WARN, INFO, DEBUG
- Trace IDs for request tracking
- Performance logging (slow queries)
```

---

## 11. **Deployment Strategy**

### 11.1 **Docker Compose Structure**
```
services:
  - api-gateway (Spring Cloud Gateway)
  - auth-service (Port 8001)
  - room-service (Port 8002)
  - user-service (Port 8003)
  - media-service (Port 8004)
  - usage-service (Port 8005)
  - mongodb
  - redis
  - mediasoup (Node.js)
  - admin-dashboard (React)
  - nginx (Reverse proxy)

Volume management for persistent data
Network isolation between services
Health checks for all services
```

---

## 12. **Development Roadmap**

### **Phase 1: MVP (2-4 weeks)**
- [ ] Auth Service + MongoDB setup
- [ ] Room Service basic CRUD
- [ ] MediaSoup integration
- [ ] Basic audio/video
- [ ] Simple chat
- [ ] Usage tracking

### **Phase 2: Core Features (4-6 weeks)**
- [ ] 10 speakers system
- [ ] Screen sharing
- [ ] Hand raise system
- [ ] Admin controls
- [ ] Usage reports
- [ ] Admin Dashboard (React)

### **Phase 3: Advanced Features (6-8 weeks)**
- [ ] Whiteboard
- [ ] Recording
- [ ] Breakout rooms
- [ ] Webhooks
- [ ] SDKs (Flutter, Web, Mobile)
- [ ] Advanced analytics

### **Phase 4: Scale & Polish (Ongoing)**
- [ ] Load testing
- [ ] Performance optimization
- [ ] Multi-region support
- [ ] Advanced monitoring
- [ ] Customer support features

---

## 13. **Testing Strategy**

### 13.1 **Unit Testing**
```
- Spring Boot: JUnit 5 + Mockito
- React: Jest + React Testing Library
- Node.js: Mocha + Chai
```

### 13.2 **Integration Testing**
```
- Database tests (Testcontainers)
- API endpoint tests
- WebSocket tests
```

### 13.3 **Load Testing**
```
- JMeter for API load tests
- Concurrent user simulation (200+)
- Media stream quality tests
- Database stress tests
```

### 13.4 **E2E Testing**
```
- Flutter test app scenarios
- Multi-user room scenarios
- Failover testing
```

---

## 14. **API Reference Examples**

### Create Application
```bash
curl -X POST https://api.example.com/auth/v1/apps/register \
  -H "Content-Type: application/json" \
  -d {
    "appName": "MyApp",
    "owner": "admin@example.com"
  }

Response:
{
  "appId": "app_abc123",
  "appSecret": "secret_xyz789",
  "appToken": "eyJhbGc...",
  "createdAt": "2024-01-15T10:30:00Z"
}
```

### Create Room
```bash
curl -X POST https://api.example.com/room/v1/rooms \
  -H "Authorization: Bearer {appToken}" \
  -H "Content-Type: application/json" \
  -d {
    "roomName": "Team Meeting",
    "maxUsers": 200,
    "settings": {
      "audioEnabled": true,
      "videoEnabled": true,
      "screenshareEnabled": true
    }
  }

Response:
{
  "roomId": "room_123",
  "roomName": "Team Meeting",
  "ownerId": "user_456",
  "status": "ACTIVE",
  "createdAt": "2024-01-15T11:00:00Z"
}
```

### Join Room
```bash
curl -X POST https://api.example.com/media/v1/rooms/room_123/join \
  -H "Authorization: Bearer {userToken}" \
  -H "X-App-Id: app_abc123" \
  -H "Content-Type: application/json" \
  -d {
    "userId": "user_456",
    "displayName": "John Doe"
  }

Response:
{
  "token": "join_token_xyz",
  "mediasoupUrl": "wss://media.example.com",
  "routerId": "router_abc",
  "existingProducers": [...]
}
```

---

## 15. **Conclusion**

This system prompt provides a comprehensive blueprint for building a production-grade SaaS platform like Agora. Key differentiators:

✅ **Scalability**: Support 200+ users per room with selective audio forwarding  
✅ **Multi-tenancy**: Complete isolation between applications  
✅ **Billing-ready**: Minute-level usage tracking for accurate billing  
✅ **Flexible**: MediaSoup for maximum control, LiveKit option for managed alternative  
✅ **Secure**: JWT auth, API key validation, encrypted media streams  
✅ **Observable**: Comprehensive logging, metrics, and usage analytics  

Start with Phase 1 MVP, validate with Flutter test app, then progressively add advanced features.
