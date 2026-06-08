import axios from 'axios';

export interface User {
  id: number;
  username: string;
  xp: number;
  level: number;
  uuid?: string;
}

const API_BASE_URL = 'http://localhost:8080/api/users';

export async function fetchRanking(): Promise<User[]> {
  const response = await axios.get<User[]>(`${API_BASE_URL}/ranking`);
  return response.data;
}

export async function clearStage(userId: number, difficulty: string): Promise<User> {
  const response = await axios.post<User>(`${API_BASE_URL}/${userId}/clear`, null, {
    params: { difficulty }
  });
  return response.data;
}

export async function registerAnonymousUser(): Promise<User> {
  const response = await axios.post<User>(`${API_BASE_URL}/register`);
  return response.data;
}

