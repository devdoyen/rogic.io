package com.devdoyen.nemologic.service;

import com.devdoyen.nemologic.model.User;
import com.devdoyen.nemologic.repository.UserRepository;
import com.devdoyen.nemologic.repository.HistoryRepository;
import com.devdoyen.nemologic.repository.StageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class UserServiceTest {

    private UserService userService;
    private UserRepository userRepository;
    private HistoryRepository historyRepository;
    private StageRepository stageRepository;

    @BeforeEach
    public void setUp() {
        userRepository = mock(UserRepository.class);
        historyRepository = mock(HistoryRepository.class);
        stageRepository = mock(StageRepository.class);
        userService = new UserService(userRepository, historyRepository, stageRepository);
    }


    @Test
    public void testUserServiceAddXp() {
        User user = new User(1L, "Player1", 200, 2);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User updatedUser = userService.addXpToUser(1L, 120);
        assertEquals(3, updatedUser.getLevel());
        assertEquals(320, updatedUser.getXp());
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
        user.addXp(120);
        assertEquals(2, user.getLevel());
        assertEquals(120, user.getXp());
    }

    @Test
    public void testAddXpMultipleLevelUps() {
        User user = new User(1L, "Alice", 0, 1);
        user.addXp(350);
        assertEquals(3, user.getLevel());
        assertEquals(350, user.getXp());
    }

    @Test
    public void testRegisterAnonymousUserAutoIncrement() {
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(4L);
            return u;
        });

        User user = userService.registerAnonymousUser();
        assertNotNull(user);
        assertEquals(4L, user.getId());
        assertNotNull(user.getUuid());
        assertFalse(user.getUuid().isEmpty());
        assertTrue(user.getUsername().startsWith("Anonymous-"));
        assertEquals(0, user.getXp());
        assertEquals(1, user.getLevel());
    }
}
