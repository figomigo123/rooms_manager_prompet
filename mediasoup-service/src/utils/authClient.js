const logger = require('./logger');
const axios = require('axios');

const authServiceUrl = process.env.AUTH_SERVICE_URL || 'http://localhost:8001';
const verifyEndpoint = process.env.AUTH_VERIFY_ENDPOINT || '/auth/v1/verify-token';

/**
 * Verify token with auth service
 */
async function verifyTokenWithAuthService(token) {
  try {
    const response = await axios.post(`${authServiceUrl}${verifyEndpoint}`, {}, {
      headers: {
        'Authorization': `Bearer ${token}`,
      },
      timeout: 5000,
    });

    return response.data;
  } catch (error) {
    logger.error('Failed to verify token with auth service:', error.message);
    return null;
  }
}

module.exports = {
  verifyTokenWithAuthService,
};
