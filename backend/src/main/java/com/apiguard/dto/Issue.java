package com.apiguard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Issue {

    private String severity;
    private String category;
    private String description;
    private String impact;
    private String knowledgeBaseArticle;
}