<template>
  <!-- Admin View (Bootstrap-based back-office) -->
  <div v-if="isAdminMode" class="container-fluid py-4 min-vh-100 bg-light text-dark admin-backoffice-view" style="font-family: 'Outfit', sans-serif;">
    <!-- Show login screen if not logged in -->
    <div v-if="!isAdminLogged" class="d-flex justify-content-center align-items-center" style="min-height: 80vh;">
      <div class="card shadow-lg border-0 p-4 admin-login-card" style="max-width: 400px; width: 100%; border-radius: 12px; background: rgba(255, 255, 255, 0.95);">
        <div class="text-center mb-4">
          <span class="fs-1">🔑</span>
          <h3 class="fw-bold mt-2 text-dark">Admin Console</h3>
          <p class="text-muted">Please log in to continue</p>
        </div>
        <form @submit.prevent="handleAdminLogin" class="admin-login-form">
          <div class="mb-3">
            <label class="form-label fw-bold text-dark">Username</label>
            <input type="text" v-model="adminUsernameInput" class="form-control admin-username-input" placeholder="Enter username" required />
          </div>
          <div class="mb-3">
            <label class="form-label fw-bold text-dark">Password</label>
            <input type="password" v-model="adminPasswordInput" class="form-control admin-password-input" placeholder="Enter password" required />
          </div>
          <div v-if="loginError" class="alert alert-danger text-center py-2 admin-login-error" role="alert">
            {{ loginError }}
          </div>
          <button type="submit" class="btn btn-dark w-100 py-2 fw-bold mt-2 admin-login-submit-btn">Login</button>
        </form>
      </div>
    </div>

    <!-- If logged in, show the admin console -->
    <div v-else class="admin-console-content">
      <!-- Bootstrap Navbar -->
      <nav class="navbar navbar-dark bg-dark rounded shadow-sm mb-4 px-4 py-3 d-flex justify-content-between align-items-center">
        <span class="navbar-brand mb-0 h1 d-flex align-items-center gap-2">
          <span class="fs-4">🔑</span> rogic.io Admin Console
        </span>
        <div class="d-flex align-items-center gap-3 text-light">
          <span class="badge bg-secondary p-2">Back Office</span>
          <div class="d-flex align-items-center gap-1 admin-ai-size-selectors">
            <span class="small text-light" style="font-size: 0.8rem;">AI Size:</span>
            <select v-model.number="adminAiWidth" class="form-select form-select-sm text-dark admin-ai-width-select" style="width: 65px; padding: 2px 5px; font-size: 0.8rem;">
              <option value="5">5</option>
              <option value="10">10</option>
              <option value="15">15</option>
              <option value="20">20</option>
              <option value="30">30</option>
            </select>
            <span class="small text-light" style="font-size: 0.8rem;">x</span>
            <select v-model.number="adminAiHeight" class="form-select form-select-sm text-dark admin-ai-height-select" style="width: 65px; padding: 2px 5px; font-size: 0.8rem;">
              <option value="5">5</option>
              <option value="10">10</option>
              <option value="15">15</option>
              <option value="20">20</option>
              <option value="30">30</option>
            </select>
          </div>
          <button class="btn btn-outline-light btn-sm admin-ai-gen-btn" @click="handleAdminAiGenerate">
            ✨ Generate AI Stage (Gemini)
          </button>
          <button class="btn btn-danger btn-sm admin-logout-btn" @click="handleAdminLogout">
            🚪 Logout
          </button>
        </div>
      </nav>

      <!-- Main Back-office Layout Grid -->
      <div class="row g-4">
        <!-- Left Column: Manage Puzzles -->
        <div class="col-lg-7">
          <div class="card shadow-sm border-0 h-100">
            <div class="card-header bg-dark text-white py-3">
              <h5 class="card-title mb-0 d-flex justify-content-between align-items-center">
                <span>📋 Manage Puzzles</span>
                <span class="badge bg-primary">{{ filteredAndSortedAdminStages.length }} / {{ adminStages.length }}</span>
              </h5>
            </div>
            <div class="card-body p-0">
              <!-- Filtering Controls -->
              <div class="p-3 bg-light border-bottom d-flex flex-wrap gap-3 align-items-center">
                <div class="flex-grow-1" style="min-width: 200px;">
                  <input 
                    type="text" 
                    v-model="adminSearchQuery" 
                    class="form-control form-control-sm admin-search-input" 
                    placeholder="🔍 Search by name..." 
                  />
                </div>
                <div style="width: 130px;">
                  <select v-model="adminSizeFilter" class="form-select form-select-sm admin-size-filter">
                    <option value="All">All Sizes</option>
                    <option value="5">5 x 5</option>
                    <option value="10">10 x 10</option>
                    <option value="15">15 x 15</option>
                    <option value="20">20 x 20</option>
                    <option value="30">30 x 30</option>
                  </select>
                </div>
                <div style="width: 150px;">
                  <select v-model="adminStatusFilter" class="form-select form-select-sm admin-status-filter">
                    <option value="All">All Statuses</option>
                    <option value="Active">Active</option>
                    <option value="Pending">Pending Approval</option>
                    <option value="Inactive">Inactive</option>
                  </select>
                </div>
              </div>

              <div class="table-responsive" style="max-height: 600px; overflow-y: auto;">
                <table class="table table-striped table-hover align-middle mb-0">
                  <thead class="table-dark position-sticky top-0" style="z-index: 10;">
                    <tr>
                      <th scope="col" class="ps-3 admin-th-id" @click="toggleAdminSort('id')" style="cursor: pointer; user-select: none;">
                        ID <span v-if="adminSortKey === 'id'">{{ adminSortOrder === 'asc' ? '▲' : '▼' }}</span>
                      </th>
                      <th scope="col" class="admin-th-name" @click="toggleAdminSort('name')" style="cursor: pointer; user-select: none;">
                        Name <span v-if="adminSortKey === 'name'">{{ adminSortOrder === 'asc' ? '▲' : '▼' }}</span>
                      </th>
                      <th scope="col" class="admin-th-size" @click="toggleAdminSort('size')" style="cursor: pointer; user-select: none;">
                        Size <span v-if="adminSortKey === 'size'">{{ adminSortOrder === 'asc' ? '▲' : '▼' }}</span>
                      </th>
                      <th scope="col" class="admin-th-status" @click="toggleAdminSort('status')" style="cursor: pointer; user-select: none;">
                        Status <span v-if="adminSortKey === 'status'">{{ adminSortOrder === 'asc' ? '▲' : '▼' }}</span>
                      </th>
                      <th scope="col" class="text-end pe-3">Actions</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr v-for="s in filteredAndSortedAdminStages" :key="s.id" class="admin-stage-item">
                      <td class="ps-3 font-monospace text-muted">{{ s.id }}</td>
                      <td>
                        <span class="fw-bold text-dark">{{ s.name }}</span>
                      </td>
                      <td>
                        <span class="badge bg-light text-dark border">{{ s.width }} x {{ s.height }}</span>
                      </td>
                      <td>
                        <span v-if="s.approved && s.active" class="badge bg-success badge-active">Active</span>
                        <span v-else-if="!s.approved" class="badge bg-warning text-dark badge-pending">Pending Approval</span>
                        <span v-else class="badge bg-danger badge-inactive">Inactive</span>
                      </td>
                      <td class="text-end pe-3">
                        <div class="btn-group btn-group-sm" role="group">
                          <button class="btn btn-outline-dark btn-preview" @click="openHistoryModal({ stageId: s.id, stageName: s.name })">
                            👁️ Preview
                          </button>
                          <button v-if="!s.approved" class="btn btn-success btn-approve" @click="handleApproveStage(s.id)">
                            ✓ Approve
                          </button>
                          <button v-if="s.active" class="btn btn-danger btn-delete" @click="handleDeleteStage(s.id)">
                            🗑️ Delete
                          </button>
                          <button v-if="!s.active && s.approved" class="btn btn-primary btn-restore" @click="handleRestoreStage(s.id)">
                            ↺ Restore
                          </button>
                        </div>
                      </td>
                    </tr>
                    <tr v-if="adminStages.length === 0">
                      <td colspan="5" class="text-center py-5 text-muted">
                        No stages found in database.
                      </td>
                    </tr>
                  </tbody>
                </table>
              </div>
            </div>
          </div>
        </div>

        <!-- Right Column: Add Custom Puzzle -->
        <div class="col-lg-5">
          <div class="card shadow-sm border-0">
            <div class="card-header bg-dark text-white py-3">
              <h5 class="card-title mb-0">✍️ Add Custom Puzzle</h5>
            </div>
            <div class="card-body">
              <div class="mb-3">
                <label class="form-label fw-bold text-dark">Puzzle Name</label>
                <input 
                  type="text" 
                  v-model="creatorName" 
                  class="form-control admin-input" 
                  placeholder="e.g. Tree, Anchor, Diamond" 
                />
              </div>
              
              <div class="row g-3 mb-4">
                <div class="col-6">
                  <label class="form-label fw-bold text-dark">Width</label>
                  <select v-model="creatorWidth" @change="initCreatorGrid" class="form-select">
                    <option :value="5">5</option>
                    <option :value="10">10</option>
                    <option :value="15">15</option>
                    <option :value="20">20</option>
                    <option :value="30">30</option>
                  </select>
                </div>
                <div class="col-6">
                  <label class="form-label fw-bold text-dark">Height</label>
                  <select v-model="creatorHeight" @change="initCreatorGrid" class="form-select">
                    <option :value="5">5</option>
                    <option :value="10">10</option>
                    <option :value="15">15</option>
                    <option :value="20">20</option>
                    <option :value="30">30</option>
                  </select>
                </div>
              </div>

              <!-- HTML5 Canvas Creator Grid Wrapper -->
              <div class="mb-4">
                <label class="form-label fw-bold d-block text-center mb-2 text-dark">Draw Puzzle Solution</label>
                <div class="d-flex justify-content-center align-items-center p-3 bg-dark rounded border" style="min-height: 260px;">
                  <canvas 
                    ref="creatorCanvasRef" 
                    @mousedown="handleCreatorMouseDown" 
                    @touchstart="handleCreatorTouchStart"
                    @contextmenu.prevent
                    style="display: block; cursor: pointer; border: 1px solid rgba(255, 255, 255, 0.2);"
                  ></canvas>
                </div>
                <div class="form-text text-center mt-2 text-muted">
                  Left-click / drag to fill cells. Right-click or drag again to clear.
                </div>
              </div>

              <button 
                class="btn btn-dark w-100 py-2 fw-bold admin-save-btn" 
                @click="handleSaveCustomStage"
              >
                💾 Save Custom Stage
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- Reused Modal for History Review / Preview inside Back Office -->
      <div v-if="isModalOpen && modalBoard" class="modal show d-block" tabindex="-1" style="background: rgba(0, 0, 0, 0.7); backdrop-filter: blur(4px); z-index: 1050;">
        <div class="modal-dialog modal-dialog-centered">
          <div class="modal-content border-0 shadow-lg bg-light text-dark">
            <div class="modal-header bg-dark text-white border-0">
              <h5 class="modal-title">👁️ Stage Solution Preview</h5>
              <button type="button" class="btn-close btn-close-white" @click="closeModal" aria-label="Close"></button>
            </div>
            <div class="modal-body text-center bg-light">
              <p class="text-muted mb-3">Stage: <strong class="text-dark">{{ selectedHistory?.stageName }}</strong></p>
              <div class="modal-canvas-wrapper mx-auto" style="width: 320px; max-width: 100%; aspect-ratio: 1; background-color: #0f172a; border-radius: 12px; overflow: hidden; border: 1px solid rgba(255, 255, 255, 0.1); display: flex; justify-content: center; align-items: center; position: relative;">
                <NonogramCanvas :board="modalBoard" :readOnly="true" :initialAngle="0" />
              </div>
            </div>
            <div class="modal-footer border-0 bg-light">
              <button type="button" class="btn btn-secondary modal-close-btn" @click="closeModal">Close</button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>

  <div v-else class="app-container">
    <!-- Slim Header -->
    <header class="app-header">
      <div class="logo-wrapper">
        <div class="logo-icon">
          <div class="logo-cell filled"></div>
          <div class="logo-cell"></div>
          <div class="logo-cell"></div>
          <div class="logo-cell filled"></div>
        </div>
        <div class="logo-title-wrapper">
          <h1 class="app-title">rogic.io</h1>
          <p class="app-subtitle">Rotate Logic Puzzle</p>
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
    <div class="app-layout">
      <!-- Center Main Column: Canvas & Solved Banner -->
      <main class="app-main">
        <template v-if="currentTab === 'play'">
          <!-- Loading State -->
          <div v-if="isLoading" class="loading-state">
            <div class="spinner-logo">
              <div class="spinner-cell filled"></div>
              <div class="spinner-cell"></div>
              <div class="spinner-cell"></div>
              <div class="spinner-cell filled"></div>
            </div>
            <p class="loading-text">Loading board data...</p>
          </div>

          <!-- Error State -->
          <div v-else-if="loadError" class="error-state">
            <div class="error-icon">⚠️</div>
            <p class="error-text">{{ loadError }}</p>
            <button class="retry-btn" @click="handleRetryLoad">
              🔄 Retry
            </button>
          </div>

          <!-- Canvas Area -->
          <div v-else-if="board" class="canvas-wrapper-container">
            <!-- Floating Stage Selector -->
            <div class="puzzle-selector-floating-container" v-if="currentActiveStage">
              <!-- Play Size Filter Bar (Always visible outside) -->
              <div class="play-size-filter-bar" v-if="availablePlaySizes.length > 0">
                <button 
                  class="play-size-filter-btn" 
                  :class="{ active: selectedPlaySizeFilter === 'All' }"
                  @click.stop="selectedPlaySizeFilter = 'All'"
                >
                  All
                </button>
                <button 
                  v-for="size in availablePlaySizes" 
                  :key="size"
                  class="play-size-filter-btn" 
                  :class="{ active: selectedPlaySizeFilter === String(size) }"
                  @click.stop="selectedPlaySizeFilter = String(size)"
                >
                  {{ size }}x{{ size }}
                </button>
              </div>

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
                      v-for="stage in filteredPlayStages" 
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
                    <div v-if="filteredPlayStages.length === 0" class="empty-stages" style="text-align: center; padding: 2rem; color: #64748b; font-size: 0.9rem;">
                      🎉 No puzzles found!
                    </div>
                  </div>
                </div>
              </transition>
            </div>

            <div class="canvas-wrapper">
              <NonogramCanvas :board="board" :rotationSteps="currentRotationSteps" :readOnly="solved" @cell-click="handleCellClick" />
            </div>
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
            <!-- Profile Info Card -->
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

            <!-- History List Section -->
            <div class="mypage-history-section">
              <div class="stage-card-list" style="display: flex; flex-direction: column; gap: 0.75rem; max-height: 280px; overflow-y: auto; padding-right: 0.25rem;">
                <div 
                  v-for="item in histories" 
                  :key="item.id" 
                  class="history-item" 
                  @click="openHistoryModal(item)"
                  style="cursor: pointer;"
                >
                  <div class="history-card-header" style="display: flex; justify-content: space-between; align-items: center;">
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
      <div class="modal-content" style="max-width: 500px;">
        <h3 class="modal-title" style="margin-top: 0; color: #38bdf8; font-weight: 700;">Review Clear History</h3>
        <p class="modal-stage-info" style="color: #94a3b8; margin-bottom: 1.5rem;">Stage: {{ selectedHistory?.stageName }}</p>
        <div class="modal-canvas-wrapper" style="width: 320px; max-width: 100%; aspect-ratio: 1; margin: 0 auto; background-color: #0f172a; border-radius: 12px; overflow: hidden; border: 1px solid rgba(255, 255, 255, 0.1); display: flex; justify-content: center; align-items: center; position: relative;">
          <NonogramCanvas :board="modalBoard" :readOnly="true" :initialAngle="0" />
        </div>
        <div style="margin-top: 1.5rem;">
          <button class="modal-close-btn" @click="closeModal" style="padding: 0.5rem 1.5rem; background-color: #f43f5e; border: none; border-radius: 8px; color: #ffffff; font-weight: 600; cursor: pointer; transition: background-color 0.2s;">Close</button>
        </div>
      </div>
    </div>

    <!-- Modal for Help / Instructions (Auto-shown to first-time visitors) -->
    <div v-if="isHelpModalOpen" class="help-modal-overlay" style="position: fixed; top: 0; left: 0; right: 0; bottom: 0; display: flex; justify-content: center; align-items: center; background: rgba(15, 23, 42, 0.85); backdrop-filter: blur(8px); z-index: 10000;" @click.self="isHelpModalOpen = false">
      <div class="modal-content" style="max-width: 420px;">
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
import { fetchAdminStages, createStage, approveStage, deleteStage, restoreStage, generateAiStage, loginAdmin, logoutAdmin, isAdminAuthenticated } from './api/adminApi';
import type { AdminStageInfo } from './api/adminApi';

