package com.roomsmanager.room.controller;

import com.roomsmanager.room.dto.CreateRoomRequest;
import com.roomsmanager.room.dto.RoomResponse;
import com.roomsmanager.room.dto.AddAdminRequest;
import com.roomsmanager.room.entity.Room;
import com.roomsmanager.room.entity.RoomAdmin;
import com.roomsmanager.room.service.RoomService;
import com.roomsmanager.room.service.RoomAdminService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/room/v1")
@CrossOrigin(origins = "*", maxAge = 3600)
public class RoomController {
    
    @Autowired
    private RoomService roomService;
    
    @Autowired
    private RoomAdminService roomAdminService;
    
    /**
     * Create a new room
     * POST /room/v1/rooms
     */
    @PostMapping("/rooms")
    public ResponseEntity<?> createRoom(
            @RequestHeader("Authorization") String authHeader,
            @RequestHeader("X-App-Id") String appId,
            @Valid @RequestBody CreateRoomRequest request) {
        try {
            String userId = extractUserIdFromToken(authHeader);
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Invalid authorization"));
            }
            
            log.info("Creating room for app: {} by user: {}", appId, userId);
            RoomResponse response = roomService.createRoom(appId, userId, request);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            log.error("Error creating room", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Get room details
     * GET /room/v1/rooms/{roomId}
     */
    @GetMapping("/rooms/{roomId}")
    public ResponseEntity<?> getRoom(@PathVariable String roomId) {
        try {
            Optional<Room> roomOpt = roomService.getRoomById(roomId);
            if (roomOpt.isPresent()) {
                return ResponseEntity.ok(roomOpt.get());
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Room not found"));
        } catch (Exception e) {
            log.error("Error fetching room", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Update room settings
     * PUT /room/v1/rooms/{roomId}
     */
    @PutMapping("/rooms/{roomId}")
    public ResponseEntity<?> updateRoom(
            @PathVariable String roomId,
            @RequestHeader("Authorization") String authHeader,
            @RequestBody CreateRoomRequest request) {
        try {
            String userId = extractUserIdFromToken(authHeader);
            
            Optional<Room> roomOpt = roomService.getRoomById(roomId);
            if (roomOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Room not found"));
            }
            
            Room room = roomOpt.get();
            if (!room.getOwnerId().equals(userId) && !room.getAdmins().contains(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "You don't have permission to update this room"));
            }
            
            RoomResponse response = roomService.updateRoom(roomId, request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error updating room", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Delete room
     * DELETE /room/v1/rooms/{roomId}
     */
    @DeleteMapping("/rooms/{roomId}")
    public ResponseEntity<?> deleteRoom(
            @PathVariable String roomId,
            @RequestHeader("Authorization") String authHeader) {
        try {
            String userId = extractUserIdFromToken(authHeader);
            
            Optional<Room> roomOpt = roomService.getRoomById(roomId);
            if (roomOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Room not found"));
            }
            
            Room room = roomOpt.get();
            if (!room.getOwnerId().equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Only owner can delete room"));
            }
            
            boolean deleted = roomService.deleteRoom(roomId);
            if (deleted) {
                return ResponseEntity.ok(Map.of("message", "Room deleted successfully"));
            }
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to delete room"));
        } catch (Exception e) {
            log.error("Error deleting room", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Add admin to room
     * POST /room/v1/rooms/{roomId}/admins
     */
    @PostMapping("/rooms/{roomId}/admins")
    public ResponseEntity<?> addAdmin(
            @PathVariable String roomId,
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody AddAdminRequest request) {
        try {
            String userId = extractUserIdFromToken(authHeader);
            
            // Check if requester is owner or admin with manage-admins permission
            Optional<Room> roomOpt = roomService.getRoomById(roomId);
            if (roomOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Room not found"));
            }
            
            Room room = roomOpt.get();
            if (!room.getOwnerId().equals(userId) && 
                !roomAdminService.hasPermission(roomId, userId, "canManageAdmins")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "You don't have permission to manage admins"));
            }
            
            RoomAdmin admin = roomAdminService.addAdmin(roomId, request.getUserId(), userId, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(admin);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error adding admin", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Remove admin from room
     * DELETE /room/v1/rooms/{roomId}/admins/{userId}
     */
    @DeleteMapping("/rooms/{roomId}/admins/{userId}")
    public ResponseEntity<?> removeAdmin(
            @PathVariable String roomId,
            @PathVariable String userId,
            @RequestHeader("Authorization") String authHeader) {
        try {
            String requesterUserId = extractUserIdFromToken(authHeader);
            
            Optional<Room> roomOpt = roomService.getRoomById(roomId);
            if (roomOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Room not found"));
            }
            
            Room room = roomOpt.get();
            if (!room.getOwnerId().equals(requesterUserId) && 
                !roomAdminService.hasPermission(roomId, requesterUserId, "canManageAdmins")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "You don't have permission to manage admins"));
            }
            
            boolean removed = roomAdminService.removeAdmin(roomId, userId);
            if (removed) {
                return ResponseEntity.ok(Map.of("message", "Admin removed successfully"));
            }
            
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Admin not found"));
        } catch (Exception e) {
            log.error("Error removing admin", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Get room admins
     * GET /room/v1/rooms/{roomId}/admins
     */
    @GetMapping("/rooms/{roomId}/admins")
    public ResponseEntity<?> getRoomAdmins(@PathVariable String roomId) {
        try {
            List<RoomAdmin> admins = roomAdminService.getRoomAdmins(roomId);
            return ResponseEntity.ok(admins);
        } catch (Exception e) {
            log.error("Error fetching room admins", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Health check
     * GET /room/v1/health
     */
    @GetMapping("/health")
    public ResponseEntity<?> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "room-service",
                "timestamp", System.currentTimeMillis()
        ));
    }
    
    /**
     * Extract userId from JWT token (simplified - in production, call auth service)
     */
    private String extractUserIdFromToken(String authHeader) {
        // This is a placeholder - in production, validate with auth service
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return "user_extracted_from_token"; // TODO: Call auth service to verify
        }
        return null;
    }
}
