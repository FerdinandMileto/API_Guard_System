package com.apiguard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiagnosticReport {

    private String diagnosticId;
    private LocalDateTime timestamp;
    private String endpoint;
    private String overallStatus;

    private AuthenticationAnalysis authAnalysis;
    private PayloadAnalysis payloadAnalysis;
    private ResponseAnalysis responseAnalysis;
    private PerformanceAnalysis performanceAnalysis;

    private List<Issue> issues;
    private List<Recommendation> recommendations;
    private String knowledgeBaseLink;
}