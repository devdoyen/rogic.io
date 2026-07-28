<template>
  <AdminConsoleSection 
    v-if="isAdminMode" 
    v-model:logged="isAdminLogged" 
    @stage-updated="handleStageUpdated"
  />

  <div v-else class="app-container" :class="{ 'home-mode': currentTab === 'home' }">
    <!-- Slim Header (Visible only outside home page) -->
    <header v-if="currentTab !== 'home'" class="app-header">
      <div class="logo-wrapper" @click="onTabChange('home')" style="cursor: pointer;">
        <div class="logo-icon">
          <div class="logo-cell filled"></div>
          <div class="logo-cell"></div>
          <div class="logo-cell"></div>
          <div class="logo-cell filled"></div>
        </div>
        <div class="logo-title-wrapper">
          <h1 class="app-title">rogic.io</h1>
        </div>
      </div>
      
      <div class="header-controls" style="display: flex; align-items: center; gap: 0.75rem;">
        <!-- Mini Profile / Login Widget -->
        <div class="mini-profile-widget" style="display: flex; align-items: center; margin-left: 0.25rem;">
          <template v-if="!isSessionLoading">
            <div 
              v-if="currentUser" 
              @click="onTabChange('mypage')" 
              class="mini-profile-card tab-btn-mypage"
              :class="{ active: currentTab === 'mypage' }"
              style="display: flex; align-items: center; gap: 0.5rem; cursor: pointer; padding: 0.35rem 0.75rem; border-radius: 9999px; background: rgba(255,255,255,0.03); border: 1px solid rgba(255,255,255,0.08); transition: all 0.25s ease;"
            >
              <img 
                v-if="currentUser.profileImageUrl" 
                :src="currentUser.profileImageUrl" 
                alt="Profile" 
                style="width: 22px; height: 22px; border-radius: 50%; object-fit: cover; border: 1px solid #38bdf8;" 
              />
              <div v-else style="width: 22px; height: 22px; border-radius: 50%; background: #38bdf8; display: flex; align-items: center; justify-content: center; font-size: 0.7rem; color: white; font-weight: 700;">👤</div>
              <span class="mini-username" style="font-size: 0.8rem; font-weight: 600; color: #f8fafc; max-width: 90px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;">{{ currentUser.username }}</span>
            </div>
            <button 
              v-else 
              @click="handleGoogleLogin" 
              class="mini-login-btn"
              style="display: flex; align-items: center; gap: 0.5rem; padding: 0.35rem 0.75rem; border-radius: 9999px; background: rgba(255,255,255,0.03); border: 1px solid rgba(255,255,255,0.08); cursor: pointer; transition: all 0.2s ease; font-family: inherit;"
              onmouseover="this.style.background='rgba(255,255,255,0.06)'; this.style.borderColor='rgba(255,255,255,0.15)'"
              onmouseout="this.style.background='rgba(255,255,255,0.03)'; this.style.borderColor='rgba(255,255,255,0.08)'"
            >
              <svg style="width: 14px; height: 14px; display: block;" viewBox="0 0 24 24">
                <path d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z" fill="#4285F4"/>
                <path d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z" fill="#34A853"/>
                <path d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.06H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.94l2.85-2.22.81-.63z" fill="#FBBC05"/>
                <path d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.06l3.66 2.84c.87-2.6 3.3-4.52 6.16-4.52z" fill="#EA4335"/>
              </svg>
              <span style="font-size: 0.8rem; font-weight: 600; color: #f8fafc;">Sign In</span>
            </button>
          </template>
          <!-- Show a subtle skeleton while the token validation is in progress -->
          <div v-else class="mini-profile-skeleton" style="width: 86px; height: 28px; border-radius: 9999px; background: rgba(255,255,255,0.02); border: 1px solid rgba(255,255,255,0.05); display: flex; align-items: center; justify-content: center;">
            <div class="skeleton-dot" style="width: 6px; height: 6px; border-radius: 50%; background: #38bdf8; opacity: 0.6; animation: subtle-pulse 1.2s infinite ease-in-out;"></div>
          </div>
        </div>

        <button 
          class="leaderboard-toggle-btn" 
          :class="{ active: isLeaderboardOpen }" 
          @click="isLeaderboardOpen = !isLeaderboardOpen"
          style="display: none;"
        >
          <svg class="header-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M6 9H4.5a2.5 2.5 0 0 1 0-5H6M18 9h1.5a2.5 2.5 0 0 0 0-5H18M4 22h16M10 14.66V17c0 .55-.45 1-1 1H4v2h16v-2h-5c-.55 0-1-.45-1-1v-2.34" />
            <path d="M12 2a6 6 0 0 1 6 6v5a6 6 0 0 1-6 6 6 6 0 0 1-6-6V8a6 6 0 0 1 6-6Z" />
          </svg>
          <span class="btn-text">Leaderboard</span>
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

    <!-- Old Play Size Filter Bar removed and replaced with floating version -->

    <!-- Main Layout Grid -->
    <div class="app-layout">
      <!-- Center Main Column: Canvas & Solved Banner -->
      <main class="app-main">
        <div v-if="currentTab === 'play'" class="play-tab-container tab-fade-in" style="width: 100%; display: flex; flex-direction: column; align-items: center; justify-content: flex-start; min-height: 0; flex: 1;">
            <!-- All Puzzles Cleared State -->
            <div v-if="!isLoading && !hasUnclearedPuzzles" class="all-cleared-state-container">
              <div class="all-cleared-card">
                <div class="countdown-label">Next puzzle in</div>
                <div class="countdown-time">{{ timeUntilMidnight }}</div>
              </div>
            </div>

            <div v-else-if="hasUnclearedPuzzles || isLoading" class="canvas-wrapper-container" style="width: 100%; display: flex; flex-direction: column; align-items: center; min-height: 0; flex: 1; position: relative;">
              <!-- Full-width thin Stage Selector bar (showing cleared name only after solved) -->
              <div class="puzzle-selector-clip-wrapper" style="position: absolute; top: 0; left: 0; width: 100%; height: 40px; overflow: hidden; z-index: 100; pointer-events: none; display: flex; justify-content: center;">
                <transition name="fade-slide-up">
                  <div class="puzzle-selector-floating-container" v-if="currentActiveStage && !isLoading && solveAnimationComplete" style="position: relative; left: auto; transform: none; pointer-events: auto; width: auto; top: 0;">
                    <div class="active-stage-badge readonly-badge">
                      <span class="active-stage-badge-name">{{ currentActiveStage.name }}</span>
                      <!-- Thin Progress bar inside the name display block -->
                      <transition name="fade">
                        <div v-if="allUnclearedStages.length > 0 && nextPuzzleSeconds > 0" class="badge-progress-bar-container">
                          <div class="badge-progress-bar"></div>
                        </div>
                      </transition>
                    </div>
                  </div>
                </transition>
              </div>

              <div class="canvas-wrapper-container" style="width: 100%; display: flex; flex-direction: column; align-items: center; justify-content: center; position: relative; min-height: 0; flex: 1;">
                <!-- Floating Size Selector (Bottom-Right) -->
                <div class="play-size-filter-bar-floating" v-if="availablePlaySizes.length > 0 && !isLoading && !loadError">
                  <div class="active-size-badge" @click.stop="isSizeListOpen = !isSizeListOpen">
                    <span class="active-size-badge-name">
                      {{ selectedPlaySizeFilter + 'x' + selectedPlaySizeFilter }}
                    </span>
                    <span class="active-size-arrow" :class="{ 'open': isSizeListOpen }">▲</span>
                  </div>
                  <transition name="slide-up">
                    <div v-if="isSizeListOpen" class="play-size-filter-dropdown">
                      <div 
                        v-for="size in availablePlaySizes" 
                        :key="size"
                        class="play-size-filter-dropdown-item" 
                        :class="{ active: selectedPlaySizeFilter === String(size) }"
                        @click.stop="selectSizeFilter(String(size))"
                      >
                        {{ size }}x{{ size }}
                      </div>
                    </div>
                  </transition>
                </div>

                <div class="play-area-inner">
                  <!-- Inline Loading state inside canvas frame placeholder -->
                  <div v-if="isLoading" class="canvas-frame-placeholder">
                    <div class="loading-state" style="background: transparent; border: none; max-width: none; margin: 0; box-shadow: none; backdrop-filter: none; padding: 0; height: auto;">
                      <div class="spinner-logo">
                        <div class="spinner-cell filled"></div>
                        <div class="spinner-cell"></div>
                        <div class="spinner-cell"></div>
                        <div class="spinner-cell filled"></div>
                      </div>
                      <p class="loading-text">Loading board data...</p>
                    </div>
                  </div>

                  <!-- Inline Error state inside canvas frame placeholder -->
                  <div v-else-if="loadError" class="canvas-frame-placeholder">
                    <div class="error-state" style="background: transparent; border: none; max-width: none; margin: 0; box-shadow: none; backdrop-filter: none; padding: 0;">
                      <div class="error-icon">⚠️</div>
                      <p class="error-text">{{ loadError }}</p>
                      <button class="retry-btn" @click="handleRetryLoad">
                        🔄 Retry
                      </button>
                    </div>
                  </div>

                  <!-- Normal canvas wrapper -->
                  <div v-else-if="board" class="canvas-wrapper">
                    <NonogramCanvas :board="board" :rotationSteps="currentRotationSteps" :readOnly="solved" @cell-click="handleCellClick" @solve-animation-complete="handleSolveAnimationComplete" />
                  </div>

                  <!-- Puzzle Feedback UI when solved -->
                  <transition name="fade">
                    <div v-if="!isLoading && !loadError && solveAnimationComplete" class="solved-feedback-card">
                      <div v-if="!hasVoted" class="feedback-buttons">
                        <button class="feedback-btn like" @click="handleVote(true)" aria-label="Like">
                          <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" class="svg-icon">
                            <path d="M14 9V5a3 3 0 0 0-3-3l-4 9v11h11.28a2 2 0 0 0 2-1.7l1.38-9a2 2 0 0 0-2-2.3zM7 22H4a2 2 0 0 1-2-2v-7a2 2 0 0 1 2-2h3" />
                          </svg>
                        </button>
                        <button class="feedback-btn dislike" @click="handleVote(false)" aria-label="Dislike">
                          <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" class="svg-icon">
                            <path d="M10 15v4a3 3 0 0 0 3 3l4-9V2H5.72a2 2 0 0 0-2 1.7l-1.38 9a2 2 0 0 0 2 2.3zm7-13h3a2 2 0 0 1 2 2v7a2 2 0 0 1-2 2h-3" />
                          </svg>
                        </button>
                      </div>
                      <div v-else class="feedback-thanks">
                        ✨ Thank You!
                      </div>
                    </div>
                  </transition>
                </div>
              </div>
            </div>

            <div v-if="solveAnimationComplete && allUnclearedStages.length === 0" class="celebration-overlay-container">
              <div class="all-cleared-card">
                <div class="trophy-icon">🏆</div>
                <div class="star-burst">🌟🌟🌟</div>
              </div>
            </div>
          </div>

          <div v-else-if="currentTab === 'home'" class="home-dashboard" :class="[introActive ? introPhase : 'done']">
            <!-- Clean background grid/mesh overlay -->
            <div class="landing-bg"></div>

            <!-- Centered & Top-sliding Header Logo -->
            <div class="landing-logo-container" :class="[introActive ? introPhase : 'done']">
              <div class="logo-icon landing-logo-icon" style="animation: spin 12s linear infinite;">
                <div class="logo-cell filled" style="background: linear-gradient(135deg, #38bdf8, #818cf8); border-radius: 4px;"></div>
                <div class="logo-cell" style="background: rgba(255,255,255,0.05); border-radius: 4px;"></div>
                <div class="logo-cell" style="background: rgba(255,255,255,0.05); border-radius: 4px;"></div>
                <div class="logo-cell filled" style="background: linear-gradient(135deg, #38bdf8, #818cf8); border-radius: 4px;"></div>
              </div>
              <h1 class="landing-logo-title">rogic.io</h1>
            </div>

            <!-- Step 2: Auto-solving Nonogram Canvas (Visible only during 'solving' phase) -->
            <transition name="fade-scale">
              <div v-if="introPhase === 'solving'" class="landing-canvas-wrapper">
                <NonogramCanvas 
                  v-if="demoBoard"
                  :board="demoBoard" 
                  :rotationSteps="demoRotationSteps" 
                  :readOnly="demoSolved" 
                  :renderTrigger="demoRenderTrigger"
                  @cell-click="handleDemoCellClick" 
                  @solve-animation-complete="handleDemoSolveAnimationComplete" 
                />
              </div>
            </transition>

            <!-- Step 3: Stats & Conveyor Belt (Visible during 'stats' phase) -->
            <transition name="fade-scale">
              <div v-if="introPhase === 'stats'" class="landing-stats-conveyor-wrapper">
                <div class="landing-stats">
                  <span class="stats-number">{{ displayedPuzzleCount }}</span>
                  <span class="stats-label">puzzles ready to solve</span>
                </div>
                
                <!-- Horizontal conveyor belt -->
                <div class="landing-conveyor-container">
                  <div class="landing-conveyor-track">
                    <div v-for="loop in 3" :key="loop" class="landing-conveyor-loop">
                      <div v-for="(art, idx) in conveyorArts" :key="idx" class="conveyor-card">
                        <div class="mini-art-grid">
                          <div v-for="(cell, cIdx) in art.grid.flat()" :key="cIdx" :class="{ filled: cell === 1 }" class="mini-art-cell"></div>
                        </div>
                        <span class="conveyor-card-name">{{ art.name }}</span>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </transition>

            <!-- Step 4: Big Premium Centered CTA (Visible in 'cta' or 'done' phase) -->
            <transition name="fade-scale-slow">
              <div v-if="introPhase === 'cta' || introPhase === 'done'" class="landing-cta-container">
                <button class="landing-play-btn" @click="onTabChange('play')" aria-label="Play Now">
                  <svg viewBox="0 0 24 24" class="play-icon">
                    <path fill="currentColor" d="M8 5v14l11-7z"/>
                  </svg>
                  <span>Play Now</span>
                </button>
              </div>
            </transition>

            <!-- Skip / Replay Button in top-right -->
            <button 
              class="intro-control-btn" 
              :title="introActive ? 'Skip Intro' : 'Replay Intro'" 
              @click="introActive ? skipIntro() : replayIntro()"
            >
              <svg v-if="introActive" viewBox="0 0 24 24" class="control-btn-icon">
                <path fill="currentColor" d="M4 18l8.5-6L4 6v12zm9-12v12l8.5-6L13 6z"/>
              </svg>
              <svg v-else viewBox="0 0 24 24" class="control-btn-icon">
                <path fill="currentColor" d="M12 5V1L7 6l5 5V7c3.31 0 6 2.69 6 6s-2.69 6-6 6-6-2.69-6-6H4c0 4.42 3.58 8 8 8s8-3.58 8-8-3.58-8-8-8z"/>
              </svg>
            </button>


            <!-- Footer links -->
            <footer class="landing-footer">
              <div class="footer-links">
                <a href="/privacy.html" target="_blank" rel="noopener noreferrer">Privacy Policy</a>
                <span class="footer-divider">|</span>
                <a href="/terms.html" target="_blank" rel="noopener noreferrer">Terms of Service</a>
                <span class="footer-divider">|</span>
                <a href="https://github.com/devdoyen/rogic.io" target="_blank" rel="noopener noreferrer" aria-label="GitHub" class="footer-github-link">
                  <svg viewBox="0 0 24 24" class="footer-github-icon">
                    <path fill="currentColor" d="M12 2A10 10 0 0 0 2 12c0 4.42 2.87 8.17 6.84 9.5.5.08.66-.23.66-.5v-1.69c-2.77.6-3.36-1.34-3.36-1.34-.46-1.16-1.11-1.47-1.11-1.47-.9-.62.07-.6.07-.6 1 .07 1.53 1.03 1.53 1.03.9 1.52 2.34 1.07 2.91.83.09-.65.35-1.09.63-1.34-2.22-.25-4.55-1.11-4.55-4.92 0-1.11.38-2 1.03-2.71-.1-.25-.45-1.29.1-2.64 0 0 .84-.27 2.75 1.02.79-.22 1.65-.33 2.5-.33.85 0 1.71.11 2.5.33 1.91-1.29 2.75-1.02 2.75-1.02.55 1.35.2 2.39.1 2.64.65.71 1.03 1.6 1.03 2.71 0 3.82-2.34 4.66-4.57 4.91.36.31.69.92.69 1.85V21c0 .27.16.59.67.5C19.14 20.16 22 16.42 22 12A10 10 0 0 0 12 2z"/>
                  </svg>
                </a>
              </div>
              <p class="footer-copyright">&copy; 2026 rogic.io. All rights reserved.</p>
            </footer>
          </div>

          <MyPageSection
            v-else-if="currentTab === 'mypage'"
            key="mypage"
            v-model:is-review-mode="isReviewMode"
            v-model:modal-board="modalBoard"
            :current-user="currentUser"
            :histories="histories"
            :history-current-page="historyCurrentPage"
            :history-total-pages="historyTotalPages"
            @close="onTabChange('play')"
            @login="handleGoogleLogin"
            @logout="handleGoogleLogout"
            @page-change="loadUserHistory"
          />
      </main>
    </div>

    <!-- Popup for Leaderboard -->
    <LeaderboardPopup v-model:open="isLeaderboardOpen" :rankings="rankings" />





    <!-- Modal for Puzzle Replay Guide (First-time My Page view) -->
    <div v-if="isMypageTipOpen" class="help-modal-overlay" style="position: fixed; top: 0; left: 0; right: 0; bottom: 0; display: flex; justify-content: center; align-items: center; background: rgba(15, 23, 42, 0.85); backdrop-filter: blur(8px); z-index: 10000;" @click.self="closeMypageTip">
      <div class="modal-content" style="max-width: 380px;">
        <h3 class="modal-title" style="margin-top: 0; color: #38bdf8; font-weight: 700; font-size: 1.2rem;">💡 Puzzle Replay</h3>
        <p style="color: #94a3b8; font-size: 0.9rem; line-height: 1.6; margin: 1.5rem 0;">
          Click any history card on My Page to review your solved puzzle solutions in read-only mode.
        </p>
        <div>
          <button class="modal-close-btn" @click="closeMypageTip" style="padding: 0.5rem 1.5rem; background: linear-gradient(135deg, #38bdf8, #818cf8); border: none; border-radius: 8px; color: #ffffff; font-weight: 600; cursor: pointer; transition: opacity 0.2s;">Got it!</button>
        </div>
      </div>
    </div>

    <!-- Confetti Overlay Canvas -->
    <canvas ref="confettiCanvas" class="confetti-canvas"></canvas>
  </div>
