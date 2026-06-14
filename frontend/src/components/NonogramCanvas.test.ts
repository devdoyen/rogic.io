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
      centerX: 200,
      centerY: 200,
      cellSize: 30,
      rowCount: 3,
      colCount: 3,
      angle: 0 // orthogonal for simplicity in basic tests
    };

    it('should map click to correct grid cell index inside bounds at 0 angle', () => {
      // Center cell (1, 1)
      expect(getGridCoordinates(200, 200, config)).toEqual({ row: 1, col: 1 });

      // Top left cell (0, 0)
      expect(getGridCoordinates(180, 180, config)).toEqual({ row: 0, col: 0 });

      // Bottom right cell (2, 2)
      expect(getGridCoordinates(220, 220, config)).toEqual({ row: 2, col: 2 });
    });

    it('should map click under 45-degree angle correctly', () => {
      const rotatedConfig = { ...config, angle: Math.PI / 4 };
      // Center remains (1, 1)
      expect(getGridCoordinates(200, 200, rotatedConfig)).toEqual({ row: 1, col: 1 });

      // Cell (0, 1): unrotated center is at x=200, y=170. Rotated by 45 degrees, it maps to x=221.21, y=178.79
      expect(getGridCoordinates(221, 179, rotatedConfig)).toEqual({ row: 0, col: 1 });
    });

    it('should return null for clicks out of bounds', () => {
      // Way off top: y = 50
      expect(getGridCoordinates(200, 50, config)).toBeNull();

      // Way off left: x = 50
      expect(getGridCoordinates(50, 200, config)).toBeNull();
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
        props: { board, initialAngle: 0 }
      });
      const canvas = wrapper.find('[data-testid="nonogram-canvas"]');

      // Mock getBoundingClientRect
      canvas.element.getBoundingClientRect = () => ({
        width: 400,
        height: 400,
        top: 0,
        left: 0,
        right: 400,
        bottom: 400,
        x: 0,
        y: 0,
        toJSON: () => {}
      });

      // Mouse down on (0, 0) which is (92, 92) relative to dynamic center 122
      await canvas.trigger('mousedown', {
        button: 0,
        clientX: 92,
        clientY: 92
      });
      expect(board.currentGrid[0][0]).toBe(1);

      // Drag to (1, 1) which is (122, 122)
      window.dispatchEvent(new MouseEvent('mousemove', {
        clientX: 122,
        clientY: 122
      }));
      expect(board.currentGrid[1][1]).toBe(1);

      // Mouse up
      window.dispatchEvent(new MouseEvent('mouseup'));

      // Move should no longer draw
      window.dispatchEvent(new MouseEvent('mousemove', {
        clientX: 152,
        clientY: 152
      }));
      expect(board.currentGrid[2][2]).toBe(0);
    });

    it('should drag to clear filled cells', async () => {
      const board = new PuzzleBoard([
        [1, 1],
        [1, 1]
      ]);
      board.currentGrid[0][0] = 1;
      board.currentGrid[1][1] = 1;
      const wrapper = mount(NonogramCanvas, {
        props: { board, initialAngle: 0 }
      });
      const canvas = wrapper.find('[data-testid="nonogram-canvas"]');

      canvas.element.getBoundingClientRect = () => ({
        width: 400,
        height: 400,
        top: 0,
        left: 0,
        right: 400,
        bottom: 400,
        x: 0,
        y: 0,
        toJSON: () => {}
      });

      // Mouse down on (0, 0) which is (85, 85) relative to center 100.5
      await canvas.trigger('mousedown', {
        button: 0,
        clientX: 85,
        clientY: 85
      });
      expect(board.currentGrid[0][0]).toBe(0);

      // Drag to (1, 1) which is (115, 115)
      window.dispatchEvent(new MouseEvent('mousemove', {
        clientX: 115,
        clientY: 115
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
        props: { board, readOnly: true, initialAngle: 0 }
      });
      const canvas = wrapper.find('[data-testid="nonogram-canvas"]');

      canvas.element.getBoundingClientRect = () => ({
        width: 400,
        height: 400,
        top: 0,
        left: 0,
        right: 400,
        bottom: 400,
        x: 0,
        y: 0,
        toJSON: () => {}
      });

      // Mouse down on (0, 0)
      await canvas.trigger('mousedown', {
        button: 0,
        clientX: 85,
        clientY: 85
      });
      expect(board.currentGrid[0][0]).toBe(0); // Should remain 0
    });
  });

  describe('Draw mode selection and mobile touch support', () => {
    it('should render draw mode selection buttons', () => {
      const board = new PuzzleBoard([
        [0, 0],
        [0, 0]
      ]);
      const wrapper = mount(NonogramCanvas, {
        props: { board }
      });
      const fillBtn = wrapper.find('.draw-mode-btn[title="Fill Mode"]');
      const xBtn = wrapper.find('.draw-mode-btn[title="X Mark Mode"]');
      expect(fillBtn.exists()).toBe(true);
      expect(xBtn.exists()).toBe(true);
    });

    it('should draw X marks on left click when drawMode is set to x', async () => {
      const board = new PuzzleBoard([
        [0, 0],
        [0, 0]
      ]);
      const wrapper = mount(NonogramCanvas, {
        props: { board, initialAngle: 0 }
      });
      const canvas = wrapper.find('[data-testid="nonogram-canvas"]');
      canvas.element.getBoundingClientRect = () => ({
        width: 400,
        height: 400,
        top: 0,
        left: 0,
        right: 400,
        bottom: 400,
        x: 0,
        y: 0,
        toJSON: () => {}
      });

      // Toggle to X mode
      const xBtn = wrapper.find('.draw-mode-btn[title="X Mark Mode"]');
      await xBtn.trigger('click');

      // Click on cell (0, 0)
      await canvas.trigger('mousedown', {
        button: 0,
        clientX: 85,
        clientY: 85
      });
      expect(board.currentGrid[0][0]).toBe(2); // Should be marked as X (2)
    });

    it('should handle touch drag to draw based on active mode', async () => {
      const board = new PuzzleBoard([
        [0, 0],
        [0, 0]
      ]);
      const wrapper = mount(NonogramCanvas, {
        props: { board, initialAngle: 0 }
      });
      const canvas = wrapper.find('[data-testid="nonogram-canvas"]');
      canvas.element.getBoundingClientRect = () => ({
        width: 400,
        height: 400,
        top: 0,
        left: 0,
        right: 400,
        bottom: 400,
        x: 0,
        y: 0,
        toJSON: () => {}
      });

      // Dispatch touchstart on (0, 0)
      await canvas.trigger('touchstart', {
        touches: [{ clientX: 85, clientY: 85 }]
      });
      expect(board.currentGrid[0][0]).toBe(1); // Default is Fill Mode (1)

      // Touch drag to (1, 1)
      window.dispatchEvent(new TouchEvent('touchmove', {
        touches: [{ clientX: 115, clientY: 115 } as any]
      }));
      expect(board.currentGrid[1][1]).toBe(1);

      // Touch end
      window.dispatchEvent(new TouchEvent('touchend'));
    });
  });
});
