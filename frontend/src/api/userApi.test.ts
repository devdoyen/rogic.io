import { describe, it, expect, vi } from 'vitest';
import axios from 'axios';
import { fetchRanking, clearStage, registerAnonymousUser, fetchUserHistory } from './userApi';

vi.mock('axios');

describe('userApi TDD Red Phase', () => {
  it('fetchRanking should call GET /ranking and return user ranking list', async () => {
    const mockRankings = [
      { id: 3, username: 'Player3', xp: 1000, level: 5 },
      { id: 2, username: 'Player2', xp: 500, level: 3 },
      { id: 1, username: 'Player1', xp: 200, level: 2 }
    ];

    vi.mocked(axios.get).mockResolvedValue({ data: mockRankings });

    const result = await fetchRanking();
    expect(axios.get).toHaveBeenCalledWith('http://localhost:8080/api/users/ranking');
    expect(result).toEqual(mockRankings);
  });

  it('clearStage should call POST /{id}/clear with difficulty query param', async () => {
    const mockUpdatedUser = { id: 1, username: 'Player1', xp: 250, level: 2 };
    vi.mocked(axios.post).mockResolvedValue({ data: mockUpdatedUser });

    const result = await clearStage(1, 'EASY');
    expect(axios.post).toHaveBeenCalledWith('http://localhost:8080/api/users/1/clear', null, {
      params: { difficulty: 'EASY' }
    });
    expect(result).toEqual(mockUpdatedUser);
  });

  it('clearStage should call POST /{id}/clear with difficulty, stageId and elapsedTime query params', async () => {
    const mockUpdatedUser = { id: 1, username: 'Player1', xp: 250, level: 2 };
    vi.mocked(axios.post).mockResolvedValue({ data: mockUpdatedUser });

    const result = await clearStage(1, 'EASY', 2, 120);
    expect(axios.post).toHaveBeenCalledWith('http://localhost:8080/api/users/1/clear', null, {
      params: { difficulty: 'EASY', stageId: 2, elapsedTime: 120 }
    });
    expect(result).toEqual(mockUpdatedUser);
  });

  it('fetchUserHistory should call GET /{id}/history and return list of user clear history', async () => {
    const mockHistory = [
      { id: 1, userId: 1, stageId: 2, stageName: 'Heart Shape', clearedAt: '2026-06-08T22:40:40', xpEarned: 50, elapsedTime: 120 }
    ];
    vi.mocked(axios.get).mockResolvedValue({ data: mockHistory });

    const result = await fetchUserHistory(1);
    expect(axios.get).toHaveBeenCalledWith('http://localhost:8080/api/users/1/history');
    expect(result).toEqual(mockHistory);
  });

  it('registerAnonymousUser should call POST /register and return new anonymous user data', async () => {
    const mockNewUser = { id: 4, username: 'Anonymous-abc', xp: 0, level: 1, uuid: 'some-uuid' };
    vi.mocked(axios.post).mockResolvedValue({ data: mockNewUser });

    const result = await registerAnonymousUser();
    expect(axios.post).toHaveBeenCalledWith('http://localhost:8080/api/users/register');
    expect(result).toEqual(mockNewUser);
  });
});


