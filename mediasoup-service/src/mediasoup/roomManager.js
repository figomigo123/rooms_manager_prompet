const logger = require('../utils/logger');
const { v4: uuidv4 } = require('uuid');

class RoomManager {
  constructor() {
    this.rooms = new Map(); // roomId -> RoomState
  }

  /**
   * Create room state
   */
  createRoom(roomId, router) {
    logger.info(`Creating room state for: ${roomId}`);

    const room = {
      roomId,
      router,
      peers: new Map(), // peerId -> Peer
      producers: new Map(), // producerId -> Producer
      consumers: new Map(), // consumerId -> Consumer
      activeSpeakers: [], // [{ peerId, volume, timestamp }]
      createdAt: new Date(),
      stats: {
        totalProducers: 0,
        totalConsumers: 0,
        peakUsers: 0,
      },
    };

    this.rooms.set(roomId, room);
    return room;
  }

  /**
   * Get room
   */
  getRoom(roomId) {
    if (!this.rooms.has(roomId)) {
      throw new Error(`Room not found: ${roomId}`);
    }
    return this.rooms.get(roomId);
  }

  /**
   * Delete room
   */
  deleteRoom(roomId) {
    logger.info(`Deleting room: ${roomId}`);
    const room = this.rooms.get(roomId);

    if (room) {
      // Close all consumers
      for (const consumer of room.consumers.values()) {
        consumer.close();
      }

      // Close all producers
      for (const producer of room.producers.values()) {
        producer.close();
      }

      // Clear peers
      room.peers.clear();
      room.producers.clear();
      room.consumers.clear();

      this.rooms.delete(roomId);
    }
  }

  /**
   * Add peer to room
   */
  addPeer(roomId, peerId, peerData) {
    const room = this.getRoom(roomId);

    const peer = {
      peerId,
      displayName: peerData.displayName || `User-${peerId.substring(0, 6)}`,
      producers: new Map(), // kind -> Producer
      consumers: new Map(), // producerId -> Consumer
      transport: null,
      joinedAt: new Date(),
      stats: {
        audioProducerId: null,
        videoProducerId: null,
        screenProducerId: null,
      },
    };

    room.peers.set(peerId, peer);
    logger.info(`Peer ${peerId} added to room ${roomId}`);

    // Update peak users
    if (room.peers.size > room.stats.peakUsers) {
      room.stats.peakUsers = room.peers.size;
    }

    return peer;
  }

  /**
   * Remove peer from room
   */
  removePeer(roomId, peerId) {
    const room = this.getRoom(roomId);
    const peer = room.peers.get(peerId);

    if (peer) {
      // Close all peer's producers
      for (const producer of peer.producers.values()) {
        producer.close();
        room.producers.delete(producer.id);
      }

      // Close all peer's consumers
      for (const consumer of peer.consumers.values()) {
        consumer.close();
        room.consumers.delete(consumer.id);
      }

      room.peers.delete(peerId);
      logger.info(`Peer ${peerId} removed from room ${roomId}`);
    }
  }

  /**
   * Get all peers in room
   */
  getPeers(roomId) {
    const room = this.getRoom(roomId);
    return Array.from(room.peers.values());
  }

  /**
   * Get peer
   */
  getPeer(roomId, peerId) {
    const room = this.getRoom(roomId);
    const peer = room.peers.get(peerId);

    if (!peer) {
      throw new Error(`Peer not found: ${peerId}`);
    }

    return peer;
  }

  /**
   * Update active speakers (10-speaker audio system)
   */
  updateActiveSpeakers(roomId, speakers) {
    const room = this.getRoom(roomId);
    room.activeSpeakers = speakers.slice(0, 10); // Limit to 10 speakers
  }

  /**
   * Get active speakers
   */
  getActiveSpeakers(roomId) {
    const room = this.getRoom(roomId);
    return room.activeSpeakers;
  }

  /**
   * Get room stats
   */
  getRoomStats(roomId) {
    const room = this.getRoom(roomId);
    return {
      roomId,
      totalPeers: room.peers.size,
      totalProducers: room.stats.totalProducers,
      totalConsumers: room.stats.totalConsumers,
      peakUsers: room.stats.peakUsers,
      activeSpeakers: room.activeSpeakers.length,
      createdAt: room.createdAt,
    };
  }
}

module.exports = new RoomManager();
