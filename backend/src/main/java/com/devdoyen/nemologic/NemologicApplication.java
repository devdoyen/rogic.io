package com.devdoyen.nemologic;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class NemologicApplication {
    public static void main(String[] args) {
        SpringApplication.run(NemologicApplication.class, args);
    }
}
