import { describe, it, expect, vi } from 'vitest';
import axios from 'axios';
import { fetchStages, fetchStageById, fetchAiStages } from './stageApi';

vi.mock('axios');

describe('stageApi TDD Red Phase', () => {
  it('fetchStages should call get and return stage summaries', async () => {
    const mockData = [
      { id: 1, name: 'Heart Shape', width: 5, height: 5 }
    ];
    
    vi.mocked(axios.get).mockResolvedValue({ data: mockData });

    const result = await fetchStages();
    expect(axios.get).toHaveBeenCalledWith('http://localhost:8080/api/stages');
    expect(result).toEqual(mockData);
  });

  it('fetchStageById should call get with ID and return detailed stage', async () => {
    const mockDetails = {
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
    
    vi.mocked(axios.get).mockResolvedValue({ data: mockDetails });

    const result = await fetchStageById(1);
    expect(axios.get).toHaveBeenCalledWith('http://localhost:8080/api/stages/1');
    expect(result).toEqual(mockDetails);
  });

  it('fetchAiStages should call get and return all stage summaries', async () => {
    const mockData = [
      { id: 1, name: 'Heart Shape', width: 5, height: 5 },
      { id: 2, name: 'AI Puzzle', width: 5, height: 5 }
    ];
    
    vi.mocked(axios.get).mockResolvedValue({ data: mockData });

    const result = await fetchAiStages();
    expect(axios.get).toHaveBeenCalledWith('http://localhost:8080/api/stages');
    expect(result).toEqual(mockData);
  });
});
