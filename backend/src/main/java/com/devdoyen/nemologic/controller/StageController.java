package com.devdoyen.nemologic.controller;

import com.devdoyen.nemologic.model.Stage;
import com.devdoyen.nemologic.service.StageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/stages")
public class StageController {

    private final StageService stageService;
    private final com.devdoyen.nemologic.service.AiStageGenerator aiStageGenerator;

    public StageController(StageService stageService, com.devdoyen.nemologic.service.AiStageGenerator aiStageGenerator) {
        this.stageService = stageService;
        this.aiStageGenerator = aiStageGenerator;
    }

    @GetMapping
    public List<Stage> getAllStages() {
        return stageService.getAllStages();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Stage> getStageById(@PathVariable Long id) {
        return stageService.getStageById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @org.springframework.web.bind.annotation.PostMapping("/{id}/start")
    public ResponseEntity<Void> startStage(@PathVariable Long id) {
        try {
            stageService.startStage(id);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @org.springframework.web.bind.annotation.PostMapping("/ai-generate")
    public ResponseEntity<Stage> triggerAiGeneration() {
        try {
            Stage generated = aiStageGenerator.generateAndSaveStage();
            return ResponseEntity.ok(generated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
