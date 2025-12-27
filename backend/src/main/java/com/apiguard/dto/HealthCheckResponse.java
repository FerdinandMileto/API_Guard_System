package com.apiguard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HealthCheckResponse {

    private String url;
    private String status;
    private Integer statusCode;
    private Long responseTimeMs;
    private String message;
    private LocalDateTime timestamp;
    private Map<String, Object> metadata;
    private List<String> issues;
    private List<String> recommendations;
}