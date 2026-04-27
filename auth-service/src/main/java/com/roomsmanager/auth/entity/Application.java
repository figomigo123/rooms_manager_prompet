package com.roomsmanager.auth.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "applications")
public class Application {
    
    @Id
    private String id;
    
    @Indexed(unique = true)
    private String appId;
    
    private String appSecret;
    
    private String appName;
    
    private String owner;
    
    @Builder.Default
    private String plan = "FREE"; // FREE, PROFESSIONAL, ENTERPRISE
    
    private String apiKey;
    
    private String webhookUrl;
    
    private Limits limits;
    
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();
    
    @Builder.Default
    private Boolean active = true;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Limits {
        @Builder.Default
        private Integer maxRooms = 100;
        
        @Builder.Default
        private Integer maxUsersPerRoom = 200;
        
        @Builder.Default
        private Integer maxMinutesMonth = 10000;
    }
}
