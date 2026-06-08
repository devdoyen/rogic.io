import { describe, it, expect } from 'vitest';
import { calculateHints, calculateLineHints } from './hintCalculator';

describe('HintCalculator Unit Tests', () => {
  describe('calculateLineHints', () => {
    it('should calculate hints for an empty line to be [0]', () => {
      expect(calculateLineHints([0, 0, 0, 0, 0])).toEqual([0]);
    });

    it('should calculate hints for a fully filled line', () => {
      expect(calculateLineHints([1, 1, 1, 1, 1])).toEqual([5]);
    });

    it('should calculate hints for lines with gaps and consecutive numbers', () => {
      // User request case: [1, 1, 0, 1, 1] -> [2, 2]
      expect(calculateLineHints([1, 1, 0, 1, 1])).toEqual([2, 2]);
    });

    it('should calculate hints for alternating filled and empty cells', () => {
      expect(calculateLineHints([1, 0, 1, 0, 1])).toEqual([1, 1, 1]);
    });

    it('should calculate hints for lines with leading and trailing empty cells', () => {
      expect(calculateLineHints([0, 1, 1, 0])).toEqual([2]);
      expect(calculateLineHints([0, 0, 1, 1, 0, 1, 0, 0])).toEqual([2, 1]);
    });

    it('should handle single element lines', () => {
      expect(calculateLineHints([0])).toEqual([0]);
      expect(calculateLineHints([1])).toEqual([1]);
    });
  });

  describe('calculateHints (2D Grid)', () => {
    it('should calculate hints for an empty 5x5 grid', () => {
      const grid = [
        [0, 0, 0, 0, 0],
        [0, 0, 0, 0, 0],
        [0, 0, 0, 0, 0],
        [0, 0, 0, 0, 0],
        [0, 0, 0, 0, 0]
      ];
      const result = calculateHints(grid);
      expect(result.rowHints).toEqual([[0], [0], [0], [0], [0]]);
      expect(result.colHints).toEqual([[0], [0], [0], [0], [0]]);
    });

    it('should calculate hints for a fully filled 5x5 grid', () => {
      const grid = [
        [1, 1, 1, 1, 1],
        [1, 1, 1, 1, 1],
        [1, 1, 1, 1, 1],
        [1, 1, 1, 1, 1],
        [1, 1, 1, 1, 1]
      ];
      const result = calculateHints(grid);
      expect(result.rowHints).toEqual([[5], [5], [5], [5], [5]]);
      expect(result.colHints).toEqual([[5], [5], [5], [5], [5]]);
    });

    it('should calculate hints for a complex 5x5 grid (checkerboard or design)', () => {
      // Pattern:
      // 1 0 1 0 1  -> [1, 1, 1]
      // 0 1 1 1 0  -> [3]
      // 1 1 0 1 1  -> [2, 2]
      // 0 0 1 0 0  -> [1]
      // 1 1 1 1 1  -> [5]
      const grid = [
        [1, 0, 1, 0, 1],
        [0, 1, 1, 1, 0],
        [1, 1, 0, 1, 1],
        [0, 0, 1, 0, 0],
        [1, 1, 1, 1, 1]
      ];
      // Column indexes:
      // Col 0: [1, 0, 1, 0, 1] -> [1, 1, 1]
      // Col 1: [0, 1, 1, 0, 1] -> [2, 1]
      // Col 2: [1, 1, 0, 1, 1] -> [2, 2]
      // Col 3: [0, 1, 1, 0, 1] -> [2, 1]
      // Col 4: [1, 0, 1, 0, 1] -> [1, 1, 1]

      const result = calculateHints(grid);
      expect(result.rowHints).toEqual([
        [1, 1, 1],
        [3],
        [2, 2],
        [1],
        [5]
      ]);
      expect(result.colHints).toEqual([
        [1, 1, 1],
        [2, 1],
        [2, 2],
        [2, 1],
        [1, 1, 1]
      ]);
    });

    it('should calculate hints for a 10x10 complex grid', () => {
      const grid = [
        [1, 1, 1, 0, 0, 1, 1, 1, 0, 0], // [3, 3]
        [0, 0, 1, 1, 1, 1, 0, 0, 1, 1], // [4, 2]
        [1, 0, 1, 0, 1, 0, 1, 0, 1, 0], // [1, 1, 1, 1, 1]
        [1, 1, 1, 1, 1, 1, 1, 1, 1, 1], // [10]
        [0, 0, 0, 0, 0, 0, 0, 0, 0, 0], // [0]
        [1, 0, 0, 0, 0, 0, 0, 0, 0, 1], // [1, 1]
        [0, 1, 1, 1, 1, 1, 1, 1, 1, 0], // [8]
        [0, 0, 1, 1, 1, 1, 1, 1, 0, 0], // [6]
        [0, 0, 0, 1, 1, 1, 1, 0, 0, 0], // [4]
        [0, 0, 0, 0, 1, 1, 0, 0, 0, 0]  // [2]
      ];

      const result = calculateHints(grid);
      expect(result.rowHints[0]).toEqual([3, 3]);
      expect(result.rowHints[1]).toEqual([4, 2]);
      expect(result.rowHints[2]).toEqual([1, 1, 1, 1, 1]);
      expect(result.rowHints[3]).toEqual([10]);
      expect(result.rowHints[4]).toEqual([0]);
      expect(result.rowHints[5]).toEqual([1, 1]);
      expect(result.rowHints[6]).toEqual([8]);
      expect(result.rowHints[7]).toEqual([6]);
      expect(result.rowHints[8]).toEqual([4]);
      expect(result.rowHints[9]).toEqual([2]);
    });

    it('should calculate hints for a rectangular grid (e.g. 3x5)', () => {
      const grid = [
        [1, 1, 0, 1, 1], // [2, 2]
        [0, 1, 1, 0, 1], // [2, 1]
        [0, 0, 0, 0, 0]  // [0]
      ];

      const result = calculateHints(grid);
      expect(result.rowHints).toEqual([
        [2, 2],
        [2, 1],
        [0]
      ]);
      expect(result.colHints).toEqual([
        [1],
        [2],
        [1],
        [1],
        [2]
      ]);
    });

    it('should throw an error or handle empty grid gracefully', () => {
      expect(() => calculateHints([])).toThrow();
    });
  });
});
