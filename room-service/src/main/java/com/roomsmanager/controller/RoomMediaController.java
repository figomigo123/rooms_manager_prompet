package com.roomsmanager.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import com.roomsmanager.mediasoup.MediaSoupClient;
import com.roomsmanager.mediasoup.MediaSoupWebSocketClient;
import lombok.extern.slf4j.Slf4j;
import java.util.Map;
import java.util.HashMap;

@Slf4j
@RestController
@RequestMapping("/room/v1/media")
public class RoomMediaController {

    @Autowired
    private MediaSoupClient mediaSoupClient;

    @Autowired
    private MediaSoupWebSocketClient mediaSoupWebSocketClient;

    /**
     * Generate access token for user to connect to MediaSoup
     * GET /room/v1/media/access-token?roomId=X&userId=Y&userName=Z
     */
    @GetMapping("/access-token")
    public ResponseEntity<?> generateAccessToken(
            @RequestParam String roomId,
            @RequestParam String userId,
            @RequestParam String userName) {

        try {
            String token = mediaSoupClient.generateAccessToken(roomId, userId, userName);

            if (token != null) {
                Map<String, Object> response = new HashMap<>();
                response.put("token", token);
                response.put("roomId", roomId);
                response.put("userId", userId);
                response.put("userName", userName);
                response.put("mediasoupUrl", "ws://mediasoup-service:3000");
                response.put("timestamp", System.currentTimeMillis());

                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to generate access token"));
            }
        } catch (Exception e) {
            log.error("Error generating access token", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get room statistics from MediaSoup
     * GET /room/v1/media/rooms/{roomId}/stats
     */
    @GetMapping("/rooms/{roomId}/stats")
    public ResponseEntity<?> getRoomStats(@PathVariable String roomId) {
        try {
            Map<String, Object> stats = mediaSoupClient.getRoomStats(roomId);

            if (stats != null) {
                return ResponseEntity.ok(stats);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Room not found"));
            }
        } catch (Exception e) {
            log.error("Error getting room stats", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get top 10 speakers in room
     * GET /room/v1/media/rooms/{roomId}/top-speakers
     */
    @GetMapping("/rooms/{roomId}/top-speakers")
    public ResponseEntity<?> getTopSpeakers(@PathVariable String roomId) {
        try {
            Map<String, Object> speakers = mediaSoupClient.getTopSpeakers(roomId);

            if (speakers != null) {
                return ResponseEntity.ok(speakers);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Room not found"));
            }
        } catch (Exception e) {
            log.error("Error getting top speakers", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Mute user's audio/video
     * POST /room/v1/media/rooms/{roomId}/mute
     */
    @PostMapping("/rooms/{roomId}/mute")
    public ResponseEntity<?> muteUser(
            @PathVariable String roomId,
            @RequestBody Map<String, String> payload) {

        try {
            String userId = payload.get("userId");
            String mediaType = payload.get("mediaType"); // audio or video

            if (userId == null || mediaType == null) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "userId and mediaType are required"));
            }

            boolean result = mediaSoupClient.muteUser(roomId, userId, mediaType);

            if (result) {
                return ResponseEntity.ok(Map.of(
                    "message", "User muted successfully",
                    "userId", userId,
                    "mediaType", mediaType
                ));
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to mute user"));
            }
        } catch (Exception e) {
            log.error("Error muting user", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Unmute user's audio/video
     * POST /room/v1/media/rooms/{roomId}/unmute
     */
    @PostMapping("/rooms/{roomId}/unmute")
    public ResponseEntity<?> unmuteUser(
            @PathVariable String roomId,
            @RequestBody Map<String, String> payload) {

        try {
            String userId = payload.get("userId");
            String mediaType = payload.get("mediaType");

            if (userId == null || mediaType == null) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "userId and mediaType are required"));
            }

            boolean result = mediaSoupClient.unmuteUser(roomId, userId, mediaType);

            if (result) {
                return ResponseEntity.ok(Map.of(
                    "message", "User unmuted successfully",
                    "userId", userId,
                    "mediaType", mediaType
                ));
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to unmute user"));
            }
        } catch (Exception e) {
            log.error("Error unmuting user", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Kick user from room
     * POST /room/v1/media/rooms/{roomId}/kick
     */
    @PostMapping("/rooms/{roomId}/kick")
    public ResponseEntity<?> kickUser(
            @PathVariable String roomId,
            @RequestBody Map<String, String> payload) {

        try {
            String userId = payload.get("userId");

            if (userId == null) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "userId is required"));
            }

            boolean result = mediaSoupClient.kickUser(roomId, userId);

            if (result) {
                return ResponseEntity.ok(Map.of(
                    "message", "User kicked successfully",
                    "userId", userId
                ));
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to kick user"));
            }
        } catch (Exception e) {
            log.error("Error kicking user", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Start recording
     * POST /room/v1/media/rooms/{roomId}/recording/start
     */
    @PostMapping("/rooms/{roomId}/recording/start")
    public ResponseEntity<?> startRecording(@PathVariable String roomId) {
        try {
            Map<String, Object> result = mediaSoupClient.startRecording(roomId);

            if (result != null) {
                return ResponseEntity.ok(result);
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to start recording"));
            }
        } catch (Exception e) {
            log.error("Error starting recording", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Stop recording
     * POST /room/v1/media/rooms/{roomId}/recording/stop
     */
    @PostMapping("/rooms/{roomId}/recording/stop")
    public ResponseEntity<?> stopRecording(@PathVariable String roomId) {
        try {
            boolean result = mediaSoupClient.stopRecording(roomId);

            if (result) {
                return ResponseEntity.ok(Map.of(
                    "message", "Recording stopped successfully",
                    "roomId", roomId
                ));
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to stop recording"));
            }
        } catch (Exception e) {
            log.error("Error stopping recording", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Health check for MediaSoup
     * GET /room/v1/media/health
     */
    @GetMapping("/health")
    public ResponseEntity<?> healthCheck() {
        try {
            boolean healthy = mediaSoupClient.isHealthy();

            if (healthy) {
                return ResponseEntity.ok(Map.of(
                    "status", "healthy",
                    "timestamp", System.currentTimeMillis()
                ));
            } else {
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("status", "unhealthy"));
            }
        } catch (Exception e) {
            log.error("Error checking MediaSoup health", e);
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of("status", "unhealthy", "error", e.getMessage()));
        }
    }
}
