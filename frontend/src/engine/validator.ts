import { calculateHints } from './hintCalculator';

/**
 * Validates whether the current player grid satisfies the target row and column hints.
 * X marks (2) are treated as empty cells (0) during comparison.
 */
export function validateGrid(
  currentGrid: number[][],
  targetRowHints: number[][],
  targetColHints: number[][],
): boolean {
  if (!currentGrid || currentGrid.length === 0) {
    return targetRowHints.length === 0 && targetColHints.length === 0;
  }

  const { rowHints, colHints } = calculateHints(currentGrid);

  if (rowHints.length !== targetRowHints.length) return false;
  if (colHints.length !== targetColHints.length) return false;

  for (let i = 0; i < rowHints.length; i++) {
    const hints = rowHints[i];
    const target = targetRowHints[i];
    if (hints.length !== target.length) return false;
    for (let j = 0; j < hints.length; j++) {
      if (hints[j] !== target[j]) return false;
    }
  }

  for (let i = 0; i < colHints.length; i++) {
    const hints = colHints[i];
    const target = targetColHints[i];
    if (hints.length !== target.length) return false;
    for (let j = 0; j < hints.length; j++) {
      if (hints[j] !== target[j]) return false;
    }
  }

  return true;
}
