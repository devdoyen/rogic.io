<template>
  <div class="nonogram-canvas-container">
    <canvas 
      ref="canvasRef" 
      data-testid="nonogram-canvas" 
      @mousedown="handleMouseDown"
      @contextmenu.prevent
    ></canvas>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch, onUnmounted } from 'vue';
import { PuzzleBoard } from '../engine/puzzleBoard';
import { getGridCoordinates } from '../engine/coordinateMapper';
import type { CanvasConfig } from '../engine/coordinateMapper';

const props = defineProps<{
  board: PuzzleBoard;
  readOnly?: boolean;
}>();

const emit = defineEmits<{
  (e: 'cell-click'): void;
}>();

const canvasRef = ref<HTMLCanvasElement | null>(null);

// Diamond grid layout dimensions
const CELL_SIZE = 30; // half-width/half-height of the diamond cell

// Let's position the top corner (row=0, col=0)
const OFFSET_X = 200; 
const OFFSET_Y = 100;

const config: CanvasConfig = {
  offsetX: OFFSET_X,
  offsetY: OFFSET_Y,
  cellSize: CELL_SIZE,
  rowCount: props.board.rowCount,
  colCount: props.board.colCount
};

function drawBoard() {
  const canvas = canvasRef.value;
  if (!canvas) return;
  const ctx = canvas.getContext('2d');
  if (!ctx) return;

  // Max bounds of the diamond grid:
  // X spans from OFFSET_X - rowCount * CELL_SIZE to OFFSET_X + colCount * CELL_SIZE
  // Y spans from OFFSET_Y to OFFSET_Y + (colCount + rowCount) * CELL_SIZE
  const width = OFFSET_X + props.board.colCount * CELL_SIZE + 100;
  const height = OFFSET_Y + (props.board.rowCount + props.board.colCount) * CELL_SIZE + 50;
  
  canvas.width = width;
  canvas.height = height;

  // Clear canvas (sleek dark themed layout)
  ctx.fillStyle = '#0f172a';
  ctx.fillRect(0, 0, width, height);

  // Helper function to draw diamond cell path
  const pathDiamond = (r: number, c: number) => {
    // Top corner (r, c) starting from (0, 0)
    // For cell center logic or top corner:
    // x = offsetX + (c - r) * cellSize
    // y = offsetY + (c + r) * cellSize
    const cx = OFFSET_X + (c - r) * CELL_SIZE;
    const cy = OFFSET_Y + (c + r) * CELL_SIZE;
    
    ctx.beginPath();
    ctx.moveTo(cx, cy); // Top corner
    ctx.lineTo(cx + CELL_SIZE, cy + CELL_SIZE); // Right corner
    ctx.lineTo(cx, cy + 2 * CELL_SIZE); // Bottom corner
    ctx.lineTo(cx - CELL_SIZE, cy + CELL_SIZE); // Left corner
    ctx.closePath();
  };

  // Draw background for overall active board area
  for (let r = 0; r < props.board.rowCount; r++) {
    for (let c = 0; c < props.board.colCount; c++) {
      pathDiamond(r, c);
      ctx.fillStyle = '#1e293b'; // slate-800
      ctx.fill();
      ctx.strokeStyle = '#334155'; // slate-700
      ctx.lineWidth = 1;
      ctx.stroke();
    }
  }

  // Draw filled cells and X marks
  for (let r = 0; r < props.board.rowCount; r++) {
    for (let c = 0; c < props.board.colCount; c++) {
      const cellState = props.board.currentGrid[r][c];
      const cx = OFFSET_X + (c - r) * CELL_SIZE;
      const cy = OFFSET_Y + (c + r) * CELL_SIZE;

      if (cellState === 1) {
        // Filled with premium gem gradient
        pathDiamond(r, c);
        const grad = ctx.createLinearGradient(cx - CELL_SIZE, cy, cx + CELL_SIZE, cy + 2 * CELL_SIZE);
        grad.addColorStop(0, '#38bdf8'); // sky-400
        grad.addColorStop(1, '#818cf8'); // indigo-400
        ctx.fillStyle = grad;
        ctx.fill();
        ctx.strokeStyle = '#6366f1';
        ctx.lineWidth = 1.5;
        ctx.stroke();
      } else if (cellState === 2) {
        // Marked (X)
        ctx.strokeStyle = '#f43f5e'; // Rose 500
        ctx.lineWidth = 2.5;
        ctx.beginPath();
        // Inner coordinates inside the diamond
        ctx.moveTo(cx - CELL_SIZE / 3, cy + CELL_SIZE - CELL_SIZE / 3);
        ctx.lineTo(cx + CELL_SIZE / 3, cy + CELL_SIZE + CELL_SIZE / 3);
        ctx.moveTo(cx + CELL_SIZE / 3, cy + CELL_SIZE - CELL_SIZE / 3);
        ctx.lineTo(cx - CELL_SIZE / 3, cy + CELL_SIZE + CELL_SIZE / 3);
        ctx.stroke();
      }
    }
  }

  // Draw bold line markers every 5 lines
  ctx.strokeStyle = '#64748b'; // slate-500
  ctx.lineWidth = 2;
  for (let r = 0; r <= props.board.rowCount; r += 5) {
    if (r === 0 || r === props.board.rowCount) continue;
    // Row line boundary separator
  }

  // Draw row hints (aligned along the NW boundary slope, rotated to match the slope, shifted outside)
  ctx.fillStyle = '#94a3b8'; // slate-400
  ctx.font = 'bold 12px sans-serif';
  ctx.textAlign = 'right';
  ctx.textBaseline = 'middle';

  for (let r = 0; r < props.board.rowCount; r++) {
    const hints = props.board.rowHints[r] || [0];
    // The top-left boundary corner of cell (r, 0) is at x = OFFSET_X - r*CELL_SIZE, y = OFFSET_Y + r*CELL_SIZE.
    // To move outside the board (up and left, i.e., in the negative-col direction):
    // We offset the start position by moving along the col=-1 vector: x_offset = -CELL_SIZE/2, y_offset = -CELL_SIZE/2
    const baseLx = OFFSET_X - r * CELL_SIZE - CELL_SIZE / 2;
    const baseLy = OFFSET_Y + r * CELL_SIZE + CELL_SIZE / 2;

    // Draw hints outwards along the NW direction
    for (let h = 0; h < hints.length; h++) {
      const hintVal = hints[hints.length - 1 - h];
      // h * 16 pixels further away outwards along the NW vector
      const hx = baseLx - 8 - h * 16;
      const hy = baseLy - 8 - h * 16;

      ctx.save();
      ctx.translate(hx, hy);
      ctx.rotate(-Math.PI / 4); // Rotate -45 degrees
      ctx.fillText(hintVal.toString(), 0, 0);
      ctx.restore();
    }
  }

  // Draw col hints (aligned along the NE boundary slope, rotated to match the slope, shifted outside)
  ctx.textAlign = 'left';
  ctx.textBaseline = 'middle';

  for (let c = 0; c < props.board.colCount; c++) {
    const hints = props.board.colHints[c] || [0];
    // The top-right boundary corner of cell (0, c) is at x = OFFSET_X + c*CELL_SIZE, y = OFFSET_Y + c*CELL_SIZE.
    // To move outside the board (up and right, i.e., in the negative-row direction):
    // We offset the start position by moving along the row=-1 vector: x_offset = +CELL_SIZE/2, y_offset = -CELL_SIZE/2
    const baseRx = OFFSET_X + c * CELL_SIZE + CELL_SIZE / 2;
    const baseRy = OFFSET_Y + c * CELL_SIZE + CELL_SIZE / 2;

    // Draw hints outwards along the NE direction
    for (let h = 0; h < hints.length; h++) {
      const hintVal = hints[hints.length - 1 - h];
      // h * 16 pixels further away outwards along the NE vector
      const hx = baseRx + 8 + h * 16;
      const hy = baseRy - 8 - h * 16;

      ctx.save();
      ctx.translate(hx, hy);
      ctx.rotate(Math.PI / 4); // Rotate 45 degrees
      ctx.fillText(hintVal.toString(), 0, 0);
      ctx.restore();
    }
  }
}



