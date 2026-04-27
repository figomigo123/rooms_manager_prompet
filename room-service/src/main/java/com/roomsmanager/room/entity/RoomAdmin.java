package com.roomsmanager.room.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "room_admins")
@CompoundIndexes({
    @CompoundIndex(name = "roomId_userId", def = "{'roomId': 1, 'userId': 1}", unique = true)
})
public class RoomAdmin {
    
    @Id
    private String id;
    
    private String roomId;
    
    private String userId;
    
    private Map<String, Boolean> permissions;
    
    private String grantedBy; // userId who granted admin
    
    @Builder.Default
    private LocalDateTime grantedAt = LocalDateTime.now();
    
    public RoomAdmin initializePermissions() {
        if (this.permissions == null) {
            this.permissions = Map.of(
                "canKickUsers", true,
                "canMuteUsers", true,
                "canUnmuteUsers", true,
                "canStartRecording", true,
                "canStopRecording", true,
                "canManageWhiteboard", true,
                "canManageChat", true,
                "canManageAdmins", true
            );
        }
        return this;
    }
}
