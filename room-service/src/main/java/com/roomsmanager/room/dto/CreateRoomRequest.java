package com.roomsmanager.room.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Min;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateRoomRequest {
    
    @NotBlank(message = "Room name is required")
    private String roomName;
    
    private String description;
    
    private String password;
    
    @Builder.Default
    @Min(2)
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
}
