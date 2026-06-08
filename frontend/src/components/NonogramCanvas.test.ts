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
        width: 190,
        height: 170,
        top: 0,
        left: 0,
        right: 190,
        bottom: 170,
        x: 0,
        y: 0,
        toJSON: () => {}
      });

      // Mouse down on (0, 0) (left click): x = 115, y = 95
      await canvas.trigger('mousedown', {
        button: 0,
        clientX: 115,
        clientY: 95
      });
      expect(board.currentGrid[0][0]).toBe(1);

      // Drag to (0, 1): x = 145, y = 95
      window.dispatchEvent(new MouseEvent('mousemove', {
        clientX: 145,
        clientY: 95
      }));
      expect(board.currentGrid[0][1]).toBe(1);

      // Drag to (1, 1): x = 145, y = 125
      window.dispatchEvent(new MouseEvent('mousemove', {
        clientX: 145,
        clientY: 125
      }));
      expect(board.currentGrid[1][1]).toBe(1);

      // Mouse up
      window.dispatchEvent(new MouseEvent('mouseup'));

      // Move should no longer draw
      window.dispatchEvent(new MouseEvent('mousemove', {
        clientX: 175,
        clientY: 155
      }));
      expect(board.currentGrid[2][2]).toBe(0);
    });

    it('should drag to clear filled cells', async () => {
      const board = new PuzzleBoard([
        [1, 1],
        [1, 1]
      ]);
      board.currentGrid[0][0] = 1;
      board.currentGrid[0][1] = 1;
      const wrapper = mount(NonogramCanvas, {
        props: { board }
      });
      const canvas = wrapper.find('[data-testid="nonogram-canvas"]');

      canvas.element.getBoundingClientRect = () => ({
        width: 160,
        height: 140,
        top: 0,
        left: 0,
        right: 160,
        bottom: 140,
        x: 0,
        y: 0,
        toJSON: () => {}
      });

      // Mouse down on (0, 0) (left click, already filled -> target is empty 0)
      await canvas.trigger('mousedown', {
        button: 0,
        clientX: 115,
        clientY: 95
      });
      expect(board.currentGrid[0][0]).toBe(0);

      // Drag to (0, 1)
      window.dispatchEvent(new MouseEvent('mousemove', {
        clientX: 145,
        clientY: 95
      }));
      expect(board.currentGrid[0][1]).toBe(0);

      window.dispatchEvent(new MouseEvent('mouseup'));
    });

    it('should drag to mark multiple cells with X', async () => {
      const board = new PuzzleBoard([
        [0, 0],
        [0, 0]
      ]);
      const wrapper = mount(NonogramCanvas, {
        props: { board }
      });
      const canvas = wrapper.find('[data-testid="nonogram-canvas"]');

      canvas.element.getBoundingClientRect = () => ({
        width: 160,
        height: 140,
        top: 0,
        left: 0,
        right: 160,
        bottom: 140,
        x: 0,
        y: 0,
        toJSON: () => {}
      });

      // Mouse down on (0, 0) (right click): button = 2
      await canvas.trigger('mousedown', {
        button: 2,
        clientX: 115,
        clientY: 95
      });
      expect(board.currentGrid[0][0]).toBe(2);

      // Drag to (0, 1)
      window.dispatchEvent(new MouseEvent('mousemove', {
        clientX: 145,
        clientY: 95
      }));
      expect(board.currentGrid[0][1]).toBe(2);

      window.dispatchEvent(new MouseEvent('mouseup'));
    });

    it('should drag to clear X-marked cells', async () => {
      const board = new PuzzleBoard([
        [1, 1],
        [1, 1]
      ]);
      board.currentGrid[0][0] = 2;
      board.currentGrid[0][1] = 2;
      const wrapper = mount(NonogramCanvas, {
        props: { board }
      });
      const canvas = wrapper.find('[data-testid="nonogram-canvas"]');

      canvas.element.getBoundingClientRect = () => ({
        width: 160,
        height: 140,
        top: 0,
        left: 0,
        right: 160,
        bottom: 140,
        x: 0,
        y: 0,
        toJSON: () => {}
      });

      // Mouse down on (0, 0) (right click, already marked -> target is empty 0)
      await canvas.trigger('mousedown', {
        button: 2,
        clientX: 115,
        clientY: 95
      });
      expect(board.currentGrid[0][0]).toBe(0);

      // Drag to (0, 1)
      window.dispatchEvent(new MouseEvent('mousemove', {
        clientX: 145,
        clientY: 95
      }));
      expect(board.currentGrid[0][1]).toBe(0);

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
        width: 160,
        height: 140,
        top: 0,
        left: 0,
        right: 160,
        bottom: 140,
        x: 0,
        y: 0,
        toJSON: () => {}
      });

      // Mouse down on (0, 0)
      await canvas.trigger('mousedown', {
        button: 0,
        clientX: 115,
        clientY: 95
      });
      expect(board.currentGrid[0][0]).toBe(0); // Should remain 0 (unaltered)

      // Mouse move to (0, 1)
      window.dispatchEvent(new MouseEvent('mousemove', {
        clientX: 145,
        clientY: 95
      }));
      expect(board.currentGrid[0][1]).toBe(0); // Should remain 0 (unaltered)

      window.dispatchEvent(new MouseEvent('mouseup'));
    });
  });
});
