package com.roomsmanager.mediasoup;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.data.redis.core.RedisTemplate;
import com.roomsmanager.service.RoomService;
import com.roomsmanager.service.UsageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import java.util.Map;
import java.util.HashMap;

@Slf4j
@Component
public class MediaSoupEventListener {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private RoomService roomService;

    @Autowired
    private UsageService usageService;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Listen to MediaSoup events via RabbitMQ
     * Events include: user-joined, user-left, producer-added, consumer-added, recording-started, etc.
     */
    @RabbitListener(queues = "mediasoup.events")
    public void handleMediaSoupEvent(String message) {
        try {
            Map<String, Object> event = objectMapper.readValue(message, Map.class);
            String eventType = (String) event.get("type");
            String roomId = (String) event.get("roomId");

            log.info("Received MediaSoup event: {} for room: {}", eventType, roomId);

            switch (eventType) {
                case "user-joined":
                    handleUserJoined(event);
                    break;
                case "user-left":
                    handleUserLeft(event);
                    break;
                case "producer-added":
                    handleProducerAdded(event);
                    break;
                case "producer-closed":
                    handleProducerClosed(event);
                    break;
                case "consumer-added":
                    handleConsumerAdded(event);
                    break;
                case "audio-level-updated":
                    handleAudioLevelUpdated(event);
                    break;
                case "recording-started":
                    handleRecordingStarted(event);
                    break;
                case "recording-stopped":
                    handleRecordingStopped(event);
                    break;
                default:
                    log.warn("Unknown event type: {}", eventType);
            }
        } catch (Exception e) {
            log.error("Error handling MediaSoup event", e);
        }
    }

    private void handleUserJoined(Map<String, Object> event) {
        String roomId = (String) event.get("roomId");
        String userId = (String) event.get("userId");
        String userName = (String) event.get("userName");
        Long timestamp = (Long) event.get("timestamp");

        log.info("User joined: {} in room: {}", userName, roomId);

        // Update room user count
        roomService.incrementUserCount(roomId);

        // Cache user session info
        String sessionKey = "session:" + roomId + ":" + userId;
        Map<String, Object> sessionData = new HashMap<>();
        sessionData.put("userId", userId);
        sessionData.put("userName", userName);
        sessionData.put("joinedAt", timestamp);
        sessionData.put("audioEnabled", true);
        sessionData.put("videoEnabled", true);

        redisTemplate.opsForHash().putAll(sessionKey, sessionData);
        redisTemplate.expire(sessionKey, java.time.Duration.ofHours(24));

        // Start usage tracking
        usageService.startUserSession(roomId, userId);
    }

    private void handleUserLeft(Map<String, Object> event) {
        String roomId = (String) event.get("roomId");
        String userId = (String) event.get("userId");
        Long duration = (Long) event.get("duration"); // milliseconds

        log.info("User left: {} from room: {} (duration: {} ms)", userId, roomId, duration);

        // Update room user count
        roomService.decrementUserCount(roomId);

        // Clean up session
        String sessionKey = "session:" + roomId + ":" + userId;
        redisTemplate.delete(sessionKey);

        // Record usage (minutes)
        if (duration != null) {
            double minutes = duration / 60000.0; // Convert ms to minutes
            usageService.recordUserMinutes(roomId, userId, minutes);
        }
    }

    private void handleProducerAdded(Map<String, Object> event) {
        String roomId = (String) event.get("roomId");
        String userId = (String) event.get("userId");
        String producerId = (String) event.get("producerId");
        String mediaType = (String) event.get("mediaType"); // audio or video

        log.info("Producer added: {} ({}) in room: {}", producerId, mediaType, roomId);

        // Cache producer info
        String producerKey = "producer:" + roomId + ":" + producerId;
        Map<String, Object> producerData = new HashMap<>();
        producerData.put("producerId", producerId);
        producerData.put("userId", userId);
        producerData.put("mediaType", mediaType);
        producerData.put("createdAt", System.currentTimeMillis());

        redisTemplate.opsForHash().putAll(producerKey, producerData);
        redisTemplate.expire(producerKey, java.time.Duration.ofHours(24));

        // Update user media status
        String sessionKey = "session:" + roomId + ":" + userId;
        if ("audio".equals(mediaType)) {
            redisTemplate.opsForHash().put(sessionKey, "audioEnabled", true);
        } else if ("video".equals(mediaType)) {
            redisTemplate.opsForHash().put(sessionKey, "videoEnabled", true);
        } else if ("screen".equals(mediaType)) {
            redisTemplate.opsForHash().put(sessionKey, "screenEnabled", true);
        }
    }

