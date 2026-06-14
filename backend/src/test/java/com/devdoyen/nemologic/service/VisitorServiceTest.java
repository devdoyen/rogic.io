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

    @BeforeEach
    public void setUp() {
        visitorLogRepository = mock(VisitorLogRepository.class);
        visitorService = new VisitorService(visitorLogRepository);
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
}
