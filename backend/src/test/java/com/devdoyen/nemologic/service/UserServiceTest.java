package com.devdoyen.nemologic.service;

import com.devdoyen.nemologic.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class UserServiceTest {

    private UserService userService;

    @BeforeEach
    public void setUp() {
        userService = new UserService();
    }

    @Test
    public void testUserServiceAddXp() {
        User user = userService.addXpToUser(1L, 120);
        assertEquals(3, user.getLevel());
        assertEquals(320, user.getXp()); // Alice initial 200 + 120 = 320
    }

    @Test
    public void testInitialUserStats() {
        User user = new User(1L, "Alice", 0, 1);
        assertEquals(0, user.getXp());
        assertEquals(1, user.getLevel());
    }

    @Test
    public void testAddXpNoLevelUp() {
        User user = new User(1L, "Alice", 0, 1);
        user.addXp(50);
        assertEquals(50, user.getXp());
        assertEquals(1, user.getLevel());
    }

    @Test
    public void testAddXpSingleLevelUp() {
        User user = new User(1L, "Alice", 0, 1);
        // Level 1 -> needs 100 XP to reach Level 2
        user.addXp(120);
        assertEquals(2, user.getLevel());
        assertEquals(120, user.getXp());
    }

    @Test
    public void testAddXpMultipleLevelUps() {
        User user = new User(1L, "Alice", 0, 1);
        // Level 1 -> needs 100 XP (reaches Level 2)
        // Level 2 -> needs 200 XP (reaches Level 3)
        // Total 300 XP needed to reach Level 3.
        // We add 350 XP. Should be Level 3 with 350 XP.
        user.addXp(350);
        assertEquals(3, user.getLevel());
        assertEquals(350, user.getXp());
    }
}
