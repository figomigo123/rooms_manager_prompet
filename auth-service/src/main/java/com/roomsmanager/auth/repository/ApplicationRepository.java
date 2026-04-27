package com.roomsmanager.auth.repository;

import com.roomsmanager.auth.entity.Application;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface ApplicationRepository extends MongoRepository<Application, String> {
    Optional<Application> findByAppId(String appId);
    Optional<Application> findByApiKey(String apiKey);
    List<Application> findByOwner(String owner);
}
