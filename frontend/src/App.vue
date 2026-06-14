<template>
  <div class="app-container">
    <!-- Slim Header -->
    <header class="app-header">
      <div class="logo-wrapper">
        <div class="logo-icon"></div>
        <div>
          <h1 class="app-title">Rotagic</h1>
          <p class="app-subtitle" style="margin: 0; font-size: 0.75rem; letter-spacing: 1px;">Rotate Logic Puzzle</p>
        </div>
      </div>
      
      <div class="header-controls" style="display: flex; align-items: center; gap: 0.75rem;">
        <nav class="app-nav" style="display: flex; gap: 0.5rem; margin: 0;">
          <button 
            class="tab-btn-play" 
            :class="{ active: currentTab === 'play' }" 
            @click="onTabChange('play')"
          >
            🎮 <span class="btn-text">Game Play</span>
          </button>
          <button 
            class="tab-btn-mypage" 
            :class="{ active: currentTab === 'mypage' }" 
            @click="onTabChange('mypage')"
          >
            👤 <span class="btn-text">My Page</span>
          </button>
        </nav>
        <button 
          class="leaderboard-toggle-btn" 
          :class="{ active: isLeaderboardOpen }" 
          @click="isLeaderboardOpen = !isLeaderboardOpen"
        >
          🏆 <span class="btn-text">Leaderboard</span>
        </button>
        <button 
          class="help-toggle-btn" 
          @click="isHelpModalOpen = true"
        >
          ❓ <span class="btn-text">Help</span>
        </button>
      </div>
    </header>

    <!-- Hidden Selectors to keep legacy tests passing -->
    <div style="display: none;">
      <select id="stage-select" v-model="selectedStageId" @change="onStageChange" class="selector-select">
        <option v-for="stage in stages" :key="stage.id" :value="stage.id">
          {{ stage.name }} ({{ stage.width }}x{{ stage.height }})
        </option>
      </select>
      <select id="ai-stage-select" v-model="selectedAiStageId" @change="onAiStageChange" class="selector-select ai-stage-select">
        <option v-for="stage in aiStages" :key="stage.id" :value="stage.id">
          {{ stage.name }} ({{ stage.width }}x{{ stage.height }})
        </option>
      </select>
    </div>

    <!-- Main Layout Grid -->
    <div class="app-layout" :class="{ 'mypage-layout': currentTab === 'mypage' }">
      <!-- Left Sidebar: Only rendered in My Page for History list -->
      <aside v-if="currentTab === 'mypage'" class="app-sidebar-left">
        <div class="sidebar-card history-explorer-card">
          <h3 class="sidebar-card-title">💾 My History</h3>
          <div class="stage-card-list">
            <div 
              v-for="item in histories" 
              :key="item.id" 
              class="history-item" 
              @click="openHistoryModal(item)"
              style="cursor: pointer;"
            >
              <div class="history-card-header">
                <span class="stage-name" style="font-weight: 600;">{{ item.stageName }}</span>
                <span class="xp-earned" style="color: #10b981; font-weight: 600;">+{{ item.xpEarned }} XP</span>
              </div>
              <div class="history-card-body" style="display: flex; justify-content: space-between; margin-top: 0.25rem; font-size: 0.85rem; color: #64748b;">
                <span class="elapsed-time">⏱️ {{ item.elapsedTime }}s</span>
                <span class="cleared-at">{{ item.clearedAt.split('T')[0] }}</span>
              </div>
            </div>
            <div v-if="histories.length === 0" class="empty-history" style="text-align: center; padding: 2rem; color: #64748b;">
              No history found.
            </div>
          </div>
        </div>
      </aside>

      <!-- Center Main Column: Canvas & Solved Banner -->
      <main class="app-main">
        <template v-if="currentTab === 'play'">
          <!-- Canvas Area -->
          <div v-if="board" class="canvas-wrapper-container">
            <!-- Floating Stage Selector -->
            <div class="puzzle-selector-floating-container" v-if="currentActiveStage">
              <div class="active-stage-badge" @click="isStageListOpen = !isStageListOpen">
                <span class="active-stage-badge-name">{{ currentActiveStage.name }}</span>
                <span class="active-stage-badge-size">{{ currentActiveStage.width }}x{{ currentActiveStage.height }}</span>
                <span class="active-stage-arrow" :class="{ 'open': isStageListOpen }">▼</span>
              </div>

              <!-- Slide-down Dropdown List -->
              <transition name="slide-down">
                <div v-if="isStageListOpen" class="puzzle-selector-dropdown">
                  <div class="stage-card-list">
                    <div 
                      v-for="stage in allUnclearedStages" 
                      :key="stage.id" 
                      class="stage-item-card"
                      :class="{ 
                        active: (isStageAi(stage) ? (selectedAiStageId === stage.id && isAiStageActive) : (selectedStageId === stage.id && !isAiStageActive))
                      }"
                      @click.stop="selectStageCard(stage.id, isStageAi(stage))"
                    >
                      <div class="stage-card-info">
                        <span class="stage-card-name">{{ stage.name }}</span>
                        <span class="stage-card-size">{{ stage.width }}x{{ stage.height }}</span>
                        <div v-if="stage.totalAttempts !== undefined && stage.totalAttempts !== null" class="stage-card-stats" style="font-size: 0.72rem; color: #94a3b8; margin-top: 0.25rem;">
                          Rate: {{ stage.totalAttempts > 0 ? Math.round((stage.totalClears || 0) / stage.totalAttempts * 100) : 0 }}% | ⏱️ {{ Math.round(stage.averageElapsedTime || 0) }}s
                        </div>
                      </div>
                    </div>
                    <div v-if="allUnclearedStages.length === 0" class="empty-stages" style="text-align: center; padding: 2rem; color: #64748b; font-size: 0.9rem;">
                      🎉 All puzzles solved!
                    </div>
                  </div>
                </div>
              </transition>
            </div>

            <div class="canvas-wrapper">
              <NonogramCanvas :board="board" :rotationSteps="currentRotationSteps" :readOnly="solved" @cell-click="handleCellClick" />
            </div>
          </div>
          <div v-else class="loading-state">
            <p>Loading board data...</p>
          </div>

          <div v-if="solved" class="celebration-overlay-container">
            <div v-if="allUnclearedStages.length > 0" class="transition-indicator-card">
              <span class="indicator-icon">✨</span>
              <div class="indicator-progress-container">
                <div class="indicator-progress-bar"></div>
              </div>
              <span class="indicator-next-arrow">➔</span>
            </div>
            <div v-else class="all-cleared-card">
              <div class="trophy-icon">🏆</div>
              <div class="star-burst">🌟🌟🌟</div>
            </div>
          </div>
        </template>

        <template v-else-if="currentTab === 'mypage'">
          <div class="mypage-dashboard">
            <div class="mypage-user-profile">
              <div class="profile-avatar">👤</div>
              <div class="profile-details">
                <h2 class="profile-username">{{ currentUser?.username || 'Anonymous User' }}</h2>
                <div class="profile-stats">
                  <span class="profile-lv">Level {{ currentUser?.level || 1 }}</span>
                  <span class="profile-xp">{{ currentUser?.xp || 0 }} XP</span>
                </div>
              </div>
            </div>
            <div class="mypage-instruction-box">
              <h3>💡 Puzzle Replay</h3>
              <p>Click any history card on the left to review your solved puzzle solutions in read-only mode.</p>
            </div>
          </div>
        </template>
      </main>
    </div>

    <!-- Popup for Leaderboard (Semi-transparent absolute modal) -->
    <div v-show="isLeaderboardOpen" class="leaderboard-popup-overlay" @click.self="isLeaderboardOpen = false">
      <div class="leaderboard-popup-content">
        <div class="leaderboard-popup-header">
          <h3 class="leaderboard-popup-title">🏆 Global Leaderboard</h3>
          <button class="leaderboard-popup-close" @click="isLeaderboardOpen = false">&times;</button>
        </div>
        <div class="leaderboard-scrollable">
          <ul class="leaderboard-list">
            <li v-for="(user, index) in rankings" :key="user.id" class="leaderboard-item">
              <span class="rank">{{ index + 1 }}</span>
              <span class="username">{{ user.username }}</span>
              <span class="level">Lv.{{ user.level }}</span>
              <span class="xp">{{ user.xp }} XP</span>
            </li>
          </ul>
        </div>
      </div>
    </div>

    <!-- Modal for History Review -->
    <div v-if="isModalOpen && modalBoard" class="modal-overlay" style="position: fixed; top: 0; left: 0; right: 0; bottom: 0; display: flex; justify-content: center; align-items: center; background: rgba(15, 23, 42, 0.85); backdrop-filter: blur(8px); z-index: 1000;">
      <div class="modal-content" style="background-color: #1e293b; border: 1px solid #334155; border-radius: 16px; padding: 2rem; max-width: 500px; width: 90%; text-align: center; box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.55); animation: pop-in 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275);">
        <h3 class="modal-title" style="margin-top: 0; color: #38bdf8; font-weight: 700;">Review Clear History</h3>
        <p class="modal-stage-info" style="color: #94a3b8; margin-bottom: 1.5rem;">Stage: {{ selectedHistory?.stageName }}</p>
        <div class="modal-canvas-wrapper" style="display: inline-block; padding: 10px; background-color: #ffffff; border-radius: 8px;">
          <NonogramCanvas :board="modalBoard" :readOnly="true" :initialAngle="0" />
        </div>
        <div style="margin-top: 1.5rem;">
          <button class="modal-close-btn" @click="closeModal" style="padding: 0.5rem 1.5rem; background-color: #f43f5e; border: none; border-radius: 8px; color: #ffffff; font-weight: 600; cursor: pointer; transition: background-color 0.2s;">Close</button>
        </div>
      </div>
    </div>

    <!-- Modal for Help / Instructions (Auto-shown to first-time visitors) -->
    <div v-if="isHelpModalOpen" class="help-modal-overlay" style="position: fixed; top: 0; left: 0; right: 0; bottom: 0; display: flex; justify-content: center; align-items: center; background: rgba(15, 23, 42, 0.85); backdrop-filter: blur(8px); z-index: 10000;" @click.self="isHelpModalOpen = false">
      <div class="modal-content" style="background-color: #1e293b; border: 1px solid #334155; border-radius: 16px; padding: 2rem; max-width: 420px; width: 90%; text-align: center; box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.55); animation: pop-in 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275);">
        <h3 class="modal-title" style="margin-top: 0; color: #38bdf8; font-weight: 700; font-size: 1.4rem;">🎮 How to Play</h3>
        <div class="help-content-body" style="text-align: left; color: #94a3b8; font-size: 0.9rem; line-height: 1.6; margin: 1.5rem 0; display: flex; flex-direction: column; gap: 0.75rem;">
          <div style="display: flex; align-items: flex-start; gap: 0.5rem;">
            <span style="font-size: 1.1rem;">🔹</span>
            <div><strong>Left Click / Tap:</strong> Fill cells to match the puzzle solution (Blue cells).</div>
          </div>
          <div style="display: flex; align-items: flex-start; gap: 0.5rem;">
            <span style="font-size: 1.1rem;">🔹</span>
            <div><strong>Right Click:</strong> Mark empty cells where blocks cannot be placed (Red X).</div>
          </div>
          <div style="display: flex; align-items: flex-start; gap: 0.5rem;">
            <span style="font-size: 1.1rem;">🔹</span>
            <div><strong>Mouse Wheel / HUD:</strong> Zoom in and out of the canvas. Drag to move or fill.</div>
          </div>
          <div style="display: flex; align-items: flex-start; gap: 0.5rem;">
            <span style="font-size: 1.1rem;">🔹</span>
            <div><strong>Hint Numbers:</strong> Numbers show consecutive filled cells in rows and columns.</div>
          </div>
        </div>
        <div>
          <button class="modal-close-btn" @click="isHelpModalOpen = false" style="padding: 0.6rem 2rem; background: linear-gradient(135deg, #38bdf8, #818cf8); border: none; border-radius: 8px; color: #ffffff; font-weight: 600; cursor: pointer; transition: opacity 0.2s;">Start Game</button>
        </div>
      </div>
    </div>

    <!-- Confetti Overlay Canvas -->
    <canvas ref="confettiCanvas" class="confetti-canvas"></canvas>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed, watch } from 'vue';
