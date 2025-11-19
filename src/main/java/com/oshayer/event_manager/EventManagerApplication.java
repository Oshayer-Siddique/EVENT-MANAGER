package com.oshayer.event_manager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class EventManagerApplication {

    public static void main(String[] args) {
        SpringApplication.run(EventManagerApplication.class, args);
    }

}
