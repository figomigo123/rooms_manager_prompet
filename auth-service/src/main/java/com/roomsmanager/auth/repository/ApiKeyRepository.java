package com.roomsmanager.auth.repository;

import com.roomsmanager.auth.entity.ApiKey;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface ApiKeyRepository extends MongoRepository<ApiKey, String> {
    Optional<ApiKey> findByKeyHash(String keyHash);
    List<ApiKey> findByAppId(String appId);
    List<ApiKey> findByAppIdAndActiveTrue(String appId);
}