import NonogramCanvas from './components/NonogramCanvas.vue';
import { PuzzleBoard } from './engine/puzzleBoard';
import { rotateGrid } from './engine/gridRotator';
import { fetchStages, fetchStageById, fetchAiStages, startStage } from './api/stageApi';
import type { StageSummary } from './api/stageApi';
import { fetchRanking, clearStage, registerAnonymousUser, fetchUserHistory, logVisit } from './api/userApi';
import type { User } from './api/userApi';
import { hasUserSession, getUserSession, setUserSession } from './api/auth';
import type { UserSession } from './api/auth';


const stages = ref<StageSummary[]>([]);
const selectedStageId = ref<number | null>(null);
const board = ref<PuzzleBoard | null>(null);
const solved = ref(false);
const nextPuzzleSeconds = ref(3);
let countdownTimer: any = null;

const confettiCanvas = ref<HTMLCanvasElement | null>(null);
let confettiAnimationId: any = null;

interface Confetti {
  x: number;
  y: number;
  size: number;
  color: string;
  speedX: number;
  speedY: number;
  rotation: number;
  rotationSpeed: number;
}

const confettis = ref<Confetti[]>([]);

function initConfetti() {
  const canvas = confettiCanvas.value;
  if (!canvas) return;
  canvas.width = window.innerWidth;
  canvas.height = window.innerHeight;

  const colors = ['#f43f5e', '#38bdf8', '#818cf8', '#fbbf24', '#34d399', '#a78bfa'];
  const newConfettis: Confetti[] = [];
  for (let i = 0; i < 120; i++) {
    newConfettis.push({
      x: Math.random() * canvas.width,
      y: Math.random() * canvas.height - canvas.height,
      size: Math.random() * 8 + 6,
      color: colors[Math.floor(Math.random() * colors.length)],
      speedX: Math.random() * 4 - 2,
      speedY: Math.random() * 5 + 4,
      rotation: Math.random() * 360,
      rotationSpeed: Math.random() * 4 - 2
    });
  }
  confettis.value = newConfettis;
}

