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
}>();

const emit = defineEmits<{
  (e: 'cell-click'): void;
}>();

const canvasRef = ref<HTMLCanvasElement | null>(null);

const CELL_SIZE = 30;
const OFFSET_X = 100;
const OFFSET_Y = 80;

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

  const width = OFFSET_X + props.board.colCount * CELL_SIZE;
  const height = OFFSET_Y + props.board.rowCount * CELL_SIZE;
  
  canvas.width = width;
  canvas.height = height;

  // Clear canvas
  ctx.fillStyle = '#ffffff';
  ctx.fillRect(0, 0, width, height);

  // Draw board background (grid area)
  ctx.fillStyle = '#f8fafc';
  ctx.fillRect(OFFSET_X, OFFSET_Y, props.board.colCount * CELL_SIZE, props.board.rowCount * CELL_SIZE);

  // Draw filled cells and X marks
  for (let r = 0; r < props.board.rowCount; r++) {
    for (let c = 0; c < props.board.colCount; c++) {
      const cellState = props.board.currentGrid[r][c];
      const x = OFFSET_X + c * CELL_SIZE;
      const y = OFFSET_Y + r * CELL_SIZE;

      if (cellState === 1) {
        // Filled
        ctx.fillStyle = '#0f172a'; // Slate 900
        ctx.fillRect(x + 1, y + 1, CELL_SIZE - 2, CELL_SIZE - 2);
      } else if (cellState === 2) {
        // Marked (X)
        ctx.strokeStyle = '#f43f5e'; // Rose 500
        ctx.lineWidth = 2;
        ctx.beginPath();
        ctx.moveTo(x + 6, y + 6);
        ctx.lineTo(x + CELL_SIZE - 6, y + CELL_SIZE - 6);
        ctx.moveTo(x + CELL_SIZE - 6, y + 6);
        ctx.lineTo(x + 6, y + CELL_SIZE - 6);
        ctx.stroke();
      }
    }
  }

  // Draw grid lines
  ctx.lineWidth = 1;
  for (let r = 0; r <= props.board.rowCount; r++) {
    const y = OFFSET_Y + r * CELL_SIZE;
    ctx.strokeStyle = r % 5 === 0 ? '#475569' : '#cbd5e1'; // Bold line every 5 rows
    ctx.lineWidth = r % 5 === 0 ? 2 : 1;
    ctx.beginPath();
    ctx.moveTo(OFFSET_X, y);
    ctx.lineTo(width, y);
    ctx.stroke();
  }

  for (let c = 0; c <= props.board.colCount; c++) {
    const x = OFFSET_X + c * CELL_SIZE;
    ctx.strokeStyle = c % 5 === 0 ? '#475569' : '#cbd5e1'; // Bold line every 5 cols
    ctx.lineWidth = c % 5 === 0 ? 2 : 1;
    ctx.beginPath();
    ctx.moveTo(x, OFFSET_Y);
    ctx.lineTo(x, height);
    ctx.stroke();
  }

  // Draw row hints (horizontal on the left)
  ctx.fillStyle = '#475569'; // Slate 600
  ctx.font = 'bold 13px sans-serif';
  ctx.textAlign = 'right';
  ctx.textBaseline = 'middle';

  for (let r = 0; r < props.board.rowCount; r++) {
    const hints = props.board.rowHints[r] || [0];
    const y = OFFSET_Y + r * CELL_SIZE + CELL_SIZE / 2;
    
    // Draw numbers from right to left
    for (let h = 0; h < hints.length; h++) {
      const hintVal = hints[hints.length - 1 - h];
      const x = OFFSET_X - 10 - h * 16;
      ctx.fillText(hintVal.toString(), x, y);
    }
  }

  // Draw col hints (vertical on the top)
  ctx.textAlign = 'center';
  ctx.textBaseline = 'bottom';

  for (let c = 0; c < props.board.colCount; c++) {
    const hints = props.board.colHints[c] || [0];
    const x = OFFSET_X + c * CELL_SIZE + CELL_SIZE / 2;

    // Draw numbers from bottom to top
    for (let h = 0; h < hints.length; h++) {
      const hintVal = hints[hints.length - 1 - h];
      const y = OFFSET_Y - 8 - h * 16;
      ctx.fillText(hintVal.toString(), x, y);
    }
  }
}

let isDragging = false;
let dragValue = 0; // 0: empty, 1: filled, 2: marked
let lastRow = -1;
let lastCol = -1;

function handleMouseDown(event: MouseEvent) {
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
  background-color: #ffffff;
  border-radius: 8px;
  box-shadow: 0 4px 6px -1px rgb(0 0 0 / 0.1), 0 2px 4px -2px rgb(0 0 0 / 0.1);
}
canvas {
  display: block;
  cursor: pointer;
}
</style>
