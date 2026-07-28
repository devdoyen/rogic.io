package com.devdoyen.nemologic.service;

import com.devdoyen.nemologic.dto.GuestClearRequest;
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
    private final StageService stageService;
    private final com.devdoyen.nemologic.security.SolveProofTokenService solveProofTokenService;

    @jakarta.persistence.PersistenceContext
    private jakarta.persistence.EntityManager entityManager;

    public UserService(
            UserRepository userRepository,
            HistoryRepository historyRepository,
            StageRepository stageRepository,
            StageService stageService,
            com.devdoyen.nemologic.security.SolveProofTokenService solveProofTokenService) {
        this.userRepository = userRepository;
        this.historyRepository = historyRepository;
        this.stageRepository = stageRepository;
        this.stageService = stageService;
        this.solveProofTokenService = solveProofTokenService;
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

    @Transactional(readOnly = true)
    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
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
            stageService.recordClear(stageId, time);
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
    public List<Long> getClearedStageIds(Long userId) {
        return historyRepository.findClearedStageIdsByUserId(userId);
    }

    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<HistoryResponse> getUserHistoryPaged(Long userId, int page, int size) {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size, org.springframework.data.domain.Sort.by("clearedAt").descending());
        return historyRepository.findByUserId(userId, pageable)
                .map(h -> new HistoryResponse(
                        h.getId(),
                        h.getUser().getId(),
                        h.getStage().getId(),
                        h.getStage().getName(),
                        h.getClearedAt(),
                        h.getXpEarned(),
                        h.getElapsedTime()
                ));
    }

    @Transactional(readOnly = true)
    public List<User> getGlobalRanking() {
        return userRepository.findAll().stream()
                .sorted(Comparator.comparingInt(User::getXp).reversed())
                .collect(Collectors.toList());
    }

    @Transactional
    public User findOrCreateByOauthId(String oauthId, String name, String email, String pictureUrl) {
        return userRepository.findByOauthId(oauthId)
                .map(user -> {
                    boolean updated = false;
                    if (name != null && !name.equals(user.getUsername())) {
                        user.setUsername(name);
                        updated = true;
                    }
                    if (email != null && !email.equals(user.getEmail())) {
                        user.setEmail(email);
                        updated = true;
                    }
                    if (pictureUrl != null && !pictureUrl.equals(user.getProfileImageUrl())) {
                        user.setProfileImageUrl(pictureUrl);
                        updated = true;
                    }
                    return updated ? userRepository.save(user) : user;
                })
                .orElseGet(() -> {
                    String username = (name != null && !name.trim().isEmpty()) ? name : "User-" + UUID.randomUUID().toString().substring(0, 8);
                    User newUser = new User(null, username, 0, 1, oauthId, email, pictureUrl);
                    return userRepository.save(newUser);
                });
    }

    @Transactional
    public User syncGuestHistory(Long userId, List<GuestClearRequest> guestClears) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        Set<Long> clearedStageIds = historyRepository.findByUserId(userId).stream()
                .map(h -> h.getStage().getId())
                .collect(Collectors.toSet());

        int totalXpReward = 0;

        for (GuestClearRequest clearReq : guestClears) {
            Long stageId = clearReq.getStageId();
            if (stageId == null || clearedStageIds.contains(stageId)) {
                continue;
            }

            if (!solveProofTokenService.verifyProofToken(clearReq.getProofToken(), stageId, clearReq.getElapsedTime())) {
                continue;
            }

            Stage stage = stageRepository.findById(stageId).orElse(null);
            if (stage == null) {
                continue;
            }

            int xpReward;
            int width = stage.getWidth();
            int height = stage.getHeight();
            if (width <= 5 && height <= 5) {
                xpReward = 50;
            } else if (width >= 10 || height >= 10) {
                xpReward = 200;
            } else {
                xpReward = 100;
            }

            int time = Math.max(0, clearReq.getElapsedTime());
            History history = new History(user, stage, java.time.LocalDateTime.now(), xpReward, time);
            historyRepository.save(history);
            stageService.recordClear(stageId, time);

            totalXpReward += xpReward;
            clearedStageIds.add(stageId);
        }

        if (totalXpReward > 0) {
            user.addXp(totalXpReward);
            return userRepository.save(user);
        }

        return user;
    }
}