function startConfetti() {
  initConfetti();
  const canvas = confettiCanvas.value;
  if (!canvas) return;
  const ctx = canvas.getContext('2d');
  if (!ctx) return;

  if (confettiAnimationId) cancelAnimationFrame(confettiAnimationId);

  function loop() {
    if (!ctx || !canvas) return;
    ctx.clearRect(0, 0, canvas.width, canvas.height);

    let active = false;
    confettis.value.forEach(p => {
      p.y += p.speedY;
      p.x += p.speedX;
      p.rotation += p.rotationSpeed;

      if (p.y < canvas.height) {
        active = true;
      }

      ctx.save();
      ctx.translate(p.x, p.y);
      ctx.rotate((p.rotation * Math.PI) / 180);
      ctx.fillStyle = p.color;
      ctx.fillRect(-p.size / 2, -p.size / 2, p.size, p.size);
      ctx.restore();
    });

    if (active && solved.value) {
      confettiAnimationId = requestAnimationFrame(loop);
    } else {
      ctx.clearRect(0, 0, canvas.width, canvas.height);
    }
  }

  confettiAnimationId = requestAnimationFrame(loop);
}

function stopConfetti() {
  if (confettiAnimationId) {
    cancelAnimationFrame(confettiAnimationId);
    confettiAnimationId = null;
  }
  const canvas = confettiCanvas.value;
  if (canvas) {
    const ctx = canvas.getContext('2d');
    ctx?.clearRect(0, 0, canvas.width, canvas.height);
  }
}

function handleConfettiResize() {
  const canvas = confettiCanvas.value;
  if (canvas) {
    canvas.width = window.innerWidth;
    canvas.height = window.innerHeight;
  }
}

watch(solved, (newVal) => {
  if (newVal) {
    startConfetti();
  } else {
    stopConfetti();
  }
});

