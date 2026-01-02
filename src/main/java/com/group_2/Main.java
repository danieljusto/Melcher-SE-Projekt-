package com.group_2;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EntityScan(basePackages = { "com.group_2" })
@EnableScheduling
public class Main {

    public static void main(String[] args) {
        // DB url: http://localhost:8080/h2-console"
        javafx.application.Application.launch(JavaFxApplication.class, args);
    }
}