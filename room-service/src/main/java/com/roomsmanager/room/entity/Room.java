package com.roomsmanager.room.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "rooms")
@CompoundIndexes({
    @CompoundIndex(name = "appId_roomId", def = "{'appId': 1, 'roomId': 1}", unique = true),
    @CompoundIndex(name = "appId_status", def = "{'appId': 1, 'status': 1}")
})
public class Room {
    
    @Id
    private String id;
    
    @Indexed
    private String roomId;
    
    private String appId;
    
    private String roomName;
    
    private String description;
    
    private String ownerId; // userId from auth-service
    
    private List<String> admins; // List of user IDs
    
    private Settings settings;
    
    @Builder.Default
    private String status = "ACTIVE"; // ACTIVE, INACTIVE, ARCHIVED
    
    private String passwordHash; // Optional room password
    
    @Builder.Default
    private Integer currentUsers = 0;
    
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();
    
    private LocalDateTime expiresAt; // Optional expiration
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Settings {
        @Builder.Default
        private Integer maxUsers = 200;
        
        @Builder.Default
        private Boolean audioEnabled = true;
        
        @Builder.Default
        private Boolean videoEnabled = true;
        
        @Builder.Default
        private Boolean screenshareEnabled = true;
        
        @Builder.Default
        private Boolean whiteboardEnabled = true;
        
        @Builder.Default
        private Boolean chatEnabled = true;
        
        @Builder.Default
        private Boolean recordingEnabled = false;
        
        @Builder.Default
        private List<String> allowedRoles = List.of("USER", "ADMIN");
    }
}
