package com.devdoyen.nemologic.model;

import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String username;

    private int xp;
    private int level;
    private String uuid;

    public User() {
    }

    public User(Long id, String username, int xp, int level) {
        this.id = id;
        this.username = username;
        this.xp = xp;
        this.level = level;
        this.uuid = null;
    }

    public User(Long id, String username, int xp, int level, String uuid) {
        this.id = id;
        this.username = username;
        this.xp = xp;
        this.level = level;
        this.uuid = uuid;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getXp() {
        return xp;
    }

    public void setXp(int xp) {
        this.xp = xp;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public void addXp(int amount) {
        if (amount <= 0) return;
        this.xp += amount;
        updateLevel();
    }

    private void updateLevel() {
        int currentLevel = 1;
        int accumulatedXpNeeded = 0;
        while (true) {
            int nextLevelThreshold = currentLevel * 100;
            if (this.xp >= accumulatedXpNeeded + nextLevelThreshold) {
                accumulatedXpNeeded += nextLevelThreshold;
                currentLevel++;
            } else {
                break;
            }
        }
        this.level = currentLevel;
    }
}
