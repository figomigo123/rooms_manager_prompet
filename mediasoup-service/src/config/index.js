module.exports = {
  mediasoup: {
    numWorkers: parseInt(process.env.MEDIASOUP_WORKER_PROCESSES || '4'),
    logLevel: process.env.NODE_ENV === 'production' ? 'warn' : 'debug',
    rtcMinPort: parseInt(process.env.MEDIASOUP_WORKER_RTC_PORT_RANGE_START || '20000'),
    rtcMaxPort: parseInt(process.env.MEDIASOUP_WORKER_RTC_PORT_RANGE_END || '30000'),
    mediaCodecs: [
      {
        kind: 'audio',
        mimeType: 'audio/opus',
        clockRate: 48000,
        channels: 2,
      },
      {
        kind: 'video',
        mimeType: 'video/VP8',
        clockRate: 90000,
        parameters: {
          'x-google-start-bitrate': 1000,
        },
      },
      {
        kind: 'video',
        mimeType: 'video/VP9',
        clockRate: 90000,
        parameters: {
          'profile-id': 0,
          'x-google-start-bitrate': 1000,
        },
      },
      {
        kind: 'video',
        mimeType: 'video/H264',
        clockRate: 90000,
        parameters: {
          'level-asymmetry-allowed': 1,
          'packetization-mode': 1,
          'profile-level-id': '42e01e',
          'x-google-start-bitrate': 1000,
        },
      },
    ],
  },
  server: {
    port: parseInt(process.env.PORT || '8003'),
    env: process.env.NODE_ENV || 'development',
    corsOrigins: (process.env.CORS_ORIGIN || 'http://localhost:3000,http://localhost:8080').split(','),
  },
  redis: {
    host: process.env.REDIS_HOST || 'localhost',
    port: parseInt(process.env.REDIS_PORT || '6379'),
    db: parseInt(process.env.REDIS_DB || '1'),
  },
};
