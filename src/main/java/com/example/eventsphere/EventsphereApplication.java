package com.example.eventsphere;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync   // enables @Async so emails are sent on a background thread
public class EventsphereApplication {
    public static void main(String[] args) {
        SpringApplication.run(EventsphereApplication.class, args);
    }
}
