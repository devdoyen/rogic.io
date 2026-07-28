package com.devdoyen.nemologic.controller;

import com.devdoyen.nemologic.dto.GuestClearRequest;
import com.devdoyen.nemologic.dto.HistoryResponse;
import com.devdoyen.nemologic.model.User;
import com.devdoyen.nemologic.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/ranking")
    public List<User> getRanking() {
        return userService.getGlobalRanking();
    }

    @PostMapping("/{id}/clear")
    public User clearStage(
            @PathVariable Long id,
            @RequestParam String difficulty,
            @RequestParam(required = false) Long stageId,
            @RequestParam(required = false) Integer elapsedTime,
            @org.springframework.security.core.annotation.AuthenticationPrincipal org.springframework.security.oauth2.jwt.Jwt jwt) {
        if (jwt == null) {
            throw new org.springframework.security.access.AccessDeniedException("Unauthorized");
        }
        User targetUser = userService.getUserById(id);
        String sub = jwt.getClaimAsString("sub");
        if (targetUser.getOauthId() == null || !targetUser.getOauthId().equals(sub)) {
            throw new org.springframework.security.access.AccessDeniedException("Access denied: User ID does not match token identity");
        }

        int xpReward;
        switch (difficulty.toUpperCase()) {
            case "EASY":
                xpReward = 50;
                break;
            case "NORMAL":
                xpReward = 100;
                break;
            case "HARD":
                xpReward = 200;
                break;
            default:
                throw new IllegalArgumentException("Unknown difficulty: " + difficulty);
        }
        return userService.clearStageWithHistory(id, stageId, xpReward, elapsedTime);
    }

    @PostMapping("/{id}/sync-history")
    public User syncGuestHistory(
            @PathVariable Long id,
            @RequestBody List<GuestClearRequest> guestClears,
            @org.springframework.security.core.annotation.AuthenticationPrincipal org.springframework.security.oauth2.jwt.Jwt jwt) {
        if (jwt == null) {
            throw new org.springframework.security.access.AccessDeniedException("Unauthorized");
        }
        User targetUser = userService.getUserById(id);
        String sub = jwt.getClaimAsString("sub");
        if (targetUser.getOauthId() == null || !targetUser.getOauthId().equals(sub)) {
            throw new org.springframework.security.access.AccessDeniedException("Access denied: User ID does not match token identity");
        }
        return userService.syncGuestHistory(id, guestClears);
    }

    @GetMapping("/{id}/history")
    public org.springframework.http.ResponseEntity<?> getUserHistory(
            @PathVariable Long id,
            @org.springframework.web.bind.annotation.RequestParam(required = false) Integer page,
            @org.springframework.web.bind.annotation.RequestParam(required = false) Integer size,
            @org.springframework.security.core.annotation.AuthenticationPrincipal org.springframework.security.oauth2.jwt.Jwt jwt) {
        if (jwt == null) {
            throw new org.springframework.security.access.AccessDeniedException("Unauthorized");
        }
        User targetUser = userService.getUserById(id);
        String sub = jwt.getClaimAsString("sub");
        if (targetUser.getOauthId() == null || !targetUser.getOauthId().equals(sub)) {
            throw new org.springframework.security.access.AccessDeniedException("Access denied: User ID does not match token identity");
        }

        if (page == null && size == null) {
            List<HistoryResponse> history = userService.getUserHistory(id);
            if (history.size() > 100) {
                history = history.subList(0, 100);
            }
            return org.springframework.http.ResponseEntity.ok(history);
        }

        int pageVal = (page != null) ? Math.max(0, page) : 0;
        int sizeVal = (size != null) ? Math.min(100, Math.max(1, size)) : 20;

        return org.springframework.http.ResponseEntity.ok(userService.getUserHistoryPaged(id, pageVal, sizeVal));
    }

    @GetMapping("/{id}/cleared-stages")
    public org.springframework.http.ResponseEntity<List<Long>> getClearedStageIds(
            @PathVariable Long id,
            @org.springframework.security.core.annotation.AuthenticationPrincipal org.springframework.security.oauth2.jwt.Jwt jwt) {
        if (jwt == null) {
            throw new org.springframework.security.access.AccessDeniedException("Unauthorized");
        }
        User targetUser = userService.getUserById(id);
        String sub = jwt.getClaimAsString("sub");
        if (targetUser.getOauthId() == null || !targetUser.getOauthId().equals(sub)) {
            throw new org.springframework.security.access.AccessDeniedException("Access denied: User ID does not match token identity");
        }
        return org.springframework.http.ResponseEntity.ok(userService.getClearedStageIds(id));
    }
}
