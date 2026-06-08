package com.devdoyen.nemologic.service;

import com.devdoyen.nemologic.client.AiClient;
import com.devdoyen.nemologic.model.Stage;
import com.devdoyen.nemologic.repository.StageRepository;
import org.springframework.stereotype.Service;

@Service
public class AiStageGenerator {

    private final AiClient aiClient;
    private final StageRepository stageRepository;

    public AiStageGenerator(AiClient aiClient, StageRepository stageRepository) {
        this.aiClient = aiClient;
        this.stageRepository = stageRepository;
    }

    public Stage generateAndSaveStage() {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
