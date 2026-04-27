const express = require('express');
const router = express.Router();
const logger = require('../utils/logger');
const { verifyToken } = require('../middleware/auth');
const roomManager = require('../mediasoup/roomManager');

/**
 * Get room stats
 * GET /api/v1/stats/rooms/:roomId
 */
router.get('/rooms/:roomId', verifyToken, (req, res) => {
  try {
    const { roomId } = req.params;

    const stats = roomManager.getRoomStats(roomId);
    res.json(stats);
  } catch (error) {
    logger.error('Error getting room stats:', error);
    res.status(404).json({ error: error.message });
  }
});

/**
 * Get producer stats
 * GET /api/v1/stats/producers/:producerId
 */
router.get('/producers/:producerId', verifyToken, async (req, res) => {
  try {
    const { producerId } = req.params;

    // TODO: Get producer from registry and fetch stats
    res.json({ producerId, stats: [] });
  } catch (error) {
    logger.error('Error getting producer stats:', error);
    res.status(500).json({ error: error.message });
  }
});

/**
 * Get consumer stats
 * GET /api/v1/stats/consumers/:consumerId
 */
router.get('/consumers/:consumerId', verifyToken, async (req, res) => {
  try {
    const { consumerId } = req.params;

    // TODO: Get consumer from registry and fetch stats
    res.json({ consumerId, stats: [] });
  } catch (error) {
    logger.error('Error getting consumer stats:', error);
    res.status(500).json({ error: error.message });
  }
});

module.exports = router;