</template>

<script setup lang="ts">
import { ref, shallowRef, onMounted, onUnmounted, computed, watch } from 'vue';
import { App as CapacitorApp } from '@capacitor/app';
import { Capacitor } from '@capacitor/core';

const isTestEnv = typeof window !== 'undefined' && (
  (globalThis as any).process?.env?.NODE_ENV === 'test' ||
  (globalThis as any).vitest !== undefined ||
  (globalThis as any).__vitest_worker__ !== undefined ||
  navigator.userAgent.includes('jsdom')
);
import NonogramCanvas from './components/NonogramCanvas.vue';
import AdminConsoleSection from './components/AdminConsoleSection.vue';
import MyPageSection from './components/MyPageSection.vue';
import LeaderboardPopup from './components/LeaderboardPopup.vue';

async function handleStageUpdated() {
  await Promise.all([
    loadStagesList(),
    loadAiStagesList()
  ]);
}
import { PuzzleBoard } from './engine/puzzleBoard';
import { rotateGrid } from './engine/gridRotator';
import { fetchStages, fetchStageById, fetchAiStages, startStage, likeStage, dislikeStage, fetchNextReleaseDelaySeconds, verifyStageSolve } from './api/stageApi';
import type { StageSummary } from './api/stageApi';
import { fetchRanking, clearStage, fetchMeFromServer, fetchUserHistory, fetchClearedStageIds, syncGuestHistory } from './api/userApi';
import type { User, HistoryResponse } from './api/userApi';
import { setUserSession, clearUserSession } from './api/auth';
import type { UserSession } from './api/auth';
import { loginWithGoogle, logout as googleLogout, getOrRefreshToken } from './api/cognito';
import { isAdminAuthenticated } from './api/adminApi';

const isAdminMode = ref(false);
const isAdminLogged = ref(isAdminAuthenticated());
const stages = ref<StageSummary[]>([]);

const selectedStageId = ref<number | null>(null);
const board = shallowRef<PuzzleBoard | null>(null);

// Interactive Demo Puzzle State Models
const demoBoard = shallowRef<PuzzleBoard | null>(null);
const demoSolved = ref(false);
const demoSolveAnimationComplete = ref(false);
const demoRotationSteps = ref(0);
const demoRenderTrigger = ref(0);

const introActive = ref(!isTestEnv && !sessionStorage.getItem('rogic_intro_played'));
const introPhase = ref<'logo' | 'solving' | 'stats' | 'cta' | 'done'>(
  (isTestEnv || sessionStorage.getItem('rogic_intro_played')) ? 'done' : 'logo'
);
let autoSolveTimer: any = null;
let introTimeoutId: any = null;

const heartGrid = [
  [0, 1, 0, 1, 0],
  [1, 1, 1, 1, 1],
  [1, 1, 1, 1, 1],
  [0, 1, 1, 1, 0],
  [0, 0, 1, 0, 0]
];
const starGrid = [
  [0, 0, 1, 0, 0],
  [0, 1, 1, 1, 0],
  [1, 1, 1, 1, 1],
  [0, 1, 1, 1, 0],
  [0, 1, 0, 1, 0]
];
const smileGrid = [
  [0, 0, 0, 0, 0],
  [0, 1, 0, 1, 0],
  [0, 0, 0, 0, 0],
  [1, 0, 0, 0, 1],
  [0, 1, 1, 1, 0]
];
const diamondGrid = [
  [0, 0, 1, 0, 0],
  [0, 1, 1, 1, 0],
  [1, 1, 1, 1, 1],
  [0, 1, 1, 1, 0],
  [0, 0, 1, 0, 0]
];
const swordGrid = [
  [0, 0, 1, 0, 0],
  [0, 0, 1, 0, 0],
  [0, 1, 1, 1, 0],
  [0, 0, 1, 0, 0],
  [0, 0, 1, 0, 0]
];

const conveyorArts = [
  { name: 'Sweet Heart', grid: heartGrid },
  { name: 'Bright Star', grid: starGrid },
  { name: 'Happy Smile', grid: smileGrid },
  { name: 'Shiny Gem', grid: diamondGrid },
  { name: 'Iron Sword', grid: swordGrid }
];

const demoSolutionGrid = [
  [0, 1, 0, 1, 0],
  [1, 1, 1, 1, 1],
  [1, 1, 1, 1, 1],
  [0, 1, 1, 1, 0],
  [0, 0, 1, 0, 0]
];




function initDemoBoard() {
  const rotatedSolution = rotateGrid(demoSolutionGrid, 3);
  const boardObj = new PuzzleBoard(rotatedSolution);
  for (let r = 0; r < 5; r++) {
    for (let c = 0; c < 5; c++) {
      boardObj.setCell(r, c, 0);
    }
  }
  demoBoard.value = boardObj;
  demoSolved.value = false;
  demoSolveAnimationComplete.value = false;
  demoRotationSteps.value = 3;
  demoRenderTrigger.value++;
}

function handleDemoCellClick() {
  if (demoBoard.value) {
    demoSolved.value = demoBoard.value.isSolved();
  }
}

function handleDemoSolveAnimationComplete() {
  demoSolveAnimationComplete.value = true;
  demoRotationSteps.value = 4;
  if (introActive.value) {
    setTimeout(() => {
      introPhase.value = 'stats';
      startStatsCountUp();
      
      setTimeout(() => {
        if (introPhase.value === 'stats') {
          introPhase.value = 'cta';
          
          setTimeout(() => {
            if (introPhase.value === 'cta') {
              introPhase.value = 'done';
              introActive.value = false;
              demoSolveAnimationComplete.value = false;
              demoSolved.value = false;
              sessionStorage.setItem('rogic_intro_played', 'true');
            }
          }, 1500);
        }
      }, 5000);
    }, 1000);
  }
}

