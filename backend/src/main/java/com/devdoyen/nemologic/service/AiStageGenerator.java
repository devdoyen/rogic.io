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
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AiStageGenerator(AiClient aiClient, StageRepository stageRepository) {
        this.aiClient = aiClient;
        this.stageRepository = stageRepository;
    }

    @Transactional
    public Stage generateAndSaveStage() {
        String json = aiClient.generateDailyPuzzleJson();
        if (json == null || json.isEmpty()) {
            throw new IllegalArgumentException("AI response is empty");
        }
        try {
            AiResponseDto dto = objectMapper.readValue(json, AiResponseDto.class);
            int[][] grid = objectMapper.readValue(dto.getGrid(), int[][].class);
            Stage newStage = new Stage(null, dto.getName(), dto.getWidth(), dto.getHeight(), grid);
            return stageRepository.save(newStage);
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to parse daily puzzle JSON", e);
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
