package com.devdoyen.nemologic.controller;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Gauge;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@SuppressWarnings("null")
public class VisitorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MeterRegistry meterRegistry;

    @Test
    public void recordVisitShouldReturnOk() throws Exception {
        mockMvc.perform(post("/api/analytics/visit")
                        .param("uuid", "integration-test-uuid")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk());
    }

    @Test
    public void prometheusMetricsShouldContainVisitorMetrics() throws Exception {
        // Record a visit first to ensure data exists
        mockMvc.perform(post("/api/analytics/visit")
                        .param("uuid", "prometheus-test-uuid")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk());

        // Assert that the Gauges exist in the MeterRegistry
        Gauge totalVisitsGauge = meterRegistry.find("visitor.total.visits").gauge();
        Gauge uniqueVisitorsGauge = meterRegistry.find("visitor.unique.visitors").gauge();
        Gauge dailyUniqueVisitorsGauge = meterRegistry.find("visitor.daily.unique.visitors").gauge();

        assertNotNull(totalVisitsGauge, "visitor.total.visits gauge should not be null");
        assertNotNull(uniqueVisitorsGauge, "visitor.unique.visitors gauge should not be null");
        assertNotNull(dailyUniqueVisitorsGauge, "visitor.daily.unique.visitors gauge should not be null");

        // The exact values can be evaluated
        assertTrue(totalVisitsGauge.value() >= 1);
        assertTrue(uniqueVisitorsGauge.value() >= 1);
        assertTrue(dailyUniqueVisitorsGauge.value() >= 1);
    }

    @Test
    public void getTelemetryStatsShouldReturnStats() throws Exception {
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/analytics/stats")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.dailyUniqueVisitors").exists())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.totalUniqueVisitors").exists())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.totalVisits").exists())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.totalAttempts").exists())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.totalClears").exists())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.uptimeRatio").value(99.98))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.mtbf").value(720.0))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.mttr").value(0.8));
    }
}

