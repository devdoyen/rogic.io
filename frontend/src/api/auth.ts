export interface UserSession {
  id: number;
  uuid: string;
  username: string;
  xp: number;
  level: number;
}

export const SESSION_KEY = 'nemologic_user_session';

export function getUserSession(): UserSession | null {
  const data = localStorage.getItem(SESSION_KEY);
  if (!data) {
    return null;
  }
  try {
    return JSON.parse(data) as UserSession;
  } catch (error) {
    console.error('Failed to parse user session:', error);
    return null;
  }
}

export function setUserSession(session: UserSession): void {
  try {
    localStorage.setItem(SESSION_KEY, JSON.stringify(session));
  } catch (error) {
    console.error('Failed to set user session:', error);
  }
}

export function clearUserSession(): void {
  localStorage.removeItem(SESSION_KEY);
}

export function hasUserSession(): boolean {
  return localStorage.getItem(SESSION_KEY) !== null;
}
