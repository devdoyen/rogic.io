import { describe, it, expect, vi, beforeEach } from 'vitest';
import { mount } from '@vue/test-utils';
import App from './App.vue';
import * as stageApi from './api/stageApi';
import * as userApi from './api/userApi';

vi.mock('./api/stageApi');
vi.mock('./api/userApi');

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
    const mockStageDetails = { id: 7, name: 'AI Daily Puzzle', width: 1, height: 1, solutionGrid: [[1]] };
    const mockAiStages = [{ id: 7, name: 'AI Daily Puzzle', width: 1, height: 1 }];
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
    expect(aiOptions[0].text()).toContain('AI Daily Puzzle');

    // Click/Select AI Stage
    const aiSelect = wrapper.find('.ai-stage-select');
    expect(aiSelect.exists()).toBe(true);
    const option = aiSelect.find('option[value="7"]');
    expect(option.exists()).toBe(true);
    (option.element as HTMLOptionElement).selected = true;
    await aiSelect.trigger('change');
    await new Promise((resolve) => setTimeout(resolve, 50));

    expect(fetchStageSpy).toHaveBeenCalledWith(7);

    // Solve the board
    (wrapper.vm as any).board.toggleFill(0, 0);
    await (wrapper.vm as any).handleCellClick();

    // Should call clearStage with HARD difficulty for AI stage
    expect(clearStageSpy).toHaveBeenCalledWith(1, 'HARD', 7, expect.any(Number));
  });
});


