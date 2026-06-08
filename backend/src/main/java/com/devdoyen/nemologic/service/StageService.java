package com.devdoyen.nemologic.service;

import com.devdoyen.nemologic.model.Stage;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class StageService {

    private final List<Stage> stages = new ArrayList<>();

    public StageService() {
        // Stage 1: Heart Shape (5x5)
        stages.add(new Stage(
            1L,
            "Heart Shape",
            5,
            5,
            new int[][]{
                {0, 1, 0, 1, 0},
                {1, 1, 1, 1, 1},
                {1, 1, 1, 1, 1},
                {0, 1, 1, 1, 0},
                {0, 0, 1, 0, 0}
            }
        ));

        // Stage 2: Checkerboard (5x5)
        stages.add(new Stage(
            2L,
            "Checkerboard",
            5,
            5,
            new int[][]{
                {1, 0, 1, 0, 1},
                {0, 1, 0, 1, 0},
                {1, 0, 1, 0, 1},
                {0, 1, 0, 1, 0},
                {1, 0, 1, 0, 1}
            }
        ));
    }

    public List<Stage> getAllStages() {
        return stages;
    }

    public Optional<Stage> getStageById(Long id) {
        return stages.stream()
                .filter(stage -> stage.id().equals(id))
                .findFirst();
    }
}