let isDragging = false;
let dragValue = 0; // 0: empty, 1: filled, 2: marked
let lastRow = -1;
let lastCol = -1;

function handleMouseDown(event: MouseEvent) {
  if (props.readOnly) return;
  const canvas = canvasRef.value;
  if (!canvas) return;

  const rect = canvas.getBoundingClientRect();
  const clickX = event.clientX - rect.left;
  const clickY = event.clientY - rect.top;

  const coords = getGridCoordinates(clickX, clickY, config);
  if (!coords) return;

  const { row, col } = coords;
  const currentValue = props.board.currentGrid[row][col];

  if (event.button === 0) {
    // Left Click
    dragValue = currentValue === 1 ? 0 : 1;
  } else if (event.button === 2) {
    // Right Click
    dragValue = currentValue === 2 ? 0 : 2;
  } else {
    return;
  }

  isDragging = true;
  props.board.setCell(row, col, dragValue);
  lastRow = row;
  lastCol = col;

  drawBoard();
  emit('cell-click');

  window.addEventListener('mousemove', handleWindowMouseMove);
  window.addEventListener('mouseup', handleWindowMouseUp);
}

function handleWindowMouseMove(event: MouseEvent) {
  if (!isDragging) return;

  const canvas = canvasRef.value;
  if (!canvas) return;

  const rect = canvas.getBoundingClientRect();
  const clickX = event.clientX - rect.left;
  const clickY = event.clientY - rect.top;

  const coords = getGridCoordinates(clickX, clickY, config);
  if (!coords) return;

  const { row, col } = coords;
  if (row !== lastRow || col !== lastCol) {
    props.board.setCell(row, col, dragValue);
    lastRow = row;
    lastCol = col;
    drawBoard();
    emit('cell-click');
  }
}

function handleWindowMouseUp() {
  if (isDragging) {
    isDragging = false;
    window.removeEventListener('mousemove', handleWindowMouseMove);
    window.removeEventListener('mouseup', handleWindowMouseUp);
  }
}

onMounted(() => {
  drawBoard();
});

onUnmounted(() => {
  window.removeEventListener('mousemove', handleWindowMouseMove);
  window.removeEventListener('mouseup', handleWindowMouseUp);
});

// Redraw if board changes
watch(() => props.board, () => {
  drawBoard();
}, { deep: true });
</script>

<style scoped>
.nonogram-canvas-container {
  display: inline-block;
  padding: 10px;
  background-color: #0f172a; /* matches app slate-900 background */
  border-radius: 12px;
  box-shadow: 0 4px 6px -1px rgb(0 0 0 / 0.1), 0 2px 4px -2px rgb(0 0 0 / 0.1);
}
canvas {
  display: block;
  cursor: pointer;
}
</style>
