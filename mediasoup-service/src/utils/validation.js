const logger = require('./logger');
const Joi = require('joi');

/**
 * Validate join room request
 */
const validateJoinRoom = (data) => {
  const schema = Joi.object({
    roomId: Joi.string().required(),
    peerId: Joi.string().required(),
    displayName: Joi.string().required(),
    rtpCapabilities: Joi.object().required(),
  });

  const { error, value } = schema.validate(data);
  if (error) {
    throw new Error(`Validation error: ${error.message}`);
  }
  return value;
};

/**
 * Validate producer parameters
 */
const validateProducerParams = (data) => {
  const schema = Joi.object({
    kind: Joi.string().valid('audio', 'video').required(),
    rtpParameters: Joi.object().required(),
    paused: Joi.boolean(),
  });

  const { error, value } = schema.validate(data);
  if (error) {
    throw new Error(`Validation error: ${error.message}`);
  }
  return value;
};

/**
 * Validate consumer parameters
 */
const validateConsumerParams = (data) => {
  const schema = Joi.object({
    producerId: Joi.string().required(),
    rtpCapabilities: Joi.object().required(),
  });

  const { error, value } = schema.validate(data);
  if (error) {
    throw new Error(`Validation error: ${error.message}`);
  }
  return value;
};

/**
 * Validate dtls parameters
 */
const validateDtlsParams = (data) => {
  const schema = Joi.object({
    role: Joi.string().valid('auto', 'client', 'server'),
    fingerprints: Joi.array().required(),
  });

  const { error, value } = schema.validate(data);
  if (error) {
    throw new Error(`Validation error: ${error.message}`);
  }
  return value;
};

module.exports = {
  validateJoinRoom,
  validateProducerParams,
  validateConsumerParams,
  validateDtlsParams,
};
