import { test, expect } from '@playwright/test';

test.describe('Staging Environment E2E Smoke Integration Test', () => {
  test('should load home page, register user, render stage, and check profile info', async ({ page }) => {
    // 1. Navigate to Staging homepage
    console.log('Navigating to homepage...');
    await page.goto('/');

    // Verify main page elements are rendered
    await expect(page.locator('.hero-title')).toHaveText('rogic.io');
    await expect(page.locator('.cta-play-btn')).toBeVisible();

    // 2. Click Play Now to navigate to the Game Play tab
    console.log('Navigating to Game Play tab...');
    await page.click('.cta-play-btn');

    // Wait for the loading screen to disappear
    await expect(page.locator('.loading-state')).not.toBeVisible({ timeout: 15000 });

    // Verify canvas board is rendered
    console.log('Verifying Nonogram Canvas board rendering...');
    const canvas = page.locator('canvas');
    await expect(canvas).toBeVisible({ timeout: 10000 });

    // Verify floating stage selector exists and displays the current stage name
    const activeStageBadge = page.locator('.active-stage-badge');
    await expect(activeStageBadge).toBeVisible();
    const stageName = await page.locator('.active-stage-badge-name').textContent();
    console.log(`Active Stage Name: ${stageName}`);
    expect(stageName?.length).toBeGreaterThan(0);

    // 3. Navigate to My Page tab
    console.log('Navigating to My Page tab...');
    await page.click('.tab-btn-mypage');

    // Verify user profile details (Anonymous registration validation)
    console.log('Verifying user profile and registration details...');
    const profileUsername = page.locator('.profile-username');
    await expect(profileUsername).toBeVisible({ timeout: 5000 });
    
    const usernameText = await profileUsername.textContent();
    console.log(`Registered Username: ${usernameText}`);
    expect(usernameText).not.toBeNull();
    expect(usernameText).not.toBe('Anonymous User');
    
    // Level and XP validation
    const profileLv = page.locator('.profile-lv');
    const profileXp = page.locator('.profile-xp');
    await expect(profileLv).toBeVisible();
    await expect(profileXp).toBeVisible();
    
    const levelText = await profileLv.textContent();
    const xpText = await profileXp.textContent();
    console.log(`Profile Info - Level: ${levelText}, XP: ${xpText}`);
    
    expect(levelText).toContain('Level');
    expect(xpText).toContain('XP');

    console.log('Staging E2E Smoke Integration Test completed successfully!');
  });
});
