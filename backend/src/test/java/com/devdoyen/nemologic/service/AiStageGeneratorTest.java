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

        when(stageRepository.findTop10ByOrderByIdDesc()).thenReturn(java.util.Collections.emptyList());
        when(stageRepository.existsBySolutionGrid(any())).thenReturn(false);
    }

    @Test
    public void testParseJsonAndSaveStage() {
        String mockJsonResponse = "{\"name\": \"AI Puzzle\", \"width\": 5, \"height\": 5, \"grid\": \"[[0,1,0,1,0],[1,1,1,1,1],[1,1,1,1,1],[0,1,1,1,0],[0,0,1,0,0]]\"}";
        when(aiClient.generatePuzzleJson(anyInt(), anyInt(), anyList())).thenReturn(mockJsonResponse);
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

        int[] sizes = {5, 10, 15, 20, 25, 30};
        for (int size : sizes) {
            verify(mockGenerator, times(5)).generateAndSaveStage(size, size, false);
        }
    }

    @Test
    public void testDailyPuzzleSchedulerReleaseChain() {
        AiStageGenerator mockGenerator = mock(AiStageGenerator.class);
        StageService mockStageService = mock(StageService.class);
        DailyPuzzleScheduler scheduler = new DailyPuzzleScheduler(mockGenerator, mockStageService);

        scheduler.releaseDailyPuzzle();

        verify(mockStageService, times(1)).releaseDailyPuzzles();
    }

    @Test
    public void testGenerateAndSaveStageRetriesOnFailureAndEventuallyThrows() {
        when(aiClient.generatePuzzleJson(anyInt(), anyInt(), anyList())).thenReturn("invalid json");

        assertThrows(IllegalArgumentException.class, () -> {
            aiStageGenerator.generateAndSaveStage();
        });

        verify(aiClient, times(3)).generatePuzzleJson(eq(5), eq(5), anyList());
    }

    @Test
    public void testGenerateAndSaveStageRetriesOnNonUniqueSolution() {
        String mockNonUniqueJsonResponse = "{\"name\": \"Invalid AI Puzzle\", \"width\": 2, \"height\": 2, \"grid\": \"[[1,0],[0,1]]\"}";
        when(aiClient.generatePuzzleJson(anyInt(), anyInt(), anyList())).thenReturn(mockNonUniqueJsonResponse);

        assertThrows(IllegalArgumentException.class, () -> {
            aiStageGenerator.generateAndSaveStage();
        });

        verify(aiClient, times(3)).generatePuzzleJson(eq(5), eq(5), anyList());
    }

    @Test
    public void testGenerateAndSaveStageRetriesWhenClientThrowsException() {
        when(aiClient.generatePuzzleJson(anyInt(), anyInt(), anyList())).thenThrow(new RuntimeException("API error"));

        assertThrows(IllegalArgumentException.class, () -> {
            aiStageGenerator.generateAndSaveStage();
        });

        verify(aiClient, times(3)).generatePuzzleJson(eq(5), eq(5), anyList());
    }

    @Test
    public void testTitleCleaning() {
        String mockJsonResponse = "{\"name\": \"AI Puzzle: Fantastic Tree\", \"width\": 5, \"height\": 5, \"grid\": \"[[0,1,0,1,0],[1,1,1,1,1],[1,1,1,1,1],[0,1,1,1,0],[0,0,1,0,0]]\"}";
        when(aiClient.generatePuzzleJson(eq(5), eq(5), anyList())).thenReturn(mockJsonResponse);
        when(stageRepository.save(any(Stage.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Stage stage = aiStageGenerator.generateAndSaveStage(5, 5, true);

        assertNotNull(stage);
        assertEquals("Fantastic Tree", stage.getName());
    }

    @Test
    public void testCustomSizeGeneration() {
        // 3x3 grid (unique solution: all 1s)
        String mockJsonResponse = "{\"name\": \"Custom 3x3\", \"width\": 3, \"height\": 3, \"grid\": \"[[1,1,1],[1,1,1],[1,1,1]]\"}";
        when(aiClient.generatePuzzleJson(eq(3), eq(3), anyList())).thenReturn(mockJsonResponse);
        when(stageRepository.save(any(Stage.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Stage stage = aiStageGenerator.generateAndSaveStage(3, 3, true);

        assertNotNull(stage);
        assertEquals("Custom 3x3", stage.getName());
        assertEquals(3, stage.getWidth());
        assertEquals(3, stage.getHeight());
    }

    @Test
    public void testDuplicateGridDeduplication() {
        String mockJsonResponse = "{\"name\": \"AI Puzzle\", \"width\": 5, \"height\": 5, \"grid\": \"[[0,1,0,1,0],[1,1,1,1,1],[1,1,1,1,1],[0,1,1,1,0],[0,0,1,0,0]]\"}";
        when(aiClient.generatePuzzleJson(anyInt(), anyInt(), anyList())).thenReturn(mockJsonResponse);
        // First existsBySolutionGrid check returns true (duplicate), second returns false
        when(stageRepository.existsBySolutionGrid(any())).thenReturn(true).thenReturn(false);
        when(stageRepository.save(any(Stage.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Stage stage = aiStageGenerator.generateAndSaveStage(5, 5, true);

        assertNotNull(stage);
        // Verified it retried and called client 2 times due to duplicate grid
        verify(aiClient, times(2)).generatePuzzleJson(eq(5), eq(5), anyList());
    }
}
