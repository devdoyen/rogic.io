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
        aiStageGenerator.generateAndSaveStage(false);
    }

    @Scheduled(cron = "0 0 0 * * ?")
    public void releaseDailyPuzzle() {
        stageService.activateAllInactiveStages();
    }
}
