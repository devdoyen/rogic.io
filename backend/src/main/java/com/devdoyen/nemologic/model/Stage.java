package com.devdoyen.nemologic.model;

public record Stage(
    Long id,
    String name,
    int width,
    int height,
    int[][] solutionGrid
) {}
