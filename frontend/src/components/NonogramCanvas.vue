<template>
  <div class="nonogram-canvas-container">
    <canvas ref="canvasRef" data-testid="nonogram-canvas" @mousedown="handleMouseDown"></canvas>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { PuzzleBoard } from '../engine/puzzleBoard';

defineProps<{
  board: PuzzleBoard;
}>();

const canvasRef = ref<HTMLCanvasElement | null>(null);

function handleMouseDown(_event: MouseEvent) {
  // Read canvasRef to prevent unused variable warnings in skeleton
  if (canvasRef.value) {
    // Stub
  }
}
</script>

<style scoped>
.nonogram-canvas-container {
  display: inline-block;
}
canvas {
  display: block;
}
</style>