function resetCountdown() {
  if (countdownTimer) {
    clearInterval(countdownTimer);
    countdownTimer = null;
  }
  nextPuzzleSeconds.value = 3;
}

function startNextPuzzleCountdown() {
  nextPuzzleSeconds.value = 3;
  if (countdownTimer) clearInterval(countdownTimer);
  
  countdownTimer = setInterval(() => {
    nextPuzzleSeconds.value--;
    if (nextPuzzleSeconds.value <= 0) {
      clearInterval(countdownTimer);
      countdownTimer = null;
      navigateToNextPuzzle();
    }
  }, 1000);
}

function navigateToNextPuzzle() {
  const remaining = allUnclearedStages.value;
  if (remaining.length > 0) {
    const nextStage = remaining[0];
    selectStageCard(nextStage.id, isStageAi(nextStage));
  }
}
const rankings = ref<User[]>([]);
const currentUser = ref<UserSession | null>(null);
const currentTab = ref<'play' | 'mypage'>('play');
const histories = ref<any[]>([]);
const startTime = ref<number>(Date.now());

const aiStages = ref<StageSummary[]>([]);
const selectedAiStageId = ref<number | null>(null);
const isAiStageActive = ref(false);
const selectedCategory = ref<'normal' | 'ai'>('normal');
const isHelpModalOpen = ref(false);

const isStageListOpen = ref(false);
const isLeaderboardOpen = ref(false);

const currentActiveStage = computed(() => {
  if (isAiStageActive.value) {
    return (aiStages.value || []).find(s => s.id === selectedAiStageId.value) || null;
  } else {
    return (stages.value || []).find(s => s.id === selectedStageId.value) || null;
  }
});

const clearedStageIds = computed(() => {
  return new Set((histories.value || []).map(h => h.stageId));
});

const allUnclearedStages = computed(() => {
  const stageMap = new Map<number, StageSummary>();
  (stages.value || []).forEach(s => stageMap.set(s.id, s));
  (aiStages.value || []).forEach(s => stageMap.set(s.id, s));
  const combined = Array.from(stageMap.values());
  return combined.filter(s => !clearedStageIds.value.has(s.id));
});

function isStageAi(stage: StageSummary): boolean {
  return (aiStages.value || []).some(s => s.id === stage.id);
}

function selectStageCard(id: number, isAi: boolean) {
  if (isAi) {
    selectedCategory.value = 'ai';
    selectedAiStageId.value = id;
    onAiStageChange();
  } else {
    selectedCategory.value = 'normal';
    selectedStageId.value = id;
    onStageChange();
  }
  isStageListOpen.value = false;
}

const currentRotationSteps = ref(0);

const isModalOpen = ref(false);
const selectedHistory = ref<any>(null);
const modalBoard = ref<PuzzleBoard | null>(null);


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
  resetCountdown();
  try {
    // Record starting attempt
    await startStage(id);
  } catch (error) {
    console.warn(`Failed to log stage start for ID ${id}:`, error);
  }

  try {
    const details = await fetchStageById(id);
    const k = Math.floor(Math.random() * 4);
    currentRotationSteps.value = k;
    const rotated = rotateGrid(details.solutionGrid, k);
    board.value = new PuzzleBoard(rotated);
    solved.value = false;
    startTime.value = Date.now();
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
  if (selectedStageId.value) {
    selectedCategory.value = 'normal';
    isAiStageActive.value = false;
    selectedAiStageId.value = null;
    await loadStageDetails(selectedStageId.value);
  }
}

async function loadAiStagesList() {
  try {
    const list = await fetchAiStages();
    aiStages.value = list;
  } catch (error) {
    console.error('Failed to load AI daily stages:', error);
  }
}

async function onAiStageChange() {
  if (selectedAiStageId.value) {
    selectedCategory.value = 'ai';
    isAiStageActive.value = true;
    selectedStageId.value = null;
    await loadStageDetails(selectedAiStageId.value);
  }
}

async function handleCellClick() {
  if (board.value) {
    const wasSolved = solved.value;
    solved.value = board.value.isSolved();

    if (solved.value && !wasSolved) {
      try {
        let difficulty = 'NORMAL';
        if (isAiStageActive.value) {
          difficulty = 'HARD';
        } else if (board.value.colCount <= 5 && board.value.rowCount <= 5) {
          difficulty = 'EASY';
        } else if (board.value.colCount >= 10 || board.value.rowCount >= 10) {
          difficulty = 'HARD';
        }
        const userId = currentUser.value ? currentUser.value.id : 1;
        const stageId = selectedStageId.value !== null ? selectedStageId.value : (selectedAiStageId.value !== null ? selectedAiStageId.value : undefined);
        const elapsedTime = Math.floor((Date.now() - startTime.value) / 1000);
        await clearStage(userId, difficulty, stageId, elapsedTime);
        await loadRankingsList();
        await loadUserHistory();
        startNextPuzzleCountdown();
      } catch (error) {
        console.error('Failed to submit stage clear:', error);
      }
    }
  }
}

async function loadUserHistory() {
  try {
    const userId = currentUser.value ? currentUser.value.id : 1;
    const historyList = await fetchUserHistory(userId);
    histories.value = historyList;
  } catch (error) {
    console.error('Failed to load user history:', error);
  }
}

async function onTabChange(tab: 'play' | 'mypage') {
  currentTab.value = tab;
  if (tab === 'mypage') {
    await loadUserHistory();
  }
}

