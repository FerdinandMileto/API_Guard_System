package com.apiguard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Distribución y análisis de errores
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorDistribution {

    private String period; // "last_24h", "last_7d", "last_30d"
    private Long totalRequests;
    private Long totalErrors;
    private Double errorRate; // Porcentaje

    // Top errores más comunes
    private List<ErrorFrequency> topErrors;

    // Distribución por categoría
    private Map<String, Long> errorsByCategory; // 4xx, 5xx

    // Tendencia
    private String trend; // "INCREASING", "DECREASING", "STABLE"

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ErrorFrequency {
        private Integer statusCode;
        private String errorMessage;
        private Long count;
        private Double percentage;
        private List<String> affectedEndpoints;
    }
}