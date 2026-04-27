package com.roomsmanager.room.service;

import com.roomsmanager.room.dto.CreateRoomRequest;
import com.roomsmanager.room.dto.RoomResponse;
import com.roomsmanager.room.entity.Room;
import com.roomsmanager.room.repository.RoomRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class RoomService {
    
    @Autowired
    private RoomRepository roomRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    /**
     * Create a new room
     */
    public RoomResponse createRoom(String appId, String ownerId, CreateRoomRequest request) {
        log.info("Creating room: {} for app: {}", request.getRoomName(), appId);
        
        // Generate unique room ID
        String roomId = "room_" + UUID.randomUUID().toString().substring(0, 12);
        
        // Create settings
        Room.Settings settings = Room.Settings.builder()
                .maxUsers(request.getMaxUsers() != null ? request.getMaxUsers() : 200)
                .audioEnabled(request.getAudioEnabled() != null ? request.getAudioEnabled() : true)
                .videoEnabled(request.getVideoEnabled() != null ? request.getVideoEnabled() : true)
                .screenshareEnabled(request.getScreenshareEnabled() != null ? request.getScreenshareEnabled() : true)
                .whiteboardEnabled(request.getWhiteboardEnabled() != null ? request.getWhiteboardEnabled() : true)
                .chatEnabled(request.getChatEnabled() != null ? request.getChatEnabled() : true)
                .build();
        
        // Create room
        Room room = Room.builder()
                .roomId(roomId)
                .appId(appId)
                .roomName(request.getRoomName())
                .description(request.getDescription())
                .ownerId(ownerId)
                .admins(List.of(ownerId)) // Owner is automatically an admin
                .settings(settings)
                .passwordHash(request.getPassword() != null ? 
                    passwordEncoder.encode(request.getPassword()) : null)
                .build();
        
        Room saved = roomRepository.save(room);
        
        // Cache room in Redis for 1 hour
        String cacheKey = "room:" + roomId;
        redisTemplate.opsForValue().set(cacheKey, saved, 1, TimeUnit.HOURS);
        
        log.info("Room created: {}", roomId);
        return mapToResponse(saved);
    }
    
    /**
     * Get room by room ID
     */
    public Optional<Room> getRoomById(String roomId) {
        // Try cache first
        String cacheKey = "room:" + roomId;
        Room cachedRoom = (Room) redisTemplate.opsForValue().get(cacheKey);
        
        if (cachedRoom != null) {
            log.debug("Room found in cache: {}", roomId);
            return Optional.of(cachedRoom);
        }
        
        // Otherwise fetch from DB and cache
        Optional<Room> room = roomRepository.findByRoomId(roomId);
        room.ifPresent(r -> redisTemplate.opsForValue().set(cacheKey, r, 1, TimeUnit.HOURS));
        
        return room;
    }
    
    /**
     * Get room by app ID and room ID
     */
    public Optional<Room> getRoomByAppIdAndRoomId(String appId, String roomId) {
        return roomRepository.findByAppIdAndRoomId(appId, roomId);
    }
    
    /**
     * Get all rooms for an app
     */
    public List<Room> getRoomsByAppId(String appId) {
        return roomRepository.findByAppId(appId);
    }
    
    /**
     * Get active rooms for an app
     */
    public List<Room> getActiveRoomsByAppId(String appId) {
        return roomRepository.findByAppIdAndStatus(appId, "ACTIVE");
    }
    
    /**
     * Update room settings
     */
    public RoomResponse updateRoom(String roomId, CreateRoomRequest request) {
        Optional<Room> roomOpt = roomRepository.findByRoomId(roomId);
        
        if (roomOpt.isPresent()) {
            Room room = roomOpt.get();
            
            if (request.getRoomName() != null) {
                room.setRoomName(request.getRoomName());
            }
            if (request.getDescription() != null) {
                room.setDescription(request.getDescription());
            }
            if (request.getMaxUsers() != null) {
                room.getSettings().setMaxUsers(request.getMaxUsers());
            }
            if (request.getAudioEnabled() != null) {
                room.getSettings().setAudioEnabled(request.getAudioEnabled());
            }
            if (request.getVideoEnabled() != null) {
                room.getSettings().setVideoEnabled(request.getVideoEnabled());
            }
            if (request.getScreenshareEnabled() != null) {
                room.getSettings().setScreenshareEnabled(request.getScreenshareEnabled());
            }
            if (request.getWhiteboardEnabled() != null) {
                room.getSettings().setWhiteboardEnabled(request.getWhiteboardEnabled());
            }
            if (request.getChatEnabled() != null) {
                room.getSettings().setChatEnabled(request.getChatEnabled());
            }
            
            room.setUpdatedAt(LocalDateTime.now());
            Room updated = roomRepository.save(room);
            
            // Update cache
            String cacheKey = "room:" + roomId;
            redisTemplate.opsForValue().set(cacheKey, updated, 1, TimeUnit.HOURS);
            
            return mapToResponse(updated);
        }
        
        return null;
    }
    
    /**
     * Delete room
     */
    public boolean deleteRoom(String roomId) {
        Optional<Room> roomOpt = roomRepository.findByRoomId(roomId);
        
        if (roomOpt.isPresent()) {
            roomRepository.deleteById(roomOpt.get().getId());
            
            // Remove from cache
            String cacheKey = "room:" + roomId;
            redisTemplate.delete(cacheKey);
            
            log.info("Room deleted: {}", roomId);
            return true;
        }
        
        return false;
    }
    
    /**
     * Check if user can join room
     */
    public boolean canJoinRoom(String roomId, String userId) {
        Optional<Room> roomOpt = roomRepository.findByRoomId(roomId);
        
        if (roomOpt.isPresent()) {
            Room room = roomOpt.get();
            
            // Check if room is active
            if (!"ACTIVE".equals(room.getStatus())) {
                return false;
            }
            
            // Check if room is full
            if (room.getCurrentUsers() >= room.getSettings().getMaxUsers()) {
                return false;
            }
            
            // Check if room has expired
            if (room.getExpiresAt() != null && room.getExpiresAt().isBefore(LocalDateTime.now())) {
                return false;
            }
            
            return true;
        }
        
        return false;
    }
    
    /**
     * Increment user count
     */
    public void incrementUserCount(String roomId) {
        Optional<Room> roomOpt = roomRepository.findByRoomId(roomId);
        
        if (roomOpt.isPresent()) {
            Room room = roomOpt.get();
            room.setCurrentUsers(room.getCurrentUsers() + 1);
            roomRepository.save(room);
            
            // Invalidate cache
            String cacheKey = "room:" + roomId;
            redisTemplate.delete(cacheKey);
        }
    }
    
    /**
     * Decrement user count
     */
    public void decrementUserCount(String roomId) {
        Optional<Room> roomOpt = roomRepository.findByRoomId(roomId);
        
        if (roomOpt.isPresent()) {
            Room room = roomOpt.get();
            if (room.getCurrentUsers() > 0) {
                room.setCurrentUsers(room.getCurrentUsers() - 1);
                roomRepository.save(room);
            }
            
            // Invalidate cache
            String cacheKey = "room:" + roomId;
            redisTemplate.delete(cacheKey);
        }
    }
    
    /**
     * Map Room entity to RoomResponse
     */
    private RoomResponse mapToResponse(Room room) {
        return RoomResponse.builder()
                .roomId(room.getRoomId())
                .roomName(room.getRoomName())
                .description(room.getDescription())
                .ownerId(room.getOwnerId())
                .admins(room.getAdmins())
                .maxUsers(room.getSettings().getMaxUsers())
                .currentUsers(room.getCurrentUsers())
                .status(room.getStatus())
                .createdAt(room.getCreatedAt())
                .updatedAt(room.getUpdatedAt())
                .expiresAt(room.getExpiresAt())
                .settings(RoomResponse.RoomSettings.builder()
                        .audioEnabled(room.getSettings().getAudioEnabled())
                        .videoEnabled(room.getSettings().getVideoEnabled())
                        .screenshareEnabled(room.getSettings().getScreenshareEnabled())
                        .whiteboardEnabled(room.getSettings().getWhiteboardEnabled())
                        .chatEnabled(room.getSettings().getChatEnabled())
                        .recordingEnabled(room.getSettings().getRecordingEnabled())
                        .build())
                .build();
    }
}
