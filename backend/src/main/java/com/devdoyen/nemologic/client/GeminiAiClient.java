package com.devdoyen.nemologic.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Component
@Profile("!test")
public class GeminiAiClient implements AiClient {

    @Value("${ai.api.key:}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String generateDailyPuzzleJson() {
        return generatePuzzleJson(5, 5);
    }

    @Override
    public String generatePuzzleJson(int width, int height) {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new IllegalStateException("[AI] API Key is missing. Cannot generate AI puzzle.");
        }

        String url = "https://generativelanguage.googleapis.com/v1/models/gemini-2.5-flash:generateContent?key=" + apiKey;
        int maxAttempts = 3;
        Exception lastException = null;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);

                Map<String, Object> requestBody = new HashMap<>();
                Map<String, Object> contents = new HashMap<>();
                Map<String, Object> parts = new HashMap<>();
                
                String prompt = String.format(
                    "Generate a valid, creative, and unique nonogram puzzle of size %dx%d in JSON format. " +
                    "Do NOT generate a simple heart shape. Create a different recognizable shape (like a tree, a letter, a face, a cup, an arrow, etc.). " +
                    "The response must follow this exact JSON schema: { \"name\": \"ObjectName\", \"width\": %d, \"height\": %d, \"grid\": \"...\" }. " +
                    "Do NOT prefix the name with 'AI Puzzle:' or 'Daily Puzzle:'. Just output the pure name of the object. " +
                    "Return only raw JSON string inside, no markdown formatting (do NOT wrap in ```json). " +
                    "Grid string must be a valid serialized JSON array representing %dx%d cells containing only 0 and 1.",
                    width, height, width, height, width, height
                );

                parts.put("text", prompt);
                contents.put("parts", Collections.singletonList(parts));
                requestBody.put("contents", Collections.singletonList(contents));

                HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
                String response = restTemplate.postForObject(url, entity, String.class);

                JsonNode root = objectMapper.readTree(response);
                String rawText = root.path("candidates")
                        .get(0)
                        .path("content")
                        .path("parts")
                        .get(0)
                        .path("text")
                        .asText();

                rawText = rawText.trim();
                if (rawText.startsWith("```json")) {
                    rawText = rawText.substring(7);
                }
                if (rawText.startsWith("```")) {
                    rawText = rawText.substring(3);
                }
                if (rawText.endsWith("```")) {
                    rawText = rawText.substring(0, rawText.length() - 3);
                }
                return rawText.trim();

            } catch (Exception e) {
                lastException = e;
                System.err.println("[AI] API call attempt " + attempt + " failed: " + e.getMessage());
                if (attempt < maxAttempts) {
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }

        throw new RuntimeException("[AI] All " + maxAttempts + " attempts to query Gemini API failed", lastException);
    }
}
