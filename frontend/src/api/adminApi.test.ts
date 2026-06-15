import { describe, it, expect, vi, beforeEach } from 'vitest';
import axios from 'axios';
import {
  fetchAdminStages,
  createStage,
  approveStage,
  deleteStage,
  restoreStage,
  generateAiStage,
  loginAdmin,
  logoutAdmin,
  isAdminAuthenticated
} from './adminApi';

vi.mock('axios');

describe('adminApi TDD', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    localStorage.clear();
  });

  it('fetchAdminStages should call get and return admin stages list', async () => {
    const mockData = [
      { id: 1, name: 'Stage 1', width: 5, height: 5, active: true, approved: true, solutionGrid: [] }
    ];
    vi.mocked(axios.get).mockResolvedValue({ data: mockData });

    const result = await fetchAdminStages();
    expect(axios.get).toHaveBeenCalledWith('http://localhost:8080/api/admin/stages');
    expect(result).toEqual(mockData);
  });

  it('createStage should call post and return created stage details', async () => {
    const newStage = { name: 'New Stage', width: 3, height: 3, solutionGrid: [[1]] };
    const mockCreated = { id: 2, ...newStage, active: true, approved: true };
    vi.mocked(axios.post).mockResolvedValue({ data: mockCreated });

    const result = await createStage(newStage as any);
    expect(axios.post).toHaveBeenCalledWith('http://localhost:8080/api/admin/stages', newStage);
    expect(result).toEqual(mockCreated);
  });

  it('approveStage should call put to approve stage', async () => {
    vi.mocked(axios.put).mockResolvedValue({ data: {} });

    await approveStage(42);
    expect(axios.put).toHaveBeenCalledWith('http://localhost:8080/api/admin/stages/42/approve');
  });

  it('deleteStage should call delete to soft delete stage', async () => {
    vi.mocked(axios.delete).mockResolvedValue({ data: {} });

    await deleteStage(42);
    expect(axios.delete).toHaveBeenCalledWith('http://localhost:8080/api/admin/stages/42');
  });

  it('restoreStage should call put to restore soft-deleted stage', async () => {
    vi.mocked(axios.put).mockResolvedValue({ data: {} });

    await restoreStage(42);
    expect(axios.put).toHaveBeenCalledWith('http://localhost:8080/api/admin/stages/42/restore');
  });

  it('generateAiStage should call post and return newly generated AI stage details', async () => {
    const mockGenerated = { id: 99, name: 'AI Stage', width: 5, height: 5, solutionGrid: [], active: false, approved: false };
    vi.mocked(axios.post).mockResolvedValue({ data: mockGenerated });

    const result = await generateAiStage();
    expect(axios.post).toHaveBeenCalledWith('http://localhost:8080/api/admin/stages/ai-generate', null, { params: {} });
    expect(result).toEqual(mockGenerated);
  });

  it('generateAiStage with custom size parameters should call post with params', async () => {
    const mockGenerated = { id: 100, name: 'AI Stage 10x10', width: 10, height: 10, solutionGrid: [], active: false, approved: false };
    vi.mocked(axios.post).mockResolvedValue({ data: mockGenerated });

    const result = await generateAiStage(10, 10);
    expect(axios.post).toHaveBeenCalledWith(
      'http://localhost:8080/api/admin/stages/ai-generate',
      null,
      { params: { width: 10, height: 10 } }
    );
    expect(result).toEqual(mockGenerated);
  });

  it('loginAdmin should perform post, set localStorage token, and return token', async () => {
    vi.mocked(axios.post).mockResolvedValue({ data: { token: 'fake-token' } });

    const token = await loginAdmin('admin', 'password');
    expect(axios.post).toHaveBeenCalledWith('http://localhost:8080/api/admin/login', { username: 'admin', password: 'password' });
    expect(token).toBe('fake-token');
    expect(localStorage.getItem('admin_token')).toBe('fake-token');
    expect(isAdminAuthenticated()).toBe(true);
  });

  it('logoutAdmin should perform post, and clear localStorage token', async () => {
    localStorage.setItem('admin_token', 'fake-token');
    vi.mocked(axios.post).mockResolvedValue({ data: {} });

    await logoutAdmin();
    expect(axios.post).toHaveBeenCalledWith('http://localhost:8080/api/admin/logout', {});
    expect(localStorage.getItem('admin_token')).toBeNull();
    expect(isAdminAuthenticated()).toBe(false);
  });
});
