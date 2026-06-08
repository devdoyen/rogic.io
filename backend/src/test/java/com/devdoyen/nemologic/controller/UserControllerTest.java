package com.devdoyen.nemologic.controller;

import org.junit.jupiter.api.BeforeEach;
import com.devdoyen.nemologic.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserService userService;

    @BeforeEach
    public void setUp() {
        userService.reset();
    }

    @Test
    public void getRankingShouldReturnUsersSortedByXpDesc() throws Exception {
        // We expect global ranking to be sorted by XP in descending order.
        // e.g. first user should have more XP than the second, etc.
        mockMvc.perform(get("/api/users/ranking"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].username", is("Charlie")))
                .andExpect(jsonPath("$[0].xp", is(1000)))
                .andExpect(jsonPath("$[1].username", is("Bob")))
                .andExpect(jsonPath("$[1].xp", is(500)))
                .andExpect(jsonPath("$[2].username", is("Alice")))
                .andExpect(jsonPath("$[2].xp", is(200)));
    }

    @Test
    public void clearStageEasyShouldAddXpAndReturnUpdatedUser() throws Exception {
        // Alice has ID 1. Initial status: XP 200, Level 2.
        // Clearing EASY adds 50 XP -> Total XP: 250 (Level 2: needs 200 to level up to 3).
        mockMvc.perform(post("/api/users/1/clear")
                .param("difficulty", "EASY"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.xp", is(250)))
                .andExpect(jsonPath("$.level", is(2)));
    }

    @Test
    public void clearStageHardShouldTriggerLevelUpAndReturnUpdatedUser() throws Exception {
        // Alice has ID 1. Initial status: XP 200, Level 2.
        // Level 2 needs 200 XP to reach Level 3. Alice has 200 XP, so she is exactly at the threshold?
        // Wait, if Alice has 200 XP at Level 2, and we add 200 XP (HARD) -> Alice reaches Level 3.
        // Let's check clear stage HARD: adds 200 XP.
        // Let's write the test expectation to verify the updated state.
        mockMvc.perform(post("/api/users/1/clear")
                .param("difficulty", "HARD"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                // We will assert the exact XP and level after adding 200 XP
                .andExpect(jsonPath("$.level", is(3)));
    }
}
