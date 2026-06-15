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
        when(aiClient.generatePuzzleJson(anyInt(), anyInt())).thenReturn(mockJsonResponse);
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
        when(aiClient.generatePuzzleJson(anyInt(), anyInt())).thenReturn("invalid json");

        assertThrows(IllegalArgumentException.class, () -> {
            aiStageGenerator.generateAndSaveStage();
        });

        verify(aiClient, times(3)).generatePuzzleJson(5, 5);
    }

    @Test
    public void testGenerateAndSaveStageRetriesOnNonUniqueSolution() {
        String mockNonUniqueJsonResponse = "{\"name\": \"Invalid AI Puzzle\", \"width\": 2, \"height\": 2, \"grid\": \"[[1,0],[0,1]]\"}";
        when(aiClient.generatePuzzleJson(anyInt(), anyInt())).thenReturn(mockNonUniqueJsonResponse);

        assertThrows(IllegalArgumentException.class, () -> {
            aiStageGenerator.generateAndSaveStage();
        });

        verify(aiClient, times(3)).generatePuzzleJson(5, 5);
    }

    @Test
    public void testGenerateAndSaveStageRetriesWhenClientThrowsException() {
        when(aiClient.generatePuzzleJson(anyInt(), anyInt())).thenThrow(new RuntimeException("API error"));

        assertThrows(IllegalArgumentException.class, () -> {
            aiStageGenerator.generateAndSaveStage();
        });

        verify(aiClient, times(3)).generatePuzzleJson(5, 5);
    }

    @Test
    public void testTitleCleaning() {
        String mockJsonResponse = "{\"name\": \"AI Puzzle: Fantastic Tree\", \"width\": 5, \"height\": 5, \"grid\": \"[[0,1,0,1,0],[1,1,1,1,1],[1,1,1,1,1],[0,1,1,1,0],[0,0,1,0,0]]\"}";
        when(aiClient.generatePuzzleJson(5, 5)).thenReturn(mockJsonResponse);
        when(stageRepository.save(any(Stage.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Stage stage = aiStageGenerator.generateAndSaveStage(5, 5, true);

        assertNotNull(stage);
        assertEquals("Fantastic Tree", stage.getName());
    }

    @Test
    public void testCustomSizeGeneration() {
        // 3x3 grid (unique solution: all 1s)
        String mockJsonResponse = "{\"name\": \"Custom 3x3\", \"width\": 3, \"height\": 3, \"grid\": \"[[1,1,1],[1,1,1],[1,1,1]]\"}";
        when(aiClient.generatePuzzleJson(3, 3)).thenReturn(mockJsonResponse);
        when(stageRepository.save(any(Stage.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Stage stage = aiStageGenerator.generateAndSaveStage(3, 3, true);

        assertNotNull(stage);
        assertEquals("Custom 3x3", stage.getName());
        assertEquals(3, stage.getWidth());
        assertEquals(3, stage.getHeight());
    }
}
