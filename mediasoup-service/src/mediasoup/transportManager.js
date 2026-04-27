const logger = require('../utils/logger');

class TransportManager {
  /**
   * Create WebRTC transport
   */
  async createTransport(router, peerId) {
    try {
      const transport = await router.createWebRtcTransport({
        listenIps: [
          {
            ip: process.env.WEBRTC_LISTEN_IPS || '0.0.0.0',
            announcedIp: process.env.WEBRTC_ANNOUNCE_IP || 'auto',
          },
        ],
        enableUdp: true,
        enableTcp: true,
        preferUdp: true,
        maxIncomingBitrate: 5000000, // 5 Mbps
        maxOutgoingBitrate: 5000000,
        iceServers: [
          {
            urls: ['stun:stun.l.google.com:19302'],
          },
          // Add TURN server if configured
          ...(process.env.TURN_SERVER ? [
            {
              urls: [`turn:${process.env.TURN_SERVER}`],
              username: process.env.TURN_USERNAME,
              credential: process.env.TURN_PASSWORD,
              credentialType: 'password',
            },
          ] : []),
        ],
      });

      transport.on('dtlsstatechange', (dtlsState) => {
        if (dtlsState === 'failed' || dtlsState === 'closed') {
          logger.warn(`Transport ${transport.id} DTLS state changed to ${dtlsState}`);
          transport.close();
        }
      });

      logger.info(`Transport created: ${transport.id} for peer ${peerId}`);
      return transport;
    } catch (error) {
      logger.error(`Failed to create transport:`, error);
      throw error;
    }
  }

  /**
   * Connect transport with client parameters
   */
  async connectTransport(transport, dtlsParameters) {
    try {
      await transport.connect({ dtlsParameters });
      logger.info(`Transport connected: ${transport.id}`);
    } catch (error) {
      logger.error(`Failed to connect transport:`, error);
      throw error;
    }
  }

  /**
   * Close transport
   */
  closeTransport(transport) {
    if (transport) {
      transport.close();
      logger.info(`Transport closed: ${transport.id}`);
    }
  }

  /**
   * Get transport stats
   */
  async getTransportStats(transport) {
    try {
      const stats = await transport.getStats();
      return stats;
    } catch (error) {
      logger.error(`Failed to get transport stats:`, error);
      return null;
    }
  }
}

module.exports = new TransportManager();
