package com.devdoyen.nemologic.service;

import com.devdoyen.nemologic.client.AiClient;
import com.devdoyen.nemologic.model.Stage;
import com.devdoyen.nemologic.repository.StageRepository;
import com.devdoyen.nemologic.scheduler.DailyPuzzleScheduler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class AiStageGeneratorTest {

    private AiClient aiClient;
    private StageRepository stageRepository;
    private AiStageGenerator aiStageGenerator;

    private NonogramSolver nonogramSolver;

    @BeforeEach
    public void setUp() {
        aiClient = mock(AiClient.class);
        stageRepository = mock(StageRepository.class);
        nonogramSolver = new NonogramSolver();
        aiStageGenerator = new AiStageGenerator(aiClient, stageRepository, nonogramSolver);
    }

    @Test
    public void testParseJsonAndSaveStage() {
        String mockJsonResponse = "{\"name\": \"AI Puzzle\", \"width\": 5, \"height\": 5, \"grid\": \"[[0,1,0,1,0],[1,1,1,1,1],[1,1,1,1,1],[0,1,1,1,0],[0,0,1,0,0]]\"}";
        when(aiClient.generateDailyPuzzleJson()).thenReturn(mockJsonResponse);
        when(stageRepository.save(any(Stage.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Stage stage = aiStageGenerator.generateAndSaveStage();

        assertNotNull(stage);
        assertEquals("AI Puzzle", stage.getName());
        assertEquals(5, stage.getWidth());
        assertEquals(5, stage.getHeight());
        assertNotNull(stage.getSolutionGrid());
        assertEquals(1, stage.getSolutionGrid()[0][1]);
    }

    @Test
    public void testDailyPuzzleSchedulerTriggerChain() {
        AiStageGenerator mockGenerator = mock(AiStageGenerator.class);
        StageService mockStageService = mock(StageService.class);
        DailyPuzzleScheduler scheduler = new DailyPuzzleScheduler(mockGenerator, mockStageService);

        scheduler.generateDailyPuzzle();

        verify(mockGenerator, times(1)).generateAndSaveStage(false);
    }

    @Test
    public void testDailyPuzzleSchedulerReleaseChain() {
        AiStageGenerator mockGenerator = mock(AiStageGenerator.class);
        StageService mockStageService = mock(StageService.class);
        DailyPuzzleScheduler scheduler = new DailyPuzzleScheduler(mockGenerator, mockStageService);

        scheduler.releaseDailyPuzzle();

        verify(mockStageService, times(1)).activateAllInactiveStages();
    }

    @Test
    public void testGenerateAndSaveStageRetriesOnFailureAndEventuallyThrows() {
        when(aiClient.generateDailyPuzzleJson()).thenReturn("invalid json");

        assertThrows(IllegalArgumentException.class, () -> {
            aiStageGenerator.generateAndSaveStage();
        });

        verify(aiClient, times(3)).generateDailyPuzzleJson();
    }

    @Test
    public void testGenerateAndSaveStageRetriesOnNonUniqueSolution() {
        String mockNonUniqueJsonResponse = "{\"name\": \"Invalid AI Puzzle\", \"width\": 2, \"height\": 2, \"grid\": \"[[1,0],[0,1]]\"}";
        when(aiClient.generateDailyPuzzleJson()).thenReturn(mockNonUniqueJsonResponse);

        assertThrows(IllegalArgumentException.class, () -> {
            aiStageGenerator.generateAndSaveStage();
        });

        verify(aiClient, times(3)).generateDailyPuzzleJson();
    }
}
