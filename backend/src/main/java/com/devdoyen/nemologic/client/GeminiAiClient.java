package com.devdoyen.nemologic.client;

import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@Component
@Profile("!test")
public class GeminiAiClient implements AiClient {

    @Value("${ai.api.key:}")
    private String apiKey;

    @Value("${ai.model.theme:gemini-3.5-flash}")
    private String themeModelName;

    @Value("${ai.model.grid:gemini-3.1-flash-lite}")
    private String gridModelName;

    @Value("classpath:prompts/theme-generation.txt")
    private org.springframework.core.io.Resource themePromptResource;

    @Value("classpath:prompts/grid-generation.txt")
    private org.springframework.core.io.Resource gridPromptResource;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String generateDailyPuzzleJson() {
        return generatePuzzleJson(5, 5);
    }

    @Override
    public String generatePuzzleJson(int width, int height) {
        return generatePuzzleJson(width, height, java.util.Collections.emptyList());
    }

    @Override
    public String generatePuzzleJson(int width, int height, java.util.List<String> recentThemes) {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new IllegalStateException("[AI] API Key is missing. Cannot generate AI puzzle.");
        }

        String excludePrompt = "";
        if (recentThemes != null && !recentThemes.isEmpty()) {
            excludePrompt = String.format("Do NOT generate puzzles with similar themes or names to the following: %s. ", String.join(", ", recentThemes));
        }

        int candidateCount = (width >= 25 || height >= 25) ? 2 : 5;
        String prompt = String.format(
            "Generate a JSON array of exactly %d different, creative, and recognizable pixel art grid designs of size %dx%d in JSON format. " +
            "Do NOT generate a simple heart shape. Create recognizable shapes. " +
            "%s" +
            "The response must follow this exact JSON schema (a JSON array of candidate objects): " +
            "[ { \"name\": \"ObjectName\", \"width\": %d, \"height\": %d, \"grid\": [[...], [...]] }, ... ]. " +
            "Do NOT prefix names with 'AI Puzzle:' or 'Daily Puzzle:'. Just output the pure name of the object. " +
            "Return only raw JSON string inside, no markdown formatting. " +
            "For each candidate, the 'grid' field MUST be a literal 2D JSON array representing %dx%d cells containing only 0 and 1. " +
            "Do NOT use any shorthand code, loops, functions, or placeholder syntax to define the grid. Every number MUST be explicitly outputted. " +
            "Ensure the filled cells form a recognizable connected shape with symmetry where appropriate, avoiding isolated noise pixels.",
            candidateCount, width, height, excludePrompt, width, height, width, height
        );

        int maxAttempts = 3;
        Exception lastException = null;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return callGeminiApi(prompt, gridModelName);
            } catch (Exception e) {
                lastException = e;
                System.err.println("[AI] generatePuzzleJson attempt " + attempt + " failed: " + e.getMessage());
                if (attempt < maxAttempts) {
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }
        throw new RuntimeException("[AI] All attempts to generate puzzle JSON failed", lastException);
    }

    @Override
    public String generateThemeJson(int width, int height, java.util.List<String> recentThemes) {
        String template = readResource(themePromptResource);
        String prompt = String.format(
            template,
            width, height, width, height,
            (recentThemes == null || recentThemes.isEmpty()) ? "none" : String.join(", ", recentThemes)
        );

        int maxAttempts = 3;
        Exception lastException = null;

        // Try primary theme model (themeModelName)
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return callGeminiApi(prompt, themeModelName);
            } catch (org.springframework.web.client.HttpStatusCodeException e) {
                lastException = e;
                System.err.println("[AI] generateThemeJson with " + themeModelName + " attempt " + attempt + " failed (HTTP " + e.getStatusCode() + ")");
                if (e.getStatusCode().value() == 429) {
                    System.err.println("[AI] 429 Resource Exhausted. Falling back to " + gridModelName);
                    break; // Fallback immediately
                }
            } catch (Exception e) {
                lastException = e;
                System.err.println("[AI] generateThemeJson with " + themeModelName + " attempt " + attempt + " failed: " + e.getMessage());
            }
            if (attempt < maxAttempts) {
                try { Thread.sleep(2000); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
            }
        }

        // Fallback to grid model (gemini-3-flash)
        log.warn("[AI] Calling fallback model {} for theme generation..."  ,gridModelName);
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return callGeminiApi(prompt, gridModelName);
            } catch (Exception e) {
                lastException = e;
                System.err.println("[AI] Fallback theme generation attempt " + attempt + " failed: " + e.getMessage());
                if (attempt < maxAttempts) {
                    try { Thread.sleep(2000); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
                }
            }
        }
        throw new RuntimeException("[AI] All attempts to generate themes failed.", lastException);
    }

    @Override
    public String generatePuzzleJsonForTheme(int width, int height, String themeName, String themeDescription) {
        int candidateCount = (width >= 25 || height >= 25) ? 2 : 5;
        String template = readResource(gridPromptResource);
        String prompt = String.format(
            template,
            candidateCount, width, height, themeName, themeDescription, width, height, themeName, themeName, themeName, width, height
        );

        int maxAttempts = 3;
        Exception lastException = null;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return callGeminiApi(prompt, gridModelName);
            } catch (Exception e) {
                lastException = e;
        log.warn("[AI] generatePuzzleJsonForTheme with {} attempt {} failed: {}", gridModelName, attempt, e.getMessage());
                if (attempt < maxAttempts) {
                    try { Thread.sleep(2000); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
                }
            }
        }
        throw new RuntimeException("[AI] All attempts to generate grid for theme failed.", lastException);
    }

    private String callGeminiApi(String prompt, String model) {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new IllegalStateException("[AI] API Key is missing. Cannot query Gemini API.");
        }

        String url = "https://generativelanguage.googleapis.com/v1/models/" + model + ":generateContent?key=" + apiKey;
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> requestBody = new HashMap<>();
        Map<String, Object> contents = new HashMap<>();
        Map<String, Object> parts = new HashMap<>();
        parts.put("text", prompt);
        contents.put("parts", Collections.singletonList(parts));
        requestBody.put("contents", Collections.singletonList(contents));

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
        String response = restTemplate.postForObject(url, entity, String.class);

        try {
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
            throw new RuntimeException("Failed to parse Gemini response: " + response, e);
        }
    }

    private String readResource(org.springframework.core.io.Resource resource) {
        try (java.io.InputStream is = resource.getInputStream()) {
            return new String(is.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
        } catch (java.io.IOException e) {
            throw new IllegalStateException("Failed to read prompt resource: " + resource.getFilename(), e);
        }
    }
}
