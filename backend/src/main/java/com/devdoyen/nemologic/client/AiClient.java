package com.devdoyen.nemologic.client;

public interface AiClient {
    String generateDailyPuzzleJson();
    String generatePuzzleJson(int width, int height);
}
