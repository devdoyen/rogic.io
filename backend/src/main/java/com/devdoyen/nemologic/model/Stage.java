package com.devdoyen.nemologic.model;

import jakarta.persistence.*;

@Entity
@Table(name = "stages")
public class Stage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private int width;
    private int height;

    @Convert(converter = GridConverter.class)
    @Column(columnDefinition = "TEXT")
    private int[][] solutionGrid;

    @Column(name = "total_attempts", nullable = false, columnDefinition = "integer default 0")
    private int totalAttempts = 0;

    @Column(name = "total_clears", nullable = false, columnDefinition = "integer default 0")
    private int totalClears = 0;

    @Column(name = "average_elapsed_time", nullable = false, columnDefinition = "double precision default 0.0")
    private double averageElapsedTime = 0.0;

    @Column(nullable = false, columnDefinition = "boolean default true")
    private boolean active = true;

    public Stage() {
    }

    public Stage(Long id, String name, int width, int height, int[][] solutionGrid) {
        this.id = id;
        this.name = name;
        this.width = width;
        this.height = height;
        this.solutionGrid = solutionGrid;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int[][] getSolutionGrid() {
        return solutionGrid;
    }

    public void setSolutionGrid(int[][] solutionGrid) {
        this.solutionGrid = solutionGrid;
    }

    // Backward compatibility with previous Record methods
    public Long id() {
        return id;
    }

    public String name() {
        return name;
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }

    public int[][] solutionGrid() {
        return solutionGrid;
    }

    public int getTotalAttempts() {
        return totalAttempts;
    }

    public void setTotalAttempts(int totalAttempts) {
        this.totalAttempts = totalAttempts;
    }

    public int getTotalClears() {
        return totalClears;
    }

    public void setTotalClears(int totalClears) {
        this.totalClears = totalClears;
    }

    public double getAverageElapsedTime() {
        return averageElapsedTime;
    }

    public void setAverageElapsedTime(double averageElapsedTime) {
        this.averageElapsedTime = averageElapsedTime;
    }

    public int totalAttempts() {
        return totalAttempts;
    }

    public int totalClears() {
        return totalClears;
    }

    public double averageElapsedTime() {
        return averageElapsedTime;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
