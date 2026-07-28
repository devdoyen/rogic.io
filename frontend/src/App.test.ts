import { describe, it, expect, vi, beforeEach } from 'vitest';
import { mount } from '@vue/test-utils';
import App from './App.vue';
import * as stageApi from './api/stageApi';
import * as userApi from './api/userApi';
import * as adminApi from './api/adminApi';

vi.mock('./api/stageApi');
vi.mock('./api/userApi');
vi.mock('./api/adminApi');
vi.mock('./api/cognito', () => ({
  loginWithGoogle: vi.fn(),
  logout: vi.fn(),
  getStoredToken: vi.fn(() => localStorage.getItem('nemologic_id_token')),
  getOrRefreshToken: vi.fn(() => Promise.resolve(localStorage.getItem('nemologic_id_token'))),
  isTokenExpired: vi.fn(() => false)
}));

describe('App.vue Leaderboard Integration TDD', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    localStorage.clear();
    (window as any).dataLayer = [];
    vi.spyOn(userApi, 'fetchClearedStageIds').mockResolvedValue([]);
    localStorage.setItem('nemologic_id_token', 'mockHeader.eyzleHAiOjk5OTk5OTk5OTl9.mockSignature');
    vi.mocked(stageApi.fetchNextReleaseDelaySeconds).mockResolvedValue(3600);
    vi.mocked(userApi.fetchMeFromServer).mockResolvedValue({
      id: 1,
      username: 'Player1',
      xp: 200,
      level: 2,
      email: 'john@example.com',
      profileImageUrl: 'https://example.com/pic.png'
    });
    vi.mocked(adminApi.fetchAdminStages).mockResolvedValue([
      { id: 1, name: 'Seeded Stage 1', width: 5, height: 5, active: true, approved: true, solutionGrid: [[1]], totalClears: 12, totalAttempts: 45 },
      { id: 9, name: 'AI Pending Stage', width: 5, height: 5, active: false, approved: true, solutionGrid: [[1]], totalClears: 0, totalAttempts: 0 }
    ]);
    vi.mocked(stageApi.verifyStageSolve).mockResolvedValue({ token: 'mock-verify-token' });
  });

  it('should call fetchStages and fetchRanking on mount, and render rankings list', async () => {
    const mockStages = [
      { id: 1, name: 'Heart Shape', width: 5, height: 5 }
    ];
    const mockStageDetails = {
      id: 1,
      name: 'Heart Shape',
      width: 5,
      height: 5,
      solutionGrid: [
        [0, 1, 0],
        [1, 1, 1],
        [0, 1, 0]
      ]
    };
    const mockRankings = [
      { id: 3, username: 'Player3', xp: 1000, level: 5 },
      { id: 2, username: 'Player2', xp: 500, level: 3 },
      { id: 1, username: 'Player1', xp: 200, level: 2 }
    ];

    vi.spyOn(stageApi, 'fetchStages').mockResolvedValue(mockStages);
    vi.spyOn(stageApi, 'fetchStageById').mockResolvedValue(mockStageDetails);
    vi.spyOn(userApi, 'fetchRanking').mockResolvedValue(mockRankings);

    const wrapper = mount(App);

    // Wait for asynchronous lifecycle hooks to settle
    await new Promise((resolve) => setTimeout(resolve, 50));

    expect(stageApi.fetchStages).toHaveBeenCalled();
    expect(userApi.fetchRanking).toHaveBeenCalled();

    // Check if rankings are rendered in the sidebar
    const items = wrapper.findAll('.leaderboard-item');
    expect(items.length).toBe(3);
    expect(items[0].text()).toContain('Player3');
    expect(items[0].text()).toContain('Lv.5');
    expect(items[0].text()).toContain('1000 XP');
  });

  it('should stay in guest mode on mount if no session or token exists', async () => {
    localStorage.clear();
    const mockStages = [{ id: 1, name: 'Heart Shape', width: 5, height: 5 }];
    const mockStageDetails = { id: 1, name: 'Heart Shape', width: 5, height: 5, solutionGrid: [[0, 1, 0], [1, 1, 1], [0, 1, 0]] };
    const mockRankings = [{ id: 3, username: 'Player3', xp: 1000, level: 5 }];

    vi.spyOn(stageApi, 'fetchStages').mockResolvedValue(mockStages);
    vi.spyOn(stageApi, 'fetchStageById').mockResolvedValue(mockStageDetails);
    vi.spyOn(userApi, 'fetchRanking').mockResolvedValue(mockRankings);
    const fetchMeSpy = vi.spyOn(userApi, 'fetchMeFromServer');

    mount(App);
    await new Promise((resolve) => setTimeout(resolve, 50));

    expect(fetchMeSpy).not.toHaveBeenCalled();
  });

  it('should call fetchMeFromServer on mount if token is stored', async () => {
    localStorage.clear();
    localStorage.setItem('nemologic_id_token', 'mockHeader.eyzleHAiOjk5OTk5OTk5OTl9.mockSignature');

    const mockStages = [{ id: 1, name: 'Heart Shape', width: 5, height: 5 }];
    const mockStageDetails = { id: 1, name: 'Heart Shape', width: 5, height: 5, solutionGrid: [[0, 1, 0], [1, 1, 1], [0, 1, 0]] };
    const mockRankings = [{ id: 3, username: 'Player3', xp: 1000, level: 5 }];
    const mockServerUser = { id: 42, username: 'GoogleUser', xp: 100, level: 2, email: 'user@example.com' };

    vi.spyOn(stageApi, 'fetchStages').mockResolvedValue(mockStages);
    vi.spyOn(stageApi, 'fetchStageById').mockResolvedValue(mockStageDetails);
    vi.spyOn(userApi, 'fetchRanking').mockResolvedValue(mockRankings);
    const fetchMeSpy = vi.spyOn(userApi, 'fetchMeFromServer').mockResolvedValue(mockServerUser);

    mount(App);
    await new Promise((resolve) => setTimeout(resolve, 50));

    expect(fetchMeSpy).toHaveBeenCalled();
  });


  it('should switch to My Page tab and fetch/render user history', async () => {
    const mockStages = [{ id: 1, name: 'Heart Shape', width: 5, height: 5 }];
    const mockStageDetails = { id: 1, name: 'Heart Shape', width: 5, height: 5, solutionGrid: [[0, 1, 0], [1, 1, 1], [0, 1, 0]] };
    const mockRankings = [{ id: 3, username: 'Player3', xp: 1000, level: 5 }];
    const mockHistory = [
      { id: 10, userId: 1, stageId: 1, stageName: 'Heart Shape', clearedAt: '2026-06-08T22:40:40', xpEarned: 50, elapsedTime: 120 }
    ];

    vi.spyOn(stageApi, 'fetchStages').mockResolvedValue(mockStages);
    vi.spyOn(stageApi, 'fetchStageById').mockResolvedValue(mockStageDetails);
    vi.spyOn(userApi, 'fetchRanking').mockResolvedValue(mockRankings);
    const historySpy = vi.spyOn(userApi, 'fetchUserHistory').mockResolvedValue(mockHistory);
    vi.spyOn(userApi, 'fetchClearedStageIds').mockResolvedValue([1, 2]);

    const wrapper = mount(App);
    await new Promise((resolve) => setTimeout(resolve, 50));

    // Click My Page tab
    const myPageTab = wrapper.find('.tab-btn-mypage');
    expect(myPageTab.exists()).toBe(true);
    await myPageTab.trigger('click');

    expect(historySpy).toHaveBeenCalledWith(1, 0, 10);

    // Check history item rendering
    const historyItems = wrapper.findAll('.history-item');
    expect(historyItems.length).toBe(1);
    expect(historyItems[0].text()).toContain('Heart Shape');
    expect(historyItems[0].text()).toContain('120s');
  });

  it('should call clearStage with stageId and elapsedTime when puzzle is solved', async () => {
    const mockStages = [{ id: 7, name: 'Mini Stage', width: 1, height: 1 }];
    const mockStageDetails = { id: 7, name: 'Mini Stage', width: 1, height: 1, solutionGrid: [[1]] };
    const mockRankings = [{ id: 3, username: 'Player3', xp: 1000, level: 5 }];

    vi.spyOn(stageApi, 'fetchStages').mockResolvedValue(mockStages);
    vi.spyOn(stageApi, 'fetchStageById').mockResolvedValue(mockStageDetails);
    vi.spyOn(userApi, 'fetchRanking').mockResolvedValue(mockRankings);
    const clearStageSpy = vi.spyOn(userApi, 'clearStage').mockResolvedValue({ id: 1, username: 'Player1', xp: 250, level: 2 });

    const wrapper = mount(App);
    await new Promise((resolve) => setTimeout(resolve, 50));

    // Force solve the board
    (wrapper.vm as any).board.toggleFill(0, 0); // Fills the 1x1 cell to match solution [[1]]
    await (wrapper.vm as any).handleCellClick();

    expect(clearStageSpy).toHaveBeenCalledWith(1, 'EASY', 7, expect.any(Number));
  });

  it('should not call clearStage but save to localStorage and update guest history when solved in Guest Mode', async () => {
    localStorage.clear(); // Ensure Guest Mode
    const mockStages = [{ id: 7, name: 'Mini Stage', width: 1, height: 1 }];
    const mockStageDetails = { id: 7, name: 'Mini Stage', width: 1, height: 1, solutionGrid: [[1]] };
    const mockRankings = [{ id: 3, username: 'Player3', xp: 1000, level: 5 }];

    vi.spyOn(stageApi, 'fetchStages').mockResolvedValue(mockStages);
    vi.spyOn(stageApi, 'fetchStageById').mockResolvedValue(mockStageDetails);
    vi.spyOn(userApi, 'fetchRanking').mockResolvedValue(mockRankings);
    const clearStageSpy = vi.spyOn(userApi, 'clearStage');

    const wrapper = mount(App);
    await new Promise((resolve) => setTimeout(resolve, 50));

    // Force solve the board
    (wrapper.vm as any).board.toggleFill(0, 0);
    await (wrapper.vm as any).handleCellClick();
    await new Promise((resolve) => setTimeout(resolve, 50));

    // In Guest Mode, clearStage API should NOT be called
    expect(clearStageSpy).not.toHaveBeenCalled();

    // But localStorage should save the cleared stage and the history item
    const guestCleared = JSON.parse(localStorage.getItem('guest_cleared_stages') || '[]');
    expect(guestCleared).toContain(7);

    const guestHistories = JSON.parse(localStorage.getItem('guest_histories') || '[]');
    expect(guestHistories.length).toBe(1);
    expect(guestHistories[0].stageId).toBe(7);
    expect(guestHistories[0].stageName).toBe('Mini Stage');
  });

  it('should open history review modal when a history item is clicked, and close it when close button is clicked', async () => {
    const mockStages = [{ id: 1, name: 'Heart Shape', width: 5, height: 5 }];
    const mockStageDetails = { id: 1, name: 'Heart Shape', width: 5, height: 5, solutionGrid: [[0, 1, 0], [1, 1, 1], [0, 1, 0]] };
    const mockRankings = [{ id: 3, username: 'Player3', xp: 1000, level: 5 }];
    const mockHistory = [
      { id: 10, userId: 1, stageId: 1, stageName: 'Heart Shape', clearedAt: '2026-06-08T22:40:40', xpEarned: 50, elapsedTime: 120 }
    ];

    vi.spyOn(stageApi, 'fetchStages').mockResolvedValue(mockStages);
    const fetchStageSpy = vi.spyOn(stageApi, 'fetchStageById').mockResolvedValue(mockStageDetails);
    vi.spyOn(userApi, 'fetchRanking').mockResolvedValue(mockRankings);
    vi.spyOn(userApi, 'fetchUserHistory').mockResolvedValue(mockHistory);

    const wrapper = mount(App);
    await new Promise((resolve) => setTimeout(resolve, 50));

    // Switch to My Page
    const myPageTab = wrapper.find('.tab-btn-mypage');
    await myPageTab.trigger('click');
    await new Promise((resolve) => setTimeout(resolve, 50));

    // Find and click the history item
    const historyItem = wrapper.find('.history-item');
    expect(historyItem.exists()).toBe(true);
    await historyItem.trigger('click');
    await new Promise((resolve) => setTimeout(resolve, 50));

    // Assert that the stage details were fetched for the correct stage ID
    expect(fetchStageSpy).toHaveBeenCalledWith(1);

    // Verify modal elements are visible (review panel)
    const reviewView = wrapper.find('.mypage-review-view');
    expect(reviewView.exists()).toBe(true);
    expect(reviewView.text()).toContain('Heart Shape');

    // Verify modal board state matches the stage solution grid
    const vm = wrapper.vm as any;
    expect(vm.isReviewMode).toBe(true);
    expect(vm.modalBoard).not.toBeNull();
    expect(vm.modalBoard.rowCount).toBe(3);
    expect(vm.modalBoard.colCount).toBe(3);
    expect(vm.modalBoard.currentGrid).toEqual([[0, 1, 0], [1, 1, 1], [0, 1, 0]]);

    // Click close/back button and verify modal is closed
    const closeBtn = wrapper.find('.mypage-popup-back-btn');
    expect(closeBtn.exists()).toBe(true);
    await closeBtn.trigger('click');
    await new Promise((resolve) => setTimeout(resolve, 50));

    expect(wrapper.find('.mypage-review-view').exists()).toBe(false);
    expect(vm.isReviewMode).toBe(false);
  });

  it('should render AI daily puzzles list, select AI puzzle, and submit clearStage with difficulty HARD', async () => {
    const mockStages = [{ id: 1, name: 'Heart Shape', width: 5, height: 5 }];
    const mockStageDetails = { 
      id: 7, 
      name: 'AI Puzzle', 
      width: 10, 
      height: 10, 
      solutionGrid: [
        [0,0,0,0,0,0,0,0,0,0],
        [0,0,0,0,0,0,0,0,0,0],
        [0,0,0,0,0,0,0,0,0,0],
        [0,0,0,0,0,0,0,0,0,0],
        [0,0,0,0,1,1,0,0,0,0],
        [0,0,0,0,1,1,0,0,0,0],
        [0,0,0,0,0,0,0,0,0,0],
        [0,0,0,0,0,0,0,0,0,0],
        [0,0,0,0,0,0,0,0,0,0],
        [0,0,0,0,0,0,0,0,0,0]
      ] 
    };
    const mockAiStages = [{ id: 7, name: 'AI Puzzle', width: 10, height: 10 }];
    const mockRankings = [{ id: 3, username: 'Player3', xp: 1000, level: 5 }];

    vi.spyOn(stageApi, 'fetchStages').mockResolvedValue(mockStages);
    const fetchAiStagesSpy = vi.spyOn(stageApi, 'fetchAiStages').mockResolvedValue(mockAiStages);
    const fetchStageSpy = vi.spyOn(stageApi, 'fetchStageById').mockResolvedValue(mockStageDetails);
    vi.spyOn(userApi, 'fetchRanking').mockResolvedValue(mockRankings);
    const clearStageSpy = vi.spyOn(userApi, 'clearStage').mockResolvedValue({ id: 1, username: 'Player1', xp: 250, level: 2 });

    const wrapper = mount(App);
    await new Promise((resolve) => setTimeout(resolve, 50));

    expect(fetchAiStagesSpy).toHaveBeenCalled();

    // Check if AI Daily Puzzle section renders the list
    const aiOptions = wrapper.findAll('.ai-stage-select option');
    expect(aiOptions.length).toBeGreaterThan(0);
    expect(aiOptions[0].text()).toContain('AI Puzzle');

    // Click/Select AI Stage
    const aiSelect = wrapper.find('.ai-stage-select');
    expect(aiSelect.exists()).toBe(true);
    const option = aiSelect.find('option[value="7"]');
    expect(option.exists()).toBe(true);
    (option.element as HTMLOptionElement).selected = true;
    await aiSelect.trigger('change');
    await new Promise((resolve) => setTimeout(resolve, 50));

    expect(fetchStageSpy).toHaveBeenCalledWith(7);

    // Solve the board (toggling the 4 center cells which are rotationally invariant)
    (wrapper.vm as any).board.toggleFill(4, 4);
    (wrapper.vm as any).board.toggleFill(4, 5);
    (wrapper.vm as any).board.toggleFill(5, 4);
    (wrapper.vm as any).board.toggleFill(5, 5);
    await (wrapper.vm as any).handleCellClick();

    // Should call clearStage with HARD difficulty for AI stage
    expect(clearStageSpy).toHaveBeenCalledWith(1, 'HARD', 7, expect.any(Number));
  });



  it('should switch to Admin Console tab, render list of admin stages, and trigger approval/actions', async () => {
    const mockStages = [{ id: 1, name: 'Heart Shape', width: 5, height: 5 }];
    const mockStageDetails = { id: 1, name: 'Heart Shape', width: 5, height: 5, solutionGrid: [[1]] };
    const mockRankings = [{ id: 3, username: 'Player3', xp: 1000, level: 5 }];

    vi.spyOn(stageApi, 'fetchStages').mockResolvedValue(mockStages);
    vi.spyOn(stageApi, 'fetchStageById').mockResolvedValue(mockStageDetails);
    vi.spyOn(userApi, 'fetchRanking').mockResolvedValue(mockRankings);

    const deleteSpy = vi.spyOn(adminApi, 'deleteStage').mockResolvedValue(undefined);

    const wrapper = mount(App);
    await new Promise((resolve) => setTimeout(resolve, 50));

    // Enable admin mode programmatically
    (wrapper.vm as any).isAdminMode = true;
    (wrapper.vm as any).isAdminLogged = true;
    await (wrapper.vm as any).onTabChange('admin');
    await wrapper.vm.$nextTick();
    await new Promise((resolve) => setTimeout(resolve, 50));

    // Verify admin dashboard elements are rendered
    expect(wrapper.find('.admin-backoffice-view').exists()).toBe(true);
    const stageItems = wrapper.findAll('.admin-stage-item');
    expect(stageItems.length).toBe(2);
    expect(stageItems[0].text()).toContain('Seeded Stage 1');
    expect(stageItems[0].text()).toContain('12 / 45');
    expect(stageItems[1].text()).toContain('AI Pending Stage');
    expect(stageItems[1].text()).toContain('0 / 0');

    // Click Delete on the active stage (first item)
    // Setup window.confirm mock
    const originalConfirm = window.confirm;
    window.confirm = vi.fn().mockReturnValue(true);
    const deleteBtn = stageItems[0].find('.btn-delete');
    expect(deleteBtn.exists()).toBe(true);
    await deleteBtn.trigger('click');
    expect(deleteSpy).toHaveBeenCalledWith(1);
    expect(window.confirm).toHaveBeenCalled();
    window.confirm = originalConfirm;

  });

  it('should render admin login card if not logged in, and handle successful login', async () => {
    const mockStages = [{ id: 1, name: 'Heart Shape', width: 5, height: 5 }];
    const mockStageDetails = { id: 1, name: 'Heart Shape', width: 5, height: 5, solutionGrid: [[1]] };
    const mockRankings = [{ id: 3, username: 'Player3', xp: 1000, level: 5 }];

    vi.spyOn(stageApi, 'fetchStages').mockResolvedValue(mockStages);
    vi.spyOn(stageApi, 'fetchStageById').mockResolvedValue(mockStageDetails);
    vi.spyOn(userApi, 'fetchRanking').mockResolvedValue(mockRankings);

    const loginSpy = vi.spyOn(adminApi, 'loginAdmin').mockResolvedValue('fake-token');

    const wrapper = mount(App);
    await new Promise((resolve) => setTimeout(resolve, 50));

    // Enable admin mode programmatically, but keep logged = false
    (wrapper.vm as any).isAdminMode = true;
    (wrapper.vm as any).isAdminLogged = false;
    await wrapper.vm.$nextTick();

    // Verify login card exists
    expect(wrapper.find('.admin-login-card').exists()).toBe(true);

    // Input credentials
    await wrapper.find('.admin-username-input').setValue('admin');
    await wrapper.find('.admin-password-input').setValue('admin123!');

    // Submit form
    await wrapper.find('.admin-login-form').trigger('submit.prevent');
    await new Promise((resolve) => setTimeout(resolve, 50));

    expect(loginSpy).toHaveBeenCalledWith('admin', 'admin123!');
    expect((wrapper.vm as any).isAdminLogged).toBe(true);
    expect(wrapper.find('.admin-login-card').exists()).toBe(false);
    expect(wrapper.find('.admin-console-content').exists()).toBe(true);
  });

  it('should filter and sort admin stages list in the back office', async () => {
    const mockStages = [{ id: 1, name: 'Heart Shape', width: 5, height: 5 }];
    const mockStageDetails = { id: 1, name: 'Heart Shape', width: 5, height: 5, solutionGrid: [[1]] };
    const mockRankings = [{ id: 3, username: 'Player3', xp: 1000, level: 5 }];

    vi.spyOn(stageApi, 'fetchStages').mockResolvedValue(mockStages);
    vi.spyOn(stageApi, 'fetchStageById').mockResolvedValue(mockStageDetails);
    vi.spyOn(userApi, 'fetchRanking').mockResolvedValue(mockRankings);

    // Mock admin stages list with 3 stages of different attributes
    vi.mocked(adminApi.fetchAdminStages).mockResolvedValue([
      { id: 2, name: 'B Stage', width: 10, height: 10, active: true, approved: true, solutionGrid: [] },
      { id: 1, name: 'A Stage', width: 5, height: 5, active: false, approved: true, solutionGrid: [] },
      { id: 3, name: 'C Stage', width: 15, height: 15, active: true, approved: true, solutionGrid: [] }
    ]);

    const wrapper = mount(App);
    await new Promise((resolve) => setTimeout(resolve, 50));

    // Enable admin mode and set authenticated
    (wrapper.vm as any).isAdminMode = true;
    (wrapper.vm as any).isAdminLogged = true;
    await (wrapper.vm as any).onTabChange('admin');
    await wrapper.vm.$nextTick();
    await new Promise((resolve) => setTimeout(resolve, 50));

    // Initially, verify all 3 stages are rendered
    let stageItems = wrapper.findAll('.admin-stage-item');
    expect(stageItems.length).toBe(3);

    // 1. Filter by search query "B"
    const searchInput = wrapper.find('.admin-search-input');
    expect(searchInput.exists()).toBe(true);
    await searchInput.setValue('B');
    await wrapper.vm.$nextTick();

    stageItems = wrapper.findAll('.admin-stage-item');
    expect(stageItems.length).toBe(1);
    expect(stageItems[0].text()).toContain('B Stage');

    // Reset search query
    await searchInput.setValue('');
    await wrapper.vm.$nextTick();

    // 2. Filter by status: Inactive
    const statusSelect = wrapper.find('.admin-status-filter');
    expect(statusSelect.exists()).toBe(true);
    await statusSelect.setValue('Inactive');
    await wrapper.vm.$nextTick();

    stageItems = wrapper.findAll('.admin-stage-item');
    expect(stageItems.length).toBe(1);
    expect(stageItems[0].text()).toContain('A Stage');

    // Reset status filter
    await statusSelect.setValue('All');
    await wrapper.vm.$nextTick();

    // 3. Filter by size: 10x10
    const sizeSelect = wrapper.find('.admin-size-filter');
    expect(sizeSelect.exists()).toBe(true);
    await sizeSelect.setValue('10');
    await wrapper.vm.$nextTick();

    stageItems = wrapper.findAll('.admin-stage-item');
    expect(stageItems.length).toBe(1);
    expect(stageItems[0].text()).toContain('B Stage');

    // Reset size filter
    await sizeSelect.setValue('All');
    await wrapper.vm.$nextTick();

    // 4. Sort by Name (click Name header)
    const nameHeader = wrapper.find('.admin-th-name');
    expect(nameHeader.exists()).toBe(true);

    // First click: Sorts Name Ascending (A Stage -> B Stage -> C Stage)
    await nameHeader.trigger('click');
    await wrapper.vm.$nextTick();
    stageItems = wrapper.findAll('.admin-stage-item');
    expect(stageItems[0].text()).toContain('A Stage');
    expect(stageItems[1].text()).toContain('B Stage');
    expect(stageItems[2].text()).toContain('C Stage');

    // Second click: Sorts Name Descending (C Stage -> B Stage -> A Stage)
    await nameHeader.trigger('click');
    await wrapper.vm.$nextTick();
    stageItems = wrapper.findAll('.admin-stage-item');
    expect(stageItems[0].text()).toContain('C Stage');
    expect(stageItems[1].text()).toContain('B Stage');
    expect(stageItems[2].text()).toContain('A Stage');
  });

  it('should display rotating logo spinner when loading board data', async () => {
    const mockStages = [{ id: 1, name: 'Heart Shape', width: 5, height: 5 }];
    const mockRankings = [{ id: 3, username: 'Player3', xp: 1000, level: 5 }];

    vi.spyOn(stageApi, 'fetchStages').mockResolvedValue(mockStages);
    let resolveStageDetails: any;
    const stageDetailsPromise = new Promise((resolve) => {
      resolveStageDetails = resolve;
    });
    vi.spyOn(stageApi, 'fetchStageById').mockReturnValue(stageDetailsPromise as any);
    vi.spyOn(userApi, 'fetchRanking').mockResolvedValue(mockRankings);

    const wrapper = mount(App);
    await new Promise((resolve) => setTimeout(resolve, 10));

    expect(wrapper.find('.spinner-logo').exists()).toBe(true);
    expect(wrapper.find('.loading-text').text()).toContain('Loading board data...');

    resolveStageDetails({ id: 1, name: 'Heart Shape', width: 5, height: 5, solutionGrid: [[1]] });
    await new Promise((resolve) => setTimeout(resolve, 10));
  });

  it('should display error message and retry button when stage load fails', async () => {
    const mockStages = [{ id: 1, name: 'Heart Shape', width: 5, height: 5 }];
    const mockRankings = [{ id: 3, username: 'Player3', xp: 1000, level: 5 }];

    vi.spyOn(stageApi, 'fetchStages').mockResolvedValue(mockStages);
    vi.spyOn(stageApi, 'fetchStageById').mockRejectedValue(new Error('500 Internal Server Error'));
    vi.spyOn(userApi, 'fetchRanking').mockResolvedValue(mockRankings);

    const wrapper = mount(App);
    await new Promise((resolve) => setTimeout(resolve, 50));

    expect(wrapper.find('.error-state').exists()).toBe(true);
    expect(wrapper.find('.error-text').text()).toContain('Failed to load');
    expect(wrapper.find('.retry-btn').exists()).toBe(true);

    const fetchStageSpy = vi.spyOn(stageApi, 'fetchStageById').mockResolvedValue({ id: 1, name: 'Heart Shape', width: 5, height: 5, solutionGrid: [[1]] } as any);
    
    await wrapper.find('.retry-btn').trigger('click');
    await new Promise((resolve) => setTimeout(resolve, 50));

    expect(fetchStageSpy).toHaveBeenCalledWith(1);
    expect(wrapper.find('.error-state').exists()).toBe(false);
    expect(wrapper.find('.canvas-wrapper-container').exists()).toBe(true);
  });

  it('should display server error message when API returns 502 status', async () => {
    const mockStages = [{ id: 1, name: 'Heart Shape', width: 5, height: 5 }];
    const mockRankings = [{ id: 3, username: 'Player3', xp: 1000, level: 5 }];

    vi.spyOn(stageApi, 'fetchStages').mockResolvedValue(mockStages);
    const error502 = {
      response: {
        status: 502,
        statusText: 'Bad Gateway'
      }
    };
    vi.spyOn(stageApi, 'fetchStageById').mockRejectedValue(error502);
    vi.spyOn(userApi, 'fetchRanking').mockResolvedValue(mockRankings);

    const wrapper = mount(App);
    await new Promise((resolve) => setTimeout(resolve, 50));

    expect(wrapper.find('.error-state').exists()).toBe(true);
    expect(wrapper.find('.error-text').text()).toContain('server error (502)');
  });

  it('should switch to Home tab, render telemetry dashboard, and switch back to Play tab when CTA button is clicked', async () => {
    const mockStages = [{ id: 1, name: 'Heart Shape', width: 5, height: 5 }];
    const mockStageDetails = { id: 1, name: 'Heart Shape', width: 5, height: 5, solutionGrid: [[1]] };
    const mockRankings = [{ id: 3, username: 'Player3', xp: 1000, level: 5 }];

    vi.spyOn(stageApi, 'fetchStages').mockResolvedValue(mockStages);
    vi.spyOn(stageApi, 'fetchStageById').mockResolvedValue(mockStageDetails);
    vi.spyOn(userApi, 'fetchRanking').mockResolvedValue(mockRankings);

    const wrapper = mount(App);
    await new Promise((resolve) => setTimeout(resolve, 50));

    // Under test, defaults to play tab
    expect((wrapper.vm as any).currentTab).toBe('play');

    // Click header logo to go to Home tab
    const headerLogo = wrapper.find('.app-header .logo-wrapper');
    expect(headerLogo.exists()).toBe(true);
    await headerLogo.trigger('click');
    await new Promise((resolve) => setTimeout(resolve, 50));

    expect((wrapper.vm as any).currentTab).toBe('home');
    expect(wrapper.find('.home-dashboard').exists()).toBe(true);

    // Verify homepage content
    const dashboardText = wrapper.find('.home-dashboard').text();
    expect(dashboardText).toContain('rogic.io');
    expect(wrapper.find('.landing-play-btn').exists()).toBe(true);

    // Click CTA play button inside home dashboard
    const ctaBtn = wrapper.find('.landing-play-btn');
    expect(ctaBtn.exists()).toBe(true);
    await ctaBtn.trigger('click');
    await new Promise((resolve) => setTimeout(resolve, 50));

    // Verify it switches back to play tab
    expect((wrapper.vm as any).currentTab).toBe('play');
    expect(wrapper.find('.home-dashboard').exists()).toBe(false);
  });

  it('should migrate guest history when user logs in', async () => {
    localStorage.clear();
    // Setup stored token and guest history
    localStorage.setItem('nemologic_id_token', 'mockHeader.eyzleHAiOjk5OTk5OTk5OTl9.mockSignature');
    localStorage.setItem('guest_cleared_stages', JSON.stringify([5]));
    localStorage.setItem('guest_histories', JSON.stringify([
      { id: 111, userId: 0, stageId: 5, stageName: 'Easy Stage', clearedAt: '2026-06-08T22:40:40', xpEarned: 100, elapsedTime: 45, proofToken: 'mock-verify-token-5' }
    ]));

    const mockStages = [{ id: 1, name: 'Heart Shape', width: 5, height: 5 }];
    const mockStageDetails = { id: 1, name: 'Heart Shape', width: 5, height: 5, solutionGrid: [[0, 1, 0], [1, 1, 1], [0, 1, 0]] };
    const mockRankings = [{ id: 3, username: 'Player3', xp: 1000, level: 5 }];
    const mockServerUser = { id: 42, username: 'GoogleUser', xp: 100, level: 2, email: 'user@example.com' };
    const mockUpdatedUser = { id: 42, username: 'GoogleUser', xp: 150, level: 2, email: 'user@example.com' };

    vi.spyOn(stageApi, 'fetchStages').mockResolvedValue(mockStages);
    vi.spyOn(stageApi, 'fetchStageById').mockResolvedValue(mockStageDetails);
    vi.spyOn(userApi, 'fetchRanking').mockResolvedValue(mockRankings);
    vi.spyOn(userApi, 'fetchMeFromServer').mockResolvedValue(mockServerUser);
    const syncSpy = vi.spyOn(userApi, 'syncGuestHistory').mockResolvedValue(mockUpdatedUser);

    const wrapper = mount(App);
    await new Promise((resolve) => setTimeout(resolve, 50));

    // Verify syncGuestHistory was called
    expect(syncSpy).toHaveBeenCalledWith(42, [
      { stageId: 5, elapsedTime: 45, proofToken: 'mock-verify-token-5' }
    ]);

    // Verify localStorage has been cleared
    expect(localStorage.getItem('guest_cleared_stages')).toBeNull();
    expect(localStorage.getItem('guest_histories')).toBeNull();

    // Verify current user XP was updated
    expect((wrapper.vm as any).currentUser.xp).toBe(150);
  });

  it('should deactivate AI stage and load regular stage when size filter changes', async () => {
    const mockStages = [
      { id: 1, name: 'Small Puzzle', width: 5, height: 5 },
      { id: 2, name: 'Large Puzzle', width: 10, height: 10 }
    ];
    const mockAiStages = [{ id: 7, name: 'AI Puzzle', width: 10, height: 10 }];
    const mockStageDetails = { id: 2, name: 'Large Puzzle', width: 10, height: 10, solutionGrid: [[1]] };

    vi.spyOn(stageApi, 'fetchStages').mockResolvedValue(mockStages);
    vi.spyOn(stageApi, 'fetchAiStages').mockResolvedValue(mockAiStages);
    const fetchStageSpy = vi.spyOn(stageApi, 'fetchStageById').mockResolvedValue(mockStageDetails);

    const wrapper = mount(App);
    await new Promise((resolve) => setTimeout(resolve, 50));

    // Ensure the size filter is initially '5'
    const vm = wrapper.vm as any;
    vm.selectedPlaySizeFilter = '5';
    await wrapper.vm.$nextTick();

    // Select AI stage via the hidden select element
    const aiSelect = wrapper.find('.ai-stage-select');
    expect(aiSelect.exists()).toBe(true);
    const option = aiSelect.find('option[value="7"]');
    expect(option.exists()).toBe(true);
    (option.element as HTMLOptionElement).selected = true;
    await aiSelect.trigger('change');
    await new Promise((resolve) => setTimeout(resolve, 50));

    // Change size filter to '10' (Large Puzzle)
    vm.selectedPlaySizeFilter = '10';
    await wrapper.vm.$nextTick();
    await new Promise((resolve) => setTimeout(resolve, 50));

    // Assert that AI stage is deactivated and regular stage is loaded
    expect(vm.selectedAiStageId).toBeNull();
    expect(vm.selectedStageId).toBe(2);
    expect(fetchStageSpy).toHaveBeenCalledWith(2);
  });

  it('should trigger confetti and clearStage API via solve-animation-complete integration in test env', async () => {
    const mockStages = [{ id: 1, name: 'Heart Shape', width: 5, height: 5 }];
    const mockStageDetails = { id: 1, name: 'Heart Shape', width: 5, height: 5, solutionGrid: [[1]] };
    const mockRankings = [{ id: 3, username: 'Player3', xp: 1000, level: 5 }];

    vi.spyOn(stageApi, 'fetchStages').mockResolvedValue(mockStages);
    vi.spyOn(stageApi, 'fetchStageById').mockResolvedValue(mockStageDetails);
    vi.spyOn(userApi, 'fetchRanking').mockResolvedValue(mockRankings);
    const clearStageSpy = vi.spyOn(userApi, 'clearStage').mockResolvedValue({ id: 1, username: 'Player1', xp: 250, level: 2 });

    const wrapper = mount(App);
    await new Promise((resolve) => setTimeout(resolve, 50));

    // Initially solved is false, solveAnimationComplete is false
    const vm = wrapper.vm as any;
    expect(vm.solved).toBe(false);
    expect(vm.solveAnimationComplete).toBe(false);

    // Solve the board
    vm.board.toggleFill(0, 0);
    await vm.handleCellClick();
    await wrapper.vm.$nextTick();

    // Under test environment, the animation resolves instantly, triggering the full chain synchronously
    expect(vm.solved).toBe(true);
    expect(vm.solveAnimationComplete).toBe(true);
    expect(clearStageSpy).toHaveBeenCalled();
  });

  it('should save progress to localStorage when cell changes, and restore it upon stage load', async () => {
    localStorage.clear();
    const mockStages = [{ id: 1, name: 'Heart Shape', width: 5, height: 5 }];
    const mockStageDetails = { id: 1, name: 'Heart Shape', width: 5, height: 5, solutionGrid: [[1, 0], [0, 1]] };

    vi.spyOn(stageApi, 'fetchStages').mockResolvedValue(mockStages);
    vi.spyOn(stageApi, 'fetchStageById').mockResolvedValue(mockStageDetails);

    const wrapper = mount(App);
    await new Promise((resolve) => setTimeout(resolve, 50));

    const vm = wrapper.vm as any;
    // Load stage
    vm.selectedStageId = 1;
    await vm.loadStageDetails(1);
    await wrapper.vm.$nextTick();

    // Verify initially empty/empty progress key
    const progressKey = 'rogic_progress_stage_1';
    expect(localStorage.getItem(progressKey)).toBeNull();

    // Modify a cell to trigger progress save
    vm.board.setCell(0, 0, 1);
    await vm.handleCellClick();

    // Verify progress key is saved in localStorage
    const saved = JSON.parse(localStorage.getItem(progressKey) || '{}');
    expect(saved.stageId).toBe(1);
    expect(saved.currentGrid[0][0]).toBe(1);

    // Now reload the stage and verify it restores cell state
    await vm.loadStageDetails(1);
    await wrapper.vm.$nextTick();

    expect(vm.board.currentGrid[0][0]).toBe(1);
  });

  it('should push stage_start and stage_clear events to dataLayer during puzzle play flow', async () => {
    const mockStages = [{ id: 7, name: 'Mini Stage', width: 1, height: 1 }];
    const mockStageDetails = { id: 7, name: 'Mini Stage', width: 1, height: 1, solutionGrid: [[1]] };
    const mockRankings = [{ id: 3, username: 'Player3', xp: 1000, level: 5 }];

    vi.spyOn(stageApi, 'fetchStages').mockResolvedValue(mockStages);
    vi.spyOn(stageApi, 'fetchStageById').mockResolvedValue(mockStageDetails);
    vi.spyOn(userApi, 'fetchRanking').mockResolvedValue(mockRankings);
    vi.spyOn(userApi, 'clearStage').mockResolvedValue({ id: 1, username: 'Player1', xp: 250, level: 2 });

    const wrapper = mount(App);
    await new Promise((resolve) => setTimeout(resolve, 50));

    const vm = wrapper.vm as any;

    // Explicitly load details
    vm.selectedStageId = 7;
    await vm.loadStageDetails(7);
    await wrapper.vm.$nextTick();

    // Verify stage_start was pushed
    const startEvent = (window as any).dataLayer.find((e: any) => e.event === 'stage_start');
    expect(startEvent).toBeDefined();
    expect(startEvent.stageId).toBe(7);
    expect(startEvent.stageName).toBe('Mini Stage');

    // Force solve the board
    vm.board.toggleFill(0, 0);
    await vm.handleCellClick();
    await wrapper.vm.$nextTick();

    // Verify stage_clear was pushed
    const clearEvent = (window as any).dataLayer.find((e: any) => e.event === 'stage_clear');
    expect(clearEvent).toBeDefined();
    expect(clearEvent.stageId).toBe(7);
    expect(clearEvent.stageName).toBe('Mini Stage');
  });

  it('should reset demoSolveAnimationComplete and demoSolved when navigating away from Home page', async () => {
    const wrapper = mount(App);
    const vm = wrapper.vm as any;

    // Trigger skip intro which normally leaves demoSolveAnimationComplete as true
    vm.skipIntro();
    expect(vm.demoSolveAnimationComplete).toBe(true);
    expect(vm.demoSolved).toBe(true);

    // Navigate to play tab
    await vm.onTabChange('play');

    // Both should be reset to false to prevent duplicate confetti triggers
    expect(vm.demoSolveAnimationComplete).toBe(false);
    expect(vm.demoSolved).toBe(false);
  });
});
