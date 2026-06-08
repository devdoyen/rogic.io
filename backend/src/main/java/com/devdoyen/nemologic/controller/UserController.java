package com.devdoyen.nemologic.controller;

import com.devdoyen.nemologic.model.User;
import com.devdoyen.nemologic.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "http://localhost:5173")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/ranking")
    public List<User> getRanking() {
        return userService.getGlobalRanking();
    }

    @PostMapping("/{id}/clear")
    public User clearStage(@PathVariable Long id, @RequestParam String difficulty) {
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
        return userService.addXpToUser(id, xpReward);
    }

    @PostMapping("/register")
    public User registerUser() {
        return userService.registerAnonymousUser();
    }
}
