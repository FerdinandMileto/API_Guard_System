package com.apiguard.controller;

import com.apiguard.dto.HealthCheckRequest;
import com.apiguard.dto.HealthCheckResponse;
import com.apiguard.service.HealthMonitorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * Controlador para monitoreo de salud de APIs
 * 
 * Proporciona endpoints para verificar disponibilidad, latencia
 * y estado de servicios externos REST/SOAP
 */
@RestController
@RequestMapping("/api/health")
@RequiredArgsConstructor
@Tag(name = "Health Monitor", description = "Monitoreo de salud de APIs")
@CrossOrigin(origins = "*")
public class HealthController {

    private final HealthMonitorService healthService;

    /**
     * Verifica el estado de un endpoint específico
     * 
     * @param url URL del endpoint a verificar
     * @return Estado del endpoint con métricas
     */
    @GetMapping("/check")
    @Operation(summary = "Verificar salud de un endpoint", description = "Realiza un health check simple de un endpoint")
    public ResponseEntity<HealthCheckResponse> checkEndpoint(
            @RequestParam String url) {

        HealthCheckResponse response = healthService.performHealthCheck(url);
        return ResponseEntity.ok(response);
    }

    /**
     * Verifica un endpoint con configuración personalizada
     * 
     * @param request Configuración detallada del health check
     * @return Resultado detallado del análisis
     */
    @PostMapping("/check/advanced")
    @Operation(summary = "Health check avanzado", description = "Verifica endpoint con headers, método y timeout personalizados")
    public ResponseEntity<HealthCheckResponse> advancedHealthCheck(
            @Valid @RequestBody HealthCheckRequest request) {

        HealthCheckResponse response = healthService.performAdvancedCheck(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Obtiene el historial de health checks de un endpoint
     * 
     * @param url URL del endpoint
     * @return Lista de health checks históricos
     */
    @GetMapping("/history")
    @Operation(summary = "Historial de health checks", description = "Obtiene el historial de verificaciones de un endpoint")
    public ResponseEntity<List<HealthCheckResponse>> getHistory(
            @RequestParam String url,
            @RequestParam(defaultValue = "10") int limit) {

        List<HealthCheckResponse> history = healthService.getHistory(url, limit);
        return ResponseEntity.ok(history);
    }

    /**
     * Obtiene estadísticas agregadas de un endpoint
     * 
     * @param url URL del endpoint
     * @return Estadísticas de disponibilidad y rendimiento
     */
    @GetMapping("/stats")
    @Operation(summary = "Estadísticas de endpoint", description = "Métricas agregadas de disponibilidad y latencia")
    public ResponseEntity<?> getEndpointStats(@RequestParam String url) {
        var stats = healthService.getEndpointStatistics(url);
        return ResponseEntity.ok(stats);
    }

    /**
     * Registra un endpoint para monitoreo continuo
     * 
     * @param request Configuración del endpoint a monitorear
     * @return Confirmación de registro
     */
    @PostMapping("/monitor/register")
    @Operation(summary = "Registrar endpoint para monitoreo", description = "Agrega un endpoint al sistema de monitoreo continuo")
    public ResponseEntity<String> registerEndpoint(
            @Valid @RequestBody HealthCheckRequest request) {

        healthService.registerForMonitoring(request);
        return ResponseEntity.ok("Endpoint registrado para monitoreo continuo");
    }

    /**
     * Lista todos los endpoints monitoreados
     * 
     * @return Lista de endpoints en monitoreo
     */
    @GetMapping("/monitor/list")
    @Operation(summary = "Listar endpoints monitoreados", description = "Obtiene todos los endpoints en monitoreo activo")
    public ResponseEntity<?> listMonitoredEndpoints() {
        var endpoints = healthService.getMonitoredEndpoints();
        return ResponseEntity.ok(endpoints);
    }
}