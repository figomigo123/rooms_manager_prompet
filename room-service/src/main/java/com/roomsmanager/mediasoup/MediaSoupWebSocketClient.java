package com.roomsmanager.mediasoup;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.socket.WebSocketMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.HashMap;

@Slf4j
@Component
public class MediaSoupWebSocketClient extends TextWebSocketHandler {

    @Value("${mediasoup.url:http://mediasoup-service:3000}")
    private String mediasoupUrl;

    private final ObjectMapper objectMapper;
    private final ConcurrentHashMap<String, WebSocketSession> activeSessions = new ConcurrentHashMap<>();

    public MediaSoupWebSocketClient(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Connect to MediaSoup WebSocket server
     */
    public void connectToMediaSoup(String roomId, String userId, String accessToken) {
        try {
            StandardWebSocketClient client = new StandardWebSocketClient();
            String wsUrl = mediasoupUrl.replace("http", "ws") + "?roomId=" + roomId + "&userId=" + userId + "&token=" + accessToken;

            log.info("Connecting to MediaSoup WebSocket: {}", wsUrl);

            WebSocketSession session = client.doHandshake(this, wsUrl).get();
            String sessionId = roomId + ":" + userId;
            activeSessions.put(sessionId, session);

            log.info("Connected to MediaSoup WebSocket: {}", sessionId);
        } catch (Exception e) {
            log.error("Error connecting to MediaSoup WebSocket", e);
        }
    }

    /**
     * Send message to MediaSoup
     */
    public void sendMessage(String roomId, String userId, Map<String, Object> message) {
        try {
            String sessionId = roomId + ":" + userId;
            WebSocketSession session = activeSessions.get(sessionId);

            if (session != null && session.isOpen()) {
                String payload = objectMapper.writeValueAsString(message);
                session.sendMessage(new TextMessage(payload));
            } else {
                log.warn("WebSocket session not found or closed: {}", sessionId);
            }
        } catch (Exception e) {
            log.error("Error sending message to MediaSoup", e);
        }
    }

    /**
     * Disconnect from MediaSoup
     */
    public void disconnect(String roomId, String userId) {
        try {
            String sessionId = roomId + ":" + userId;
            WebSocketSession session = activeSessions.get(sessionId);

            if (session != null && session.isOpen()) {
                session.close();
                activeSessions.remove(sessionId);
                log.info("Disconnected from MediaSoup WebSocket: {}", sessionId);
            }
        } catch (Exception e) {
            log.error("Error disconnecting from MediaSoup", e);
        }
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) {
        try {
            Map<String, Object> data = objectMapper.readValue(message.getPayload(), Map.class);
            String eventType = (String) data.get("type");

            log.debug("Received WebSocket message: {}", eventType);

            // Handle different message types
            switch (eventType) {
                case "error":
                    handleError(data);
                    break;
                case "notification":
                    handleNotification(data);
                    break;
                default:
                    log.debug("Unknown message type: {}", eventType);
            }
        } catch (Exception e) {
            log.error("Error handling WebSocket message", e);
        }
    }

    private void handleError(Map<String, Object> data) {
        String message = (String) data.get("message");
        String roomId = (String) data.get("roomId");
        log.error("MediaSoup error in room {}: {}", roomId, message);
    }

    private void handleNotification(Map<String, Object> data) {
        String notification = (String) data.get("notification");
        log.info("MediaSoup notification: {}", notification);
    }

    /**
     * Get active session count
     */
    public int getActiveSessionCount() {
        return activeSessions.size();
    }
}
