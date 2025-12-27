package com.apiguard.service;

import com.apiguard.dto.KnowledgeArticle;
import com.apiguard.dto.CodeExample;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Servicio de Base de Conocimiento
 * 
 * Gestiona artículos técnicos, guías y documentación
 */
@Service
@Slf4j
public class KnowledgeBaseService {

    private final ObjectMapper objectMapper;
    private List<KnowledgeArticle> articles;

    public KnowledgeBaseService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.articles = new ArrayList<>();
    }

    @PostConstruct
    public void init() {
        loadArticlesFromJson();
        if (articles.isEmpty()) {
            loadDefaultArticles();
        }
        log.info("Base de conocimiento inicializada con {} artículos", articles.size());
    }

    /**
     * Carga artículos desde archivo JSON
     */
    private void loadArticlesFromJson() {
        try {
            ClassPathResource resource = new ClassPathResource("data/knowledge-base.json");
            if (resource.exists()) {
                articles = objectMapper.readValue(
                        resource.getInputStream(),
                        new TypeReference<List<KnowledgeArticle>>() {
                        });
            }
        } catch (IOException e) {
            log.warn("No se pudo cargar knowledge-base.json, usando artículos por defecto");
        }
    }

    /**
     * Carga artículos por defecto si no existe el JSON
     */
    private void loadDefaultArticles() {
        articles = Arrays.asList(
                createArticle("api-rest-basics", "Introducción a APIs REST",
                        "INTEGRATION", "BEGINNER",
                        "Una API REST utiliza métodos HTTP estándar (GET, POST, PUT, DELETE) y retorna datos en JSON.",
                        "rest", "api", "http"),

                createArticle("auth-bearer-token", "Autenticación con Bearer Token",
                        "INTEGRATION", "BEGINNER",
                        "El esquema Bearer Token requiere el formato: Authorization: Bearer YOUR_TOKEN",
                        "authentication", "bearer", "security"),

                createArticle("http-401-fix", "Solución: Error 401 Unauthorized",
                        "TROUBLESHOOTING", "INTERMEDIATE",
                        "Error 401 indica fallo de autenticación. Verifica token, formato y permisos.",
                        "error", "401", "authentication"),

                createArticle("http-500-fix", "Solución: Error 500 Server Error",
                        "TROUBLESHOOTING", "INTERMEDIATE",
                        "Error 500 indica problema en servidor. Captura request completo y contacta soporte.",
                        "error", "500", "server"),

                createArticle("soap-integration", "Integración de Servicios SOAP",
                        "INTEGRATION", "ADVANCED",
                        "SOAP usa XML para mensajes. Necesitas archivo WSDL y generar clases cliente.",
                        "soap", "wsdl", "xml"),

                createArticle("api-best-practices", "Mejores Prácticas de APIs",
                        "BEST_PRACTICES", "INTERMEDIATE",
                        "Usa HTTPS, implementa rate limiting, versiona tu API y valida inputs.",
                        "best-practices", "security", "performance"),

                createArticle("rate-limiting", "Manejo de Rate Limiting",
                        "BEST_PRACTICES", "INTERMEDIATE",
                        "Al recibir 429, espera el tiempo indicado en Retry-After header.",
                        "rate-limit", "throttling", "429"),

                createArticle("json-validation", "Validación de Payloads JSON",
                        "INTEGRATION", "BEGINNER",
                        "Valida JSON contra schema, verifica tipos de datos y campos requeridos.",
                        "json", "validation", "schema"),

                createArticle("timeout-handling", "Manejo de Timeouts en APIs",
                        "TROUBLESHOOTING", "INTERMEDIATE",
                        "Configura connection timeout (5-10s) y read timeout (30-60s).",
                        "timeout", "retry", "resilience"),

                createArticle("api-versioning", "Versionado de APIs",
                        "BEST_PRACTICES", "ADVANCED",
                        "Usa URL path (/v1/, /v2/) para versionado claro y compatible.",
                        "versioning", "compatibility", "migration"));
    }

    private KnowledgeArticle createArticle(String id, String title,
            String category, String difficulty,
            String content, String... tags) {
        return KnowledgeArticle.builder()
                .id(id)
                .title(title)
                .category(category)
                .difficulty(difficulty)
                .content(content)
                .tags(Arrays.asList(tags))
                .views(0)
                .rating(4.5)
                .createdAt(java.time.LocalDateTime.now())
                .updatedAt(java.time.LocalDateTime.now())
                .build();
    }

    @Cacheable("articles")
    public List<KnowledgeArticle> getAllArticles() {
        return new ArrayList<>(articles);
    }

    public List<KnowledgeArticle> searchArticles(String query) {
        String lowerQuery = query.toLowerCase();
        return articles.stream()
                .filter(a -> a.getTitle().toLowerCase().contains(lowerQuery) ||
                        a.getContent().toLowerCase().contains(lowerQuery) ||
                        a.getTags().stream().anyMatch(t -> t.toLowerCase().contains(lowerQuery)))
                .collect(Collectors.toList());
    }

    public Optional<KnowledgeArticle> getArticleById(String id) {
        return articles.stream()
                .filter(a -> a.getId().equals(id))
                .findFirst();
    }

    public List<KnowledgeArticle> getArticlesByCategory(String category) {
        return articles.stream()
                .filter(a -> a.getCategory().equalsIgnoreCase(category))
                .collect(Collectors.toList());
    }

    public List<KnowledgeArticle> getArticlesByDifficulty(String difficulty) {
        return articles.stream()
                .filter(a -> a.getDifficulty().equalsIgnoreCase(difficulty))
                .collect(Collectors.toList());
    }

    public List<String> getCategories() {
        return articles.stream()
                .map(KnowledgeArticle::getCategory)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    public List<KnowledgeArticle> getPopularArticles(int limit) {
        return articles.stream()
                .sorted((a, b) -> Integer.compare(b.getViews(), a.getViews()))
                .limit(limit)
                .collect(Collectors.toList());
    }

    public void incrementViews(String id) {
        articles.stream()
                .filter(a -> a.getId().equals(id))
                .findFirst()
                .ifPresent(article -> article.setViews(article.getViews() + 1));
    }
}