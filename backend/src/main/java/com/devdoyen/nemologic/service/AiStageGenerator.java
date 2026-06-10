package com.devdoyen.nemologic.service;

import com.devdoyen.nemologic.client.AiClient;
import com.devdoyen.nemologic.model.Stage;
import com.devdoyen.nemologic.repository.StageRepository;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.io.IOException;

@Service
public class AiStageGenerator {

    private final AiClient aiClient;
    private final StageRepository stageRepository;
    private final NonogramSolver nonogramSolver;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AiStageGenerator(AiClient aiClient, StageRepository stageRepository, NonogramSolver nonogramSolver) {
        this.aiClient = aiClient;
        this.stageRepository = stageRepository;
        this.nonogramSolver = nonogramSolver;
    }

    @Transactional
    public Stage generateAndSaveStage() {
        int maxAttempts = 3;
        Exception lastException = null;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                String json = aiClient.generateDailyPuzzleJson();
                if (json == null || json.isEmpty()) {
                    throw new IllegalArgumentException("AI response is empty");
                }

                AiResponseDto dto = objectMapper.readValue(json, AiResponseDto.class);
                int[][] grid = objectMapper.readValue(dto.getGrid(), int[][].class);

                validateGrid(grid, dto.getWidth(), dto.getHeight());

                if (!nonogramSolver.isUnique(grid)) {
                    throw new IllegalArgumentException("Generated puzzle does not have a unique solution");
                }

                Stage newStage = new Stage(null, dto.getName(), dto.getWidth(), dto.getHeight(), grid);
                return stageRepository.save(newStage);
            } catch (Exception e) {
                lastException = e;
                System.err.println("[AI] Attempt " + attempt + " failed: " + e.getMessage());
            }
        }

        throw new IllegalArgumentException("Failed to generate valid stage after " + maxAttempts + " attempts", lastException);
    }

    private void validateGrid(int[][] grid, int expectedWidth, int expectedHeight) {
        if (grid == null) {
            throw new IllegalArgumentException("Grid is null");
        }
        if (grid.length != expectedHeight) {
            throw new IllegalArgumentException("Grid height mismatch. Expected: " + expectedHeight + ", Actual: " + grid.length);
        }
        for (int r = 0; r < expectedHeight; r++) {
            if (grid[r] == null || grid[r].length != expectedWidth) {
                throw new IllegalArgumentException("Grid width mismatch at row " + r + ". Expected: " + expectedWidth);
            }
            for (int c = 0; c < expectedWidth; c++) {
                int val = grid[r][c];
                if (val != 0 && val != 1) {
                    throw new IllegalArgumentException("Invalid cell value: " + val + " at (" + r + ", " + c + "). Must be 0 or 1.");
                }
            }
        }
    }

    private static class AiResponseDto {
        private String name;
        private int width;
        private int height;
        private String grid;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getWidth() {
            return width;
        }

        public void setWidth(int width) {
            this.width = width;
        }

        public int getHeight() {
            return height;
        }

        public void setHeight(int height) {
            this.height = height;
        }

        public String getGrid() {
            return grid;
        }

        public void setGrid(String grid) {
            this.grid = grid;
        }
    }
}
