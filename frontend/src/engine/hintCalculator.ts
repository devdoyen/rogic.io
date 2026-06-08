export interface NonogramHints {
  rowHints: number[][];
  colHints: number[][];
}

/**
   * Calculates row and column hints for a given 2D nonogram grid.
   * @param grid A 2D array where 1 represents a filled cell and 0 represents an empty cell.
   *             Grid is indexed as grid[row][col].
   */
export function calculateHints(grid: number[][]): NonogramHints {
  if (!grid || grid.length === 0) {
    return {
      rowHints: [],
      colHints: []
    };
  }

  const rowCount = grid.length;
  const colCount = grid[0].length;

  // Validate that all rows have the same length
  for (let r = 0; r < rowCount; r++) {
    if (!grid[r] || grid[r].length !== colCount) {
      throw new Error('Grid must have uniform row lengths');
    }
  }

  // Calculate row hints
  const rowHints = grid.map(row => calculateLineHints(row));

  // Calculate col hints
  const colHints: number[][] = [];
  for (let c = 0; c < colCount; c++) {
    const colLine: number[] = [];
    for (let r = 0; r < rowCount; r++) {
      colLine.push(grid[r][c]);
    }
    colHints.push(calculateLineHints(colLine));
  }

  return {
    rowHints,
    colHints
  };
}

/**
 * Calculates hints for a single 1D line (row or column).
 * @param line A 1D array of 0s and 1s.
 */
export function calculateLineHints(line: number[]): number[] {
  const hints: number[] = [];
  let consecutiveCount = 0;

  for (let i = 0; i < line.length; i++) {
    if (line[i] === 1) {
      consecutiveCount++;
    } else {
      if (consecutiveCount > 0) {
        hints.push(consecutiveCount);
        consecutiveCount = 0;
      }
    }
  }

  if (consecutiveCount > 0) {
    hints.push(consecutiveCount);
  }

  return hints.length === 0 ? [0] : hints;
}
