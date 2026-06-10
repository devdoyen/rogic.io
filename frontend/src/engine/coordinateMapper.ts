export interface CanvasConfig {
  centerX: number; // The x-coordinate of the center of the grid
  centerY: number; // The y-coordinate of the center of the grid
  cellSize: number; // Size of a square cell (width and height)
  rowCount: number;
  colCount: number;
  angle: number; // Rotation angle in radians
}

/**
 * Converts pixel coordinates (x, y) from click/touch events into grid indices (row, col)
 * under a dynamic rotation angle.
 */
export function getGridCoordinates(x: number, y: number, config: CanvasConfig): { row: number, col: number } | null {
  const dx = x - config.centerX;
  const dy = y - config.centerY;

  // Apply inverse rotation
  const cos = Math.cos(-config.angle);
  const sin = Math.sin(-config.angle);
  const rx = dx * cos - dy * sin;
  const ry = dx * sin + dy * cos;

  // Convert to row and column indices (centered grid layout)
  const col = Math.floor(rx / config.cellSize + config.colCount / 2);
  const row = Math.floor(ry / config.cellSize + config.rowCount / 2);

  if (row >= 0 && row < config.rowCount && col >= 0 && col < config.colCount) {
    return { row, col };
  }

  return null;
}

