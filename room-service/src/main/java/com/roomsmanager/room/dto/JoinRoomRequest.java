package com.roomsmanager.room.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JoinRoomRequest {
    
    private String userId;
    
    private String displayName;
    
    private String roomPassword; // If room is password protected
}
