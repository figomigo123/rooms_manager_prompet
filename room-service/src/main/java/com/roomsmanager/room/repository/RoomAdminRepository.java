package com.roomsmanager.room.repository;

import com.roomsmanager.room.entity.RoomAdmin;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface RoomAdminRepository extends MongoRepository<RoomAdmin, String> {
    Optional<RoomAdmin> findByRoomIdAndUserId(String roomId, String userId);
    List<RoomAdmin> findByRoomId(String roomId);
    List<RoomAdmin> findByUserId(String userId);
}
