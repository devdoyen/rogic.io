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
import { ref, onMounted, watch, onUnmounted, computed } from 'vue';
import { PuzzleBoard } from '../engine/puzzleBoard';
import { getGridCoordinates } from '../engine/coordinateMapper';

const props = defineProps<{
  board: PuzzleBoard;
  readOnly?: boolean;
  initialAngle?: number;
  rotationSteps?: number;
}>();

const emit = defineEmits<{
  (e: 'cell-click'): void;
}>();

const canvasRef = ref<HTMLCanvasElement | null>(null);

// Standard grid layout dimensions
const getCellSize = (maxCount: number) => {
  if (maxCount <= 10) return 30;
  if (maxCount <= 15) return 20;
  if (maxCount <= 20) return 15;
  return 10; // 30x30 or larger
};

let CELL_SIZE = getCellSize(Math.max(props.board.colCount, props.board.rowCount));

const playAngle = props.initialAngle !== undefined ? props.initialAngle : Math.PI / 4;
const targetOrthogonalAngle = computed(() => {
  return (props.initialAngle !== undefined ? props.initialAngle : 0) - (props.rotationSteps || 0) * Math.PI / 2;
});

// Function to select starting angle
const getStartingAngle = () => {
  if (props.board.isSolved()) {
    return targetOrthogonalAngle.value;
  }
  return playAngle;
};

const currentAngle = ref(getStartingAngle());

// Dynamic calculations for bounds
const getDimensions = () => {
  const boardWidth = props.board.colCount * CELL_SIZE;
  const boardHeight = props.board.rowCount * CELL_SIZE;
  const boardDiag = Math.sqrt(boardWidth * boardWidth + boardHeight * boardHeight);

  const maxRowHintsLength = Math.max(...props.board.rowHints.map(h => h.length), 1);
  const maxColHintsLength = Math.max(...props.board.colHints.map(h => h.length), 1);
  const hintPadding = Math.max(maxRowHintsLength, maxColHintsLength) * 18 + 40;

  const size = Math.ceil(boardDiag + hintPadding * 2);
  return {
    width: size,
    height: size,
    halfW: boardWidth / 2,
    halfH: boardHeight / 2
  };
};

const initialDims = getDimensions();
const config = {
  centerX: initialDims.width / 2,
  centerY: initialDims.height / 2,
  cellSize: CELL_SIZE,
  rowCount: props.board.rowCount,
  colCount: props.board.colCount,
  angle: currentAngle.value
};

