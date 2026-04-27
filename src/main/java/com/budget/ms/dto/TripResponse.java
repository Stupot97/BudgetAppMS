package com.budget.ms.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class TripResponse {
    private Long id;
    private String tripName;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal dailySpending;
    private String currency;
    private BigDecimal tripBudget;
}
