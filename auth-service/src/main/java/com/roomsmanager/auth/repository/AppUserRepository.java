package com.roomsmanager.auth.repository;

import com.roomsmanager.auth.entity.AppUser;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface AppUserRepository extends MongoRepository<AppUser, String> {
    Optional<AppUser> findByAppIdAndUserId(String appId, String userId);
    Optional<AppUser> findByAppIdAndEmail(String appId, String email);
    Optional<AppUser> findByEmail(String email);
    List<AppUser> findByAppId(String appId);
}
