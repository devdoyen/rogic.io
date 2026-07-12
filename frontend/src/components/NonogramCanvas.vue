<template>
  <div class="nonogram-canvas-container">
    <div 
      class="canvas-frame" 
      ref="frameRef"
      @mousedown="handleMouseDown"
      @touchstart="handleTouchStart"
      @wheel="handleWheel"
      @mouseenter="handleFrameMouseEnter"
      @mousemove="handleFrameMouseMove"
      @mouseleave="handleFrameMouseLeave"
      :style="{ cursor: activeCursor }"
      @contextmenu.prevent
    >
      <div class="canvas-anim-wrapper">
        <canvas 
          ref="canvasRef" 
          data-testid="nonogram-canvas" 
          :style="canvasStyle"
        ></canvas>
      </div>
    </div>

    <DrawModeHUD v-if="!readOnly" v-model="drawMode" />
    <HistoryHUD v-if="!readOnly" :can-undo="canUndo" :can-redo="canRedo" @undo="handleUndo" @redo="handleRedo" />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch, onUnmounted, computed } from 'vue';
import { PuzzleBoard } from '../engine/puzzleBoard';
import { getGridCoordinates } from '../engine/coordinateMapper';
import { drawNonogramBoard, getBoardDimensions } from '../engine/canvasRenderer';
import DrawModeHUD from './DrawModeHUD.vue';
import HistoryHUD from './HistoryHUD.vue';

const props = defineProps<{
  board: PuzzleBoard;
  readOnly?: boolean;
  initialAngle?: number;
  rotationSteps?: number;
  renderTrigger?: number;
}>();

const emit = defineEmits<{
  (e: 'cell-click'): void;
  (e: 'solve-animation-complete'): void;
}>();

const canvasRef = ref<HTMLCanvasElement | null>(null);
const drawMode = ref<'fill' | 'x'>('fill');

// Standard grid layout dimensions
const getCellSize = (maxCount: number) => {
  if (maxCount <= 10) return 30;
  if (maxCount <= 15) return 24;
  if (maxCount <= 20) return 20;
  if (maxCount <= 25) return 18;
  return 16; // 30x30 or larger
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
  return getBoardDimensions(props.board, CELL_SIZE.value);
};

const scale = ref(1.0);
const isDragging = ref(false);
const showSolveImpact = ref(false);
const glowIntensity = ref(0.0);
const glowBlur = ref(20);
let glowAnimationId: any = null;

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

const offsetX = ref(0);
const offsetY = ref(0);
const isPanning = ref(false);
let panStartX = 0;
let panStartY = 0;
let touchStartDist = 0;
let touchStartScale = 1.0;

const canvasStyle = computed(() => {
  return {
    transform: `translate(${offsetX.value}px, ${offsetY.value}px) scale(${scale.value})`,
    transformOrigin: 'center center',
    transition: 'none'
  };
});

function clampOffsets() {
  const visibleWidth = props.board.colCount * CELL_SIZE.value;
  const visibleHeight = props.board.rowCount * CELL_SIZE.value;
  const scaledWidth = visibleWidth * scale.value;
  const scaledHeight = visibleHeight * scale.value;
  
  // Keep at least 80 pixels overlap with the frame
  const minOverlap = 80;
  
  const maxOffsetX = Math.max(0, frameWidth.value / 2 + scaledWidth / 2 - minOverlap);
  const maxOffsetY = Math.max(0, frameHeight.value / 2 + scaledHeight / 2 - minOverlap);
  
  offsetX.value = Math.max(-maxOffsetX, Math.min(maxOffsetX, offsetX.value));
  offsetY.value = Math.max(-maxOffsetY, Math.min(maxOffsetY, offsetY.value));
}

