package com.apiguard.controller;

import com.apiguard.dto.*;
import com.apiguard.service.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador de Analytics y Data Science
 * 
 * Proporciona endpoints para análisis estadístico de métricas
 */
@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
@Tag(name = "Analytics & Data Science", description = "Análisis estadístico y detección de anomalías")
@CrossOrigin(origins = "*")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    /**
     * Obtiene estadísticas descriptivas de latencia
     * Incluye percentiles P50, P90, P95, P99 para SLA
     */
    @GetMapping("/latency-stats")
    @Operation(summary = "Estadísticas de latencia", description = "Calcula media, mediana, desviación estándar y percentiles (P50, P90, P95, P99) de latencia")
    public ResponseEntity<LatencyStatistics> getLatencyStatistics(
            @RequestParam String endpoint) {

        LatencyStatistics stats = analyticsService.calculateLatencyStats(endpoint);
        return ResponseEntity.ok(stats);
    }

    /**
     * Analiza distribución de errores en el período especificado
     */
    @GetMapping("/error-distribution")
    @Operation(summary = "Distribución de errores", description = "Analiza errores más comunes, categorías (4xx vs 5xx) y tendencias")
    public ResponseEntity<ErrorDistribution> getErrorDistribution(
            @RequestParam(defaultValue = "last_24h") String period) {

        ErrorDistribution distribution = analyticsService.analyzeErrorDistribution(period);
        return ResponseEntity.ok(distribution);
    }

    /**
     * Detecta anomalías usando análisis de Z-score
     */
    @GetMapping("/anomalies")
    @Operation(summary = "Detección de anomalías", description = "Detecta picos de latencia y comportamiento anómalo usando Z-score (threshold: desviaciones estándar)")
    public ResponseEntity<AnomalyReport> detectAnomalies(
            @RequestParam String endpoint,
            @RequestParam(defaultValue = "2.0") double threshold) {

        AnomalyReport report = analyticsService.detectAnomalies(endpoint, threshold);
        return ResponseEntity.ok(report);
    }

    /**
     * Genera resumen ejecutivo de rendimiento
     */
    @GetMapping("/performance-summary")
    @Operation(summary = "Resumen de rendimiento", description = "Dashboard ejecutivo con métricas globales, SLA compliance y top endpoints")
    public ResponseEntity<PerformanceSummary> getPerformanceSummary() {
        PerformanceSummary summary = analyticsService.generatePerformanceSummary();
        return ResponseEntity.ok(summary);
    }

    /**
     * Reporte SLA simplificado
     */
    @GetMapping("/sla-report")
    @Operation(summary = "Reporte SLA", description = "Verifica cumplimiento de SLA (P95 < 1000ms, Uptime > 99%)")
    public ResponseEntity<SLAReport> getSLAReport(@RequestParam String endpoint) {
        LatencyStatistics stats = analyticsService.calculateLatencyStats(endpoint);

        boolean meetsLatencySLA = stats.getP95() != null && stats.getP95() < 1000;

        SLAReport report = SLAReport.builder()
                .endpoint(endpoint)
                .p95Latency(stats.getP95())
                .p99Latency(stats.getP99())
                .latencySLATarget(1000.0)
                .meetsLatencySLA(meetsLatencySLA)
                .performanceGrade(stats.getPerformanceGrade())
                .recommendation(
                        meetsLatencySLA ? "✅ Cumple con SLA de latencia" : "⚠️ No cumple SLA: P95 debe ser < 1000ms")
                .build();

        return ResponseEntity.ok(report);
    }

    /**
     * DTO interno para reporte SLA
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class SLAReport {
        private String endpoint;
        private Double p95Latency;
        private Double p99Latency;
        private Double latencySLATarget;
        private Boolean meetsLatencySLA;
        private String performanceGrade;
        private String recommendation;
    }
}