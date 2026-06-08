import { calculateHints } from './hintCalculator';

export class PuzzleBoard {
  public readonly rowCount: number;
  public readonly colCount: number;
  public readonly solutionGrid: number[][];
  public readonly currentGrid: number[][];
  public readonly rowHints: number[][];
  public readonly colHints: number[][];

  constructor(solutionGrid: number[][]) {
    if (!solutionGrid || solutionGrid.length === 0) {
      throw new Error('Solution grid cannot be empty');
    }

    this.rowCount = solutionGrid.length;
    this.colCount = solutionGrid[0].length;

    if (this.colCount === 0) {
      throw new Error('Solution grid width cannot be zero');
    }

    // Validate uniform row lengths
    for (let r = 0; r < this.rowCount; r++) {
      if (!solutionGrid[r] || solutionGrid[r].length !== this.colCount) {
        throw new Error('Solution grid must have uniform row lengths');
      }
    }

    this.solutionGrid = solutionGrid;

    // Initialize current board states to empty (0)
    this.currentGrid = Array.from({ length: this.rowCount }, () =>
      Array(this.colCount).fill(0)
    );

    // Calculate row & column hints
    const hints = calculateHints(solutionGrid);
    this.rowHints = hints.rowHints;
    this.colHints = hints.colHints;
  }

  public toggleFill(row: number, col: number): void {
    if (row < 0 || row >= this.rowCount || col < 0 || col >= this.colCount) return;
    const current = this.currentGrid[row][col];
    this.currentGrid[row][col] = current === 1 ? 0 : 1;
  }

  public toggleMark(row: number, col: number): void {
    if (row < 0 || row >= this.rowCount || col < 0 || col >= this.colCount) return;
    const current = this.currentGrid[row][col];
    this.currentGrid[row][col] = current === 2 ? 0 : 2;
  }

  public isSolved(): boolean {
    for (let r = 0; r < this.rowCount; r++) {
      for (let c = 0; c < this.colCount; c++) {
        const isSolutionFilled = this.solutionGrid[r][c] === 1;
        const isPlayerFilled = this.currentGrid[r][c] === 1;
        if (isSolutionFilled !== isPlayerFilled) {
          return false;
        }
      }
    }
    return true;
  }
}
