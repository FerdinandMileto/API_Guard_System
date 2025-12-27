package com.apiguard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiagnosticRequest {

    @NotBlank(message = "Endpoint es requerido")
    private String endpoint;

    @NotBlank(message = "Método HTTP es requerido")
    private String method;

    private Map<String, String> headers;
    private String requestBody;
    private String authenticationType;
    private String authenticationValue;
    private Integer expectedStatusCode;
}