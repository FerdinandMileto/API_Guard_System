package com.apiguard.service;

import com.apiguard.dto.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Servicio de diagnóstico automatizado de APIs
 * 
 * Analiza problemas comunes y proporciona soluciones
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class DiagnosticService {

    private final ObjectMapper objectMapper;

    /**
     * Realiza diagnóstico completo de una integración
     */
    public DiagnosticReport performFullDiagnosis(DiagnosticRequest request) {
        log.info("Iniciando diagnóstico completo de: {}", request.getEndpoint());

        String diagnosticId = UUID.randomUUID().toString();

        DiagnosticReport report = DiagnosticReport.builder()
                .diagnosticId(diagnosticId)
                .timestamp(LocalDateTime.now())
                .endpoint(request.getEndpoint())
                .issues(new ArrayList<>())
                .recommendations(new ArrayList<>())
                .build();

        try {
            // 1. Análisis de autenticación
            AuthenticationAnalysis authAnalysis = analyzeAuthentication(request);
            report.setAuthAnalysis(authAnalysis);
            if (!authAnalysis.getIsValid()) {
                addIssue(report, "HIGH", "AUTH",
                        "Problema de autenticación detectado",
                        "Las solicitudes no serán autorizadas");
            }

            // 2. Análisis de payload
            if (request.getRequestBody() != null) {
                PayloadAnalysis payloadAnalysis = analyzePayload(request.getRequestBody());
                report.setPayloadAnalysis(payloadAnalysis);
                if (!payloadAnalysis.getIsValidJson()) {
                    addIssue(report, "HIGH", "PAYLOAD",
                            "Formato JSON inválido",
                            "El servidor rechazará la solicitud");
                }
            }

            // 3. Ejecutar llamada real y analizar respuesta
            ResponseAnalysis responseAnalysis = executeAndAnalyze(request);
            report.setResponseAnalysis(responseAnalysis);

            if (!responseAnalysis.getIsExpectedStatus()) {
                String severity = responseAnalysis.getStatusCode() >= 500 ? "HIGH" : "MEDIUM";
                addIssue(report, severity, "RESPONSE",
                        "Código de estado inesperado: " + responseAnalysis.getStatusCode(),
                        getHttpErrorImpact(responseAnalysis.getStatusCode()));
            }

            // 4. Análisis de rendimiento
            PerformanceAnalysis perfAnalysis = analyzePerformance(request);
            report.setPerformanceAnalysis(perfAnalysis);

            if ("SLOW".equals(perfAnalysis.getPerformanceRating())) {
                addIssue(report, "MEDIUM", "PERFORMANCE",
                        "Tiempo de respuesta elevado",
                        "Puede afectar experiencia del usuario");
            }

            // 5. Determinar estado general
            String overallStatus = determineOverallStatus(report);
            report.setOverallStatus(overallStatus);

            // 6. Generar recomendaciones
            generateRecommendations(report);

            // 7. Enlazar documentación relevante
            report.setKnowledgeBaseLink("/api/knowledge/troubleshooting");

        } catch (Exception e) {
            log.error("Error en diagnóstico: {}", e.getMessage(), e);
            report.setOverallStatus("CRITICAL");
            addIssue(report, "HIGH", "SYSTEM",
                    "Error inesperado en diagnóstico: " + e.getMessage(),
                    "No se pudo completar el análisis");
        }

        return report;
    }

    /**
     * Analiza autenticación
     */
    private AuthenticationAnalysis analyzeAuthentication(DiagnosticRequest request) {
        AuthenticationAnalysis analysis = AuthenticationAnalysis.builder()
                .problems(new ArrayList<>())
                .build();

        String authType = request.getAuthenticationType();
        String authValue = request.getAuthenticationValue();

        if (authType == null || "None".equals(authType)) {
            analysis.setIsValid(true);
            analysis.setType("None");
            analysis.setMessage("Sin autenticación configurada");
            return analysis;
        }

        analysis.setType(authType);

        switch (authType) {
            case "Bearer":
                if (authValue == null || authValue.trim().isEmpty()) {
                    analysis.setIsValid(false);
                    analysis.getProblems().add("Token Bearer vacío");
                    analysis.setCorrectFormat("Authorization: Bearer YOUR_TOKEN_HERE");
                } else if (!authValue.startsWith("Bearer ")) {
                    analysis.setIsValid(false);
                    analysis.getProblems().add("Falta prefijo 'Bearer '");
                    analysis.setCorrectFormat("Authorization: Bearer " + authValue);
                } else {
                    analysis.setIsValid(true);
                    analysis.setMessage("Formato Bearer correcto");
                }
                break;

            case "Basic":
                if (authValue == null || !authValue.contains(":")) {
                    analysis.setIsValid(false);
                    analysis.getProblems().add("Formato Basic inválido");
                    analysis.setCorrectFormat("Authorization: Basic base64(username:password)");
                } else {
                    analysis.setIsValid(true);
                    analysis.setMessage("Formato Basic correcto");
                }
                break;

            case "ApiKey":
                if (authValue == null || authValue.length() < 16) {
                    analysis.setIsValid(false);
                    analysis.getProblems().add("API Key muy corta o vacía");
                } else {
                    analysis.setIsValid(true);
                    analysis.setMessage("API Key presente");
                }
                break;

            default:
                analysis.setIsValid(false);
                analysis.getProblems().add("Tipo de autenticación no reconocido");
        }

        return analysis;
    }

    /**
     * Analiza estructura del payload
     */
    private PayloadAnalysis analyzePayload(String payload) {
        PayloadAnalysis analysis = PayloadAnalysis.builder()
                .missingFields(new ArrayList<>())
                .unexpectedFields(new ArrayList<>())
                .formatErrors(new ArrayList<>())
                .build();

        try {
            objectMapper.readTree(payload);
            analysis.setIsValidJson(true);
            analysis.setSizeBytes(payload.getBytes().length);
        } catch (Exception e) {
            analysis.setIsValidJson(false);
            analysis.getFormatErrors().add("JSON inválido: " + e.getMessage());
        }

        return analysis;
    }

    /**
     * Ejecuta la llamada y analiza respuesta
     */
    private ResponseAnalysis executeAndAnalyze(DiagnosticRequest request) {
        ResponseAnalysis analysis = ResponseAnalysis.builder()
                .responseHeaders(new HashMap<>())
                .build();

        try {
            URL url = new URL(request.getEndpoint());
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod(request.getMethod());
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);

            // Agregar headers
            if (request.getHeaders() != null) {
                request.getHeaders().forEach(conn::setRequestProperty);
            }

            // Agregar autenticación
            if (request.getAuthenticationValue() != null) {
                conn.setRequestProperty("Authorization", request.getAuthenticationValue());
            }

            int statusCode = conn.getResponseCode();
            analysis.setStatusCode(statusCode);
            analysis.setContentType(conn.getContentType());
            analysis.setContentLength(conn.getContentLength());

            // Categorizar status
            if (statusCode >= 200 && statusCode < 300) {
                analysis.setStatusCategory("SUCCESS");
            } else if (statusCode >= 400 && statusCode < 500) {
                analysis.setStatusCategory("CLIENT_ERROR");
            } else if (statusCode >= 500) {
                analysis.setStatusCategory("SERVER_ERROR");
            }

            // Verificar si es el código esperado
            boolean isExpected = request.getExpectedStatusCode() == null ||
                    statusCode == request.getExpectedStatusCode();
            analysis.setIsExpectedStatus(isExpected);

            conn.disconnect();

        } catch (Exception e) {
            log.error("Error ejecutando request: {}", e.getMessage());
            analysis.setStatusCode(0);
            analysis.setStatusCategory("ERROR");
            analysis.setErrorMessage(e.getMessage());
            analysis.setIsExpectedStatus(false);
        }

        return analysis;
    }

    /**
     * Analiza rendimiento
     */
    private PerformanceAnalysis analyzePerformance(DiagnosticRequest request) {
        long startTime = System.currentTimeMillis();

        try {
            URL url = new URL(request.getEndpoint());
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("HEAD");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            conn.getResponseCode();
            long responseTime = System.currentTimeMillis() - startTime;

            String rating;
            if (responseTime < 100)
                rating = "EXCELLENT";
            else if (responseTime < 500)
                rating = "GOOD";
            else if (responseTime < 1000)
                rating = "ACCEPTABLE";
            else if (responseTime < 3000)
                rating = "SLOW";
            else
                rating = "TIMEOUT";

            conn.disconnect();

            return PerformanceAnalysis.builder()
                    .responseTimeMs(responseTime)
                    .performanceRating(rating)
                    .build();

        } catch (Exception e) {
            return PerformanceAnalysis.builder()
                    .responseTimeMs(System.currentTimeMillis() - startTime)
                    .performanceRating("TIMEOUT")
                    .build();
        }
    }

    /**
     * Valida formato de autenticación
     */
    public Map<String, Object> validateAuthentication(String authHeader, String authType) {
        Map<String, Object> result = new HashMap<>();

        boolean isValid = false;
        List<String> issues = new ArrayList<>();
        String correctFormat = "";

        switch (authType) {
            case "Bearer":
                isValid = authHeader != null && authHeader.startsWith("Bearer ");
                if (!isValid) {
                    issues.add("Debe comenzar con 'Bearer '");
                    correctFormat = "Bearer YOUR_TOKEN_HERE";
                }
                break;
            case "Basic":
                isValid = authHeader != null && authHeader.startsWith("Basic ");
                if (!isValid) {
                    issues.add("Debe comenzar con 'Basic '");
                    correctFormat = "Basic base64(username:password)";
                }
                break;
        }

        result.put("isValid", isValid);
        result.put("issues", issues);
        result.put("correctFormat", correctFormat);

        return result;
    }

    /**
     * Analiza errores HTTP comunes
     */
    public Map<String, Object> analyzeHttpError(int statusCode, String endpoint) {
        Map<String, Object> analysis = new HashMap<>();

        String explanation = "";
        List<String> solutions = new ArrayList<>();
        String kbLink = "";

        switch (statusCode) {
            case 400:
                explanation = "Bad Request - El servidor no puede procesar la solicitud";
                solutions.add("Verificar formato del payload JSON");
                solutions.add("Validar que todos los campos requeridos estén presentes");
                solutions.add("Revisar tipos de datos de los campos");
                kbLink = "/kb/http-400-bad-request";
                break;
            case 401:
                explanation = "Unauthorized - Falta autenticación o es inválida";
                solutions.add("Verificar que el token/credenciales sean correctos");
                solutions.add("Confirmar que el header Authorization esté presente");
                solutions.add("Validar que el token no haya expirado");
                kbLink = "/kb/http-401-unauthorized";
                break;
            case 403:
                explanation = "Forbidden - No tienes permisos para este recurso";
                solutions.add("Verificar permisos del usuario/aplicación");
                solutions.add("Confirmar que la API Key tenga los scopes necesarios");
                solutions.add("Contactar al administrador para solicitar acceso");
                kbLink = "/kb/http-403-forbidden";
                break;
            case 404:
                explanation = "Not Found - El endpoint no existe";
                solutions.add("Verificar la URL del endpoint");
                solutions.add("Confirmar la versión de la API (v1, v2, etc.)");
                solutions.add("Revisar documentación de la API");
                kbLink = "/kb/http-404-not-found";
                break;
            case 500:
                explanation = "Internal Server Error - Error en el servidor destino";
                solutions.add("Reportar el error al equipo de soporte");
                solutions.add("Incluir detalles de la solicitud en el reporte");
                solutions.add("Intentar nuevamente más tarde");
                kbLink = "/kb/http-500-server-error";
                break;
            default:
                explanation = "Código de estado HTTP: " + statusCode;
                solutions.add("Consultar documentación de la API");
        }

        analysis.put("statusCode", statusCode);
        analysis.put("explanation", explanation);
        analysis.put("solutions", solutions);
        analysis.put("knowledgeBaseLink", kbLink);
        analysis.put("endpoint", endpoint);

        return analysis;
    }

    /**
     * Valida payload JSON
     */
    public Map<String, Object> validateJsonPayload(String payload, String schema) {
        Map<String, Object> validation = new HashMap<>();
        List<String> errors = new ArrayList<>();

        try {
            objectMapper.readTree(payload);
            validation.put("isValid", true);
            validation.put("size", payload.getBytes().length);
        } catch (Exception e) {
            validation.put("isValid", false);
            errors.add("JSON inválido: " + e.getMessage());
        }

        validation.put("errors", errors);
        return validation;
    }

    /**
     * Simula llamada API
     */
    public Map<String, Object> simulateApiCall(DiagnosticRequest request) {
        Map<String, Object> simulation = new HashMap<>();
        List<String> logs = new ArrayList<>();

        logs.add("=== SIMULACIÓN DE LLAMADA API ===");
        logs.add("Endpoint: " + request.getEndpoint());
        logs.add("Método: " + request.getMethod());

        if (request.getHeaders() != null) {
            logs.add("Headers:");
            request.getHeaders().forEach((k, v) -> logs.add("  " + k + ": " + v));
        }

        try {
            long start = System.currentTimeMillis();
            URL url = new URL(request.getEndpoint());
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod(request.getMethod());

            int statusCode = conn.getResponseCode();
            long duration = System.currentTimeMillis() - start;

            logs.add("Status Code: " + statusCode);
            logs.add("Tiempo de respuesta: " + duration + "ms");
            logs.add("Content-Type: " + conn.getContentType());

            simulation.put("success", statusCode >= 200 && statusCode < 300);
            simulation.put("statusCode", statusCode);
            simulation.put("responseTime", duration);

            conn.disconnect();

        } catch (Exception e) {
            logs.add("ERROR: " + e.getMessage());
            simulation.put("success", false);
            simulation.put("error", e.getMessage());
        }

        simulation.put("logs", logs);
        return simulation;
    }

    /**
     * Genera código de ejemplo para integración
     */
    public Map<String, String> generateIntegrationCode(
            String language, String endpoint, String method, String authType) {

        Map<String, String> codeMap = new HashMap<>();
        String code = "";

        switch (language.toLowerCase()) {
            case "java":
                code = generateJavaCode(endpoint, method, authType);
                break;
            case "python":
                code = generatePythonCode(endpoint, method, authType);
                break;
            case "php":
                code = generatePhpCode(endpoint, method, authType);
                break;
            case "javascript":
                code = generateJavaScriptCode(endpoint, method, authType);
                break;
            default:
                code = "// Lenguaje no soportado";
        }

        codeMap.put("language", language);
        codeMap.put("code", code);
        codeMap.put("description", "Ejemplo de integración en " + language);

        return codeMap;
    }

    private String generateJavaCode(String endpoint, String method, String authType) {
        return String.format("""
                // Java - Ejemplo de integración
                import java.net.http.*;
                import java.net.URI;

                public class ApiClient {
                    public void callApi() throws Exception {
                        HttpClient client = HttpClient.newHttpClient();

                        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                            .uri(URI.create("%s"))
                            .%s();

                        %s

                        HttpResponse<String> response = client.send(
                            requestBuilder.build(),
                            HttpResponse.BodyHandlers.ofString()
                        );

                        System.out.println("Status: " + response.statusCode());
                        System.out.println("Body: " + response.body());
                    }
                }
                """,
                endpoint,
                method.toLowerCase(),
                authType != null && authType.equals("Bearer")
                        ? "requestBuilder.header(\"Authorization\", \"Bearer YOUR_TOKEN\");"
                        : "");
    }

    private String generatePythonCode(String endpoint, String method, String authType) {
        return String.format("""
                # Python - Ejemplo de integración
                import requests

                url = "%s"
                headers = {
                    "Content-Type": "application/json"%s
                }

                response = requests.%s(url, headers=headers)

                print(f"Status: {response.status_code}")
                print(f"Body: {response.text}")
                """,
                endpoint,
                authType != null && authType.equals("Bearer")
                        ? ",\n    \"Authorization\": \"Bearer YOUR_TOKEN\""
                        : "",
                method.toLowerCase());
    }

    private String generatePhpCode(String endpoint, String method, String authType) {
        return String.format("""
                <?php
                // PHP - Ejemplo de integración
                $url = "%s";

                $headers = [
                    "Content-Type: application/json"%s
                ];

                $ch = curl_init($url);
                curl_setopt($ch, CURLOPT_HTTPHEADER, $headers);
                curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);

                $response = curl_exec($ch);
                $statusCode = curl_getinfo($ch, CURLINFO_HTTP_CODE);

                curl_close($ch);

                echo "Status: $statusCode\\n";
                echo "Body: $response\\n";
                ?>
                """,
                endpoint,
                authType != null && authType.equals("Bearer")
                        ? ",\n    \"Authorization: Bearer YOUR_TOKEN\""
                        : "");
    }

    private String generateJavaScriptCode(String endpoint, String method, String authType) {
        return String.format("""
                // JavaScript - Ejemplo de integración
                const url = "%s";

                const options = {
                    method: "%s",
                    headers: {
                        "Content-Type": "application/json"%s
                    }
                };

                fetch(url, options)
                    .then(response => response.json())
                    .then(data => console.log("Success:", data))
                    .catch(error => console.error("Error:", error));
                """,
                endpoint,
                method.toUpperCase(),
                authType != null && authType.equals("Bearer")
                        ? ",\n        \"Authorization\": \"Bearer YOUR_TOKEN\""
                        : "");
    }

    /**
     * Obtiene mejores prácticas
     */
    public List<Map<String, String>> getBestPractices(String apiType) {
        List<Map<String, String>> practices = new ArrayList<>();

        if ("REST".equals(apiType)) {
            practices.add(Map.of(
                    "title", "Usar HTTPS siempre",
                    "description", "Nunca enviar credenciales por HTTP sin cifrar",
                    "priority", "CRITICAL"));
            practices.add(Map.of(
                    "title", "Implementar rate limiting",
                    "description", "Limitar número de requests para evitar abusos",
                    "priority", "HIGH"));
            practices.add(Map.of(
                    "title", "Versionar la API",
                    "description", "Usar /v1/, /v2/ para mantener compatibilidad",
                    "priority", "HIGH"));
        }

        return practices;
    }

    // Métodos auxiliares

    private void addIssue(DiagnosticReport report, String severity,
            String category, String description, String impact) {
        Issue issue = Issue.builder()
                .severity(severity)
                .category(category)
                .description(description)
                .impact(impact)
                .build();
        report.getIssues().add(issue);
    }

    private String determineOverallStatus(DiagnosticReport report) {
        long highIssues = report.getIssues().stream()
                .filter(i -> "HIGH".equals(i.getSeverity()))
                .count();

        if (highIssues > 0)
            return "CRITICAL";
        if (!report.getIssues().isEmpty())
            return "WARNING";
        return "HEALTHY";
    }

    private void generateRecommendations(DiagnosticReport report) {
        if ("CRITICAL".equals(report.getOverallStatus())) {
            report.getRecommendations().add(
                    Recommendation.builder()
                            .priority("CRITICAL")
                            .title("Resolver problemas críticos primero")
                            .description("Hay problemas que impiden el funcionamiento")
                            .build());
        }
    }

    private String getHttpErrorImpact(int statusCode) {
        if (statusCode >= 400 && statusCode < 500) {
            return "Error del cliente - verificar configuración de la solicitud";
        } else if (statusCode >= 500) {
            return "Error del servidor - contactar al proveedor de la API";
        }
        return "Código inesperado";
    }
}