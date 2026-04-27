const logger = require('../utils/logger');
const config = require('../config');
const mediasoupWorkerPool = require('./workerPool');
const roomManager = require('./roomManager');
const transportManager = require('./transportManager');
const producerConsumerManager = require('./producerConsumerManager');
const audioManager = require('./audioManager');
const videoManager = require('./videoManager');

class MediaSoupService {
  /**
   * Initialize MediaSoup service
   */
  async initialize() {
    logger.info('Initializing MediaSoup service...');
    await mediasoupWorkerPool.initialize();
    logger.info('MediaSoup service initialized');
  }

  /**
   * Create room and its router
   */
  async createRoom(roomId) {
    try {
      logger.info(`Creating room: ${roomId}`);

      // Create router for the room
      const router = await mediasoupWorkerPool.createRouter(roomId);

      // Create room state
      const room = roomManager.createRoom(roomId, router);

      return room;
    } catch (error) {
      logger.error(`Failed to create room: ${error.message}`);
      throw error;
    }
  }

  /**
   * Get router capabilities
   */
  getRouterCapabilities(router) {
    return {
      rtpCapabilities: router.rtpCapabilities,
      mediaCodecs: config.mediasoup.mediaCodecs,
    };
  }

  /**
   * Join peer to room
   */
  async joinRoom(roomId, peerId, peerData) {
    try {
      logger.info(`Peer ${peerId} joining room ${roomId}`);

      const room = roomManager.getRoom(roomId);

      // Add peer to room
      const peer = roomManager.addPeer(roomId, peerId, peerData);

      return peer;
    } catch (error) {
      logger.error(`Failed to join room: ${error.message}`);
      throw error;
    }
  }

  /**
   * Create send transport for peer
   */
  async createSendTransport(roomId, peerId) {
    try {
      const room = roomManager.getRoom(roomId);
      const transport = await transportManager.createTransport(room.router, peerId);

      const peer = roomManager.getPeer(roomId, peerId);
      peer.transport = transport;

      return {
        id: transport.id,
        iceParameters: transport.iceParameters,
        iceCandidates: transport.iceCandidates,
        dtlsParameters: transport.dtlsParameters,
      };
    } catch (error) {
      logger.error(`Failed to create send transport: ${error.message}`);
      throw error;
    }
  }

  /**
   * Create receive transport for peer
   */
  async createReceiveTransport(roomId, peerId) {
    try {
      const room = roomManager.getRoom(roomId);
      const transport = await transportManager.createTransport(room.router, peerId);

      const peer = roomManager.getPeer(roomId, peerId);
      // You might want to store receive transport separately

      return {
        id: transport.id,
        iceParameters: transport.iceParameters,
        iceCandidates: transport.iceCandidates,
        dtlsParameters: transport.dtlsParameters,
      };
    } catch (error) {
      logger.error(`Failed to create receive transport: ${error.message}`);
      throw error;
    }
  }

  /**
   * Cleanup
   */
  async close() {
    logger.info('Closing MediaSoup service...');
    await mediasoupWorkerPool.close();
    logger.info('MediaSoup service closed');
  }
}

module.exports = new MediaSoupService();
