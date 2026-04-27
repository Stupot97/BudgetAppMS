package com.budget.ms.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class SummaryResponse {
    private List<CategorySummaryResponse> categoryResponses;
    private BigDecimal total;
    private String currency;
}
