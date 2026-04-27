package com.roomsmanager.room.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddAdminRequest {
    
    private String userId;
    
    private Boolean canKickUsers;
    
    private Boolean canMuteUsers;
    
    private Boolean canStartRecording;
    
    private Boolean canManageWhiteboard;
    
    private Boolean canManageAdmins;
}
