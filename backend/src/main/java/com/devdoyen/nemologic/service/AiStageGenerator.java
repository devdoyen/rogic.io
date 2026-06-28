package com.devdoyen.nemologic.service;

import com.devdoyen.nemologic.client.AiClient;
import com.devdoyen.nemologic.model.Stage;
import com.devdoyen.nemologic.repository.StageRepository;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.transaction.annotation.Transactional;
import com.fasterxml.jackson.databind.JsonNode;

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
        return generateAndSaveStage(5, 5, true);
    }
 
    @Transactional
    public Stage generateAndSaveStage(boolean active) {
        return generateAndSaveStage(5, 5, active);
    }

    @Transactional
    public Stage generateAndSaveStage(int width, int height, boolean active) {
        int maxAttempts = 5;
        Exception lastException = null;

        java.util.List<Stage> recentStages = stageRepository.findTop10ByOrderByIdDesc();
        java.util.List<String> recentThemes = new java.util.ArrayList<>();
        for (Stage s : recentStages) {
            if (s.getName() != null) {
                recentThemes.add(s.getName());
            }
        }

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                String json = aiClient.generatePuzzleJson(width, height, recentThemes);
                if (json == null || json.isEmpty()) {
                    throw new IllegalArgumentException("AI response is empty");
                }

                java.util.List<AiResponseDto> candidates = new java.util.ArrayList<>();
                String trimmedJson = json.trim();
                if (trimmedJson.startsWith("[")) {
                    com.fasterxml.jackson.core.type.TypeReference<java.util.List<AiResponseDto>> typeRef = 
                        new com.fasterxml.jackson.core.type.TypeReference<java.util.List<AiResponseDto>>() {};
                    candidates = objectMapper.readValue(trimmedJson, typeRef);
                } else if (trimmedJson.startsWith("{")) {
                    AiResponseDto singleDto = objectMapper.readValue(trimmedJson, AiResponseDto.class);
                    candidates.add(singleDto);
                } else {
                    throw new IllegalArgumentException("Invalid JSON format from AI");
                }

                java.util.List<ValidatedCandidate> validatedList = new java.util.ArrayList<>();
                for (AiResponseDto dto : candidates) {
                    try {
                        if (dto.getGrid() == null) continue;
                        int[][] grid;
                        if (dto.getGrid().isTextual()) {
                            grid = objectMapper.readValue(dto.getGrid().asText(), int[][].class);
                        } else {
                            grid = objectMapper.convertValue(dto.getGrid(), int[][].class);
                        }

                        validateGrid(grid, dto.getWidth(), dto.getHeight());

                        if (dto.getWidth() != width || dto.getHeight() != height) {
                            continue;
                        }

                        if (stageRepository.existsBySolutionGrid(grid)) {
                            continue;
                        }

                        ValidatedCandidate vc = new ValidatedCandidate();
                        vc.dto = dto;
                        vc.grid = grid;
                        vc.isLogicalOnly = nonogramSolver.isLogicalOnly(grid);
                        if (vc.isLogicalOnly) {
                            validatedList.add(vc);
                        }
                    } catch (Exception e) {
                        // ignore invalid candidates
                    }
                }

                ValidatedCandidate selected = null;
                // Stage 1: Logical-only
                for (ValidatedCandidate vc : validatedList) {
                    if (vc.isLogicalOnly) {
                        selected = vc;
                        break;
                    }
                }

                if (selected == null) {
                    throw new IllegalArgumentException("No valid logical-only nonogram puzzle found among AI candidates");
                }

                AiResponseDto selectedDto = selected.dto;
                int[][] selectedGrid = selected.grid;

                String rawName = selectedDto.getName();
                String cleanName = rawName != null ? rawName.replaceAll("^(?i)(AI\\s+Puzzle|Daily\\s+Puzzle)[:\\s-]*", "").trim() : "Puzzle";
                if (cleanName.isEmpty()) {
                    cleanName = "Puzzle";
                }

                Stage newStage = new Stage(null, cleanName, selectedDto.getWidth(), selectedDto.getHeight(), selectedGrid);
                newStage.setActive(active);
                newStage.setApproved(true);
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


    public static class AiResponseDto {
        private String name;
        private int width;
        private int height;
        private JsonNode grid;

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

        public JsonNode getGrid() {
            return grid;
        }

        public void setGrid(JsonNode grid) {
            this.grid = grid;
        }
    }

    private static class ValidatedCandidate {
        AiResponseDto dto;
        int[][] grid;
        boolean isLogicalOnly;
    }
}
