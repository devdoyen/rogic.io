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

        // State grid: -1 = unknown, 0 = empty, 1 = filled
        int[][] state = new int[rowCount][colCount];
        for (int r = 0; r < rowCount; r++) {
            for (int c = 0; c < colCount; c++) {
                state[r][c] = -1;
            }
        }

        // 2. Run logical line solver
        boolean changed = true;
        while (changed) {
            changed = false;
            // Solve rows
            for (int r = 0; r < rowCount; r++) {
                if (solveLine(state[r], rowHints[r])) {
                    changed = true;
                }
            }
            // Solve columns
            for (int c = 0; c < colCount; c++) {
                int[] colLine = new int[rowCount];
                for (int r = 0; r < rowCount; r++) {
                    colLine[r] = state[r][c];
                }
                if (solveLine(colLine, colHints[c])) {
                    changed = true;
                    for (int r = 0; r < rowCount; r++) {
                        state[r][c] = colLine[r];
                    }
                }
            }
        }

        // Check if there is any contradiction or if fully solved
        boolean fullySolved = true;
        for (int r = 0; r < rowCount; r++) {
            for (int c = 0; c < colCount; c++) {
                if (state[r][c] == -1) {
                    fullySolved = false;
                    break;
                }
            }
        }

        if (fullySolved) {
            // Since logical solver only fills cells that MUST have that value,
            // if it is fully solved, it matches solutionGrid and is unique!
            return true;
        }

        long startTime = System.currentTimeMillis();
        int[] solutionsCount = new int[1];
        try {
            solveDFSCell(0, 0, state, rowHints, colHints, solutionsCount, startTime);
        } catch (SolverTimeoutException e) {
            // If search timeout occurs, safely assume it is not a uniquely verifiable puzzle within threshold
            return false;
        }

        return solutionsCount[0] == 1;
    }

    private void solveDFSCell(int r, int c, int[][] state, List<Integer>[] rowHints, List<Integer>[] colHints,
                              int[] solutionsCount, long startTime) {
        if (System.currentTimeMillis() - startTime > TIMEOUT_MS) {
            throw new SolverTimeoutException("Solver timed out after " + TIMEOUT_MS + " ms");
        }
        if (solutionsCount[0] >= 2) {
            return;
        }

        int rowCount = state.length;
        int colCount = state[0].length;

        if (r == rowCount) {
            solutionsCount[0]++;
            return;
        }

        int nextR = (c == colCount - 1) ? r + 1 : r;
        int nextC = (c == colCount - 1) ? 0 : c + 1;

        if (state[r][c] != -1) {
            solveDFSCell(nextR, nextC, state, rowHints, colHints, solutionsCount, startTime);
            return;
        }

        // Try placing 0
        state[r][c] = 0;
        if (isAssignmentValid(r, c, state, rowHints, colHints)) {
            solveDFSCell(nextR, nextC, state, rowHints, colHints, solutionsCount, startTime);
        }

        if (solutionsCount[0] >= 2) {
            state[r][c] = -1;
            return;
        }

        // Try placing 1
        state[r][c] = 1;
        if (isAssignmentValid(r, c, state, rowHints, colHints)) {
            solveDFSCell(nextR, nextC, state, rowHints, colHints, solutionsCount, startTime);
        }

        state[r][c] = -1; // backtrack
    }

    private boolean isAssignmentValid(int r, int c, int[][] state, List<Integer>[] rowHints, List<Integer>[] colHints) {
        // Check row compatibility
        if (!isPartialLineCompatible(state[r], rowHints[r])) {
            return false;
        }

        // Check column compatibility
        int rowCount = state.length;
        int[] colLine = new int[rowCount];
        for (int i = 0; i < rowCount; i++) {
            colLine[i] = state[i][c];
        }
        if (!isPartialLineCompatible(colLine, colHints[c])) {
            return false;
        }

        return true;
    }

    private boolean isPartialLineCompatible(int[] line, List<Integer> hints) {
        int L = line.length;
        int K = hints.size();
        boolean[][] dp = new boolean[L + 1][K + 1];
        dp[0][0] = true;

        for (int i = 1; i <= L; i++) {
            dp[i][0] = dp[i-1][0] && (line[i-1] != 1);
        }

        for (int j = 1; j <= K; j++) {
            int len = hints.get(j-1);
            for (int i = 1; i <= L; i++) {
                if (line[i-1] != 1) {
                    dp[i][j] = dp[i-1][j];
                }
                int start = i - len;
                if (start >= 0) {
                    boolean canPlace = true;
                    for (int x = start; x < i; x++) {
                        if (line[x] == 0) {
                            canPlace = false;
                            break;
                        }
                    }
                    if (canPlace) {
                        if (start > 0) {
                            if (line[start - 1] != 1) {
                                dp[i][j] = dp[i][j] || dp[start - 1][j-1];
                            }
                        } else {
                            if (j == 1) {
                                dp[i][j] = dp[i][j] || dp[0][0];
                            }
                        }
                    }
                }
            }
        }
        return dp[L][K];
    }

    private boolean solveLine(int[] line, List<Integer> hints) {
        int L = line.length;
        int K = hints.size();

        boolean[][] prefixDP = computePrefixDP(line, hints);

        int[] revLine = new int[L];
        for (int i = 0; i < L; i++) {
            revLine[i] = line[L - 1 - i];
        }
        List<Integer> revHints = new ArrayList<>(hints);
        java.util.Collections.reverse(revHints);
        boolean[][] revDP = computePrefixDP(revLine, revHints);

        boolean[][] suffixDP = new boolean[L + 1][K + 1];
        for (int x = 0; x <= L; x++) {
            for (int y = 0; y <= K; y++) {
                suffixDP[x][y] = revDP[L - x][y];
            }
        }

        boolean changed = false;
        for (int c = 0; c < L; c++) {
            if (line[c] == -1) {
                boolean c1 = canBe1(c, line, hints, prefixDP, suffixDP);
                boolean c0 = canBe0(c, line, hints, prefixDP, suffixDP);
                if (c1 && !c0) {
                    line[c] = 1;
                    changed = true;
                } else if (c0 && !c1) {
                    line[c] = 0;
                    changed = true;
                }
            }
        }
        return changed;
    }

    private boolean[][] computePrefixDP(int[] line, List<Integer> hints) {
        int L = line.length;
        int K = hints.size();
        boolean[][] dp = new boolean[L + 1][K + 1];
        dp[0][0] = true;

        for (int i = 1; i <= L; i++) {
            dp[i][0] = dp[i-1][0] && (line[i-1] != 1);
        }

        for (int j = 1; j <= K; j++) {
            int len = hints.get(j-1);
            for (int i = 1; i <= L; i++) {
                if (line[i-1] != 1) {
                    dp[i][j] = dp[i-1][j];
                }
                int start = i - len;
                if (start >= 0) {
                    boolean canPlace = true;
                    for (int x = start; x < i; x++) {
                        if (line[x] == 0) {
                            canPlace = false;
                            break;
                        }
                    }
                    if (canPlace) {
                        if (start > 0) {
                            if (line[start - 1] != 1) {
                                dp[i][j] = dp[i][j] || dp[start - 1][j-1];
                            }
                        } else {
                            if (j == 1) {
                                dp[i][j] = dp[i][j] || dp[0][0];
                            }
                        }
                    }
                }
            }
        }
        return dp;
    }

    private boolean canBe1(int c, int[] line, List<Integer> hints, boolean[][] prefixDP, boolean[][] suffixDP) {
        if (line[c] == 0) return false;
        int L = line.length;
        int K = hints.size();
        for (int j = 0; j < K; j++) {
            int len = hints.get(j);
            int minStart = Math.max(0, c - len + 1);
            int maxStart = Math.min(c, L - len);
            for (int start = minStart; start <= maxStart; start++) {
                boolean ok = true;
                for (int x = start; x < start + len; x++) {
                    if (line[x] == 0) {
                        ok = false;
                        break;
                    }
                }
                if (!ok) continue;

                if (start > 0 && line[start - 1] == 1) continue;
                if (start + len < L && line[start + len] == 1) continue;

                boolean prefixOk = false;
                if (start > 0) {
                    prefixOk = prefixDP[start - 1][j];
                } else {
                    prefixOk = (j == 0);
                }
                if (!prefixOk) continue;

                boolean suffixOk = false;
                if (start + len < L) {
                    suffixOk = suffixDP[start + len + 1][K - 1 - j];
                } else {
                    suffixOk = (K - 1 - j == 0);
                }

                if (suffixOk) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean canBe0(int c, int[] line, List<Integer> hints, boolean[][] prefixDP, boolean[][] suffixDP) {
        if (line[c] == 1) return false;
        int L = line.length;
        int K = hints.size();
        for (int j = 0; j <= K; j++) {
            boolean prefixOk = prefixDP[c][j];
            boolean suffixOk = (c + 1 < L) ? suffixDP[c + 1][K - j] : (K - j == 0);
            if (prefixOk && suffixOk) {
                return true;
            }
        }
        return false;
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
