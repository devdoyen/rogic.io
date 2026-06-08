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
        return stageRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Stage> getStageById(Long id) {
        return stageRepository.findById(id);
    }
}
