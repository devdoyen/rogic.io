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
      offsetX: 200, // Top corner X coordinate
      offsetY: 100, // Top corner Y coordinate
      cellSize: 30, // Half-width/half-height of diamond cells
      rowCount: 3,
      colCount: 3
    };

    it('should map click to correct grid cell index inside bounds', () => {
      // For row=0, col=0: dx = 0, dy = 0. Center is x = 200, y = 130
      // dx_pixel = (200 - 200)/30 = 0, dy_pixel = (130 - 100)/30 = 1
      // col = floor((0 + 1)/2) = 0, row = floor((1 - 0)/2) = 0
      expect(getGridCoordinates(200, 130, config)).toEqual({ row: 0, col: 0 });

      // For row=1, col=1: col-row=0, col+row=2. Center is x = 200, y = 160
      // dx = 0, dy = (160 - 100)/30 = 2 -> col = floor((0+2)/2) = 1, row = floor((2-0)/2) = 1
      expect(getGridCoordinates(200, 160, config)).toEqual({ row: 1, col: 1 });

      // For row=0, col=2: col-row=2, col+row=2. Center is x = 200 + 2*30 = 260, y = 160
      // dx = (260 - 200)/30 = 2, dy = (160 - 100)/30 = 2
      // col = floor(4/2) = 2, row = floor(0/2) = 0
      expect(getGridCoordinates(260, 160, config)).toEqual({ row: 0, col: 2 });
    });

    it('should return null for clicks inside the hint offset area or out of bounds', () => {
      // Way off top: y = 50
      expect(getGridCoordinates(200, 50, config)).toBeNull();

      // Way off left: x = 50
      expect(getGridCoordinates(50, 160, config)).toBeNull();
    });
  });

  describe('Mouse drag interactions on canvas', () => {
    it('should drag to fill multiple cells', async () => {
      const board = new PuzzleBoard([
        [0, 0, 0],
        [0, 0, 0],
        [0, 0, 0]
      ]);
      const wrapper = mount(NonogramCanvas, {
        props: { board }
      });
      const canvas = wrapper.find('[data-testid="nonogram-canvas"]');

      // Mock getBoundingClientRect
      canvas.element.getBoundingClientRect = () => ({
        width: 400,
        height: 300,
        top: 0,
        left: 0,
        right: 400,
        bottom: 300,
        x: 0,
        y: 0,
        toJSON: () => {}
      });

      // Mouse down on (0, 0) (left click): x = 200, y = 130
      await canvas.trigger('mousedown', {
        button: 0,
        clientX: 200,
        clientY: 130
      });
      expect(board.currentGrid[0][0]).toBe(1);

      // Drag to (1, 1): x = 200, y = 160
      window.dispatchEvent(new MouseEvent('mousemove', {
        clientX: 200,
        clientY: 160
      }));
      expect(board.currentGrid[1][1]).toBe(1);

      // Mouse up
      window.dispatchEvent(new MouseEvent('mouseup'));

      // Move should no longer draw
      window.dispatchEvent(new MouseEvent('mousemove', {
        clientX: 260,
        clientY: 160
      }));
      expect(board.currentGrid[0][2]).toBe(0);
    });

    it('should drag to clear filled cells', async () => {
      const board = new PuzzleBoard([
        [1, 1],
        [1, 1]
      ]);
      board.currentGrid[0][0] = 1;
      board.currentGrid[1][1] = 1;
      const wrapper = mount(NonogramCanvas, {
        props: { board }
      });
      const canvas = wrapper.find('[data-testid="nonogram-canvas"]');

      canvas.element.getBoundingClientRect = () => ({
        width: 400,
        height: 300,
        top: 0,
        left: 0,
        right: 400,
        bottom: 300,
        x: 0,
        y: 0,
        toJSON: () => {}
      });

      // Mouse down on (0, 0)
      await canvas.trigger('mousedown', {
        button: 0,
        clientX: 200,
        clientY: 130
      });
      expect(board.currentGrid[0][0]).toBe(0);

      // Drag to (1, 1)
      window.dispatchEvent(new MouseEvent('mousemove', {
        clientX: 200,
        clientY: 160
      }));
      expect(board.currentGrid[1][1]).toBe(0);

      window.dispatchEvent(new MouseEvent('mouseup'));
    });
  });

  describe('Read-Only mode interactions on canvas', () => {
    it('should not alter board state on mouse down or drag if readOnly is true', async () => {
      const board = new PuzzleBoard([
        [0, 0],
        [0, 0]
      ]);
      const wrapper = mount(NonogramCanvas, {
        props: { board, readOnly: true }
      });
      const canvas = wrapper.find('[data-testid="nonogram-canvas"]');

      canvas.element.getBoundingClientRect = () => ({
        width: 400,
        height: 300,
        top: 0,
        left: 0,
        right: 400,
        bottom: 300,
        x: 0,
        y: 0,
        toJSON: () => {}
      });

      // Mouse down on (0, 0)
      await canvas.trigger('mousedown', {
        button: 0,
        clientX: 200,
        clientY: 130
      });
      expect(board.currentGrid[0][0]).toBe(0); // Should remain 0
    });
  });
});

