package com.example.Shop.controllers;

import com.example.Shop.dto.AnalyticsDTO;
import com.example.Shop.dto.StaffKPIDTO;
import com.example.Shop.services.AnalyticsService;
import com.example.Shop.services.KPIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/analytics")
@CrossOrigin("*")
public class AnalyticsController {

    @Autowired
    private AnalyticsService analyticsService;

    @GetMapping("/abc")
    public List<AnalyticsDTO> getABC(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return analyticsService.getABCAnalysis(start, end);
    }

    @Autowired
    private KPIService kpiService;

    @GetMapping("/staff-kpi")
    public List<StaffKPIDTO> getStaffKPI(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return kpiService.calculateStaffKPI(start, end);
    }
}