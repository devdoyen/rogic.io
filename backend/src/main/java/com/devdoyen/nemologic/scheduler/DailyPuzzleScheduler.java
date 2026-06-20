package com.devdoyen.nemologic.scheduler;

import com.devdoyen.nemologic.service.AiStageGenerator;
import com.devdoyen.nemologic.service.StageService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class DailyPuzzleScheduler {

    private final AiStageGenerator aiStageGenerator;
    private final StageService stageService;

    public DailyPuzzleScheduler(AiStageGenerator aiStageGenerator, StageService stageService) {
        this.aiStageGenerator = aiStageGenerator;
        this.stageService = stageService;
    }

    @Scheduled(cron = "0 17 4 * * ?")
    public void generateDailyPuzzle() {
        int[] sizes = {5, 10, 15, 20, 25, 30};
        for (int size : sizes) {
            try {
                aiStageGenerator.generateAndSaveStage(size, size, false);
            } catch (Exception e) {
                System.err.println("[Scheduler] Failed to generate daily puzzle of size " + size + "x" + size + ": " + e.getMessage());
            }
        }
    }

    // Automatic release is disabled to require manual admin approval
    public void releaseDailyPuzzle() {
        stageService.activateAllInactiveStages();
    }
}