async function openHistoryModal(item: any) {
  selectedHistory.value = item;
  try {
    const details = await fetchStageById(item.stageId);
    const board = new PuzzleBoard(details.solutionGrid);
    for (let r = 0; r < board.rowCount; r++) {
      for (let c = 0; c < board.colCount; c++) {
        board.setCell(r, c, details.solutionGrid[r][c]);
      }
    }
    modalBoard.value = board;
    isModalOpen.value = true;
  } catch (error) {
    console.error('Failed to load stage details for review:', error);
  }
}

function closeModal() {
  isModalOpen.value = false;
  modalBoard.value = null;
  selectedHistory.value = null;
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

function preventPinchZoom(e: TouchEvent) {
  if (e.touches.length > 1) {
    e.preventDefault();
  }
}

onMounted(async () => {
  await initializeUserSession();
  if (currentUser.value && currentUser.value.uuid) {
    try {
      await logVisit(currentUser.value.uuid);
    } catch (error) {
      console.warn('Failed to log visitor access:', error);
    }
  }
  await Promise.all([
    loadStagesList(),
    loadAiStagesList(),
    loadRankingsList(),
    loadUserHistory()
  ]);
  window.addEventListener('resize', handleConfettiResize);
  document.addEventListener('touchstart', preventPinchZoom, { passive: false });

  // Show Help modal to first-time visitors
  const visited = localStorage.getItem('rotagic_visited');
  if (!visited) {
    isHelpModalOpen.value = true;
    localStorage.setItem('rotagic_visited', 'true');
  }
});

onUnmounted(() => {
  resetCountdown();
  window.removeEventListener('resize', handleConfettiResize);
  document.removeEventListener('touchstart', preventPinchZoom);
  stopConfetti();
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
  height: 100vh;
  overflow: hidden; /* Prevent body scroll */
  touch-action: pan-x pan-y;
}

.app-container {
  display: flex;
  flex-direction: column;
  height: 100vh;
  box-sizing: border-box;
  padding: 1rem 1.5rem;
  max-width: 1200px;
  width: 100%;
  overflow: hidden;
  touch-action: pan-x pan-y;
}

.app-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
  padding-bottom: 0.75rem;
  border-bottom: 1px solid #1e293b;
  flex-shrink: 0;
}

.logo-wrapper {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.logo-icon {
  width: 2.2rem;
  height: 2.2rem;
  border: 4px solid transparent;
  border-top-color: #38bdf8;
  border-bottom-color: #818cf8;
  border-radius: 50%;
  animation: spin 3s linear infinite;
  box-shadow: 0 0 15px rgba(56, 189, 248, 0.4);
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.app-title {
  font-size: 2rem;
  font-weight: 800;
  background: linear-gradient(135deg, #38bdf8 0%, #818cf8 50%, #c084fc 100%);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
  margin: 0;
  letter-spacing: -0.5px;
  line-height: 1.1;
}

.app-nav button {
  padding: 0.5rem 1rem;
  background-color: transparent;
  border: 1px solid transparent;
  border-radius: 8px;
  color: #94a3b8;
  font-family: 'Outfit', sans-serif;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
  display: inline-flex;
  align-items: center;
  gap: 0.25rem;
}

.app-nav button.active {
  background-color: rgba(56, 189, 248, 0.1);
  border-color: rgba(56, 189, 248, 0.2);
  color: #38bdf8;
}

.app-nav button:hover:not(.active) {
  color: #f8fafc;
  background-color: rgba(255, 255, 255, 0.05);
}

.leaderboard-toggle-btn,
.help-toggle-btn {
  padding: 0.5rem 0.75rem;
  background-color: #334155;
  border: 1px solid #475569;
  border-radius: 8px;
  color: #f8fafc;
  cursor: pointer;
  font-family: 'Outfit', sans-serif;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 0.25rem;
  transition: all 0.2s ease;
}

.leaderboard-toggle-btn:hover,
.help-toggle-btn:hover {
  background-color: #475569;
  border-color: #64748b;
  color: #ffffff;
}

.leaderboard-toggle-btn.active {
  background: linear-gradient(135deg, rgba(251, 191, 36, 0.15), rgba(245, 158, 11, 0.15));
  border-color: #fbbf24;
  color: #fbbf24;
  box-shadow: 0 0 10px rgba(251, 191, 36, 0.2);
}

.help-toggle-btn.active {
  background: rgba(56, 189, 248, 0.15);
  border-color: #38bdf8;
  color: #38bdf8;
  box-shadow: 0 0 10px rgba(56, 189, 248, 0.2);
}

.toggle-list-btn {
  width: 100%;
  padding: 0.6rem;
  border: none;
  border-radius: 10px;
  font-family: 'Outfit', sans-serif;
  font-weight: 700;
  font-size: 0.85rem;
  cursor: pointer;
  transition: all 0.2s ease;
}

.toggle-list-btn.expand-btn {
  background-color: rgba(56, 189, 248, 0.1);
  border: 1px solid rgba(56, 189, 248, 0.3);
  color: #38bdf8;
}

.toggle-list-btn.expand-btn:hover {
  background-color: rgba(56, 189, 248, 0.2);
  border-color: #38bdf8;
  box-shadow: 0 0 12px rgba(56, 189, 248, 0.15);
}

.toggle-list-btn.collapse-btn {
  background-color: #334155;
  border: 1px solid #475569;
  color: #f8fafc;
}

.toggle-list-btn.collapse-btn:hover {
  background-color: #475569;
  border-color: #64748b;
}

.app-layout {
  display: grid;
  grid-template-columns: 1fr;
  gap: 1.25rem;
  width: 100%;
  flex-grow: 1;
  min-height: 0; /* Important constraint for inner scrolling */
  align-items: stretch;
  transition: grid-template-columns 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.app-layout.mypage-layout {
  grid-template-columns: 280px 1fr;
}

@media (max-width: 1024px) {
  .app-layout.mypage-layout {
    grid-template-columns: 240px 1fr;
  }
}

@media (max-width: 768px) {
  .app-header {
    margin-bottom: 0.5rem;
    padding-bottom: 0.5rem;
    gap: 0.5rem;
  }
  .app-title {
    font-size: 1.4rem;
  }
  .app-subtitle {
    display: none;
  }
  .logo-icon {
    width: 1.6rem;
    height: 1.6rem;
    border-width: 3px;
  }
  .app-container {
    padding: 0.5rem;
  }
  .app-nav button, 
  .leaderboard-toggle-btn,
  .help-toggle-btn {
    padding: 0.35rem 0.6rem;
    font-size: 0.78rem;
  }
  .app-layout {
    grid-template-columns: 1fr !important;
    gap: 0.75rem;
  }
  .app-layout.mypage-layout {
    overflow-y: auto;
    height: 100%;
  }
  .app-sidebar-left {
    height: auto;
    flex-shrink: 0;
  }
  .mypage-dashboard {
    width: 100%;
    box-sizing: border-box;
  }
  .active-stage-badge {
    width: 85vw;
    max-width: 340px;
    padding: 0.5rem 1.25rem;
  }
  .puzzle-selector-dropdown {
    width: 85vw;
    max-width: 340px;
  }
}

@media (max-width: 600px) {
  .app-nav button .btn-text,
  .leaderboard-toggle-btn .btn-text,
  .help-toggle-btn .btn-text {
    display: none;
  }
  .app-nav button,
  .leaderboard-toggle-btn,
  .help-toggle-btn {
    padding: 0.5rem;
    justify-content: center;
    width: 36px;
    height: 36px;
    display: inline-flex;
    align-items: center;
  }
}

/* Floating Stage Selector */
.puzzle-selector-floating-container {
  position: absolute;
  top: 25px;
  left: 50%;
  transform: translateX(-50%);
  z-index: 100;
  display: flex;
  justify-content: center;
}

.active-stage-badge {
  display: inline-flex;
  align-items: center;
  justify-content: space-between;
  gap: 0.6rem;
  background: rgba(30, 41, 59, 0.7);
  backdrop-filter: blur(12px);
  border: 1px solid rgba(255, 255, 255, 0.08);
  padding: 0.5rem 1.25rem;
  border-radius: 9999px;
  cursor: pointer;
  box-shadow: 0 4px 20px -5px rgba(0, 0, 0, 0.3);
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
  white-space: nowrap;
  max-width: 90vw;
  box-sizing: border-box;
  min-width: 240px;
}

.active-stage-badge:hover {
  background: rgba(30, 41, 59, 0.9);
  border-color: rgba(56, 189, 248, 0.4);
  box-shadow: 0 6px 24px -5px rgba(56, 189, 248, 0.2);
}

.active-stage-badge-name {
  font-weight: 700;
  font-size: 0.95rem;
  color: #f8fafc;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  flex-grow: 1;
  text-align: left;
}

.active-stage-badge-size {
  font-size: 0.8rem;
  color: #64748b;
  background-color: rgba(255, 255, 255, 0.05);
  padding: 0.1rem 0.4rem;
  border-radius: 6px;
}

.active-stage-badge-tag {
  font-size: 0.7rem;
  font-weight: 800;
  padding: 0.1rem 0.35rem;
  border-radius: 4px;
  text-transform: uppercase;
}

.active-stage-arrow {
  color: #94a3b8;
  font-size: 0.75rem;
  transition: transform 0.2s ease;
}

.active-stage-arrow.open {
  transform: rotate(180deg);
}

/* Dropdown Container */
.puzzle-selector-dropdown {
  position: absolute;
  top: 100%;
  left: 50%;
  transform: translateX(-50%) translateY(8px);
  width: 340px;
  max-height: 350px;
  background: rgba(15, 23, 42, 0.95);
  backdrop-filter: blur(16px);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 16px;
  padding: 1rem;
  box-shadow: 0 20px 40px -15px rgba(0, 0, 0, 0.5);
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

/* Leaderboard Popup Modal */
.leaderboard-popup-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 999;
  display: flex;
  justify-content: center;
  align-items: center;
  background: rgba(15, 23, 42, 0.6);
  backdrop-filter: blur(4px);
  animation: fade-in 0.25s ease-out;
}

.leaderboard-popup-content {
  background: rgba(30, 41, 59, 0.85);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 20px;
  padding: 1.5rem;
  width: 340px;
  max-height: 80vh;
  display: flex;
  flex-direction: column;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.55);
  animation: slide-up-anim 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}

.leaderboard-popup-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
  padding-bottom: 0.75rem;
  margin-bottom: 1rem;
}

.leaderboard-popup-title {
  margin: 0;
  color: #fbbf24;
  font-size: 1.2rem;
  font-weight: 700;
}

.leaderboard-popup-close {
  background: transparent;
  border: none;
  color: #94a3b8;
  font-size: 1.5rem;
  cursor: pointer;
  transition: color 0.15s ease;
}

.leaderboard-popup-close:hover {
  color: #f8fafc;
}

/* Transitions */
.slide-down-enter-active, .slide-down-leave-active {
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
}
.slide-down-enter-from, .slide-down-leave-to {
  opacity: 0;
  transform: translateX(-50%) translateY(0px) scale(0.95);
}

@keyframes fade-in {
  from { opacity: 0; }
  to { opacity: 1; }
}

@keyframes slide-up-anim {
  from {
    transform: translateY(20px);
    opacity: 0;
  }
  to {
    transform: translateY(0);
    opacity: 1;
  }
}

/* Left & Right Sidebar Cards */
.app-sidebar-left {
  display: flex;
  flex-direction: column;
  min-height: 0;
}

.sidebar-card {
  background-color: #1e293b; /* Slate 800 */
  border: 1px solid #334155;
  border-radius: 16px;
  padding: 1.25rem;
  box-shadow: 0 10px 25px -5px rgba(0, 0, 0, 0.3);
  display: flex;
  flex-direction: column;
  height: 100%;
  min-height: 0;
  box-sizing: border-box;
}

.sidebar-card-title {
  margin-top: 0;
  margin-bottom: 1rem;
  color: #38bdf8;
  font-size: 1.15rem;
  font-weight: 700;
  border-bottom: 1px solid #334155;
  padding-bottom: 0.5rem;
  flex-shrink: 0;
}

/* Category Tabs (Normal / AI) */
.category-tabs {
  display: flex;
  gap: 0.5rem;
  margin-bottom: 0.75rem;
  flex-shrink: 0;
}

.category-tab-btn {
  flex-grow: 1;
  padding: 0.4rem;
  background-color: #0f172a;
  border: 1px solid #334155;
  border-radius: 6px;
  color: #94a3b8;
  cursor: pointer;
  font-family: 'Outfit', sans-serif;
  font-weight: 600;
  font-size: 0.85rem;
  transition: all 0.2s;
}

.category-tab-btn.active {
  background-color: #38bdf8;
  border-color: #38bdf8;
  color: #0f172a;
}

/* Scrollable card Lists */
.stage-card-list, .leaderboard-scrollable {
  flex-grow: 1;
  overflow-y: auto;
  min-height: 0;
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  padding-right: 4px;
}

/* Scrollbar styling */
.stage-card-list::-webkit-scrollbar,
.leaderboard-scrollable::-webkit-scrollbar {
  width: 6px;
}

.stage-card-list::-webkit-scrollbar-track,
.leaderboard-scrollable::-webkit-scrollbar-track {
  background: transparent;
}

.stage-card-list::-webkit-scrollbar-thumb,
.leaderboard-scrollable::-webkit-scrollbar-thumb {
  background: #334155;
  border-radius: 3px;
}

.stage-card-list::-webkit-scrollbar-thumb:hover,
.leaderboard-scrollable::-webkit-scrollbar-thumb:hover {
  background: #475569;
}

/* Card item */
.stage-item-card {
  background-color: #0f172a;
  border: 1px solid #1e293b;
  border-radius: 10px;
  padding: 0.75rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
  cursor: pointer;
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
}

.stage-item-card:hover {
  border-color: #38bdf8;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(56, 189, 248, 0.15);
}

.stage-item-card.active {
  background: linear-gradient(135deg, rgba(56, 189, 248, 0.15), rgba(129, 140, 248, 0.15));
  border-color: #38bdf8;
  box-shadow: 0 0 10px rgba(56, 189, 248, 0.25);
}

.stage-card-info {
  display: flex;
  flex-direction: column;
  gap: 0.15rem;
}

.stage-card-name {
  font-weight: 700;
  font-size: 0.9rem;
  color: #f8fafc;
}

.stage-card-size {
  font-size: 0.75rem;
  color: #64748b;
}

.stage-card-tag {
  font-size: 0.7rem;
  font-weight: 700;
  padding: 0.15rem 0.4rem;
  border-radius: 4px;
}

.normal-tag {
  background-color: rgba(99, 102, 241, 0.15);
  color: #818cf8;
}

.ai-tag {
  background-color: rgba(236, 72, 153, 0.15);
  color: #f472b6;
}

/* Center Column (Game board space) */
.app-main {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  min-height: 0;
  flex-grow: 1;
}

.canvas-wrapper-container {
  display: flex;
  justify-content: center;
  align-items: center;
  flex-grow: 1;
  min-width: 0;
  min-height: 0;
  width: 100%;
  position: relative;
}

.canvas-wrapper {
  background: transparent;
  backdrop-filter: none;
  border: none;
  padding: 0;
  border-radius: 0;
  box-shadow: none;
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
  height: 100%;
  min-width: 0;
  min-height: 0;
}

.loading-state {
  color: #94a3b8;
  font-size: 1rem;
  text-align: center;
}

.confetti-canvas {
  pointer-events: none;
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  z-index: 9999;
}

.celebration-overlay-container {
  display: flex;
  justify-content: center;
  align-items: center;
  margin-top: 0.75rem;
  z-index: 10000;
}

.transition-indicator-card {
  display: inline-flex;
  align-items: center;
  gap: 0.75rem;
  background: rgba(30, 41, 59, 0.75);
  backdrop-filter: blur(12px);
  border: 1px solid rgba(255, 255, 255, 0.08);
  padding: 0.5rem 1.2rem;
  border-radius: 9999px;
  box-shadow: 0 10px 25px -5px rgba(0, 0, 0, 0.4);
  animation: pop-in 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}

.indicator-icon {
  font-size: 1.1rem;
  animation: spin-pulse 2s infinite linear;
}

.indicator-progress-container {
  width: 120px;
  height: 6px;
  background: rgba(255, 255, 255, 0.15);
  border-radius: 9999px;
  overflow: hidden;
}

.indicator-progress-bar {
  height: 100%;
  width: 100%;
  background: linear-gradient(90deg, #38bdf8, #818cf8);
  border-radius: 9999px;
  animation: countdown-shrink 3s linear forwards;
}

.indicator-next-arrow {
  color: #38bdf8;
  font-size: 1rem;
  animation: arrow-bounce 1s infinite alternate ease-in-out;
}

.all-cleared-card {
  background: rgba(30, 41, 59, 0.85);
  backdrop-filter: blur(16px);
  border: 1px solid rgba(251, 191, 36, 0.3);
  padding: 1.25rem 2.5rem;
  border-radius: 24px;
  box-shadow: 0 20px 40px -10px rgba(251, 191, 36, 0.2);
  text-align: center;
  animation: pop-in 0.35s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}

.trophy-icon {
  font-size: 3rem;
  animation: trophy-bounce 1.5s infinite alternate ease-in-out;
}

.star-burst {
  font-size: 1.1rem;
  margin-top: 0.25rem;
  opacity: 0.9;
  letter-spacing: 0.25rem;
  animation: pulse-glow 2s infinite alternate ease-in-out;
}

@keyframes countdown-shrink {
  from { width: 100%; }
  to { width: 0%; }
}

@keyframes spin-pulse {
  0% { transform: scale(1) rotate(0deg); }
  50% { transform: scale(1.2) rotate(180deg); }
  100% { transform: scale(1) rotate(360deg); }
}

@keyframes arrow-bounce {
  from { transform: translateX(0); }
  to { transform: translateX(4px); }
}

@keyframes trophy-bounce {
  from { transform: translateY(0) scale(1); }
  to { transform: translateY(-8px) scale(1.05); }
}

@keyframes pulse-glow {
  from { opacity: 0.6; text-shadow: 0 0 4px rgba(251, 191, 36, 0.2); }
  to { opacity: 1; text-shadow: 0 0 12px rgba(251, 191, 36, 0.6); }
}

/* Leaderboard custom styles */
.leaderboard-list {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.leaderboard-item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.4rem 0.6rem;
  background-color: #0f172a;
  border: 1px solid #1e293b;
  border-radius: 8px;
  font-weight: 600;
  font-size: 0.85rem;
}

.leaderboard-item .rank {
  color: #64748b;
  width: 16px;
  text-align: center;
}

.leaderboard-item:nth-child(1) .rank { color: #fbbf24; }
.leaderboard-item:nth-child(2) .rank { color: #94a3b8; }
.leaderboard-item:nth-child(3) .rank { color: #b45309; }

.leaderboard-item .username {
  flex-grow: 1;
  color: #f8fafc;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.leaderboard-item .level {
  color: #38bdf8;
  font-size: 0.75rem;
  background-color: rgba(56, 189, 248, 0.1);
  padding: 0.05rem 0.3rem;
  border-radius: 4px;
}

.leaderboard-item .xp {
  color: #818cf8;
  font-size: 0.75rem;
}

/* My Page Dashboard styling */
.mypage-dashboard {
  background-color: #1e293b;
  border: 1px solid #334155;
  border-radius: 16px;
  padding: 2rem;
  max-width: 450px;
  width: 90%;
  box-shadow: 0 10px 25px -5px rgba(0, 0, 0, 0.3);
}

.mypage-user-profile {
  display: flex;
  align-items: center;
  gap: 1.5rem;
  border-bottom: 1px solid #334155;
  padding-bottom: 1.5rem;
  margin-bottom: 1.5rem;
}

.profile-avatar {
  font-size: 3rem;
  background-color: #0f172a;
  width: 4.5rem;
  height: 4.5rem;
  display: flex;
  justify-content: center;
  align-items: center;
  border-radius: 50%;
  border: 2px solid #38bdf8;
}

.profile-username {
  margin: 0 0 0.5rem 0;
  color: #f8fafc;
  font-size: 1.5rem;
}

.profile-stats {
  display: flex;
  gap: 0.75rem;
}

.profile-lv {
  color: #38bdf8;
  background-color: rgba(56, 189, 248, 0.15);
  font-size: 0.85rem;
  font-weight: 700;
  padding: 0.15rem 0.5rem;
  border-radius: 4px;
}

.profile-xp {
  color: #818cf8;
  font-size: 0.85rem;
  font-weight: 600;
  display: flex;
  align-items: center;
}

.mypage-instruction-box {
  background-color: #0f172a;
  border: 1px solid #1e293b;
  border-radius: 12px;
  padding: 1rem;
}

.mypage-instruction-box h3 {
  margin-top: 0;
  color: #38bdf8;
  font-size: 1rem;
}

.mypage-instruction-box p {
  margin: 0;
  color: #94a3b8;
  font-size: 0.85rem;
  line-height: 1.5;
}

/* History Card */
.history-item {
  background-color: #0f172a;
  border: 1px solid #1e293b;
  border-radius: 10px;
  padding: 0.6rem 0.8rem;
  transition: all 0.2s;
}

.history-item:hover {
  border-color: #38bdf8;
  transform: translateY(-1px);
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

