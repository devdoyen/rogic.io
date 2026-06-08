export interface CanvasConfig {
  offsetX: number;
  offsetY: number;
  cellSize: number;
  rowCount: number;
  colCount: number;
}

/**
 * Converts pixel coordinates (x, y) from click/touch events into grid indices (row, col).
 * @returns {row, col} or null if out of bounds or in the hint offset area.
 */
export function getGridCoordinates(_x: number, _y: number, _config: CanvasConfig): { row: number, col: number } | null {
  // Stub for TDD (Red phase)
  return null;
}
