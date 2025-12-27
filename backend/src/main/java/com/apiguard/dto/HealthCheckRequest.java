package com.apiguard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HealthCheckRequest {

    @NotBlank(message = "URL es requerida")
    @Pattern(regexp = "^https?://.*", message = "URL debe comenzar con http:// o https://")
    private String url;

    private String method = "GET";
    private Map<String, String> headers;
    private String body;
    private Integer timeoutSeconds = 30;
    private Boolean followRedirects = true;
}