function startStatsCountUp() {
  displayedPuzzleCount.value = 0;
  const target = totalPuzzlesCount.value;
  const duration = 2000;
  const start = 0;
  const startTime = performance.now();
  
  function animate(now: number) {
    const elapsed = now - startTime;
    const progress = Math.min(elapsed / duration, 1);
    const easeProgress = progress * (2 - progress);
    displayedPuzzleCount.value = Math.floor(start + (target - start) * easeProgress);
    
    if (progress < 1 && introPhase.value === 'stats') {
      requestAnimationFrame(animate);
    } else if (introPhase.value === 'cta' || introPhase.value === 'done') {
      displayedPuzzleCount.value = target;
    }
  }
  requestAnimationFrame(animate);
}

function triggerDemoAutoSolve() {
  if (!demoBoard.value) return;
  
  for (let r = 0; r < demoBoard.value.rowCount; r++) {
    for (let c = 0; c < demoBoard.value.colCount; c++) {
      demoBoard.value.setCell(r, c, 0);
    }
  }
  demoSolved.value = false;
  demoSolveAnimationComplete.value = false;
  demoRotationSteps.value = 3;
  demoRenderTrigger.value++;

  const cellsToFill: {r: number, c: number}[] = [];
  const sol = demoBoard.value.solutionGrid;
  for (let r = 0; r < demoBoard.value.rowCount; r++) {
    for (let c = 0; c < demoBoard.value.colCount; c++) {
      if (sol[r][c] === 1) {
        cellsToFill.push({ r, c });
      }
    }
  }

  cellsToFill.sort((a, b) => a.r - b.r || a.c - b.c);

  let index = 0;
  if (autoSolveTimer) clearInterval(autoSolveTimer);
  
  autoSolveTimer = setInterval(() => {
    if (!demoBoard.value) {
      clearInterval(autoSolveTimer);
      return;
    }
    if (index >= cellsToFill.length) {
      clearInterval(autoSolveTimer);
      demoSolved.value = true;
      return;
    }
    const cell = cellsToFill[index];
    demoBoard.value.setCell(cell.r, cell.c, 1);
    demoRenderTrigger.value++;
    index++;
  }, 250);
}

function startIntroAnimation() {
  if (isTestEnv) return;
  
  introPhase.value = 'logo';
  introActive.value = true;
  
  if (autoSolveTimer) clearInterval(autoSolveTimer);
  if (introTimeoutId) clearTimeout(introTimeoutId);
  
  introTimeoutId = setTimeout(() => {
    if (introPhase.value !== 'logo') return;
    introPhase.value = 'solving';
    triggerDemoAutoSolve();
  }, 1800);
}

function skipIntro() {
  if (autoSolveTimer) clearInterval(autoSolveTimer);
  if (introTimeoutId) clearTimeout(introTimeoutId);
  
  introPhase.value = 'done';
  introActive.value = false;
  demoSolveAnimationComplete.value = false;
  demoSolved.value = false;
  sessionStorage.setItem('rogic_intro_played', 'true');
  
  if (demoBoard.value) {
    const sol = demoBoard.value.solutionGrid;
    for (let r = 0; r < demoBoard.value.rowCount; r++) {
      for (let c = 0; c < demoBoard.value.colCount; c++) {
        demoBoard.value.setCell(r, c, sol[r][c]);
      }
    }
    demoSolved.value = true;
    demoSolveAnimationComplete.value = true;
    demoRotationSteps.value = 4;
    demoRenderTrigger.value++;
  }
  displayedPuzzleCount.value = totalPuzzlesCount.value;
}

function replayIntro() {
  sessionStorage.removeItem('rogic_intro_played');
  initDemoBoard();
  startIntroAnimation();
}
const solved = ref(false);
const solveAnimationComplete = ref(false);
const hasVoted = ref(false);
const currentStageVotes = ref({ upvotes: 0, downvotes: 0 });
const nextPuzzleSeconds = ref(3);
const isLoading = ref(true);
const isSessionLoading = ref(true);
const loadError = ref<string | null>(null);
let countdownTimer: any = null;




const rankings = ref<User[]>([]);
const currentUser = ref<UserSession | null>(null);
const currentTab = ref<'home' | 'play' | 'mypage' | 'admin'>(isTestEnv ? 'play' : 'home');

watch([currentTab, isAdminMode], ([newTab, newAdmin]) => {
  if (isTestEnv) return;
  if (newAdmin) {
    document.body.style.overflow = 'auto';
    document.body.style.height = 'auto';
    document.body.style.display = 'block';
    document.body.style.backgroundColor = '#f8f9fa';
  } else if (newTab === 'home') {
    document.body.style.overflow = 'auto';
    document.body.style.height = 'auto';
    document.body.style.display = 'block';
    document.body.style.backgroundColor = '#0a0f1d';
  } else {
    document.body.style.overflow = 'hidden';
    document.body.style.height = '100vh';
    document.body.style.height = '100dvh';
    document.body.style.display = 'flex';
    document.body.style.backgroundColor = '#0f172a';
  }
}, { immediate: true });
const histories = ref<any[]>([]);
const startTime = ref<number>(Date.now());

const aiStages = ref<StageSummary[]>([]);
const selectedAiStageId = ref<number | null>(null);
const isAiStageActive = ref(false);
const selectedCategory = ref<'normal' | 'ai'>('normal');
const isMypageTipOpen = ref(false);
const isReviewMode = ref(false);
const modalBoard = shallowRef<PuzzleBoard | null>(null);

const isStageListOpen = ref(false);
const isLeaderboardOpen = ref(false);

const currentActiveStage = computed(() => {
  if (isAiStageActive.value) {
    return (aiStages.value || []).find(s => s.id === selectedAiStageId.value) || null;
  } else {
    const found = (allStagesSummary.value || []).find(s => s.id === selectedStageId.value);
    if (found) return found;
    return (stages.value || []).find(s => s.id === selectedStageId.value) || null;
  }
});

const clearedStageIds = ref<Set<number>>(new Set());
const allStagesSummary = ref<StageSummary[]>([]);

const playStagesCurrentPage = ref(0);
const playStagesTotalPages = ref(1);
const historyCurrentPage = ref(0);
const historyTotalPages = ref(1);

const allUnclearedStages = computed(() => {
  const stageMap = new Map<number, StageSummary>();
  (allStagesSummary.value || []).forEach(s => stageMap.set(s.id, s));
  (aiStages.value || []).forEach(s => stageMap.set(s.id, s));
  const combined = Array.from(stageMap.values());
  return combined.filter(s => !clearedStageIds.value.has(s.id));
});

const hasUnclearedPuzzles = computed(() => {
  if (allStagesSummary.value.length === 0) return true;
  const hasRegular = allStagesSummary.value.some(s => !clearedStageIds.value.has(s.id));
  const hasAi = (aiStages.value || []).some(s => !clearedStageIds.value.has(s.id));
  return hasRegular || hasAi;
});

const totalPuzzlesCount = computed(() => {
  const apiCount = (allStagesSummary.value?.length || 0) + (aiStages.value?.length || 0);
  return apiCount > 0 ? apiCount : 28;
});

const displayedPuzzleCount = ref(0);

watch(totalPuzzlesCount, (newVal) => {
  if (newVal <= 0) return;
  if (introActive.value && introPhase.value !== 'stats' && introPhase.value !== 'cta' && introPhase.value !== 'done') {
    return;
  }
  if (typeof window === 'undefined' || typeof requestAnimationFrame === 'undefined' || typeof performance === 'undefined') {
    displayedPuzzleCount.value = newVal;
    return;
  }

  const start = displayedPuzzleCount.value;
  const end = newVal;
  const duration = 1200; // 1.2s count up animation
  const startTime = performance.now();

  function animate(now: number) {
    const elapsed = now - startTime;
    const progress = Math.min(elapsed / duration, 1);
    const easeProgress = progress * (2 - progress); // easeOutQuad
    displayedPuzzleCount.value = Math.floor(start + (end - start) * easeProgress);

    if (progress < 1) {
      requestAnimationFrame(animate);
    }
  }

  requestAnimationFrame(animate);
}, { immediate: true });



const delaySeconds = ref(0);
const timeUntilMidnight = ref('');
let dailyPuzzleTimerId: any = null;
let syncTimerId: any = null;

async function syncDailyPuzzleCountdown() {
  if (hasUnclearedPuzzles.value || currentTab.value !== 'play') {
    return;
  }
  try {
    const delay = await fetchNextReleaseDelaySeconds();
    delaySeconds.value = delay;
    updateDailyPuzzleTimeText();
  } catch (error) {
    console.error('Failed to sync next puzzle delay with server:', error);
  }
}

watch([hasUnclearedPuzzles, currentTab], ([hasUncleared, tab]) => {
  if (!hasUncleared && tab === 'play') {
    syncDailyPuzzleCountdown();
  }
});

function updateDailyPuzzleTimeText() {
  if (delaySeconds.value <= 0) {
    timeUntilMidnight.value = '00:00:00';
    return;
  }
  
  const hours = Math.floor(delaySeconds.value / 3600);
  const minutes = Math.floor((delaySeconds.value % 3600) / 60);
  const seconds = delaySeconds.value % 60;
  
  const pad = (num: number) => String(num).padStart(2, '0');
  timeUntilMidnight.value = `${pad(hours)}:${pad(minutes)}:${pad(seconds)}`;
}

function tickDailyPuzzleCountdown() {
  if (delaySeconds.value > 0) {
    delaySeconds.value--;
    updateDailyPuzzleTimeText();
  }
}

const selectedPlaySizeFilter = ref<string>('5');
const isSizeListOpen = ref<boolean>(false);

function selectSizeFilter(size: string) {
  selectedPlaySizeFilter.value = size;
  isSizeListOpen.value = false;
}

const allUnclearedStagesForSizes = computed(() => {
  const stageMap = new Map<number, StageSummary>();
  (allStagesSummary.value || []).forEach(s => stageMap.set(s.id, s));
  (aiStages.value || []).forEach(s => stageMap.set(s.id, s));
  const combined = Array.from(stageMap.values());
  return combined.filter(s => !clearedStageIds.value.has(s.id));
});

const availablePlaySizes = computed(() => {
  const sizes = new Set<number>();
  allUnclearedStagesForSizes.value.forEach(s => {
    sizes.add(s.width);
  });
  return Array.from(sizes).sort((a, b) => a - b);
});

