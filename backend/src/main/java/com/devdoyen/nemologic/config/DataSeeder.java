package com.devdoyen.nemologic.config;

import com.devdoyen.nemologic.model.Stage;
import com.devdoyen.nemologic.model.User;
import com.devdoyen.nemologic.repository.StageRepository;
import com.devdoyen.nemologic.repository.UserRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import org.springframework.core.env.Environment;

import java.io.InputStream;
import java.util.List;

@Component
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final StageRepository stageRepository;
    private final ObjectMapper objectMapper;
    private final Environment env;

    public DataSeeder(UserRepository userRepository, StageRepository stageRepository, ObjectMapper objectMapper, Environment env) {
        this.userRepository = userRepository;
        this.stageRepository = stageRepository;
        this.objectMapper = objectMapper;
        this.env = env;
    }

    @Override
    public void run(String... args) throws Exception {
        // Seed default users if database is empty
        if (userRepository.count() == 0) {
            userRepository.save(new User(null, "Player1", 200, 2));
            userRepository.save(new User(null, "Player2", 500, 3));
            userRepository.save(new User(null, "Player3", 1000, 5));
        }

        java.util.List<String> activeProfiles = java.util.Arrays.asList(env.getActiveProfiles());
        boolean isTestOrDevProfile = activeProfiles.contains("test")
                || activeProfiles.contains("local")
                || activeProfiles.contains("stage")
                || activeProfiles.isEmpty();

        // Seed stages from puzzles/stages.json directly to support GraalVM Native Image
        ClassPathResource resource = new ClassPathResource("puzzles/stages.json");
        if (resource.exists()) {
            try (InputStream is = resource.getInputStream()) {
                List<StageDto> dtos = objectMapper.readValue(is, new TypeReference<List<StageDto>>() {});
                for (StageDto dto : dtos) {
                    java.util.Optional<Stage> existingStageOpt = stageRepository.findByName(dto.getName());
                    if (existingStageOpt.isEmpty()) {
                        Stage stage = new Stage(null, dto.getName(), dto.getWidth(), dto.getHeight(), dto.getSolutionGrid());
                        if (isTestOrDevProfile) {
                            stage.setActive(true);
                            stage.setApproved(true);
                        } else {
                            stage.setActive(dto.isActive());
                            stage.setApproved(dto.isApproved());
                        }
                        stageRepository.save(stage);
                    } else {
                        // Self-Healing: Update existing stages to active=true, approved=true if in dev/test/staging environments
                        Stage stage = existingStageOpt.get();
                        if (isTestOrDevProfile && (!stage.isActive() || !stage.approved())) {
                            stage.setActive(true);
                            stage.setApproved(true);
                            stageRepository.save(stage);
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("[Seeder] Failed to seed resource " + resource.getFilename() + ": " + e.getMessage());
            }
        } else {
            System.err.println("[Seeder] Resource puzzles/stages.json does not exist");
        }
    }

    public static class StageDto {
        private String name;
        private int width;
        private int height;
        private int[][] solutionGrid;
        private boolean active = true;
        private boolean approved = true;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public int getWidth() { return width; }
        public void setWidth(int width) { this.width = width; }
        public int getHeight() { return height; }
        public void setHeight(int height) { this.height = height; }
        public int[][] getSolutionGrid() { return solutionGrid; }
        public void setSolutionGrid(int[][] solutionGrid) { this.solutionGrid = solutionGrid; }
        public boolean isActive() { return active; }
        public void setActive(boolean active) { this.active = active; }
        public boolean isApproved() { return approved; }
        public void setApproved(boolean approved) { this.approved = approved; }
    }
}
