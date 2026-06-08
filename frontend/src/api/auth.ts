export interface UserSession {
  id: number;
  uuid: string;
  username: string;
  xp: number;
  level: number;
}

export const SESSION_KEY = 'nemologic_user_session';

export function getUserSession(): UserSession | null {
  // Skeleton implementation: return null to trigger test failure in Red Phase
  return null;
}

export function setUserSession(_session: UserSession): void {
  // Skeleton implementation: do nothing to trigger test failure in Red Phase
}

export function clearUserSession(): void {
  // Skeleton implementation: do nothing to trigger test failure in Red Phase
}

export function hasUserSession(): boolean {
  // Skeleton implementation: return false to trigger test failure in Red Phase
  return false;
}
