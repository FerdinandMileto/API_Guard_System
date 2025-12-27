package com.apiguard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Estadísticas descriptivas de latencia
 * Incluye percentiles y métricas para SLA
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LatencyStatistics {

    private String endpoint;
    private LocalDateTime periodStart;
    private LocalDateTime periodEnd;

    // Estadísticas básicas
    private Double mean;
    private Double median;
    private Double standardDeviation;
    private Double variance;

    // Percentiles (SLA metrics)
    private Double p50; // Mediana
    private Double p90; // 90% de requests bajo este tiempo
    private Double p95; // 95% de requests bajo este tiempo
    private Double p99; // 99% de requests bajo este tiempo

    // Min/Max
    private Long minLatency;
    private Long maxLatency;

    // Metadata
    private Long totalSamples;
    private String performanceGrade; // A, B, C, D, F
}