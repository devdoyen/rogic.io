import axios from 'axios';
import type { StageSummary, StageDetails } from './stageApi';

export interface AdminStageInfo extends StageSummary {
  active: boolean;
  approved: boolean;
  solutionGrid: number[][];
}

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL
  ? `${import.meta.env.VITE_API_BASE_URL}/api/admin/stages`
  : (import.meta.env.PROD ? '/api/admin/stages' : 'http://localhost:8080/api/admin/stages');

// Request interceptor to automatically add Authorization header for admin endpoints
axios.interceptors.request.use((config) => {
  const token = localStorage.getItem('admin_token');
  if (token && config.url && config.url.includes('/api/admin')) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
}, (error) => {
  return Promise.reject(error);
});

export async function loginAdmin(username: string, password: string): Promise<string> {
  const baseUrl = import.meta.env.VITE_API_BASE_URL
    ? `${import.meta.env.VITE_API_BASE_URL}/api/admin`
    : (import.meta.env.PROD ? '/api/admin' : 'http://localhost:8080/api/admin');
  const response = await axios.post<{ token: string }>(`${baseUrl}/login`, { username, password });
  const token = response.data.token;
  localStorage.setItem('admin_token', token);
  return token;
}

export async function logoutAdmin(): Promise<void> {
  const baseUrl = import.meta.env.VITE_API_BASE_URL
    ? `${import.meta.env.VITE_API_BASE_URL}/api/admin`
    : (import.meta.env.PROD ? '/api/admin' : 'http://localhost:8080/api/admin');
  const token = localStorage.getItem('admin_token');
  if (token) {
    try {
      await axios.post(`${baseUrl}/logout`, {});
    } catch (error) {
      console.error('Logout request failed:', error);
    } finally {
      localStorage.removeItem('admin_token');
    }
  }
}

export function isAdminAuthenticated(): boolean {
  return !!localStorage.getItem('admin_token');
}

export async function fetchAdminStages(): Promise<AdminStageInfo[]> {
  const response = await axios.get<AdminStageInfo[]>(API_BASE_URL);
  return response.data;
}

export async function createStage(stage: Omit<StageDetails, 'id'>): Promise<StageDetails> {
  const response = await axios.post<StageDetails>(API_BASE_URL, stage);
  return response.data;
}

export async function approveStage(id: number): Promise<void> {
  await axios.put(`${API_BASE_URL}/${id}/approve`);
}

export async function deleteStage(id: number): Promise<void> {
  await axios.delete(`${API_BASE_URL}/${id}`);
}

export async function restoreStage(id: number): Promise<void> {
  await axios.put(`${API_BASE_URL}/${id}/restore`);
}

export async function generateAiStage(width?: number, height?: number): Promise<StageDetails> {
  const params: Record<string, any> = {};
  if (width !== undefined) params.width = width;
  if (height !== undefined) params.height = height;
  const response = await axios.post<StageDetails>(`${API_BASE_URL}/ai-generate`, null, { params });
  return response.data;
}
