package com.devdoyen.nemologic.service;

import com.devdoyen.nemologic.model.Stage;
import com.devdoyen.nemologic.repository.StageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
public class StageServiceTest {

    private StageRepository stageRepository;
    private StageService stageService;

    @BeforeEach
    public void setUp() {
        stageRepository = mock(StageRepository.class);
        stageService = new StageService(stageRepository);
    }

    @Test
    public void testStartStageIncrementsAttempts() {
        Stage stage = new Stage(1L, "Test Stage", 5, 5, new int[5][5]);
        stage.setTotalAttempts(5);

        when(stageRepository.findById(1L)).thenReturn(Optional.of(stage));
        when(stageRepository.save(any(Stage.class))).thenAnswer(invocation -> invocation.getArgument(0));

        stageService.startStage(1L);

        assertEquals(6, stage.getTotalAttempts());
        verify(stageRepository, times(1)).save(stage);
    }

    @Test
    public void testRecordClearFirstTimeSetsAverageTime() {
        Stage stage = new Stage(1L, "Test Stage", 5, 5, new int[5][5]);
        stage.setTotalClears(0);
        stage.setAverageElapsedTime(0.0);

        when(stageRepository.findById(1L)).thenReturn(Optional.of(stage));
        when(stageRepository.save(any(Stage.class))).thenAnswer(invocation -> invocation.getArgument(0));

        stageService.recordClear(1L, 100);

        assertEquals(1, stage.getTotalClears());
        assertEquals(100.0, stage.getAverageElapsedTime(), 0.001);
        verify(stageRepository, times(1)).save(stage);
    }

    @Test
    public void testRecordClearSubsequentCalculatesNewAverage() {
        Stage stage = new Stage(1L, "Test Stage", 5, 5, new int[5][5]);
        stage.setTotalClears(2);
        stage.setAverageElapsedTime(100.0);

        when(stageRepository.findById(1L)).thenReturn(Optional.of(stage));
        when(stageRepository.save(any(Stage.class))).thenAnswer(invocation -> invocation.getArgument(0));

        stageService.recordClear(1L, 160);

        assertEquals(3, stage.getTotalClears());
        assertEquals(120.0, stage.getAverageElapsedTime(), 0.001);
        verify(stageRepository, times(1)).save(stage);
    }
}