function drawBoard() {
  const canvas = canvasRef.value;
  if (!canvas) return;
  const ctx = canvas.getContext('2d');
  if (!ctx) return;

  const { width, height, halfW, halfH } = getDimensions();
  canvas.width = width;
  canvas.height = height;

  config.centerX = width / 2;
  config.centerY = height / 2;
  config.angle = currentAngle.value;
  config.rowCount = props.board.rowCount;
  config.colCount = props.board.colCount;

  // Clear canvas (sleek dark themed layout)
  ctx.fillStyle = '#0f172a';
  ctx.fillRect(0, 0, width, height);

  ctx.save();
  ctx.translate(config.centerX, config.centerY);
  ctx.rotate(config.angle);

  // Draw background for overall active board area
  ctx.fillStyle = '#1e293b'; // slate-800
  ctx.fillRect(-halfW, -halfH, props.board.colCount * CELL_SIZE, props.board.rowCount * CELL_SIZE);

  // Draw grid cells
  for (let r = 0; r < props.board.rowCount; r++) {
    for (let c = 0; c < props.board.colCount; c++) {
      const x = -halfW + c * CELL_SIZE;
      const y = -halfH + r * CELL_SIZE;

      ctx.strokeStyle = '#334155'; // slate-700
      ctx.lineWidth = 1;
      ctx.strokeRect(x, y, CELL_SIZE, CELL_SIZE);

      const cellState = props.board.currentGrid[r][c];
      if (cellState === 1) {
        // Filled with premium gem gradient
        const grad = ctx.createLinearGradient(x, y, x + CELL_SIZE, y + CELL_SIZE);
        grad.addColorStop(0, '#38bdf8'); // sky-400
        grad.addColorStop(1, '#818cf8'); // indigo-400
        ctx.fillStyle = grad;
        ctx.fillRect(x + 1.5, y + 1.5, CELL_SIZE - 3, CELL_SIZE - 3);

        ctx.strokeStyle = '#6366f1';
        ctx.lineWidth = 1.5;
        ctx.strokeRect(x + 1.5, y + 1.5, CELL_SIZE - 3, CELL_SIZE - 3);
      } else if (cellState === 2) {
        // Marked (X)
        ctx.strokeStyle = '#f43f5e'; // Rose 500
        ctx.lineWidth = 2.5;
        ctx.beginPath();
        ctx.moveTo(x + CELL_SIZE / 4, y + CELL_SIZE / 4);
        ctx.lineTo(x + 3 * CELL_SIZE / 4, y + 3 * CELL_SIZE / 4);
        ctx.moveTo(x + 3 * CELL_SIZE / 4, y + CELL_SIZE / 4);
        ctx.lineTo(x + CELL_SIZE / 4, y + 3 * CELL_SIZE / 4);
        ctx.stroke();
      }
    }
  }

  // Draw bold line markers every 5 lines
  ctx.strokeStyle = '#64748b'; // slate-500
  ctx.lineWidth = 2.5;
  for (let r = 0; r <= props.board.rowCount; r += 5) {
    if (r > 0 && r < props.board.rowCount) {
      const y = -halfH + r * CELL_SIZE;
      ctx.beginPath();
      ctx.moveTo(-halfW, y);
      ctx.lineTo(halfW, y);
      ctx.stroke();
    }
  }
  for (let c = 0; c <= props.board.colCount; c += 5) {
    if (c > 0 && c < props.board.colCount) {
      const x = -halfW + c * CELL_SIZE;
      ctx.beginPath();
      ctx.moveTo(x, -halfH);
      ctx.lineTo(x, halfH);
      ctx.stroke();
    }
  }

  // Draw hints ONLY if not solved
  if (!props.board.isSolved()) {
    // Draw row hints (on the left side)
    ctx.fillStyle = '#94a3b8'; // slate-400
    ctx.font = 'bold 12px sans-serif';
    ctx.textAlign = 'center';
    ctx.textBaseline = 'middle';

    for (let r = 0; r < props.board.rowCount; r++) {
      const hints = props.board.rowHints[r] || [0];
      const y = -halfH + r * CELL_SIZE + CELL_SIZE / 2;
      for (let h = 0; h < hints.length; h++) {
        const hintVal = hints[hints.length - 1 - h];
        const hx = -halfW - 8 - h * 16;
        
        ctx.save();
        ctx.translate(hx, y);
        ctx.rotate(-config.angle);
        ctx.fillText(hintVal.toString(), 0, 0);
        ctx.restore();
      }
    }

    // Draw col hints (above the grid)
    ctx.textAlign = 'center';
    ctx.textBaseline = 'middle';

    for (let c = 0; c < props.board.colCount; c++) {
      const hints = props.board.colHints[c] || [0];
      const x = -halfW + c * CELL_SIZE + CELL_SIZE / 2;
      for (let h = 0; h < hints.length; h++) {
        const hintVal = hints[hints.length - 1 - h];
        const hy = -halfH - 8 - h * 16;
        
        ctx.save();
        ctx.translate(x, hy);
        ctx.rotate(-config.angle);
        ctx.fillText(hintVal.toString(), 0, 0);
        ctx.restore();
      }
    }
  }

  ctx.restore();
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

function animateRotationToTarget() {
  const duration = 1000; // 1 second
  const startAngle = currentAngle.value;
  const targetAngle = targetOrthogonalAngle.value;
  const startTime = performance.now();

  function tick(now: number) {
    const elapsed = now - startTime;
    const progress = Math.min(elapsed / duration, 1);

    // Easing: easeInOutCubic
    const ease = progress < 0.5 
      ? 4 * progress * progress * progress 
      : 1 - Math.pow(-2 * progress + 2, 3) / 2;

    currentAngle.value = startAngle + (targetAngle - startAngle) * ease;
    drawBoard();

    if (progress < 1) {
      requestAnimationFrame(tick);
    }
  }

  requestAnimationFrame(tick);
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
  CELL_SIZE = getCellSize(Math.max(props.board.colCount, props.board.rowCount));
  currentAngle.value = getStartingAngle();
  const dims = getDimensions();
  config.centerX = dims.width / 2;
  config.centerY = dims.height / 2;
  config.cellSize = CELL_SIZE;
  config.rowCount = props.board.rowCount;
  config.colCount = props.board.colCount;
  config.angle = currentAngle.value;
  drawBoard();
}, { deep: false });

// Watch for solved state to rotate to target
watch(() => props.board.isSolved(), (solved) => {
  if (solved) {
    animateRotationToTarget();
  }
});
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
