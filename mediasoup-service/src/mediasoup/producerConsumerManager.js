const logger = require('../utils/logger');

class ProducerConsumerManager {
  /**
   * Create producer
   */
  async createProducer(transport, { kind, rtpParameters, paused = false }) {
    try {
      const producer = await transport.produce({
        kind,
        rtpParameters,
        paused,
      });

      logger.info(`Producer created: ${producer.id} (${kind})`);

      producer.on('score', (score) => {
        logger.debug(`Producer ${producer.id} score:`, score);
      });

      return producer;
    } catch (error) {
      logger.error(`Failed to create producer:`, error);
      throw error;
    }
  }

  /**
   * Create consumer
   */
  async createConsumer(router, consumerTransport, producer, peerId) {
    try {
      // Check if router can consume from this producer
      if (!router.canConsume({
        producerId: producer.id,
        rtpCapabilities: peerId.rtpCapabilities,
      })) {
        throw new Error('Cannot consume producer with this peer capabilities');
      }

      const consumer = await consumerTransport.consume({
        producerId: producer.id,
        rtpCapabilities: peerId.rtpCapabilities,
        paused: true, // Start paused
      });

      consumer.on('transportclose', () => {
        consumer.close();
        logger.info(`Consumer closed due to transport close: ${consumer.id}`);
      });

      logger.info(`Consumer created: ${consumer.id}`);
      return consumer;
    } catch (error) {
      logger.error(`Failed to create consumer:`, error);
      throw error;
    }
  }

  /**
   * Close producer
   */
  closeProducer(producer) {
    if (producer) {
      producer.close();
      logger.info(`Producer closed: ${producer.id}`);
    }
  }

  /**
   * Close consumer
   */
  closeConsumer(consumer) {
    if (consumer) {
      consumer.close();
      logger.info(`Consumer closed: ${consumer.id}`);
    }
  }

  /**
   * Pause producer
   */
  async pauseProducer(producer) {
    if (!producer.paused) {
      await producer.pause();
      logger.info(`Producer paused: ${producer.id}`);
    }
  }

  /**
   * Resume producer
   */
  async resumeProducer(producer) {
    if (producer.paused) {
      await producer.resume();
      logger.info(`Producer resumed: ${producer.id}`);
    }
  }

  /**
   * Pause consumer
   */
  async pauseConsumer(consumer) {
    if (!consumer.paused) {
      await consumer.pause();
      logger.info(`Consumer paused: ${consumer.id}`);
    }
  }

  /**
   * Resume consumer
   */
  async resumeConsumer(consumer) {
    if (consumer.paused) {
      await consumer.resume();
      logger.info(`Consumer resumed: ${consumer.id}`);
    }
  }

  /**
   * Set consumer preferred layers (for simulcast)
   */
  async setConsumerPreferredLayers(consumer, spatialLayer = 2, temporalLayer = 2) {
    try {
      await consumer.setPreferredLayers({
        spatialLayer,
        temporalLayer,
      });
      logger.info(`Consumer ${consumer.id} preferred layers set to ${spatialLayer}:${temporalLayer}`);
    } catch (error) {
      logger.error(`Failed to set consumer preferred layers:`, error);
    }
  }

  /**
   * Get producer stats
   */
  async getProducerStats(producer) {
    try {
      const stats = await producer.getStats();
      return stats;
    } catch (error) {
      logger.error(`Failed to get producer stats:`, error);
      return null;
    }
  }

  /**
   * Get consumer stats
   */
  async getConsumerStats(consumer) {
    try {
      const stats = await consumer.getStats();
      return stats;
    } catch (error) {
      logger.error(`Failed to get consumer stats:`, error);
      return null;
    }
  }
}

module.exports = new ProducerConsumerManager();
