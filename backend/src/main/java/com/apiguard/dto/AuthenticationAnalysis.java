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
public class AuthenticationAnalysis {

    private Boolean isValid;
    private String type;
    private String message;
    private List<String> problems;
    private String correctFormat;
}