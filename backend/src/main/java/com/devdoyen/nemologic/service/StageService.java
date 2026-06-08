package com.devdoyen.nemologic.service;

import com.devdoyen.nemologic.model.Stage;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class StageService {

    public List<Stage> getAllStages() {
        // Stub for TDD (Red phase)
        return Collections.emptyList();
    }

    public Optional<Stage> getStageById(Long id) {
        // Stub for TDD (Red phase)
        return Optional.empty();
    }
}
