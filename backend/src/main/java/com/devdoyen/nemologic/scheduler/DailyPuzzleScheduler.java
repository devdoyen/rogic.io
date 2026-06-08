package com.devdoyen.nemologic.scheduler;

import com.devdoyen.nemologic.service.AiStageGenerator;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class DailyPuzzleScheduler {

    private final AiStageGenerator aiStageGenerator;

    public DailyPuzzleScheduler(AiStageGenerator aiStageGenerator) {
        this.aiStageGenerator = aiStageGenerator;
    }

    @Scheduled(cron = "0 0 0 * * ?")
    public void generateDailyPuzzle() {
        aiStageGenerator.generateAndSaveStage();
    }
}
