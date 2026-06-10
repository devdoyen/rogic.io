package com.devdoyen.nemologic.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class NonogramSolverTest {

    private NonogramSolver solver;

    @BeforeEach
    public void setUp() {
        solver = new NonogramSolver();
    }

    @Test
    public void testUniqueSolutionHeart() {
        // 5x5 Heart Shape - Should have a unique solution
        int[][] heartGrid = {
            {0, 1, 0, 1, 0},
            {1, 1, 1, 1, 1},
            {1, 1, 1, 1, 1},
            {0, 1, 1, 1, 0},
            {0, 0, 1, 0, 0}
        };

        assertTrue(solver.isUnique(heartGrid), "Heart shape should have a unique solution");
    }

    @Test
    public void testMultipleSolutionsSymmetric() {
        // 2x2 Symmetric pattern (diagonal 1s) - Has another solution (anti-diagonal 1s)
        int[][] symGrid = {
            {1, 0},
            {0, 1}
        };

        assertFalse(solver.isUnique(symGrid), "2x2 diagonal grid should have multiple solutions and return false");
    }

    @Test
    public void testUniqueSolutionHourglass() {
        // 5x5 Hourglass - Should have a unique solution
        int[][] hourglassGrid = {
            {1, 1, 1, 1, 1},
            {0, 1, 1, 1, 0},
            {0, 0, 1, 0, 0},
            {0, 1, 1, 1, 0},
            {1, 1, 1, 1, 1}
        };

        assertTrue(solver.isUnique(hourglassGrid), "Hourglass should have a unique solution");
    }

    @Test
    public void testEmptyGrid() {
        // Empty 3x3 grid - Technically all 0s could be a solution but let's test if it handles it
        int[][] emptyGrid = {
            {0, 0, 0},
            {0, 0, 0},
            {0, 0, 0}
        };

        // If the grid is empty, the only solution is all 0s. 
        // Let's verify it counts it as unique.
        assertTrue(solver.isUnique(emptyGrid), "Empty grid should have a unique solution of all 0s");
    }
}
