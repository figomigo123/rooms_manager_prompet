const logger = require('../utils/logger');

class VideoManager {
  /**
   * Configure simulcast for video producer
   */
  async configureSimulcast(transport, rtpParameters) {
    // Simulcast with 3 layers: low, medium, high
    const encodings = [
      {
        rid: 'r0',
        maxBitrate: 100000, // 100 kbps
        maxFramerate: 15,
        scaleResolutionDownBy: 4.0,
      },
      {
        rid: 'r1',
        maxBitrate: 300000, // 300 kbps
        maxFramerate: 20,
        scaleResolutionDownBy: 2.0,
      },
      {
        rid: 'r2',
        maxBitrate: 900000, // 900 kbps
        maxFramerate: 30,
        scaleResolutionDownBy: 1.0,
      },
    ];

    // Add RTCP feedback for quality adaptation
    const rtcp = {
      cname: rtpParameters.rtcp?.cname || 'video-rtcp',
      reducedSize: true,
      mux: true,
    };

    return { encodings, rtcp };
  }

  /**
   * Get screen share bitrate (higher than regular video)
   */
  getScreenShareBitrate() {
    return {
      encodings: [
        {
          maxBitrate: 500000, // 500 kbps for low
        },
        {
          maxBitrate: 1500000, // 1.5 Mbps for medium
        },
        {
          maxBitrate: 3000000, // 3 Mbps for high
        },
      ],
    };
  }

  /**
   * Update video bitrate based on network conditions
   */
  async updateVideoBitrate(consumer, availableBitrate) {
    try {
      // Dynamically set preferred spatial and temporal layers
      if (availableBitrate < 300000) {
        // Low quality
        await consumer.setPreferredLayers({
          spatialLayer: 0,
          temporalLayer: 0,
        });
        logger.debug(`Consumer ${consumer.id} set to low quality`);
      } else if (availableBitrate < 800000) {
        // Medium quality
        await consumer.setPreferredLayers({
          spatialLayer: 1,
          temporalLayer: 1,
        });
        logger.debug(`Consumer ${consumer.id} set to medium quality`);
      } else {
        // High quality
        await consumer.setPreferredLayers({
          spatialLayer: 2,
          temporalLayer: 2,
        });
        logger.debug(`Consumer ${consumer.id} set to high quality`);
      }
    } catch (error) {
      logger.error(`Failed to update video bitrate:`, error);
    }
  }

  /**
   * Resume video producer
   */
  async resumeVideo(producer) {
    try {
      if (producer.paused) {
        await producer.resume();
        logger.info(`Video resumed for producer ${producer.id}`);
      }
    } catch (error) {
      logger.error(`Failed to resume video:`, error);
    }
  }

  /**
   * Pause video producer
   */
  async pauseVideo(producer) {
    try {
      if (!producer.paused) {
        await producer.pause();
        logger.info(`Video paused for producer ${producer.id}`);
      }
    } catch (error) {
      logger.error(`Failed to pause video:`, error);
    }
  }

  /**
   * Get video stats
   */
  async getVideoStats(consumer) {
    try {
      const stats = await consumer.getStats();
      return stats.map(stat => ({
        type: stat.type,
        bytesReceived: stat.bytesReceived,
        framesDecoded: stat.framesDecoded,
        framesDropped: stat.framesDropped,
        jitter: stat.jitter,
        roundTripTime: stat.roundTripTime,
      }));
    } catch (error) {
      logger.error(`Failed to get video stats:`, error);
      return null;
    }
  }
}

module.exports = new VideoManager();
