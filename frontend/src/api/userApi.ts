import axios from 'axios';

export interface User {
  id: number;
  username: string;
  xp: number;
  level: number;
  uuid?: string;
}

export interface HistoryResponse {
  id: number;
  userId: number;
  stageId: number;
  stageName: string;
  clearedAt: string;
  xpEarned: number;
  elapsedTime: number;
}

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL
  ? `${import.meta.env.VITE_API_BASE_URL}/api/users`
  : (import.meta.env.PROD ? '/api/users' : 'http://localhost:8080/api/users');

export async function fetchRanking(): Promise<User[]> {
  const response = await axios.get<User[]>(`${API_BASE_URL}/ranking`);
  return response.data;
}

export async function clearStage(userId: number, difficulty: string, stageId?: number, elapsedTime?: number): Promise<User> {
  const params: any = { difficulty };
  if (stageId !== undefined) {
    params.stageId = stageId;
  }
  if (elapsedTime !== undefined) {
    params.elapsedTime = elapsedTime;
  }
  const response = await axios.post<User>(`${API_BASE_URL}/${userId}/clear`, null, { params });
  return response.data;
}

export async function fetchUserHistory(userId: number): Promise<HistoryResponse[]> {
  const response = await axios.get<HistoryResponse[]>(`${API_BASE_URL}/${userId}/history`);
  return response.data;
}


export async function registerAnonymousUser(): Promise<User> {

  const response = await axios.post<User>(`${API_BASE_URL}/register`);
  return response.data;
}

export async function logVisit(uuid: string): Promise<void> {
  const ANALYTICS_BASE_URL = import.meta.env.VITE_API_BASE_URL
    ? `${import.meta.env.VITE_API_BASE_URL}/api/analytics`
    : (import.meta.env.PROD ? '/api/analytics' : 'http://localhost:8080/api/analytics');

  await axios.post(`${ANALYTICS_BASE_URL}/visit`, null, {
    params: { uuid }
  });
}

