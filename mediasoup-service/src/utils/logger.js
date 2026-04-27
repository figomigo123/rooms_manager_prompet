const winston = require('winston');
const path = require('path');

const logLevel = process.env.LOG_LEVEL || 'debug';
const logFile = process.env.LOG_FILE || 'logs/mediasoup.log';

// Create logs directory if it doesn't exist
const fs = require('fs');
const logsDir = path.dirname(logFile);
if (!fs.existsSync(logsDir)) {
  fs.mkdirSync(logsDir, { recursive: true });
}

const logger = winston.createLogger({
  level: logLevel,
  format: winston.format.combine(
    winston.format.timestamp({ format: 'YYYY-MM-DD HH:mm:ss' }),
    winston.format.errors({ stack: true }),
    winston.format.printf(({ timestamp, level, message, stack }) => {
      const msg = stack || message;
      return `${timestamp} [${level.toUpperCase()}] ${msg}`;
    })
  ),
  defaultMeta: { service: 'mediasoup-service' },
  transports: [
    new winston.transports.File({ filename: logFile }),
    new winston.transports.Console({
      format: winston.format.combine(
        winston.format.colorize(),
        winston.format.printf(({ timestamp, level, message, stack }) => {
          const msg = stack || message;
          return `${timestamp} [${level}] ${msg}`;
        })
      ),
    }),
  ],
});

module.exports = logger;
