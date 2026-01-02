package com.group_2.service.core;

import com.group_2.dto.cleaning.CleaningMapper;
import com.group_2.dto.cleaning.RoomDTO;
import com.group_2.model.WG;
import com.group_2.model.cleaning.Room;
import com.group_2.repository.WGRepository;
import com.group_2.repository.cleaning.RoomRepository;
import com.group_2.service.cleaning.RoomService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Core boundary service for household setup operations. Provides a facade over
 * domain-specific services (e.g., cleaning) so that core UI controllers don't
 * depend on feature domain services directly.
 */
@Service
public class HouseholdSetupService {

    private final RoomService roomService;
    private final CleaningMapper cleaningMapper;
    private final WGRepository wgRepository;
    private final RoomRepository roomRepository;

    @Autowired
    public HouseholdSetupService(RoomService roomService, CleaningMapper cleaningMapper, WGRepository wgRepository,
            RoomRepository roomRepository) {
        this.roomService = roomService;
        this.cleaningMapper = cleaningMapper;
        this.wgRepository = wgRepository;
        this.roomRepository = roomRepository;
    }

    // ========== Room Management (delegating to cleaning domain) ==========

    @Transactional
    public Room createRoom(String name) {
        return roomService.createRoom(name);
    }

    @Transactional
    public RoomDTO createRoomDTO(String name) {
        return cleaningMapper.toRoomDTO(createRoom(name));
    }

    public Optional<Room> getRoom(Long id) {
        return roomService.getRoom(id);
    }

    public List<Room> getAllRooms() {
        return roomService.getAllRooms();
    }

    public List<RoomDTO> getAllRoomsDTO() {
        return cleaningMapper.toRoomDTOList(getAllRooms());
    }

    public List<RoomDTO> getRoomsForWgDTO(WG wg) {
        if (wg == null || wg.getId() == null) {
            return List.of();
        }
        return cleaningMapper.toRoomDTOList(roomRepository.findByWgId(wg.getId()));
    }

    public List<RoomDTO> getRoomsForWgDTO(Long wgId) {
        if (wgId == null) {
            return List.of();
        }
        return cleaningMapper.toRoomDTOList(roomRepository.findByWgId(wgId));
    }

    @Transactional
    public Room updateRoom(Long id, String name) {
        return roomService.updateRoom(id, name);
    }

    @Transactional
    public RoomDTO updateRoomDTO(Long id, String name) {
        return cleaningMapper.toRoomDTO(updateRoom(id, name));
    }

    @Transactional
    public void deleteRoom(Long id) {
        roomService.deleteRoom(id);
    }

    // With full cleanup of associated data
    @Transactional
    public void deleteRoom(Room room, WG wg) {
        roomService.deleteRoom(room, wg);
    }

    @Transactional
    public void deleteRoomById(Long roomId, WG wg) {
        getRoom(roomId).ifPresent(room -> deleteRoom(room, wg));
    }

    @Transactional
    public void deleteRoomById(Long roomId, Long wgId) {
        if (wgId == null) {
            throw new RuntimeException("WG ID is required to delete room");
        }
        WG wg = wgRepository.findById(wgId).orElseThrow(() -> new RuntimeException("WG not found"));
        deleteRoomById(roomId, wg);
    }
}
