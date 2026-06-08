import { describe, it, expect } from 'vitest';
import { mount } from '@vue/test-utils';
import NonogramCanvas from './NonogramCanvas.vue';
import { PuzzleBoard } from '../engine/puzzleBoard';
import { getGridCoordinates } from '../engine/coordinateMapper';
import type { CanvasConfig } from '../engine/coordinateMapper';

describe('NonogramCanvas TDD Red Phase', () => {
  describe('NonogramCanvas Vue Component mounting', () => {
    it('should mount the component and contain canvas element', () => {
      const board = new PuzzleBoard([
        [1, 0],
        [0, 1]
      ]);
      const wrapper = mount(NonogramCanvas, {
        props: { board }
      });
      const canvas = wrapper.find('[data-testid="nonogram-canvas"]');
      expect(canvas.exists()).toBe(true);
    });
  });

  describe('Coordinate mapping calculations', () => {
    const config: CanvasConfig = {
      offsetX: 100, // Hint space on the left
      offsetY: 80,  // Hint space on the top
      cellSize: 30,
      rowCount: 5,
      colCount: 5
    };

    it('should map click to correct grid cell index inside bounds', () => {
      // Cell (0, 0): offset + cell center/offset (x=115, y=95)
      expect(getGridCoordinates(115, 95, config)).toEqual({ row: 0, col: 0 });

      // Cell (4, 4): offset + (4 * 30) + offset (x=230, y=210)
      expect(getGridCoordinates(230, 210, config)).toEqual({ row: 4, col: 4 });
    });

    it('should return null for clicks inside the hint offset area', () => {
      // Left hint area: x < 100
      expect(getGridCoordinates(50, 95, config)).toBeNull();

      // Top hint area: y < 80
      expect(getGridCoordinates(115, 40, config)).toBeNull();
    });

    it('should return null for out of bounds clicks', () => {
      // Exceeding total canvas grid size (100 + 5 * 30 = 250, 80 + 5 * 30 = 230)
      expect(getGridCoordinates(260, 210, config)).toBeNull();
      expect(getGridCoordinates(230, 240, config)).toBeNull();
    });
  });
});
