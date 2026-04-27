package com.budget.ms.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class AnalyticsResponse {
    private BigDecimal actual;
    private BigDecimal predicted;
    private LocalDate date;
}
