<template>
  <div class="app-container">
    <header class="app-header">
      <h1 class="app-title">Nemologic Puzzle Game</h1>
      <p class="app-subtitle">TDD Core Engine & HTML5 Canvas Rendering Demo</p>
    </header>

    <nav class="app-nav" style="margin-bottom: 1.5rem; display: flex; gap: 1rem;">
      <button 
        class="tab-btn-play" 
        :class="{ active: currentTab === 'play' }" 
        @click="currentTab = 'play'"
      >Game Play</button>
      <button 
        class="tab-btn-mypage" 
        :class="{ active: currentTab === 'mypage' }" 
        @click="currentTab = 'mypage'"
      >My Page</button>
    </nav>

    <div class="app-layout">
      <main v-if="currentTab === 'play'" class="app-main">

        <!-- Stage Selector Section -->
        <div class="stage-selector-card">
          <label for="stage-select" class="selector-label">Select Stage:</label>
          <select 
            id="stage-select" 
            v-model="selectedStageId" 
            @change="onStageChange"
            class="selector-select"
          >
            <option v-for="stage in stages" :key="stage.id" :value="stage.id">
              {{ stage.name }} ({{ stage.width }}x{{ stage.height }})
            </option>
          </select>
        </div>

        <div class="game-instructions">
          <h3>How to Play</h3>
          <ul>
            <li><span class="key-indicator left-click">Left Click</span> Fill Cell</li>
            <li><span class="key-indicator right-click">Right Click</span> Mark X</li>
          </ul>
        </div>

        <!-- Canvas Area: only render if board is initialized -->
        <div v-if="board" class="canvas-wrapper">
          <NonogramCanvas :board="board" @cell-click="handleCellClick" />
        </div>
        <div v-else class="loading-state">
          <p>Loading board data...</p>
        </div>

        <div v-if="solved" class="solved-banner">
          <h2>🎉 Solved! Congratulations!</h2>
        </div>
      </main>

      <!-- My Page View -->
      <main v-else-if="currentTab === 'mypage'" class="app-main mypage-view">
        <div class="history-card" style="background-color: #1e293b; border: 1px solid #334155; border-radius: 16px; padding: 1.5rem; width: 100%; box-sizing: border-box;">
          <h3 style="margin-top: 0; color: #38bdf8;">My Puzzle Clear History</h3>
          <div v-if="histories.length > 0" class="history-list" style="display: flex; flex-direction: column; gap: 0.75rem;">
            <div v-for="item in histories" :key="item.id" class="history-item" style="display: flex; justify-content: space-between; padding: 0.5rem 0.75rem; background-color: #0f172a; border: 1px solid #1e293b; border-radius: 8px;">
              <span class="stage-name" style="font-weight: 600;">{{ item.stageName }}</span>
              <span class="elapsed-time" style="color: #64748b;">{{ item.elapsedTime }}s</span>
              <span class="xp-earned" style="color: #10b981;">+{{ item.xpEarned }} XP</span>
              <span class="cleared-at" style="color: #818cf8; font-size: 0.85rem;">{{ item.clearedAt }}</span>
            </div>
          </div>
          <div v-else class="empty-history" style="text-align: center; color: #94a3b8; padding: 2rem;">
            <p>No history records found.</p>
          </div>
        </div>
      </main>


      <!-- Sidebar Area (Leaderboard) -->
      <aside class="app-sidebar">
        <div class="leaderboard-card">
          <h3 class="leaderboard-title">🏆 Global Leaderboard</h3>
          <ul class="leaderboard-list">
            <li v-for="(user, index) in rankings" :key="user.id" class="leaderboard-item">
              <span class="rank">{{ index + 1 }}</span>
              <span class="username">{{ user.username }}</span>
              <span class="level">Lv.{{ user.level }}</span>
              <span class="xp">{{ user.xp }} XP</span>
            </li>
          </ul>
        </div>
      </aside>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import NonogramCanvas from './components/NonogramCanvas.vue';
import { PuzzleBoard } from './engine/puzzleBoard';
import { fetchStages, fetchStageById } from './api/stageApi';
import type { StageSummary } from './api/stageApi';
import { fetchRanking, clearStage, registerAnonymousUser, fetchUserHistory } from './api/userApi';
import type { User } from './api/userApi';
import { hasUserSession, getUserSession, setUserSession } from './api/auth';
import type { UserSession } from './api/auth';


const stages = ref<StageSummary[]>([]);
const selectedStageId = ref<number | null>(null);
const board = ref<PuzzleBoard | null>(null);
const solved = ref(false);
const rankings = ref<User[]>([]);
const currentUser = ref<UserSession | null>(null);
const currentTab = ref<'play' | 'mypage'>('play');
const histories = ref<any[]>([]);
const startTime = ref<number>(Date.now());


