package com.apiguard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeArticle {

    private String id;
    private String title;
    private String category;
    private String content;
    private List<String> tags;
    private String difficulty;
    private List<CodeExample> codeExamples;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer views;
    private Double rating;
}