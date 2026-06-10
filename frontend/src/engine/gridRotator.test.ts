import { describe, it, expect } from 'vitest';
import { rotateGrid } from './gridRotator';

describe('gridRotator utility', () => {
  const grid = [
    [1, 2],
    [3, 4]
  ];

  it('should not rotate if steps is 0', () => {
    expect(rotateGrid(grid, 0)).toEqual([
      [1, 2],
      [3, 4]
    ]);
  });

  it('should rotate 90 degrees clockwise if steps is 1', () => {
    expect(rotateGrid(grid, 1)).toEqual([
      [3, 1],
      [4, 2]
    ]);
  });

  it('should rotate 180 degrees if steps is 2', () => {
    expect(rotateGrid(grid, 2)).toEqual([
      [4, 3],
      [2, 1]
    ]);
  });

  it('should rotate 270 degrees if steps is 3', () => {
    expect(rotateGrid(grid, 3)).toEqual([
      [2, 4],
      [1, 3]
    ]);
  });

  it('should handle negative steps correctly', () => {
    expect(rotateGrid(grid, -1)).toEqual([
      [2, 4],
      [1, 3]
    ]);
  });
});
