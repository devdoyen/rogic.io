package com.devdoyen.nemologic.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
public class StageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void getAllStagesShouldReturnStagesList() throws Exception {
        // We expect eight stages to be returned after seeding larger stages.
        mockMvc.perform(get("/api/stages"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(8)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].name", is("Diamond Emblem")))
                .andExpect(jsonPath("$[0].width", is(5)))
                .andExpect(jsonPath("$[0].height", is(5)));
    }

    @Test
    public void getStageByIdShouldReturnStageDetails() throws Exception {
        // We expect stage 1 to exist and return its full details.
        mockMvc.perform(get("/api/stages/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Diamond Emblem")))
                .andExpect(jsonPath("$.solutionGrid", hasSize(5)));
    }

    @Test
    public void getStageByIdShouldReturnNotFoundForInvalidId() throws Exception {
        mockMvc.perform(get("/api/stages/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void startStageShouldIncrementAttemptsAndReturnOk() throws Exception {
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/stages/1/start"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/stages/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalAttempts").exists());
    }

    @Test
    public void triggerAiGenerationShouldCreateStageAndReturnOk() throws Exception {
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/stages/ai-generate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("AI Daily Puzzle")))
                .andExpect(jsonPath("$.width", is(5)))
                .andExpect(jsonPath("$.height", is(5)));
    }
}
