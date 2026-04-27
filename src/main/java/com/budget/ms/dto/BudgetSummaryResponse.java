package com.budget.ms.dto;

import lombok.Data;
import lombok.NonNull;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class BudgetSummaryResponse {
    private Long id;
    private String name;
    private BigDecimal amountSpent;
    private BigDecimal amountRemaining;
    private String currency;
    private LocalDate startDate;
    private LocalDate endDate;
}
