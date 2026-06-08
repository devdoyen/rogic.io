import { describe, it, expect } from 'vitest';
import { validateGrid } from './validator';

describe('Validator TDD Phase', () => {
  it('should return true for an empty grid matching empty hints', () => {
    const grid: number[][] = [];
    const rowHints: number[][] = [];
    const colHints: number[][] = [];
    expect(validateGrid(grid, rowHints, colHints)).toBe(true);
  });

  it('should return true when player grid matches target hints exactly', () => {
    const grid = [
      [1, 0, 1],
      [0, 1, 0],
      [1, 1, 1]
    ];
    const rowHints = [[1, 1], [1], [3]];
    const colHints = [[1, 1], [2], [1, 1]];
    expect(validateGrid(grid, rowHints, colHints)).toBe(true);
  });

  it('should return false when player grid does not match target hints', () => {
    const grid = [
      [1, 0, 0],
      [0, 1, 0],
      [1, 1, 1]
    ];
    // Hints for this grid would be [[1], [1], [3]] row-wise and [[1, 1], [1, 1], [1]] col-wise
    // We pass target hints that don't match:
    const rowHints = [[1, 1], [1], [3]];
    const colHints = [[1, 1], [2], [1, 1]];
    expect(validateGrid(grid, rowHints, colHints)).toBe(false);
  });

  it('should ignore X marks (2) and treat them as empty (0)', () => {
    const grid = [
      [1, 2, 1],
      [2, 1, 2],
      [1, 1, 1]
    ];
    const rowHints = [[1, 1], [1], [3]];
    const colHints = [[1, 1], [2], [1, 1]];
    expect(validateGrid(grid, rowHints, colHints)).toBe(true);
  });

  it('should validate correctly for an alternate grid layout', () => {
    const grid = [
      [1, 1, 0],
      [1, 1, 0],
      [0, 0, 0]
    ];
    const rowHints = [[2], [2], [0]];
    const colHints = [[2], [2], [0]];
    expect(validateGrid(grid, rowHints, colHints)).toBe(true);
  });
});
