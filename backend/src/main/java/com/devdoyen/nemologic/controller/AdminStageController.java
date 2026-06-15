package com.devdoyen.nemologic.controller;

import com.devdoyen.nemologic.model.Stage;
import com.devdoyen.nemologic.service.StageService;
import com.devdoyen.nemologic.service.AiStageGenerator;
import com.devdoyen.nemologic.service.NonogramSolver;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/admin/stages")
public class AdminStageController {

    private final StageService stageService;
    private final AiStageGenerator aiStageGenerator;
    private final NonogramSolver nonogramSolver;

    public AdminStageController(StageService stageService, AiStageGenerator aiStageGenerator, NonogramSolver nonogramSolver) {
        this.stageService = stageService;
        this.aiStageGenerator = aiStageGenerator;
        this.nonogramSolver = nonogramSolver;
    }

    @GetMapping
    public List<Stage> getAllStages() {
        return stageService.getAllStagesForAdmin();
    }

    @PostMapping
    public ResponseEntity<?> createStage(@RequestBody Stage stage) {
        if (stage.getName() == null || stage.getName().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Stage name cannot be empty");
        }
        int[][] grid = stage.getSolutionGrid();
        if (grid == null || grid.length == 0 || grid[0].length == 0) {
            return ResponseEntity.badRequest().body("Grid cannot be empty");
        }
        if (grid.length != stage.getHeight() || grid[0].length != stage.getWidth()) {
            return ResponseEntity.badRequest().body("Grid dimensions mismatch");
        }

        // Validate cell values (must be 0 or 1)
        for (int r = 0; r < grid.length; r++) {
            for (int c = 0; c < grid[r].length; c++) {
                if (grid[r][c] != 0 && grid[r][c] != 1) {
                    return ResponseEntity.badRequest().body("Invalid cell value at (" + r + "," + c + "). Must be 0 or 1.");
                }
            }
        }

        // Ensure unique solution
        if (!nonogramSolver.isUnique(grid)) {
            return ResponseEntity.badRequest().body("The puzzle does not have a unique solution");
        }

        stage.setActive(true);
        stage.setApproved(true);
        Stage saved = stageService.saveStage(stage);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<Void> approveStage(@PathVariable Long id) {
        try {
            stageService.approveStage(id);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStage(@PathVariable Long id) {
        try {
            stageService.deleteStageSoft(id);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}/restore")
    public ResponseEntity<Void> restoreStage(@PathVariable Long id) {
        try {
            stageService.restoreStage(id);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/ai-generate")
    public ResponseEntity<Stage> triggerAiGeneration(
            @RequestParam(value = "width", defaultValue = "5") int width,
            @RequestParam(value = "height", defaultValue = "5") int height) {
        try {
            // Generate as inactive (active = false, approved = false) for admin approval
            Stage generated = aiStageGenerator.generateAndSaveStage(width, height, false);
            return ResponseEntity.ok(generated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
