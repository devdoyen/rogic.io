<template>
  <div class="app-container">
    <header class="app-header">
      <h1 class="app-title">Nemologic Puzzle Game</h1>
      <p class="app-subtitle">TDD Core Engine & HTML5 Canvas Rendering Demo</p>
    </header>

    <main class="app-main">
      <div class="game-instructions">
        <h3>How to Play</h3>
        <ul>
          <li><span class="key-indicator left-click">Left Click</span> Fill Cell</li>
          <li><span class="key-indicator right-click">Right Click</span> Mark X</li>
        </ul>
      </div>

      <div class="canvas-wrapper">
        <NonogramCanvas :board="board" @cell-click="handleCellClick" />
      </div>

      <div v-if="solved" class="solved-banner">
        <h2>🎉 Solved! Congratulations!</h2>
      </div>
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import NonogramCanvas from './components/NonogramCanvas.vue';
import { PuzzleBoard } from './engine/puzzleBoard';

// 5x5 heart-like shape pattern
const solution = [
  [0, 1, 0, 1, 0],
  [1, 1, 1, 1, 1],
  [1, 1, 1, 1, 1],
  [0, 1, 1, 1, 0],
  [0, 0, 1, 0, 0]
];

const board = ref(new PuzzleBoard(solution));
const solved = ref(false);

function handleCellClick() {
  solved.value = board.value.isSolved();
}
</script>

<style>
@import url('https://fonts.googleapis.com/css2?family=Outfit:wght@300;400;600;700&display=swap');

body {
  margin: 0;
  padding: 0;
  background-color: #0f172a; /* Slate 900 dark mode */
  color: #f8fafc;
  font-family: 'Outfit', sans-serif;
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
}

.app-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 2rem;
  max-width: 600px;
  width: 100%;
}

.app-header {
  text-align: center;
  margin-bottom: 2rem;
}

.app-title {
  font-size: 2.5rem;
  font-weight: 700;
  background: linear-gradient(to right, #38bdf8, #818cf8);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
  margin: 0;
}

.app-subtitle {
  color: #94a3b8;
  font-size: 1rem;
  margin-top: 0.5rem;
}

.app-main {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 1.5rem;
  width: 100%;
}

.game-instructions {
  background-color: #1e293b; /* Slate 800 */
  border: 1px solid #334155;
  border-radius: 12px;
  padding: 1rem 1.5rem;
  width: 100%;
  box-sizing: border-box;
}

.game-instructions h3 {
  margin-top: 0;
  color: #38bdf8;
  font-weight: 600;
}

.game-instructions ul {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  justify-content: space-around;
}

.key-indicator {
  padding: 0.25rem 0.5rem;
  border-radius: 4px;
  font-weight: 600;
  font-size: 0.85rem;
  margin-right: 0.5rem;
}

.left-click {
  background-color: #38bdf8;
  color: #0f172a;
}

.right-click {
  background-color: #f43f5e;
  color: #ffffff;
}

.canvas-wrapper {
  background: rgba(30, 41, 59, 0.7);
  backdrop-filter: blur(8px);
  border: 1px solid rgba(255, 255, 255, 0.1);
  padding: 1.5rem;
  border-radius: 16px;
  box-shadow: 0 10px 25px -5px rgba(0, 0, 0, 0.3);
}

.solved-banner {
  background: linear-gradient(135deg, #10b981, #059669);
  color: white;
  padding: 1rem 2rem;
  border-radius: 12px;
  text-align: center;
  box-shadow: 0 4px 15px rgba(16, 185, 129, 0.4);
  animation: pop-in 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}

@keyframes pop-in {
  0% {
    transform: scale(0.8);
    opacity: 0;
  }
  100% {
    transform: scale(1);
    opacity: 1;
  }
}
</style>
