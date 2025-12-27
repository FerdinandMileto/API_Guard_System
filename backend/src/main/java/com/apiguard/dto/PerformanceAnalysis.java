package com.apiguard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PerformanceAnalysis {

    private Long responseTimeMs;
    private String performanceRating;
    private Long dnsLookupMs;
    private Long connectionTimeMs;
    private Long tlsHandshakeMs;
    private Long timeToFirstByteMs;
}