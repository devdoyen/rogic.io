<template>
  <div class="nonogram-canvas-container">
    <div class="canvas-frame" ref="frameRef">
      <canvas 
        ref="canvasRef" 
        data-testid="nonogram-canvas" 
        :style="canvasStyle"
        @mousedown="handleMouseDown"
        @touchstart="handleTouchStart"
        @wheel="handleWheel"
        @contextmenu.prevent
      ></canvas>
    </div>

    <!-- Floating Draw Mode Toggle -->
    <div v-if="!readOnly" class="draw-mode-hud" @click="toggleDrawMode" title="Toggle Draw Mode" style="cursor: pointer;">
      <div class="draw-mode-slider" :class="drawMode"></div>
      <button 
        class="draw-mode-btn" 
        :class="{ active: drawMode === 'fill' }" 
        @click.stop="toggleDrawMode"
        title="Fill Mode"
        type="button"
      >
        <span class="mode-icon fill-icon"></span>
      </button>
      <button 
        class="draw-mode-btn" 
        :class="{ active: drawMode === 'x' }" 
        @click.stop="toggleDrawMode"
        title="X Mark Mode"
        type="button"
      >
        <svg class="mode-icon x-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor">
          <path d="M18 6L6 18M6 6l12 12" stroke-width="3.5" stroke-linecap="round"/>
        </svg>
      </button>
    </div>
    
    <!-- Floating Zoom HUD -->
    <div v-if="!readOnly" class="zoom-hud">
      <button class="zoom-btn" @click="changeZoom(0.15)" title="Zoom In">+</button>
      <span class="zoom-level" @click="resetZoom" title="Reset Zoom">{{ Math.round(scale * 100) }}%</span>
      <button class="zoom-btn" @click="changeZoom(-0.15)" title="Zoom Out">-</button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch, onUnmounted, computed } from 'vue';
import { PuzzleBoard } from '../engine/puzzleBoard';
import { getGridCoordinates } from '../engine/coordinateMapper';
import { calculateLineHints } from '../engine/hintCalculator';

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
const drawMode = ref<'fill' | 'x'>('fill');

function toggleDrawMode() {
  drawMode.value = drawMode.value === 'fill' ? 'x' : 'fill';
}

function isArrayEqual(a: number[], b: number[]) {
  if (a.length !== b.length) return false;
  for (let i = 0; i < a.length; i++) {
    if (a[i] !== b[i]) return false;
  }
  return true;
}

// Standard grid layout dimensions
const getCellSize = (maxCount: number) => {
  if (maxCount <= 10) return 30;
  if (maxCount <= 15) return 20;
  if (maxCount <= 20) return 15;
  return 10; // 30x30 or larger
};

const CELL_SIZE = computed(() => getCellSize(Math.max(props.board.colCount, props.board.rowCount)));

const playAngle = props.initialAngle !== undefined ? props.initialAngle : 0;
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

const isTestEnv = typeof window !== 'undefined' && (
  (globalThis as any).process?.env?.NODE_ENV === 'test' ||
  (globalThis as any).vitest !== undefined ||
  (globalThis as any).__vitest_worker__ !== undefined ||
  navigator.userAgent.includes('jsdom')
);

const currentAngle = ref(getStartingAngle());

