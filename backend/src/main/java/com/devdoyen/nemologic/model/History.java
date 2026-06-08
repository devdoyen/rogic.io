package com.devdoyen.nemologic.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "histories")
public class History {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stage_id", nullable = false)
    private Stage stage;

    @Column(name = "cleared_at", nullable = false)
    private LocalDateTime clearedAt;

    @Column(name = "xp_earned", nullable = false)
    private int xpEarned;

    @Column(name = "elapsed_time", nullable = false)
    private int elapsedTime;

    public History() {
    }

    public History(User user, Stage stage, LocalDateTime clearedAt, int xpEarned, int elapsedTime) {
        this.user = user;
        this.stage = stage;
        this.clearedAt = clearedAt;
        this.xpEarned = xpEarned;
        this.elapsedTime = elapsedTime;
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Stage getStage() {
        return stage;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public LocalDateTime getClearedAt() {
        return clearedAt;
    }

    public void setClearedAt(LocalDateTime clearedAt) {
        this.clearedAt = clearedAt;
    }

    public int getXpEarned() {
        return xpEarned;
    }

    public void setXpEarned(int xpEarned) {
        this.xpEarned = xpEarned;
    }

    public int getElapsedTime() {
        return elapsedTime;
    }

    public void setElapsedTime(int elapsedTime) {
        this.elapsedTime = elapsedTime;
    }
}

