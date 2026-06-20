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
                long currentBuffer = stageService.getInactiveApprovedCount(size, size);
                long targetBuffer = 5;
                if (currentBuffer < targetBuffer) {
                    long needed = targetBuffer - currentBuffer;
                    System.out.println("[Scheduler] Size " + size + "x" + size + " pool size is " + currentBuffer + ". Refilling " + needed + " puzzles...");
                    for (int i = 0; i < needed; i++) {
                        aiStageGenerator.generateAndSaveStage(size, size, false);
                    }
                } else {
                    System.out.println("[Scheduler] Size " + size + "x" + size + " pool size is " + currentBuffer + " (sufficient).");
                }
            } catch (Exception e) {
                System.err.println("[Scheduler] Failed to verify/refill daily puzzle of size " + size + "x" + size + ": " + e.getMessage());
            }
        }
    }

    @Scheduled(cron = "0 0 0 * * ?")
    public void releaseDailyPuzzle() {
        stageService.releaseDailyPuzzles();
    }
}
