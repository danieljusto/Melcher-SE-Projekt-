package com.group_2;

import com.group_2.service.WGService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.ArrayList;
import java.util.List;

@SpringBootApplication
public class Main {

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

    @Bean
    public CommandLineRunner demo(WGService wgService) {
        return (args) -> {
            User admin = new User("Alice");
            Room room1 = new Room("Living Room");
            Room room2 = new Room("Kitchen");
            List<Room> rooms = new ArrayList<>();
            rooms.add(room1);
            rooms.add(room2);

            WG wg = wgService.createWG("My WG", admin, rooms);

            // Add another user
            User bob = new User("Bob");
            wgService.addMitbewohner(wg.getId(), bob); // Assuming getId() exists, need to check entity

            System.out.println("WG persisted successfully!");

            List<WG> wgs = wgService.getAllWGs();
            System.out.println("WG count: " + wgs.size());
        };
    }
}