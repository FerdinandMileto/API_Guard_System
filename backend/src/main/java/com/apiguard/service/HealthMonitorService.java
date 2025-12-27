package com.apiguard.service;

import com.apiguard.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Servicio de monitoreo de salud de APIs
 * 
 * Implementa lógica para:
 * - Health checks síncronos y asíncronos
 * - Almacenamiento de historial
 * - Cálculo de estadísticas
 * - Monitoreo continuo programado
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class HealthMonitorService {

    // Storage en memoria (en producción usar BD)
    private final Map<String, List<HealthCheckResponse>> healthHistory = new ConcurrentHashMap<>();
    private final Map<String, HealthCheckRequest> monitoredEndpoints = new ConcurrentHashMap<>();

    /**
     * Realiza un health check simple de un endpoint
     */
    public HealthCheckResponse performHealthCheck(String url) {
        log.info("Iniciando health check para: {}", url);

        long startTime = System.currentTimeMillis();
        HealthCheckResponse response = HealthCheckResponse.builder()
                .url(url)
                .timestamp(LocalDateTime.now())
                .build();

        try {
            URL endpoint = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) endpoint.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.setInstanceFollowRedirects(true);

            int statusCode = connection.getResponseCode();
            long responseTime = System.currentTimeMillis() - startTime;

            response.setStatusCode(statusCode);
            response.setResponseTimeMs(responseTime);

            // Determinar estado
            if (statusCode >= 200 && statusCode < 300) {
                response.setStatus("UP");
                response.setMessage("Endpoint respondió correctamente");
            } else if (statusCode >= 300 && statusCode < 400) {
                response.setStatus("DEGRADED");
                response.setMessage("Endpoint redirige - verificar configuración");
                response.setIssues(List.of("Redirección detectada - puede impactar rendimiento"));
            } else if (statusCode >= 400 && statusCode < 500) {
                response.setStatus("DOWN");
                response.setMessage("Error del cliente - verificar request");
                response.setIssues(List.of("Error 4xx - problema en la configuración del cliente"));
            } else {
                response.setStatus("DOWN");
                response.setMessage("Error del servidor");
                response.setIssues(List.of("Error 5xx - problema en el servidor destino"));
            }

            // Análisis de rendimiento
            List<String> recommendations = new ArrayList<>();
            if (responseTime > 3000) {
                recommendations.add("Tiempo de respuesta elevado (>3s) - considerar optimización");
            } else if (responseTime > 1000) {
                recommendations.add("Tiempo de respuesta aceptable pero mejorable");
            }
            response.setRecommendations(recommendations);

            // Metadata adicional
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("contentType", connection.getContentType());
            metadata.put("contentLength", connection.getContentLength());
            metadata.put("server", connection.getHeaderField("Server"));
            response.setMetadata(metadata);

            connection.disconnect();

        } catch (Exception e) {
            log.error("Error en health check de {}: {}", url, e.getMessage());
            response.setStatus("DOWN");
            response.setMessage("Error al conectar: " + e.getMessage());
            response.setResponseTimeMs(System.currentTimeMillis() - startTime);
            response.setIssues(List.of(
                    "No se pudo establecer conexión",
                    "Verificar: URL, red, firewall, DNS"));
        }

        // Guardar en historial
        saveToHistory(url, response);

        return response;
    }

    /**
     * Health check avanzado con configuración personalizada
     */
    public HealthCheckResponse performAdvancedCheck(HealthCheckRequest request) {
        log.info("Health check avanzado para: {}", request.getUrl());

        long startTime = System.currentTimeMillis();
        HealthCheckResponse response = HealthCheckResponse.builder()
                .url(request.getUrl())
                .timestamp(LocalDateTime.now())
                .build();

        try {
            URL endpoint = new URL(request.getUrl());
            HttpURLConnection connection = (HttpURLConnection) endpoint.openConnection();

            // Configurar método
            connection.setRequestMethod(request.getMethod());

            // Configurar timeouts
            int timeout = request.getTimeoutSeconds() * 1000;
            connection.setConnectTimeout(timeout);
            connection.setReadTimeout(timeout);

            // Configurar redirects
            connection.setInstanceFollowRedirects(request.getFollowRedirects());

            // Agregar headers personalizados
            if (request.getHeaders() != null) {
                request.getHeaders().forEach(connection::setRequestProperty);
            }

            // Agregar body si existe
            if (request.getBody() != null && !request.getBody().isEmpty()) {
                connection.setDoOutput(true);
                connection.getOutputStream().write(request.getBody().getBytes());
            }

            int statusCode = connection.getResponseCode();
            long responseTime = System.currentTimeMillis() - startTime;

            response.setStatusCode(statusCode);
            response.setResponseTimeMs(responseTime);

            if (statusCode >= 200 && statusCode < 300) {
                response.setStatus("UP");
                response.setMessage("Endpoint saludable");
            } else {
                response.setStatus("DOWN");
                response.setMessage("Código de estado inesperado: " + statusCode);
            }

            connection.disconnect();

        } catch (Exception e) {
            log.error("Error en health check avanzado: {}", e.getMessage());
            response.setStatus("DOWN");
            response.setMessage(e.getMessage());
            response.setResponseTimeMs(System.currentTimeMillis() - startTime);
        }

        saveToHistory(request.getUrl(), response);
        return response;
    }

    /**
     * Obtiene el historial de health checks
     */
    public List<HealthCheckResponse> getHistory(String url, int limit) {
        List<HealthCheckResponse> history = healthHistory.getOrDefault(url, new ArrayList<>());
        return history.stream()
                .sorted((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()))
                .limit(limit)
                .toList();
    }

    /**
     * Calcula estadísticas de un endpoint
     */
    @Cacheable(value = "endpointStats", key = "#url")
    public EndpointStatistics getEndpointStatistics(String url) {
        List<HealthCheckResponse> history = healthHistory.getOrDefault(url, new ArrayList<>());

        if (history.isEmpty()) {
            return EndpointStatistics.builder()
                    .endpoint(url)
                    .totalChecks(0L)
                    .uptimePercentage(0.0)
                    .build();
        }

        long total = history.size();
        long successful = history.stream()
                .filter(h -> "UP".equals(h.getStatus()))
                .count();

        double uptime = (successful * 100.0) / total;

        DoubleSummaryStatistics responseTimeStats = history.stream()
                .filter(h -> h.getResponseTimeMs() != null)
                .mapToDouble(HealthCheckResponse::getResponseTimeMs)
                .summaryStatistics();

        return EndpointStatistics.builder()
                .endpoint(url)
                .totalChecks(total)
                .successfulChecks(successful)
                .failedChecks(total - successful)
                .uptimePercentage(Math.round(uptime * 100.0) / 100.0)
                .avgResponseTimeMs(responseTimeStats.getAverage())
                .minResponseTimeMs((long) responseTimeStats.getMin())
                .maxResponseTimeMs((long) responseTimeStats.getMax())
                .lastCheckTime(history.get(history.size() - 1).getTimestamp())
                .build();
    }

    /**
     * Registra un endpoint para monitoreo continuo
     */
    public void registerForMonitoring(HealthCheckRequest request) {
        monitoredEndpoints.put(request.getUrl(), request);
        log.info("Endpoint registrado para monitoreo: {}", request.getUrl());
    }

    /**
     * Obtiene lista de endpoints monitoreados
     */
    public Map<String, HealthCheckRequest> getMonitoredEndpoints() {
        return new HashMap<>(monitoredEndpoints);
    }

    /**
     * Monitoreo automático cada minuto
     */
    @Scheduled(fixedRate = 60000)
    @Async
    public void scheduledMonitoring() {
        if (monitoredEndpoints.isEmpty()) {
            return;
        }

        log.info("Ejecutando monitoreo programado de {} endpoints", monitoredEndpoints.size());

        monitoredEndpoints.values().forEach(request -> {
            try {
                performAdvancedCheck(request);
            } catch (Exception e) {
                log.error("Error en monitoreo programado de {}: {}",
                        request.getUrl(), e.getMessage());
            }
        });
    }

    /**
     * Guarda resultado en historial
     */
    private void saveToHistory(String url, HealthCheckResponse response) {
        healthHistory.computeIfAbsent(url, k -> new ArrayList<>()).add(response);

        // Mantener solo últimos 100 registros por endpoint
        List<HealthCheckResponse> history = healthHistory.get(url);
        if (history.size() > 100) {
            history.remove(0);
        }
    }
}