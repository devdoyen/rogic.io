package com.devdoyen.nemologic.service;

import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;

@Component
public class NonogramSolver {

    private static final long TIMEOUT_MS = 300;

    public static class SolverTimeoutException extends RuntimeException {
        public SolverTimeoutException(String message) {
            super(message);
        }
    }

    public boolean isUnique(int[][] solutionGrid) {
        if (solutionGrid == null || solutionGrid.length == 0 || solutionGrid[0].length == 0) {
            return true; 
        }

        int rowCount = solutionGrid.length;
        int colCount = solutionGrid[0].length;

        // 1. Calculate row and col hints from the solutionGrid
        List<Integer>[] rowHints = calculateRowHints(solutionGrid);
        List<Integer>[] colHints = calculateColHints(solutionGrid);

        // 2. Generate all possible row patterns for each row matching rowHints
        List<int[]>[] possibleRowPatterns = new List[rowCount];
        for (int r = 0; r < rowCount; r++) {
            List<int[]> patterns = new ArrayList<>();
            generatePatterns(0, colCount, rowHints[r], new int[colCount], 0, 0, patterns);
            possibleRowPatterns[r] = patterns;
            // If any row has no valid pattern, then no solution is possible (though grid itself is a solution, so this shouldn't happen)
            if (patterns.isEmpty()) {
                return false;
            }
        }

        // 3. Backtracking row by row to count solutions
        long startTime = System.currentTimeMillis();
        int[] solutionCounter = new int[1]; // array wrapper to modify within recursive context
        int[][] tempGrid = new int[rowCount][colCount];

        try {
            solveDFS(0, rowCount, colCount, possibleRowPatterns, colHints, tempGrid, solutionCounter, startTime);
        } catch (SolverTimeoutException e) {
            // If search timeout occurs, safely assume it is not a uniquely verifiable puzzle within threshold
            return false;
        }

        return solutionCounter[0] == 1;
    }

    private void solveDFS(int r, int rowCount, int colCount, List<int[]>[] possibleRowPatterns, 
                          List<Integer>[] colHints, int[][] tempGrid, int[] solutionCounter, long startTime) {
        
        // Timeout check
        if (System.currentTimeMillis() - startTime > TIMEOUT_MS) {
            throw new SolverTimeoutException("Solver timed out after " + TIMEOUT_MS + " ms");
        }

        // Early exit if 2 or more solutions are found
        if (solutionCounter[0] >= 2) {
            return;
        }

        if (r == rowCount) {
            // Final check on columns
            if (validateAllColumns(tempGrid, colHints)) {
                solutionCounter[0]++;
            }
            return;
        }

        for (int[] pattern : possibleRowPatterns[r]) {
            tempGrid[r] = pattern;
            if (isValidColumnPrefix(r, rowCount, colCount, tempGrid, colHints)) {
                solveDFS(r + 1, rowCount, colCount, possibleRowPatterns, colHints, tempGrid, solutionCounter, startTime);
                if (solutionCounter[0] >= 2) {
                    return; // Early exit propagation
                }
            }
        }
    }

    private boolean isValidColumnPrefix(int r, int rowCount, int colCount, int[][] tempGrid, List<Integer>[] colHints) {
        for (int c = 0; c < colCount; c++) {
            List<Integer> colHint = colHints[c];
            int hintSize = colHint.size();

            int segmentIndex = 0;
            int runningLength = 0;

            for (int i = 0; i <= r; i++) {
                if (tempGrid[i][c] == 1) {
                    runningLength++;
                } else {
                    if (runningLength > 0) {
                        if (segmentIndex >= hintSize || colHint.get(segmentIndex) != runningLength) {
                            return false; // Completed segment does not match hint
                        }
                        segmentIndex++;
                        runningLength = 0;
                    }
                }
            }

            // Check the current incomplete running segment
            if (runningLength > 0) {
                if (segmentIndex >= hintSize || runningLength > colHint.get(segmentIndex)) {
                    return false; // Current segment exceeds the expected hint length
                }
            }

            // If it is the last row, the columns must match perfectly
            if (r == rowCount - 1) {
                int finalSegmentIndex = segmentIndex;
                if (runningLength > 0) {
                    if (finalSegmentIndex >= hintSize || colHint.get(finalSegmentIndex) != runningLength) {
                        return false;
                    }
                    finalSegmentIndex++;
                }
                if (finalSegmentIndex != hintSize) {
                    return false; // Total number of segments doesn't match
                }
            } else {
                // Feasibility check: Can the remaining space satisfy the rest of the hints?
                int remainingRows = rowCount - 1 - r;
                int minRequiredSpace = 0;
                int startIdx = segmentIndex;
                if (runningLength > 0) {
                    minRequiredSpace += (colHint.get(segmentIndex) - runningLength);
                    startIdx++;
                }
                for (int j = startIdx; j < hintSize; j++) {
                    if (minRequiredSpace > 0) {
                        minRequiredSpace += 1; // at least one 0 separator
                    }
                    minRequiredSpace += colHint.get(j);
                }
                if (minRequiredSpace > remainingRows) {
                    return false; // Not enough rows left to complete the hints
                }
            }
        }
        return true;
    }

