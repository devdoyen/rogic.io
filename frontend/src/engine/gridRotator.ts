/**
 * Rotates a 2D grid clockwise by 90 degrees * steps.
 */
export function rotateGrid(grid: number[][], steps: number): number[][] {
  let current = grid;
  const normalizedSteps = ((steps % 4) + 4) % 4;

  for (let i = 0; i < normalizedSteps; i++) {
    const R = current.length;
    const C = current[0].length;
    const next: number[][] = Array.from({ length: C }, () => Array(R).fill(0));
    for (let r = 0; r < R; r++) {
      for (let c = 0; c < C; c++) {
        next[c][R - 1 - r] = current[r][c];
      }
    }
    current = next;
  }
  return current;
}
