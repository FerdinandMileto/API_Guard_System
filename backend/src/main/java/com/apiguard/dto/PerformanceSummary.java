package com.apiguard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Resumen ejecutivo de rendimiento
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PerformanceSummary {

    private LocalDateTime generatedAt;
    private String period;

    // Métricas globales
    private Long totalEndpoints;
    private Long totalRequests;
    private Double averageLatency;
    private Double uptimePercentage;

    // SLA Metrics
    private Double p95Latency;
    private Double p99Latency;
    private Boolean meetsSLA; // true si P95 < 1000ms

    // Top endpoints
    private List<EndpointPerformance> topSlowestEndpoints;
    private List<EndpointPerformance> topFastestEndpoints;

    // Alertas
    private List<String> alerts;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EndpointPerformance {
        private String endpoint;
        private Double avgLatency;
        private Double p95Latency;
        private Long requestCount;
        private String grade;
    }
}