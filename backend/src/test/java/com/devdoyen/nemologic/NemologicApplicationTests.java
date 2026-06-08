package com.devdoyen.nemologic;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("test")
public class NemologicApplicationTests {

    @Autowired
    private Environment environment;

    @Test
    void contextLoadsInTestProfile() {
        String activeProfile = environment.getActiveProfiles()[0];
        assertEquals("test", activeProfile);

        String driver = environment.getProperty("spring.datasource.driver-class-name");
        assertEquals("org.h2.Driver", driver);

        String ddlAuto = environment.getProperty("spring.jpa.hibernate.ddl-auto");
        assertEquals("create-drop", ddlAuto);
    }
}
