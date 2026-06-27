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

    @Test
    public void testUniqueSolutionSmileFace10x10() {
        int[][] smileGrid = {
            {0, 0, 1, 1, 1, 1, 1, 1, 0, 0},
            {0, 1, 0, 0, 0, 0, 0, 0, 1, 0},
            {1, 0, 1, 0, 0, 0, 0, 1, 0, 1},
            {1, 0, 1, 0, 0, 0, 0, 1, 0, 1},
            {1, 0, 0, 0, 0, 0, 0, 0, 0, 1},
            {1, 0, 1, 0, 0, 0, 0, 1, 0, 1},
            {1, 0, 0, 1, 1, 1, 1, 0, 0, 1},
            {0, 1, 0, 0, 0, 0, 0, 0, 1, 0},
            {0, 0, 1, 1, 1, 1, 1, 1, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0}
        };
        assertTrue(solver.isUnique(smileGrid), "Smile face 10x10 should have a unique solution");
    }

    @Test
    public void testUniqueSolutionAscendingStar15x15() {
        int[][] starGrid = {
            {0,0,0,0,0,0,0,1,0,0,0,0,0,0,0},
            {0,0,0,0,0,0,1,1,1,0,0,0,0,0,0},
            {0,0,0,0,0,1,1,1,1,1,0,0,0,0,0},
            {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
            {0,1,1,1,1,1,1,1,1,1,1,1,1,1,0},
            {0,0,1,1,1,1,1,1,1,1,1,1,1,0,0},
            {0,0,0,1,1,1,1,1,1,1,1,1,0,0,0},
            {0,0,0,0,1,1,1,1,1,1,1,0,0,0,0},
            {0,0,0,0,1,1,1,1,1,1,1,0,0,0,0},
            {0,0,0,1,1,1,1,0,1,1,1,1,0,0,0},
            {0,0,1,1,1,1,0,0,0,1,1,1,1,0,0},
            {0,1,1,1,0,0,0,0,0,0,0,1,1,1,0},
            {1,1,1,0,0,0,0,0,0,0,0,0,1,1,1},
            {1,1,0,0,0,0,0,0,0,0,0,0,0,1,1},
            {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0}
        };
        assertTrue(solver.isUnique(starGrid), "Ascending Star 15x15 should have a unique solution");
    }

    @Test
    public void testNonUniqueSolutionGiantCross30x30() {
        int[][] grid30 = new int[30][30];
        for (int i = 0; i < 30; i++) {
            grid30[i][i] = 1;
            grid30[i][29 - i] = 1;
        }
        assertFalse(solver.isUnique(grid30), "Giant Cross 30x30 should be recognized as non-unique");
    }

    @Test
    public void testUniqueSolutionSolid30x30() {
        int[][] solidGrid = new int[30][30];
        for (int i = 0; i < 30; i++) {
            for (int j = 0; j < 30; j++) {
                solidGrid[i][j] = 1;
            }
        }
        assertTrue(solver.isUnique(solidGrid), "Solid 30x30 grid should have a unique solution");
    }

    @Test
    public void testIsLogicalOnlyWithHeart() {
        int[][] heartGrid = {
            {0, 1, 0, 1, 0},
            {1, 1, 1, 1, 1},
            {1, 1, 1, 1, 1},
            {0, 1, 1, 1, 0},
            {0, 0, 1, 0, 0}
        };
        assertTrue(solver.isLogicalOnly(heartGrid), "Heart shape should be solvable purely logically");
    }

    @Test
    public void testIsLogicalOnlyWithMultipleSolutions() {
        int[][] symGrid = {
            {1, 0},
            {0, 1}
        };
        assertFalse(solver.isLogicalOnly(symGrid), "2x2 diagonal grid should not be solvable purely logically");
    }
}