function handleWheel(event: WheelEvent) {
  event.preventDefault();
  const zoomFactor = event.deltaY < 0 ? 1.05 : 0.95;
  scale.value = Math.max(0.2, Math.min(4.0, scale.value * zoomFactor));
  clampOffsets();
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

  const { width, height } = getDimensions();
  const cellSizeVal = CELL_SIZE.value;
  if (canvas.width !== width) {
    canvas.width = width;
  }
  if (canvas.height !== height) {
    canvas.height = height;
  }

  const ctx = canvas.getContext('2d');
  if (!ctx) return;

  config.centerX = width / 2;
  config.centerY = height / 2;
  config.angle = currentAngle.value;
  config.rowCount = props.board.rowCount;
  config.colCount = props.board.colCount;
  config.cellSize = cellSizeVal;

  drawNonogramBoard(ctx, props.board, config, {
    glowIntensity: glowIntensity.value,
    glowBlur: glowBlur.value,
    cellSize: cellSizeVal
  });
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

const isHoveringGrid = ref(false);
let cachedCanvasRect: DOMRect | null = null;
let lastMouseX = 0;
let lastMouseY = 0;
let isMouseInsideFrame = false;

function updateHoverState(clientX: number, clientY: number) {
  if (props.readOnly) return;
  if (isPanning.value) {
    isHoveringGrid.value = false;
    return;
  }

  if (!cachedCanvasRect && canvasRef.value) {
    cachedCanvasRect = canvasRef.value.getBoundingClientRect();
  }
  if (!cachedCanvasRect || !canvasRef.value) return;

  const rect = cachedCanvasRect;
  const currentScale = isTestEnv ? 1.0 : (rect.width / canvasRef.value.width);
  const clickX = (clientX - rect.left) / currentScale;
  const clickY = (clientY - rect.top) / currentScale;
  
  const coords = getGridCoordinates(clickX, clickY, config);
  isHoveringGrid.value = !!coords;
}

function handleFrameMouseEnter(event: MouseEvent) {
  isMouseInsideFrame = true;
  if (canvasRef.value) {
    cachedCanvasRect = canvasRef.value.getBoundingClientRect();
  }
  lastMouseX = event.clientX;
  lastMouseY = event.clientY;
  updateHoverState(lastMouseX, lastMouseY);
}

function handleFrameMouseMove(event: MouseEvent) {
  lastMouseX = event.clientX;
  lastMouseY = event.clientY;
  updateHoverState(lastMouseX, lastMouseY);
}

function handleFrameMouseLeave() {
  isHoveringGrid.value = false;
  isMouseInsideFrame = false;
  cachedCanvasRect = null;
}

const activeCursor = computed(() => {
  if (props.readOnly) return 'default';
  if (isPanning.value) return 'grabbing';
  if (isHoveringGrid.value) {
    if (drawMode.value === 'fill') {
      return `url("data:image/svg+xml;utf8,<svg xmlns='http://www.w3.org/2000/svg' width='24' height='24' viewBox='0 0 24 24'><rect x='2' y='2' width='20' height='20' rx='4' fill='%2338bdf8' stroke='%236366f1' stroke-width='2.5'/></svg>") 12 12, auto`;
    } else {
      return `url("data:image/svg+xml;utf8,<svg xmlns='http://www.w3.org/2000/svg' width='24' height='24' viewBox='0 0 24 24'><line x1='4' y1='4' x2='20' y2='20' stroke='%23f43f5e' stroke-width='3.5' stroke-linecap='round'/><line x1='20' y1='4' x2='4' y2='20' stroke='%23f43f5e' stroke-width='3.5' stroke-linecap='round'/></svg>") 12 12, auto`;
    }
  }
  return 'grab';
});

watch([scale, offsetX, offsetY, currentAngle], () => {
  if (canvasRef.value) {
    cachedCanvasRect = canvasRef.value.getBoundingClientRect();
    if (isMouseInsideFrame) {
      updateHoverState(lastMouseX, lastMouseY);
    }
  }
});

const canUndo = ref(false);
const canRedo = ref(false);

function updateHistoryFlags() {
  canUndo.value = props.board.canUndo();
  canRedo.value = props.board.canRedo();
}

function handleUndo() {
  if (props.readOnly) return;
  const success = props.board.undo();
  if (success) {
    updateHistoryFlags();
    drawBoard();
    emit('cell-click');
  }
}

function handleRedo() {
  if (props.readOnly) return;
  const success = props.board.redo();
  if (success) {
    updateHistoryFlags();
    drawBoard();
    emit('cell-click');
  }
}

function handleKeyDown(event: KeyboardEvent) {
  if (props.readOnly) return;
  if ((event.ctrlKey || event.metaKey) && event.key.toLowerCase() === 'z') {
    event.preventDefault();
    if (event.shiftKey) {
      handleRedo();
    } else {
      handleUndo();
    }
  } else if ((event.ctrlKey || event.metaKey) && event.key.toLowerCase() === 'y') {
    event.preventDefault();
    handleRedo();
  }
}

function handlePanMouseMove(event: MouseEvent) {
  if (!isPanning.value) return;
  offsetX.value = event.clientX - panStartX;
  offsetY.value = event.clientY - panStartY;
  clampOffsets();
}

function handlePanMouseUp() {
  if (isPanning.value) {
    isPanning.value = false;
    window.removeEventListener('mousemove', handlePanMouseMove);
    window.removeEventListener('mouseup', handlePanMouseUp);
  }
}

function handlePanTouchMove(event: TouchEvent) {
  if (!isPanning.value) return;
  event.preventDefault();

  if (event.touches.length === 1) {
    const touch = event.touches[0];
    offsetX.value = touch.clientX - panStartX;
    offsetY.value = touch.clientY - panStartY;
  } else if (event.touches.length > 1) {
    const t1 = event.touches[0];
    const t2 = event.touches[1];
    const midX = (t1.clientX + t2.clientX) / 2;
    const midY = (t1.clientY + t2.clientY) / 2;
    offsetX.value = midX - panStartX;
    offsetY.value = midY - panStartY;

    if (touchStartDist > 0) {
      const currentDist = Math.hypot(t1.clientX - t2.clientX, t1.clientY - t2.clientY);
      const scaleFactor = currentDist / touchStartDist;
      scale.value = Math.max(0.2, Math.min(4.0, touchStartScale * scaleFactor));
    }
  }
  clampOffsets();
}

function handlePanTouchEnd() {
  if (isPanning.value) {
    isPanning.value = false;
    touchStartDist = 0;
    window.removeEventListener('touchmove', handlePanTouchMove);
    window.removeEventListener('touchend', handlePanTouchEnd);
    window.removeEventListener('touchcancel', handlePanTouchEnd);
  }
}

function handleMouseDown(event: MouseEvent) {
  if (props.readOnly) return;

  // Handle middle click panning
  if (event.button === 1) {
    isPanning.value = true;
    panStartX = event.clientX - offsetX.value;
    panStartY = event.clientY - offsetY.value;
    window.addEventListener('mousemove', handlePanMouseMove);
    window.addEventListener('mouseup', handlePanMouseUp);
    return;
  }

  const coords = getCoordinatesFromEvent(event.clientX, event.clientY);
  
  // If clicked outside the grid (coords is null) and it is left click, start panning!
  if (!coords) {
    if (event.button === 0) {
      isPanning.value = true;
      panStartX = event.clientX - offsetX.value;
      panStartY = event.clientY - offsetY.value;
      window.addEventListener('mousemove', handlePanMouseMove);
      window.addEventListener('mouseup', handlePanMouseUp);
    }
    return;
  }

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

  // Save undo state before changing cell
  props.board.saveState();
  updateHistoryFlags();

  isDragging.value = true;
  props.board.setCell(row, col, dragValue);
  lastRow = row;
  lastCol = col;

  drawBoard();
  emit('cell-click');

  if (props.board.isSolved()) {
    isDragging.value = false;
    return;
  }

  window.addEventListener('mousemove', handleWindowMouseMove);
  window.addEventListener('mouseup', handleWindowMouseUp);
}

function handleWindowMouseMove(event: MouseEvent) {
  if (!isDragging.value) return;
  if (props.board.isSolved()) {
    isDragging.value = false;
    window.removeEventListener('mousemove', handleWindowMouseMove);
    window.removeEventListener('mouseup', handleWindowMouseUp);
    return;
  }

  const coords = getCoordinatesFromEvent(event.clientX, event.clientY);
  if (!coords) return;

  const { row, col } = coords;
  if (row !== lastRow || col !== lastCol) {
    props.board.setCell(row, col, dragValue);
    lastRow = row;
    lastCol = col;
    drawBoard();
    emit('cell-click');

    if (props.board.isSolved()) {
      isDragging.value = false;
      window.removeEventListener('mousemove', handleWindowMouseMove);
      window.removeEventListener('mouseup', handleWindowMouseUp);
    }
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

  // Handle multi-touch panning
  if (event.touches.length > 1) {
    isPanning.value = true;
    const t1 = event.touches[0];
    const t2 = event.touches[1];
    const midX = (t1.clientX + t2.clientX) / 2;
    const midY = (t1.clientY + t2.clientY) / 2;
    panStartX = midX - offsetX.value;
    panStartY = midY - offsetY.value;

    const dist = Math.hypot(t1.clientX - t2.clientX, t1.clientY - t2.clientY);
    touchStartDist = dist;
    touchStartScale = scale.value;

    window.addEventListener('touchmove', handlePanTouchMove, { passive: false });
    window.addEventListener('touchend', handlePanTouchEnd);
    window.addEventListener('touchcancel', handlePanTouchEnd);
    return;
  }

  if (event.touches.length !== 1) return;
  const touch = event.touches[0];
  const coords = getCoordinatesFromEvent(touch.clientX, touch.clientY);

  // If touched outside the grid, start panning!
  if (!coords) {
    isPanning.value = true;
    panStartX = touch.clientX - offsetX.value;
    panStartY = touch.clientY - offsetY.value;
    window.addEventListener('touchmove', handlePanTouchMove, { passive: false });
    window.addEventListener('touchend', handlePanTouchEnd);
    window.addEventListener('touchcancel', handlePanTouchEnd);
    return;
  }

  event.preventDefault(); // Prevent page scroll/zoom gestures during drawing

  const { row, col } = coords;
  const currentValue = props.board.currentGrid[row][col];

  if (drawMode.value === 'fill') {
    dragValue = currentValue === 1 ? 0 : 1;
  } else {
    dragValue = currentValue === 2 ? 0 : 2;
  }

  // Save undo state before changing cell
  props.board.saveState();
  updateHistoryFlags();

  isDragging.value = true;
  props.board.setCell(row, col, dragValue);
  lastRow = row;
  lastCol = col;

  drawBoard();
  emit('cell-click');

  if (props.board.isSolved()) {
    isDragging.value = false;
    return;
  }

  window.addEventListener('touchmove', handleWindowTouchMove, { passive: false });
  window.addEventListener('touchend', handleWindowTouchEnd);
  window.addEventListener('touchcancel', handleWindowTouchEnd);
}

function handleWindowTouchMove(event: TouchEvent) {
  if (!isDragging.value || event.touches.length !== 1) return;
  if (props.board.isSolved()) {
    isDragging.value = false;
    window.removeEventListener('touchmove', handleWindowTouchMove);
    window.removeEventListener('touchend', handleWindowTouchEnd);
    window.removeEventListener('touchcancel', handleWindowTouchEnd);
    return;
  }
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

    if (props.board.isSolved()) {
      isDragging.value = false;
      window.removeEventListener('touchmove', handleWindowTouchMove);
      window.removeEventListener('touchend', handleWindowTouchEnd);
      window.removeEventListener('touchcancel', handleWindowTouchEnd);
    }
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

function startSolvedGlowAnimation() {
  stopGlowAnimation();
  const startTime = performance.now();

  function tick(now: number) {
    if (!props.board.isSolved()) {
      stopGlowAnimation();
      return;
    }

    const elapsed = now - startTime;
    if (elapsed < 200) {
      // 0 to 200ms: shoot up to 1.0 (flash)
      const progress = elapsed / 200;
      glowIntensity.value = progress * 1.0;
      glowBlur.value = 15 + progress * 20; // 15 to 35 blur
    } else if (elapsed < 1200) {
      // 200ms to 1200ms: decay down to 0.35
      const progress = (elapsed - 200) / 1000;
      const ease = 1 - Math.pow(1 - progress, 2);
      glowIntensity.value = 1.0 - (1.0 - 0.35) * ease;
      glowBlur.value = 35 - (35 - 20) * ease;
    } else {
      // After 1200ms: gentle pulse infinitely
      const pulseElapsed = now - (startTime + 1200);
      const pulse = Math.sin(pulseElapsed / 600) * 0.08;
      glowIntensity.value = 0.35 + pulse;
      glowBlur.value = 20 + Math.sin(pulseElapsed / 600) * 4;
    }

    drawBoard();
    glowAnimationId = requestAnimationFrame(tick);
  }

  glowAnimationId = requestAnimationFrame(tick);
}

function stopGlowAnimation() {
  if (glowAnimationId !== null) {
    cancelAnimationFrame(glowAnimationId);
    glowAnimationId = null;
  }
  glowIntensity.value = 0.0;
}

function animateRotationToTarget() {
  const targetAngle = targetOrthogonalAngle.value;
  if (isTestEnv) {
    currentAngle.value = targetAngle;
    offsetX.value = 0;
    offsetY.value = 0;
    scale.value = fitScale.value;
    glowIntensity.value = 0.35;
    drawBoard();
    showSolveImpact.value = true;
    emit('solve-animation-complete');
    return;
  }

  const duration = 1000; // 1 second
  const startAngle = currentAngle.value;
  const startOffsetX = offsetX.value;
  const startOffsetY = offsetY.value;
  const startScale = scale.value;
  const targetScale = fitScale.value;
  const startTime = performance.now();

  function tick(now: number) {
    const elapsed = now - startTime;
    const progress = Math.min(elapsed / duration, 1);

    // Easing: easeInOutCubic
    const ease = progress < 0.5 
      ? 4 * progress * progress * progress 
      : 1 - Math.pow(-2 * progress + 2, 3) / 2;

    currentAngle.value = startAngle + (targetAngle - startAngle) * ease;
    offsetX.value = startOffsetX + (0 - startOffsetX) * ease;
    offsetY.value = startOffsetY + (0 - startOffsetY) * ease;
    scale.value = startScale + (targetScale - startScale) * ease;
    drawBoard();

    if (progress < 1) {
      requestAnimationFrame(tick);
    } else {
      showSolveImpact.value = true;
      startSolvedGlowAnimation();
      emit('solve-animation-complete');
    }
  }

  requestAnimationFrame(tick);
}

let resizeObserver: ResizeObserver | null = null;

onMounted(() => {
  window.addEventListener('keydown', handleKeyDown);
  if (frameRef.value) {
    updateFrameSize();
    if (typeof ResizeObserver !== 'undefined') {
      resizeObserver = new ResizeObserver(() => {
        updateFrameSize();
        cachedCanvasRect = null;
      });
      resizeObserver.observe(frameRef.value);
    }
  }
  scale.value = fitScale.value;
  drawBoard();
});

onUnmounted(() => {
  stopGlowAnimation();
  window.removeEventListener('keydown', handleKeyDown);
  window.removeEventListener('mousemove', handleWindowMouseMove);
  window.removeEventListener('mouseup', handleWindowMouseUp);
  window.removeEventListener('touchmove', handleWindowTouchMove);
  window.removeEventListener('touchend', handleWindowTouchEnd);
  window.removeEventListener('touchcancel', handleWindowTouchEnd);
  window.removeEventListener('mousemove', handlePanMouseMove);
  window.removeEventListener('mouseup', handlePanMouseUp);
  window.removeEventListener('touchmove', handlePanTouchMove);
  window.removeEventListener('touchend', handlePanTouchEnd);
  window.removeEventListener('touchcancel', handlePanTouchEnd);
  if (resizeObserver) {
    resizeObserver.disconnect();
  }
});

watch(fitScale, (newFitScale) => {
  scale.value = newFitScale;
});

// Redraw if board changes
watch(() => props.board, (newBoard) => {
  stopGlowAnimation();
  showSolveImpact.value = false;
  currentAngle.value = getStartingAngle();
  scale.value = fitScale.value;
  offsetX.value = 0;
  offsetY.value = 0;
  if (newBoard) {
    newBoard.resetHistory();
    updateHistoryFlags();
  }
  const dims = getDimensions();
  config.centerX = dims.width / 2;
  config.centerY = dims.height / 2;
  config.cellSize = CELL_SIZE.value;
  config.rowCount = props.board.rowCount;
  config.colCount = props.board.colCount;
  config.angle = currentAngle.value;
  drawBoard();
}, { deep: false, immediate: true });

// Invalidate cached canvas bounding rect whenever zoom, pan, or rotation changes
watch([scale, offsetX, offsetY, currentAngle], () => {
  cachedCanvasRect = null;
  if (isMouseInsideFrame) {
    updateHoverState(lastMouseX, lastMouseY);
  }
});

// Watch for readOnly (which is bound to parent's solved state) to trigger solve animation
watch(() => props.readOnly, (isReadOnly) => {
  if (isReadOnly) {
    animateRotationToTarget();
  }
});

watch(() => props.renderTrigger, () => {
  drawBoard();
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
  border-radius: 0 0 8px 8px;
  border: 1px solid rgba(255, 255, 255, 0.05);
  border-top: none;
  position: relative;
  cursor: grab;
}

canvas {
  display: block;
  cursor: inherit;
  position: absolute;
  -webkit-tap-highlight-color: transparent;
  -webkit-touch-callout: none;
  user-select: none;
  touch-action: none;
}

.canvas-anim-wrapper {
  position: relative;
  width: 100%;
  height: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
  border-radius: 8px;
  overflow: hidden;
}

</style>
