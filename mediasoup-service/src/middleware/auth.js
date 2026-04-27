const logger = require('../utils/logger');
const { verifyTokenWithAuthService } = require('../utils/authClient');

/**
 * Middleware to verify JWT token
 */
const verifyToken = async (req, res, next) => {
  try {
    const authHeader = req.headers.authorization;

    if (!authHeader || !authHeader.startsWith('Bearer ')) {
      return res.status(401).json({ error: 'Missing or invalid authorization header' });
    }

    const token = authHeader.substring(7);
    const tokenData = await verifyTokenWithAuthService(token);

    if (!tokenData || !tokenData.valid) {
      return res.status(401).json({ error: 'Invalid or expired token' });
    }

    req.user = {
      id: tokenData.subject,
      token,
    };

    next();
  } catch (error) {
    logger.error('Token verification error:', error);
    res.status(401).json({ error: 'Token verification failed' });
  }
};

module.exports = {
  verifyToken,
};
