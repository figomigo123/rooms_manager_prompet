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
@Document(collection = "api_keys")
public class ApiKey {
    
    @Id
    private String id;
    
    @Indexed
    private String appId;
    
    @Indexed(unique = true)
    private String keyHash;
    
    private String name;
    
    @Builder.Default
    private List<String> permissions = List.of();
    
    @Builder.Default
    private Integer rateLimit = 1000;
    
    private LocalDateTime lastUsedAt;
    
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    
    private LocalDateTime expiresAt;
    
    @Builder.Default
    private Boolean active = true;
}
