package com.roomsmanager.room.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomResponse {
    
    private String roomId;
    
    private String roomName;
    
    private String description;
    
    private String ownerId;
    
    private List<String> admins;
    
    private Integer maxUsers;
    
    private Integer currentUsers;
    
    private String status;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    private LocalDateTime expiresAt;
    
    private RoomSettings settings;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RoomSettings {
        private Boolean audioEnabled;
        private Boolean videoEnabled;
        private Boolean screenshareEnabled;
        private Boolean whiteboardEnabled;
        private Boolean chatEnabled;
        private Boolean recordingEnabled;
    }
}
