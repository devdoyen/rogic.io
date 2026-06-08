package com.devdoyen.nemologic.service;

import com.devdoyen.nemologic.dto.HistoryResponse;
import com.devdoyen.nemologic.model.History;
import com.devdoyen.nemologic.model.Stage;
import com.devdoyen.nemologic.model.User;
import com.devdoyen.nemologic.repository.HistoryRepository;
import com.devdoyen.nemologic.repository.StageRepository;
import com.devdoyen.nemologic.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final HistoryRepository historyRepository;
    private final StageRepository stageRepository;

    @jakarta.persistence.PersistenceContext
    private jakarta.persistence.EntityManager entityManager;

    public UserService(UserRepository userRepository, HistoryRepository historyRepository, StageRepository stageRepository) {
        this.userRepository = userRepository;
        this.historyRepository = historyRepository;
        this.stageRepository = stageRepository;
    }

    @Transactional
    public void reset() {
        historyRepository.deleteAll();
        userRepository.deleteAll();
        if (entityManager != null) {
            entityManager.createNativeQuery("ALTER TABLE users ALTER COLUMN id RESTART WITH 1").executeUpdate();
            entityManager.createNativeQuery("ALTER TABLE histories ALTER COLUMN id RESTART WITH 1").executeUpdate();
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

    @Transactional
    public User clearStageWithHistory(Long userId, Long stageId, int xpAmount, Integer elapsedTime) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        user.addXp(xpAmount);
        User savedUser = userRepository.save(user);

        if (stageId != null) {
            Stage stage = stageRepository.findById(stageId)
                    .orElseThrow(() -> new IllegalArgumentException("Stage not found: " + stageId));
            int time = (elapsedTime != null) ? elapsedTime : 0;
            History history = new History(savedUser, stage, java.time.LocalDateTime.now(), xpAmount, time);
            historyRepository.save(history);
        }

        return savedUser;
    }

    @Transactional(readOnly = true)
    public List<HistoryResponse> getUserHistory(Long userId) {
        return historyRepository.findByUserId(userId).stream()
                .map(h -> new HistoryResponse(
                        h.getId(),
                        h.getUser().getId(),
                        h.getStage().getId(),
                        h.getStage().getName(),
                        h.getClearedAt(),
                        h.getXpEarned(),
                        h.getElapsedTime()
                ))
                .collect(Collectors.toList());
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
