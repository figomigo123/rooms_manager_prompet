const logger = require('../utils/logger');

const MAX_AUDIO_SPEAKERS = parseInt(process.env.MAX_AUDIO_SPEAKERS || '10');

class AudioManager {
  constructor() {
    this.speakerLevels = new Map(); // peerId -> { volume, timestamp }
    this.activeSpeakersCache = new Map(); // roomId -> [speakers]
  }

  /**
   * Update speaker volume
   */
  updateSpeakerVolume(peerId, volume) {
    this.speakerLevels.set(peerId, {
      volume,
      timestamp: Date.now(),
    });
  }

  /**
   * Get top N speakers
   */
  getTopSpeakers(speakerVolumes, count = MAX_AUDIO_SPEAKERS) {
    return speakerVolumes
      .sort((a, b) => b.volume - a.volume)
      .slice(0, count)
      .map(s => ({
        peerId: s.peerId,
        volume: s.volume,
      }));
  }

  /**
   * Calculate active speakers from producer stats
   */
  async calculateActiveSpeakers(room) {
    try {
      const speakerVolumes = [];

      // Get audio levels from all producers
      for (const [producerId, producer] of room.producers.entries()) {
        if (producer.kind === 'audio') {
          const stats = await producer.getStats();
          
          // Extract audio level from stats
          if (stats && stats.length > 0) {
            const audioStat = stats.find(s => s.type === 'inbound-rtp');
            if (audioStat && audioStat.audioLevel !== undefined) {
              // Find peer with this producer
              for (const [peerId, peer] of room.peers.entries()) {
                if (peer.producers.get('audio')?.id === producerId) {
                  speakerVolumes.push({
                    peerId,
                    volume: audioStat.audioLevel,
                  });
                  break;
                }
              }
            }
          }
        }
      }

      // Get top speakers
      const topSpeakers = this.getTopSpeakers(speakerVolumes, MAX_AUDIO_SPEAKERS);
      this.activeSpeakersCache.set(room.roomId, topSpeakers);

      return topSpeakers;
    } catch (error) {
      logger.error(`Failed to calculate active speakers:`, error);
      return [];
    }
  }

  /**
   * Get cached active speakers
   */
  getActiveSpeakers(roomId) {
    return this.activeSpeakersCache.get(roomId) || [];
  }

  /**
   * Update audio bitrate for adaptive quality
   */
  async updateAudioBitrate(producer, bitrate) {
    try {
      // MediaSoup handles audio bitrate automatically
      // This is for future custom bitrate management
      logger.debug(`Audio bitrate for producer ${producer.id}: ${bitrate} bps`);
    } catch (error) {
      logger.error(`Failed to update audio bitrate:`, error);
    }
  }

  /**
   * Mute audio producer
   */
  async muteAudio(producer) {
    try {
      if (!producer.paused) {
        await producer.pause();
        logger.info(`Audio muted for producer ${producer.id}`);
      }
    } catch (error) {
      logger.error(`Failed to mute audio:`, error);
    }
  }

  /**
   * Unmute audio producer
   */
  async unmuteAudio(producer) {
    try {
      if (producer.paused) {
        await producer.resume();
        logger.info(`Audio unmuted for producer ${producer.id}`);
      }
    } catch (error) {
      logger.error(`Failed to unmute audio:`, error);
    }
  }
}

module.exports = new AudioManager();
