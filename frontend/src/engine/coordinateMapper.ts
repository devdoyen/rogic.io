export interface CanvasConfig {
  offsetX: number; // The x-coordinate of the top corner of the diamond grid (row=0, col=0)
  offsetY: number; // The y-coordinate of the top corner of the diamond grid (row=0, col=0)
  cellSize: number; // Half-width of the diamond cell (also equals half-height to keep 45 deg, so total width=2*cellSize, height=2*cellSize)
  rowCount: number;
  colCount: number;
}

/**
 * Converts pixel coordinates (x, y) from click/touch events into grid indices (row, col)
 * under a 45-degree diamond projection.
 *
 * Matrix translation:
 * In isometric projection:
 * x_pixel = offsetX + (col - row) * cellSize
 * y_pixel = offsetY + (col + row) * (cellSize / 2)  -- Standard 2:1 projection
 * Let's use standard 45-degree rotation where cells are uniform:
 * x_pixel = offsetX + (col - row) * cellSize
 * y_pixel = offsetY + (col + row) * cellSize
 *
 * To invert:
 * col - row = (x_pixel - offsetX) / cellSize
 * col + row = (y_pixel - offsetY) / cellSize
 *
 * Adding them:
 * 2 * col = (x_pixel - offsetX)/cellSize + (y_pixel - offsetY)/cellSize
 * Subtracting them:
 * 2 * row = (y_pixel - offsetY)/cellSize - (x_pixel - offsetX)/cellSize
 */
export function getGridCoordinates(x: number, y: number, config: CanvasConfig): { row: number, col: number } | null {
  const dx = (x - config.offsetX) / config.cellSize;
  const dy = (y - config.offsetY) / config.cellSize;

  // Compute raw floating point indices
  const col = Math.floor((dx + dy) / 2);
  const row = Math.floor((dy - dx) / 2);

  if (row >= 0 && row < config.rowCount && col >= 0 && col < config.colCount) {
    return { row, col };
  }

  return null;
}