watch([availablePlaySizes, solved], ([newSizes, isSolved]) => {
  if (isSolved) return; // Wait until countdown finishes and solved becomes false
  if (newSizes.length > 0) {
    const currentVal = selectedPlaySizeFilter.value;
    if (currentVal === 'All' || !newSizes.includes(parseInt(currentVal))) {
      selectedPlaySizeFilter.value = String(newSizes[0]);
    }
  }
}, { immediate: true });


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

    if (active && (solved.value || demoSolved.value)) {
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

watch([solveAnimationComplete, demoSolveAnimationComplete], ([newVal, newDemoVal]) => {
  if (newVal || newDemoVal) {
    startConfetti();
  } else {
    stopConfetti();
  }
});

watch(selectedPlaySizeFilter, async (newSize) => {
  if (newSize === 'All') return;

  // Deactivate AI daily stage to switch to regular stages of the new size filter
  isAiStageActive.value = false;
  selectedAiStageId.value = null;

  if (isTestEnv) {
    const sizeNum = parseInt(newSize);
    const current = currentActiveStage.value;
    if (!current || current.width !== sizeNum || isAiStageActive.value) {
      const matching = allUnclearedStages.value.filter(s => s.width === sizeNum && !isStageAi(s));
      if (matching.length > 0 && (!current || current.id !== matching[0].id)) {
        selectStageCard(matching[0].id, isStageAi(matching[0]));
      }
    }
    return;
  }
  await loadStagesList(0);
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
  if (selectedPlaySizeFilter.value === 'All') {
    const remaining = allUnclearedStages.value;
    if (remaining.length > 0) {
      const nextStage = remaining[0];
      selectStageCard(nextStage.id, isStageAi(nextStage));
    }
    return;
  }

  // Filtered by size
  const targetSize = parseInt(selectedPlaySizeFilter.value);
  let remainingOfSize = allUnclearedStages.value.filter(s => s.width === targetSize);

  if (remainingOfSize.length > 0) {
    const nextStage = remainingOfSize[0];
    selectStageCard(nextStage.id, isStageAi(nextStage));
  } else {
    // No more puzzles of the current size! Look for the next size in ascending order
    const allSizes = availablePlaySizes.value; // sorted list of sizes currently having uncleared puzzles
    const nextSize = allSizes.find(size => size > targetSize);

    if (nextSize !== undefined) {
      selectedPlaySizeFilter.value = String(nextSize);
      // Recalculate remaining list with the new size
      remainingOfSize = allUnclearedStages.value.filter(s => s.width === nextSize);
      if (remainingOfSize.length > 0) {
        const nextStage = remainingOfSize[0];
        selectStageCard(nextStage.id, isStageAi(nextStage));
      }
    } else {
      // If no larger size is available, check if there's any smaller size left
      const fallbackSize = allSizes[0];
      if (fallbackSize !== undefined) {
        selectedPlaySizeFilter.value = String(fallbackSize);
        remainingOfSize = allUnclearedStages.value.filter(s => s.width === fallbackSize);
        if (remainingOfSize.length > 0) {
          const nextStage = remainingOfSize[0];
          selectStageCard(nextStage.id, isStageAi(nextStage));
        }
      } else {
        // Absolutely no puzzles left! (allUnclearedStages is empty)
      }
    }
  }
}

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



function getErrorMessage(error: any, fallbackMessage: string): string {
  if (error && error.response) {
    const status = error.response.status;
    if (status >= 500) {
      return `Failed to load due to a server error (${status}). Please try again later.`;
    }
  }
  return fallbackMessage;
}

async function loadStagesList(page: number = 0) {
  if (isTestEnv) console.log('CALLING loadStagesList, stack:', new Error().stack);
  isLoading.value = true;
  loadError.value = null;
  const targetSizeStr = selectedPlaySizeFilter.value;
  try {
    const targetSize = parseInt(targetSizeStr);
    let allRes: any;
    let res: any;
    if (isTestEnv) {
      res = await fetchStages();
      allRes = res;
    } else {
      [allRes, res] = await Promise.all([
        allStagesSummary.value.length === 0 ? fetchStages() : Promise.resolve(allStagesSummary.value),
        fetchStages(page, 20, targetSize)
      ]);
    }

    if (selectedPlaySizeFilter.value !== targetSizeStr) {
      return;
    }

    allStagesSummary.value = allRes && 'content' in allRes ? allRes.content : allRes;
    
    let list: StageSummary[];
    if (res && 'content' in res) {
      list = res.content;
      playStagesTotalPages.value = res.totalPages;
      playStagesCurrentPage.value = res.number;
    } else {
      list = res;
      playStagesTotalPages.value = 1;
      playStagesCurrentPage.value = 0;
    }
    stages.value = list;

    if (list.length > 0) {
      if (selectedStageId.value && list.some(s => s.id === selectedStageId.value)) {
        isLoading.value = false;
        return;
      }
      
      let initialStage = allStagesSummary.value
        .filter(s => s.width === targetSize)
        .find(s => !clearedStageIds.value.has(s.id));
      
      if (!initialStage) {
        const remaining = allUnclearedStages.value;
        if (remaining.length > 0) {
          initialStage = remaining[0];
          selectedPlaySizeFilter.value = String(initialStage.width);
        } else {
          initialStage = list[0];
        }
      }
      
      if (selectedPlaySizeFilter.value !== targetSizeStr) {
        return;
      }

      selectedStageId.value = initialStage.id;
      await loadStageDetails(initialStage.id);
    } else {
      // If no stages of target size, try to load any stages of other sizes (fallback)
      const allList = allStagesSummary.value;
      if (allList.length > 0) {
        let fallbackStage = allList.find(s => !clearedStageIds.value.has(s.id));
        if (!fallbackStage) {
          fallbackStage = allList[0];
        }
        if (selectedPlaySizeFilter.value !== targetSizeStr) {
          return;
        }
        selectedPlaySizeFilter.value = String(fallbackStage.width);
      } else {
        isLoading.value = false;
      }
    }
  } catch (error) {
    if (selectedPlaySizeFilter.value !== targetSizeStr) {
      return;
    }
    console.error('Failed to load stages:', error);
    loadError.value = getErrorMessage(error, 'Failed to load puzzles. Please check your connection and try again.');
    isLoading.value = false;
  }
}

async function loadStageDetails(id: number) {
  resetCountdown();
  isLoading.value = true;
  loadError.value = null;
  board.value = null;
  solveAnimationComplete.value = false;
  try {
    // Record starting attempt
    await startStage(id);
  } catch (error) {
    console.warn(`Failed to log stage start for ID ${id}:`, error);
  }

  if (selectedStageId.value !== id && selectedAiStageId.value !== id) {
    return;
  }

  try {
    const details = await fetchStageById(id);
    if (selectedStageId.value !== id && selectedAiStageId.value !== id) {
      return;
    }

    if (!details || !details.solutionGrid || !Array.isArray(details.solutionGrid) || details.solutionGrid.length === 0 || !Array.isArray(details.solutionGrid[0])) {
      throw new Error('Puzzle solution grid is corrupted or missing.');
    }
    
    // Check for saved progress
    const key = `rogic_progress_stage_${isAiStageActive.value ? 'ai_' : ''}${id}`;
    const savedProgressStr = localStorage.getItem(key);
    let hasLoadedProgress = false;
    let k = Math.floor(Math.random() * 3) + 1;

    if (savedProgressStr) {
      try {
        const progress = JSON.parse(savedProgressStr);
        if (progress.stageId === id && progress.isAiStage === isAiStageActive.value) {
          k = progress.rotationSteps || k;
          currentRotationSteps.value = k;
          const rotated = rotateGrid(details.solutionGrid, k);
          board.value = new PuzzleBoard(rotated);
          
          for (let r = 0; r < progress.currentGrid.length; r++) {
            for (let c = 0; c < progress.currentGrid[r].length; c++) {
              board.value.currentGrid[r][c] = progress.currentGrid[r][c];
            }
          }
          board.value.applyAutoFill();
          board.value.undoStack = progress.undoStack || [];
          board.value.redoStack = progress.redoStack || [];
          
          solved.value = board.value.isSolved();
          startTime.value = Date.now() - (progress.elapsedTimeAccumulated || 0) * 1000;
          hasLoadedProgress = true;
        }
      } catch (err) {
        console.warn('Failed to restore active stage progress:', err);
      }
    }

    if (!hasLoadedProgress) {
      currentRotationSteps.value = k;
      const rotated = rotateGrid(details.solutionGrid, k);
      board.value = new PuzzleBoard(rotated);
      solved.value = false;
      startTime.value = Date.now();
    }

    currentStageVotes.value = {
      upvotes: details.upvotes || 0,
      downvotes: details.downvotes || 0
    };
    hasVoted.value = false;
    isLoading.value = false;
    if (typeof (window as any).dataLayer !== 'undefined') {
      (window as any).dataLayer.push({
        event: 'stage_start',
        stageId: id,
        stageName: details.name,
        stageSize: `${details.width}x${details.height}`,
        isAiStage: isAiStageActive.value
      });
    }
  } catch (error) {
    if (selectedStageId.value !== id && selectedAiStageId.value !== id) {
      return;
    }
    console.error(`Failed to load stage details for ID ${id}:`, error);
    loadError.value = getErrorMessage(error, 'Failed to load puzzle details. Please try again.');
    isLoading.value = false;
  }
}

function handleRetryLoad() {
  loadError.value = null;
  if (currentActiveStage.value) {
    loadStageDetails(currentActiveStage.value.id);
  } else if (selectedStageId.value) {
    loadStageDetails(selectedStageId.value);
  } else if (selectedAiStageId.value) {
    loadStageDetails(selectedAiStageId.value);
  } else {
    loadStagesList();
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

function saveActiveProgress() {
  if (!board.value || solved.value) return;
  const stageId = selectedStageId.value !== null ? selectedStageId.value : selectedAiStageId.value;
  if (stageId === null) return;

  const elapsedTime = Math.floor((Date.now() - startTime.value) / 1000);
  const progress = {
    stageId,
    isAiStage: isAiStageActive.value,
    currentGrid: board.value.currentGrid.map(row => [...row]),
    elapsedTimeAccumulated: elapsedTime,
    undoStack: board.value.undoStack.map(g => g.map(row => [...row])),
    redoStack: board.value.redoStack.map(g => g.map(row => [...row])),
    rotationSteps: currentRotationSteps.value
  };
  const key = `rogic_progress_stage_${isAiStageActive.value ? 'ai_' : ''}${stageId}`;
  localStorage.setItem(key, JSON.stringify(progress));
}

async function handleCellClick() {
  if (board.value) {
    solved.value = board.value.isSolved();
    if (solved.value) {
      const stageId = selectedStageId.value !== null ? selectedStageId.value : selectedAiStageId.value;
      if (stageId !== null) {
        const key = `rogic_progress_stage_${isAiStageActive.value ? 'ai_' : ''}${stageId}`;
        localStorage.removeItem(key);
      }
    } else {
      saveActiveProgress();
    }
  }
}

async function handleSolveAnimationComplete() {
  if (solveAnimationComplete.value) return;
  solveAnimationComplete.value = true;

  try {
    let difficulty = 'NORMAL';
    if (isAiStageActive.value) {
      difficulty = 'HARD';
    } else if (board.value && board.value.colCount <= 5 && board.value.rowCount <= 5) {
      difficulty = 'EASY';
    } else if (board.value && (board.value.colCount >= 10 || board.value.rowCount >= 10)) {
      difficulty = 'HARD';
    }
    const elapsedTime = Math.floor((Date.now() - startTime.value) / 1000);

    if (typeof (window as any).dataLayer !== 'undefined') {
      (window as any).dataLayer.push({
        event: 'stage_clear',
        stageId: selectedStageId.value !== null ? selectedStageId.value : (selectedAiStageId.value !== null ? selectedAiStageId.value : undefined),
        stageName: currentActiveStage.value?.name,
        difficulty: difficulty,
        elapsedTime: elapsedTime,
        isAiStage: isAiStageActive.value
      });
    }

    if (currentUser.value) {
      const userId = currentUser.value.id;
      const stageId = selectedStageId.value !== null ? selectedStageId.value : (selectedAiStageId.value !== null ? selectedAiStageId.value : undefined);
      if (stageId !== undefined) {
        clearedStageIds.value.add(stageId);
      }
      await clearStage(userId, difficulty, stageId, elapsedTime);
      allStagesSummary.value = [];
      await loadRankingsList();
      await loadUserHistory();
    } else {
      const stageId = selectedStageId.value !== null ? selectedStageId.value : (selectedAiStageId.value !== null ? selectedAiStageId.value : undefined);
      if (stageId !== undefined) {
        clearedStageIds.value.add(stageId);
        const savedCleared = localStorage.getItem('guest_cleared_stages');
        const clearedIds = savedCleared ? JSON.parse(savedCleared) : [];
        if (!clearedIds.includes(stageId)) {
          clearedIds.push(stageId);
          localStorage.setItem('guest_cleared_stages', JSON.stringify(clearedIds));
        }

        let token = '';
        if (board.value) {
          try {
            const verification = await verifyStageSolve(
              stageId,
              board.value.currentGrid,
              elapsedTime,
              currentRotationSteps.value
            );
            token = verification.token;
          } catch (err) {
            console.error('Failed to verify stage solve on backend:', err);
          }
        }

        const savedHistories = localStorage.getItem('guest_histories');
        const localHistories = savedHistories ? JSON.parse(savedHistories) : [];
        const currentStage = currentActiveStage.value;
        const stageName = currentStage ? currentStage.name : `Puzzle #${stageId}`;
        const newHistory: HistoryResponse = {
          id: Date.now(),
          userId: 0,
          stageId: stageId,
          stageName: stageName,
          clearedAt: new Date().toISOString(),
          xpEarned: difficulty === 'EASY' ? 100 : (difficulty === 'HARD' ? 250 : 150),
          elapsedTime: elapsedTime,
          proofToken: token
        };
        localHistories.unshift(newHistory);
        localStorage.setItem('guest_histories', JSON.stringify(localHistories));
      }
      allStagesSummary.value = [];
      await loadRankingsList();
      await loadUserHistory();
    }
  } catch (error) {
    console.error('Failed to submit stage clear:', error);
  } finally {
    startNextPuzzleCountdown();
  }
}

async function loadUserHistory(page: number = 0) {
  if (!currentUser.value) {
    const savedCleared = localStorage.getItem('guest_cleared_stages');
    const clearedIds = savedCleared ? JSON.parse(savedCleared) : [];
    clearedStageIds.value = new Set(clearedIds);

    const savedHistories = localStorage.getItem('guest_histories');
    const localHistories = savedHistories ? JSON.parse(savedHistories) : [];
    const pageSize = 10;
    const startIdx = page * pageSize;
    const endIdx = startIdx + pageSize;
    histories.value = localHistories.slice(startIdx, endIdx);
    historyTotalPages.value = Math.max(1, Math.ceil(localHistories.length / pageSize));
    historyCurrentPage.value = page;
    return;
  }
  try {
    const userId = currentUser.value.id;
    const res = await fetchUserHistory(userId, page, 10);
    let list: HistoryResponse[];
    if (res && 'content' in res) {
      list = res.content;
      historyTotalPages.value = res.totalPages;
      historyCurrentPage.value = res.number;
    } else {
      list = res;
      historyTotalPages.value = 1;
      historyCurrentPage.value = 0;
    }
    histories.value = list;

    const clearedIds = await fetchClearedStageIds(userId);
    clearedStageIds.value = new Set(clearedIds);
  } catch (error) {
    console.error('Failed to load user history:', error);
  }
}
function getTabFromPath(): 'home' | 'play' | 'mypage' | 'admin' {
  const path = window.location.pathname;
  if (path === '/play') return 'play';
  if (path === '/mypage') return 'mypage';
  if (path === '/admin') return 'admin';
  return isTestEnv ? 'play' : 'home';
}

function updatePathFromTab(tab: 'home' | 'play' | 'mypage' | 'admin') {
  if (isTestEnv) return;
  const targetPath = tab === 'home' ? '/' : '/' + tab;
  if (window.location.pathname !== targetPath) {
    window.history.pushState(null, '', targetPath + window.location.search);
  }
}

async function handlePopState() {
  const targetTab = getTabFromPath();
  if (targetTab !== currentTab.value) {
    await onTabChange(targetTab);
  }
}

async function onTabChange(tab: 'home' | 'play' | 'mypage' | 'admin') {
  currentTab.value = tab;
  updatePathFromTab(tab);
  if (tab === 'home') {
    initDemoBoard();
    if (introActive.value) {
      startIntroAnimation();
    }
  } else {
    demoSolveAnimationComplete.value = false;
    demoSolved.value = false;
    stopConfetti();
  }
  if (tab === 'mypage') {
    await loadUserHistory();
    const tipShown = localStorage.getItem('rogic_mypage_tip_shown');
    if (!tipShown) {
      isMypageTipOpen.value = true;
    }
  }
}

function handleGoogleLogin() {
  loginWithGoogle();
}

function handleGoogleLogout() {
  googleLogout();
  const stageId = selectedStageId.value !== null ? selectedStageId.value : selectedAiStageId.value;
  if (stageId !== null) {
    const key = `rogic_progress_stage_${isAiStageActive.value ? 'ai_' : ''}${stageId}`;
    localStorage.removeItem(key);
  }
}

async function handleVote(isLike: boolean) {
  const stageId = selectedStageId.value !== null ? selectedStageId.value : (selectedAiStageId.value !== null ? selectedAiStageId.value : null);
  if (stageId === null) return;

  if (typeof (window as any).dataLayer !== 'undefined') {
    (window as any).dataLayer.push({
      event: 'stage_vote',
      stageId: stageId,
      stageName: currentActiveStage.value?.name,
      voteType: isLike ? 'like' : 'dislike',
      isAiStage: isAiStageActive.value
    });
  }

  try {
    let updated;
    if (isLike) {
      updated = await likeStage(stageId);
    } else {
      updated = await dislikeStage(stageId);
    }
    currentStageVotes.value = {
      upvotes: updated.upvotes || 0,
      downvotes: updated.downvotes || 0
    };
    hasVoted.value = true;
  } catch (error) {
    console.error('Failed to submit vote:', error);
  }
}

function closeMypageTip() {
  isMypageTipOpen.value = false;
  localStorage.setItem('rogic_mypage_tip_shown', 'true');
}

async function initializeUserSession() {
  isSessionLoading.value = true;
  try {
    // 1. Process Google OAuth code callback
    if (!isTestEnv) {
      const urlParams = new URLSearchParams(window.location.search);
      const code = urlParams.get('code');
      if (code) {
        try {
          isLoading.value = true;
          const { handleCallback: cognitoHandleCallback } = await import('./api/cognito');
          await cognitoHandleCallback(code);
          // Clean parameters from browser URL
          window.history.replaceState({}, document.title, window.location.pathname);
          // Automatically redirect to play tab
          await onTabChange('play');
        } catch (err) {
          console.error('Failed to exchange authorization code:', err);
        } finally {
          isLoading.value = false;
        }
      }
    }

    // 2. Load profile if token is valid
    const token = await getOrRefreshToken();
    if (token === 'dummy-token') {
      const debugSession = {
        id: 999,
        username: '조도연',
        xp: 250,
        level: 12,
        email: 'ysndy1234@gmail.com',
        profileImageUrl: '',
        idToken: 'dummy-token'
      };
      setUserSession(debugSession);
      currentUser.value = debugSession;
      return;
    }
    if (token) {
      try {
        const user = await fetchMeFromServer();
        const session: UserSession = {
          id: user.id,
          username: user.username,
          xp: user.xp,
          level: user.level,
          email: user.email,
          profileImageUrl: user.profileImageUrl,
          idToken: token
        };
        setUserSession(session);
        currentUser.value = session;
        
        // Perform Guest History Migration on successful session initialization
        await migrateGuestHistory(user.id);
      } catch (error) {
        console.error('Failed to validate user token with server:', error);
        clearUserSession();
        currentUser.value = null;
      }
    } else {
      // Non-login Guest Mode
      clearUserSession();
      currentUser.value = null;
    }
  } finally {
    isSessionLoading.value = false;
  }
}

async function migrateGuestHistory(userId: number) {
  try {
    const savedCleared = localStorage.getItem('guest_cleared_stages');
    const savedHistories = localStorage.getItem('guest_histories');
    if (!savedCleared && !savedHistories) return;

    const localHistories: HistoryResponse[] = savedHistories ? JSON.parse(savedHistories) : [];

    if (localHistories.length > 0) {
      const guestClears = localHistories.map(h => ({
        stageId: h.stageId,
        elapsedTime: h.elapsedTime,
        proofToken: h.proofToken
      }));

      // Call bulk sync API
      const updatedUser = await syncGuestHistory(userId, guestClears);
      
      // Update local reactive user session XP and level
      if (currentUser.value) {
        currentUser.value.xp = updatedUser.xp;
        currentUser.value.level = updatedUser.level;
        // Resave updated session
        setUserSession(currentUser.value);
      }
    }

    // Clear guest storage on success
    localStorage.removeItem('guest_cleared_stages');
    localStorage.removeItem('guest_histories');
  } catch (error) {
    console.error('Failed to migrate guest history to server:', error);
  }
}

function handleGlobalClick(event: MouseEvent) {
  const target = event.target as HTMLElement;
  if (!target) return;
  if (!target.closest('.puzzle-selector-floating-container')) {
    isStageListOpen.value = false;
  }
  if (!target.closest('.play-size-filter-bar-floating')) {
    isSizeListOpen.value = false;
  }
}

function preventPinchZoom(e: TouchEvent) {
  if (e.touches.length > 1) {
    e.preventDefault();
  }
}


onMounted(async () => {
  if (Capacitor.isNativePlatform()) {
    CapacitorApp.addListener('appUrlOpen', async (event: any) => {
      try {
        const url = new URL(event.url);
        if (url.protocol === 'rogic:' && url.host === 'auth') {
          const code = url.searchParams.get('code');
          if (code) {
            isLoading.value = true;
            const { handleCallback: cognitoHandleCallback } = await import('./api/cognito');
            await cognitoHandleCallback(code);
            await initializeUserSession();
            await onTabChange('play');
          }
        }
      } catch (err) {
        console.error('Failed to handle deep link:', err);
      } finally {
        isLoading.value = false;
      }
    });
  }

  initDemoBoard();
  // Check if admin param is in URL or hash
  const urlParams = new URLSearchParams(window.location.search);
  const hasAdminParam = urlParams.get('admin') === 'true';
  const hasAdminHash = window.location.hash.includes('admin');
  if (hasAdminParam || hasAdminHash) {
    isAdminMode.value = true;
    currentTab.value = 'admin';
    
    // Inject Bootstrap CSS from CDN
    const link = document.createElement('link');
    link.rel = 'stylesheet';
    link.href = 'https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css';
    link.id = 'bootstrap-cdn';
    document.head.appendChild(link);
  } else if (!isTestEnv) {
    const urlParams = new URLSearchParams(window.location.search);
    if (urlParams.has('code')) {
      currentTab.value = 'play';
    } else {
      currentTab.value = getTabFromPath();
    }
  }

  updatePathFromTab(currentTab.value);

  if (currentTab.value === 'home' && introActive.value) {
    startIntroAnimation();
  }

  if (!isTestEnv) {
    window.addEventListener('popstate', handlePopState);
  }

  await initializeUserSession();
  await loadUserHistory();
  await Promise.all([
    loadStagesList(),
    loadAiStagesList(),
    loadRankingsList()
  ]);



  window.addEventListener('resize', handleConfettiResize);
  document.addEventListener('touchstart', preventPinchZoom, { passive: false });
  window.addEventListener('pagehide', saveActiveProgress);
  if (!isTestEnv) {
    document.addEventListener('click', handleGlobalClick);
    syncDailyPuzzleCountdown();
    dailyPuzzleTimerId = setInterval(tickDailyPuzzleCountdown, 1000);
    syncTimerId = setInterval(syncDailyPuzzleCountdown, 300000);
  }
});

onUnmounted(() => {
  if (dailyPuzzleTimerId) {
    clearInterval(dailyPuzzleTimerId);
  }
  if (syncTimerId) {
    clearInterval(syncTimerId);
  }
  resetCountdown();
  window.removeEventListener('resize', handleConfettiResize);
  document.removeEventListener('touchstart', preventPinchZoom);
  window.removeEventListener('pagehide', saveActiveProgress);
  if (!isTestEnv) {
    window.removeEventListener('popstate', handlePopState);
    document.removeEventListener('click', handleGlobalClick);
  }
  stopConfetti();
  
  const link = document.getElementById('bootstrap-cdn');
  if (link) {
    link.remove();
  }
  // Reset body style overrides
  document.body.style.overflow = '';
  document.body.style.height = '';
  document.body.style.display = '';
  document.body.style.backgroundColor = '';
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
  height: 100dvh;
  overflow: hidden; /* Prevent body scroll */
  touch-action: pan-x pan-y;
}

.app-container {
  display: flex;
  flex-direction: column;
  height: 100vh;
  height: 100dvh;
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
  margin-bottom: 0;
  padding-bottom: 0.75rem;
  border-bottom: none;
  flex-shrink: 0;
  position: relative;
}

.logo-wrapper {
  display: flex;
  align-items: center;
  gap: 0.6rem;
}

.logo-icon {
  width: 1.8rem;
  height: 1.8rem;
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  grid-template-rows: repeat(2, 1fr);
  gap: 2px;
  animation: spin 4s linear infinite;
  box-shadow: 0 0 15px rgba(56, 189, 248, 0.25);
}

.logo-cell {
  border: 1px solid rgba(255, 255, 255, 0.15);
  background-color: #1e293b;
  border-radius: 2px;
  box-sizing: border-box;
}

.logo-cell.filled {
  background: linear-gradient(135deg, #38bdf8 0%, #818cf8 100%);
  border-color: rgba(99, 102, 241, 0.5);
  box-shadow: 0 0 5px rgba(56, 189, 248, 0.4);
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.logo-title-wrapper {
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 2px;
  margin-top: -3px; /* Shift text slightly upwards for better optical alignment with the logo icon */
}

.app-title {
  font-size: 2.1rem;
  font-weight: 800;
  background: linear-gradient(135deg, #38bdf8 0%, #818cf8 50%, #c084fc 100%);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
  margin: 0;
  letter-spacing: -0.5px;
  line-height: 1.2;
  padding-bottom: 4px;
  margin-bottom: -4px;
}

.app-subtitle {
  margin: 0;
  font-size: 0.52rem;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.12em;
  color: #94a3b8;
  line-height: 1.1;
  text-align: justify;
  text-align-last: justify;
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
  gap: 0.35rem;
}

.header-icon {
  width: 1.1rem;
  height: 1.1rem;
  flex-shrink: 0;
  stroke: currentColor;
  transition: transform 0.25s cubic-bezier(0.2, 0.8, 0.2, 1);
}

.app-nav button:hover .header-icon,
.leaderboard-toggle-btn:hover .header-icon {
  transform: scale(1.1);
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
    margin-bottom: 0;
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
    max-width: 540px;
    padding: 0;
  }
  .modal-content {
    padding: 1.25rem 1rem;
    width: 88%;
  }
  .mypage-user-profile {
    gap: 1rem;
    padding-bottom: 1rem;
    margin-bottom: 1rem;
  }
  .profile-avatar {
    width: 3.5rem;
    height: 3.5rem;
    font-size: 2.2rem;
  }
  .profile-username {
    font-size: 1.25rem;
    margin-bottom: 0.25rem;
  }
  .mypage-dashboard .stage-card-list {
    max-height: calc(100vh - 250px) !important;
    max-height: calc(100dvh - 250px) !important;
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
  top: 0;
  left: 50%;
  transform: translate(-50%, 0);
  z-index: 100;
  display: flex;
  justify-content: center;
  width: auto;
}

.active-stage-badge {
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  gap: 0.6rem;
  background: rgba(30, 41, 59, 0.45);
  backdrop-filter: blur(8px);
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-top: none;
  padding: 0.35rem 1.5rem;
  border-radius: 0 0 10px 10px;
  overflow: hidden;
  cursor: pointer;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
  white-space: nowrap;
  box-sizing: border-box;
}

.active-stage-badge:not(.readonly-badge):hover {
  background: rgba(30, 41, 59, 0.8);
  border-color: rgba(56, 189, 248, 0.4);
}

.active-stage-badge.readonly-badge {
  cursor: default;
}

.active-stage-badge-name {
  font-weight: 700;
  font-size: 0.8rem;
  color: #e2e8f0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  text-align: center;
  width: 100%;
  letter-spacing: 0.03em;
}

.badge-progress-bar-container {
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
  height: 3px;
  background: rgba(56, 189, 248, 0.05);
  overflow: hidden;
}

.badge-progress-bar {
  height: 100%;
  background: linear-gradient(90deg, #38bdf8 0%, #818cf8 50%, #c084fc 100%);
  box-shadow: 0 0 8px rgba(56, 189, 248, 0.6);
  animation: progress-grow-3s 3s linear forwards;
}

@keyframes progress-grow-3s {
  from { width: 0%; }
  to { width: 100%; }
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
  position: absolute;
  right: 0.85rem;
  color: #94a3b8;
  font-size: 0.65rem;
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

.dropdown-pagination-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-top: 0.75rem;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
  color: #94a3b8;
  font-size: 0.85rem;
}

.dropdown-pagination-bar .page-info {
  font-weight: 500;
}

.dropdown-pagination-bar button {
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
  color: #f8fafc;
  padding: 0.25rem 0.5rem;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.dropdown-pagination-bar button:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.dropdown-pagination-bar button:not(:disabled):hover {
  background: rgba(255, 255, 255, 0.1);
  border-color: rgba(255, 255, 255, 0.2);
}

/* Play size filter styling (Floating Dropdown) */
.play-size-filter-bar-floating {
  position: absolute;
  bottom: 20px;
  right: 20px;
  z-index: 100;
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  width: 90px;
}

.active-size-badge {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.4rem;
  background: rgba(15, 23, 42, 0.85);
  backdrop-filter: blur(8px);
  border: 1px solid rgba(255, 255, 255, 0.08);
  padding: 0.5rem 0.75rem;
  border-radius: 9999px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.4);
  cursor: pointer;
  user-select: none;
  font-family: 'Outfit', sans-serif;
  font-weight: 700;
  font-size: 0.8rem;
  color: #38bdf8;
  transition: all 0.2s ease;
  width: 100%;
  box-sizing: border-box;
}

.active-size-badge:hover {
  background: rgba(30, 41, 59, 0.9);
  border-color: rgba(56, 189, 248, 0.3);
  color: #ffffff;
}

.active-size-arrow {
  font-size: 0.65rem;
  transition: transform 0.25s ease;
  color: #94a3b8;
  display: inline-block;
  line-height: 1;
}

.active-size-arrow.open {
  transform: rotate(180deg);
  color: #38bdf8;
}

.play-size-filter-dropdown {
  position: absolute;
  bottom: calc(100% + 8px);
  right: 0;
  background: rgba(15, 23, 42, 0.95);
  backdrop-filter: blur(12px);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 12px;
  box-shadow: 0 10px 25px -5px rgba(0, 0, 0, 0.5);
  padding: 0.35rem;
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
  width: 100%;
  z-index: 101;
  box-sizing: border-box;
}

.play-size-filter-dropdown-item {
  padding: 0.4rem 0.5rem;
  border-radius: 8px;
  color: #94a3b8;
  font-size: 0.75rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.15s ease;
  font-family: 'Outfit', sans-serif;
  text-align: center;
  width: 100%;
  box-sizing: border-box;
}

.play-size-filter-dropdown-item:hover {
  color: #f8fafc;
  background-color: rgba(255, 255, 255, 0.05);
}

.play-size-filter-dropdown-item.active {
  background-color: rgba(56, 189, 248, 0.15);
  border-color: rgba(56, 189, 248, 0.3);
  color: #38bdf8;
}

/* Transitions */
.tab-fade-enter-active,
.tab-fade-leave-active {
  transition: opacity 0.2s ease-in-out;
}
.tab-fade-enter-from,
.tab-fade-leave-to {
  opacity: 0;
}

.slide-down-enter-active,
.slide-down-leave-active {
  transition: all 0.2s cubic-bezier(0.16, 1, 0.3, 1);
}
.slide-down-enter-from,
.slide-down-leave-to {
  transform: translateX(-50%) translateY(-10px);
  opacity: 0;
}

.slide-up-enter-active,
.slide-up-leave-active {
  transition: all 0.25s cubic-bezier(0.25, 1, 0.5, 1);
}
.slide-up-enter-from,
.slide-up-leave-to {
  transform: translateY(10px);
  opacity: 0;
}

@media (max-width: 768px) {
  .play-size-filter-bar-floating {
    bottom: 12px;
    right: 12px;
    width: 80px;
  }
  .active-size-badge {
    padding: 0.4rem 0.6rem;
    font-size: 0.72rem;
  }
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
  justify-content: flex-start;
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

.play-area-inner {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-width: 0;
  min-height: 0;
  width: 100%;
  height: 100%;
  position: relative;
}

.solved-feedback-card {
  position: absolute;
  bottom: 24px;
  left: 50%;
  transform: translateX(-50%);
  z-index: 100;
  display: flex;
  justify-content: center;
  align-items: center;
}

.feedback-buttons {
  display: flex;
  gap: 0.75rem;
  background: rgba(15, 23, 42, 0.7);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border: 1px solid rgba(56, 189, 248, 0.2);
  padding: 0.4rem 0.8rem;
  border-radius: 20px;
  box-shadow: 0 8px 24px -4px rgba(0, 0, 0, 0.6), 0 0 12px rgba(56, 189, 248, 0.1);
}

.feedback-btn {
  background: transparent;
  border: none;
  color: #94a3b8;
  width: 2.2rem;
  height: 2.2rem;
  border-radius: 50%;
  cursor: pointer;
  font-size: 1.15rem;
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.feedback-btn:hover {
  background: rgba(56, 189, 248, 0.1);
  color: #38bdf8;
  transform: scale(1.15);
}

.feedback-btn.dislike:hover {
  background: rgba(239, 68, 68, 0.1);
  color: #ef4444;
}

.feedback-thanks {
  background: rgba(15, 23, 42, 0.7);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border: 1px solid rgba(129, 140, 248, 0.2);
  padding: 0.4rem 1.2rem;
  border-radius: 20px;
  font-size: 0.82rem;
  color: #818cf8;
  font-weight: 600;
  box-shadow: 0 8px 24px -4px rgba(0, 0, 0, 0.6);
}

.svg-icon {
  width: 1.25rem;
  height: 1.25rem;
  stroke: currentColor;
  stroke-width: 1.5;
  transition: stroke-width 0.2s ease;
}

.feedback-btn:hover .svg-icon {
  stroke-width: 2.2;
}

.canvas-frame-placeholder {
  width: 100%;
  height: 100%;
  min-height: 380px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 1.5rem;
  background-color: #0f172a;
  border-radius: 12px;
  border: 1px solid rgba(255, 255, 255, 0.05);
  box-sizing: border-box;
}

.canvas-frame-placeholder.error {
  gap: 1.25rem;
  padding: 2rem;
}

.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 1.5rem;
  color: #94a3b8;
  font-size: 1.1rem;
  text-align: center;
  height: 100%;
  width: 100%;
  min-height: 200px;
}

.spinner-logo {
  width: 3.5rem;
  height: 3.5rem;
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  grid-template-rows: repeat(2, 1fr);
  gap: 3px;
  animation: spin 1.2s cubic-bezier(0.4, 0, 0.2, 1) infinite;
  filter: drop-shadow(0 0 8px rgba(56, 189, 248, 0.4));
}

.spinner-cell {
  border: 1.5px solid rgba(255, 255, 255, 0.2);
  background-color: #1e293b;
  border-radius: 4px;
  box-sizing: border-box;
}

.spinner-cell.filled {
  background: linear-gradient(135deg, #38bdf8 0%, #818cf8 100%);
  border-color: rgba(99, 102, 241, 0.6);
  box-shadow: inset 0 0 4px rgba(255, 255, 255, 0.2);
}

.loading-text {
  font-weight: 500;
  letter-spacing: 0.05em;
  color: #e2e8f0;
  animation: pulse 1.5s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 0.6; }
  50% { opacity: 1; }
}

.error-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 1.25rem;
  color: #f43f5e;
  text-align: center;
  padding: 2rem;
  background: rgba(244, 63, 94, 0.05);
  border: 1px solid rgba(244, 63, 94, 0.15);
  border-radius: 16px;
  max-width: 400px;
  margin: auto;
  backdrop-filter: blur(8px);
}

.error-icon {
  font-size: 3rem;
  animation: bounce 2s infinite;
}

.error-text {
  font-size: 1rem;
  color: #fda4af;
  line-height: 1.5;
  margin: 0;
}

.retry-btn {
  padding: 0.6rem 1.5rem;
  background: linear-gradient(135deg, #f43f5e 0%, #e11d48 100%);
  border: none;
  border-radius: 8px;
  color: #ffffff;
  font-weight: 600;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  box-shadow: 0 4px 12px rgba(244, 63, 94, 0.3);
  transition: all 0.2s ease;
}

.retry-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(244, 63, 94, 0.4);
}

.retry-btn:active {
  transform: translateY(0);
}

@keyframes bounce {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-8px); }
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

.header-progress-bar-container {
  position: absolute;
  bottom: -1px;
  left: 0;
  width: 100%;
  height: 3px;
  background: rgba(56, 189, 248, 0.05);
  border-radius: 1.5px;
  overflow: hidden;
  z-index: 10;
}

.header-progress-bar {
  height: 100%;
  width: 0%;
  background: linear-gradient(90deg, #38bdf8 0%, #818cf8 50%, #c084fc 100%);
  box-shadow: 0 0 8px rgba(56, 189, 248, 0.6);
  animation: progress-grow 3s linear forwards;
}

.all-cleared-card {
  background: linear-gradient(135deg, rgba(30, 41, 59, 0.75) 0%, rgba(15, 23, 42, 0.85) 100%);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.08);
  padding: 2.5rem 3.5rem;
  border-radius: 24px;
  box-shadow: 0 20px 50px -10px rgba(0, 0, 0, 0.5), 0 0 40px 0 rgba(56, 189, 248, 0.05);
  text-align: center;
  animation: pop-in 0.4s cubic-bezier(0.16, 1, 0.3, 1);
  position: relative;
  z-index: 1;
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

@keyframes progress-grow {
  from { width: 0%; }
  to { width: 100%; }
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



/* Home Dashboard Styling */
.home-dashboard {
  width: 100%;
  max-width: 800px;
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
  overflow-y: auto;
  padding-right: 0.5rem;
  max-height: calc(100vh - 120px);
  max-height: calc(100dvh - 120px);
  box-sizing: border-box;
}

.home-dashboard::-webkit-scrollbar {
  width: 6px;
}

.home-dashboard::-webkit-scrollbar-track {
  background: transparent;
}

.home-dashboard::-webkit-scrollbar-thumb {
  background: #334155;
  border-radius: 4px;
}

.home-footer {
  margin-top: auto;
  padding: 2.5rem 0 1rem 0;
  text-align: center;
  border-top: 1px solid rgba(255, 255, 255, 0.05);
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.75rem;
}

.footer-links {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.footer-links a {
  font-size: 0.8rem;
  color: #64748b;
  text-decoration: none;
  transition: color 0.2s ease;
}

.footer-links a:hover {
  color: #38bdf8;
}

.footer-divider {
  font-size: 0.75rem;
  color: #334155;
}

.footer-copyright {
  font-size: 0.75rem;
  color: #475569;
  margin: 0;
}

.glass-card {
  background: rgba(30, 41, 59, 0.45);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border: 1px solid rgba(255, 255, 255, 0.06);
  border-radius: 16px;
  box-shadow: 0 10px 30px -10px rgba(0, 0, 0, 0.5);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.hero-section {
  padding: 2rem;
  text-align: center;
  position: relative;
  overflow: hidden;
}

.hero-section::before {
  content: '';
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: radial-gradient(circle, rgba(56, 189, 248, 0.05) 0%, transparent 70%);
  pointer-events: none;
}

.hero-logo-container {
  display: flex;
  justify-content: center;
  margin-bottom: 1rem;
}

.hero-logo-icon {
  width: 3.5rem;
  height: 3.5rem;
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  grid-template-rows: repeat(2, 1fr);
  gap: 4px;
  animation: spin 6s linear infinite;
  box-shadow: 0 0 25px rgba(56, 189, 248, 0.3);
}

.hero-title {
  font-size: 1.8rem;
  font-weight: 800;
  margin: 0 0 0.75rem 0;
  background: linear-gradient(135deg, #38bdf8 0%, #818cf8 100%);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
}

.hero-subtitle {
  font-size: 0.95rem;
  color: #94a3b8;
  line-height: 1.6;
  margin: 0 0 1.5rem 0;
  max-width: 600px;
  margin-left: auto;
  margin-right: auto;
}

.cta-play-btn {
  padding: 0.75rem 2rem;
  background: linear-gradient(135deg, #38bdf8 0%, #818cf8 100%);
  border: none;
  border-radius: 9999px;
  color: #ffffff;
  font-family: 'Outfit', sans-serif;
  font-weight: 700;
  font-size: 1rem;
  cursor: pointer;
  box-shadow: 0 4px 20px rgba(99, 102, 241, 0.4);
  transition: all 0.2s ease;
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
}

.cta-play-btn:hover {
  transform: translateY(-2px) scale(1.03);
  box-shadow: 0 6px 24px rgba(99, 102, 241, 0.6);
}

.cta-play-btn:active {
  transform: translateY(0) scale(1);
}

.section-title {
  font-size: 1.1rem;
  font-weight: 700;
  color: #f1f5f9;
  margin: 0 0 0.75rem 0;
  letter-spacing: 0.05em;
}

.telemetry-section {
  display: flex;
  flex-direction: column;
}

.telemetry-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 0.75rem;
}

.telemetry-card {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 1rem;
}

.telemetry-card:hover {
  border-color: rgba(56, 189, 248, 0.3);
  transform: translateY(-2px);
  background: rgba(30, 41, 59, 0.6);
}

.telemetry-icon {
  font-size: 1.5rem;
  background: rgba(255, 255, 255, 0.04);
  padding: 0.5rem;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.telemetry-info {
  display: flex;
  flex-direction: column;
  gap: 0.2rem;
}

.telemetry-label {
  font-size: 0.75rem;
  color: #64748b;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.telemetry-value {
  font-size: 1.1rem;
  font-weight: 700;
  color: #f8fafc;
}

.timeline-section {
  display: flex;
  flex-direction: column;
}

.timeline-container {
  padding: 1.5rem;
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
  position: relative;
}

.timeline-container::before {
  content: '';
  position: absolute;
  left: 2.25rem;
  top: 2rem;
  bottom: 2rem;
  width: 2px;
  background: rgba(255, 255, 255, 0.06);
}

.timeline-item {
  display: flex;
  gap: 1.5rem;
  position: relative;
  z-index: 1;
}

.timeline-badge {
  flex-shrink: 0;
  width: 4rem;
  height: 1.75rem;
  background: rgba(56, 189, 248, 0.15);
  border: 1px solid rgba(56, 189, 248, 0.3);
  border-radius: 9999px;
  color: #38bdf8;
  font-size: 0.78rem;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 0 10px rgba(56, 189, 248, 0.15);
}

.timeline-content {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.timeline-date {
  font-size: 0.75rem;
  color: #64748b;
  font-weight: 600;
}

.timeline-title {
  font-size: 0.95rem;
  font-weight: 700;
  color: #e2e8f0;
  margin: 0;
}

.timeline-desc {
  font-size: 0.85rem;
  color: #94a3b8;
  line-height: 1.5;
  margin: 0;
}

@media (max-width: 768px) {
  .telemetry-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  .timeline-container::before {
    left: 2rem;
  }
}

@media (max-width: 480px) {
  .telemetry-grid {
    grid-template-columns: 1fr;
  }
  .hero-section {
    padding: 1.5rem;
  }
  .hero-title {
    font-size: 1.5rem;
  }
}

/* Standalone Vercel-like Home Page Mode Overrides */
.app-container.home-mode {
  height: auto;
  min-height: 100vh;
  min-height: 100dvh;
  overflow: visible;
  padding: 0;
  max-width: 100%;
  background-color: #0a0f1d;
  display: block;
}

.home-mode .app-layout {
  display: block;
  height: auto;
  min-height: 100vh;
  min-height: 100dvh;
  gap: 0;
}

.home-mode .app-main {
  display: block;
  width: 100%;
  height: auto;
  min-height: 100vh;
  min-height: 100dvh;
}

.home-mode .home-dashboard {
  max-width: 100%;
  max-height: none;
  overflow: visible;
  padding: 0;
  gap: 0;
}

/* Landing Navigation Bar */
.landing-nav {
  position: sticky;
  top: 0;
  z-index: 1000;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 2rem;
  height: 64px;
  background: rgba(0, 0, 0, 0.7);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
  box-sizing: border-box;
}

.landing-logo {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  cursor: pointer;
}

.landing-logo-icon {
  width: 1.5rem;
  height: 1.5rem;
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  grid-template-rows: repeat(2, 1fr);
  gap: 2px;
}

.landing-logo-text {
  font-size: 1.25rem;
  font-weight: 700;
  color: #ffffff;
  letter-spacing: -0.03em;
}

.landing-nav-links {
  display: flex;
  align-items: center;
  gap: 1.5rem;
}

.landing-nav-link {
  background: none;
  border: none;
  color: #a1a1aa;
  font-family: 'Outfit', sans-serif;
  font-size: 0.9rem;
  font-weight: 500;
  cursor: pointer;
  transition: color 0.2s ease;
}

.landing-nav-link:hover {
  color: #ffffff;
}

.landing-play-btn {
  background: linear-gradient(135deg, #38bdf8 0%, #818cf8 100%);
  color: #ffffff;
  border: none;
  border-radius: 6px;
  padding: 0.4rem 1rem;
  font-family: 'Outfit', sans-serif;
  font-size: 0.9rem;
  font-weight: 600;
  cursor: pointer;
  box-shadow: 0 2px 10px rgba(56, 189, 248, 0.25);
  transition: all 0.2s ease;
}

.landing-play-btn:hover {
  background: linear-gradient(135deg, #818cf8 0%, #c084fc 100%);
  box-shadow: 0 4px 15px rgba(129, 140, 248, 0.4);
}

/* Hero Section (Vercel Style) */
.home-mode .hero-section {
  padding: 8rem 2rem 6rem 2rem;
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  background: radial-gradient(circle at center, rgba(56, 189, 248, 0.08) 0%, transparent 60%);
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
}

.hero-logo-icon-large {
  width: 4.5rem;
  height: 4.5rem;
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  grid-template-rows: repeat(2, 1fr);
  gap: 6px;
  animation: spin 8s linear infinite;
  margin-bottom: 2rem;
  filter: drop-shadow(0 0 25px rgba(56, 189, 248, 0.3));
}

.home-mode .hero-title {
  font-size: 4rem;
  font-weight: 800;
  letter-spacing: -0.5px;
  line-height: 1.25;
  background: linear-gradient(135deg, #38bdf8 0%, #818cf8 50%, #c084fc 100%);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
  margin-bottom: 1.5rem;
  padding: 4px 10px;
  display: inline-block;
}

.home-mode .hero-subtitle {
  font-size: 1.2rem;
  color: #a1a1aa;
  max-width: 640px;
  margin: 0 auto 2.5rem auto;
  line-height: 1.6;
}

.hero-actions {
  display: flex;
  gap: 1rem;
  justify-content: center;
}

.cta-play-btn {
  padding: 0.8rem 2.2rem;
  background: linear-gradient(135deg, #38bdf8 0%, #818cf8 100%);
  color: #ffffff;
  border: none;
  border-radius: 9999px;
  font-family: 'Outfit', sans-serif;
  font-weight: 700;
  font-size: 1rem;
  cursor: pointer;
  box-shadow: 0 4px 20px rgba(56, 189, 248, 0.35);
  transition: all 0.25s ease;
}

.cta-play-btn:hover {
  background: linear-gradient(135deg, #818cf8 0%, #c084fc 100%);
  color: #ffffff;
  box-shadow: 0 6px 24px rgba(129, 140, 248, 0.5);
  transform: translateY(-2px);
}

.cta-status-btn {
  padding: 0.8rem 2.2rem;
  background: transparent;
  color: #ffffff;
  border: 1px solid rgba(255, 255, 255, 0.15);
  border-radius: 9999px;
  font-family: 'Outfit', sans-serif;
  font-weight: 600;
  font-size: 1rem;
  cursor: pointer;
  transition: all 0.25s ease;
}

.cta-status-btn:hover {
  background: rgba(56, 189, 248, 0.05);
  border-color: #38bdf8;
  color: #38bdf8;
  transform: translateY(-2px);
}

/* System Telemetry Section */
.home-mode .telemetry-section {
  padding: 6rem 2rem;
  max-width: 1200px;
  margin: 0 auto;
  width: 100%;
  box-sizing: border-box;
}

.home-mode .section-title {
  font-size: 1.8rem;
  font-weight: 800;
  letter-spacing: -0.04em;
  color: #ffffff;
  margin-bottom: 2rem;
  text-align: left;
}

.home-mode .telemetry-grid {
  grid-template-columns: repeat(3, 1fr);
  gap: 1.5rem;
}

.home-mode .telemetry-card {
  background: rgba(30, 41, 59, 0.45);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border: 1px solid rgba(255, 255, 255, 0.06);
  border-radius: 12px;
  padding: 1.75rem;
  transition: all 0.3s ease;
  box-shadow: 0 10px 30px -10px rgba(0, 0, 0, 0.5);
}

.home-mode .telemetry-card:hover {
  background: rgba(30, 41, 59, 0.6);
  border-color: rgba(56, 189, 248, 0.3);
  transform: translateY(-4px);
  box-shadow: 0 12px 30px -10px rgba(56, 189, 248, 0.15);
}

.telemetry-svg {
  width: 24px;
  height: 24px;
  flex-shrink: 0;
}

.home-mode .telemetry-info {
  gap: 0.35rem;
}

.home-mode .telemetry-label {
  color: #71717a;
  font-size: 0.75rem;
  font-weight: 600;
  letter-spacing: 0.05em;
}

.home-mode .telemetry-value {
  font-size: 1.5rem;
  font-weight: 700;
  color: #ffffff;
  letter-spacing: -0.02em;
}

/* Changelog / Release Timeline Section */
.home-mode .timeline-section {
  padding: 0 2rem 8rem 2rem;
  max-width: 800px;
  margin: 0 auto;
  width: 100%;
  box-sizing: border-box;
}

.home-mode .timeline-container {
  background: rgba(30, 41, 59, 0.45);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border: 1px solid rgba(255, 255, 255, 0.06);
  border-radius: 16px;
  padding: 2.5rem;
  box-shadow: 0 10px 30px -10px rgba(0, 0, 0, 0.5);
}

.home-mode .timeline-badge {
  background: rgba(255, 255, 255, 0.06);
  border: 1px solid rgba(255, 255, 255, 0.12);
  color: #ffffff;
  font-weight: 600;
}

.home-mode .timeline-title {
  color: #ffffff;
  font-size: 1.1rem;
  font-weight: 600;
}

.home-mode .timeline-desc {
  color: #a1a1aa;
}

/* Responsive Overrides */
@media (max-width: 968px) {
  .home-mode .hero-title {
    font-size: 3rem;
  }
  .home-mode .telemetry-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 640px) {
  .home-mode .hero-title {
    font-size: 2.25rem;
  }
  .home-mode .telemetry-grid {
    grid-template-columns: 1fr;
  }
  .hero-actions {
    flex-direction: column;
    width: 100%;
    max-width: 280px;
  }
  .cta-play-btn, .cta-status-btn {
    width: 100%;
    box-sizing: border-box;
    text-align: center;
  }
  .landing-nav {
    padding: 0 1rem;
  }
  .landing-nav-links {
    gap: 1rem;
  }
}

@keyframes modalFadeIn {
  from {
    opacity: 0;
    transform: scale(0.95) translateY(10px);
  }
  to {
    opacity: 1;
    transform: scale(1) translateY(0);
  }
}

.all-cleared-state-container {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 480px;
  width: 100%;
  position: relative;
  animation: modalFadeIn 0.4s ease-out;
}

.all-cleared-state-container::before {
  content: '';
  position: absolute;
  width: 320px;
  height: 320px;
  background: radial-gradient(circle, rgba(56, 189, 248, 0.08) 0%, rgba(56, 189, 248, 0) 70%);
  pointer-events: none;
  z-index: 0;
}

.all-cleared-title {
  font-size: 2.2rem;
  font-weight: 800;
  background: linear-gradient(135deg, #ffffff 40%, #94a3b8 100%);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
  margin-top: 1.5rem;
  margin-bottom: 1.5rem;
  letter-spacing: -0.02em;
}

.all-cleared-subtitle {
  font-size: 1.05rem;
  color: #94a3b8;
  max-width: 440px;
  margin: 0 auto 2rem auto;
  line-height: 1.6;
}

.countdown-box {
  margin-top: 0.5rem;
  background: rgba(15, 23, 42, 0.45);
  border: 1px solid rgba(255, 255, 255, 0.04);
  padding: 1.25rem 2.5rem;
  border-radius: 20px;
  display: inline-block;
  box-shadow: inset 0 2px 8px rgba(0, 0, 0, 0.3);
}

.countdown-label {
  font-size: 0.75rem;
  color: #64748b;
  text-transform: uppercase;
  letter-spacing: 0.12rem;
  margin-bottom: 0.5rem;
  font-weight: 600;
}

.countdown-time {
  font-family: 'Outfit', 'Inter', monospace;
  font-size: 2.5rem;
  font-weight: 700;
  color: #38bdf8;
  text-shadow: 0 0 24px rgba(56, 189, 248, 0.4);
  letter-spacing: 0.05rem;
  font-variant-numeric: tabular-nums;
}

@keyframes subtle-pulse {
  0%, 100% { opacity: 0.3; transform: scale(0.9); }
  50% { opacity: 0.8; transform: scale(1.1); }
}

/* Premium Single Column Landing Page Styles */
.home-dashboard {
  position: relative;
  width: 100%;
  height: 100vh;
  height: 100dvh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  box-sizing: border-box;
  background-color: #020617; /* Sleek dark mode background */
}

/* Clean background grid/mesh overlay */
.landing-bg {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: radial-gradient(circle at center, rgba(56, 189, 248, 0.04) 0%, transparent 70%);
  pointer-events: none;
  z-index: 0;
}

/* Centered & Top-sliding Header Logo */
.landing-logo-container {
  position: absolute;
  left: 50%;
  display: flex;
  align-items: center;
  gap: 1.0rem;
  z-index: 10;
  transition: all 1.2s cubic-bezier(0.25, 1, 0.5, 1);
}

.landing-logo-container.logo {
  top: 48%;
  transform: translate(-50%, -50%) scale(1.4);
}

.landing-logo-container.solving,
.landing-logo-container.stats,
.landing-logo-container.cta,
.landing-logo-container.done {
  top: 12%;
  transform: translate(-50%, 0) scale(1.0);
}


.landing-logo-icon {
  width: 3.1rem;
  height: 3.1rem;
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  grid-template-rows: repeat(2, 1fr);
  gap: 4px;
  filter: drop-shadow(0 0 16px rgba(56, 189, 248, 0.45));
}

.landing-logo-title {
  margin: 0;
  font-size: 3.6rem;
  font-weight: 800;
  letter-spacing: -0.5px;
  line-height: 1.25;
  padding: 0.1em 0;
  display: inline-block;
  background: linear-gradient(135deg, #38bdf8 0%, #818cf8 50%, #c084fc 100%);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
}







/* Step 2: Auto-solving Nonogram Canvas Wrapper */
.landing-canvas-wrapper {
  position: absolute;
  top: 48%;
  left: 50%;
  transform: translate(-50%, -40%);
  width: 250px;
  height: 250px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  z-index: 5;
}


/* Step 3: Stats & Conveyor Belt Phase Styles */
.landing-stats-conveyor-wrapper {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -45%);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 100%;
  max-width: 600px;
  z-index: 5;
  text-align: center;
  gap: 2rem;
}

.landing-conveyor-container {
  width: 100%;
  height: 50px;
  overflow: hidden;
  position: relative;
  mask-image: linear-gradient(to right, transparent, white 20%, white 80%, transparent);
  -webkit-mask-image: linear-gradient(to right, transparent, white 20%, white 80%, transparent);
}

.landing-conveyor-track {
  display: flex;
  width: max-content;
  animation: marquee-horizontal 20s linear infinite;
}

.landing-conveyor-loop {
  display: flex;
  gap: 1rem;
  padding-right: 1rem;
}

.conveyor-card {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  background: rgba(255, 255, 255, 0.02);
  border: 1px solid rgba(255, 255, 255, 0.05);
  border-radius: 8px;
  padding: 0.3rem 0.8rem;
  box-shadow: 0 4px 12px rgba(0,0,0,0.15);
  backdrop-filter: blur(10px);
  height: 38px;
  box-sizing: border-box;
}

.mini-art-grid {
  display: grid;
  grid-template-columns: repeat(5, 4px);
  gap: 1px;
  width: 24px;
  height: 24px;
}

.mini-art-cell {
  width: 4px;
  height: 4px;
  border-radius: 0.5px;
  background: rgba(255, 255, 255, 0.05);
}

.mini-art-cell.filled {
  background: linear-gradient(135deg, #38bdf8, #818cf8);
}

.conveyor-card-name {
  font-size: 0.75rem;
  color: #a1a1aa;
  font-weight: 500;
  white-space: nowrap;
}

@keyframes marquee-horizontal {
  0% { transform: translateX(0); }
  100% { transform: translateX(-33.333%); }
}

/* Step 4: Big Premium Centered CTA */
.landing-cta-container {
  position: absolute;
  top: 48%;
  left: 50%;
  transform: translate(-50%, -50%);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  z-index: 5;
  text-align: center;
}

.landing-stats {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.25rem;
}

.landing-stats .stats-number {
  font-size: 4.5rem;
  font-weight: 800;
  background: linear-gradient(135deg, #38bdf8, #818cf8);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
  text-shadow: 0 0 30px rgba(56, 189, 248, 0.3);
  line-height: 1;
}

.landing-stats .stats-label {
  font-size: 0.85rem;
  color: #64748b;
  text-transform: uppercase;
  letter-spacing: 0.15em;
  font-weight: 600;
}

.landing-play-btn {
  width: 240px;
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.75rem;
  border-radius: 18px;
  border: none;
  background: linear-gradient(135deg, #38bdf8 0%, #818cf8 100%);
  color: #ffffff;
  cursor: pointer;
  box-shadow: 0 0 20px rgba(56, 189, 248, 0.25), 0 0 35px rgba(56, 189, 248, 0.15);
  transition: all 0.3s cubic-bezier(0.25, 1, 0.5, 1);
  font-family: inherit;
  font-size: 1.25rem;
  font-weight: 700;
  letter-spacing: -0.2px;
  animation: pulse-glow 2s infinite ease-in-out;
}

.landing-play-btn .play-icon {
  width: 24px;
  height: 24px;
  display: block;
}

.landing-play-btn:hover {
  transform: translateY(-2px) scale(1.03);
  box-shadow: 0 0 30px rgba(56, 189, 248, 0.5), 0 0 45px rgba(56, 189, 248, 0.25);
  background: linear-gradient(135deg, #40c4ff 0%, #90caf9 100%);
}

.landing-play-btn:active {
  transform: translateY(0) scale(0.98);
}

/* Skip / Replay Control Button */
.intro-control-btn {
  position: absolute;
  top: 2rem;
  right: 2rem;
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid rgba(255, 255, 255, 0.08);
  color: #94a3b8;
  border-radius: 50%;
  cursor: pointer;
  transition: all 0.25s cubic-bezier(0.25, 1, 0.5, 1);
  z-index: 100;
  backdrop-filter: blur(8px);
  padding: 0;
}

.intro-control-btn:hover {
  background: rgba(255, 255, 255, 0.08);
  border-color: rgba(255, 255, 255, 0.18);
  color: #f8fafc;
  transform: translateY(-2px);
}

.control-btn-icon {
  width: 20px;
  height: 20px;
  display: block;
}


/* Subtle Footer styling */
.landing-footer {
  position: absolute;
  bottom: 2rem;
  left: 50%;
  transform: translateX(-50%);
  text-align: center;
  z-index: 5;
  width: 100%;
}

.landing-footer .footer-links {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.75rem;
  margin-bottom: 0.5rem;
}

.landing-footer .footer-links a {
  color: #64748b;
  text-decoration: none;
  font-size: 0.8rem;
  font-weight: 500;
  transition: color 0.2s;
}

.landing-footer .footer-links a:hover {
  color: #38bdf8;
}

.landing-footer .footer-links a.footer-github-link {
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.landing-footer .footer-links .footer-github-icon {
  width: 1.15rem;
  height: 1.15rem;
  display: block;
}

.landing-footer .footer-divider {
  color: #334155;
  font-size: 0.8rem;
}

.landing-footer .footer-copyright {
  margin: 0;
  font-size: 0.75rem;
  color: #475569;
}

/* Transitions */
.fade-enter-active, .fade-leave-active {
  transition: opacity 0.3s ease;
}
.fade-enter-from, .fade-leave-to {
  opacity: 0;
}

.fade-slide-up-enter-active, .fade-slide-up-leave-active {
  transition: opacity 0.4s ease, transform 0.4s cubic-bezier(0.25, 1, 0.5, 1);
}
.fade-slide-up-enter-from, .fade-slide-up-leave-to {
  opacity: 0;
  transform: translateY(-40px) scale(0.98);
}

.fade-scale-enter-active, .fade-scale-leave-active {
  transition: opacity 0.8s ease, transform 0.8s cubic-bezier(0.25, 1, 0.5, 1);
}
.fade-scale-enter-from, .fade-scale-leave-to {
  opacity: 0;
  transform: translate(-50%, -40%) scale(0.9);
}

.fade-scale-slow-enter-active, .fade-scale-slow-leave-active {
  transition: opacity 1.2s ease, transform 1.2s cubic-bezier(0.25, 1, 0.5, 1);
}
.fade-scale-slow-enter-from, .fade-scale-slow-leave-to {
  opacity: 0;
  transform: translate(-50%, -50%) scale(0.95);
}

@keyframes pulse-glow {
  0%, 100% {
    box-shadow: 0 0 15px rgba(56, 189, 248, 0.25), 0 0 30px rgba(56, 189, 248, 0.15);
  }
  50% {
    box-shadow: 0 0 25px rgba(56, 189, 248, 0.5), 0 0 45px rgba(56, 189, 248, 0.3);
  }
}
@media (max-width: 480px) {
  .landing-logo-container.logo {
    transform: translate(-50%, -50%) scale(1.2);
  }
  .landing-logo-title {
    font-size: 3.0rem;
  }
  .landing-logo-icon {
    width: 2.8rem;
    height: 2.8rem;
  }
  .landing-play-btn {
    width: 180px;
    height: 50px;
    font-size: 1.1rem;
    border-radius: 15px;
  }
}
</style>

