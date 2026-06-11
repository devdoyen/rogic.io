package com.devdoyen.nemologic.service;

import com.devdoyen.nemologic.model.Stage;
import com.devdoyen.nemologic.repository.StageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class StageService {

    private final StageRepository stageRepository;

    public StageService(StageRepository stageRepository) {
        this.stageRepository = stageRepository;
    }

    @Transactional(readOnly = true)
    public List<Stage> getAllStages() {
        return stageRepository.findByActive(true);
    }

    @Transactional
    public void activateAllInactiveStages() {
        List<Stage> inactive = stageRepository.findByActive(false);
        for (Stage stage : inactive) {
            stage.setActive(true);
            stageRepository.save(stage);
        }
    }

    @Transactional(readOnly = true)
    public Optional<Stage> getStageById(Long id) {
        return stageRepository.findById(id);
    }

    @Transactional
    public void startStage(Long id) {
        Stage stage = stageRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Stage not found: " + id));
        stage.setTotalAttempts(stage.getTotalAttempts() + 1);
        stageRepository.save(stage);
    }

    @Transactional
    public void recordClear(Long id, int elapsedTime) {
        Stage stage = stageRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Stage not found: " + id));
        
        int currentClears = stage.getTotalClears();
        double currentAvg = stage.getAverageElapsedTime();
        
        double newAvg;
        if (currentClears == 0) {
            newAvg = elapsedTime;
        } else {
            newAvg = (currentAvg * currentClears + elapsedTime) / (currentClears + 1);
        }
        
        stage.setTotalClears(currentClears + 1);
        stage.setAverageElapsedTime(newAvg);
        stageRepository.save(stage);
    }
}