async function loadStagesList() {
  try {
    const list = await fetchStages();
    stages.value = list;
    if (list.length > 0) {
      selectedStageId.value = list[0].id;
      await loadStageDetails(list[0].id);
    }
  } catch (error) {
    console.error('Failed to load stages:', error);
  }
}

async function loadStageDetails(id: number) {
  try {
    const details = await fetchStageById(id);
    board.value = new PuzzleBoard(details.solutionGrid);
    solved.value = false;
  } catch (error) {
    console.error(`Failed to load stage details for ID ${id}:`, error);
  }
}

async function loadRankingsList() {
  try {
    const list = await fetchRanking();
    rankings.value = list;
  } catch (error) {
    console.error('Failed to load rankings:', error);
  }
}

async function onStageChange() {
  if (selectedStageId.value !== null) {
    await loadStageDetails(selectedStageId.value);
  }
}

async function handleCellClick() {
  if (board.value) {
    const wasSolved = solved.value;
    solved.value = board.value.isSolved();

    if (solved.value && !wasSolved) {
      try {
        let difficulty = 'NORMAL';
        if (board.value.colCount <= 5 && board.value.rowCount <= 5) {
          difficulty = 'EASY';
        } else if (board.value.colCount >= 10 || board.value.rowCount >= 10) {
          difficulty = 'HARD';
        }
        const userId = currentUser.value ? currentUser.value.id : 1;
        await clearStage(userId, difficulty);
        await loadRankingsList();
      } catch (error) {
        console.error('Failed to submit stage clear:', error);
      }
    }
  }
}

async function initializeUserSession() {
  if (hasUserSession()) {
    currentUser.value = getUserSession();
  } else {
    try {
      const registered = await registerAnonymousUser();
      const session: UserSession = {
        id: registered.id,
        uuid: registered.uuid || 'temp-uuid',
        username: registered.username,
        xp: registered.xp,
        level: registered.level
      };
      setUserSession(session);
      currentUser.value = session;
    } catch (error) {
      console.error('Failed to register anonymous user:', error);
    }
  }
}

onMounted(async () => {
  await initializeUserSession();
  await Promise.all([
    loadStagesList(),
    loadRankingsList()
  ]);
});
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
  max-width: 1000px;
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

.stage-selector-card {
  background-color: #1e293b; /* Slate 800 */
  border: 1px solid #334155;
  border-radius: 12px;
  padding: 1rem 1.5rem;
  width: 100%;
  box-sizing: border-box;
  display: flex;
  align-items: center;
  gap: 1rem;
}

.selector-label {
  font-weight: 600;
  color: #94a3b8;
}

.selector-select {
  flex-grow: 1;
  background-color: #0f172a;
  color: #f8fafc;
  border: 1px solid #334155;
  border-radius: 6px;
  padding: 0.5rem;
  font-family: 'Outfit', sans-serif;
  font-size: 1rem;
  outline: none;
  cursor: pointer;
  transition: border-color 0.2s;
}

.selector-select:focus {
  border-color: #38bdf8;
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

.loading-state {
  color: #94a3b8;
  font-size: 1.1rem;
  padding: 2rem;
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

.app-layout {
  display: flex;
  gap: 2rem;
  width: 100%;
  align-items: flex-start;
  justify-content: center;
}

@media (max-width: 768px) {
  .app-layout {
    flex-direction: column;
    align-items: center;
  }
}

.app-sidebar {
  width: 320px;
  flex-shrink: 0;
}

@media (max-width: 768px) {
  .app-sidebar {
    width: 100%;
  }
}

.leaderboard-card {
  background-color: #1e293b;
  border: 1px solid #334155;
  border-radius: 16px;
  padding: 1.5rem;
  box-shadow: 0 10px 25px -5px rgba(0, 0, 0, 0.3);
}

.leaderboard-title {
  margin-top: 0;
  margin-bottom: 1rem;
  color: #38bdf8;
  font-size: 1.25rem;
  font-weight: 700;
  border-bottom: 1px solid #334155;
  padding-bottom: 0.75rem;
}

.leaderboard-list {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.leaderboard-item {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.5rem 0.75rem;
  background-color: #0f172a;
  border: 1px solid #1e293b;
  border-radius: 8px;
  font-weight: 600;
}

.leaderboard-item .rank {
  color: #64748b;
  width: 20px;
  text-align: center;
}

.leaderboard-item:nth-child(1) .rank {
  color: #fbbf24; /* Gold */
}

.leaderboard-item:nth-child(2) .rank {
  color: #94a3b8; /* Silver */
}

.leaderboard-item:nth-child(3) .rank {
  color: #b45309; /* Bronze */
}

.leaderboard-item .username {
  flex-grow: 1;
  color: #f8fafc;
}

.leaderboard-item .level {
  color: #38bdf8;
  font-size: 0.85rem;
  background-color: rgba(56, 189, 248, 0.1);
  padding: 0.1rem 0.4rem;
  border-radius: 4px;
}

.leaderboard-item .xp {
  color: #818cf8;
  font-size: 0.85rem;
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
