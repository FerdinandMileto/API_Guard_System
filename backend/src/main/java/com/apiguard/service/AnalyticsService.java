package com.apiguard.service;

import com.apiguard.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Servicio de Analytics y Data Science
 * 
 * Implementa análisis estadístico de métricas de APIs
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AnalyticsService {

    private final HealthMonitorService healthMonitorService;

    /**
     * Calcula estadísticas descriptivas de latencia
     */
    public LatencyStatistics calculateLatencyStats(String endpoint) {
        log.info("Calculando estadísticas de latencia para: {}", endpoint);

        List<HealthCheckResponse> history = healthMonitorService.getHistory(endpoint, 100);

        if (history.isEmpty()) {
            return LatencyStatistics.builder()
                    .endpoint(endpoint)
                    .totalSamples(0L)
                    .build();
        }

        List<Long> latencies = history.stream()
                .filter(h -> h.getResponseTimeMs() != null)
                .map(HealthCheckResponse::getResponseTimeMs)
                .sorted()
                .collect(Collectors.toList());

        if (latencies.isEmpty()) {
            return LatencyStatistics.builder()
                    .endpoint(endpoint)
                    .totalSamples(0L)
                    .build();
        }

        // Estadísticas básicas
        double mean = calculateMean(latencies);
        double median = calculatePercentile(latencies, 50);
        double stdDev = calculateStandardDeviation(latencies, mean);
        double variance = stdDev * stdDev;

        // Percentiles (SLA metrics)
        double p50 = calculatePercentile(latencies, 50);
        double p90 = calculatePercentile(latencies, 90);
        double p95 = calculatePercentile(latencies, 95);
        double p99 = calculatePercentile(latencies, 99);

        // Min/Max
        long min = latencies.get(0);
        long max = latencies.get(latencies.size() - 1);

        // Calificar rendimiento basado en P95
        String grade = calculatePerformanceGrade(p95);

        return LatencyStatistics.builder()
                .endpoint(endpoint)
                .periodStart(history.get(0).getTimestamp())
                .periodEnd(history.get(history.size() - 1).getTimestamp())
                .mean(round(mean, 2))
                .median(round(median, 2))
                .standardDeviation(round(stdDev, 2))
                .variance(round(variance, 2))
                .p50(round(p50, 2))
                .p90(round(p90, 2))
                .p95(round(p95, 2))
                .p99(round(p99, 2))
                .minLatency(min)
                .maxLatency(max)
                .totalSamples((long) latencies.size())
                .performanceGrade(grade)
                .build();
    }

    /**
     * Analiza distribución de errores
     */
    public ErrorDistribution analyzeErrorDistribution(String period) {
        log.info("Analizando distribución de errores: {}", period);

        // Obtener todos los endpoints monitoreados
        Map<String, HealthCheckRequest> monitored = healthMonitorService.getMonitoredEndpoints();

        List<HealthCheckResponse> allHistory = new ArrayList<>();
        for (String endpoint : monitored.keySet()) {
            allHistory.addAll(healthMonitorService.getHistory(endpoint, 100));
        }

        long totalRequests = allHistory.size();
        long totalErrors = allHistory.stream()
                .filter(h -> !"UP".equals(h.getStatus()))
                .count();

        double errorRate = totalRequests > 0 ? (totalErrors * 100.0 / totalRequests) : 0.0;

        // Agrupar errores por código de estado
        Map<Integer, Long> errorCounts = allHistory.stream()
                .filter(h -> h.getStatusCode() != null && h.getStatusCode() >= 400)
                .collect(Collectors.groupingBy(
                        HealthCheckResponse::getStatusCode,
                        Collectors.counting()));

        // Top 5 errores
        List<ErrorDistribution.ErrorFrequency> topErrors = errorCounts.entrySet().stream()
                .sorted(Map.Entry.<Integer, Long>comparingByValue().reversed())
                .limit(5)
                .map(entry -> ErrorDistribution.ErrorFrequency.builder()
                        .statusCode(entry.getKey())
                        .errorMessage(getErrorMessage(entry.getKey()))
                        .count(entry.getValue())
                        .percentage(round((entry.getValue() * 100.0 / totalErrors), 2))
                        .affectedEndpoints(getAffectedEndpoints(allHistory, entry.getKey()))
                        .build())
                .collect(Collectors.toList());

        // Errores por categoría
        Map<String, Long> byCategory = new HashMap<>();
        byCategory.put("4xx_Client_Errors", errorCounts.entrySet().stream()
                .filter(e -> e.getKey() >= 400 && e.getKey() < 500)
                .mapToLong(Map.Entry::getValue)
                .sum());
        byCategory.put("5xx_Server_Errors", errorCounts.entrySet().stream()
                .filter(e -> e.getKey() >= 500)
                .mapToLong(Map.Entry::getValue)
                .sum());

        return ErrorDistribution.builder()
                .period(period)
                .totalRequests(totalRequests)
                .totalErrors(totalErrors)
                .errorRate(round(errorRate, 2))
                .topErrors(topErrors)
                .errorsByCategory(byCategory)
                .trend("STABLE")
                .build();
    }

    /**
     * Detecta anomalías usando Z-score
     */
    public AnomalyReport detectAnomalies(String endpoint, double threshold) {
        log.info("Detectando anomalías en {}, threshold: {}", endpoint, threshold);

        List<HealthCheckResponse> history = healthMonitorService.getHistory(endpoint, 50);

        if (history.size() < 10) {
            return AnomalyReport.builder()
                    .endpoint(endpoint)
                    .detectionTime(LocalDateTime.now())
                    .anomalyType("INSUFFICIENT_DATA")
                    .anomalies(new ArrayList<>())
                    .severity("LOW")
                    .recommendation("Necesita más datos para análisis (mínimo 10 muestras)")
                    .build();
        }

        List<Long> latencies = history.stream()
                .filter(h -> h.getResponseTimeMs() != null)
                .map(HealthCheckResponse::getResponseTimeMs)
                .collect(Collectors.toList());

        double mean = calculateMean(latencies);
        double stdDev = calculateStandardDeviation(latencies, mean);

        List<AnomalyReport.AnomalyEvent> anomalies = new ArrayList<>();

        for (int i = 0; i < history.size(); i++) {
            HealthCheckResponse check = history.get(i);
            if (check.getResponseTimeMs() == null)
                continue;

            double zScore = (check.getResponseTimeMs() - mean) / stdDev;

            if (Math.abs(zScore) > threshold) {
                anomalies.add(AnomalyReport.AnomalyEvent.builder()
                        .timestamp(check.getTimestamp())
                        .value((double) check.getResponseTimeMs())
                        .expectedValue(round(mean, 2))
                        .zScore(round(zScore, 2))
                        .description(String.format("Latencia anómala: %dms (esperado: %.0fms, Z-score: %.2f)",
                                check.getResponseTimeMs(), mean, zScore))
                        .build());
            }
        }

        String severity = anomalies.size() > 5 ? "HIGH" : anomalies.size() > 2 ? "MEDIUM" : "LOW";

        return AnomalyReport.builder()
                .endpoint(endpoint)
                .detectionTime(LocalDateTime.now())
                .anomalyType("LATENCY_SPIKE")
                .anomalies(anomalies)
                .severity(severity)
                .recommendation(generateRecommendation(anomalies.size(), severity))
                .build();
    }

    /**
     * Genera resumen ejecutivo de rendimiento
     */
    public PerformanceSummary generatePerformanceSummary() {
        log.info("Generando resumen de rendimiento");

        Map<String, HealthCheckRequest> monitored = healthMonitorService.getMonitoredEndpoints();

        List<PerformanceSummary.EndpointPerformance> allEndpoints = new ArrayList<>();
        List<Long> allLatencies = new ArrayList<>();
        long totalRequests = 0;
        double totalUptime = 0;

        for (String endpoint : monitored.keySet()) {
            List<HealthCheckResponse> history = healthMonitorService.getHistory(endpoint, 100);
            if (history.isEmpty())
                continue;

            List<Long> latencies = history.stream()
                    .filter(h -> h.getResponseTimeMs() != null)
                    .map(HealthCheckResponse::getResponseTimeMs)
                    .collect(Collectors.toList());

            if (!latencies.isEmpty()) {
                allLatencies.addAll(latencies);
                totalRequests += latencies.size();

                double avg = calculateMean(latencies);
                double p95 = calculatePercentile(latencies, 95);

                EndpointStatistics stats = healthMonitorService.getEndpointStatistics(endpoint);
                totalUptime += stats.getUptimePercentage();

                allEndpoints.add(PerformanceSummary.EndpointPerformance.builder()
                        .endpoint(endpoint)
                        .avgLatency(round(avg, 2))
                        .p95Latency(round(p95, 2))
                        .requestCount((long) latencies.size())
                        .grade(calculatePerformanceGrade(p95))
                        .build());
            }
        }

        allLatencies.sort(Long::compareTo);

        double globalP95 = allLatencies.isEmpty() ? 0 : calculatePercentile(allLatencies, 95);
        double globalP99 = allLatencies.isEmpty() ? 0 : calculatePercentile(allLatencies, 99);
        boolean meetsSLA = globalP95 < 1000; // SLA: P95 < 1s

        // Top 5 más lentos y más rápidos
        List<PerformanceSummary.EndpointPerformance> topSlowest = allEndpoints.stream()
                .sorted(Comparator.comparingDouble(PerformanceSummary.EndpointPerformance::getP95Latency).reversed())
                .limit(5)
                .collect(Collectors.toList());

        List<PerformanceSummary.EndpointPerformance> topFastest = allEndpoints.stream()
                .sorted(Comparator.comparingDouble(PerformanceSummary.EndpointPerformance::getP95Latency))
                .limit(5)
                .collect(Collectors.toList());

        // Alertas
        List<String> alerts = new ArrayList<>();
        if (!meetsSLA) {
            alerts.add("⚠️ SLA no cumplido: P95 = " + round(globalP95, 0) + "ms (objetivo: <1000ms)");
        }
        if (totalUptime / monitored.size() < 99.0) {
            alerts.add("⚠️ Uptime bajo: " + round(totalUptime / monitored.size(), 2) + "% (objetivo: >99%)");
        }

        return PerformanceSummary.builder()
                .generatedAt(LocalDateTime.now())
                .period("last_100_checks")
                .totalEndpoints((long) monitored.size())
                .totalRequests(totalRequests)
                .averageLatency(allLatencies.isEmpty() ? 0 : round(calculateMean(allLatencies), 2))
                .uptimePercentage(monitored.isEmpty() ? 0 : round(totalUptime / monitored.size(), 2))
                .p95Latency(round(globalP95, 2))
                .p99Latency(round(globalP99, 2))
                .meetsSLA(meetsSLA)
                .topSlowestEndpoints(topSlowest)
                .topFastestEndpoints(topFastest)
                .alerts(alerts)
                .build();
    }

    // ========== MÉTODOS AUXILIARES ==========

    private double calculateMean(List<Long> values) {
        return values.stream().mapToLong(Long::longValue).average().orElse(0.0);
    }

    private double calculatePercentile(List<Long> sortedValues, int percentile) {
        if (sortedValues.isEmpty())
            return 0.0;
        int index = (int) Math.ceil(percentile / 100.0 * sortedValues.size()) - 1;
        index = Math.max(0, Math.min(index, sortedValues.size() - 1));
        return sortedValues.get(index).doubleValue();
    }

    private double calculateStandardDeviation(List<Long> values, double mean) {
        double variance = values.stream()
                .mapToDouble(v -> Math.pow(v - mean, 2))
                .average()
                .orElse(0.0);
        return Math.sqrt(variance);
    }

    private double round(double value, int decimals) {
        double scale = Math.pow(10, decimals);
        return Math.round(value * scale) / scale;
    }

    private String calculatePerformanceGrade(double p95) {
        if (p95 < 200)
            return "A"; // Excelente
        if (p95 < 500)
            return "B"; // Bueno
        if (p95 < 1000)
            return "C"; // Aceptable
        if (p95 < 2000)
            return "D"; // Malo
        return "F"; // Crítico
    }

    private String getErrorMessage(int statusCode) {
        return switch (statusCode) {
            case 400 -> "Bad Request";
            case 401 -> "Unauthorized";
            case 403 -> "Forbidden";
            case 404 -> "Not Found";
            case 429 -> "Too Many Requests";
            case 500 -> "Internal Server Error";
            case 502 -> "Bad Gateway";
            case 503 -> "Service Unavailable";
            case 504 -> "Gateway Timeout";
            default -> "Error " + statusCode;
        };
    }

    private List<String> getAffectedEndpoints(List<HealthCheckResponse> history, int statusCode) {
        return history.stream()
                .filter(h -> h.getStatusCode() != null && h.getStatusCode() == statusCode)
                .map(HealthCheckResponse::getUrl)
                .distinct()
                .limit(5)
                .collect(Collectors.toList());
    }

    private String generateRecommendation(int anomalyCount, String severity) {
        if (anomalyCount == 0) {
            return "No se detectaron anomalías. Rendimiento estable.";
        }
        if ("HIGH".equals(severity)) {
            return "Se detectaron múltiples anomalías. Revisar inmediatamente: logs del servidor, recursos (CPU/memoria), red.";
        }
        return "Se detectaron algunas anomalías. Monitorear de cerca y verificar si persisten.";
    }
}