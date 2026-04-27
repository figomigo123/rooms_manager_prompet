package com.roomsmanager.room.service;

import com.roomsmanager.room.dto.AddAdminRequest;
import com.roomsmanager.room.entity.RoomAdmin;
import com.roomsmanager.room.repository.RoomAdminRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.List;

@Slf4j
@Service
public class RoomAdminService {
    
    @Autowired
    private RoomAdminRepository roomAdminRepository;
    
    /**
     * Add admin to room
     */
    public RoomAdmin addAdmin(String roomId, String userId, String grantedBy, AddAdminRequest request) {
        log.info("Adding admin {} to room {}", userId, roomId);
        
        // Check if already admin
        Optional<RoomAdmin> existingAdmin = roomAdminRepository.findByRoomIdAndUserId(roomId, userId);
        if (existingAdmin.isPresent()) {
            throw new IllegalArgumentException("User is already an admin of this room");
        }
        
        // Create permissions map
        Map<String, Boolean> permissions = new HashMap<>();
        permissions.put("canKickUsers", request.getCanKickUsers() != null ? request.getCanKickUsers() : true);
        permissions.put("canMuteUsers", request.getCanMuteUsers() != null ? request.getCanMuteUsers() : true);
        permissions.put("canUnmuteUsers", true);
        permissions.put("canStartRecording", request.getCanStartRecording() != null ? request.getCanStartRecording() : true);
        permissions.put("canStopRecording", true);
        permissions.put("canManageWhiteboard", request.getCanManageWhiteboard() != null ? request.getCanManageWhiteboard() : true);
        permissions.put("canManageChat", true);
        permissions.put("canManageAdmins", request.getCanManageAdmins() != null ? request.getCanManageAdmins() : false);
        
        RoomAdmin admin = RoomAdmin.builder()
                .roomId(roomId)
                .userId(userId)
                .grantedBy(grantedBy)
                .permissions(permissions)
                .build();
        
        RoomAdmin saved = roomAdminRepository.save(admin);
        log.info("Admin added successfully: {}", userId);
        return saved;
    }
    
    /**
     * Remove admin from room
     */
    public boolean removeAdmin(String roomId, String userId) {
        Optional<RoomAdmin> adminOpt = roomAdminRepository.findByRoomIdAndUserId(roomId, userId);
        
        if (adminOpt.isPresent()) {
            roomAdminRepository.deleteById(adminOpt.get().getId());
            log.info("Admin removed: {} from room: {}", userId, roomId);
            return true;
        }
        
        return false;
    }
    
    /**
     * Check if user is admin
     */
    public boolean isUserAdmin(String roomId, String userId) {
        return roomAdminRepository.findByRoomIdAndUserId(roomId, userId).isPresent();
    }
    
    /**
     * Get admin details
     */
    public Optional<RoomAdmin> getAdmin(String roomId, String userId) {
        return roomAdminRepository.findByRoomIdAndUserId(roomId, userId);
    }
    
    /**
     * Get all admins for a room
     */
    public List<RoomAdmin> getRoomAdmins(String roomId) {
        return roomAdminRepository.findByRoomId(roomId);
    }
    
    /**
     * Check if user has specific permission
     */
    public boolean hasPermission(String roomId, String userId, String permission) {
        Optional<RoomAdmin> adminOpt = roomAdminRepository.findByRoomIdAndUserId(roomId, userId);
        
        if (adminOpt.isPresent()) {
            RoomAdmin admin = adminOpt.get();
            Boolean hasPermission = admin.getPermissions().getOrDefault(permission, false);
            return hasPermission != null && hasPermission;
        }
        
        return false;
    }
    
    /**
     * Update admin permissions
     */
    public RoomAdmin updatePermissions(String roomId, String userId, Map<String, Boolean> newPermissions) {
        Optional<RoomAdmin> adminOpt = roomAdminRepository.findByRoomIdAndUserId(roomId, userId);
        
        if (adminOpt.isPresent()) {
            RoomAdmin admin = adminOpt.get();
            admin.setPermissions(newPermissions);
            return roomAdminRepository.save(admin);
        }
        
        return null;
    }
}
