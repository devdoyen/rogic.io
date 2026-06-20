package com.devdoyen.nemologic.controller;

import com.devdoyen.nemologic.service.VisitorService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/analytics")
@CrossOrigin(origins = "*")
public class VisitorController {

    private final VisitorService visitorService;

    public VisitorController(VisitorService visitorService) {
        this.visitorService = visitorService;
    }

    @PostMapping("/visit")
    public void recordVisit(@RequestParam String uuid, HttpServletRequest request) {
        String ip = getClientIp(request);
        visitorService.recordVisit(uuid, ip);
    }

    @GetMapping("/stats")
    public com.devdoyen.nemologic.dto.TelemetryStatsResponse getTelemetryStats() {
        return visitorService.getTelemetryStats();
    }

    private String getClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.trim().isEmpty()) {
            return xff.split(",")[0].trim();
        }
        String proxyClientIp = request.getHeader("Proxy-Client-IP");
        if (proxyClientIp != null && !proxyClientIp.trim().isEmpty()) {
            return proxyClientIp;
        }
        String wlProxyClientIp = request.getHeader("WL-Proxy-Client-IP");
        if (wlProxyClientIp != null && !wlProxyClientIp.trim().isEmpty()) {
            return wlProxyClientIp;
        }
        return request.getRemoteAddr();
    }
}
