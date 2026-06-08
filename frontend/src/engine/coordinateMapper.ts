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
export function getGridCoordinates(x: number, y: number, config: CanvasConfig): { row: number, col: number } | null {
  if (x < config.offsetX || y < config.offsetY) {
    return null;
  }

  const col = Math.floor((x - config.offsetX) / config.cellSize);
  const row = Math.floor((y - config.offsetY) / config.cellSize);

  if (row >= 0 && row < config.rowCount && col >= 0 && col < config.colCount) {
    return { row, col };
  }

  return null;
}
