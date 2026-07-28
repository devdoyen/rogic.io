import apiClient from './apiClient';

export interface User {
  id: number;
  username: string;
  xp: number;
  level: number;
  uuid?: string;
  email?: string;
  profileImageUrl?: string;
}

export interface HistoryResponse {
  id: number;
  userId: number;
  stageId: number;
  stageName: string;
  clearedAt: string;
  xpEarned: number;
  elapsedTime: number;
  proofToken?: string;
}

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL
  ? `${import.meta.env.VITE_API_BASE_URL}/api/users`
  : (import.meta.env.PROD ? '/api/users' : 'http://localhost:8080/api/users');

export async function fetchRanking(): Promise<User[]> {
  const response = await apiClient.get<User[]>(`${API_BASE_URL}/ranking`);
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
  const response = await apiClient.post<User>(`${API_BASE_URL}/${userId}/clear`, null, {
    params,
  });
  return response.data;
}

import type { PageResponse } from './stageApi';

export async function fetchUserHistory(userId: number, page?: number, size?: number): Promise<HistoryResponse[] | PageResponse<HistoryResponse>> {
  const params: any = { _t: Date.now() };
  if (page !== undefined) params.page = page;
  if (size !== undefined) params.size = size;
  
  const config: any = {
    params
  };
  
  const response = await apiClient.get<HistoryResponse[] | PageResponse<HistoryResponse>>(`${API_BASE_URL}/${userId}/history`, config);
  return response.data;
}

export async function fetchClearedStageIds(userId: number): Promise<number[]> {
  const response = await apiClient.get<number[]>(`${API_BASE_URL}/${userId}/cleared-stages`);
  return response.data;
}

export async function fetchMeFromServer(): Promise<User> {
  const authBaseUrl = import.meta.env.VITE_API_BASE_URL
    ? `${import.meta.env.VITE_API_BASE_URL}/api/auth`
    : (import.meta.env.PROD ? '/api/auth' : 'http://localhost:8080/api/auth');
  const response = await apiClient.post<User>(`${authBaseUrl}/me`, null);
  return response.data;
}

export async function syncGuestHistory(
  userId: number,
  guestClears: { stageId: number; elapsedTime: number; proofToken?: string }[]
): Promise<User> {
  const response = await apiClient.post<User>(`${API_BASE_URL}/${userId}/sync-history`, guestClears);
  return response.data;
}


