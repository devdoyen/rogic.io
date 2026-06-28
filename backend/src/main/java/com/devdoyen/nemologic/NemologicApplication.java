package com.devdoyen.nemologic;

import com.devdoyen.nemologic.config.NemologicRuntimeHints;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportRuntimeHints;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@ImportRuntimeHints(NemologicRuntimeHints.class)
public class NemologicApplication {
    public static void main(String[] args) {
        SpringApplication.run(NemologicApplication.class, args);
    }
}
