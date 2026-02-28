package com.urlshortener.controller;

import com.urlshortener.dto.DailyClicksResponse;
import com.urlshortener.dto.UserAgentClicksResponse;
import com.urlshortener.service.AnalyticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/{id}/clicks")
    public ResponseEntity<Map<String, Long>> getTotalClicks(@PathVariable Long id) {
        long total = analyticsService.getTotalClicks(id);
        return ResponseEntity.ok(Map.of("linkId", id, "totalClicks", total));
    }

    @GetMapping("/{id}/clicks/daily")
    public ResponseEntity<List<DailyClicksResponse>> getDailyClicks(@PathVariable Long id) {
        return ResponseEntity.ok(analyticsService.getDailyClicks(id));
    }

    @GetMapping("/{id}/clicks/user-agents")
    public ResponseEntity<List<UserAgentClicksResponse>> getUserAgentBreakdown(@PathVariable Long id) {
        return ResponseEntity.ok(analyticsService.getUserAgentBreakdown(id));
    }
}
