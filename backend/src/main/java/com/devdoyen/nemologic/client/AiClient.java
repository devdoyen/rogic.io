package com.devdoyen.nemologic.client;

public interface AiClient {
    String generateDailyPuzzleJson();
    String generatePuzzleJson(int width, int height);
    String generatePuzzleJson(int width, int height, java.util.List<String> recentThemes);
}
