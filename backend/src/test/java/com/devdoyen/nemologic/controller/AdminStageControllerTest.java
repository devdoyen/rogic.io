package com.devdoyen.nemologic.controller;

import com.devdoyen.nemologic.model.Stage;
import com.devdoyen.nemologic.repository.StageRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
public class AdminStageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private StageRepository stageRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private com.devdoyen.nemologic.security.AdminSessionManager sessionManager;

    private String adminToken;

    @org.junit.jupiter.api.BeforeEach
    public void setUp() {
        adminToken = java.util.UUID.randomUUID().toString();
        sessionManager.registerToken(adminToken, "admin");
    }

    @Test
    public void testGetAllStagesForAdmin() throws Exception {
        mockMvc.perform(get("/api/admin/stages")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()", greaterThanOrEqualTo(8)));
    }

    @Test
    public void testCreateStageValid() throws Exception {
        int[][] validGrid = {
            {0, 1, 0},
            {1, 1, 1},
            {0, 1, 0}
        };
        Stage stage = new Stage(null, "Admin Custom Stage", 3, 3, validGrid);

        String json = objectMapper.writeValueAsString(stage);

        mockMvc.perform(post("/api/admin/stages")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Admin Custom Stage")))
                .andExpect(jsonPath("$.width", is(3)))
                .andExpect(jsonPath("$.height", is(3)))
                .andExpect(jsonPath("$.active", is(true)))
                .andExpect(jsonPath("$.approved", is(true)));
    }

    @Test
    public void testCreateStageInvalidNonUnique() throws Exception {
        // A non-unique 2x2 grid (checkerboard pattern: multiple solutions)
        int[][] invalidGrid = {
            {1, 0},
            {0, 1}
        };
        Stage stage = new Stage(null, "Non Unique Stage", 2, 2, invalidGrid);
        String json = objectMapper.writeValueAsString(stage);

        mockMvc.perform(post("/api/admin/stages")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testApproveStage() throws Exception {
        // Save an unapproved stage
        Stage stage = new Stage(null, "Pending Stage", 3, 3, new int[][]{{0, 1, 0}, {1, 1, 1}, {0, 1, 0}});
        stage.setApproved(false);
        stage.setActive(false);
        Stage saved = stageRepository.save(stage);

        mockMvc.perform(put("/api/admin/stages/" + saved.getId() + "/approve")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());

        Stage updated = stageRepository.findById(saved.getId()).orElseThrow();
        assertFalse(updated.isActive());
        assertTrue(updated.isApproved());
    }

    @Test
    public void testDeleteStageSoft() throws Exception {
        Stage stage = new Stage(null, "To Delete Stage", 3, 3, new int[][]{{0, 1, 0}, {1, 1, 1}, {0, 1, 0}});
        Stage saved = stageRepository.save(stage);

        mockMvc.perform(delete("/api/admin/stages/" + saved.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());

        assertFalse(stageRepository.findById(saved.getId()).isPresent());
    }

    @Test
    public void testRestoreStage() throws Exception {
        Stage stage = new Stage(null, "To Restore Stage", 3, 3, new int[][]{{0, 1, 0}, {1, 1, 1}, {0, 1, 0}});
        stage.setActive(false);
        Stage saved = stageRepository.save(stage);

        mockMvc.perform(put("/api/admin/stages/" + saved.getId() + "/restore")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());

        Stage updated = stageRepository.findById(saved.getId()).orElseThrow();
        assertTrue(updated.isActive());
        assertTrue(updated.isApproved());
    }

    @Test
    public void testTriggerAiGenerationFromAdmin() throws Exception {
        mockMvc.perform(post("/api/admin/stages/ai-generate")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active", is(false)))
                .andExpect(jsonPath("$.approved", is(true)));
    }

    @Test
    public void testTriggerAiGenerationFromAdminWithCustomSize() throws Exception {
        mockMvc.perform(post("/api/admin/stages/ai-generate")
                        .param("width", "10")
                        .param("height", "10")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.width", is(10)))
                .andExpect(jsonPath("$.height", is(10)))
                .andExpect(jsonPath("$.active", is(false)))
                .andExpect(jsonPath("$.approved", is(true)));
    }
}