    private void handleProducerClosed(Map<String, Object> event) {
        String roomId = (String) event.get("roomId");
        String userId = (String) event.get("userId");
        String producerId = (String) event.get("producerId");
        String mediaType = (String) event.get("mediaType");

        log.info("Producer closed: {} ({}) in room: {}", producerId, mediaType, roomId);

        // Clean up producer cache
        String producerKey = "producer:" + roomId + ":" + producerId;
        redisTemplate.delete(producerKey);

        // Update user media status
        String sessionKey = "session:" + roomId + ":" + userId;
        if ("audio".equals(mediaType)) {
            redisTemplate.opsForHash().put(sessionKey, "audioEnabled", false);
        } else if ("video".equals(mediaType)) {
            redisTemplate.opsForHash().put(sessionKey, "videoEnabled", false);
        } else if ("screen".equals(mediaType)) {
            redisTemplate.opsForHash().put(sessionKey, "screenEnabled", false);
        }
    }

    private void handleConsumerAdded(Map<String, Object> event) {
        String roomId = (String) event.get("roomId");
        String consumerId = (String) event.get("consumerId");
        String producerId = (String) event.get("producerId");
        String mediaType = (String) event.get("mediaType");

        log.debug("Consumer added: {} consuming {} in room: {}", consumerId, producerId, roomId);

        // Cache consumer info
        String consumerKey = "consumer:" + roomId + ":" + consumerId;
        Map<String, Object> consumerData = new HashMap<>();
        consumerData.put("consumerId", consumerId);
        consumerData.put("producerId", producerId);
        consumerData.put("mediaType", mediaType);
        consumerData.put("createdAt", System.currentTimeMillis());

        redisTemplate.opsForHash().putAll(consumerKey, consumerData);
        redisTemplate.expire(consumerKey, java.time.Duration.ofHours(24));
    }

    private void handleAudioLevelUpdated(Map<String, Object> event) {
        String roomId = (String) event.get("roomId");
        Map<String, Double> audioLevels = (Map<String, Double>) event.get("audioLevels");

        if (audioLevels == null || audioLevels.isEmpty()) {
            return;
        }

        // Cache audio levels for top 10 speakers calculation
        String audioLevelKey = "audio-levels:" + roomId;
        audioLevels.forEach((userId, level) -> {
            redisTemplate.opsForZSet().add(audioLevelKey, userId, level);
        });
        redisTemplate.expire(audioLevelKey, java.time.Duration.ofMinutes(5));

        log.debug("Audio levels updated in room: {} - {} speakers", roomId, audioLevels.size());
    }

    private void handleRecordingStarted(Map<String, Object> event) {
        String roomId = (String) event.get("roomId");
        String recordingId = (String) event.get("recordingId");

        log.info("Recording started for room: {}", roomId);

        // Cache recording info
        String recordingKey = "recording:" + roomId;
        Map<String, Object> recordingData = new HashMap<>();
        recordingData.put("recordingId", recordingId);
        recordingData.put("startedAt", System.currentTimeMillis());
        recordingData.put("status", "active");

        redisTemplate.opsForHash().putAll(recordingKey, recordingData);
    }

    private void handleRecordingStopped(Map<String, Object> event) {
        String roomId = (String) event.get("roomId");
        String recordingUrl = (String) event.get("recordingUrl");
        Long duration = (Long) event.get("duration");

        log.info("Recording stopped for room: {} (URL: {}, duration: {}ms)", roomId, recordingUrl, duration);

        // Update recording status
        String recordingKey = "recording:" + roomId;
        redisTemplate.opsForHash().put(recordingKey, "status", "completed");
        redisTemplate.opsForHash().put(recordingKey, "recordingUrl", recordingUrl);
        redisTemplate.opsForHash().put(recordingKey, "duration", duration);
        redisTemplate.opsForHash().put(recordingKey, "completedAt", System.currentTimeMillis());
    }
}
