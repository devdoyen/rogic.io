import { describe, it, expect, vi } from 'vitest';
import axios from 'axios';
import { fetchRanking, clearStage } from './userApi';

vi.mock('axios');

describe('userApi TDD Red Phase', () => {
  it('fetchRanking should call GET /ranking and return user ranking list', async () => {
    const mockRankings = [
      { id: 3, username: 'Charlie', xp: 1000, level: 5 },
      { id: 2, username: 'Bob', xp: 500, level: 3 },
      { id: 1, username: 'Alice', xp: 200, level: 2 }
    ];

    vi.mocked(axios.get).mockResolvedValue({ data: mockRankings });

    const result = await fetchRanking();
    expect(axios.get).toHaveBeenCalledWith('http://localhost:8080/api/users/ranking');
    expect(result).toEqual(mockRankings);
  });

  it('clearStage should call POST /{id}/clear with difficulty query param', async () => {
    const mockUpdatedUser = { id: 1, username: 'Alice', xp: 250, level: 2 };
    vi.mocked(axios.post).mockResolvedValue({ data: mockUpdatedUser });

    const result = await clearStage(1, 'EASY');
    expect(axios.post).toHaveBeenCalledWith('http://localhost:8080/api/users/1/clear', null, {
      params: { difficulty: 'EASY' }
    });
    expect(result).toEqual(mockUpdatedUser);
  });
});
