import { describe, it, expect, beforeEach } from 'vitest';
import {
  getUserSession,
  setUserSession,
  clearUserSession,
  hasUserSession,
  SESSION_KEY,
  type UserSession
} from './auth';

describe('auth session utility unit tests (Red Phase)', () => {
  beforeEach(() => {
    localStorage.clear();
  });

  it('should return null when there is no user session', () => {
    expect(getUserSession()).toBeNull();
    expect(hasUserSession()).toBe(false);
  });

  it('should set and get user session successfully', () => {
    const mockSession: UserSession = {
      id: 42,
      uuid: 'abc-123-uuid',
      username: 'AnonymousHero',
      xp: 0,
      level: 1
    };

    setUserSession(mockSession);

    expect(hasUserSession()).toBe(true);
    expect(getUserSession()).toEqual(mockSession);
    expect(localStorage.getItem(SESSION_KEY)).not.toBeNull();
  });

  it('should clear user session successfully', () => {
    const mockSession: UserSession = {
      id: 42,
      uuid: 'abc-123-uuid',
      username: 'AnonymousHero',
      xp: 0,
      level: 1
    };

    setUserSession(mockSession);
    clearUserSession();

    expect(hasUserSession()).toBe(false);
    expect(getUserSession()).toBeNull();
    expect(localStorage.getItem(SESSION_KEY)).toBeNull();
  });
});
