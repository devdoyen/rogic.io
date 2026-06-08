package com.devdoyen.nemologic.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
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
        // We expect at least two stages (e.g. ID 1, "Heart Shape", ID 2 "Checkerboard") to be returned.
        // In the Red phase, StageService returns an empty list, so this will fail.
        mockMvc.perform(get("/api/stages"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                // DataSeeder가 AI 퍼즐을 추가 생성하므로 최소 2개 이상임을 검증
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(2))))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].name", is("Heart Shape")))
                .andExpect(jsonPath("$[0].width", is(5)))
                .andExpect(jsonPath("$[0].height", is(5)));
    }

    @Test
    public void getStageByIdShouldReturnStageDetails() throws Exception {
        // We expect stage 1 to exist and return its full details.
        // In the Red phase, StageService returns Optional.empty() resulting in 404, so this will fail.
        mockMvc.perform(get("/api/stages/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Heart Shape")))
                .andExpect(jsonPath("$.solutionGrid", hasSize(5)));
    }

    @Test
    public void getStageByIdShouldReturnNotFoundForInvalidId() throws Exception {
        mockMvc.perform(get("/api/stages/999"))
                .andExpect(status().isNotFound());
    }
}