const isAdminMode = ref(false);
const isAdminLogged = ref(isAdminAuthenticated());
const adminUsernameInput = ref('');
const adminPasswordInput = ref('');
const loginError = ref('');
const stages = ref<StageSummary[]>([]);

watch(isAdminMode, (newVal) => {
  if (newVal) {
    document.body.style.overflow = 'auto';
    document.body.style.height = 'auto';
    document.body.style.display = 'block';
    document.body.style.backgroundColor = '#f8f9fa';
  } else {
    document.body.style.overflow = 'hidden';
    document.body.style.height = '100vh';
    document.body.style.display = 'flex';
    document.body.style.backgroundColor = '#0f172a';
  }
}, { immediate: true });
const selectedStageId = ref<number | null>(null);
const board = ref<PuzzleBoard | null>(null);
const solved = ref(false);
const nextPuzzleSeconds = ref(3);
const isLoading = ref(true);
const loadError = ref<string | null>(null);
let countdownTimer: any = null;

const rankings = ref<User[]>([]);
const currentUser = ref<UserSession | null>(null);
const currentTab = ref<'play' | 'mypage' | 'admin'>('play');
const histories = ref<any[]>([]);
const startTime = ref<number>(Date.now());

const aiStages = ref<StageSummary[]>([]);
const selectedAiStageId = ref<number | null>(null);
const isAiStageActive = ref(false);
const selectedCategory = ref<'normal' | 'ai'>('normal');
const isHelpModalOpen = ref(false);
const isMypageTipOpen = ref(false);

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

