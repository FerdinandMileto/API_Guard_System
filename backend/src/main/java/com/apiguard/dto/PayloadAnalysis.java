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
public class PayloadAnalysis {

    private Boolean isValidJson;
    private Boolean matchesSchema;
    private Integer sizeBytes;
    private List<String> missingFields;
    private List<String> unexpectedFields;
    private List<String> formatErrors;
}