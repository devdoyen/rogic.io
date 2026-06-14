package com.devdoyen.nemologic.service;

import com.devdoyen.nemologic.model.VisitorLog;
import com.devdoyen.nemologic.repository.VisitorLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class VisitorService {

    private final VisitorLogRepository visitorLogRepository;

    public VisitorService(VisitorLogRepository visitorLogRepository) {
        this.visitorLogRepository = visitorLogRepository;
    }

    @Transactional
    public void recordVisit(String uuid, String ip) {
        if (uuid == null || uuid.trim().isEmpty()) {
            return;
        }

        String ipHash = hashIp(ip);
        LocalDate today = LocalDate.now();

        // Guard against duplicate UUID or IP visits on the same day
        boolean uuidVisitedToday = visitorLogRepository.existsByUuidAndVisitedDate(uuid, today);
        boolean ipVisitedToday = visitorLogRepository.existsByIpHashAndVisitedDate(ipHash, today);

        if (!uuidVisitedToday && !ipVisitedToday) {
            VisitorLog log = new VisitorLog(uuid, ipHash, LocalDateTime.now(), today);
            visitorLogRepository.save(log);
        }
    }

    public String hashIp(String ip) {
        if (ip == null || ip.trim().isEmpty()) {
            return "unknown-ip";
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(ip.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception ex) {
            return "hashing-error";
        }
    }
}
