import { describe, it, expect, vi, beforeEach } from 'vitest';
import { mount } from '@vue/test-utils';
import App from './App.vue';
import * as stageApi from './api/stageApi';
import * as userApi from './api/userApi';
import * as adminApi from './api/adminApi';

vi.mock('./api/stageApi');
vi.mock('./api/userApi');
vi.mock('./api/adminApi');

describe('App.vue Leaderboard Integration TDD', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    localStorage.clear();
    vi.mocked(userApi.registerAnonymousUser).mockResolvedValue({
      id: 1,
      uuid: 'temp-uuid',
      username: 'Player1',
      xp: 200,
      level: 2
    });
    vi.mocked(adminApi.fetchAdminStages).mockResolvedValue([
      { id: 1, name: 'Seeded Stage 1', width: 5, height: 5, active: true, approved: true, solutionGrid: [[1]] },
      { id: 9, name: 'AI Pending Stage', width: 5, height: 5, active: false, approved: false, solutionGrid: [[1]] }
    ]);
    vi.mocked(userApi.fetchTelemetryStats).mockResolvedValue({
      dailyUniqueVisitors: 5,
      totalUniqueVisitors: 20,
      totalVisits: 100,
      totalAttempts: 50,
      totalClears: 30,
      uptimeRatio: 0.9998,
      mtbf: 720,
      mttr: 0.8
    });
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

  it('should call registerAnonymousUser on mount if no session exists', async () => {
    localStorage.clear();
    const mockStages = [{ id: 1, name: 'Heart Shape', width: 5, height: 5 }];
    const mockStageDetails = { id: 1, name: 'Heart Shape', width: 5, height: 5, solutionGrid: [[0, 1, 0], [1, 1, 1], [0, 1, 0]] };
    const mockRankings = [{ id: 3, username: 'Player3', xp: 1000, level: 5 }];
    const mockNewUser = { id: 4, username: 'Anonymous-123', xp: 0, level: 1, uuid: 'temp-uuid' };

    vi.spyOn(stageApi, 'fetchStages').mockResolvedValue(mockStages);
    vi.spyOn(stageApi, 'fetchStageById').mockResolvedValue(mockStageDetails);
    vi.spyOn(userApi, 'fetchRanking').mockResolvedValue(mockRankings);
    const registerSpy = vi.spyOn(userApi, 'registerAnonymousUser').mockResolvedValue(mockNewUser);

    mount(App);
    await new Promise((resolve) => setTimeout(resolve, 50));

    expect(registerSpy).toHaveBeenCalled();
  });

  it('should not call registerAnonymousUser on mount if session already exists', async () => {
    localStorage.clear();
    const mockSession = {
      id: 42,
      uuid: 'abc-123-uuid',
      username: 'AnonymousHero',
      xp: 0,
      level: 1
    };
    localStorage.setItem('nemologic_user_session', JSON.stringify(mockSession));

    const mockStages = [{ id: 1, name: 'Heart Shape', width: 5, height: 5 }];
    const mockStageDetails = { id: 1, name: 'Heart Shape', width: 5, height: 5, solutionGrid: [[0, 1, 0], [1, 1, 1], [0, 1, 0]] };
    const mockRankings = [{ id: 3, username: 'Player3', xp: 1000, level: 5 }];

    vi.spyOn(stageApi, 'fetchStages').mockResolvedValue(mockStages);
    vi.spyOn(stageApi, 'fetchStageById').mockResolvedValue(mockStageDetails);
    vi.spyOn(userApi, 'fetchRanking').mockResolvedValue(mockRankings);
    const registerSpy = vi.spyOn(userApi, 'registerAnonymousUser');

    mount(App);
    await new Promise((resolve) => setTimeout(resolve, 50));

    expect(registerSpy).not.toHaveBeenCalled();
  });

  it('should call logVisit on mount with the user session UUID', async () => {
    localStorage.clear();
    const mockStages = [{ id: 1, name: 'Heart Shape', width: 5, height: 5 }];
    const mockStageDetails = { id: 1, name: 'Heart Shape', width: 5, height: 5, solutionGrid: [[1]] };
    const mockRankings = [{ id: 3, username: 'Player3', xp: 1000, level: 5 }];

    vi.spyOn(stageApi, 'fetchStages').mockResolvedValue(mockStages);
    vi.spyOn(stageApi, 'fetchStageById').mockResolvedValue(mockStageDetails);
    vi.spyOn(userApi, 'fetchRanking').mockResolvedValue(mockRankings);
    
    const logVisitSpy = vi.spyOn(userApi, 'logVisit').mockResolvedValue(undefined as any);

    mount(App);
    await new Promise((resolve) => setTimeout(resolve, 50));

    expect(logVisitSpy).toHaveBeenCalledWith('temp-uuid');
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

    const wrapper = mount(App);
    await new Promise((resolve) => setTimeout(resolve, 50));

    // Click My Page tab
    const myPageTab = wrapper.find('.tab-btn-mypage');
    expect(myPageTab.exists()).toBe(true);
    await myPageTab.trigger('click');

    expect(historySpy).toHaveBeenCalledWith(1);

    // Check history item rendering
    const historyItems = wrapper.findAll('.history-item');
    expect(historyItems.length).toBe(1);
    expect(historyItems[0].text()).toContain('Heart Shape');
    expect(historyItems[0].text()).toContain('120s');
    expect(historyItems[0].text()).toContain('+50 XP');
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

    // Verify modal elements are visible
    const modalOverlay = wrapper.find('.modal-overlay');
    expect(modalOverlay.exists()).toBe(true);
    expect(wrapper.find('.modal-stage-info').text()).toContain('Heart Shape');

    // Verify modal board state matches the stage solution grid
    const vm = wrapper.vm as any;
    expect(vm.isModalOpen).toBe(true);
    expect(vm.modalBoard).not.toBeNull();
    expect(vm.modalBoard.rowCount).toBe(3);
    expect(vm.modalBoard.colCount).toBe(3);
    expect(vm.modalBoard.currentGrid).toEqual([[0, 1, 0], [1, 1, 1], [0, 1, 0]]);

    // Click close button and verify modal is closed
    const closeBtn = wrapper.find('.modal-close-btn');
    expect(closeBtn.exists()).toBe(true);
    await closeBtn.trigger('click');
    await new Promise((resolve) => setTimeout(resolve, 50));

    expect(wrapper.find('.modal-overlay').exists()).toBe(false);
    expect(vm.isModalOpen).toBe(false);
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

  it('should manage Help modal visibility: auto-open on first visit, manually open via Help button, close via close button', async () => {
    localStorage.clear(); // First-time visit scenario
    const mockStages = [{ id: 1, name: 'Heart Shape', width: 5, height: 5 }];
    const mockStageDetails = { id: 1, name: 'Heart Shape', width: 5, height: 5, solutionGrid: [[1]] };
    const mockRankings = [{ id: 3, username: 'Player3', xp: 1000, level: 5 }];

    vi.spyOn(stageApi, 'fetchStages').mockResolvedValue(mockStages);
    vi.spyOn(stageApi, 'fetchStageById').mockResolvedValue(mockStageDetails);
    vi.spyOn(userApi, 'fetchRanking').mockResolvedValue(mockRankings);

    const wrapper = mount(App);
    await new Promise((resolve) => setTimeout(resolve, 50));

    // Verify modal is open automatically
    expect((wrapper.vm as any).isHelpModalOpen).toBe(true);
    expect(wrapper.find('.help-modal-overlay').exists()).toBe(true);
    expect(localStorage.getItem('rotagic_visited')).toBe('true');

    // Click modal close/Start button
    const closeBtn = wrapper.find('.help-modal-overlay .modal-close-btn');
    expect(closeBtn.exists()).toBe(true);
    await closeBtn.trigger('click');
    await new Promise((resolve) => setTimeout(resolve, 50));

    // Verify modal is closed
    expect((wrapper.vm as any).isHelpModalOpen).toBe(false);
    expect(wrapper.find('.help-modal-overlay').exists()).toBe(false);

    // Verify manual open via Help button
    const helpBtn = wrapper.find('.help-toggle-btn');
    expect(helpBtn.exists()).toBe(true);
    await helpBtn.trigger('click');
    await new Promise((resolve) => setTimeout(resolve, 50));

    expect((wrapper.vm as any).isHelpModalOpen).toBe(true);
    expect(wrapper.find('.help-modal-overlay').exists()).toBe(true);
  });

  it('should not auto-open Help modal if user has visited before', async () => {
    localStorage.clear();
    localStorage.setItem('rotagic_visited', 'true'); // Visited scenario
    const mockStages = [{ id: 1, name: 'Heart Shape', width: 5, height: 5 }];
    const mockStageDetails = { id: 1, name: 'Heart Shape', width: 5, height: 5, solutionGrid: [[1]] };
    const mockRankings = [{ id: 3, username: 'Player3', xp: 1000, level: 5 }];

    vi.spyOn(stageApi, 'fetchStages').mockResolvedValue(mockStages);
    vi.spyOn(stageApi, 'fetchStageById').mockResolvedValue(mockStageDetails);
    vi.spyOn(userApi, 'fetchRanking').mockResolvedValue(mockRankings);

    const wrapper = mount(App);
    await new Promise((resolve) => setTimeout(resolve, 50));

    // Verify modal is NOT open automatically
    expect((wrapper.vm as any).isHelpModalOpen).toBe(false);
    expect(wrapper.find('.help-modal-overlay').exists()).toBe(false);
  });

  it('should switch to Admin Console tab, render list of admin stages, and trigger approval/actions', async () => {
    const mockStages = [{ id: 1, name: 'Heart Shape', width: 5, height: 5 }];
    const mockStageDetails = { id: 1, name: 'Heart Shape', width: 5, height: 5, solutionGrid: [[1]] };
    const mockRankings = [{ id: 3, username: 'Player3', xp: 1000, level: 5 }];

    vi.spyOn(stageApi, 'fetchStages').mockResolvedValue(mockStages);
    vi.spyOn(stageApi, 'fetchStageById').mockResolvedValue(mockStageDetails);
    vi.spyOn(userApi, 'fetchRanking').mockResolvedValue(mockRankings);

    const approveSpy = vi.spyOn(adminApi, 'approveStage').mockResolvedValue(undefined);
    const deleteSpy = vi.spyOn(adminApi, 'deleteStage').mockResolvedValue(undefined);
    const aiGenSpy = vi.spyOn(adminApi, 'generateAiStage').mockResolvedValue({ id: 10, name: 'Generated AI', width: 5, height: 5, solutionGrid: [[1]], active: false, approved: false } as any);

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
    expect(stageItems[1].text()).toContain('AI Pending Stage');

    // Click Approve on the pending stage (second item)
    const approveBtn = stageItems[1].find('.btn-approve');
    expect(approveBtn.exists()).toBe(true);
    await approveBtn.trigger('click');
    expect(approveSpy).toHaveBeenCalledWith(9);

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

    // Click AI Gen button
    const aiGenBtn = wrapper.find('.admin-ai-gen-btn');
    expect(aiGenBtn.exists()).toBe(true);
    const widthSelect = wrapper.find('.admin-ai-width-select');
    const heightSelect = wrapper.find('.admin-ai-height-select');
    expect(widthSelect.exists()).toBe(true);
    expect(heightSelect.exists()).toBe(true);
    await widthSelect.setValue('10');
    await heightSelect.setValue('15');
    // Mock window.alert to prevent blocking
    const originalAlert = window.alert;
    window.alert = vi.fn();
    await aiGenBtn.trigger('click');
    expect(aiGenSpy).toHaveBeenCalledWith(10, 15);
    expect(window.alert).toHaveBeenCalled();
    window.alert = originalAlert;
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
      { id: 1, name: 'A Stage', width: 5, height: 5, active: false, approved: false, solutionGrid: [] },
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

    // 2. Filter by status: Pending Approval
    const statusSelect = wrapper.find('.admin-status-filter');
    expect(statusSelect.exists()).toBe(true);
    await statusSelect.setValue('Pending');
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
    vi.spyOn(userApi, 'fetchTelemetryStats');

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
    expect(dashboardText).toContain('The next-generation Nonogram');

    // Click CTA play button inside home dashboard
    const ctaBtn = wrapper.find('.cta-play-btn');
    expect(ctaBtn.exists()).toBe(true);
    await ctaBtn.trigger('click');
    await new Promise((resolve) => setTimeout(resolve, 50));

    // Verify it switches back to play tab
    expect((wrapper.vm as any).currentTab).toBe('play');
    expect(wrapper.find('.home-dashboard').exists()).toBe(false);
  });
});


