const express = require('express');
const cors = require('cors');
const http = require('http');
const socketIO = require('socket.io');
const logger = require('./utils/logger');
const config = require('./config');
const mediasoupService = require('./mediasoup/service');
const roomManager = require('./mediasoup/roomManager');
const transportManager = require('./mediasoup/transportManager');
const producerConsumerManager = require('./mediasoup/producerConsumerManager');
const audioManager = require('./mediasoup/audioManager');
const { verifyToken } = require('./middleware/auth');
const statsRoutes = require('./routes/stats');

const app = express();
const server = http.createServer(app);
const io = socketIO(server, {
  cors: {
    origin: config.server.corsOrigins,
    methods: ['GET', 'POST'],
  },
});

// Middleware
app.use(cors({ origin: config.server.corsOrigins }));
app.use(express.json());

// Routes
app.use('/api/v1/stats', statsRoutes);

// Health check
app.get('/health', (req, res) => {
  res.json({
    status: 'UP',
    service: 'mediasoup-service',
    timestamp: new Date().toISOString(),
  });
});

// WebSocket connection handling
io.use((socket, next) => {
  const token = socket.handshake.auth.token;
  if (!token) {
    return next(new Error('Missing authentication token'));
  }
  socket.token = token;
  next();
});

io.on('connection', (socket) => {
  logger.info(`Socket connected: ${socket.id}`);

  socket.on('join-room', async (data, callback) => {
    try {
      const { roomId, peerId, displayName, rtpCapabilities } = data;
      logger.info(`Peer ${peerId} joining room ${roomId}`);

      // Create room if not exists
      let room;
      try {
        room = roomManager.getRoom(roomId);
      } catch (error) {
        room = await mediasoupService.createRoom(roomId);
      }

      // Join peer to room
      const peer = await mediasoupService.joinRoom(roomId, peerId, { displayName, rtpCapabilities });

      // Get router capabilities
      const capabilities = mediasoupService.getRouterCapabilities(room.router);

      socket.join(roomId);
      socket.roomId = roomId;
      socket.peerId = peerId;

      // Notify others about new peer
      socket.to(roomId).emit('peer-joined', {
        peerId,
        displayName,
        totalPeers: room.peers.size,
      });

      callback({
        success: true,
        routerCapabilities: capabilities.rtpCapabilities,
        peers: Array.from(room.peers.values()).map(p => ({
          peerId: p.peerId,
          displayName: p.displayName,
        })),
      });
    } catch (error) {
      logger.error('Error joining room:', error);
      callback({ success: false, error: error.message });
    }
  });

  socket.on('create-producer-transport', async (data, callback) => {
    try {
      const { roomId, peerId } = data;
      const transport = await mediasoupService.createSendTransport(roomId, peerId);

      callback({
        success: true,
        transport,
      });
    } catch (error) {
      logger.error('Error creating producer transport:', error);
      callback({ success: false, error: error.message });
    }
  });

  socket.on('create-consumer-transport', async (data, callback) => {
    try {
      const { roomId, peerId } = data;
      const transport = await mediasoupService.createReceiveTransport(roomId, peerId);

      callback({
        success: true,
        transport,
      });
    } catch (error) {
      logger.error('Error creating consumer transport:', error);
      callback({ success: false, error: error.message });
    }
  });

  socket.on('disconnect', () => {
    const roomId = socket.roomId;
    const peerId = socket.peerId;

    if (roomId && peerId) {
      try {
        roomManager.removePeer(roomId, peerId);
        logger.info(`Peer ${peerId} disconnected from room ${roomId}`);

        socket.to(roomId).emit('peer-left', {
          peerId,
          totalPeers: roomManager.getRoom(roomId).peers.size,
        });
      } catch (error) {
        logger.error('Error handling disconnect:', error);
      }
    }

    logger.info(`Socket disconnected: ${socket.id}`);
  });
});

// Initialize and start server
async function start() {
  try {
    await mediasoupService.initialize();

    const port = config.server.port;
    server.listen(port, () => {
      logger.info(`MediaSoup service running on port ${port}`);
    });
  } catch (error) {
    logger.error('Failed to start MediaSoup service:', error);
    process.exit(1);
  }
}

// Graceful shutdown
process.on('SIGTERM', async () => {
  logger.info('SIGTERM received, shutting down gracefully...');
  await mediasoupService.close();
  server.close(() => {
    logger.info('Server closed');
    process.exit(0);
  });
});

start();

module.exports = app;
