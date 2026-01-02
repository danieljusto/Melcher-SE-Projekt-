package com.group_2.repository.cleaning;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.group_2.model.WG;
import com.group_2.model.cleaning.Room;
import com.group_2.model.cleaning.RoomAssignmentQueue;

import jakarta.persistence.LockModeType;
import java.util.List;

/**
 * Repository for managing room assignment queues.
 */
@Repository
public interface RoomAssignmentQueueRepository extends JpaRepository<RoomAssignmentQueue, Long> {

    List<RoomAssignmentQueue> findByWgAndRoom(WG wg, Room room);

    // With lock to prevent concurrent queue rotation issues
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT q FROM RoomAssignmentQueue q WHERE q.wg = :wg AND q.room = :room")
    List<RoomAssignmentQueue> findByWgAndRoomForUpdate(@Param("wg") WG wg, @Param("room") Room room);

    List<RoomAssignmentQueue> findByWg(WG wg);

    void deleteByWg(WG wg);

    void deleteByRoom(Room room);

    // Used to determine offset for new rooms
    long countByWg(WG wg);
}
