package com.devdoyen.nemologic.client;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("test")
public class MockAiClient implements AiClient {

    @Override
    public String generateDailyPuzzleJson() {
        return "{\"name\": \"AI Puzzle: Daily Apple\", \"width\": 5, \"height\": 5, \"grid\": \"[[0,1,0,1,0],[1,1,1,1,1],[1,1,1,1,1],[0,1,1,1,0],[0,0,1,0,0]]\"}";
    }

    @Override
    public String generatePuzzleJson(int width, int height) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int r = 0; r < height; r++) {
            sb.append("[");
            for (int c = 0; c < width; c++) {
                sb.append("1");
                if (c < width - 1) sb.append(",");
            }
            sb.append("]");
            if (r < height - 1) sb.append(",");
        }
        sb.append("]");
        String gridStr = sb.toString();
        return String.format("{\"name\": \"AI Puzzle: Custom Shape\", \"width\": %d, \"height\": %d, \"grid\": \"%s\"}", width, height, gridStr);
    }

    @Override
    public String generatePuzzleJson(int width, int height, java.util.List<String> recentThemes) {
        return generatePuzzleJson(width, height);
    }
}
