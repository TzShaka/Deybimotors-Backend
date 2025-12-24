package com.deybimotors.controller;

import com.deybimotors.dto.DashboardDTO;
import com.deybimotors.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador de Dashboard - RF-003
 * Endpoints: /api/dashboard/**
 */
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DashboardController {

    private final DashboardService dashboardService;

    /**
     * GET /api/dashboard
     * Obtener datos del dashboard
     */
    @GetMapping
    public ResponseEntity<DashboardDTO.DashboardResponse> obtenerDatosDashboard(
            @RequestParam Long sedeId
    ) {
        return ResponseEntity.ok(dashboardService.obtenerDatosDashboard(sedeId));
    }
}