const mediasoup = require('mediasoup');
const Redis = require('redis');
const logger = require('./utils/logger');
const config = require('./config');

class MediaSoupWorkerPool {
  constructor() {
    this.workers = [];
    this.nextWorkerIndex = 0;
    this.routers = new Map(); // roomId -> router
  }

  /**
   * Initialize worker pool
   */
  async initialize() {
    logger.info(`Initializing MediaSoup worker pool with ${config.mediasoup.numWorkers} workers`);

    for (let i = 0; i < config.mediasoup.numWorkers; i++) {
      try {
        const worker = await mediasoup.createWorker({
          logLevel: config.mediasoup.logLevel,
          logTags: ['rtp', 'rtcp', 'rtx', 'bwe', 'score', 'simulcast', 'svc'],
          rtcMinPort: config.mediasoup.rtcMinPort,
          rtcMaxPort: config.mediasoup.rtcMaxPort,
        });

        worker.on('died', () => {
          logger.error(`MediaSoup worker ${i} died, restarting...`);
          this.handleWorkerDeath(i);
        });

        this.workers.push(worker);
        logger.info(`MediaSoup worker ${i} created`);
      } catch (error) {
        logger.error(`Failed to create MediaSoup worker ${i}:`, error);
      }
    }

    logger.info(`MediaSoup worker pool initialized with ${this.workers.length} workers`);
  }

  /**
   * Get next available worker (round-robin)
   */
  getWorker() {
    if (this.workers.length === 0) {
      throw new Error('No MediaSoup workers available');
    }

    const worker = this.workers[this.nextWorkerIndex];
    this.nextWorkerIndex = (this.nextWorkerIndex + 1) % this.workers.length;
    return worker;
  }

  /**
   * Create router for a room
   */
  async createRouter(roomId) {
    logger.info(`Creating router for room: ${roomId}`);

    if (this.routers.has(roomId)) {
      return this.routers.get(roomId);
    }

    const worker = this.getWorker();

    const router = await worker.createRouter({
      mediaCodecs: config.mediasoup.mediaCodecs,
    });

    // Handle router close
    router.on('close', () => {
      logger.info(`Router closed for room: ${roomId}`);
      this.routers.delete(roomId);
    });

    this.routers.set(roomId, router);
    logger.info(`Router created for room: ${roomId}`);

    return router;
  }

  /**
   * Get router for a room
   */
  getRouter(roomId) {
    if (!this.routers.has(roomId)) {
      throw new Error(`Router not found for room: ${roomId}`);
    }
    return this.routers.get(roomId);
  }

  /**
   * Handle worker death
   */
  async handleWorkerDeath(index) {
    logger.warn(`Handling death of worker ${index}`);
    // TODO: Implement worker restart logic
  }

  /**
   * Cleanup
   */
  async close() {
    logger.info('Closing MediaSoup worker pool');

    for (const router of this.routers.values()) {
      router.close();
    }

    for (const worker of this.workers) {
      await worker.close();
    }

    this.routers.clear();
    this.workers = [];
    logger.info('MediaSoup worker pool closed');
  }
}

module.exports = new MediaSoupWorkerPool();
