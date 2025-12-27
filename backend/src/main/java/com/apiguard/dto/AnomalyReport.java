package com.apiguard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Reporte de detección de anomalías
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnomalyReport {

    private String endpoint;
    private LocalDateTime detectionTime;
    private String anomalyType; // "LATENCY_SPIKE", "ERROR_SURGE", "TIMEOUT"

    private List<AnomalyEvent> anomalies;

    private String severity; // "LOW", "MEDIUM", "HIGH", "CRITICAL"
    private String recommendation;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AnomalyEvent {
        private LocalDateTime timestamp;
        private Double value;
        private Double expectedValue;
        private Double zScore; // Desviaciones estándar
        private String description;
    }
}