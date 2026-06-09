import axios from 'axios';

export interface StageSummary {
  id: number;
  name: string;
  width: number;
  height: number;
}

export interface StageDetails extends StageSummary {
  solutionGrid: number[][];
}

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL
  ? `${import.meta.env.VITE_API_BASE_URL}/api/stages`
  : (import.meta.env.PROD ? '/api/stages' : 'http://localhost:8080/api/stages');

export async function fetchStages(): Promise<StageSummary[]> {
  const response = await axios.get<StageSummary[]>(API_BASE_URL);
  return response.data;
}

export async function fetchStageById(id: number): Promise<StageDetails> {
  const response = await axios.get<StageDetails>(`${API_BASE_URL}/${id}`);
  return response.data;
}

export async function fetchAiStages(): Promise<StageSummary[]> {
  const response = await axios.get<StageSummary[]>(API_BASE_URL);
  return response.data.filter(s => s.name.includes('AI'));
}
