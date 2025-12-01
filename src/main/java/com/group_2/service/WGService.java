package com.group_2.service;

import com.group_2.Room;
import com.group_2.User;
import com.group_2.WG;
import com.group_2.repository.WGRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class WGService {

    private final WGRepository wgRepository;

    @Autowired
    public WGService(WGRepository wgRepository) {
        this.wgRepository = wgRepository;
    }

    @Transactional
    public WG createWG(String name, User admin, List<Room> rooms) {
        WG wg = new WG(name, admin, rooms);
        return wgRepository.save(wg);
    }

    @Transactional
    public WG addMitbewohner(Long wgId, User user) {
        WG wg = wgRepository.findById(wgId).orElseThrow(() -> new RuntimeException("WG not found"));
        wg.addMitbewohner(user);
        return wgRepository.save(wg);
    }

    public List<WG> getAllWGs() {
        return wgRepository.findAll();
    }
}
