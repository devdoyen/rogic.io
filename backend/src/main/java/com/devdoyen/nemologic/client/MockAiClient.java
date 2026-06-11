package com.devdoyen.nemologic.client;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("test")
public class MockAiClient implements AiClient {

    @Override
    public String generateDailyPuzzleJson() {
        return "{\"name\": \"AI Puzzle\", \"width\": 5, \"height\": 5, \"grid\": \"[[0,1,0,1,0],[1,1,1,1,1],[1,1,1,1,1],[0,1,1,1,0],[0,0,1,0,0]]\"}";
    }
}
