package com.devdoyen.nemologic.repository;

import com.devdoyen.nemologic.model.Stage;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class StageRepositoryTest {

    @Autowired
    private StageRepository stageRepository;

    @Test
    public void testSaveAndFindStage() {
        int[][] grid = {
            {0, 1, 0},
            {1, 1, 1},
            {0, 1, 0}
        };
        Stage stage = new Stage(null, "TestStage", 3, 3, grid);
        Stage savedStage = stageRepository.save(stage);

        assertNotNull(savedStage.getId());

        Stage foundStage = stageRepository.findById(savedStage.getId()).orElse(null);
        assertNotNull(foundStage);
        // Green Phase: Assert actual value
        assertEquals("TestStage", foundStage.getName());
    }
}
