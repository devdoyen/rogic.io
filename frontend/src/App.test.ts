import { describe, it, expect, vi, beforeEach } from 'vitest';
import { mount } from '@vue/test-utils';
import App from './App.vue';
import * as stageApi from './api/stageApi';
import * as userApi from './api/userApi';

vi.mock('./api/stageApi');
vi.mock('./api/userApi');

describe('App.vue Leaderboard Integration TDD', () => {
  beforeEach(() => {
    vi.restoreAllMocks();
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
      { id: 3, username: 'Charlie', xp: 1000, level: 5 },
      { id: 2, username: 'Bob', xp: 500, level: 3 },
      { id: 1, username: 'Alice', xp: 200, level: 2 }
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
    expect(items[0].text()).toContain('Charlie');
    expect(items[0].text()).toContain('Lv.5');
    expect(items[0].text()).toContain('1000 XP');
  });
});
