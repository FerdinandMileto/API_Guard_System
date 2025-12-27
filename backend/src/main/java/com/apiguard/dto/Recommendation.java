package com.apiguard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Recommendation {

    private String priority;
    private String title;
    private String description;
    private String implementation;
    private String codeExample;
    private List<String> benefits;
}