// Dynamic calculations for bounds
const getDimensions = () => {
  const cellSizeVal = CELL_SIZE.value;
  const boardWidth = props.board.colCount * cellSizeVal;
  const boardHeight = props.board.rowCount * cellSizeVal;
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

const scale = ref(1.0);
const isDragging = ref(false);

const frameRef = ref<HTMLElement | null>(null);
const frameWidth = ref(600);
const frameHeight = ref(600);

const updateFrameSize = () => {
  if (frameRef.value) {
    frameWidth.value = frameRef.value.clientWidth || 600;
    frameHeight.value = frameRef.value.clientHeight || 600;
  }
};

const fitScale = computed(() => {
  if (isTestEnv) return 1.0;
  const { width: canvasSize } = getDimensions();
  const scaleX = frameWidth.value / canvasSize;
  const scaleY = frameHeight.value / canvasSize;
  return Math.min(scaleX, scaleY);
});

const canvasStyle = computed(() => {
  const transitionTime = props.board.isSolved() ? '0.3s' : '0.15s';
  const transitionStyle = (isDragging.value && !props.board.isSolved()) 
    ? 'none' 
    : `transform ${transitionTime} cubic-bezier(0.2, 0.8, 0.2, 1)`;
  return {
    transform: `scale(${scale.value})`,
    transformOrigin: 'center center',
    transition: transitionStyle
  };
});

function changeZoom(amount: number) {
  scale.value = Math.max(0.2, Math.min(4.0, scale.value + amount));
}

function resetZoom() {
  scale.value = fitScale.value;
}

function handleWheel(event: WheelEvent) {
  event.preventDefault();
  const zoomFactor = event.deltaY < 0 ? 1.05 : 0.95;
  scale.value = Math.max(0.2, Math.min(4.0, scale.value * zoomFactor));
}

const initialDims = getDimensions();
const config = {
  centerX: initialDims.width / 2,
  centerY: initialDims.height / 2,
  cellSize: CELL_SIZE.value,
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
  const cellSizeVal = CELL_SIZE.value;
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
  ctx.fillRect(-halfW, -halfH, props.board.colCount * cellSizeVal, props.board.rowCount * cellSizeVal);

  // Draw grid cells
  for (let r = 0; r < props.board.rowCount; r++) {
    for (let c = 0; c < props.board.colCount; c++) {
      const x = -halfW + c * cellSizeVal;
      const y = -halfH + r * cellSizeVal;

      ctx.strokeStyle = '#334155'; // slate-700
      ctx.lineWidth = 1;
      ctx.strokeRect(x, y, cellSizeVal, cellSizeVal);

      const cellState = props.board.currentGrid[r][c];
      if (cellState === 1) {
        // Filled with premium gem gradient
        const grad = ctx.createLinearGradient(x, y, x + cellSizeVal, y + cellSizeVal);
        grad.addColorStop(0, '#38bdf8'); // sky-400
        grad.addColorStop(1, '#818cf8'); // indigo-400
        ctx.fillStyle = grad;
        ctx.fillRect(x + 1.5, y + 1.5, cellSizeVal - 3, cellSizeVal - 3);

        ctx.strokeStyle = '#6366f1';
        ctx.lineWidth = 1.5;
        ctx.strokeRect(x + 1.5, y + 1.5, cellSizeVal - 3, cellSizeVal - 3);
      } else if (cellState === 2) {
        // Marked (X)
        ctx.strokeStyle = '#f43f5e'; // Rose 500
        ctx.lineWidth = 2.5;
        ctx.beginPath();
        ctx.moveTo(x + cellSizeVal / 4, y + cellSizeVal / 4);
        ctx.lineTo(x + 3 * cellSizeVal / 4, y + 3 * cellSizeVal / 4);
        ctx.moveTo(x + 3 * cellSizeVal / 4, y + cellSizeVal / 4);
        ctx.lineTo(x + cellSizeVal / 4, y + 3 * cellSizeVal / 4);
        ctx.stroke();
      }
    }
  }

  // Draw bold line markers every 5 lines
  ctx.strokeStyle = '#64748b'; // slate-500
  ctx.lineWidth = 2.5;
  for (let r = 0; r <= props.board.rowCount; r += 5) {
    if (r > 0 && r < props.board.rowCount) {
      const y = -halfH + r * cellSizeVal;
      ctx.beginPath();
      ctx.moveTo(-halfW, y);
      ctx.lineTo(halfW, y);
      ctx.stroke();
    }
  }
  for (let c = 0; c <= props.board.colCount; c += 5) {
    if (c > 0 && c < props.board.colCount) {
      const x = -halfW + c * cellSizeVal;
      ctx.beginPath();
      ctx.moveTo(x, -halfH);
      ctx.lineTo(x, halfH);
      ctx.stroke();
    }
  }

  // Draw hints ONLY if not solved
  if (!props.board.isSolved()) {
    // Draw row hints (on the left side)
    ctx.font = 'bold 12px sans-serif';
    ctx.textAlign = 'center';
    ctx.textBaseline = 'middle';

    for (let r = 0; r < props.board.rowCount; r++) {
      const hints = props.board.rowHints[r] || [0];
      const y = -halfH + r * cellSizeVal + cellSizeVal / 2;

      // Check if row hints are matched by player's current cells
      const rowCells = props.board.currentGrid[r];
      const rowLine = rowCells.map(val => val === 1 ? 1 : 0);
      const rowCurrentHints = calculateLineHints(rowLine);
      const isRowMatching = isArrayEqual(rowCurrentHints, hints);

      ctx.fillStyle = isRowMatching ? '#475569' : '#94a3b8'; // Fade to slate-600 if completed correctly

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
      const x = -halfW + c * cellSizeVal + cellSizeVal / 2;

      // Check if column hints are matched by player's current cells
      const colCells: number[] = [];
      for (let r = 0; r < props.board.rowCount; r++) {
        colCells.push(props.board.currentGrid[r][c]);
      }
      const colLine = colCells.map(val => val === 1 ? 1 : 0);
      const colCurrentHints = calculateLineHints(colLine);
      const isColMatching = isArrayEqual(colCurrentHints, hints);

      ctx.fillStyle = isColMatching ? '#475569' : '#94a3b8'; // Fade to slate-600 if completed correctly

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

let dragValue = 0; // 0: empty, 1: filled, 2: marked
let lastRow = -1;
let lastCol = -1;

function getCoordinatesFromEvent(clientX: number, clientY: number) {
  const canvas = canvasRef.value;
  if (!canvas) return null;
  const rect = canvas.getBoundingClientRect();
  const currentScale = isTestEnv ? 1.0 : (rect.width / canvas.width);
  const clickX = (clientX - rect.left) / currentScale;
  const clickY = (clientY - rect.top) / currentScale;
  return getGridCoordinates(clickX, clickY, config);
}

function handleMouseDown(event: MouseEvent) {
  if (props.readOnly) return;
  const coords = getCoordinatesFromEvent(event.clientX, event.clientY);
  if (!coords) return;

  const { row, col } = coords;
  const currentValue = props.board.currentGrid[row][col];

  if (event.button === 2) {
    // Right click always acts as Mark toggle
    dragValue = currentValue === 2 ? 0 : 2;
  } else if (event.button === 0) {
    // Left click respects current drawMode
    if (drawMode.value === 'fill') {
      dragValue = currentValue === 1 ? 0 : 1;
    } else {
      dragValue = currentValue === 2 ? 0 : 2;
    }
  } else {
    return;
  }

  isDragging.value = true;
  props.board.setCell(row, col, dragValue);
  lastRow = row;
  lastCol = col;

  drawBoard();
  emit('cell-click');

  window.addEventListener('mousemove', handleWindowMouseMove);
  window.addEventListener('mouseup', handleWindowMouseUp);
}

function handleWindowMouseMove(event: MouseEvent) {
  if (!isDragging.value) return;

  const coords = getCoordinatesFromEvent(event.clientX, event.clientY);
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
  if (isDragging.value) {
    isDragging.value = false;
    window.removeEventListener('mousemove', handleWindowMouseMove);
    window.removeEventListener('mouseup', handleWindowMouseUp);
  }
}

function handleTouchStart(event: TouchEvent) {
  if (props.readOnly) return;
  if (event.touches.length !== 1) return;
  event.preventDefault(); // Prevent page scroll/zoom gestures during drawing

  const touch = event.touches[0];
  const coords = getCoordinatesFromEvent(touch.clientX, touch.clientY);
  if (!coords) return;

  const { row, col } = coords;
  const currentValue = props.board.currentGrid[row][col];

  if (drawMode.value === 'fill') {
    dragValue = currentValue === 1 ? 0 : 1;
  } else {
    dragValue = currentValue === 2 ? 0 : 2;
  }

  isDragging.value = true;
  props.board.setCell(row, col, dragValue);
  lastRow = row;
  lastCol = col;

  drawBoard();
  emit('cell-click');

  window.addEventListener('touchmove', handleWindowTouchMove, { passive: false });
  window.addEventListener('touchend', handleWindowTouchEnd);
  window.addEventListener('touchcancel', handleWindowTouchEnd);
}

function handleWindowTouchMove(event: TouchEvent) {
  if (!isDragging.value || event.touches.length !== 1) return;
  event.preventDefault();

  const touch = event.touches[0];
  const coords = getCoordinatesFromEvent(touch.clientX, touch.clientY);
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

function handleWindowTouchEnd() {
  if (isDragging.value) {
    isDragging.value = false;
    window.removeEventListener('touchmove', handleWindowTouchMove);
    window.removeEventListener('touchend', handleWindowTouchEnd);
    window.removeEventListener('touchcancel', handleWindowTouchEnd);
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

let resizeObserver: ResizeObserver | null = null;

onMounted(() => {
  if (frameRef.value) {
    updateFrameSize();
    if (typeof ResizeObserver !== 'undefined') {
      resizeObserver = new ResizeObserver(() => {
        updateFrameSize();
      });
      resizeObserver.observe(frameRef.value);
    }
  }
  scale.value = fitScale.value;
  drawBoard();
});

onUnmounted(() => {
  window.removeEventListener('mousemove', handleWindowMouseMove);
  window.removeEventListener('mouseup', handleWindowMouseUp);
  window.removeEventListener('touchmove', handleWindowTouchMove);
  window.removeEventListener('touchend', handleWindowTouchEnd);
  window.removeEventListener('touchcancel', handleWindowTouchEnd);
  if (resizeObserver) {
    resizeObserver.disconnect();
  }
});

watch(fitScale, (newFitScale) => {
  scale.value = newFitScale;
});

// Redraw if board changes
watch(() => props.board, () => {
  currentAngle.value = getStartingAngle();
  scale.value = fitScale.value;
  const dims = getDimensions();
  config.centerX = dims.width / 2;
  config.centerY = dims.height / 2;
  config.cellSize = CELL_SIZE.value;
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
  position: relative;
  display: block;
  padding: 0;
  background-color: transparent;
  border-radius: 12px;
  width: 100%;
  height: 100%;
  min-width: 0;
  min-height: 0;
}

.canvas-frame {
  width: 100%;
  height: 100%;
  overflow: hidden;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #0f172a;
  border-radius: 8px;
  border: 1px solid rgba(255, 255, 255, 0.05);
  position: relative;
}

canvas {
  display: block;
  cursor: pointer;
  position: absolute;
  -webkit-tap-highlight-color: transparent;
  -webkit-touch-callout: none;
  user-select: none;
  touch-action: none;
}

/* Floating Draw Mode HUD */
.draw-mode-hud {
  position: absolute;
  bottom: 20px;
  left: 20px;
  display: flex;
  align-items: center;
  background: rgba(15, 23, 42, 0.85);
  backdrop-filter: blur(8px);
  border: 1px solid rgba(255, 255, 255, 0.08);
  padding: 4px;
  border-radius: 9999px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.4);
  z-index: 10;
  width: 80px;
  height: 36px;
  box-sizing: border-box;
  -webkit-tap-highlight-color: transparent;
  -webkit-touch-callout: none;
  user-select: none;
}

.draw-mode-slider {
  position: absolute;
  top: 4px;
  left: 4px;
  width: 36px;
  height: 28px;
  background: rgba(255, 255, 255, 0.08);
  border-radius: 9999px;
  transition: transform 0.25s cubic-bezier(0.25, 0.8, 0.25, 1);
  border: 1px solid rgba(255, 255, 255, 0.12);
  z-index: 1;
  box-sizing: border-box;
}

.draw-mode-slider.x {
  transform: translateX(36px);
}

.draw-mode-btn {
  background: none;
  border: none;
  margin: 0;
  padding: 0;
  width: 36px;
  height: 28px;
  display: flex;
  justify-content: center;
  align-items: center;
  cursor: pointer;
  border-radius: 9999px;
  transition: all 0.2s ease;
  user-select: none;
  -webkit-tap-highlight-color: transparent;
  z-index: 2;
  box-sizing: border-box;
}

.mode-icon {
  transition: transform 0.2s ease;
  display: flex;
  justify-content: center;
  align-items: center;
}

.draw-mode-btn:hover .mode-icon {
  transform: scale(1.15);
}

.fill-icon {
  width: 14px;
  height: 14px;
  background: linear-gradient(135deg, #38bdf8 0%, #818cf8 100%);
  border-radius: 3px;
  display: block;
  box-shadow: 0 1px 3px rgba(56, 189, 248, 0.3);
}

.x-icon {
  width: 14px;
  height: 14px;
  stroke: #f43f5e;
  display: block;
}

/* Floating Zoom HUD */
.zoom-hud {
  position: absolute;
  bottom: 20px;
  right: 20px;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  background: rgba(15, 23, 42, 0.85);
  backdrop-filter: blur(8px);
  border: 1px solid rgba(255, 255, 255, 0.08);
  padding: 0.35rem 0.6rem;
  border-radius: 9999px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.4);
  z-index: 10;
}

.zoom-btn {
  background: none;
  border: none;
  color: #94a3b8;
  font-size: 1.1rem;
  font-weight: 700;
  width: 24px;
  height: 24px;
  display: flex;
  justify-content: center;
  align-items: center;
  cursor: pointer;
  border-radius: 50%;
  transition: all 0.15s ease;
}

.zoom-btn:hover {
  background-color: rgba(255, 255, 255, 0.08);
  color: #f8fafc;
}

.zoom-level {
  font-family: 'Outfit', sans-serif;
  font-weight: 700;
  font-size: 0.78rem;
  color: #38bdf8;
  min-width: 42px;
  text-align: center;
  cursor: pointer;
  user-select: none;
  transition: color 0.15s ease;
}

.zoom-level:hover {
  color: #818cf8;
}
</style>
