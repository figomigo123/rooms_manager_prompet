package com.roomsmanager.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import com.roomsmanager.entity.Usage;
import java.time.LocalDate;
import java.util.List;

public interface UsageRepository extends MongoRepository<Usage, String> {

    List<Usage> findByAppIdAndDate(String appId, LocalDate date);

    List<Usage> findByRoomIdAndUserId(String roomId, String userId);

    List<Usage> findByAppId(String appId);

    List<Usage> findByRoomId(String roomId);

    @Query("{ 'appId': ?0, 'date': { $gte: ?1, $lte: ?2 } }")
    List<Usage> findByAppIdAndDateBetween(String appId, LocalDate startDate, LocalDate endDate);
}
