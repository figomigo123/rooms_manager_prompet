package com.roomsmanager.room.repository;

import com.roomsmanager.room.entity.Room;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface RoomRepository extends MongoRepository<Room, String> {
    Optional<Room> findByRoomId(String roomId);
    Optional<Room> findByAppIdAndRoomId(String appId, String roomId);
    List<Room> findByAppId(String appId);
    List<Room> findByAppIdAndStatus(String appId, String status);
    List<Room> findByOwnerId(String ownerId);
}
