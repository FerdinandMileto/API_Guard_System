package com.apiguard.controller;

import com.apiguard.dto.DiagnosticRequest;
import com.apiguard.dto.DiagnosticReport;
import com.apiguard.service.DiagnosticService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

/**
 * Controlador de herramientas de diagnóstico
 * 
 * Proporciona análisis automatizado de problemas comunes
 * en integraciones de APIs
 */
@RestController
@RequestMapping("/api/diagnose")
@RequiredArgsConstructor
@Tag(name = "Diagnostic Tools", description = "Herramientas de diagnóstico automatizado")
@CrossOrigin(origins = "*")
public class DiagnosticController {

    private final DiagnosticService diagnosticService;

    /**
     * Realiza un diagnóstico completo de una integración API
     * 
     * Analiza:
     * - Formato de headers
     * - Validez de tokens
     * - Estructura de payload
     * - Códigos de estado HTTP
     * - Tiempos de respuesta
     * 
     * @param request Configuración de la API a diagnosticar
     * @return Reporte detallado con problemas encontrados y soluciones
     */
    @PostMapping("/full")
    @Operation(summary = "Diagnóstico completo de API", description = "Analiza una integración API y detecta problemas comunes")
    public ResponseEntity<DiagnosticReport> fullDiagnosis(
            @Valid @RequestBody DiagnosticRequest request) {

        DiagnosticReport report = diagnosticService.performFullDiagnosis(request);
        return ResponseEntity.ok(report);
    }

    /**
     * Valida el formato de autenticación
     * 
     * @param authHeader Header de autenticación
     * @param authType   Tipo de autenticación (Bearer, Basic, ApiKey)
     * @return Resultado de validación con sugerencias
     */
    @PostMapping("/auth/validate")
    @Operation(summary = "Validar autenticación", description = "Verifica formato correcto de headers de autenticación")
    public ResponseEntity<?> validateAuth(
            @RequestParam String authHeader,
            @RequestParam(defaultValue = "Bearer") String authType) {

        var result = diagnosticService.validateAuthentication(authHeader, authType);
        return ResponseEntity.ok(result);
    }

    /**
     * Analiza errores HTTP comunes
     * 
     * @param statusCode Código de estado HTTP
     * @param endpoint   Endpoint que generó el error
     * @return Explicación del error y pasos de solución
     */
    @GetMapping("/error/analyze")
    @Operation(summary = "Analizar error HTTP", description = "Proporciona explicación y solución para códigos de error")
    public ResponseEntity<?> analyzeError(
            @RequestParam int statusCode,
            @RequestParam(required = false) String endpoint) {

        var analysis = diagnosticService.analyzeHttpError(statusCode, endpoint);
        return ResponseEntity.ok(analysis);
    }

    /**
     * Valida estructura JSON de un payload
     * 
     * @param payload JSON a validar
     * @param schema  Schema esperado (opcional)
     * @return Resultado de validación con errores específicos
     */
    @PostMapping("/payload/validate")
    @Operation(summary = "Validar payload JSON", description = "Verifica estructura y formato de payloads JSON")
    public ResponseEntity<?> validatePayload(
            @RequestBody String payload,
            @RequestParam(required = false) String schema) {

        var validation = diagnosticService.validateJsonPayload(payload, schema);
        return ResponseEntity.ok(validation);
    }

    /**
     * Simula una llamada API con los parámetros proporcionados
     * 
     * Útil para testing antes de implementar en producción
     * 
     * @param request Configuración de la llamada a simular
     * @return Resultado de la simulación con logs detallados
     */
    @PostMapping("/simulate")
    @Operation(summary = "Simular llamada API", description = "Ejecuta y registra una llamada API de prueba")
    public ResponseEntity<?> simulateApiCall(
            @Valid @RequestBody DiagnosticRequest request) {

        var simulation = diagnosticService.simulateApiCall(request);
        return ResponseEntity.ok(simulation);
    }

    /**
     * Genera ejemplos de código para integración
     * 
     * @param language Lenguaje de programación (java, python, php, javascript)
     * @param endpoint Endpoint a integrar
     * @param method   Método HTTP
     * @return Código de ejemplo listo para usar
     */
    @GetMapping("/codegen")
    @Operation(summary = "Generar código de ejemplo", description = "Crea snippets de código para integración API")
    public ResponseEntity<?> generateCode(
            @RequestParam String language,
            @RequestParam String endpoint,
            @RequestParam(defaultValue = "GET") String method,
            @RequestParam(required = false) String authType) {

        var code = diagnosticService.generateIntegrationCode(
                language, endpoint, method, authType);
        return ResponseEntity.ok(code);
    }

    /**
     * Obtiene recomendaciones de mejores prácticas
     * 
     * @param apiType Tipo de API (REST, SOAP)
     * @return Lista de best practices aplicables
     */
    @GetMapping("/bestpractices")
    @Operation(summary = "Mejores prácticas", description = "Recomendaciones para integración segura y eficiente")
    public ResponseEntity<?> getBestPractices(
            @RequestParam(defaultValue = "REST") String apiType) {

        var practices = diagnosticService.getBestPractices(apiType);
        return ResponseEntity.ok(practices);
    }
}