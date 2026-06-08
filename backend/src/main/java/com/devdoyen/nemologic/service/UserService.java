package com.devdoyen.nemologic.service;

import com.devdoyen.nemologic.model.User;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final Map<Long, User> users = new ConcurrentHashMap<>();

    public UserService() {
        reset();
    }

    public void reset() {
        users.clear();
        // Alice starts with 200 XP. Since Level 1 requires 100 XP, she is Level 2 (200 XP >= 100 XP).
        users.put(1L, new User(1L, "Alice", 200, 2));
        users.put(2L, new User(2L, "Bob", 500, 3));
        users.put(3L, new User(3L, "Charlie", 1000, 5));
    }

    public User addXpToUser(Long userId, int xpAmount) {
        User user = users.get(userId);
        if (user == null) {
            throw new IllegalArgumentException("User not found: " + userId);
        }
        user.addXp(xpAmount);
        return user;
    }

    public List<User> getGlobalRanking() {
        return users.values().stream()
                .sorted(Comparator.comparingInt(User::getXp).reversed())
                .collect(Collectors.toList());
    }
}
