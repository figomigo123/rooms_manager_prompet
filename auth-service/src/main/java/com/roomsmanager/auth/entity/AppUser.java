package com.roomsmanager.auth.entity;

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
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "app_users")
@CompoundIndexes({
    @CompoundIndex(name = "appId_userId", def = "{'appId': 1, 'userId': 1}", unique = true),
    @CompoundIndex(name = "appId_email", def = "{'appId': 1, 'email': 1}", unique = true)
})
public class AppUser {
    
    @Id
    private String id;
    
    private String userId;
    
    private String appId;
    
    @Indexed
    private String email;
    
    private String passwordHash;
    
    private String name;
    
    @Builder.Default
    private String role = "USER"; // ADMIN, MODERATOR, USER
    
    @Builder.Default
    private List<String> permissions = List.of();
    
    private Map<String, Object> metadata;
    
    @Builder.Default
    private String status = "ACTIVE"; // ACTIVE, INACTIVE, SUSPENDED
    
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();
    
    @Builder.Default
    private LocalDateTime lastLoginAt = LocalDateTime.now();
}
