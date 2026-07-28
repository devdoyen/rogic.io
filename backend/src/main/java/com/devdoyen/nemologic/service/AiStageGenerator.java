package com.devdoyen.nemologic.service;

import com.devdoyen.nemologic.client.AiClient;
import com.devdoyen.nemologic.model.Stage;
import com.devdoyen.nemologic.repository.StageRepository;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.transaction.annotation.Transactional;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class AiStageGenerator {

    private final AiClient aiClient;
    private final StageRepository stageRepository;
    private final NonogramSolver nonogramSolver;
    private final com.devdoyen.nemologic.repository.ThemePoolRepository themePoolRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final java.util.Map<Integer, java.util.List<ThemeDto>> STATIC_FALLBACK_THEMES = new java.util.HashMap<>();
    static {
        STATIC_FALLBACK_THEMES.put(5, java.util.List.of(
            new ThemeDto("Apple", "A simple apple shape with a small leaf/stem at the top center."),
            new ThemeDto("Smile", "A happy smiling face with eyes and a curved mouth."),
            new ThemeDto("Cup", "A basic cup/mug silhouette with a handle on the right side."),
            new ThemeDto("Key", "A small key outline with teeth at the bottom."),
            new ThemeDto("Star", "A classic five-pointed star silhouette."),
            new ThemeDto("Tree", "A simple pine tree with a trunk at the bottom.")
        ));
        STATIC_FALLBACK_THEMES.put(10, java.util.List.of(
            new ThemeDto("Sailboat", "A sailboat floating on water with sails pointing up."),
            new ThemeDto("Mushroom", "A cute woodland mushroom with a wide cap and spots."),
            new ThemeDto("Rocket", "A rocket ship pointing diagonally up into space."),
            new ThemeDto("Penguin", "A cute little penguin standing facing forward."),
            new ThemeDto("Teapot", "A steaming teapot outline with handle and spout."),
            new ThemeDto("Guitar", "A simple guitar silhouette with neck and body.")
        ));
        STATIC_FALLBACK_THEMES.put(15, java.util.List.of(
            new ThemeDto("Eiffel Tower", "A recognizable silhouette of the Eiffel Tower."),
            new ThemeDto("Gamepad", "A retro controller layout with D-pad and buttons."),
            new ThemeDto("Owl on Branch", "A cute owl sitting on a branch under the moon."),
            new ThemeDto("Pizza Slice", "A triangular slice of pepperoni pizza showing toppings."),
            new ThemeDto("Hot Air Balloon", "A large hot air balloon floating with a basket below."),
            new ThemeDto("Anchor", "A classic navy anchor with crossbar and hooks.")
        ));
    }

    public AiStageGenerator(AiClient aiClient, StageRepository stageRepository, NonogramSolver nonogramSolver, com.devdoyen.nemologic.repository.ThemePoolRepository themePoolRepository) {
        this.aiClient = aiClient;
        this.stageRepository = stageRepository;
        this.nonogramSolver = nonogramSolver;
        this.themePoolRepository = themePoolRepository;
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

        java.util.List<Stage> recentStages = stageRepository.findTop50ByOrderByIdDesc();
        java.util.List<String> recentThemes = new java.util.ArrayList<>();
        for (Stage s : recentStages) {
            if (s.getName() != null) {
                recentThemes.add(s.getName());
            }
        }

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                ThemeDto theme = getOrGenerateTheme(width, height, recentThemes);
                String json = aiClient.generatePuzzleJsonForTheme(width, height, theme.getName(), theme.getDescription());
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

                        String rawName = dto.getName();
                        String cleanName = rawName != null ? rawName.replaceAll("^(?i)(AI\\s+Puzzle|Daily\\s+Puzzle)[:\\s-]*", "").trim() : "Puzzle";
                        if (cleanName.isEmpty()) {
                            cleanName = "Puzzle";
                        }

                        if (stageRepository.existsBySolutionGrid(grid)) {
                            continue;
                        }

                        if (stageRepository.existsByNameIgnoreCase(cleanName)) {
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
                newStage.setGeneratorVersion("V3");
                return stageRepository.save(newStage);
            } catch (Exception e) {
                lastException = e;
        log.warn("[AI] Attempt {} failed: {}", attempt, e.getMessage());
    }
        }

        throw new IllegalArgumentException("Failed to generate valid stage after " + maxAttempts + " attempts", lastException);
    }

    private ThemeDto getOrGenerateTheme(int width, int height, java.util.List<String> recentThemes) {
        try {
            org.springframework.data.domain.PageRequest pageRequest = org.springframework.data.domain.PageRequest.of(0, 1);
            java.util.List<com.devdoyen.nemologic.model.ThemePool> unusedThemes = 
                themePoolRepository.findFirstUnused(width, height, pageRequest);

            if (unusedThemes != null && !unusedThemes.isEmpty()) {
                com.devdoyen.nemologic.model.ThemePool theme = unusedThemes.get(0);
                theme.setUsed(true);
                themePoolRepository.save(theme);
                return new ThemeDto(theme.getName(), theme.getDescription());
            }
        } catch (Exception e) {
            log.warn("[AI] Failed to fetch theme from DB: {}", e.getMessage());
        }

        log.warn("[AI] DB Theme Pool is exhausted for size {}x{}. Falling back to static themes.", width, height);
        java.util.List<ThemeDto> staticList = STATIC_FALLBACK_THEMES.get(width);
        if (staticList == null || staticList.isEmpty()) {
            staticList = STATIC_FALLBACK_THEMES.get(10);
        }
        int randomIndex = new java.util.Random().nextInt(staticList.size());
        ThemeDto baseFallback = staticList.get(randomIndex);
        String randomName = baseFallback.getName() + " " + (100 + new java.util.Random().nextInt(900));
        return new ThemeDto(randomName, baseFallback.getDescription());
    }

    private void validateGrid(int[][] grid, int expectedWidth, int expectedHeight) {
        if (grid == null) {
            throw new IllegalArgumentException("Grid is null");
        }
        if (grid.length != expectedHeight) {
            throw new IllegalArgumentException("Grid height mismatch. Expected: " + expectedHeight + ", Actual: " + grid.length);
        }
        int filledCount = 0;
        for (int r = 0; r < expectedHeight; r++) {
            if (grid[r] == null || grid[r].length != expectedWidth) {
                throw new IllegalArgumentException("Grid width mismatch at row " + r + ". Expected: " + expectedWidth);
            }
            for (int c = 0; c < expectedWidth; c++) {
                int val = grid[r][c];
                if (val != 0 && val != 1) {
                    throw new IllegalArgumentException("Invalid cell value: " + val + " at (" + r + ", " + c + "). Must be 0 or 1.");
                }
                if (val == 1) {
                    filledCount++;
                }
            }
        }
        if (expectedWidth >= 5 && expectedHeight >= 5) {
            double density = (double) filledCount / (expectedWidth * expectedHeight);
            if (density < 0.20 || density > 0.80) {
                throw new IllegalArgumentException("Grid density " + density + " is out of valid bounds (0.20 to 0.80)");
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

    public static class ThemeDto {
        private String name;
        private String description;

        public ThemeDto() {}
        public ThemeDto(String name, String description) {
            this.name = name;
            this.description = description;
        }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }
}
