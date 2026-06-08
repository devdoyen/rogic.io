package com.devdoyen.nemologic.service;

import com.devdoyen.nemologic.model.User;
import com.devdoyen.nemologic.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;

    @jakarta.persistence.PersistenceContext
    private jakarta.persistence.EntityManager entityManager;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public void reset() {
        userRepository.deleteAll();
        if (entityManager != null) {
            entityManager.createNativeQuery("ALTER TABLE users ALTER COLUMN id RESTART WITH 1").executeUpdate();
        }
        userRepository.save(new User(null, "Player1", 200, 2));
        userRepository.save(new User(null, "Player2", 500, 3));
        userRepository.save(new User(null, "Player3", 1000, 5));
    }

    @Transactional
    public User addXpToUser(Long userId, int xpAmount) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        user.addXp(xpAmount);
        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public List<User> getGlobalRanking() {
        return userRepository.findAll().stream()
                .sorted(Comparator.comparingInt(User::getXp).reversed())
                .collect(Collectors.toList());
    }

    @Transactional
    public User registerAnonymousUser() {
        String uuid = UUID.randomUUID().toString();
        String username = "Anonymous-" + uuid.substring(0, 8);
        User newUser = new User(null, username, 0, 1, uuid);
        return userRepository.save(newUser);
    }
}
