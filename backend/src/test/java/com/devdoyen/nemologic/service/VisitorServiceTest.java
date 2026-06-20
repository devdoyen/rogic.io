package com.devdoyen.nemologic.service;

import com.devdoyen.nemologic.model.VisitorLog;
import com.devdoyen.nemologic.repository.VisitorLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SuppressWarnings("null")
public class VisitorServiceTest {

    private VisitorService visitorService;
    private VisitorLogRepository visitorLogRepository;
    private com.devdoyen.nemologic.repository.StageRepository stageRepository;

    @BeforeEach
    public void setUp() {
        visitorLogRepository = mock(VisitorLogRepository.class);
        stageRepository = mock(com.devdoyen.nemologic.repository.StageRepository.class);
        visitorService = new VisitorService(visitorLogRepository, stageRepository);
    }

    @Test
    public void testHashIp() {
        String ip = "127.0.0.1";
        String hash = visitorService.hashIp(ip);
        assertNotNull(hash);
        assertNotEquals(ip, hash);
        assertEquals(64, hash.length()); // SHA-256 is 64 hex characters
    }

    @Test
    public void testRecordNewVisit() {
        String uuid = "test-uuid-12345";
        String ip = "192.168.1.100";
        String ipHash = visitorService.hashIp(ip);
        LocalDate today = LocalDate.now();

        when(visitorLogRepository.existsByUuidAndVisitedDate(uuid, today)).thenReturn(false);
        when(visitorLogRepository.existsByIpHashAndVisitedDate(ipHash, today)).thenReturn(false);

        visitorService.recordVisit(uuid, ip);

        ArgumentCaptor<VisitorLog> logCaptor = ArgumentCaptor.forClass(VisitorLog.class);
        verify(visitorLogRepository, times(1)).save(logCaptor.capture());

        VisitorLog savedLog = logCaptor.getValue();
        assertEquals(uuid, savedLog.getUuid());
        assertEquals(ipHash, savedLog.getIpHash());
        assertEquals(today, savedLog.getVisitedDate());
        assertNotNull(savedLog.getVisitedAt());
    }

    @Test
    public void testDoNotRecordDuplicateUuidVisitOnSameDay() {
        String uuid = "test-uuid-12345";
        String ip = "192.168.1.100";
        LocalDate today = LocalDate.now();

        when(visitorLogRepository.existsByUuidAndVisitedDate(uuid, today)).thenReturn(true);

        visitorService.recordVisit(uuid, ip);

        verify(visitorLogRepository, never()).save(any(VisitorLog.class));
    }

    @Test
    public void testDoNotRecordDuplicateIpHashVisitOnSameDay() {
        String uuid = "test-uuid-12345";
        String ip = "192.168.1.100";
        String ipHash = visitorService.hashIp(ip);
        LocalDate today = LocalDate.now();

        when(visitorLogRepository.existsByUuidAndVisitedDate(uuid, today)).thenReturn(false);
        when(visitorLogRepository.existsByIpHashAndVisitedDate(ipHash, today)).thenReturn(true);

        visitorService.recordVisit(uuid, ip);

        verify(visitorLogRepository, never()).save(any(VisitorLog.class));
    }

    @Test
    public void testGetTelemetryStats() {
        LocalDate today = LocalDate.now();
        when(visitorLogRepository.countUniqueVisitorsByDate(today)).thenReturn(5L);
        when(visitorLogRepository.countUniqueVisitors()).thenReturn(100L);
        when(visitorLogRepository.count()).thenReturn(500L);
        when(stageRepository.sumTotalAttempts()).thenReturn(1200L);
        when(stageRepository.sumTotalClears()).thenReturn(800L);

        com.devdoyen.nemologic.dto.TelemetryStatsResponse stats = visitorService.getTelemetryStats();

        assertEquals(5L, stats.getDailyUniqueVisitors());
        assertEquals(100L, stats.getTotalUniqueVisitors());
        assertEquals(500L, stats.getTotalVisits());
        assertEquals(1200L, stats.getTotalAttempts());
        assertEquals(800L, stats.getTotalClears());
        assertEquals(99.98, stats.getUptimeRatio());
        assertEquals(720.0, stats.getMtbf());
        assertEquals(0.8, stats.getMttr());
    }
}
