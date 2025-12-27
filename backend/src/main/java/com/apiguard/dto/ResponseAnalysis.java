package com.apiguard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResponseAnalysis {

    private Integer statusCode;
    private String statusCategory;
    private Boolean isExpectedStatus;
    private String contentType;
    private Integer contentLength;
    private Map<String, String> responseHeaders;
    private String errorMessage;
}