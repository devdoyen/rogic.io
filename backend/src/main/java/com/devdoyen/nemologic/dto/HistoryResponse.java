package com.devdoyen.nemologic.dto;

import java.time.LocalDateTime;

public class HistoryResponse {
    private Long id;
    private Long userId;
    private Long stageId;
    private String stageName;
    private LocalDateTime clearedAt;
    private int xpEarned;
    private int elapsedTime;

    public HistoryResponse() {
    }

    public HistoryResponse(Long id, Long userId, Long stageId, String stageName, LocalDateTime clearedAt, int xpEarned, int elapsedTime) {
        this.id = id;
        this.userId = userId;
        this.stageId = stageId;
        this.stageName = stageName;
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

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getStageId() {
        return stageId;
    }

    public void setStageId(Long stageId) {
        this.stageId = stageId;
    }

    public String getStageName() {
        return stageName;
    }

    public void setStageName(String stageName) {
        this.stageName = stageName;
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

