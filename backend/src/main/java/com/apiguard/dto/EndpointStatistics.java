package com.apiguard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EndpointStatistics {

    private String endpoint;
    private Double uptimePercentage;
    private Long totalChecks;
    private Long successfulChecks;
    private Long failedChecks;
    private Double avgResponseTimeMs;
    private Long minResponseTimeMs;
    private Long maxResponseTimeMs;
    private LocalDateTime lastCheckTime;
    private LocalDateTime lastSuccessTime;
    private LocalDateTime lastFailureTime;
}