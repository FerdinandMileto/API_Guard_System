package com.apiguard.controller;

import com.apiguard.dto.KnowledgeArticle;
import com.apiguard.service.KnowledgeBaseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador de Base de Conocimiento
 * 
 * Proporciona acceso a documentación técnica, guías de integración
 * y soluciones a problemas comunes
 */
@RestController
@RequestMapping("/api/knowledge")
@RequiredArgsConstructor
@Tag(name = "Knowledge Base", description = "Base de conocimiento técnico")
@CrossOrigin(origins = "*")
public class KnowledgeController {

    private final KnowledgeBaseService knowledgeService;

    /**
     * Obtiene todos los artículos de la base de conocimiento
     */
    @GetMapping("/articles")
    @Operation(summary = "Listar todos los artículos", description = "Obtiene la lista completa de artículos técnicos")
    public ResponseEntity<List<KnowledgeArticle>> getAllArticles() {
        return ResponseEntity.ok(knowledgeService.getAllArticles());
    }

    /**
     * Busca artículos por palabra clave
     */
    @GetMapping("/search")
    @Operation(summary = "Buscar artículos", description = "Busca artículos por palabra clave en título y contenido")
    public ResponseEntity<List<KnowledgeArticle>> searchArticles(
            @RequestParam String query) {
        return ResponseEntity.ok(knowledgeService.searchArticles(query));
    }

    /**
     * Obtiene un artículo específico por ID
     */
    @GetMapping("/articles/{id}")
    @Operation(summary = "Obtener artículo por ID", description = "Obtiene el detalle completo de un artículo")
    public ResponseEntity<KnowledgeArticle> getArticleById(
            @PathVariable String id) {
        return knowledgeService.getArticleById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Obtiene artículos por categoría
     */
    @GetMapping("/articles/category/{category}")
    @Operation(summary = "Artículos por categoría", description = "Filtra artículos por categoría específica")
    public ResponseEntity<List<KnowledgeArticle>> getArticlesByCategory(
            @PathVariable String category) {
        return ResponseEntity.ok(
                knowledgeService.getArticlesByCategory(category));
    }

    /**
     * Obtiene artículos por nivel de dificultad
     */
    @GetMapping("/articles/difficulty/{level}")
    @Operation(summary = "Artículos por dificultad", description = "Filtra artículos por nivel de dificultad")
    public ResponseEntity<List<KnowledgeArticle>> getArticlesByDifficulty(
            @PathVariable String level) {
        return ResponseEntity.ok(
                knowledgeService.getArticlesByDifficulty(level));
    }

    /**
     * Obtiene las categorías disponibles
     */
    @GetMapping("/categories")
    @Operation(summary = "Listar categorías", description = "Obtiene todas las categorías de artículos")
    public ResponseEntity<List<String>> getCategories() {
        return ResponseEntity.ok(knowledgeService.getCategories());
    }

    /**
     * Obtiene artículos más vistos
     */
    @GetMapping("/articles/popular")
    @Operation(summary = "Artículos populares", description = "Obtiene los artículos más consultados")
    public ResponseEntity<List<KnowledgeArticle>> getPopularArticles(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(
                knowledgeService.getPopularArticles(limit));
    }

    /**
     * Incrementa el contador de vistas de un artículo
     */
    @PostMapping("/articles/{id}/view")
    @Operation(summary = "Registrar vista", description = "Incrementa el contador de vistas de un artículo")
    public ResponseEntity<Void> incrementViews(@PathVariable String id) {
        knowledgeService.incrementViews(id);
        return ResponseEntity.ok().build();
    }
}