const selectedPlaySizeFilter = ref<string>('All');

const availablePlaySizes = computed(() => {
  const sizes = new Set<number>();
  allUnclearedStages.value.forEach(s => {
    sizes.add(s.width);
  });
  return Array.from(sizes).sort((a, b) => a - b);
});

const filteredPlayStages = computed(() => {
  const list = allUnclearedStages.value;
  if (selectedPlaySizeFilter.value === 'All') {
    return list;
  }
  const size = parseInt(selectedPlaySizeFilter.value);
  return list.filter(s => s.width === size);
});


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

watch(selectedPlaySizeFilter, (newSize) => {
  if (newSize === 'All') return;
  const sizeNum = parseInt(newSize);
  const current = currentActiveStage.value;
  if (!current || current.width !== sizeNum) {
    const matching = allUnclearedStages.value.filter(s => s.width === sizeNum);
    if (matching.length > 0 && (!current || current.id !== matching[0].id)) {
      selectStageCard(matching[0].id, isStageAi(matching[0]));
    }
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

const isModalOpen = ref(false);
const selectedHistory = ref<any>(null);
const modalBoard = ref<PuzzleBoard | null>(null);


function getErrorMessage(error: any, fallbackMessage: string): string {
  if (error && error.response) {
    const status = error.response.status;
    if (status >= 500) {
      return `Failed to load due to a server error (${status}). Please try again later.`;
    }
  }
  return fallbackMessage;
}

async function loadStagesList() {
  isLoading.value = true;
  loadError.value = null;
  try {
    const list = await fetchStages();
    stages.value = list;
    if (list.length > 0) {
      selectedStageId.value = list[0].id;
      await loadStageDetails(list[0].id);
    } else {
      isLoading.value = false;
    }
  } catch (error) {
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
    isLoading.value = false;
  } catch (error) {
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

async function onTabChange(tab: 'play' | 'mypage' | 'admin') {
  currentTab.value = tab;
  if (tab === 'mypage') {
    await loadUserHistory();
    const tipShown = localStorage.getItem('rogic_mypage_tip_shown');
    if (!tipShown) {
      isMypageTipOpen.value = true;
    }
  } else if (tab === 'admin') {
    if (isAdminLogged.value) {
      await loadAdminStagesList();
      initCreatorGrid();
    }
  }
}

const adminStages = ref<AdminStageInfo[]>([]);
const adminSearchQuery = ref('');
const adminSizeFilter = ref('All');
const adminStatusFilter = ref('All');
const adminSortKey = ref<'id' | 'name' | 'size' | 'status'>('id');
const adminSortOrder = ref<'asc' | 'desc'>('asc');

const filteredAndSortedAdminStages = computed(() => {
  let list = [...adminStages.value];

  // 1. Filter by search query (Name case-insensitive)
  if (adminSearchQuery.value.trim() !== '') {
    const q = adminSearchQuery.value.toLowerCase();
    list = list.filter(s => s.name.toLowerCase().includes(q));
  }

  // 2. Filter by size (width x height)
  if (adminSizeFilter.value !== 'All') {
    const size = parseInt(adminSizeFilter.value);
    list = list.filter(s => s.width === size || s.height === size);
  }

  // 3. Filter by status
  if (adminStatusFilter.value !== 'All') {
    if (adminStatusFilter.value === 'Active') {
      list = list.filter(s => s.approved && s.active);
    } else if (adminStatusFilter.value === 'Pending') {
      list = list.filter(s => !s.approved);
    } else if (adminStatusFilter.value === 'Inactive') {
      list = list.filter(s => s.approved && !s.active);
    }
  }

  // 4. Sort
  list.sort((a, b) => {
    let valA: any;
    let valB: any;

    if (adminSortKey.value === 'id') {
      valA = a.id;
      valB = b.id;
    } else if (adminSortKey.value === 'name') {
      valA = a.name.toLowerCase();
      valB = b.name.toLowerCase();
    } else if (adminSortKey.value === 'size') {
      valA = a.width * a.height;
      valB = b.width * b.height;
    } else if (adminSortKey.value === 'status') {
      valA = a.approved && a.active ? 3 : (!a.approved ? 2 : 1);
      valB = b.approved && b.active ? 3 : (!b.approved ? 2 : 1);
    }

    if (valA < valB) return adminSortOrder.value === 'asc' ? -1 : 1;
    if (valA > valB) return adminSortOrder.value === 'asc' ? 1 : -1;
    return 0;
  });

  return list;
});

function toggleAdminSort(key: 'id' | 'name' | 'size' | 'status') {
  if (adminSortKey.value === key) {
    adminSortOrder.value = adminSortOrder.value === 'asc' ? 'desc' : 'asc';
  } else {
    adminSortKey.value = key;
    adminSortOrder.value = 'asc';
  }
}
const creatorName = ref('');
const creatorWidth = ref(5);
const creatorHeight = ref(5);
const adminAiWidth = ref(5);
const adminAiHeight = ref(5);
const creatorGrid = ref<number[][]>([]);
const creatorCanvasRef = ref<HTMLCanvasElement | null>(null);
const isCreatorDragging = ref(false);
const creatorDragVal = ref(0);
const lastCreatorRow = ref(-1);
const lastCreatorCol = ref(-1);

async function loadAdminStagesList() {
  try {
    const list = await fetchAdminStages();
    adminStages.value = list;
  } catch (error) {
    console.error('Failed to load admin stages:', error);
  }
}

function initCreatorGrid() {
  creatorGrid.value = Array.from({ length: creatorHeight.value }, () =>
    Array(creatorWidth.value).fill(0)
  );
  setTimeout(drawCreatorGrid, 20);
}

function drawCreatorGrid() {
  const canvas = creatorCanvasRef.value;
  if (!canvas) return;
  const ctx = canvas.getContext('2d');
  if (!ctx) return;

  const maxCells = Math.max(creatorWidth.value, creatorHeight.value);
  const cellPixelSize = maxCells <= 5 ? 30 : (maxCells <= 10 ? 20 : (maxCells <= 15 ? 15 : (maxCells <= 20 ? 12 : 8)));
  
  canvas.width = creatorWidth.value * cellPixelSize;
  canvas.height = creatorHeight.value * cellPixelSize;

  // Background
  ctx.fillStyle = '#0f172a';
  ctx.fillRect(0, 0, canvas.width, canvas.height);

  for (let r = 0; r < creatorHeight.value; r++) {
    for (let c = 0; c < creatorWidth.value; c++) {
      const x = c * cellPixelSize;
      const y = r * cellPixelSize;

      ctx.strokeStyle = '#334155';
      ctx.lineWidth = 1;
      ctx.strokeRect(x, y, cellPixelSize, cellPixelSize);

      const val = creatorGrid.value[r]?.[c] || 0;
      if (val === 1) {
        const grad = ctx.createLinearGradient(x, y, x + cellPixelSize, y + cellPixelSize);
        grad.addColorStop(0, '#38bdf8');
        grad.addColorStop(1, '#818cf8');
        ctx.fillStyle = grad;
        ctx.fillRect(x + 1, y + 1, cellPixelSize - 2, cellPixelSize - 2);
      } else {
        ctx.fillStyle = '#1e293b';
        ctx.fillRect(x + 1, y + 1, cellPixelSize - 2, cellPixelSize - 2);
      }
    }
  }

  // Draw grid helper lines every 5 cells
  ctx.strokeStyle = '#64748b';
  ctx.lineWidth = 2;
  for (let r = 0; r <= creatorHeight.value; r += 5) {
    if (r > 0 && r < creatorHeight.value) {
      ctx.beginPath();
      ctx.moveTo(0, r * cellPixelSize);
      ctx.lineTo(canvas.width, r * cellPixelSize);
      ctx.stroke();
    }
  }
  for (let c = 0; c <= creatorWidth.value; c += 5) {
    if (c > 0 && c < creatorWidth.value) {
      ctx.beginPath();
      ctx.moveTo(c * cellPixelSize, 0);
      ctx.lineTo(c * cellPixelSize, canvas.height);
      ctx.stroke();
    }
  }
}

function getCreatorCoords(clientX: number, clientY: number) {
  const canvas = creatorCanvasRef.value;
  if (!canvas) return null;
  const rect = canvas.getBoundingClientRect();
  const scaleX = canvas.width / rect.width;
  const scaleY = canvas.height / rect.height;
  const clickX = (clientX - rect.left) * scaleX;
  const clickY = (clientY - rect.top) * scaleY;

  const maxCells = Math.max(creatorWidth.value, creatorHeight.value);
  const cellPixelSize = maxCells <= 5 ? 30 : (maxCells <= 10 ? 20 : (maxCells <= 15 ? 15 : (maxCells <= 20 ? 12 : 8)));
  
  const col = Math.floor(clickX / cellPixelSize);
  const row = Math.floor(clickY / cellPixelSize);

  if (row < 0 || row >= creatorHeight.value || col < 0 || col >= creatorWidth.value) {
    return null;
  }
  return { row, col };
}

function handleCreatorMouseDown(event: MouseEvent) {
  if (event.button !== 0) return; // Only left click
  const coords = getCreatorCoords(event.clientX, event.clientY);
  if (!coords) return;

  const { row, col } = coords;
  creatorDragVal.value = creatorGrid.value[row][col] === 1 ? 0 : 1;
  creatorGrid.value[row][col] = creatorDragVal.value;
  isCreatorDragging.value = true;
  lastCreatorRow.value = row;
  lastCreatorCol.value = col;
  drawCreatorGrid();

  window.addEventListener('mousemove', handleCreatorWindowMouseMove);
  window.addEventListener('mouseup', handleCreatorWindowMouseUp);
}

function handleCreatorWindowMouseMove(event: MouseEvent) {
  if (!isCreatorDragging.value) return;
  const coords = getCreatorCoords(event.clientX, event.clientY);
  if (!coords) return;

  const { row, col } = coords;
  if (row !== lastCreatorRow.value || col !== lastCreatorCol.value) {
    creatorGrid.value[row][col] = creatorDragVal.value;
    lastCreatorRow.value = row;
    lastCreatorCol.value = col;
    drawCreatorGrid();
  }
}

function handleCreatorWindowMouseUp() {
  if (isCreatorDragging.value) {
    isCreatorDragging.value = false;
    window.removeEventListener('mousemove', handleCreatorWindowMouseMove);
    window.removeEventListener('mouseup', handleCreatorWindowMouseUp);
  }
}

function handleCreatorTouchStart(event: TouchEvent) {
  if (event.touches.length !== 1) return;
  event.preventDefault();
  const touch = event.touches[0];
  const coords = getCreatorCoords(touch.clientX, touch.clientY);
  if (!coords) return;

  const { row, col } = coords;
  creatorDragVal.value = creatorGrid.value[row][col] === 1 ? 0 : 1;
  creatorGrid.value[row][col] = creatorDragVal.value;
  isCreatorDragging.value = true;
  lastCreatorRow.value = row;
  lastCreatorCol.value = col;
  drawCreatorGrid();

  window.addEventListener('touchmove', handleCreatorWindowTouchMove, { passive: false });
  window.addEventListener('touchend', handleCreatorWindowTouchEnd);
  window.addEventListener('touchcancel', handleCreatorWindowTouchEnd);
}

function handleCreatorWindowTouchMove(event: TouchEvent) {
  if (!isCreatorDragging.value || event.touches.length !== 1) return;
  event.preventDefault();
  const touch = event.touches[0];
  const coords = getCreatorCoords(touch.clientX, touch.clientY);
  if (!coords) return;

  const { row, col } = coords;
  if (row !== lastCreatorRow.value || col !== lastCreatorCol.value) {
    creatorGrid.value[row][col] = creatorDragVal.value;
    lastCreatorRow.value = row;
    lastCreatorCol.value = col;
    drawCreatorGrid();
  }
}

function handleCreatorWindowTouchEnd() {
  if (isCreatorDragging.value) {
    isCreatorDragging.value = false;
    window.removeEventListener('touchmove', handleCreatorWindowTouchMove);
    window.removeEventListener('touchend', handleCreatorWindowTouchEnd);
    window.removeEventListener('touchcancel', handleCreatorWindowTouchEnd);
  }
}

async function handleSaveCustomStage() {
  if (!creatorName.value || creatorName.value.trim() === '') {
    alert('Please enter a stage name.');
    return;
  }

  // Verify at least one filled cell
  let hasFilled = false;
  for (let r = 0; r < creatorHeight.value; r++) {
    for (let c = 0; c < creatorWidth.value; c++) {
      if (creatorGrid.value[r][c] === 1) {
        hasFilled = true;
        break;
      }
    }
  }

  if (!hasFilled) {
    alert('Please draw at least one filled cell.');
    return;
  }

  try {
    const newStage = {
      name: creatorName.value,
      width: creatorWidth.value,
      height: creatorHeight.value,
      solutionGrid: creatorGrid.value
    };

    await createStage(newStage as any);
    alert('Stage saved successfully!');
    
    // Clear and reload
    creatorName.value = '';
    initCreatorGrid();
    await loadAdminStagesList();
    await loadStagesList();
  } catch (error: any) {
    console.error('Failed to save stage:', error);
    alert(error.response?.data || 'Failed to save stage. Ensure it has a unique solution.');
  }
}

async function handleApproveStage(id: number) {
  try {
    await approveStage(id);
    await loadAdminStagesList();
    await loadStagesList();
    await loadAiStagesList();
  } catch (error) {
    console.error('Failed to approve stage:', error);
  }
}

async function handleDeleteStage(id: number) {
  if (!confirm('Are you sure you want to delete this stage? (Soft delete)')) return;
  try {
    await deleteStage(id);
    await loadAdminStagesList();
    await loadStagesList();
    await loadAiStagesList();
  } catch (error) {
    console.error('Failed to delete stage:', error);
  }
}

async function handleRestoreStage(id: number) {
  try {
    await restoreStage(id);
    await loadAdminStagesList();
    await loadStagesList();
    await loadAiStagesList();
  } catch (error) {
    console.error('Failed to restore stage:', error);
  }
}

async function handleAdminAiGenerate() {
  try {
    const w = Number(adminAiWidth.value) || 5;
    const h = Number(adminAiHeight.value) || 5;
    await generateAiStage(w, h);
    alert('AI Stage generated as Pending Approval!');
    await loadAdminStagesList();
  } catch (error) {
    console.error('Failed to generate AI stage:', error);
    alert('Failed to generate AI stage.');
  }
}

async function handleAdminLogin() {
  try {
    loginError.value = '';
    await loginAdmin(adminUsernameInput.value, adminPasswordInput.value);
    isAdminLogged.value = true;
    adminUsernameInput.value = '';
    adminPasswordInput.value = '';
    await loadAdminStagesList();
    initCreatorGrid();
  } catch (error: any) {
    console.error('Admin login failed:', error);
    loginError.value = 'Invalid username or password';
  }
}

async function handleAdminLogout() {
  await logoutAdmin();
  isAdminLogged.value = false;
}

function closeMypageTip() {
  isMypageTipOpen.value = false;
  localStorage.setItem('rogic_mypage_tip_shown', 'true');
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
  }

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

  if (isAdminMode.value && isAdminLogged.value) {
    await loadAdminStagesList();
    initCreatorGrid();
  }

  window.addEventListener('resize', handleConfettiResize);
  document.addEventListener('touchstart', preventPinchZoom, { passive: false });

  // Show Help modal to first-time visitors
  if (!isAdminMode.value) {
    const visited = localStorage.getItem('rotagic_visited');
    if (!visited) {
      isHelpModalOpen.value = true;
      localStorage.setItem('rotagic_visited', 'true');
    }
  }
});

onUnmounted(() => {
  resetCountdown();
  window.removeEventListener('resize', handleConfettiResize);
  document.removeEventListener('touchstart', preventPinchZoom);
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

.puzzle-selector-floating-container {
  position: absolute;
  top: 15px; /* Shift slightly up to accommodate the filter bar */
  left: 50%;
  transform: translateX(-50%);
  z-index: 100;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.55rem;
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

/* Play size filter styling */
.play-size-filter-bar {
  display: flex;
  gap: 0.35rem;
  overflow-x: auto;
  background: rgba(30, 41, 59, 0.55);
  backdrop-filter: blur(12px);
  border: 1px solid rgba(255, 255, 255, 0.08);
  padding: 0.25rem 0.5rem;
  border-radius: 9999px;
  flex-shrink: 0;
  box-shadow: 0 4px 15px -3px rgba(0, 0, 0, 0.2);
  scrollbar-width: none; /* Hide scrollbar for Firefox */
}

.play-size-filter-bar::-webkit-scrollbar {
  display: none; /* Hide scrollbar for Chrome/Safari */
}

.play-size-filter-btn {
  padding: 0.2rem 0.65rem;
  background-color: transparent;
  border: 1px solid transparent;
  border-radius: 9999px;
  color: #94a3b8;
  font-size: 0.72rem;
  font-weight: 700;
  cursor: pointer;
  white-space: nowrap;
  transition: all 0.15s ease;
  font-family: 'Outfit', sans-serif;
}

.play-size-filter-btn:hover {
  color: #f8fafc;
  background-color: rgba(255, 255, 255, 0.05);
}

.play-size-filter-btn.active {
  background-color: rgba(56, 189, 248, 0.15);
  border-color: rgba(56, 189, 248, 0.3);
  color: #38bdf8;
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
  max-width: 600px;
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 1.25rem;
  box-sizing: border-box;
}

.mypage-user-profile {
  display: flex;
  align-items: center;
  gap: 1.5rem;
  background-color: #1e293b;
  border: 1px solid #334155;
  border-radius: 16px;
  padding: 1.5rem;
  box-shadow: 0 10px 25px -5px rgba(0, 0, 0, 0.3);
  box-sizing: border-box;
  width: 100%;
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

/* History Card */
.history-item {
  background-color: #1e293b;
  border: 1px solid #334155;
  border-radius: 10px;
  padding: 0.8rem 1rem;
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

.modal-content {
  background-color: #1e293b;
  border: 1px solid #334155;
  border-radius: 16px;
  padding: 2rem;
  width: 90%;
  text-align: center;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.55);
  animation: pop-in 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275);
  box-sizing: border-box;
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

