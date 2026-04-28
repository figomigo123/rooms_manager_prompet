package com.roomsmanager.mediasoup;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class MediaSoupClient {

    @Value("${mediasoup.url:http://mediasoup-service:3000}")
    private String mediasoupUrl;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public MediaSoupClient(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * Create a new room on MediaSoup
     */
    public Map<String, Object> createRoom(String roomId, String appId) {
        try {
            String url = mediasoupUrl + "/api/rooms";
            Map<String, String> payload = new HashMap<>();
            payload.put("roomId", roomId);
            payload.put("appId", appId);

            ResponseEntity<Map> response = restTemplate.postForEntity(
                url,
                new HttpEntity<>(payload, getHeaders()),
                Map.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Room created on MediaSoup: {}", roomId);
                return response.getBody();
            } else {
                log.error("Failed to create room on MediaSoup: {}", response.getStatusCode());
                return null;
            }
        } catch (Exception e) {
            log.error("Error creating room on MediaSoup", e);
            return null;
        }
    }

    /**
     * Get room information from MediaSoup
     */
    public Map<String, Object> getRoom(String roomId) {
        try {
            String url = mediasoupUrl + "/api/rooms/" + roomId;
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                return response.getBody();
            } else {
                log.warn("Room not found on MediaSoup: {}", roomId);
                return null;
            }
        } catch (Exception e) {
            log.error("Error getting room from MediaSoup", e);
            return null;
        }
    }

    /**
     * Close room on MediaSoup
     */
    public boolean closeRoom(String roomId) {
        try {
            String url = mediasoupUrl + "/api/rooms/" + roomId;
            restTemplate.delete(url);
            log.info("Room closed on MediaSoup: {}", roomId);
            return true;
        } catch (Exception e) {
            log.error("Error closing room on MediaSoup", e);
            return false;
        }
    }

    /**
     * Get room statistics (users, producers, consumers, bitrate)
     */
    public Map<String, Object> getRoomStats(String roomId) {
        try {
            String url = mediasoupUrl + "/api/rooms/" + roomId + "/stats";
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                return response.getBody();
            }
            return null;
        } catch (Exception e) {
            log.error("Error getting room stats from MediaSoup", e);
            return null;
        }
    }

    /**
     * Get top 10 speakers from MediaSoup
     */
    public Map<String, Object> getTopSpeakers(String roomId) {
        try {
            String url = mediasoupUrl + "/api/rooms/" + roomId + "/top-speakers";
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                return response.getBody();
            }
            return null;
        } catch (Exception e) {
            log.error("Error getting top speakers from MediaSoup", e);
            return null;
        }
    }

    /**
     * Generate JWT token for WebSocket connection
     */
    public String generateAccessToken(String roomId, String userId, String userName) {
        try {
            String url = mediasoupUrl + "/api/access-token";
            Map<String, String> payload = new HashMap<>();
            payload.put("roomId", roomId);
            payload.put("userId", userId);
            payload.put("userName", userName);

            ResponseEntity<Map> response = restTemplate.postForEntity(
                url,
                new HttpEntity<>(payload, getHeaders()),
                Map.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Object token = response.getBody().get("token");
                return token != null ? token.toString() : null;
            }
            return null;
        } catch (Exception e) {
            log.error("Error generating access token from MediaSoup", e);
            return null;
        }
    }

    /**
     * Mute specific user's audio/video
     */
    public boolean muteUser(String roomId, String userId, String mediaType) {
        try {
            String url = mediasoupUrl + "/api/rooms/" + roomId + "/mute";
            Map<String, String> payload = new HashMap<>();
            payload.put("userId", userId);
            payload.put("mediaType", mediaType); // audio or video

            ResponseEntity<Map> response = restTemplate.postForEntity(
                url,
                new HttpEntity<>(payload, getHeaders()),
                Map.class
            );

            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            log.error("Error muting user on MediaSoup", e);
            return false;
        }
    }

    /**
     * Unmute specific user's audio/video
     */
    public boolean unmuteUser(String roomId, String userId, String mediaType) {
        try {
            String url = mediasoupUrl + "/api/rooms/" + roomId + "/unmute";
            Map<String, String> payload = new HashMap<>();
            payload.put("userId", userId);
            payload.put("mediaType", mediaType);

            ResponseEntity<Map> response = restTemplate.postForEntity(
                url,
                new HttpEntity<>(payload, getHeaders()),
                Map.class
            );

            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            log.error("Error unmuting user on MediaSoup", e);
            return false;
        }
    }

    /**
     * Kick user from room
     */
    public boolean kickUser(String roomId, String userId) {
        try {
            String url = mediasoupUrl + "/api/rooms/" + roomId + "/kick";
            Map<String, String> payload = new HashMap<>();
            payload.put("userId", userId);

            ResponseEntity<Map> response = restTemplate.postForEntity(
                url,
                new HttpEntity<>(payload, getHeaders()),
                Map.class
            );

            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            log.error("Error kicking user from MediaSoup", e);
            return false;
        }
    }

    /**
     * Start recording session
     */
    public Map<String, Object> startRecording(String roomId) {
        try {
            String url = mediasoupUrl + "/api/rooms/" + roomId + "/recording/start";
            ResponseEntity<Map> response = restTemplate.postForEntity(
                url,
                new HttpEntity<>(new HashMap<>(), getHeaders()),
                Map.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Recording started for room: {}", roomId);
                return response.getBody();
            }
            return null;
        } catch (Exception e) {
            log.error("Error starting recording on MediaSoup", e);
            return null;
        }
    }

    /**
     * Stop recording session
     */
    public boolean stopRecording(String roomId) {
        try {
            String url = mediasoupUrl + "/api/rooms/" + roomId + "/recording/stop";
            ResponseEntity<Map> response = restTemplate.postForEntity(
                url,
                new HttpEntity<>(new HashMap<>(), getHeaders()),
                Map.class
            );

            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            log.error("Error stopping recording on MediaSoup", e);
            return false;
        }
    }

    /**
     * Check MediaSoup health
     */
    public boolean isHealthy() {
        try {
            String url = mediasoupUrl + "/health";
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            log.warn("MediaSoup health check failed", e);
            return false;
        }
    }

    private HttpHeaders getHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }
}