    private boolean validateAllColumns(int[][] tempGrid, List<Integer>[] colHints) {
        int rowCount = tempGrid.length;
        int colCount = tempGrid[0].length;

        for (int c = 0; c < colCount; c++) {
            List<Integer> colHint = colHints[c];
            List<Integer> actual = new ArrayList<>();
            int count = 0;
            for (int r = 0; r < rowCount; r++) {
                if (tempGrid[r][c] == 1) {
                    count++;
                } else {
                    if (count > 0) {
                        actual.add(count);
                        count = 0;
                    }
                }
            }
            if (count > 0) {
                actual.add(count);
            }

            if (!actual.equals(colHint)) {
                return false;
            }
        }
        return true;
    }

    private void generatePatterns(int colIdx, int colCount, List<Integer> hints, int[] currentPattern, 
                                  int hintIdx, int runLength, List<int[]> results) {
        
        // Base case: processed all cells in the row
        if (colIdx == colCount) {
            // Verify hints are satisfied
            if (hintIdx == hints.size() && runLength == 0) {
                results.add(currentPattern.clone());
            } else if (hintIdx == hints.size() - 1 && runLength > 0 && runLength == hints.get(hintIdx)) {
                results.add(currentPattern.clone());
            }
            return;
        }

        // Option 1: Place 0
        if (runLength == 0) {
            currentPattern[colIdx] = 0;
            generatePatterns(colIdx + 1, colCount, hints, currentPattern, hintIdx, 0, results);
        } else {
            // A run is active, we can only place 0 if the current run length matches the hint
            if (hintIdx < hints.size() && runLength == hints.get(hintIdx)) {
                currentPattern[colIdx] = 0;
                generatePatterns(colIdx + 1, colCount, hints, currentPattern, hintIdx + 1, 0, results);
            }
        }

        // Option 2: Place 1
        if (hintIdx < hints.size()) {
            if (runLength < hints.get(hintIdx)) {
                currentPattern[colIdx] = 1;
                generatePatterns(colIdx + 1, colCount, hints, currentPattern, hintIdx, runLength + 1, results);
            }
        }
    }

    private List<Integer>[] calculateRowHints(int[][] grid) {
        int rowCount = grid.length;
        List<Integer>[] rowHints = new List[rowCount];
        for (int r = 0; r < rowCount; r++) {
            List<Integer> hints = new ArrayList<>();
            int count = 0;
            for (int c = 0; c < grid[r].length; c++) {
                if (grid[r][c] == 1) {
                    count++;
                } else {
                    if (count > 0) {
                        hints.add(count);
                        count = 0;
                    }
                }
            }
            if (count > 0) {
                hints.add(count);
            }
            rowHints[r] = hints;
        }
        return rowHints;
    }

    private List<Integer>[] calculateColHints(int[][] grid) {
        int rowCount = grid.length;
        int colCount = grid[0].length;
        List<Integer>[] colHints = new List[colCount];
        for (int c = 0; c < colCount; c++) {
            List<Integer> hints = new ArrayList<>();
            int count = 0;
            for (int r = 0; r < rowCount; r++) {
                if (grid[r][c] == 1) {
                    count++;
                } else {
                    if (count > 0) {
                        hints.add(count);
                        count = 0;
                    }
                }
            }
            if (count > 0) {
                hints.add(count);
            }
            colHints[c] = hints;
        }
        return colHints;
    }
}
