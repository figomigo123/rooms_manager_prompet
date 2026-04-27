# MediaSoup Service - WebRTC Media Server

High-performance Node.js WebRTC media server using MediaSoup for handling audio/video streaming, screen sharing, and supporting 200+ users per room with 10-speaker audio system.

## Features

- ✅ **WebRTC SFU (Selective Forwarding Unit)**
- ✅ **10-Speaker Audio System** - Automatically selects top 10 speakers
- ✅ **Simulcast Support** - Multiple video quality layers
- ✅ **Screen Sharing** - Full-resolution screen capture
- ✅ **Audio/Video Management**
  - Mute/Unmute control
  - Bitrate adaptation
  - Codec selection (VP8, VP9, H264 for video, Opus for audio)
- ✅ **200+ Users Per Room**
- ✅ **Real-time Statistics**
- ✅ **Redis Caching**
- ✅ **Docker Support**

## Installation

```bash
cd mediasoup-service
npm install
```

## Configuration

### Environment Variables

Create `.env` file (or use `.env.example`):

```bash
PORT=8003
NODE_ENV=development

# MediaSoup
MEDIASOUP_WORKER_PROCESSES=4
MEDIASOUP_WORKER_RTC_PORT_RANGE_START=20000
MEDIASOUP_WORKER_RTC_PORT_RANGE_END=30000

# WebRTC
WEBRTC_ANNOUNCE_IP=auto
WEBRTC_LISTEN_IPS=0.0.0.0

# Redis
REDIS_HOST=redis
REDIS_PORT=6379
REDIS_DB=1

# Auth Service
AUTH_SERVICE_URL=http://auth-service:8001
AUTH_VERIFY_ENDPOINT=/auth/v1/verify-token

# Room Service
ROOM_SERVICE_URL=http://room-service:8002

# Audio/Video
MAX_AUDIO_SPEAKERS=10
AUDIO_CODECS=opus
VIDEO_CODECS=vp8,vp9,h264
DEFAULT_VIDEO_BITRATE=500000

# TURN Server (optional)
TURN_SERVER=turn.example.com
TURN_USERNAME=username
TURN_PASSWORD=password
```

## Development

```bash
# Install dependencies
npm install

# Start with auto-reload
npm run dev

# Start production
npm start

# Run tests
npm test
```

## Docker

### Build
```bash
docker build -t mediasoup-service:latest .
```

### Run
```bash
docker run -d \
  --name mediasoup \
  -p 8003:8003 \
  -p 20000-30000:20000-30000/udp \
  -p 20000-30000:20000-30000/tcp \
  --env-file .env \
  mediasoup-service:latest
```

## WebSocket Events

### Client → Server

#### join-room
Join a room with peer information
```javascript
socket.emit('join-room', {
  roomId: 'room_abc123',
  peerId: 'peer_xyz',
  displayName: 'John Doe',
  rtpCapabilities: {...}
}, (response) => {
  if (response.success) {
    // Connected to room
  }
});
```

#### create-producer-transport
Create transport for sending media
```javascript
socket.emit('create-producer-transport', {
  roomId: 'room_abc123',
  peerId: 'peer_xyz'
}, (response) => {
  // response.transport contains ICE candidates and DTLS parameters
});
```

#### create-consumer-transport
Create transport for receiving media
```javascript
socket.emit('create-consumer-transport', {
  roomId: 'room_abc123',
  peerId: 'peer_xyz'
}, (response) => {
  // response.transport contains ICE candidates and DTLS parameters
});
```

### Server → Client

#### peer-joined
New peer joined the room
```javascript
socket.on('peer-joined', (data) => {
  console.log(`${data.displayName} joined - Total: ${data.totalPeers}`);
});
```

#### peer-left
Peer left the room
```javascript
socket.on('peer-left', (data) => {
  console.log(`Peer left - Total: ${data.totalPeers}`);
});
```

## Architecture

```
MediaSoup Service (Port 8003)
├── Worker Pool (4 workers by default)
│   ├── Worker 1 (Router)
│   ├── Worker 2 (Router)
│   ├── Worker 3 (Router)
│   └── Worker 4 (Router)
├── Room Manager
│   ├── Room State
│   ├── Peers
│   ├── Producers
│   └── Consumers
├── Audio Manager (10-speaker selection)
├── Video Manager (Simulcast, bitrate adaptation)
├── Transport Manager (WebRTC transports)
└── Producer/Consumer Manager
```

## 10-Speaker Audio System

Automatically selects and prioritizes the 10 most active speakers:

```javascript
// Audio levels are monitored continuously
const topSpeakers = audioManager.calculateActiveSpeakers(room);
// Returns top 10 speakers sorted by volume
```

## Simulcast Layers

Video is encoded in 3 layers for adaptive quality:

```
Layer 0 (Low):    100 kbps @ 15fps (4x downscaled)
Layer 1 (Medium): 300 kbps @ 20fps (2x downscaled)
Layer 2 (High):   900 kbps @ 30fps (Full resolution)
```

## Performance Optimization

- ✅ **Worker Pool** - Distribute load across multiple workers
- ✅ **Producer/Consumer Pausing** - Pause inactive streams
- ✅ **Bitrate Adaptation** - Adjust quality based on bandwidth
- ✅ **Spatial/Temporal Layers** - SVC for VP9, H264
- ✅ **RTCP Feedback** - Real-time quality monitoring

## REST API

### Health Check
```bash
GET /health
```

### Room Statistics
```bash
GET /api/v1/stats/rooms/:roomId
Authorization: Bearer {token}
```

Response:
```json
{
  "roomId": "room_abc123",
  "totalPeers": 45,
  "totalProducers": 45,
  "totalConsumers": 1980,
  "peakUsers": 50,
  "activeSpeakers": 10,
  "createdAt": "2024-01-15T10:30:00Z"
}
```

## File Structure

```
mediasoup-service/
├── src/
│   ├── index.js                          # Main server file
│   ├── config/
│   │   └── index.js                      # Configuration
│   ├── mediasoup/
│   │   ├── workerPool.js                 # Worker pool management
│   │   ├── roomManager.js                # Room state management
│   │   ├── transportManager.js           # WebRTC transports
│   │   ├── producerConsumerManager.js    # Media producers/consumers
│   │   ├── audioManager.js               # Audio processing (10-speaker)
│   │   ├── videoManager.js               # Video processing
│   │   └── service.js                    # MediaSoup service
│   ├── routes/
│   │   └── stats.js                      # Statistics endpoints
│   ├── middleware/
│   │   └── auth.js                       # Authentication
│   └── utils/
│       ├── logger.js                     # Logging
│       ├── redis.js                      # Redis client
│       ├── authClient.js                 # Auth service client
│       └── validation.js                 # Input validation
├── Dockerfile
├── package.json
├── .env
├── .env.example
├── .gitignore
└── README.md
```

## Testing

```bash
# Unit tests
npm test

# Load testing
# Use tools like: Apache JMeter, k6, or custom scripts
```

## Troubleshooting

### Port Range Conflicts
If RTC ports are in use, change the range:
```bash
MEDIASOUP_WORKER_RTC_PORT_RANGE_START=30000
MEDIASOUP_WORKER_RTC_PORT_RANGE_END=40000
```

### Redis Connection Issues
```bash
# Check Redis
redis-cli ping

# Restart Redis
docker-compose restart redis
```

### WebRTC Connection Issues
- Ensure firewall allows UDP ports 20000-30000
- Check NAT/firewall with TURN server
- Verify STUN server is accessible

## License

MIT
